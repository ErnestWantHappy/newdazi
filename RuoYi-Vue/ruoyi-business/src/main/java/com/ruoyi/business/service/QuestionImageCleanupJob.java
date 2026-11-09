package com.ruoyi.business.service;

import java.nio.file.Files;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/** 只清理超过七天的未绑定图，历史题目使用过的RETAINED图永不自动删除。 */
@Component
public class QuestionImageCleanupJob {
    private static final Logger LOG=LoggerFactory.getLogger(QuestionImageCleanupJob.class);
    @Autowired private QuestionMediaStore store;
    @Autowired private QuestionImageService images;
    @Autowired private org.springframework.transaction.PlatformTransactionManager transactions;

    @Scheduled(cron="0 15 3 * * ?")
    public void clean() {
        for(Map<String,Object> candidate:store.jdbc().queryForList("select image_id from biz_question_image where state in ('TEMP','EXPIRED') and create_time < date_sub(now(),interval 7 day) order by image_id limit 500")) {
            Long id=QuestionMediaStore.number(candidate.get("image_id"));
            Map<String,Object> expired=new TransactionTemplate(transactions).execute(status->{
                Map<String,Object> row=store.one("select * from biz_question_image where image_id=? for update",id);
                if(row==null || "RETAINED".equals(row.get("state")))return null;
                store.jdbc().update("update biz_question_image set state='EXPIRED' where image_id=?",id);
                return row;
            });
            if(expired!=null)try{
                Files.deleteIfExists(images.resolve(expired));
                store.jdbc().update("delete from biz_question_image where image_id=? and state='EXPIRED'",id);
            }catch(Exception ex){LOG.warn("暂存图片清理失败，下次重试 imageId={}",id);}
        }
    }
}
