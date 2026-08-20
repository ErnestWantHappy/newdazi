package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IotPasscodeUtilTest
{
    @Test
    void shouldGenerateValidPasscodeWithoutAmbiguousCharacters()
    {
        for (int i = 0; i < 100; i++)
        {
            String passcode = IotPasscodeUtil.generatePasscode();
            assertEquals(6, passcode.length());
            // 确保排除 0, O, 1, I, L
            assertFalse(passcode.contains("0"), "口令不能包含 0");
            assertFalse(passcode.contains("O"), "口令不能包含 O");
            assertFalse(passcode.contains("1"), "口令不能包含 1");
            assertFalse(passcode.contains("I"), "口令不能包含 I");
            assertFalse(passcode.contains("L"), "口令不能包含 L");
            assertTrue(passcode.matches("^[2-9A-HJ-NP-Z]{6}$"), "口令必须符合易读字符集");
        }
    }

    @Test
    void shouldEncryptAndDecryptPasscodeCorrectly()
    {
        String secret = "MyTestSecret2026";
        String passcode = "K7P3M8";

        String ciphertext = IotPasscodeUtil.encrypt(passcode, secret);
        assertNotNull(ciphertext);
        assertFalse(ciphertext.contains(passcode), "密文不能包含明文");

        String decrypted = IotPasscodeUtil.decrypt(ciphertext, secret);
        assertEquals(passcode, decrypted, "解密后必须还原为原始 6 位口令");
    }

    @Test
    void shouldSortStudentNumbersNaturally()
    {
        List<String> studentNos = new ArrayList<>(Arrays.asList(
                "10", "2", "1", "01", "20", "9", "10A", "3B", "st99_363", "st99_001", "abc", "def"
        ));

        studentNos.sort(IotPasscodeUtil::compareStudentNo);

        // 验证 1, 2, 9, 10 的升序关系
        int idx1 = studentNos.indexOf("1");
        int idx2 = studentNos.indexOf("2");
        int idx9 = studentNos.indexOf("9");
        int idx10 = studentNos.indexOf("10");
        int idx20 = studentNos.indexOf("20");

        assertTrue(idx1 < idx2);
        assertTrue(idx2 < idx9);
        assertTrue(idx9 < idx10);
        assertTrue(idx10 < idx20);
    }
}
