package com.ruoyi.business.service;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import com.ruoyi.business.mapper.TextbookCaseFeedbackMapper;
import com.ruoyi.common.exception.ServiceException;

@Service
public class TextbookCaseFeedbackService
{
    @Autowired private TextbookCaseFeedbackMapper mapper;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    public Map<String, Object> summary(Long caseId, Long userId)
    {
        return mapper.summary(caseId, userId);
    }

    public List<Map<String, Object>> comments(Long caseId)
    {
        requirePublished(caseId);
        return mapper.comments(caseId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> vote(Long caseId, Long userId, int vote)
    {
        return vote(caseId, userId, vote, null);
    }

    // 重复改票可能是无变化更新；读已提交避免锁等待前的旧快照返回上一票。
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> vote(Long caseId, Long userId, int vote, String reason)
    {
        if (vote < -1 || vote > 1) throw new ServiceException("请选择推荐或不推荐");
        requirePublished(caseId);
        if (reason != null)
        {
            reason = reason.trim();
            if (reason.length() > 500) throw new ServiceException("评价原因不能超过500字");
            // 空串表示明确清空；null表示快捷改票时保留已有理由。
        }
        mapper.vote(caseId, userId, vote, reason);
        return summary(caseId, userId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean invitation(Long caseId, Long userId)
    {
        requirePublished(caseId);
        // 同一教师并发打开多个课例时，先锁教师行，确保每天最多一次邀请。
        Map<String, Object> global = mapper.lockInvitation(userId);
        Instant now = Instant.now();
        Instant last = global == null ? null : instant(global.get("last_invited_at"));
        if (last != null && last.atZone(ZONE).toLocalDate().equals(now.atZone(ZONE).toLocalDate())) return false;
        if (global != null && recent(global.get("last_attempt_at"), now, Duration.ofMinutes(30))) return false;
        Map<String, Object> feedback = mapper.feedback(caseId, userId);
        Number voteNum = feedback == null ? null : (Number) feedback.get("vote");
        int currentVote = voteNum == null ? 0 : voteNum.intValue();
        if (feedback != null && (currentVote != 0
            || recent(feedback.get("last_invited_at"), now, Duration.ofDays(14)))) return false;
        boolean selected = draw();
        mapper.attempted(userId, selected);
        if (selected) mapper.invited(caseId, userId);
        return selected;
    }

    protected boolean draw() { return ThreadLocalRandom.current().nextInt(4) == 0; }

    private boolean recent(Object value, Instant now, Duration duration)
    {
        Instant time = instant(value);
        return time != null && time.plus(duration).isAfter(now);
    }

    private Instant instant(Object value) { return value == null ? null : ((Date) value).toInstant(); }

    private void requirePublished(Long caseId)
    {
        if (mapper.published(caseId) != 1) throw new ServiceException("课例已下架或不存在");
    }
}
