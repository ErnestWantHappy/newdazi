package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizScoringDetail;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.common.exception.ServiceException;

class PracticalScoringPolicyServiceTest
{
    private final PracticalScoringPolicyService service = new PracticalScoringPolicyService();

    @Test
    void shouldKeepItemMaximumTotalExactAtRoundingBoundary()
    {
        List<PracticalScoringItemVo> items = service.buildScoringItems(Arrays.asList(
                item(1L, 33), item(2L, 33), item(3L, 34)), 10);

        assertEquals(10, items.stream().mapToInt(PracticalScoringItemVo::getMaxScore).sum());
        assertEquals(Arrays.asList(3, 3, 4), Arrays.asList(
                items.get(0).getMaxScore(), items.get(1).getMaxScore(), items.get(2).getMaxScore()));
    }

    @Test
    void shouldRejectDuplicateOrForeignScoringItems()
    {
        List<PracticalScoringItemVo> items = service.buildScoringItems(
                Arrays.asList(item(1L, 50), item(2L, 50)), 40);

        assertThrows(ServiceException.class, () -> service.resolveFinalScore(
                40, 40, items, Arrays.asList(detail(1L, 20), detail(1L, 20))));
        assertThrows(ServiceException.class, () -> service.resolveFinalScore(
                40, 40, items, Arrays.asList(detail(1L, 20), detail(99L, 20))));
    }

    @Test
    void shouldRejectIncompleteOrOverLimitDetails()
    {
        List<PracticalScoringItemVo> items = service.buildScoringItems(
                Arrays.asList(item(1L, 50), item(2L, 50)), 40);

        assertThrows(ServiceException.class, () -> service.resolveFinalScore(
                20, 40, items, Collections.singletonList(detail(1L, 20))));
        assertThrows(ServiceException.class, () -> service.resolveFinalScore(
                40, 40, items, Arrays.asList(detail(1L, 21), detail(2L, 19))));
    }

    @Test
    void shouldUseServerValidatedDetailTotal()
    {
        List<PracticalScoringItemVo> items = service.buildScoringItems(
                Arrays.asList(item(1L, 25), item(2L, 75)), 40);

        assertEquals(31, service.resolveFinalScore(
                31, 40, items, Arrays.asList(detail(1L, 7), detail(2L, 24))));
        assertThrows(ServiceException.class, () -> service.resolveFinalScore(
                32, 40, items, Arrays.asList(detail(1L, 7), detail(2L, 24))));
    }

    private BizScoringItem item(Long itemId, int weightPercent)
    {
        BizScoringItem item = new BizScoringItem();
        item.setItemId(itemId);
        item.setQuestionId(101L);
        item.setItemName("评分项" + itemId);
        item.setItemScore(weightPercent);
        item.setOrderNum(itemId.intValue());
        return item;
    }

    private BizScoringDetail detail(Long itemId, int score)
    {
        BizScoringDetail detail = new BizScoringDetail();
        detail.setItemId(itemId);
        detail.setScore(score);
        return detail;
    }
}
