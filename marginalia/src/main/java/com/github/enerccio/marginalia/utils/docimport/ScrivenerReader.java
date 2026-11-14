package com.github.enerccio.marginalia.utils.docimport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reads a Scrivener project, the {@code .scriv} folder zipped (Scrivener 2 and 3). Only the manuscript is read, the
 * {@code Draft} folder of the binder, not the research, the notes and the trash, and not the documents that are not
 * included in the compile. The binder gives the structure: a folder or a document with documents under it is a heading
 * (level by its depth, the title of the item), and the documents that are all there is in a folder are its scenes: their
 * text follows one after another with a break between them, the titles are not used. Documents directly in the manuscript or
 * in a folder with other folders are chapters of their own, with their titles. The text of a document is RTF
 * ({@link RtfReader}), the name of the project is the title.
 */
final class ScrivenerReader {

    private static final Pattern CONTENT = Pattern.compile(".*(?:^|/)(Files/(?:Data/[^/]+/content\\.rtf|Docs/\\d+\\.rtf))");
    private static final String SCENE_BREAK = "\\* \\* \\*";

    private record Item(String key, String title, boolean folder, boolean included, List<Item> children) {

        boolean container() {
            return folder || !children.isEmpty();
        }
    }

    private final Map<String, byte[]> files;
    private final List<TextBlock> blocks = new ArrayList<>();

    private ScrivenerReader(Map<String, byte[]> files) {
        this.files = files;
    }

    static TextDocument read(byte[] data) {
        Map<String, byte[]> files = new java.util.LinkedHashMap<>();
        Map<String, byte[]> read = ZipFiles.read(data, ScrivenerReader::wanted);
        String project = null;
        for (Map.Entry<String, byte[]> entry : read.entrySet()) {
            String name = entry.getKey();
            if (name.toLowerCase(Locale.ROOT).endsWith(".scrivx")) {
                // the one nearest to the top is the project's
                if (project == null || depth(name) < depth(project)) {
                    project = name;
                }
                continue;
            }
            java.util.regex.Matcher matcher = CONTENT.matcher(name);
            if (matcher.matches()) {
                files.put(matcher.group(1), entry.getValue());
            }
        }
        if (project == null) {
            throw new IllegalArgumentException("Not a Scrivener project: no .scrivx file");
        }
        Item draft = draft(parse(read.get(project)));
        if (draft == null) {
            throw new IllegalArgumentException("Not a Scrivener project: no manuscript (Draft) folder");
        }
        ScrivenerReader reader = new ScrivenerReader(files);
        reader.walk(draft, 0);
        if (reader.blocks.isEmpty()) {
            throw new IllegalArgumentException("The manuscript has no text");
        }
        return new TextDocument(ZipFiles.baseName(project), reader.blocks);
    }

    private static int depth(String name) {
        return (int) name.chars().filter(c -> c == '/').count();
    }

    private static boolean wanted(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (name.startsWith("__MACOSX/") || name.contains("/__MACOSX/") || ZipFiles.fileName(name).startsWith("._")) {
            return false;
        }
        return lower.endsWith(".scrivx") || CONTENT.matcher(name).matches();
    }

    private static Document parse(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a Scrivener project: " + e.getMessage(), e);
        }
    }

    private static Item draft(Document document) {
        Element binder = child(document.getDocumentElement(), "Binder");
        if (binder == null) {
            return null;
        }
        for (Element item : children(binder, "BinderItem")) {
            if ("DraftFolder".equals(item.getAttribute("Type"))) {
                return item(item);
            }
        }
        return null;
    }

    private static Item item(Element element) {
        String type = element.getAttribute("Type");
        // Scrivener 3 names the files by UUID, Scrivener 2 by number
        String key = !element.getAttribute("UUID").isEmpty() ? "Files/Data/" + element.getAttribute("UUID") + "/content.rtf"
                : "Files/Docs/" + element.getAttribute("ID") + ".rtf";
        Element titleElement = child(element, "Title");
        String title = titleElement == null ? "" : titleElement.getTextContent().strip();
        Element meta = child(element, "MetaData");
        Element include = meta == null ? null : child(meta, "IncludeInCompile");
        boolean included = include == null || !include.getTextContent().strip().equalsIgnoreCase("No");
        List<Item> children = new ArrayList<>();
        Element nested = child(element, "Children");
        if (nested != null) {
            children(nested, "BinderItem").forEach(item -> children.add(item(item)));
        }
        return new Item(key, title, type.equals("Folder") || type.equals("DraftFolder"), included, children);
    }

    private static Element child(Element parent, String name) {
        List<Element> found = children(parent, name);
        return found.isEmpty() ? null : found.getFirst();
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> found = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getTagName().equals(name)) {
                found.add(element);
            }
        }
        return found;
    }

    // the children of an item at the depth (0 = the manuscript itself)
    private void walk(Item parent, int depth) {
        boolean scenes = depth > 0 && parent.children.stream().noneMatch(Item::container);
        boolean firstScene = true;
        int level = Math.min(TextBlock.MAX_LEVEL, depth + 1);
        for (Item child : parent.children) {
            if (scenes) {
                List<TextBlock> text = child.included ? text(child, depth) : List.of();
                if (!text.isEmpty()) {
                    if (!firstScene) {
                        blocks.add(new TextBlock(0, SCENE_BREAK));
                    }
                    blocks.addAll(text);
                    firstScene = false;
                }
                continue;
            }
            List<TextBlock> own = child.included ? text(child, level) : List.of();
            if (child.included && !child.title.isBlank() && (child.container() || !own.isEmpty())) {
                blocks.add(new TextBlock(level, child.title));
            }
            blocks.addAll(own);
            walk(child, depth + 1);
        }
    }

    // the text of the document of the item, its headings below the level
    private List<TextBlock> text(Item item, int level) {
        byte[] rtf = files.get(item.key);
        if (rtf == null || rtf.length == 0) {
            return List.of();
        }
        return RtfReader.read(rtf).blocks().stream().map(block -> block.shifted(level)).toList();
    }
}
