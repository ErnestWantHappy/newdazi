package com.ruoyi.business.service;

import java.util.Arrays;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.TeacherAiProviderKey;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.dto.TeacherAiConfigRequest;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class TeacherAiConfigService
{
    public static final String DEFAULT_ENDPOINT = PracticalVisionModelCatalog.QWEN_ENDPOINT;
    public static final String DEFAULT_MODEL = "qwen3.8-max";
    @Autowired private PracticalAiGradingMapper mapper;
    @Autowired private PracticalAiCipherService cipherService;
    @Autowired private PracticalVisionGradingProvider provider;

    public TeacherAiConfig status(Long teacherUserId) { return mapper.selectConfig(teacherUserId); }
    public TeacherAiConfig statusForUpdate(Long teacherUserId) { return mapper.selectConfigForUpdate(teacherUserId); }
    public java.util.List<TeacherAiProviderKey> savedProviders(Long teacherUserId)
    {
        return mapper.selectProviderKeys(teacherUserId);
    }
    public boolean isMasterKeyConfigured() { return cipherService.isConfigured(); }

    @Transactional(rollbackFor = Exception.class)
    public TeacherAiConfig save(Long teacherUserId, TeacherAiConfigRequest request)
    {
        if (request == null) throw new ServiceException("配置内容不能为空");
        String apiKey = StringUtils.trim(request.getApiKey());
        boolean replaceKey = StringUtils.isNotBlank(apiKey);
        if (replaceKey && (apiKey.length() < 12 || apiKey.length() > 300))
            throw new ServiceException("请输入有效的模型厂商 API Key");
        String providerCode = StringUtils.isBlank(request.getProviderCode()) ? "QWEN"
                : request.getProviderCode().trim().toUpperCase(java.util.Locale.ROOT);
        String model = StringUtils.isBlank(request.getModelName()) ? DEFAULT_MODEL : request.getModelName().trim();
        String endpoint = PracticalVisionModelCatalog.endpoint(providerCode, model);
        TeacherAiConfig config = mapper.selectConfigForUpdate(teacherUserId);
        TeacherAiProviderKey providerKey = mapper.selectProviderKey(teacherUserId, providerCode);
        if (!replaceKey && (providerKey == null || StringUtils.isBlank(providerKey.getApiKeyCiphertext())))
            throw new ServiceException("该厂商尚未保存 API Key，请先输入");
        if (providerKey == null) providerKey = new TeacherAiProviderKey();
        providerKey.setTeacherUserId(teacherUserId);
        providerKey.setProviderCode(providerCode);
        providerKey.setModelName(model);
        if (replaceKey)
        {
            providerKey.setApiKeyCiphertext(cipherService.encrypt(apiKey));
            providerKey.setApiKeyHint("****" + apiKey.substring(apiKey.length() - 4));
        }
        mapper.upsertProviderKey(providerKey);
        if (config == null) config = new TeacherAiConfig();
        config.setTeacherUserId(teacherUserId);
        config.setProviderCode(providerCode);
        config.setModelName(model);
        config.setEndpointUrl(endpoint);
        // 旧配置表仍表示当前厂商；任务续跑从厂商表取凭据，避免切换模型后漂移。
        config.setApiKeyCiphertext(providerKey.getApiKeyCiphertext());
        config.setApiKeyHint(providerKey.getApiKeyHint());
        config.setEnabled(true);
        mapper.upsertConfig(config);
        return mapper.selectConfig(teacherUserId);
    }

    public String apiKey(TeacherAiConfig config)
    {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) throw new ServiceException("请先配置并启用 AI API Key");
        return cipherService.decrypt(config.getApiKeyCiphertext());
    }

    public TeacherAiConfig configForJob(Long teacherUserId, String providerCode, String modelName)
    {
        String endpoint = PracticalVisionModelCatalog.endpoint(providerCode, modelName);
        TeacherAiProviderKey key = mapper.selectProviderKey(teacherUserId, providerCode);
        if (key == null || StringUtils.isBlank(key.getApiKeyCiphertext()))
            throw new ServiceException("任务所用的 " + providerCode + " API Key 已删除，无法继续批改");
        TeacherAiConfig config = new TeacherAiConfig();
        config.setTeacherUserId(teacherUserId);
        config.setProviderCode(providerCode);
        config.setModelName(modelName);
        config.setEndpointUrl(endpoint);
        config.setApiKeyCiphertext(key.getApiKeyCiphertext());
        config.setApiKeyHint(key.getApiKeyHint());
        config.setEnabled(true);
        return config;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long teacherUserId)
    {
        mapper.deleteConfig(teacherUserId);
        mapper.deleteProviderKeys(teacherUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(Long teacherUserId, String providerCode)
    {
        PracticalVisionModelCatalog.assertProvider(providerCode);
        TeacherAiConfig active = mapper.selectConfigForUpdate(teacherUserId);
        if (active != null && providerCode.equals(active.getProviderCode())) mapper.deleteConfig(teacherUserId);
        mapper.deleteProviderKey(teacherUserId, providerCode);
    }

    /** 用无学生信息的本地测试图验证 Key、网络、模型与结构化输出。 */
    public void testConnection(Long teacherUserId)
    {
        TeacherAiConfig config = status(teacherUserId);
        File image = null;
        try
        {
            image = File.createTempFile("practical-ai-check-", ".jpg");
            BufferedImage canvas = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = canvas.createGraphics();
            try
            {
                graphics.setColor(Color.WHITE); graphics.fillRect(0, 0, 640, 360);
                graphics.setColor(Color.BLACK); graphics.drawString("AI CONNECTION TEST", 220, 180);
            }
            finally { graphics.dispose(); }
            ImageIO.write(canvas, "jpg", image);

            PracticalRubricSnapshot rubric = new PracticalRubricSnapshot();
            rubric.setQuestionContent("测试图片中是否能看到英文 AI CONNECTION TEST");
            rubric.setQuestionScore(1);
            PracticalScoringItemVo item = new PracticalScoringItemVo();
            item.setItemId(1L); item.setItemName("能识别测试文字"); item.setMaxScore(1);
            PracticalAiGradingInput input = new PracticalAiGradingInput();
            input.setRubric(rubric); input.setScoringItems(Arrays.asList(item)); input.setPageImages(Arrays.asList(image));
            provider.grade(config, apiKey(config), input);
        }
        catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new ServiceException("AI 连通性测试失败"); }
        finally { if (image != null) image.delete(); }
    }
}
