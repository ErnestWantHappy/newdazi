package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;

/** 会话密钥只以 AES-GCM 密文写入数据库，避免数据库备份直接泄露协作权限。 */
@Service
public class CollaborationSecretService
{
    private static final String PREFIX = "v1:";
    private final SecureRandom random = new SecureRandom();

    @Value("${collaboration.cryptpad.key-secret:}")
    private String keySecret;

    public String generateKey()
    {
        byte[] value = new byte[32];
        random.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public String encrypt(String plain)
    {
        if (StringUtils.isBlank(plain)) throw new ServiceException("协作密钥不能为空");
        try
        {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(result);
        }
        catch (Exception e)
        {
            throw new ServiceException("协作密钥加密失败");
        }
    }

    public String decrypt(String cipherText)
    {
        if (StringUtils.isBlank(cipherText)) throw new ServiceException("协作密钥不存在");
        if (!cipherText.startsWith(PREFIX)) throw new ServiceException("协作密钥格式无效");
        try
        {
            byte[] value = Base64.getUrlDecoder().decode(cipherText.substring(PREFIX.length()));
            if (value.length <= 12) throw new ServiceException("协作密钥格式无效");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, value, 0, 12));
            return new String(cipher.doFinal(value, 12, value.length - 12), StandardCharsets.UTF_8);
        }
        catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new ServiceException("协作密钥解密失败"); }
    }

    private SecretKeySpec key() throws Exception
    {
        if (StringUtils.isBlank(keySecret) || keySecret.length() < 32)
            throw new ServiceException("未配置至少32位 COLLABORATION_KEY_SECRET");
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(keySecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(digest, "AES");
    }
}
