package com.ruoyi.business.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DocxConnectorNormalizerTest
{
    @TempDir Path root;
    private static final String PREFIX = "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:v=\"urn:schemas-microsoft-com:vml\"><w:body>";
    private static final String SECTION = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:left=\"1800\" w:right=\"1800\"/><w:cols/><w:docGrid w:type=\"lines\" w:linePitch=\"312\"/></w:sectPr></w:body></w:document>";
    private static final String P = "<w:p><w:pPr><w:rPr><w:sz w:val=\"32\"/></w:rPr></w:pPr><w:r>";
    private static final String END_P = "</w:r></w:p>";
    private static final String PICTURE = "<w:drawing><wp:anchor distR=\"114300\"><wp:positionH relativeFrom=\"column\"><wp:posOffset>219075</wp:posOffset></wp:positionH><wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>123825</wp:posOffset></wp:positionV><wp:extent cx=\"4087495\" cy=\"1537335\"/><wp:effectExtent r=\"8255\" b=\"5715\"/><wp:wrapSquare wrapText=\"bothSides\"/><pic:pic/></wp:anchor></w:drawing>";

    private String connector(long offset, String margin)
    {
        return "<mc:AlternateContent><mc:Choice Requires=\"a\"><w:drawing><wp:anchor><wp:positionH relativeFrom=\"column\"><wp:posOffset>" + offset + "</wp:posOffset></wp:positionH><wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>348615</wp:posOffset></wp:positionV><a:prstGeom prst=\"straightConnector1\"/></wp:anchor></w:drawing></mc:Choice><mc:Fallback><w:pict><v:shape type=\"#_x0000_t32\" style=\"position:absolute;margin-left:" + margin + "pt;margin-top:27.45pt;flip:y\"/></w:pict></mc:Fallback></mc:AlternateContent>";
    }

    private String xml(String content) { return PREFIX + content.replace("<wp:anchor distR=", "<wp:anchor distL=\"114300\" distR=") + SECTION; }
    private String normalized(String xml) throws Exception
    {
        byte[] result = DocxConnectorNormalizer.normalizeXml(xml.getBytes(StandardCharsets.UTF_8));
        return result == null ? null : new String(result, StandardCharsets.UTF_8);
    }

    @Test void compensatesWrapOriginInBothBranchesWithoutChangingVerticalOrDirection() throws Exception
    {
        // 17.25 + 321.85 + 0.65 + 9 = 348.75pt，不能取负偏移的绝对值。
        String result = normalized(xml(P + connector(-2276475, "-179.25") + PICTURE + END_P));
        assertNotNull(result);
        assertTrue(result.contains("<wp:posOffset>3295650</wp:posOffset>"));
        assertTrue(result.contains("relativeFrom=\"page\""));
        assertTrue(result.contains("mso-position-horizontal-relative:page"));
        assertTrue(result.contains("margin-left:259.5pt;margin-top:27.45pt;flip:y"));
        assertTrue(result.contains("<wp:posOffset>348615</wp:posOffset>"));
    }

    @Test void handlesLaterEmptyParagraphWhileImageStillWrapsIt() throws Exception
    {
        String result = normalized(xml(P + PICTURE + END_P + P + END_P + P + connector(-3667125, "-288.75") + END_P));
        assertTrue(result.contains("margin-left:150pt"));
    }

    @Test void isIdempotentAndDoesNotTouchPositiveOffsets() throws Exception
    {
        assertNull(normalized(xml(P + PICTURE + connector(2438400, "192") + END_P)));
        String once = normalized(xml(P + PICTURE + connector(-2276475, "-179.25") + END_P));
        assertNull(normalized(once));
    }

    @Test void preservesLegalNegativeCoordinatesInsidePage() throws Exception
    {
        assertNull(normalized(xml(P + PICTURE + connector(-127000, "-10") + END_P)));
    }

    @Test void doesNotCompensateWhenImageLeavesEnoughTextSpaceOnLeft() throws Exception
    {
        assertNull(normalized(xml(P + PICTURE.replace(">219075<", ">352425<") + connector(-2276475, "-179.25") + END_P)));
    }

    @Test void supportsHeadingAndSoftBreakAnchorsButSkipsPageBreaks() throws Exception
    {
        assertNotNull(normalized(xml(P + "<w:t>连线题</w:t><w:br/>" + PICTURE + connector(-2276475, "-179.25") + END_P)));
        assertNull(normalized(xml(P + "<w:br w:type=\"page\"/>" + PICTURE + connector(-2276475, "-179.25") + END_P)));
    }

    @Test void doesNotMoveRealOffPageDrawingsIntoAnAnswer() throws Exception
    {
        assertNull(normalized(xml(P + PICTURE + connector(-9000000, "-708.66141732") + END_P)));
    }

    @Test void preservesVmlLineAndOtherShapes() throws Exception
    {
        String oldLine = connector(-2276475, "-179.25").replace("v:shape", "v:line");
        assertNull(normalized(xml(P + PICTURE + oldLine + END_P)));
        assertNull(normalized(xml(P + PICTURE + connector(-2276475, "-179.25").replace("straightConnector1", "rect") + END_P)));
    }

    @Test void doesNotGuessWithoutWrapImageOrWithInconsistentFallback() throws Exception
    {
        assertNull(normalized(xml(P + connector(-2276475, "-179.25") + END_P)));
        assertNull(normalized(xml(P + PICTURE + connector(-2276475, "-100") + END_P)));
        assertNull(normalized(xml(P + PICTURE.replace("wrapSquare", "wrapNone") + connector(-2276475, "-179.25") + END_P)));
    }

    @Test void acceptsWordFallbackRoundingAndUsesPreciseDrawingPosition() throws Exception
    {
        String result = normalized(xml(P + PICTURE + connector(-2014537, "-158.6") + END_P));
        assertNotNull(result);
        assertTrue(result.contains("<wp:posOffset>3557588</wp:posOffset>"));
        assertFalse(result.contains("margin-left:-158.6pt"));
    }

    @Test void stopsAfterWrappedAreaAndAtTextOrTableBoundary() throws Exception
    {
        String gap = P + END_P;
        assertNull(normalized(xml(P + PICTURE + END_P + gap + gap + gap + gap + gap + P + connector(-2276475, "-179.25") + END_P)));
        assertNull(normalized(xml(P + PICTURE + END_P + P + "<w:t>新题</w:t>" + END_P + P + connector(-2276475, "-179.25") + END_P)));
        assertNull(normalized(xml("<w:tbl><w:tr><w:tc>" + P + PICTURE + connector(-2276475, "-179.25") + END_P + "</w:tc></w:tr></w:tbl>")));
    }

    @Test void leavesMulticolumnAndMultipleSectionsToOriginalConversion() throws Exception
    {
        String data = xml(P + PICTURE + connector(-2276475, "-179.25") + END_P);
        assertNull(normalized(data.replace("<w:cols/>", "<w:cols w:num=\"2\"/>")));
        assertNull(normalized(data.replace("<w:cols/>", "<w:cols/><w:sectPr/>")));
    }

    @Test void createsTemporaryCopyPreservesOriginalAndCleansItOnClose() throws Exception
    {
        File source = archive("student.docx", xml(P + PICTURE + connector(-2276475, "-179.25") + END_P));
        byte[] before = Files.readAllBytes(source.toPath());
        File copy;
        try (DocxConnectorNormalizer.PreparedInput prepared = DocxConnectorNormalizer.prepare(source))
        {
            copy = prepared.getFile();
            assertNotEquals(source, copy);
            assertEquals(source.getName(), copy.getName());
            try (ZipFile zip = new ZipFile(copy))
            {
                try (java.io.InputStream input = zip.getInputStream(zip.getEntry("word/media/image.png")))
                {
                    assertEquals(1, input.read());
                    assertEquals(2, input.read());
                    assertEquals(3, input.read());
                    assertEquals(-1, input.read());
                }
            }
            assertArrayEquals(before, Files.readAllBytes(source.toPath()));
        }
        assertFalse(copy.exists());
        assertFalse(copy.getParentFile().exists());
        assertArrayEquals(before, Files.readAllBytes(source.toPath()));
    }

    @Test void bypassesPlainDocxNonOfficeAndMissingDocumentXml() throws Exception
    {
        File plain = archive("plain.docx", xml(P + END_P));
        try (DocxConnectorNormalizer.PreparedInput p = DocxConnectorNormalizer.prepare(plain)) { assertEquals(plain, p.getFile()); }
        File missing = archive("missing.docx", null);
        try (DocxConnectorNormalizer.PreparedInput p = DocxConnectorNormalizer.prepare(missing)) { assertEquals(missing, p.getFile()); }
        File text = root.resolve("plain.txt").toFile();
        try (DocxConnectorNormalizer.PreparedInput p = DocxConnectorNormalizer.prepare(text)) { assertEquals(text, p.getFile()); }
    }

    @Test void fallsBackOnDamagedZipXmlAndRejectsExternalEntities() throws Exception
    {
        File broken = root.resolve("broken.docx").toFile();
        Files.write(broken.toPath(), new byte[] {1, 2, 3});
        try (DocxConnectorNormalizer.PreparedInput p = DocxConnectorNormalizer.prepare(broken)) { assertEquals(broken, p.getFile()); }
        File xmlBroken = archive("bad-xml.docx", "<broken>");
        try (DocxConnectorNormalizer.PreparedInput p = DocxConnectorNormalizer.prepare(xmlBroken)) { assertEquals(xmlBroken, p.getFile()); }
        assertThrows(Exception.class, () -> normalized("<!DOCTYPE x [<!ENTITY ext SYSTEM 'file:///unavailable'>]>" + xml(P + "<w:t>&ext;</w:t>" + END_P)));
    }

    private File archive(String name, String document) throws Exception
    {
        File file = root.resolve(name).toFile();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file.toPath())))
        {
            if (document != null)
            {
                zip.putNextEntry(new ZipEntry("word/document.xml"));
                zip.write(document.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            zip.putNextEntry(new ZipEntry("word/media/image.png"));
            zip.write(new byte[] {1, 2, 3});
            zip.closeEntry();
        }
        return file;
    }
}
