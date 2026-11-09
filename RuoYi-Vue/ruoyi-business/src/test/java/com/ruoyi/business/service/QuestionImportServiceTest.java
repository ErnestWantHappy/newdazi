package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.business.service.QuestionWorkbookReader.RowDraft;
import com.ruoyi.business.service.QuestionWorkbookReader.PendingImage;
import com.ruoyi.business.utils.QuestionMediaCodec;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

class QuestionImportServiceTest {
    private QuestionImportService service;
    private QuestionMediaStore store;
    private JdbcTemplate jdbc;
    private QuestionImageService images;
    private QuestionImportService.Patch patch;
    private RowDraft draft;
    private Map<String,Object> item;
    private final List<String> savedDrafts = new ArrayList<>();

    @BeforeEach
    void setup() {
        SysUser user = new SysUser(); user.setUserId(9L); user.setUserName("test");
        LoginUser login = new LoginUser(9L, 2L, user, new HashSet<>());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
        jdbc = mock(JdbcTemplate.class);
        store = mock(QuestionMediaStore.class);
        images = mock(QuestionImageService.class);
        when(store.jdbc()).thenReturn(jdbc);
        when(store.count(anyString(), any())).thenReturn(0L);
        when(jdbc.update(startsWith("insert ignore into biz_question_import_batch"), any(), any(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(), any(), any())).thenAnswer(invocation -> {
            // String.valueOf 有 char[] 重载，显式取 String，避免泛型推断把 SQL 参数误转成 char[]。
            if (((String) invocation.getArgument(0)).startsWith("update biz_question_import_item set draft_json=?")) {
                savedDrafts.add((String) invocation.getArgument(1));
            }
            return 1;
        });
        service = spy(new QuestionImportService());
        ReflectionTestUtils.setField(service, "store", store);
        ReflectionTestUtils.setField(service, "reader", new QuestionWorkbookReader());
        ReflectionTestUtils.setField(service, "images", images);
        ReflectionTestUtils.setField(service, "questions", mock(IBizQuestionService.class));
        doReturn(new HashMap<>()).when(service).detail(anyLong());
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void duplicatePicturesGetDistinctPendingIds() throws Exception {
        byte[] workbookBytes = workbook();
        Map<String,Object> batch = new HashMap<>();
        batch.put("batch_id", 1L);
        batch.put("owner_id", 9L);
        batch.put("file_hash", QuestionMediaCodec.hash(workbookBytes));
        batch.put("state", "PARSING");
        when(store.one(startsWith("select * from biz_question_import_batch where owner_id=?"), any(), any())).thenReturn(batch);
        when(store.insert(startsWith("insert into biz_question_import_item"), any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            savedDrafts.add((String) invocation.getArgument(5));
            return 1L;
        });
        when(images.saveBytes(any(byte[].class))).thenReturn(Collections.singletonMap("imageId", "101"));

        MockMultipartFile file = new MockMultipartFile("file", "同图重复.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbookBytes);
        service.upload(file, "request-1", true);

        assertEquals(2, savedDrafts.size());
        RowDraft first = QuestionMediaCodec.JSON.readValue(savedDrafts.get(0), RowDraft.class);
        assertEquals(2, first.pendingImages.size());
        assertEquals(first.pendingImages.get(0).imageId, first.pendingImages.get(1).imageId);
        assertNotNull(first.pendingImages.get(0).pendingId);
        assertNotNull(first.pendingImages.get(1).pendingId);
        assertNotEquals(first.pendingImages.get(0).pendingId, first.pendingImages.get(1).pendingId);
    }

    @Test
    void legacyPlacementCanChooseRepeatedImageByOriginalSlot() throws Exception {
        prepareDraft();
        draft.pendingImages.add(new PendingImage(null, "101", "stem", "跨题图片"));
        draft.pendingImages.add(new PendingImage(null, "101", "A", "跨题图片"));
        prepareItem();

        QuestionImportService.Patch request = new QuestionImportService.Patch();
        request.expectedRevision = 1L;
        QuestionImportService.Placement placement = new QuestionImportService.Placement();
        placement.imageId = "101"; placement.slot = "A"; placement.discard = true;
        request.placements = Collections.singletonList(placement);
        service.patch(1L, 10L, request);

        RowDraft saved = QuestionMediaCodec.JSON.readValue(savedDrafts.get(0), RowDraft.class);
        assertEquals(1, saved.pendingImages.size());
        assertEquals("stem", saved.pendingImages.get(0).slot);
    }

    @Test
    void pendingIdSelectsExactRepeatedImage() throws Exception {
        prepareDraft();
        draft.pendingImages.add(new PendingImage("0:1", "101", "stem", "跨题图片"));
        draft.pendingImages.add(new PendingImage("0:2", "101", "A", "跨题图片"));
        prepareItem();

        QuestionImportService.Patch request = new QuestionImportService.Patch();
        request.expectedRevision = 1L;
        QuestionImportService.Placement placement = new QuestionImportService.Placement();
        placement.pendingId = "0:2"; placement.imageId = "101"; placement.slot = "B"; placement.discard = true;
        request.placements = Collections.singletonList(placement);
        service.patch(1L, 10L, request);

        RowDraft saved = QuestionMediaCodec.JSON.readValue(savedDrafts.get(0), RowDraft.class);
        assertEquals(1, saved.pendingImages.size());
        assertEquals("0:1", saved.pendingImages.get(0).pendingId);
    }

    @Test
    void crossQuestionMoveValidatesAndSavesWholeTargetDraft() throws Exception {
        prepareDraft();
        draft.pendingImages.add(new PendingImage("0:1", "101", "stem", "跨题图片"));
        prepareItem();
        RowDraft target = baseDraft("目标题");
        Map<String,Object> targetItem = item(20L, 1L, target, 1);
        when(store.one(startsWith("select * from biz_question_import_item where item_id=? and batch_id=?"), any(), any()))
                .thenAnswer(invocation -> Long.valueOf(10L).equals(invocation.getArgument(1)) ? item : targetItem);

        QuestionImportService.Patch request = new QuestionImportService.Patch();
        request.expectedRevision = 1L;
        QuestionImportService.Placement placement = new QuestionImportService.Placement();
        placement.pendingId = "0:1"; placement.imageId = "101"; placement.slot = "B";
        placement.targetItemId = 20L;
        request.placements = Collections.singletonList(placement);
        service.patch(1L, 10L, request);

        assertEquals(2, savedDrafts.size());
        RowDraft savedTarget = QuestionMediaCodec.JSON.readValue(savedDrafts.get(0), RowDraft.class);
        assertEquals("101", savedTarget.question.getMedia().options.get("B").get(0).imageId);
        verify(images).readable(101L, null, null, null, 1L);
    }

    private void prepareDraft() {
        draft = baseDraft("原题");
    }

    private void prepareItem() {
        item = item(10L, 1L, draft, 1);
        when(store.one(startsWith("select * from biz_question_import_batch where batch_id=?"), any())).thenReturn(batch());
        when(store.one(startsWith("select * from biz_question_import_item where item_id=? and batch_id=?"), any(), any())).thenReturn(item);
    }

    private Map<String,Object> batch() {
        Map<String,Object> batch = new HashMap<>();
        batch.put("batch_id", 1L); batch.put("owner_id", 9L); batch.put("state", "REVIEW"); batch.put("revision", 1L);
        return batch;
    }

    private RowDraft baseDraft(String stem) {
        RowDraft row = new RowDraft();
        row.sourceKey = "0:1"; row.sheetName = "题库"; row.sourceRow = 2;
        BizQuestion q = new BizQuestion();
        q.setQuestionType("choice"); q.setQuestionContent(stem);
        q.setOptionA("甲"); q.setOptionB("乙"); q.setOptionC("丙"); q.setOptionD("丁"); q.setAnswer("A");
        q.setMedia(new QuestionMedia());
        row.question = q;
        return row;
    }

    private Map<String,Object> item(Long id, Long batchId, RowDraft row, int included) {
        Map<String,Object> value = new HashMap<>();
        value.put("item_id", id); value.put("batch_id", batchId);
        value.put("draft_json", QuestionMediaCodec.write(row));
        value.put("issues_json", "[]"); value.put("included", included);
        return value;
    }

    private byte[] workbook() throws Exception {
        try (Workbook book = new XSSFWorkbook()) {
            Sheet sheet = book.createSheet("题库");
            Row header = sheet.createRow(0);
            String[] headers = {"题目类型","题目内容","选项A","选项B","选项C","选项D","标准答案","年级","学期"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            Row first = sheet.createRow(1);
            String[] values = {"选择题","第一题","甲","乙","丙","丁","A","六年级","上册"};
            for (int i = 0; i < values.length; i++) first.createCell(i).setCellValue(values[i]);
            sheet.createRow(2).createCell(1).setCellValue("第二题");
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(100, 60, BufferedImage.TYPE_INT_RGB), "png", png);
            int picture = book.addPicture(png.toByteArray(), Workbook.PICTURE_TYPE_PNG);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            for (int i = 0; i < 2; i++) {
                ClientAnchor anchor = book.getCreationHelper().createClientAnchor();
                anchor.setRow1(1); anchor.setRow2(3); anchor.setCol1(1); anchor.setCol2(2);
                drawing.createPicture(anchor, picture);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            book.write(out);
            return out.toByteArray();
        }
    }
}
