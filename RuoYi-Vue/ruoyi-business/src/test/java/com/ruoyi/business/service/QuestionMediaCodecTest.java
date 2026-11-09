package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.business.domain.vo.StudentLessonQuestionVo;
import com.ruoyi.business.utils.QuestionMediaCodec;
import com.ruoyi.common.exception.ServiceException;

class QuestionMediaCodecTest {
    @Test void legacyNullRemainsEmpty() {assertTrue(QuestionMediaCodec.ids(QuestionMediaCodec.parse(null)).isEmpty());}
    @Test void invalidSchemaAndForeignSlotRejected() {
        QuestionMedia media=new QuestionMedia();media.schemaVersion=2;assertThrows(ServiceException.class,()->QuestionMediaCodec.normalize(media));
        media.schemaVersion=1;media.options.put("E",new java.util.ArrayList<>());assertThrows(ServiceException.class,()->QuestionMediaCodec.normalize(media));
    }
    @Test void numericImageIdsAndCountsEnforced() {
        QuestionMedia media=new QuestionMedia();media.stem.add(new QuestionMedia.ImageRef("https://host/a.png"));assertThrows(ServiceException.class,()->QuestionMediaCodec.normalize(media));
        media.stem.clear();for(int i=0;i<4;i++)media.stem.add(new QuestionMedia.ImageRef("1"));assertThrows(ServiceException.class,()->QuestionMediaCodec.normalize(media));
    }
    @Test void imageOnlyCorrectOptionAndStemAllowed() {
        BizQuestion q=new BizQuestion();q.setQuestionType("choice");q.setAnswer("A");q.setOptionB("文字");
        QuestionMedia media=new QuestionMedia();media.stem.add(new QuestionMedia.ImageRef("1"));media.options.get("A").add(new QuestionMedia.ImageRef("2"));q.setMedia(media);
        assertTrue(QuestionMediaCodec.validate(q,false).isEmpty());assertFalse(QuestionMediaCodec.validate(q,true).isEmpty());
    }
    @Test void studentPayloadHasMediaButNoInternalJsonOrAnswers() throws Exception {
        StudentLessonQuestionVo q=new StudentLessonQuestionVo();q.setMediaJson("{\"schemaVersion\":1,\"stem\":[{\"imageId\":\"1\"}]}");
        String json=QuestionMediaCodec.JSON.writeValueAsString(q);assertTrue(json.contains("imageId"));assertFalse(json.contains("mediaJson"));assertFalse(json.contains("answer"));
    }
    @Test void readonlyFingerprintDoesNotBreakDraftDeserialization() throws Exception {
        BizQuestion q=new BizQuestion();q.setQuestionType("choice");q.setOptionA("甲");q.setMedia(new QuestionMedia());
        String json=QuestionMediaCodec.write(q);BizQuestion copy=QuestionMediaCodec.JSON.readValue(json,BizQuestion.class);
        assertEquals(q.getSourceFingerprint(),copy.getSourceFingerprint());assertTrue(copy.isMediaTouched());
    }
    @Test void corruptedAndFakeImagesRejected() throws Exception {
        assertThrows(ServiceException.class,()->QuestionImageService.inspect("not a png".getBytes("UTF-8")));
        ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(80,40,BufferedImage.TYPE_INT_RGB),"png",out);
        QuestionImageService.ImageInfo info=QuestionImageService.inspect(out.toByteArray());assertEquals(80,info.width);assertEquals(40,info.height);
    }
    @Test void onlyContentAffectsHistoricalFingerprint() {
        BizQuestion q=new BizQuestion();q.setQuestionType("choice");q.setQuestionContent("题干");String original=q.getSourceFingerprint();
        q.setGrade(6L);q.setIsPublic("N");assertEquals(original,q.getSourceFingerprint());q.setOptionA("修改");assertNotEquals(original,q.getSourceFingerprint());
    }
}
