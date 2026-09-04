package com.ruoyi.business.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;

/** 使用部署环境主密钥对教师 API Key 做带认证加密。 */
@Service
public class PracticalAiCipherService
{
    private static final SecureRandom RANDOM = new SecureRandom();
    @Value("${business.practical-ai.master-key:${PRACTICAL_AI_MASTER_KEY:}}")
    private String masterKey;

    public boolean isConfigured() { return masterKey != null && masterKey.trim().length() >= 24; }

    public String encrypt(String plaintext)
    {
        if (!isConfigured()) throw new ServiceException("服务器尚未配置 AI 密钥加密主密钥");
        try
        {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted);
            return "v1:" + Base64.getEncoder().encodeToString(buffer.array());
        }
        catch (Exception e)
        {
            throw new ServiceException("AI API Key 加密失败");
        }
    }

    public String decrypt(String ciphertext)
    {
        if (!isConfigured()) throw new ServiceException("服务器尚未配置 AI 密钥加密主密钥");
        if (ciphertext == null || !ciphertext.startsWith("v1:")) throw new ServiceException("AI API Key 密文格式无效");
        try
        {
            byte[] payload = Base64.getDecoder().decode(ciphertext.substring(3));
            if (payload.length < 29) throw new IllegalArgumentException("ciphertext");
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException("AI API Key 无法解密，请教师重新保存");
        }
    }

    private SecretKeySpec key() throws Exception
    {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(masterKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(digest, "AES");
    }
}
