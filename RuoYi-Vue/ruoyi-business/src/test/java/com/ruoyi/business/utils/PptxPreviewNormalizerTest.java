package com.ruoyi.business.utils;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class PptxPreviewNormalizerTest
{
    private static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
    private static final String P = "http://schemas.openxmlformats.org/presentationml/2006/main";
    @TempDir Path directory;

    @Test void keepsSavedOutOfBoundsBendsInsteadOfClampingOrRerouting() throws Exception
    {
        Document result = parse(normalize(connector("bentConnector5",
                "<a:gd name='adj1' fmla='val -6407'/><a:gd name='adj2' fmla='val -143286'/>"
                + "<a:gd name='adj3' fmla='val 100000'/>")));
        assertEquals(0, result.getElementsByTagNameNS(P, "cxnSp").getLength());
        assertEquals(0, result.getElementsByTagNameNS(A, "stCxn").getLength());
        assertEquals(6, result.getElementsByTagNameNS(A, "pt").getLength());
        Element bend = (Element) result.getElementsByTagNameNS(A, "pt").item(2);
        assertEquals("-6407", bend.getAttribute("x"));
        assertEquals("-143286", bend.getAttribute("y"));
        Element transform = (Element) result.getElementsByTagNameNS(A, "xfrm").item(0);
        assertEquals("5400000", transform.getAttribute("rot"));
        assertEquals("1", transform.getAttribute("flipH"));
        assertEquals("triangle", ((Element) result.getElementsByTagNameNS(A, "tailEnd").item(0)).getAttribute("type"));
        assertEquals("82", ((Element) result.getElementsByTagNameNS(P, "cNvPr").item(0)).getAttribute("id"));
    }

    @Test void preservesAllSupportedPresetSegmentCountsAndDefaultAdjustments() throws Exception
    {
        for (int segments = 1; segments <= 5; segments++)
        {
            String type = segments == 1 ? "straightConnector1" : "bentConnector" + segments;
            Document doc = parse(normalize(connector(type, "")));
            assertEquals(segments + 1, doc.getElementsByTagNameNS(A, "pt").getLength());
        }
        Document doc = parse(normalize(connector("bentConnector3", "")));
        assertEquals("50000", ((Element) doc.getElementsByTagNameNS(A, "pt").item(1)).getAttribute("x"));
    }

    @Test void leavesUnknownCurvesAndNonConstantGuidesUntouched() throws Exception
    {
        assertNull(normalize(connector("curvedConnector3", "")));
        assertNull(normalize(connector("bentConnector3", "<a:gd name='adj1' fmla='*/ w 1 2'/>")));
    }

    @Test void protectsOnlyAuthoredShortSingleLineInputOutputBoxes() throws Exception
    {
        Document doc = parse(normalize(textBox("", "", "292608")));
        assertEquals("none", ((Element) doc.getElementsByTagNameNS(A, "bodyPr").item(0)).getAttribute("wrap"));
        assertNull(normalize(textBox("wrap='square'", "", "292608")));
        assertNull(normalize(textBox("vert='vert'", "", "292608")));
        assertNull(normalize(textBox("", "<a:br/>", "292608")));
        assertNull(normalize(textBox("", "", "900000")));
    }

    @Test void repeatedPreparationDoesNotChangeAlreadyFrozenGeometry() throws Exception
    {
        byte[] first = normalize(connector("bentConnector4", ""));
        assertNotNull(first);
        assertNull(PptxPreviewNormalizer.normalizeXml(first));
    }

    @Test void copyKeepsOriginalAndOtherZipEntriesAndCleansOnlyItsOwnDirectory() throws Exception
    {
        Path source = directory.resolve("original.pptx");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(source)))
        {
            out.putNextEntry(new ZipEntry("ppt/slides/slide1.xml"));
            out.write(wrap(connector("bentConnector2", "")).getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new ZipEntry("ppt/media/image1.bin"));
            out.write(new byte[] {1,2,3,4}); out.closeEntry();
        }
        byte[] before = Files.readAllBytes(source);
        Path temporary;
        try (PptxPreviewNormalizer.PreparedInput prepared = PptxPreviewNormalizer.prepare(source.toFile()))
        {
            temporary = prepared.getFile().toPath();
            assertNotEquals(source, temporary);
            assertEquals(source.getFileName(), temporary.getFileName());
            try (ZipFile zip = new ZipFile(temporary.toFile()))
            { assertEquals(4, zip.getEntry("ppt/media/image1.bin").getSize()); }
            assertArrayEquals(before, Files.readAllBytes(source));
        }
        assertFalse(Files.exists(temporary));
        assertTrue(Files.exists(source));
    }

    @Test void malformedPackageFallsBackAndOtherFormatsAreNotOpened() throws Exception
    {
        Path bad = directory.resolve("bad.pptx"); Files.write(bad, new byte[] {1,2});
        try (PptxPreviewNormalizer.PreparedInput input = PptxPreviewNormalizer.prepare(bad.toFile()))
        { assertEquals(bad.toFile(), input.getFile()); }
        Path other = directory.resolve("sample.docx");
        try (PptxPreviewNormalizer.PreparedInput input = PptxPreviewNormalizer.prepare(other.toFile()))
        { assertEquals(other.toFile(), input.getFile()); }
    }

    @Test void refusesExternalEntityDocuments() throws Exception
    {
        String xml = "<!DOCTYPE p:sld [<!ENTITY x SYSTEM 'file:///missing-private-file'>]>" + wrap("&x;");
        assertThrows(Exception.class, () -> PptxPreviewNormalizer.normalizeXml(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] normalize(String content) throws Exception
    { return PptxPreviewNormalizer.normalizeXml(wrap(content).getBytes(StandardCharsets.UTF_8)); }
    private static String wrap(String content)
    { return "<p:sld xmlns:p='" + P + "' xmlns:a='" + A + "'><p:cSld><p:spTree>" + content + "</p:spTree></p:cSld></p:sld>"; }
    private static Document parse(byte[] xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }
    private static String connector(String type, String guides)
    {
        return "<p:cxnSp><p:nvCxnSpPr><p:cNvPr id='82' name='连线'/><p:cNvCxnSpPr>"
                + "<a:stCxn id='2' idx='0'/><a:endCxn id='3' idx='1'/></p:cNvCxnSpPr><p:nvPr/></p:nvCxnSpPr>"
                + "<p:spPr><a:xfrm rot='5400000' flipH='1'><a:off x='1' y='2'/><a:ext cx='0' cy='100'/></a:xfrm>"
                + "<a:prstGeom prst='" + type + "'><a:avLst>" + guides + "</a:avLst></a:prstGeom>"
                + "<a:ln><a:tailEnd type='triangle'/></a:ln></p:spPr></p:cxnSp>";
    }
    private static String textBox(String attributes, String extra, String height)
    {
        return "<p:sp><p:spPr><a:xfrm><a:ext cx='1773936' cy='" + height + "'/></a:xfrm>"
                + "<a:prstGeom prst='flowChartInputOutput'/></p:spPr><p:txBody><a:bodyPr " + attributes + "/>"
                + "<a:p><a:r><a:rPr sz='1200'/><a:t>输入cai的值</a:t></a:r>" + extra + "</a:p></p:txBody></p:sp>";
    }
}
