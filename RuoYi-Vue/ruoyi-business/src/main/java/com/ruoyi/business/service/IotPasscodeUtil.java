package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 物联网 6 位易读课堂口令生成、AES-256-GCM 加解密与学号自然排序工具类。
 */
public final class IotPasscodeUtil
{
    // 31 个无歧义字符（剔除 0, O, 1, I, L）
    private static final char[] PASSCODE_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    private IotPasscodeUtil() { }

    /**
     * 生成 6 位易读口令（如 K7P3M8）
     */
    public static String generatePasscode()
    {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++)
        {
            sb.append(PASSCODE_CHARS[RANDOM.nextInt(PASSCODE_CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * 计算口令哈希（BCrypt）
     */
    public static String hashPasscode(String passcode)
    {
        if (passcode == null || passcode.trim().isEmpty()) return null;
        return BCRYPT.encode(passcode.trim());
    }

    /**
     * 对称加密（AES-256-GCM），防止数据库备份泄露明文口令
     */
    public static String encrypt(String plaintext, String customSecret)
    {
        if (plaintext == null || plaintext.trim().isEmpty()) return null;
        try
        {
            byte[] keyBytes = deriveKey(customSecret);
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("口令加密失败", e);
        }
    }

    /**
     * 对称解密（AES-256-GCM）
     */
    public static String decrypt(String ciphertext, String customSecret)
    {
        if (ciphertext == null || ciphertext.trim().isEmpty()) return null;
        try
        {
            byte[] combined = Base64.getDecoder().decode(ciphertext.trim());
            if (combined.length < 16) return null;

            byte[] keyBytes = deriveKey(customSecret);
            byte[] iv = new byte[12];
            System.arraycopy(combined, 0, iv, 0, 12);

            byte[] cipherBytes = new byte[combined.length - 12];
            System.arraycopy(combined, 12, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 学号自然排序比较器：优先提取第一段连续数字按数值大小排序，无数字或数字相同时按字典序排序
     */
    public static int compareStudentNo(String a, String b)
    {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        String sa = a.trim();
        String sb = b.trim();

        Matcher ma = DIGIT_PATTERN.matcher(sa);
        Matcher mb = DIGIT_PATTERN.matcher(sb);
        boolean hasA = ma.find();
        boolean hasB = mb.find();

        if (hasA && hasB)
        {
            try
            {
                java.math.BigInteger na = new java.math.BigInteger(ma.group());
                java.math.BigInteger nb = new java.math.BigInteger(mb.group());
                int comp = na.compareTo(nb);
                if (comp != 0) return comp;
            }
            catch (Exception ignored) { }
        }
        else if (hasA)
        {
            return -1; // 带数字排在纯字母前
        }
        else if (hasB)
        {
            return 1;
        }

        return sa.compareToIgnoreCase(sb);
    }

    private static byte[] deriveKey(String secret) throws Exception
    {
        if (secret == null || secret.trim().isEmpty())
            throw new IllegalArgumentException("IoT 口令加密密钥未配置");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(secret.trim().getBytes(StandardCharsets.UTF_8));
    }
}
