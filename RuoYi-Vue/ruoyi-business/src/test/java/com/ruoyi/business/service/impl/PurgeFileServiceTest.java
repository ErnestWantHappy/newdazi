package com.ruoyi.business.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.ruoyi.business.mapper.PurgeMapper;
import com.ruoyi.common.config.RuoYiConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * 彻底清除文件收尾：只删无引用独占文件，越界与被引用一律保留。
 */
class PurgeFileServiceTest
{
    private Path profileRoot;
    private PurgeFileService service;

    @BeforeEach
    void setUp() throws Exception
    {
        profileRoot = Files.createTempDirectory("purge-test");
        new RuoYiConfig().setProfile(profileRoot.toString());
        service = new PurgeFileService();
        PurgeMapper mapper = Mockito.mock(PurgeMapper.class);
        Mockito.when(mapper.countPathReferences(anyString())).thenReturn(0);
        Mockito.when(mapper.countPathReferences("/profile/upload/b.jpg")).thenReturn(2);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "purgeMapper", mapper);
        Files.createDirectories(profileRoot.resolve("upload"));
    }

    @AfterEach
    void tearDown() throws Exception
    {
        org.springframework.test.util.ReflectionTestUtils.setField(service, "purgeMapper", null);
        deleteRecursively(profileRoot);
    }

    @Test
    void deletesOnlyUnreferencedInsideProfile() throws Exception
    {
        Path a = Files.write(profileRoot.resolve("upload/a.jpg"), new byte[] { 1 });
        Path b = Files.write(profileRoot.resolve("upload/b.jpg"), new byte[] { 2 });
        List<String> candidates = Arrays.asList(
                "/profile/upload/a.jpg", "/profile/upload/b.jpg",
                "http://10.52.1.123/aicuoti/", "/profile/../outside.jpg", null, "  ");
        int deleted = service.deleteUnreferenced(candidates);
        assertEquals(1, deleted);
        assertFalse(Files.exists(a));
        assertTrue(Files.exists(b));
    }

    @Test
    void normalizeKeepsDbShape() throws Exception
    {
        List<String> out = service.collectForTest(Arrays.asList(
                "/profile/upload/x.jpg", "profile/upload/y.jpg", "D:\\upload\\z.jpg"));
        assertEquals(Arrays.asList("/profile/upload/x.jpg", "/profile/upload/y.jpg"), out);
    }

    private static void deleteRecursively(Path root) throws Exception
    {
        if (root == null || !Files.exists(root))
        {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(root))
        {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }
}
