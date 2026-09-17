package com.ruoyi.business.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OfficeLinuxProcessScopeTest {
    @Test
    void onlyConfiguredServicePoolCanBeTerminated() {
        int[] ports = {2002, 2003};
        assertTrue(FileConversionUtils.isManagedLinuxOfficeProcess(
                "42 soffice.bin /usr/lib/soffice.bin -env:UserInstallation=file:///tmp/.jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1", "soffice.bin", ports));
        assertFalse(FileConversionUtils.isManagedLinuxOfficeProcess("43 soffice.bin /usr/lib/soffice.bin document.docx", "soffice.bin", ports));
        assertFalse(FileConversionUtils.isManagedLinuxOfficeProcess("44 soffice.bin .jodconverter_socket_host-127.0.0.1_port-20020_tcpNoDelay-1", "soffice.bin", ports));
        assertFalse(FileConversionUtils.isManagedLinuxOfficeProcess("45 bash .jodconverter_socket_host-127.0.0.1_port-2002_tcpNoDelay-1", "soffice.bin", ports));
    }
}
