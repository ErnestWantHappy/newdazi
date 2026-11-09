package com.ruoyi.business.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.utils.QuestionMediaCodec;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class QuestionImageService {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    @Autowired private QuestionMediaStore store;
    @Autowired private BizQuestionMapper questions;

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) throw new ServiceException("请选择不超过5MB的PNG或JPG图片");
        return saveBytes(file.getBytes());
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> saveBytes(byte[] bytes) throws IOException {
        ImageInfo info = inspect(bytes);
        String name = LocalDate.now().toString() + "/" + UUID.randomUUID().toString().replace("-", "") + "." + info.extension;
        Path root = root();
        Path target = root.resolve(name).normalize();
        if (!target.startsWith(root)) throw new ServiceException("图片路径异常");
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) try { Files.deleteIfExists(target); } catch(IOException ignored) { }
                }
            });
        }
        long id;
        try {
            id = store.insert("insert into biz_question_image(uploader_id,resource_path,mime_type,byte_size,width,height,sha256,state) values(?,?,?,?,?,?,?,'TEMP')",
                    SecurityUtils.getUserId(), name, info.mime, bytes.length, info.width, info.height, QuestionMediaCodec.hash(bytes));
        } catch (RuntimeException ex) { Files.deleteIfExists(target); throw ex; }
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("imageId", String.valueOf(id)); result.put("width",info.width); result.put("height",info.height);
        result.put("bytes",bytes.length); result.put("state","TEMP");
        return result;
    }

    public static ImageInfo inspect(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) throw new ServiceException("单张图片不能超过5MB");
        boolean png = bytes.length>8 && bytes[0]==(byte)137 && bytes[1]==80 && bytes[2]==78 && bytes[3]==71;
        boolean jpg = bytes.length>3 && bytes[0]==(byte)255 && bytes[1]==(byte)216 && bytes[2]==(byte)255;
        if (!png && !jpg) throw new ServiceException("仅支持真实PNG或JPG图片，请截图后粘贴");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new ServiceException("图片无法识别，请重新截图");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width=reader.getWidth(0), height=reader.getHeight(0);
                if (width<=0 || height<=0 || width>8192 || height>8192 || (long)width*height>20000000)
                    throw new ServiceException("图片尺寸过大，请缩小至8192像素以内且不超过2000万像素");
                if (reader.read(0)==null) throw new ServiceException("图片损坏，请重新上传");
                return new ImageInfo(width,height,png?"png":"jpg",png?"image/png":"image/jpeg");
            } finally { reader.dispose(); }
        } catch (IOException ex) { throw new ServiceException("图片损坏，请重新截图"); }
    }

    public void bind(BizQuestion q, BizQuestion original) {
        for (Long id : new TreeSet<>(QuestionMediaCodec.ids(q.getMedia()))) {
            Map<String,Object> row = store.one("select * from biz_question_image where image_id=? for update",id);
            if (row==null || !Files.isRegularFile(resolve(row))) throw new ServiceException("图片已失效，请重新上传");
            boolean own = SecurityUtils.getUserId().equals(QuestionMediaStore.number(row.get("uploader_id")));
            boolean source = original!=null && QuestionMediaCodec.contains(original.getMediaJson(),id);
            if (!own && !source && !SecurityUtils.isAdmin(SecurityUtils.getUserId())) throw new ServiceException("无权使用该图片");
            if (!"TEMP".equals(row.get("state")) && !"RETAINED".equals(row.get("state"))) throw new ServiceException("图片已过期，请重新上传");
            store.jdbc().update("update biz_question_image set state='RETAINED',bound_time=coalesce(bound_time,now()) where image_id=?",id);
        }
    }

    public Map<String,Object> readable(Long id, Long questionId, Long lessonId, Long examId, Long batchId) {
        Map<String,Object> image = store.one("select * from biz_question_image where image_id=?",id);
        if (image==null || !("TEMP".equals(image.get("state"))||"RETAINED".equals(image.get("state")))) throw new ServiceException("图片不存在或已过期");
        if ((lessonId!=null && examId!=null) || (batchId!=null && (lessonId!=null||examId!=null||questionId!=null))) throw new ServiceException("图片查看范围不明确");
        Long user=SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(user)) return image;
        if (batchId!=null) {
            if (store.count("select count(*) from biz_question_import_batch where batch_id=? and owner_id=?",batchId,user)==0) deny();
            // 同批暂存图仍需属于上传者，不能借自己的批次读取他人图片。
            if (user.equals(QuestionMediaStore.number(image.get("uploader_id")))) return image;
            deny();
        }
        if (lessonId==null && examId==null && user.equals(QuestionMediaStore.number(image.get("uploader_id")))) return image;
        if (questionId==null) {
            deny();
        }
        Long student=studentId(user);
        if (examId!=null) {
            // 管理端组卷预览尚未生成个人卷，仍须验证图片属于该考试实际引用的源题。
            if (student==null && hasRole("researcher") && store.count("select count(*) from biz_county_exam_question where exam_id=? and question_id=?",examId,questionId)>0) {
                BizQuestion source=questions.selectBizQuestionByQuestionId(questionId);
                if (source!=null && QuestionMediaCodec.contains(source.getMediaJson(),id)) return image;
            }
            List<Map<String,Object>> refs=store.jdbc().queryForList("select media_json from biz_county_exam_paper_question where exam_id=? and question_id=?"+(student==null?"":" and student_id=?"),student==null?new Object[]{examId,questionId}:new Object[]{examId,questionId,student});
            boolean match=refs.stream().anyMatch(r->QuestionMediaCodec.contains((String)r.get("media_json"),id));
            if (match && (student!=null || hasRole("researcher"))) return image;
            deny();
        }
        BizQuestion q=questions.selectBizQuestionByQuestionId(questionId);
        if (q==null || !QuestionMediaCodec.contains(q.getMediaJson(),id)) deny();
        if (lessonId!=null) {
            if (store.count("select count(*) from biz_lesson_question where lesson_id=? and question_id=?",lessonId,questionId)==0) deny();
            if (student!=null && (store.count("select count(*) from biz_student_answer where student_id=? and lesson_id=? and question_id=?",student,lessonId,questionId)>0 ||
                store.count("select count(*) from biz_student s join sys_user u on u.user_id=s.user_id join biz_lesson_assignment a on a.dept_id=u.dept_id and a.entry_year=s.entry_year and a.class_code=s.class_code where s.student_id=? and a.lesson_id=?",student,lessonId)>0)) return image;
            if (hasRole("teacher") || hasRole("researcher")) {
                if (store.count("select count(*) from biz_lesson where lesson_id=? and (creator_id=? or (creator_id is null and create_by=?))",lessonId,user,SecurityUtils.getUsername())>0) return image;
                if (store.count("select count(*) from biz_lesson l join biz_lesson_assignment a on a.lesson_id=l.lesson_id join biz_teacher_class tc on tc.dept_id=a.dept_id and tc.entry_year=a.entry_year and replace(tc.class_code,'班','')=replace(a.class_code,'班','') where l.lesson_id=? and l.dept_id=? and tc.user_id=? and tc.dept_id=?",lessonId,SecurityUtils.getDeptId(),user,SecurityUtils.getDeptId())>0) return image;
            }
            deny();
        }
        if ((hasRole("teacher")||hasRole("researcher")) && (user.equals(q.getCreatorId()) || "Y".equalsIgnoreCase(q.getIsPublic()) || "1".equals(q.getIsPublic()))) return image;
        deny(); return null;
    }

    private Long studentId(Long user) {
        Map<String,Object> s=store.one("select student_id from biz_student where user_id=?",user);
        return s==null?null:QuestionMediaStore.number(s.get("student_id"));
    }
    public static boolean hasRole(String key) {
        return SecurityUtils.getLoginUser().getUser().getRoles()!=null && SecurityUtils.getLoginUser().getUser().getRoles().stream().anyMatch(r->key.equals(r.getRoleKey()));
    }
    private void deny() { throw new ServiceException("无权查看该题目图片",403); }
    public Path resolve(Map<String,Object> row) {
        Path file=root().resolve(String.valueOf(row.get("resource_path"))).normalize();
        if (!file.startsWith(root())) throw new ServiceException("图片路径异常");
        return file;
    }
    private Path root() {
        // 必须置于公开/profile映射之外，猜到相对路径也不能绕过题目授权。
        Path profile=Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        return profile.resolveSibling(profile.getFileName().toString()+"-question-images");
    }
    public byte[] bytesForExport(Long id, Long questionId) throws IOException {
        return Files.readAllBytes(resolve(readable(id,questionId,null,null,null)));
    }
    public static class ImageInfo {
        public final int width,height; public final String extension,mime;
        ImageInfo(int width,int height,String extension,String mime) { this.width=width;this.height=height;this.extension=extension;this.mime=mime; }
    }
}
