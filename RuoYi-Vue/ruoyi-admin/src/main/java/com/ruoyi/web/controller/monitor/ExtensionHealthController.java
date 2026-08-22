package com.ruoyi.web.controller.monitor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.business.config.IotMqttProperties;
import com.ruoyi.business.judge.Judge0Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.service.CollaborationRoomService;
import com.ruoyi.business.service.IotMqttReceiver;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;

/**
 * 扩展机（10.52.1.129）一体化健康看板聚合接口。
 *
 * 为什么单独建接口：现有诊断中心只覆盖 123 主机自身（JVM/Redis/Druid/LibreOffice），
 * 而 Judge0 判题、CryptPad 协作、EMQX 物联网都在扩展机上，课堂中任一服务卡死
 * 教研员必须在一屏内看到，而不是登录服务器逐个 curl。
 *
 * 设计约束：所有探针并行执行且单个失败只降级为 down+error，绝不让一个探针拖垮整个接口；
 * 探针超时远小于前端轮询间隔；错误信息只含原因不含凭据。
 */
@RestController
@RequestMapping("/monitor/extension")
public class ExtensionHealthController
{
    private static final Logger log = LoggerFactory.getLogger(ExtensionHealthController.class);

    /** 单探针超时：判题/EMQX 正常应在百毫秒级，2 秒足够区分“慢”和“挂”。 */
    private static final long PROBE_TIMEOUT_MS = 2000L;

    @Autowired private Judge0Properties judge0Properties;
    @Autowired private IotMqttProperties iotMqttProperties;
    @Autowired private IotMqttReceiver iotMqttReceiver;
    @Autowired private CollaborationRoomService collaborationRoomService;

    /** 探针专用短超时客户端，与业务调用隔离，避免占用判题轮询的连接配置。 */
    private volatile RestTemplate probeRestTemplate;

    /** 判题执行器排队情况是 Judge0 管道压力的直接信号（提交在 123 侧排队等 129 判题）。 */
    @Autowired(required = false)
    @Qualifier("judge0Executor")
    private org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor judgeExecutor;

    /**
     * 129 硬件探针命令：经免密 SSH 拉取 hwprobe.sh 的 JSON 输出。
     * 留空（默认）则探针禁用；生产由 NSSM 环境变量 MONITOR_HOST129_SSH_COMMAND 注入。
     */
    @Value("${monitor.host129.ssh-command:${MONITOR_HOST129_SSH_COMMAND:}}")
    private String host129SshCommand;

    /** 硬件信息变化缓慢，缓存 60 秒，避免每次看板刷新都打一次 SSH。 */
    private static final long HOST129_CACHE_MS = 60_000L;
    private static final long HOST129_PROBE_TIMEOUT_MS = 6000L;
    private volatile long host129CacheAt;
    private volatile Map<String, Object> host129Cache;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @GetMapping("/health")
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    public AjaxResult health()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        long start = System.currentTimeMillis();

        CompletableFuture<Map<String, Object>> hostHw =
                CompletableFuture.supplyAsync(this::probeHost129);
        CompletableFuture<Map<String, Object>> judge0 =
                CompletableFuture.supplyAsync(this::probeJudge0);
        CompletableFuture<Map<String, Object>> emqx =
                CompletableFuture.supplyAsync(this::probeEmqx);
        CompletableFuture<Map<String, Object>> cryptpad =
                CompletableFuture.supplyAsync(this::probeCryptPad);
        data.put("judge0", joinQuietly(judge0));
        data.put("emqx", joinQuietly(emqx));
        data.put("cryptpad", joinQuietly(cryptpad));

        // MQTT 平台接收器是本进程内连接状态，直接读内存即可
        Map<String, Object> mqtt = new LinkedHashMap<>();
        boolean mqttConnected = iotMqttReceiver.isConnected();
        mqtt.put("status", mqttConnected ? "up" : "down");
        mqtt.put("enabled", iotMqttProperties.isEnabled());
        mqtt.put("brokerUrl", maskBroker(iotMqttProperties.getBrokerUrl()));
        mqtt.put("subscription", iotMqttProperties.getSubscription());
        data.put("mqttReceiver", mqtt);

        Map<String, Object> hostHardware = joinQuietly(hostHw);
        data.put("hostHardware", hostHardware);
        data.put("systemInfo", buildSystemInfo((Map<String, Object>) data.get("emqx"), hostHardware));
        data.put("latencyMs", System.currentTimeMillis() - start);
        data.put("checkedAt", new java.util.Date());
        return AjaxResult.success(data);
    }

    /**
     * 129 主机资源聚合：CPU 负载与内存来自 EMQX 管理接口的节点指标；
     * 判题管道压力来自 123 侧判题执行器队列。磁盘/GPU 需在 129 部署探针后接入，
     * 明确返回 available=false 而不是伪造数据。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildSystemInfo(Map<String, Object> emqx, Map<String, Object> hw)
    {
        Map<String, Object> info = new LinkedHashMap<>();
        boolean emqxUp = emqx != null && "up".equals(emqx.get("status"));
        boolean hwUp = hw != null && Boolean.TRUE.equals(hw.get("available"));
        info.put("source", hwUp ? "SSH_PROBE+EMQX" : (emqxUp ? "EMQX_MANAGEMENT_API" : "UNAVAILABLE"));

        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("load1", emqxUp ? emqx.get("cpuLoad1") : null);
        cpu.put("load5", emqxUp ? emqx.get("cpuLoad5") : null);
        cpu.put("load15", emqxUp ? emqx.get("cpuLoad15") : null);
        info.put("cpu", cpu);

        // 硬件探针可用时补充静态属性：CPU 型号/物理核数、操作系统、主机名、IP、Node/Java 版本
        if (hwUp)
        {
            info.put("hostname", hw.get("hostname"));
            info.put("ip", hw.get("ip"));
            info.put("os", hw.get("os"));
            info.put("kernel", hw.get("kernel"));
            info.put("nodeVersion", hw.get("nodeVersion"));
            info.put("cryptpadNodeVersion", hw.get("cryptpadNodeVersion"));
            info.put("javaVersion", hw.get("javaVersion"));
        }

        Number memoryUsed = emqxUp ? asNumber(emqx.get("memoryUsed"))
                : (hwUp ? asNumber(subMap(hw, "memory").get("usedBytes")) : null);
        Number memoryTotal = emqxUp ? asNumber(emqx.get("memoryTotal"))
                : (hwUp ? asNumber(subMap(hw, "memory").get("totalBytes")) : null);
        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("usedBytes", memoryUsed == null ? null : memoryUsed.longValue());
        memory.put("totalBytes", memoryTotal == null ? null : memoryTotal.longValue());
        memory.put("usagePercent", memoryUsed != null && memoryTotal != null && memoryTotal.longValue() > 0
                ? Math.round(memoryUsed.doubleValue() * 1000D / memoryTotal.doubleValue()) / 10D
                : null);
        info.put("memory", memory);

        Map<String, Object> disk = new LinkedHashMap<>();
        Object cpuModel = subMap(hw, "cpu").get("model");
        Object cpuCores = subMap(hw, "cpu").get("cores");
        if (cpuModel != null) { cpu.put("model", cpuModel); }
        if (cpuCores != null) { cpu.put("cores", cpuCores); }
        if (hwUp)
        {
            disk.put("available", true);
            disk.put("disks", hw.get("disks"));
        }
        else
        {
            disk.put("available", false);
            disk.put("note", "129 未部署硬件探针或探针不可达");
        }
        info.put("disk", disk);

        Map<String, Object> gpu = new LinkedHashMap<>();
        String gpuName = hwUp && hw.get("gpu") != null ? String.valueOf(hw.get("gpu")) : "";
        gpu.put("available", !gpuName.isEmpty());
        if (!gpuName.isEmpty()) { gpu.put("name", gpuName); } else { gpu.put("note", "129 无 GPU 或未接入采集"); }
        info.put("gpu", gpu);

        Map<String, Object> judgePipeline = new LinkedHashMap<>();

        if (judgeExecutor != null)
        {
            ThreadPoolExecutor executor = judgeExecutor.getThreadPoolExecutor();
            judgePipeline.put("queueSize", executor.getQueue().size());
            judgePipeline.put("activeCount", executor.getActiveCount());
            judgePipeline.put("completedTaskCount", executor.getCompletedTaskCount());
            judgePipeline.put("maxPoolSize", executor.getMaximumPoolSize());
        }
        else
        {
            judgePipeline.put("available", false);
        }
        info.put("judgePipeline", judgePipeline);
        return info;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> subMap(Map<String, Object> parent, String key)
    {
        Object v = parent == null ? null : parent.get(key);
        return v instanceof Map ? (Map<String, Object>) v : new LinkedHashMap<>();
    }

    /**
     * 129 硬件探针：执行配置的免密 SSH 命令拉取 hwprobe.sh 的 JSON。
     * 结果缓存 60 秒；未配置命令、超时、非零退出或 JSON 解析失败一律降级为 available=false。
     */
    private Map<String, Object> probeHost129()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (host129SshCommand == null || host129SshCommand.trim().isEmpty())
        {
            result.put("available", false);
            result.put("error", "未配置 monitor.host129.ssh-command");
            return result;
        }
        Map<String, Object> cached = host129Cache;
        if (cached != null && System.currentTimeMillis() - host129CacheAt < HOST129_CACHE_MS)
        {
            return cached;
        }
        try
        {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", host129SshCommand.trim());
            pb.redirectErrorStream(false);
            Process p = pb.start();
            boolean done = p.waitFor(HOST129_PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!done)
            {
                p.destroyForcibly();
                return unavailableHost("探针超时");
            }
            String out = readAll(p.getInputStream());
            int rc = p.exitValue();
            if (rc != 0 || out.trim().isEmpty() || !out.trim().startsWith("{"))
            {
                return unavailableHost("探针退出码 " + rc);
            }
            JsonNode node = objectMapper.readTree(out.trim());
            Map<String, Object> parsed = objectMapper.convertValue(node, Map.class);
            parsed.put("available", true);
            parsed.put("fetchedAt", new java.util.Date());
            host129Cache = parsed;
            host129CacheAt = System.currentTimeMillis();
            return parsed;
        }
        catch (Exception e)
        {
            return unavailableHost(safeMessage(e));
        }
    }

    private Map<String, Object> unavailableHost(String reason)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", false);
        result.put("error", reason);
        return result;
    }

    private String readAll(InputStream in)
    {
        try
        {
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int n;
            while ((n = in.read(chunk)) != -1 && buf.size() < 64 * 1024)
            {
                buf.write(chunk, 0, n);
            }
            in.close();
            return new String(buf.toByteArray(), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            return "";
        }
    }
    private Number asNumber(Object value)
    {
        return value instanceof Number ? (Number) value : null;
    }
    /** 解析 EMQX 的可读容量串（如 “31.34G”“512M”“1024K”）为字节；非数字返回 null。 */
    private Long parseSizeToBytes(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim().toUpperCase();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("^([0-9]+(?:\\.[0-9]+)?)\\s*([KMGT]?)B?$").matcher(text);
        if (!matcher.matches())
        {
            return null;
        }
        double number = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2);
        long multiplier = unit.isEmpty() ? 1L
                : "K".equals(unit) ? 1024L
                : "M".equals(unit) ? 1024L * 1024
                : "G".equals(unit) ? 1024L * 1024 * 1024
                : 1024L * 1024 * 1024 * 1024;
        return Math.round(number * multiplier);
    }

    private void putIfNotNull(Map<String, Object> result, String key, Object value)
    {
        if (value != null)
        {
            result.put(key, value);
        }
    }

    private Map<String, Object> probeJudge0()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        String baseUrl = judge0Properties.getBaseUrl();
        if (StringUtils.isEmpty(baseUrl) || StringUtils.isEmpty(judge0Properties.getAuthToken()))
        {
            result.put("status", "unconfigured");
            result.put("mode", judge0Properties.getMode());
            return result;
        }
        long start = System.currentTimeMillis();
        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.set(judge0Properties.getAuthHeader(), judge0Properties.getAuthToken());
            String url = trimTrailingSlash(baseUrl) + "/about";
            Map body = probeRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();
            result.put("status", "up");
            result.put("version", body == null ? null : body.get("version"));
            result.put("latencyMs", System.currentTimeMillis() - start);
        }
        catch (Exception e)
        {
            result.put("status", "down");
            result.put("error", safeMessage(e));
            result.put("latencyMs", System.currentTimeMillis() - start);
        }
        return result;
    }

    /** EMQX 管理面探测：GET /nodes 列出节点即视为健康；未配管理密钥则报 unconfigured。 */
    private Map<String, Object> probeEmqx()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        String apiUrl = iotMqttProperties.getEmqxApiUrl();
        boolean hasApiKey = StringUtils.isNotEmpty(iotMqttProperties.getEmqxApiKey())
                && StringUtils.isNotEmpty(iotMqttProperties.getEmqxApiSecret());
        boolean hasToken = StringUtils.isNotEmpty(iotMqttProperties.getEmqxApiToken());
        if (StringUtils.isEmpty(apiUrl) || (!hasApiKey && !hasToken))
        {
            result.put("status", "unconfigured");
            return result;
        }
        long start = System.currentTimeMillis();
        try
        {
            HttpHeaders headers = new HttpHeaders();
            if (hasApiKey)
            {
                // EMQX v5 管理 API 使用 API Key/Secret 的 Basic 认证
                String pair = iotMqttProperties.getEmqxApiKey() + ":" + iotMqttProperties.getEmqxApiSecret();
                headers.set("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8)));
            }
            else
            {
                headers.set("Authorization", "Bearer " + iotMqttProperties.getEmqxApiToken());
            }
            String url = trimTrailingSlash(apiUrl) + "/nodes";
            List body = probeRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class).getBody();
            result.put("status", "up");
            result.put("nodeCount", body == null ? 0 : body.size());
            if (body != null && !body.isEmpty() && body.get(0) instanceof Map)
            {
                Map node = (Map) body.get(0);
                Object connections = node.get("connections");
                if (connections != null) result.put("connections", connections);
                Object liveConnections = node.get("live_connections");
                if (liveConnections != null) result.put("liveConnections", liveConnections);
                Object uptime = node.get("uptime");
                if (uptime != null) result.put("uptime", uptime);
                // EMQX v5.8 实测字段：load1/load5/load15（主机负载）、memory_used/total 为“31.34G”类可读串
                putIfNotNull(result, "cpuLoad1", node.get("load1"));
                putIfNotNull(result, "cpuLoad5", node.get("load5"));
                putIfNotNull(result, "cpuLoad15", node.get("load15"));
                Object memoryTotal = node.get("memory_total");
                Object memoryUsed = node.get("memory_used");
                putIfNotNull(result, "memoryTotalStr", memoryTotal);
                putIfNotNull(result, "memoryUsedStr", memoryUsed);
                Long totalBytes = parseSizeToBytes(memoryTotal);
                Long usedBytes = parseSizeToBytes(memoryUsed);
                if (totalBytes != null) result.put("memoryTotal", totalBytes);
                if (usedBytes != null) result.put("memoryUsed", usedBytes);
                Object nodeName = node.get("node");
                if (nodeName != null) result.put("node", nodeName);
            }
        }
        catch (Exception e)
        {
            result.put("status", "down");
            result.put("error", safeMessage(e));
            result.put("latencyMs", System.currentTimeMillis() - start);
        }
        return result;
    }

    /** CryptPad 复用协作服务的健康检查（配置完整性 + 存储可写），本地检查毫秒级返回。 */
    private Map<String, Object> probeCryptPad()
    {
        try
        {
            Map<String, Object> health = collaborationRoomService.health();

            // 统一状态语义：ready=true 即 up
            Object ready = health.get("ready");
            health.put("status", Boolean.TRUE.equals(ready) ? "up" : "degraded");
            return health;
        }
        catch (Exception e)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "down");
            result.put("error", safeMessage(e));
            return result;
        }
    }

    private Map<String, Object> joinQuietly(CompletableFuture<Map<String, Object>> future)
    {
        try
        {
            return future.get(PROBE_TIMEOUT_MS + 1000L, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "down");
            result.put("error", "探针超时或被拒绝");
            return result;
        }
    }

    private RestTemplate probeRestTemplate()
    {
        if (probeRestTemplate == null)
        {
            synchronized (this)
            {
                if (probeRestTemplate == null)
                {
                    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
                    f.setConnectTimeout((int) PROBE_TIMEOUT_MS);
                    f.setReadTimeout((int) PROBE_TIMEOUT_MS);
                    probeRestTemplate = new RestTemplate(f);
                }
            }
        }
        return probeRestTemplate;
    }

    private String trimTrailingSlash(String value)
    {
        return value.replaceAll("/+$", "");
    }

    /** broker 地址去掉可能内嵌的凭据段（tcp://user:pass@host 形式）。 */
    private String maskBroker(String brokerUrl)
    {
        if (brokerUrl == null) return null;
        int at = brokerUrl.indexOf('@');
        int scheme = brokerUrl.indexOf("://");
        if (at > scheme && scheme >= 0)
        {
            return brokerUrl.substring(0, scheme + 3) + "***@" + brokerUrl.substring(at + 1);
        }
        return brokerUrl;
    }

    private String safeMessage(Exception e)
    {
        String msg = e.getMessage();
        if (msg == null) msg = e.getClass().getSimpleName();
        // 只保留首行原因，避免堆栈与 URL 查询串里的敏感内容进看板
        int nl = msg.indexOf('\n');
        String first = nl > 0 ? msg.substring(0, nl) : msg;
        return first.length() > 300 ? first.substring(0, 300) : first;
    }
}
