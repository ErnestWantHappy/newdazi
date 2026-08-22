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
        assertEquals("SERVICE_ERROR", Judge0StatusMapper.toPlatformStatus((Integer) null));
        assertEquals("SERVICE_ERROR", Judge0StatusMapper.toPlatformStatus(1));
        assertEquals("SERVICE_ERROR", Judge0StatusMapper.toPlatformStatus(99));
    }

    @Test
    void recognizesPythonSyntaxErrorsReportedAsRuntimeErrors() {
        Judge0Result syntax = new Judge0Result();
        syntax.setStatusId(11);
        syntax.setStderr("SyntaxError: '(' was never closed");
        assertEquals("SYNTAX_ERROR", Judge0StatusMapper.toPlatformStatus(syntax));

        Judge0Result indentation = new Judge0Result();
        indentation.setStatusId(11);
        indentation.setStderr("IndentationError: unexpected indent");
        assertEquals("SYNTAX_ERROR", Judge0StatusMapper.toPlatformStatus(indentation));

        Judge0Result runtime = new Judge0Result();
        runtime.setStatusId(11);
        runtime.setStderr("ZeroDivisionError: division by zero");
        assertEquals("RUNTIME_ERROR", Judge0StatusMapper.toPlatformStatus(runtime));
    }
}
