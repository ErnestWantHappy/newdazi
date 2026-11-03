package com.ruoyi.business.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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

/** 仅在转换副本中固定 PowerPoint 已保存的连接线路径，避免 Impress 重新布线。 */
public final class PptxPreviewNormalizer
{
    private static final Logger log = LoggerFactory.getLogger(PptxPreviewNormalizer.class);
    private static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
    private static final String P = "http://schemas.openxmlformats.org/presentationml/2006/main";
    private static final int MAX_XML_BYTES = 8 * 1024 * 1024;
    private static final long MAX_PACKAGE_BYTES = 256L * 1024 * 1024;

    private PptxPreviewNormalizer() {}

    public static PreparedInput prepare(File source)
    {
        Path directory = null;
        if (!source.getName().toLowerCase(Locale.ROOT).endsWith(".pptx")) return new PreparedInput(source, null);
        try (ZipFile archive = new ZipFile(source))
        {
            Map<String, byte[]> replacements = new LinkedHashMap<>();
            Enumeration<? extends ZipEntry> entries = archive.entries();
            long xmlBytes = 0;
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().matches("ppt/slides/slide[0-9]+\\.xml")) continue;
                byte[] original;
                try (InputStream input = archive.getInputStream(entry)) { original = readXml(input); }
                xmlBytes += original.length;
                if (xmlBytes > MAX_PACKAGE_BYTES) throw new IllegalArgumentException("演示文稿过大");
                byte[] normalized = normalizeXml(original);
                if (normalized != null) replacements.put(entry.getName(), normalized);
            }
            if (replacements.isEmpty()) return new PreparedInput(source, null);
            directory = Files.createTempDirectory("newdazi-ppt-preview-");
            Path target = directory.resolve(source.getName());
            try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(target)))
            {
                entries = archive.entries();
                byte[] buffer = new byte[8192];
                long copied = 0;
                while (entries.hasMoreElements())
                {
                    ZipEntry entry = entries.nextElement();
                    output.putNextEntry(new ZipEntry(entry.getName()));
                    byte[] replacement = replacements.get(entry.getName());
                    if (replacement != null) { output.write(replacement); copied += replacement.length; }
                    else try (InputStream input = archive.getInputStream(entry))
                    {
                        int size;
                        while ((size = input.read(buffer)) != -1)
                        {
                            copied += size;
                            if (copied > MAX_PACKAGE_BYTES) throw new IllegalArgumentException("演示文稿过大");
                            output.write(buffer, 0, size);
                        }
                    }
                    output.closeEntry();
                }
            }
            return new PreparedInput(target.toFile(), directory);
        }
        catch (Exception e)
        {
            new PreparedInput(source, directory).close();
            log.info("【PPT预览兼容】保持原转换: {}", e.getClass().getSimpleName());
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
        boolean changed = false;
        // DOM列表是实时视图；从末尾替换，避免漏处理相邻连接符。
        NodeList connectors = doc.getElementsByTagNameNS(P, "cxnSp");
        for (int i = connectors.getLength() - 1; i >= 0; i--)
            changed |= freezeConnector((Element) connectors.item(i));
        NodeList shapes = doc.getElementsByTagNameNS(P, "sp");
        for (int i = 0; i < shapes.getLength(); i++) changed |= preserveSingleLine((Element) shapes.item(i));
        if (!changed) return null;
        TransformerFactory transformers = TransformerFactory.newInstance();
        transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = transformers.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output.toByteArray();
    }

    private static boolean freezeConnector(Element connector)
    {
        Element properties = child(connector, P, "spPr");
        Element preset = child(properties, A, "prstGeom");
        Element transform = child(properties, A, "xfrm");
        Element nonVisual = child(connector, P, "nvCxnSpPr");
        if (preset == null || transform == null || nonVisual == null
                || child(transform, A, "off") == null || child(transform, A, "ext") == null) return false;
        String type = preset.getAttribute("prst");
        if (!type.matches("straightConnector1|bentConnector[2-5]")) return false;
        long[] adjustments = { 50000, 50000, 50000 };
        Element list = child(preset, A, "avLst");
        if (list != null)
        {
            NodeList guides = list.getElementsByTagNameNS(A, "gd");
            for (int i = 0; i < guides.getLength(); i++)
            {
                Element guide = (Element) guides.item(i);
                String name = guide.getAttribute("name"), formula = guide.getAttribute("fmla").trim();
                if (!name.matches("adj[1-3]") || !formula.matches("val [-+]?\\d{1,10}")) return false;
                long value = Long.parseLong(formula.substring(4));
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) return false;
                adjustments[name.charAt(3) - '1'] = value;
            }
        }
        long x1 = adjustments[0], y2 = adjustments[1], x3 = adjustments[2], end = 100000;
        long[][] points;
        switch (type)
        {
            case "straightConnector1": points = new long[][] {{0,0},{end,end}}; break;
            case "bentConnector2": points = new long[][] {{0,0},{end,0},{end,end}}; break;
            case "bentConnector3": points = new long[][] {{0,0},{x1,0},{x1,end},{end,end}}; break;
            case "bentConnector4": points = new long[][] {{0,0},{x1,0},{x1,y2},{end,y2},{end,end}}; break;
            default: points = new long[][] {{0,0},{x1,0},{x1,y2},{x3,y2},{x3,end},{end,end}};
        }
        Document doc = connector.getOwnerDocument();
        Element geometry = doc.createElementNS(A, "a:custGeom");
        for (String tag : new String[] {"avLst", "gdLst", "ahLst", "cxnLst"}) append(geometry, tag);
        Element rect = append(geometry, "rect");
        for (String side : new String[] {"l", "t", "r", "b"}) rect.setAttribute(side, side);
        Element path = append(append(geometry, "pathLst"), "path");
        path.setAttribute("w", "100000"); path.setAttribute("h", "100000"); path.setAttribute("fill", "none");
        for (int i = 0; i < points.length; i++)
        {
            Element point = append(append(path, i == 0 ? "moveTo" : "lnTo"), "pt");
            point.setAttribute("x", String.valueOf(points[i][0]));
            point.setAttribute("y", String.valueOf(points[i][1]));
        }
        properties.replaceChild(geometry, preset);
        // 普通路径不参与自动布线；原旋转/翻转、颜色、箭头、层级与ID仍保留。
        doc.renameNode(connector, P, "p:sp");
        doc.renameNode(nonVisual, P, "p:nvSpPr");
        Element old = child(nonVisual, P, "cNvCxnSpPr");
        if (old != null) nonVisual.replaceChild(doc.createElementNS(P, "p:cNvSpPr"), old);
        return true;
    }

    private static boolean preserveSingleLine(Element shape)
    {
        Element preset = child(child(shape, P, "spPr"), A, "prstGeom");
        if (preset == null || !"flowChartInputOutput".equals(preset.getAttribute("prst"))) return false;
        Element body = child(shape, P, "txBody"), bodyPr = child(body, A, "bodyPr");
        Element extent = child(child(child(shape, P, "spPr"), A, "xfrm"), A, "ext");
        if (bodyPr == null || extent == null || bodyPr.hasAttribute("wrap") || bodyPr.hasAttribute("vert")
                || child(bodyPr, A, "spAutoFit") != null || child(bodyPr, A, "normAutofit") != null
                || body.getElementsByTagNameNS(A, "p").getLength() != 1
                || body.getElementsByTagNameNS(A, "br").getLength() != 0) return false;
        NodeList runs = body.getElementsByTagNameNS(A, "rPr");
        long font = 0;
        for (int i = 0; i < runs.getLength(); i++)
        {
            String size = ((Element) runs.item(i)).getAttribute("sz");
            if (!size.matches("\\d+")) return false;
            font = Math.max(font, Long.parseLong(size));
        }
        String height = extent.getAttribute("cy");
        if (font == 0 || !height.matches("\\d+") || Long.parseLong(height) > font * 127 * 2) return false;
        // 只保护原文单段、明确字号且高度仅容一行的输入输出框；显式换行/自动适应保持原样。
        bodyPr.setAttribute("wrap", "none");
        return true;
    }

    private static Element append(Element parent, String name)
    {
        Element element = parent.getOwnerDocument().createElementNS(A, "a:" + name);
        parent.appendChild(element);
        return element;
    }

    private static Element child(Element parent, String namespace, String name)
    {
        if (parent == null) return null;
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling())
            if (node instanceof Element && namespace.equals(node.getNamespaceURI()) && name.equals(node.getLocalName()))
                return (Element) node;
        return null;
    }

    private static byte[] readXml(InputStream input) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int size;
        while ((size = input.read(buffer)) != -1)
        {
            if (output.size() + size > MAX_XML_BYTES) throw new IllegalArgumentException("XML过大");
            output.write(buffer, 0, size);
        }
        return output.toByteArray();
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
            try (java.nio.file.DirectoryStream<Path> files = Files.newDirectoryStream(directory))
            { for (Path path : files) Files.deleteIfExists(path); }
            catch (Exception e) { log.info("【PPT预览兼容】副本清理失败: {}", e.getClass().getSimpleName()); }
            try { Files.deleteIfExists(directory); }
            catch (Exception e) { log.info("【PPT预览兼容】目录清理失败: {}", e.getClass().getSimpleName()); }
        }
    }
}
