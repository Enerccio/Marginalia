package com.github.enerccio.marginalia.utils.docimport;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads an EPUB (a zip of XHTML pages, EPUB 2 and 3). The pages of the spine are read in their order ({@link XhtmlReader}):
 * the headings {@code h1} to {@code h6} start the chapters, the title is the one of the metadata. A page without a heading
 * that the table of contents names (the NCX, or the navigation document of EPUB 3) gets that name as the heading, at the depth
 * of the entry. The cover, the table of contents and the navigation document are not read.
 * A lone {@code h1} before the deeper headings (a title page) is the title of the book, not a chapter.
 */
final class EpubReader {

    private static final String DC = "http://purl.org/dc/elements/1.1/";
    private static final Pattern NAV_START = Pattern.compile("<nav\\b[^>]*epub:type\\s*=\\s*[\"'][^\"']*\\btoc\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAV_TOKEN = Pattern.compile("<(/?)(ol|a|nav)\\b([^>]*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HREF = Pattern.compile("href\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)')", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAGS = Pattern.compile("<[^>]*>");

    private record Entry(String label, int depth) {
    }

    private record Item(String path, String type, String properties) {
    }

    private final Map<String, byte[]> files;
    private final Map<String, Entry> toc = new HashMap<>();

    private EpubReader(Map<String, byte[]> files) {
        this.files = files;
    }

    static TextDocument read(byte[] data) {
        Map<String, byte[]> files = ZipFiles.read(data, EpubReader::wanted);
        String opfPath = packagePath(files);
        Document opf = parse(files.get(opfPath));
        String base = opfPath.contains("/") ? opfPath.substring(0, opfPath.lastIndexOf('/') + 1) : "";

        Map<String, Item> manifest = new LinkedHashMap<>();
        NodeList items = opf.getElementsByTagNameNS("*", "item");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            manifest.put(item.getAttribute("id"), new Item(resolve(base, item.getAttribute("href")),
                    item.getAttribute("media-type"), item.getAttribute("properties")));
        }
        Set<String> skipped = new HashSet<>();
        NodeList references = opf.getElementsByTagNameNS("*", "reference");
        for (int i = 0; i < references.getLength(); i++) {
            Element reference = (Element) references.item(i);
            if (reference.getAttribute("type").equals("cover") || reference.getAttribute("type").equals("toc")) {
                skipped.add(resolve(base, reference.getAttribute("href")));
            }
        }

        EpubReader reader = new EpubReader(files);
        Element spine = first(opf, "spine");
        reader.readToc(spine == null ? null : manifest.get(spine.getAttribute("toc")), manifest);

        List<TextBlock> blocks = new ArrayList<>();
        NodeList itemrefs = opf.getElementsByTagNameNS("*", "itemref");
        for (int i = 0; i < itemrefs.getLength(); i++) {
            Item item = manifest.get(((Element) itemrefs.item(i)).getAttribute("idref"));
            if (item == null || skipped.contains(item.path) || item.properties.matches(".*\\bnav\\b.*") || !isPage(item)) {
                continue;
            }
            byte[] page = files.get(item.path);
            if (page != null) {
                blocks.addAll(reader.page(item.path, new String(page, StandardCharsets.UTF_8)));
            }
        }
        Element titleElement = (Element) firstOf(opf.getElementsByTagNameNS(DC, "title"));
        String title = titleElement == null ? null : titleElement.getTextContent().strip();
        String lifted = MarkdownReader.liftLoneTitle(blocks);
        if (title == null || title.isEmpty()) {
            title = lifted;
        }
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("The book has no text");
        }
        return new TextDocument(title, blocks);
    }

    private static boolean isPage(Item item) {
        return item.type.equals("application/xhtml+xml") || item.type.equals("text/html") || item.type.isEmpty() && item.path.matches("(?i).*\\.x?html?");
    }

    private static boolean wanted(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".opf")
                || lower.endsWith(".ncx") || lower.equals("meta-inf/container.xml");
    }

    // the package document: where container.xml says, or the first .opf
    private static String packagePath(Map<String, byte[]> files) {
        byte[] container = files.get("META-INF/container.xml");
        if (container != null) {
            NodeList roots = parse(container).getElementsByTagNameNS("*", "rootfile");
            for (int i = 0; i < roots.getLength(); i++) {
                String path = resolve("", ((Element) roots.item(i)).getAttribute("full-path"));
                if (files.containsKey(path)) {
                    return path;
                }
            }
        }
        return files.keySet().stream().filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".opf")).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not an EPUB: no package document"));
    }

    private static Document parse(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new IllegalArgumentException("Not an EPUB: " + e.getMessage(), e);
        }
    }

    private static Element first(Document document, String name) {
        return (Element) firstOf(document.getElementsByTagNameNS("*", name));
    }

    private static Node firstOf(NodeList nodes) {
        return nodes.getLength() == 0 ? null : nodes.item(0);
    }

    // the path of the href, from the folder of the document that has it: no fragment, no %20, no ..
    static String resolve(String base, String href) {
        String path = href;
        int fragment = path.indexOf('#');
        if (fragment >= 0) {
            path = path.substring(0, fragment);
        }
        try {
            path = URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // not an encoded path: as it is
        }
        Deque<String> segments = new ArrayDeque<>();
        String joined = path.startsWith("/") ? path : base + path;
        for (String segment : joined.split("/")) {
            if (segment.equals("..")) {
                if (!segments.isEmpty()) {
                    segments.removeLast();
                }
            } else if (!segment.isEmpty() && !segment.equals(".")) {
                segments.addLast(segment);
            }
        }
        return String.join("/", segments);
    }

    // the table of contents: the NCX of the spine, else the navigation document of EPUB 3
    private void readToc(Item ncx, Map<String, Item> manifest) {
        if (ncx == null) {
            ncx = manifest.values().stream().filter(item -> item.type.equals("application/x-dtbncx+xml")).findFirst().orElse(null);
        }
        try {
            if (ncx != null && files.containsKey(ncx.path)) {
                Document document = parse(files.get(ncx.path));
                Element map = first(document, "navMap");
                if (map != null) {
                    navPoints(map, 1, ncx.path.contains("/") ? ncx.path.substring(0, ncx.path.lastIndexOf('/') + 1) : "");
                }
            }
            if (toc.isEmpty()) {
                for (Item item : manifest.values()) {
                    if (item.properties.matches(".*\\bnav\\b.*") && files.containsKey(item.path)) {
                        navigation(new String(files.get(item.path), StandardCharsets.UTF_8),
                                item.path.contains("/") ? item.path.substring(0, item.path.lastIndexOf('/') + 1) : "");
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // a table of contents that can't be read is no help, the headings of the pages remain
            toc.clear();
        }
    }

    private void navPoints(Element parent, int depth, String base) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element point && "navPoint".equals(point.getLocalName())) {
                Element label = null;
                String src = "";
                for (Node child = point.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof Element element && "navLabel".equals(element.getLocalName())) {
                        label = element;
                    } else if (child instanceof Element element && "content".equals(element.getLocalName())) {
                        src = element.getAttribute("src");
                    }
                }
                if (label != null && !src.isEmpty()) {
                    toc.putIfAbsent(resolve(base, src), new Entry(label.getTextContent().strip(), depth));
                }
                navPoints(point, depth + 1, base);
            }
        }
    }

    // the links of <nav epub:type="toc">, the depth is the number of lists they are in
    private void navigation(String html, String base) {
        Matcher start = NAV_START.matcher(html);
        if (!start.find()) {
            return;
        }
        int end = html.toLowerCase(Locale.ROOT).indexOf("</nav", start.end());
        String nav = html.substring(start.end(), end < 0 ? html.length() : end);
        Matcher token = NAV_TOKEN.matcher(nav);
        int depth = 0;
        while (token.find()) {
            boolean closing = !token.group(1).isEmpty();
            if (token.group(2).equalsIgnoreCase("ol")) {
                depth += closing ? -1 : 1;
            } else if (token.group(2).equalsIgnoreCase("a") && !closing) {
                Matcher href = HREF.matcher(token.group(3));
                int close = nav.toLowerCase(Locale.ROOT).indexOf("</a", token.end());
                if (href.find() && close >= 0) {
                    String label = StringEscapeUtils.unescapeHtml4(TAGS.matcher(nav.substring(token.end(), close)).replaceAll("")).replaceAll("\\s+", " ").strip();
                    String src = href.group(1) != null ? href.group(1) : href.group(2);
                    if (!label.isEmpty() && !src.isEmpty()) {
                        toc.putIfAbsent(resolve(base, src), new Entry(label, Math.max(1, depth)));
                    }
                }
            }
        }
    }

    // the blocks of a page; a page with no heading that the table of contents names starts with that name
    private List<TextBlock> page(String path, String html) {
        List<TextBlock> blocks = XhtmlReader.read(html);
        Entry entry = toc.get(path);
        if (entry == null || blocks.isEmpty() || blocks.stream().anyMatch(TextBlock::isHeading)) {
            return blocks;
        }
        List<TextBlock> result = new ArrayList<>(blocks);
        // the name is often the first paragraph already
        if (letters(result.getFirst().text()).equals(letters(entry.label))) {
            result.removeFirst();
        }
        result.addFirst(new TextBlock(Math.min(TextBlock.MAX_LEVEL, entry.depth), entry.label));
        return result;
    }

    private static String letters(String text) {
        return text.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase(Locale.ROOT);
    }
}
