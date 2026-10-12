package com.ruoyi.business.service;

import com.ruoyi.business.controller.StudentHomeController;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BugReviewRegressionTest {
    @Test void returnedFileCanResubmitWhenClosedButOldLessonCannot() {
        StudentHomeController controller = new StudentHomeController();
        BizLessonAssignmentMapper mapper = mock(BizLessonAssignmentMapper.class);
        ClassroomTaskStateService states = mock(ClassroomTaskStateService.class);
        ReflectionTestUtils.setField(controller, "lessonAssignmentMapper", mapper);
        ReflectionTestUtils.setField(controller, "classroomTaskStateService", states);
        BizStudent student = new BizStudent(); student.setStudentId(7L);
        student.setEntryYear("2026"); student.setClassCode("1");
        BizLessonAssignment assignment = new BizLessonAssignment();
        assignment.setLessonId(9L); assignment.setPracticalOpen(0);
        when(mapper.selectBizLessonAssignmentList(any())).thenReturn(Collections.singletonList(assignment));
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(controller, "assertPracticalOpen", student, 3L, 9L, 10L));
        when(states.isReturned(7L, 9L, 10L)).thenReturn(true);
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(controller, "assertPracticalOpen", student, 3L, 9L, 10L));
        assignment.setLessonId(11L);
        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(controller, "assertPracticalOpen", student, 3L, 9L, 10L));
    }

    @Test void externalImagesDoNotProduceBrokenLocalProxyAddresses() {
        assertFalse(LoginPreviewService.isLocalImageSource("https://example.org/photo.jpg"));
        assertFalse(LoginPreviewService.isLocalImageSource("https://example.org/research-activity/images/a.jpg"));
        assertTrue(LoginPreviewService.isLocalImageSource("/profile/upload/research-activity/images/a.jpg"));
        assertTrue(LoginPreviewService.isLocalImageSource("/prod-api/common/resource/view?resource=a"));
    }

    @Test void activityPreviewSkipsExternalImageBeforeLocalImage() {
        com.ruoyi.business.mapper.LoginPreviewMapper mapper = mock(com.ruoyi.business.mapper.LoginPreviewMapper.class);
        when(mapper.selectTopicContentHtml(12L)).thenReturn(
                "<img src=\"https://example.org/first.jpg\"><img src=\"/profile/upload/research-activity/images/local.jpg\">");
        LoginPreviewService service = new LoginPreviewService(mapper);
        assertEquals("/business/login-preview/activity-image?topicId=12", service.extractFirstImage(12L));
    }

    @Test void sevenZipDownloadStillRejectsTraversal() {
        assertTrue(FileUtils.checkAllowDownload("/profile/upload/work.7z"));
        assertFalse(FileUtils.checkAllowDownload("/profile/../work.7z"));
        assertFalse(FileUtils.checkAllowDownload("/profile/work.exe"));
    }
}
