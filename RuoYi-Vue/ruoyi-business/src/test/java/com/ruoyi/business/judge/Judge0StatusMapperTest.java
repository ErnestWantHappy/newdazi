package com.ruoyi.business.judge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Judge0StatusMapperTest {
    @Test
    void mapsKnownJudge0TerminalStates() {
        assertEquals("ACCEPTED", Judge0StatusMapper.toPlatformStatus(3));
        assertEquals("WRONG_ANSWER", Judge0StatusMapper.toPlatformStatus(4));
        assertEquals("TIME_LIMIT", Judge0StatusMapper.toPlatformStatus(5));
        assertEquals("SYNTAX_ERROR", Judge0StatusMapper.toPlatformStatus(6));
        assertEquals("RUNTIME_ERROR", Judge0StatusMapper.toPlatformStatus(7));
        assertEquals("MEMORY_LIMIT", Judge0StatusMapper.toPlatformStatus(12));
    }

    @Test
    void keepsUnknownOrMissingStateAsServiceFailure() {
        assertEquals("SERVICE_ERROR", Judge0StatusMapper.toPlatformStatus(null));
        assertEquals("SERVICE_ERROR", Judge0StatusMapper.toPlatformStatus(1));
        assertEquals("SERVICE_ERROR", Judge0StatusMapper.toPlatformStatus(99));
    }
}
