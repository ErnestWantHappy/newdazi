package com.ruoyi.business.judge;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.ruoyi.common.exception.ServiceException;

@Component("judge0HttpClient")
public class Judge0HttpClient implements Judge0Client {
    @Autowired private Judge0Properties properties;
    @Override public Judge0Result submit(Judge0Request request) {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().trim().isEmpty() || properties.getAuthToken() == null || properties.getAuthToken().trim().isEmpty()) throw new ServiceException("Judge0 服务或认证令牌未配置");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("source_code", request.getSourceCode()); body.put("language_id", properties.getPythonLanguageId());
        body.put("stdin", request.getStdin()); body.put("expected_output", request.getExpectedOutput());
        body.put("cpu_time_limit", request.getCpuTimeLimit()); body.put("cpu_extra_time", 0.5); body.put("wall_time_limit", request.getCpuTimeLimit() + 1D);
        body.put("memory_limit", request.getMemoryLimitKb()); body.put("max_processes_and_or_threads", request.getMaxProcesses());
        body.put("max_file_size", request.getMaxFileSizeKb()); body.put("max_output_size", request.getMaxOutputKb()); body.put("enable_network", false);
        Map response = restTemplate().postForObject(url("/submissions?base64_encoded=false&wait=false"), new HttpEntity<Map<String, Object>>(body, headers()), Map.class);
        return toResult(response);
    }
    @Override public Judge0Result poll(String token) { return toResult(restTemplate().exchange(url("/submissions/" + token + "?base64_encoded=false"), HttpMethod.GET, new HttpEntity<Void>(headers()), Map.class).getBody()); }
    // RestTemplate 线程安全；复用单例避免每次判题都重建连接（整班集中交编程题时显著减少握手开销）。
    private volatile RestTemplate cachedRestTemplate;

    private RestTemplate restTemplate() {
        if (cachedRestTemplate == null) {
            synchronized (this) {
                if (cachedRestTemplate == null) {
                    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
                    f.setConnectTimeout(properties.getConnectTimeoutMs());
                    f.setReadTimeout(properties.getReadTimeoutMs());
                    cachedRestTemplate = new RestTemplate(f);
                }
            }
        }
        return cachedRestTemplate;
    }
    private HttpHeaders headers() { HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); h.set(properties.getAuthHeader(), properties.getAuthToken()); return h; }
    private String url(String path) { return properties.getBaseUrl().replaceAll("/+$", "") + path; }
    private Judge0Result toResult(Map value) {
        if (value == null) throw new ServiceException("Judge0 返回为空");
        Judge0Result r = new Judge0Result(); r.setToken(string(value.get("token"))); r.setStdout(string(value.get("stdout"))); r.setStderr(string(value.get("stderr")));
        r.setCompileOutput(string(value.get("compile_output"))); r.setMessage(string(value.get("message"))); r.setTimeSeconds(number(value.get("time"))); r.setMemoryKb(integer(value.get("memory")));
        Object status = value.get("status"); if (status instanceof Map) { r.setStatusId(integer(((Map) status).get("id"))); r.setStatusDescription(string(((Map) status).get("description"))); }
        return r;
    }
    private String string(Object v) { return v == null ? null : String.valueOf(v); }
    private Integer integer(Object v) { try { return v == null ? null : Integer.valueOf(String.valueOf(v)); } catch (NumberFormatException e) { return null; } }
    private Double number(Object v) { try { return v == null ? null : Double.valueOf(String.valueOf(v)); } catch (NumberFormatException e) { return null; } }
}
