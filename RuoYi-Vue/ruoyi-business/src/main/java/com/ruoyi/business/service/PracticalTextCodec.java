package com.ruoyi.business.service;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import com.ruoyi.common.exception.ServiceException;

/** 文本作品有独立大小上限；只解码，不把 TXT 中的标签当网页执行。 */
public final class PracticalTextCodec {
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private PracticalTextCodec() { }

    public static String decode(byte[] bytes) {
        if (bytes.length > MAX_BYTES) throw new ServiceException("TXT/HTML 文件不能超过2 MiB");
        int offset = 0;
        String charset = "UTF-8";
        if (bytes.length >= 3 && bytes[0] == (byte)0xef && bytes[1] == (byte)0xbb && bytes[2] == (byte)0xbf) offset = 3;
        else if (bytes.length >= 2 && bytes[0] == (byte)0xff && bytes[1] == (byte)0xfe) { charset = "UTF-16LE"; offset = 2; }
        else if (bytes.length >= 2 && bytes[0] == (byte)0xfe && bytes[1] == (byte)0xff) { charset = "UTF-16BE"; offset = 2; }
        String text;
        try { text = decode(bytes, offset, charset); }
        catch (Exception e) {
            if (offset != 0) throw new ServiceException("文本编码无法识别，请另存为UTF-8");
            try { text = decode(bytes, 0, "GB18030"); }
            catch (Exception ignored) { throw new ServiceException("文本编码无法识别，请另存为UTF-8"); }
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 && c != '\n' && c != '\r' && c != '\t')
                throw new ServiceException("文件含有二进制内容，请上传真实文本文件");
        }
        return text;
    }

    private static String decode(byte[] bytes, int offset, String charset) throws Exception {
        return Charset.forName(charset).newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset)).toString();
    }
}
