package com.ruoyi.business.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.business.service.QuestionWorkbookReader.*;
import com.ruoyi.business.utils.QuestionMediaCodec;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 持久化预览和正式题库分离；只有确认事务成功才出现正式题目。 */
@Service
public class QuestionImportService {
    private static final Semaphore PARSING = new Semaphore(2);
    @Autowired private QuestionMediaStore store;
    @Autowired private QuestionWorkbookReader reader;
    @Autowired private QuestionImageService images;
    @Autowired private IBizQuestionService questions;

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> upload(MultipartFile file,String requestId,boolean allowDuplicate) throws IOException {
        requireKey(requestId);
        if(file==null||file.isEmpty()||file.getSize()>QuestionWorkbookReader.MAX_FILE)throw new ServiceException("请选择不超过50MB的Excel文件");
        String name=file.getOriginalFilename()==null?"题库.xlsx":file.getOriginalFilename();
        if(name.length()>255||!name.toLowerCase(Locale.ROOT).matches(".*\\.(xls|xlsx)$"))throw new ServiceException("请选择xls或xlsx文件");
        if(!PARSING.tryAcquire())throw new ServiceException("当前正在识别其他文件，请稍后再试，尚未创建题目");
        try {
            byte[] bytes=file.getBytes();String hash=QuestionMediaCodec.hash(bytes);Long owner=SecurityUtils.getUserId();
            if(!allowDuplicate){Map<String,Object> prior=store.one("select batch_id from biz_question_import_batch where owner_id=? and file_hash=? and state='COMMITTED' order by batch_id desc limit 1",owner,hash);if(prior!=null){Map<String,Object> r=detail(QuestionMediaStore.number(prior.get("batch_id")));r.put("alreadyImported",true);return r;}}
            int inserted=store.jdbc().update("insert ignore into biz_question_import_batch(owner_id,request_id,file_hash,original_name,state) values(?,?,?,?,'PARSING')",owner,requestId,hash,name);
            Map<String,Object> batch=store.one("select * from biz_question_import_batch where owner_id=? and request_id=? for update",owner,requestId);
            if(!hash.equals(batch.get("file_hash")))throw new ServiceException("该上传请求对应其他文件，请重新选择");
            long id=QuestionMediaStore.number(batch.get("batch_id"));
            if(inserted==0)return detail(id);
            Result result=reader.read(bytes);
            Map<String,String> dedup=new HashMap<>();
            for(RowDraft row:result.rows) {
                QuestionMedia media=row.question.getMedia();
                for(Picture picture:row.pictures) {
                    String key=QuestionMediaCodec.hash(picture.bytes), imageId=dedup.get(key);
                    try {
                        // 可修正的格式错误在进入上传事务代理前识别，避免整批被标成只回滚。
                        QuestionImageService.inspect(picture.bytes);
                        if(imageId==null){imageId=String.valueOf(images.saveBytes(picture.bytes).get("imageId"));dedup.put(key,imageId);}
                        int occupied="stem".equals(picture.slot)?media.stem.size():media.options.get(picture.slot).size();
                        if(occupied>=("stem".equals(picture.slot)?3:1)) picture.reason="该位置图片超过上限，请移到其他位置或不导入此图";
                        if(picture.reason!=null) {
                            // 同一图片字节可能出现在多个位置，pendingId 用于稳定区分每个待确认位置。
                            String pendingId=row.sourceKey+":"+(row.pendingImages.size()+1);
                            row.pendingImages.add(new PendingImage(pendingId,imageId,picture.slot,picture.reason));
                        }
                        else addImage(media,picture.slot,imageId);
                    }catch(ServiceException ex){row.issues.add(new Issue(picture.slot,ex.getMessage()));}
                }
                row.question.setMedia(media);
                store.insert("insert into biz_question_import_item(batch_id,source_key,sheet_name,source_row,draft_json,issues_json) values(?,?,?,?,?,?)",id,row.sourceKey,row.sheetName,row.sourceRow,QuestionMediaCodec.write(row),QuestionMediaCodec.write(validation(row)));
            }
            Map<String,Object> global=new LinkedHashMap<>();global.put("messages",new ArrayList<>(new LinkedHashSet<>(result.globalIssues)));global.put("reviewed",result.globalIssues.isEmpty());
            store.jdbc().update("update biz_question_import_batch set state='REVIEW',question_count=?,image_count=?,issues_json=?,update_time=now() where batch_id=?",result.rows.size(),result.imageCount,QuestionMediaCodec.write(global),id);
            return detail(id);
        } finally {PARSING.release();}
    }

    public Map<String,Object> detail(Long id) {
        Map<String,Object> batch=requireBatch(id,false);
        Map<String,Object> r=new LinkedHashMap<>();r.put("batchId",String.valueOf(id));r.put("state",batch.get("state"));r.put("revision",batch.get("revision"));r.put("fileName",batch.get("original_name"));r.put("imageCount",batch.get("image_count"));
        r.put("createTime",batch.get("create_time"));r.put("issues",jsonMap((String)batch.get("issues_json")));
        List<Map<String,Object>> rows=new ArrayList<>();int valid=0,included=0;
        for(Map<String,Object> item:store.jdbc().queryForList("select * from biz_question_import_item where batch_id=? order by item_id",id)) {
            RowDraft draft=draft(item);List<String> errors=validation(draft);boolean use=((Number)item.get("included")).intValue()==1;
            Map<String,Object> row=new LinkedHashMap<>();row.put("itemId",String.valueOf(item.get("item_id")));row.put("draft",draft);row.put("errors",errors);row.put("included",use);row.put("questionId",item.get("question_id"));rows.add(row);
            if(use){included++;if(errors.isEmpty())valid++;}
        }
        r.put("items",rows);r.put("questionCount",rows.size());r.put("includedCount",included);r.put("validCount",valid);r.put("errorCount",included-valid);
        if(batch.get("result_json")!=null)try{r.put("result",QuestionMediaCodec.JSON.readValue((String)batch.get("result_json"),Map.class));}catch(IOException ex){throw new ServiceException("导入结果异常，请联系管理员");}
        return r;
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> patch(Long id,Long itemId,Patch request) {
        Map<String,Object> batch=requireReview(id,request.expectedRevision);
        Map<String,Object> item=store.one("select * from biz_question_import_item where item_id=? and batch_id=? for update",itemId,id);
        if(item==null)throw new ServiceException("本批次不存在此题目");
        RowDraft row=draft(item);
        if(request.question!=null) {
            if(request.question.getQuestionId()!=null)throw new ServiceException("预览只能创建新题，不能覆盖题库题目");
            row.question=request.question;
            row.question.setMedia(QuestionMediaCodec.normalize(row.question.getMedia()));
            if(request.resolvedFields!=null)row.issues.removeIf(issue->request.resolvedFields.contains(issue.field));
        }
        if(request.placements!=null) for(Placement placement:request.placements) {
            PendingImage pending=pending(row,placement);
            if(pending==null)throw new ServiceException("待分配图片已变化，请刷新本题");
            if(!placement.discard) {
                long targetId=placement.targetItemId==null?itemId:placement.targetItemId;
                if(targetId==itemId){QuestionMedia m=row.question.getMedia();addImage(m,placement.slot,placement.imageId);row.question.setMedia(m);}
                else {
                    Map<String,Object> target=store.one("select * from biz_question_import_item where item_id=? and batch_id=? for update",targetId,id);
                    if(target==null)throw new ServiceException("目标题目不属于本批次");
                    RowDraft other=draft(target);QuestionMedia m=other.question.getMedia();addImage(m,placement.slot,placement.imageId);other.question.setMedia(m);
                    // 目标题原有图片也重新走一遍授权，避免跨题移动绕过完整草稿校验。
                    for(Long image:QuestionMediaCodec.ids(other.question.getMedia())) images.readable(image,null,null,null,id);
                    writeItem(targetId,other);
                }
            }
            row.pendingImages.remove(pending);
        }
        // 用户提供的图片必须是本人的真实暂存/已保留图，不能借草稿注入他人ID。
        for(Long image:QuestionMediaCodec.ids(row.question.getMedia())) images.readable(image,null,null,null,id);
        writeItem(itemId,row);
        if(request.included!=null)store.jdbc().update("update biz_question_import_item set included=? where item_id=?",request.included?1:0,itemId);
        store.jdbc().update("update biz_question_import_batch set revision=revision+1,update_time=now() where batch_id=?",id);
        return detail(id);
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> reviewGlobal(Long id,Long revision) {
        Map<String,Object> batch=requireReview(id,revision);Map<String,Object> global=jsonMap((String)batch.get("issues_json"));global.put("reviewed",true);
        store.jdbc().update("update biz_question_import_batch set issues_json=?,revision=revision+1,update_time=now() where batch_id=?",QuestionMediaCodec.write(global),id);
        return detail(id);
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> confirm(Long id,Long expectedRevision) {
        Map<String,Object> batch=requireBatch(id,true);
        if("COMMITTED".equals(batch.get("state")))return detail(id);
        if(!"REVIEW".equals(batch.get("state"))||expectedRevision==null||QuestionMediaStore.number(batch.get("revision"))!=expectedRevision)throw new ServiceException("预览已变化或失效，请刷新后确认");
        if(!Boolean.TRUE.equals(jsonMap((String)batch.get("issues_json")).get("reviewed")))throw new ServiceException("请先核对文件中的特殊图片提示");
        List<Map<String,Object>> items=store.jdbc().queryForList("select * from biz_question_import_item where batch_id=? and included=1 order by item_id for update",id);
        if(items.isEmpty())throw new ServiceException("请至少保留一道题目");
        for(Map<String,Object> item:items){List<String> errors=validation(draft(item));if(!errors.isEmpty())throw new ServiceException("Excel第"+item.get("source_row")+"行尚未修正："+String.join("；",errors));}
        List<Long> ids=new ArrayList<>();
        for(Map<String,Object> item:items) {
            RowDraft checked=draft(item);validation(checked);
            BizQuestion q=checked.question;q.setQuestionId(null);q.allowMediaSave();
            images.bind(q,null);
            if(questions.insertBizQuestion(q)!=1)throw new ServiceException("题目写入失败，整批未导入，可重试");
            ids.add(q.getQuestionId());store.jdbc().update("update biz_question_import_item set question_id=? where item_id=?",q.getQuestionId(),item.get("item_id"));
        }
        Map<String,Object> result=new LinkedHashMap<>();result.put("questionIds",ids);result.put("importedCount",ids.size());result.put("excludedCount",((Number)batch.get("question_count")).intValue()-ids.size());
        store.jdbc().update("update biz_question_import_batch set state='COMMITTED',result_json=?,update_time=now() where batch_id=?",QuestionMediaCodec.write(result),id);
        return detail(id);
    }

    public List<Map<String,Object>> recent() {
        return store.jdbc().queryForList("select batch_id as batchId,original_name as fileName,state,question_count as questionCount,create_time as createTime from biz_question_import_batch where owner_id=? order by batch_id desc limit 20",SecurityUtils.getUserId());
    }
    public List<Long> resultIds(Long id) {
        Map<String,Object> b=requireBatch(id,false);if(!"COMMITTED".equals(b.get("state")))throw new ServiceException("本批题目尚未正式导入");
        List<Long> ids=new ArrayList<>();for(Map<String,Object> r:store.jdbc().queryForList("select question_id from biz_question_import_item where batch_id=? and question_id is not null order by item_id",id))ids.add(QuestionMediaStore.number(r.get("question_id")));return ids;
    }
    private Map<String,Object> requireReview(Long id,Long revision) {
        Map<String,Object> b=requireBatch(id,true);
        if(!"REVIEW".equals(b.get("state")))throw new ServiceException("本批次已完成或失效，不能继续修改");
        if(revision==null||QuestionMediaStore.number(b.get("revision"))!=revision)throw new ServiceException("预览已在其他窗口变化，请刷新后再修正");return b;
    }
    private Map<String,Object> requireBatch(Long id,boolean lock) {
        Map<String,Object> b=store.one("select * from biz_question_import_batch where batch_id=?"+(lock?" for update":""),id);
        if(b==null||!SecurityUtils.getUserId().equals(QuestionMediaStore.number(b.get("owner_id"))))throw new ServiceException("无权读取该导入批次",403);
        if(!"COMMITTED".equals(b.get("state"))&&store.count("select count(*) from biz_question_import_batch where batch_id=? and create_time < date_sub(now(),interval 7 day)",id)>0)throw new ServiceException("导入预览已过期，请重新上传Excel");return b;
    }
    private RowDraft draft(Map<String,Object> item) {
        try{return QuestionMediaCodec.JSON.readValue((String)item.get("draft_json"),RowDraft.class);}catch(IOException ex){throw new ServiceException("预览内容异常，请重新上传");}
    }
    private void writeItem(long id,RowDraft row){store.jdbc().update("update biz_question_import_item set draft_json=?,issues_json=? where item_id=?",QuestionMediaCodec.write(row),QuestionMediaCodec.write(validation(row)),id);}
    public static List<String> validation(RowDraft row) {
        List<String> result=new ArrayList<>();for(Issue issue:row.issues)result.add(issue.message);
        if(!row.pendingImages.isEmpty())result.add("还有"+row.pendingImages.size()+"张图片需要确认所属位置");
        BizQuestion q=row.question;
        if("choice".equals(q.getQuestionType()))result.addAll(QuestionMediaCodec.validate(q,false));
        else {
            if(QuestionMediaCodec.empty(q.getQuestionContent()).trim().isEmpty())result.add("题目内容不能为空");
            if(QuestionMediaCodec.hasImages(q.getMediaJson()))result.add("目前只有选择题支持内嵌图片");
            if("judgment".equals(q.getQuestionType())) {
                String a=QuestionMediaCodec.empty(q.getAnswer());
                if(java.util.Arrays.asList("正确","对","1","TRUE","T").contains(a))q.setAnswer("T");
                else if(java.util.Arrays.asList("错误","错","0","FALSE","F").contains(a))q.setAnswer("F");else result.add("判断题答案请填写T或F");
            } else if(!"typing".equals(q.getQuestionType())) result.add("仅支持选择/判断/打字题，操作题请在题库新增");
        }
        return result;
    }
    private static void addImage(QuestionMedia media,String slot,String id) {
        List<QuestionMedia.ImageRef> refs="stem".equals(slot)?media.stem:media.options.get(slot);
        if(refs==null)throw new ServiceException("请选择题干或选项A、B、C、D");
        if(refs.size()>=("stem".equals(slot)?3:1))throw new ServiceException("该位置图片已满，请先移除原图");
        refs.add(new QuestionMedia.ImageRef(id));
    }
    private static PendingImage pending(RowDraft row,Placement placement) {
        if(placement.pendingId!=null && !placement.pendingId.isEmpty()) {
            PendingImage exact=row.pendingImages.stream().filter(p->placement.pendingId.equals(p.pendingId)).findFirst().orElse(null);
            return exact!=null && Objects.equals(exact.imageId,placement.imageId)?exact:null;
        }
        // 旧预览没有 pendingId：图片唯一时兼容；同图重复时只接受原位置唯一匹配，歧义则要求刷新。
        List<PendingImage> same=new ArrayList<>();
        for(PendingImage p:row.pendingImages) if(Objects.equals(p.imageId,placement.imageId)) same.add(p);
        if(same.size()==1) return same.get(0);
        if(same.size()>1 && placement.slot!=null) {
            List<PendingImage> bySlot=new ArrayList<>();
            for(PendingImage p:same) if(placement.slot.equals(p.slot)) bySlot.add(p);
            if(bySlot.size()==1) return bySlot.get(0);
        }
        return null;
    }
    @SuppressWarnings("unchecked") private Map<String,Object> jsonMap(String json) {
        if(json==null)return new LinkedHashMap<>();try{return QuestionMediaCodec.JSON.readValue(json,Map.class);}catch(IOException ex){throw new ServiceException("导入状态异常，请重新打开");}
    }
    private void requireKey(String key){if(key==null||!key.matches("[A-Za-z0-9_-]{8,64}"))throw new ServiceException("上传请求无效，请重新打开导入窗口");}
    public static class Patch {
        public Long expectedRevision;public BizQuestion question;public List<String> resolvedFields;public List<Placement> placements;public Boolean included;
    }
    public static class Placement {public String pendingId,imageId,slot;public Long targetItemId;public boolean discard;}
}
