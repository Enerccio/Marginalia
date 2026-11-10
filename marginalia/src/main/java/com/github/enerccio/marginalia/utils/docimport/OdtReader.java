package com.github.enerccio.marginalia.utils.docimport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads an OpenDocument text ({@code .odt}, what LibreOffice and OpenOffice save). A heading is a {@code text:h} with its
 * outline level, a paragraph of the style {@code Title} is the title of the document. Bold and italic come from the
 * automatic styles of the text and its paragraphs (and the styles they are based on); the text of tracked deletions, notes,
 * comments and the table of contents is left out.
 */
final class OdtReader {

    private static final String TEXT = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
    private static final String STYLE = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
    private static final String FO = "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0";

    private static final int MAX_XML_BYTES = 128 * 1024 * 1024;

    private record Look(String parent, boolean bold, boolean italic) {
    }

    private final Map<String, Look> styles = new HashMap<>();
    private final List<TextBlock> blocks = new ArrayList<>();
    private String title;

    private OdtReader() {
    }

    static TextDocument read(byte[] data) {
        byte[] content = null;
        byte[] styles = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("content.xml") || entry.getName().equals("styles.xml")) {
                    byte[] bytes = zip.readNBytes(MAX_XML_BYTES + 1);
                    if (bytes.length > MAX_XML_BYTES) {
                        throw new IllegalArgumentException("Document is too big");
                    }
                    if (entry.getName().equals("content.xml")) {
                        content = bytes;
                    } else {
                        styles = bytes;
                    }
                }
            }
            if (content == null) {
                throw new IllegalArgumentException("Not an OpenDocument text: no content.xml");
            }
            OdtReader reader = new OdtReader();
            if (styles != null) {
                reader.collectStyles(parse(styles));
            }
            Document document = parse(content);
            reader.collectStyles(document);
            NodeList bodies = document.getElementsByTagNameNS("urn:oasis:names:tc:opendocument:xmlns:office:1.0", "text");
            if (bodies.getLength() == 0) {
                throw new IllegalArgumentException("Not an OpenDocument text: no text body");
            }
            reader.walk((Element) bodies.item(0));
            return new TextDocument(reader.title, reader.blocks);
        } catch (IOException e) {
            throw new IllegalArgumentException("Not an OpenDocument text: " + e.getMessage(), e);
        }
    }

    private static Document parse(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // the file comes from the user's upload: no DTDs, no external entities
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new IllegalArgumentException("Not an OpenDocument text: " + e.getMessage(), e);
        }
    }

    private void collectStyles(Document document) {
        NodeList all = document.getElementsByTagNameNS(STYLE, "style");
        for (int i = 0; i < all.getLength(); i++) {
            Element style = (Element) all.item(i);
            String name = style.getAttributeNS(STYLE, "name");
            boolean bold = false;
            boolean italic = false;
            for (Node child = style.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof Element properties && STYLE.equals(properties.getNamespaceURI())
                        && properties.getLocalName().equals("text-properties")) {
                    italic = "italic".equals(properties.getAttributeNS(FO, "font-style"))
                            || "oblique".equals(properties.getAttributeNS(FO, "font-style"));
                    String weight = properties.getAttributeNS(FO, "font-weight");
                    bold = "bold".equals(weight) || weight.matches("[6-9]00");
                }
            }
            styles.put(name, new Look(style.getAttributeNS(STYLE, "parent-style-name"), bold, italic));
        }
    }

    // a style is bold or italic itself or through the style it is based on
    private boolean has(String styleName, boolean wantBold) {
        for (int depth = 0; depth < 10 && styleName != null && !styleName.isEmpty(); depth++) {
            Look look = styles.get(styleName);
            if (look == null) {
                return false;
            }
            if (wantBold ? look.bold : look.italic) {
                return true;
            }
            styleName = look.parent;
        }
        return false;
    }

    private void walk(Element parent) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (!(node instanceof Element element)) {
                continue;
            }
            String local = element.getLocalName();
            if (TEXT.equals(element.getNamespaceURI())) {
                switch (local) {
                    case "h" -> {
                        RichText text = new RichText();
                        inline(element, text, false, false);
                        if (!text.isBlank()) {
                            blocks.add(new TextBlock(level(element), text.plain()));
                        }
                        continue;
                    }
                    case "p" -> {
                        paragraph(element);
                        continue;
                    }
                    case "tracked-changes", "table-of-content", "illustration-index", "table-index", "object-index",
                         "user-index", "alphabetical-index", "bibliography" -> {
                        continue;
                    }
                    default -> {
                    }
                }
            }
            // lists, sections, tables and their rows and cells hold paragraphs
            walk(element);
        }
    }

    private int level(Element heading) {
        String level = heading.getAttributeNS(TEXT, "outline-level");
        try {
            return Math.max(1, Math.min(TextBlock.MAX_LEVEL, Integer.parseInt(level)));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void paragraph(Element element) {
        String style = element.getAttributeNS(TEXT, "style-name");
        RichText text = new RichText();
        inline(element, text, has(style, true), has(style, false));
        if (text.isBlank()) {
            return;
        }
        if (style.equals("Title")) {
            if (title == null) {
                title = text.plain();
            }
            return;
        }
        blocks.add(new TextBlock(0, text.markdown()));
    }

    private void inline(Element element, RichText out, boolean bold, boolean italic) {
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof org.w3c.dom.Text text) {
                out.add(text.getData(), bold, italic);
            } else if (node instanceof Element child && TEXT.equals(child.getNamespaceURI())) {
                switch (child.getLocalName()) {
                    case "s" -> {
                        String count = child.getAttributeNS(TEXT, "c");
                        out.add(" ".repeat(count.matches("\\d{1,3}") ? Math.max(1, Integer.parseInt(count)) : 1), bold, italic);
                    }
                    case "tab" -> out.add(" ", bold, italic);
                    case "line-break" -> out.add("\n", bold, italic);
                    case "span" -> {
                        String style = child.getAttributeNS(TEXT, "style-name");
                        inline(child, out, bold || has(style, true), italic || has(style, false));
                    }
                    case "note", "annotation", "annotation-end", "bookmark", "bookmark-start", "bookmark-end", "tracked-changes" -> {
                    }
                    default -> inline(child, out, bold, italic);
                }
            }
            // elements of other namespaces inside a paragraph (comments of office:, drawing frames) are not text of the story
        }
    }
}
