package com.ruoyi.business.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.dto.TeacherAiConfigRequest;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class TeacherAiConfigService
{
    public static final String DEFAULT_ENDPOINT = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    public static final String DEFAULT_MODEL = "qwen3.7-plus";
    private static final Set<String> MODELS = new HashSet<String>(Arrays.asList(DEFAULT_MODEL, "qwen3.6-flash"));
    @Autowired private PracticalAiGradingMapper mapper;
    @Autowired private PracticalAiCipherService cipherService;
    @Autowired private PracticalVisionGradingProvider provider;

    public TeacherAiConfig status(Long teacherUserId) { return mapper.selectConfig(teacherUserId); }
    public TeacherAiConfig statusForUpdate(Long teacherUserId) { return mapper.selectConfigForUpdate(teacherUserId); }
    public boolean isMasterKeyConfigured() { return cipherService.isConfigured(); }

    public TeacherAiConfig save(Long teacherUserId, TeacherAiConfigRequest request)
    {
        String apiKey = request == null ? null : StringUtils.trim(request.getApiKey());
        if (StringUtils.isBlank(apiKey) || apiKey.length() < 12 || apiKey.length() > 300)
            throw new ServiceException("请输入有效的阿里云百炼 API Key");
        String model = StringUtils.isBlank(request.getModelName()) ? DEFAULT_MODEL : request.getModelName().trim();
        if (!MODELS.contains(model)) throw new ServiceException("暂不支持该模型");
        TeacherAiConfig config = new TeacherAiConfig();
        config.setTeacherUserId(teacherUserId);
        config.setProviderCode("QWEN");
        config.setModelName(model);
        config.setEndpointUrl(DEFAULT_ENDPOINT);
        config.setApiKeyCiphertext(cipherService.encrypt(apiKey));
        config.setApiKeyHint("****" + apiKey.substring(apiKey.length() - 4));
        config.setEnabled(true);
        mapper.upsertConfig(config);
        return mapper.selectConfig(teacherUserId);
    }

    public String apiKey(TeacherAiConfig config)
    {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) throw new ServiceException("请先配置并启用 AI API Key");
        return cipherService.decrypt(config.getApiKeyCiphertext());
    }

    public void delete(Long teacherUserId) { mapper.deleteConfig(teacherUserId); }

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
