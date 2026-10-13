package com.ruoyi.business.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Word 会从环绕底图右侧计算连接符负偏移；LibreOffice 从正文栏左侧计算，导致连线出界。
 * 只对可确定环绕原点的双分支直线连接符制作转换副本，不猜测表格、多栏或普通负坐标。
 */
public final class DocxConnectorNormalizer
{
    private static final Logger log = LoggerFactory.getLogger(DocxConnectorNormalizer.class);
    private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    private static final String WP = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";
    private static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
    private static final String MC = "http://schemas.openxmlformats.org/markup-compatibility/2006";
    private static final String V = "urn:schemas-microsoft-com:vml";
    private static final Pattern MARGIN = Pattern.compile("(?:^|;)margin-left:([-+]?\\d+(?:\\.\\d+)?)pt(?=;|$)");
    private static final int MAX_XML_BYTES = 8 * 1024 * 1024;

    private DocxConnectorNormalizer() {}

    public static PreparedInput prepare(File source)
    {
        Path directory = null;
        if (!source.getName().toLowerCase(Locale.ROOT).endsWith(".docx")) return new PreparedInput(source, null);
        try (ZipFile archive = new ZipFile(source))
        {
            ZipEntry documentEntry = archive.getEntry("word/document.xml");
            if (documentEntry == null) return new PreparedInput(source, null);
            byte[] original;
            try (InputStream input = archive.getInputStream(documentEntry)) { original = readBounded(input); }
            byte[] normalized = normalizeXml(original);
            if (normalized == null) return new PreparedInput(source, null);
            directory = Files.createTempDirectory("newdazi-connector-");
            // 保留原文件名，使一次性 LibreOffice 进程仍生成调用方约定的 PDF 名称。
            Path target = directory.resolve(source.getName());
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(target)))
            {
                Enumeration<? extends ZipEntry> entries = archive.entries();
                byte[] buffer = new byte[8192];
                while (entries.hasMoreElements())
                {
                    ZipEntry entry = entries.nextElement();
                    ZipEntry copied = new ZipEntry(entry.getName());
                    copied.setTime(entry.getTime());
                    output.putNextEntry(copied);
                    if (entry.getName().equals("word/document.xml")) output.write(normalized);
                    else try (InputStream input = archive.getInputStream(entry))
                    {
                        int size;
                        while ((size = input.read(buffer)) != -1) output.write(buffer, 0, size);
                    }
                    output.closeEntry();
                }
            }
            log.info("【连接符兼容】已生成转换副本，学生原件保持不变");
            return new PreparedInput(target.toFile(), directory);
        }
        catch (Exception e)
        {
            new PreparedInput(source, directory).close();
            log.info("【连接符兼容】预处理未完成，继续使用原文件: {}", e.getClass().getSimpleName());
            return new PreparedInput(source, null);
        }
    }

    static byte[] normalizeXml(byte[] original) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(original));
        NodeList sections = doc.getElementsByTagNameNS(W, "sectPr");
        if (sections.getLength() != 1) return null;
        Element section = (Element) sections.item(0);
        Element cols = child(section, W, "cols");
        if (cols != null && (number(cols, W, "num", 1) != 1 || child(cols, W, "col") != null)) return null;
        Element pageMargin = child(section, W, "pgMar");
        Element pageSize = child(section, W, "pgSz");
        if (pageMargin == null || pageSize == null) return null;
        long left = number(pageMargin, W, "left", 0) * 635;
        long columnWidth = (number(pageSize, W, "w", 0) - number(pageMargin, W, "left", 0)
                - number(pageMargin, W, "right", 0)) * 635;
        Element grid = child(section, W, "docGrid");
        long pitch = grid != null && "lines".equals(grid.getAttributeNS(W, "type"))
                ? number(grid, W, "linePitch", 0) * 635 : 0;
        Element body = child(doc.getDocumentElement(), W, "body");
        if (body == null || columnWidth <= 0) return null;
        long origin = 0, top = 0, bottom = 0, y = 0;
        boolean changed = false;
        for (Node node = body.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (!(node instanceof Element)) continue;
            Element paragraph = (Element) node;
            if (!W.equals(paragraph.getNamespaceURI()) || !"p".equals(paragraph.getLocalName()))
            {
                origin = 0;
                continue;
            }
            if (paragraph.getElementsByTagNameNS(W, "t").getLength() > 0) origin = 0;
            NodeList breaks = paragraph.getElementsByTagNameNS(W, "br");
            boolean pageBreak = false;
            for (int i = 0; i < breaks.getLength(); i++)
            {
                String type = ((Element) breaks.item(i)).getAttributeNS(W, "type");
                pageBreak |= "page".equals(type) || "column".equals(type);
            }
            if (pageBreak) { origin = 0; continue; }
            long lineHeight = lineHeight(paragraph, pitch);
            NodeList anchors = paragraph.getElementsByTagNameNS(WP, "anchor");
            // 同段连接符可能出现在底图 XML 之前，须先确定环绕边界再修正连接符。
            for (int i = 0; i < anchors.getLength(); i++)
            {
                Element anchor = (Element) anchors.item(i);
                Element wrap = child(anchor, WP, "wrapSquare");
                Element h = child(anchor, WP, "positionH"), v = child(anchor, WP, "positionV");
                Element extent = child(anchor, WP, "extent"), effect = child(anchor, WP, "effectExtent");
                if (wrap == null || !"bothSides".equals(wrap.getAttribute("wrapText"))
                        || anchor.getElementsByTagNameNS("http://schemas.openxmlformats.org/drawingml/2006/picture", "pic").getLength() != 1
                        || h == null || !"column".equals(h.getAttribute("relativeFrom"))
                        || v == null || !"paragraph".equals(v.getAttribute("relativeFrom"))
                        || child(h, WP, "posOffset") == null || child(v, WP, "posOffset") == null || extent == null) continue;
                // 底图左侧能容下字符时，Word 可继续从栏左侧定位，不能强行补偿到右侧。
                // 只覆盖已验证的左侧窄空隙情形，较宽空隙保留原转换行为。
                long leftSpace = offset(h) - number(effect, null, "l", 0) - number(anchor, null, "distL", 0);
                if (leftSpace >= fontSize(paragraph) * 6350) { origin = 0; continue; }
                origin = offset(h) + number(extent, null, "cx", 0)
                        + number(effect, null, "r", 0) + number(anchor, null, "distR", 0);
                top = offset(v) - number(effect, null, "t", 0) - number(anchor, null, "distT", 0);
                bottom = offset(v) + number(extent, null, "cy", 0)
                        + number(effect, null, "b", 0) + number(anchor, null, "distB", 0);
                y = 0;
            }
            if (origin > 0 && origin < columnWidth && y < bottom && y + lineHeight > top)
            {
                for (int i = 0; i < anchors.getLength(); i++)
                    changed |= normalizeConnector((Element) anchors.item(i), origin, left, columnWidth);
            }
            y += lineHeight;
            if (y >= bottom) origin = 0;
        }
        if (!changed) return null;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output.toByteArray();
    }

    private static boolean normalizeConnector(Element anchor, long origin, long left, long width)
    {
        Element h = child(anchor, WP, "positionH");
        if (h == null || !"column".equals(h.getAttribute("relativeFrom")) || child(h, WP, "posOffset") == null) return false;
        long old = offset(h), corrected = old + origin;
        if (old >= 0 || left + old >= 0 || corrected < 0 || corrected >= width) return false;
        NodeList geometries = anchor.getElementsByTagNameNS(A, "prstGeom");
        if (geometries.getLength() != 1 || !"straightConnector1".equals(((Element) geometries.item(0)).getAttribute("prst"))) return false;
        Node alternate = anchor;
        while (alternate != null && !(MC.equals(alternate.getNamespaceURI()) && "AlternateContent".equals(alternate.getLocalName()))) alternate = alternate.getParentNode();
        if (!(alternate instanceof Element)) return false;
        Element fallback = child((Element) alternate, MC, "Fallback");
        if (fallback == null || fallback.getElementsByTagNameNS(V, "line").getLength() != 0) return false;
        NodeList shapes = fallback.getElementsByTagNameNS(V, "shape");
        if (shapes.getLength() != 1) return false;
        Element shape = (Element) shapes.item(0);
        if (!"#_x0000_t32".equals(shape.getAttribute("type"))) return false;
        String style = shape.getAttribute("style");
        Matcher margin = MARGIN.matcher(style);
        if (!margin.find()) return false;
        BigDecimal oldMargin = new BigDecimal(margin.group(1));
        // Word 的 VML 分支可能按 0.05pt 取整；超出这个误差才视为两分支真正不一致。
        if (oldMargin.multiply(BigDecimal.valueOf(12700)).subtract(BigDecimal.valueOf(old)).abs().doubleValue() > 635) return false;
        String replacement = BigDecimal.valueOf(left + corrected).divide(BigDecimal.valueOf(12700), 8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        String newStyle = style.substring(0, margin.start(1)) + replacement + style.substring(margin.end(1));
        newStyle = newStyle.replaceAll("mso-position-horizontal-relative:[^;]+", "mso-position-horizontal-relative:page");
        if (!newStyle.contains("mso-position-horizontal-relative:")) newStyle += ";mso-position-horizontal-relative:page";
        shape.setAttribute("style", newStyle);
        // LibreOffice 的两种导入分支对环绕原点的解释不同，统一为页面绝对坐标避免重复补偿。
        h.setAttribute("relativeFrom", "page");
        child(h, WP, "posOffset").setTextContent(Long.toString(left + corrected));
        return true;
    }

    private static long lineHeight(Element paragraph, long pitch)
    {
        Element properties = child(paragraph, W, "pPr");
        Element spacing = child(properties, W, "spacing");
        if (spacing != null && "exact".equals(spacing.getAttributeNS(W, "lineRule"))) return number(spacing, W, "line", 240) * 635;
        long size = fontSize(paragraph);
        long height = size * 6350 * 6 / 5;
        return pitch > 0 ? ((height + pitch - 1) / pitch) * pitch : height;
    }

    private static long fontSize(Element paragraph)
    {
        long size = 24;
        NodeList sizes = paragraph.getElementsByTagNameNS(W, "sz");
        for (int i = 0; i < sizes.getLength(); i++) size = Math.max(size, number((Element) sizes.item(i), W, "val", 24));
        return size;
    }

    private static Element child(Element parent, String namespace, String name)
    {
        if (parent == null) return null;
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling())
            if (node instanceof Element && namespace.equals(node.getNamespaceURI()) && name.equals(node.getLocalName())) return (Element) node;
        return null;
    }

    private static long number(Element element, String namespace, String name, long fallback)
    {
        if (element == null) return fallback;
        String value = namespace == null ? element.getAttribute(name) : element.getAttributeNS(namespace, name);
        return value.isEmpty() ? fallback : Long.parseLong(value);
    }

    private static long offset(Element position) { return Long.parseLong(child(position, WP, "posOffset").getTextContent()); }

    private static byte[] readBounded(InputStream input) throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int size;
        while ((size = input.read(buffer)) != -1)
        {
            if (bytes.size() + size > MAX_XML_BYTES) throw new IllegalArgumentException("XML 过大");
            bytes.write(buffer, 0, size);
        }
        return bytes.toByteArray();
    }

    public static final class PreparedInput implements AutoCloseable
    {
        private final File file;
        private final Path directory;
        private PreparedInput(File file, Path directory) { this.file = file; this.directory = directory; }
        public File getFile() { return file; }
        @Override public void close()
        {
            if (directory == null) return;
            try
            {
                // 临时目录只有本次创建的副本，不递归删除任何学生作品或其他目录。
                try (java.nio.file.DirectoryStream<Path> files = Files.newDirectoryStream(directory))
                { for (Path path : files) Files.deleteIfExists(path); }
                Files.deleteIfExists(directory);
            }
            catch (Exception e) { log.info("【连接符兼容】临时副本清理失败: {}", e.getClass().getSimpleName()); }
        }
    }
}
