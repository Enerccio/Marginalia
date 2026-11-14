package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportLoreEntry;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An Obsidian vault in a ZIP file: a note with the front matter and the title page that lists everything, a note for each chapter (linked to
 * the one before and after it), a note for each entry of the lorebook (in a folder for its lorebook), the images of the book. The first
 * mention of an entry in a note links to the note of the entry, so the graph of Obsidian shows who is in which chapter.
 */
public class ObsidianExporter extends ExporterBase {
    private static final Pattern FORBIDDEN_IN_NAME = Pattern.compile("[\\\\/:*?\"<>|#^\\[\\]\\p{Cntrl}]");
    private static final int MIN_LINKED_NAME = 3;

    @Override
    public String getName() {
        return "Obsidian vault";
    }

    @Override
    public String getId() {
        return "obsidian";
    }

    @Override
    public String getFileExtension() {
        return "zip";
    }

    @Override
    public String getMimeType() {
        return "application/zip";
    }

    @Override
    public boolean usesLore() {
        return true;
    }

    @Override
    public List<ExportTemplate> getTemplates() {
        return List.of(TITLE_PAGE_TEMPLATE, FRONT_MATTER_TEMPLATE, AUTHORSHIP_TEMPLATE);
    }

    @Override
    protected ExportWriter open(ExportOptions options, OutputStream out) throws Exception {
        return new VaultWriter(options, buildFrontMatter(options), out);
    }

    /**
     * @return name valid for a file of Obsidian, which is also what a link to the note names
     */
    static String noteName(String text, String fallback) {
        String name = FORBIDDEN_IN_NAME.matcher(StringUtils.defaultString(text)).replaceAll("_").replaceAll("\\s+", " ").trim();
        name = StringUtils.abbreviate(name, "", 100).trim();
        // a name that starts with a dot is a hidden file
        name = StringUtils.stripStart(name, ".").trim();
        return name.isEmpty() ? fallback : name;
    }

    /**
     * @return the tag Obsidian accepts (no spaces and no {@code #}), null if nothing is left
     */
    static String tag(String text) {
        String tag = StringUtils.defaultString(text).replaceAll("[\\s#,\\[\\]]+", "-").replaceAll("^-+|-+$", "");
        return tag.isEmpty() ? null : tag;
    }

    /**
     * Links the first mention of every entry in the text, as {@code [[note|mention]]}. Code blocks and what is already a link are left alone.
     */
    static String link(String markdown, Map<String, String> notes, String self) {
        if (notes.isEmpty()) {
            return markdown;
        }
        List<String> names = notes.keySet().stream().filter(n -> !n.equals(self)).sorted(Comparator.comparingInt(String::length).reversed()).toList();
        if (names.isEmpty()) {
            return markdown;
        }
        Pattern pattern = Pattern.compile("(?<![\\p{L}\\p{N}_\\[|])(" + String.join("|", names.stream().map(Pattern::quote).toList())
                + ")(?![\\p{L}\\p{N}_\\]])");
        Set<String> linked = new HashSet<>();
        StringBuilder result = new StringBuilder();
        boolean code = false;
        for (String line : markdown.split("\n", -1)) {
            if (line.stripLeading().startsWith("```") || line.stripLeading().startsWith("~~~")) {
                code = !code;
            }
            if (!result.isEmpty()) {
                result.append('\n');
            }
            if (code || line.startsWith("    ")) {
                result.append(line);
                continue;
            }
            Matcher matcher = pattern.matcher(line);
            StringBuilder replaced = new StringBuilder();
            while (matcher.find()) {
                String mention = matcher.group(1);
                matcher.appendReplacement(replaced, Matcher.quoteReplacement(
                        linked.add(mention) ? "[[" + notes.get(mention) + "|" + mention + "]]" : mention));
            }
            matcher.appendTail(replaced);
            result.append(replaced);
        }
        return result.toString();
    }

    private class VaultWriter implements ExportWriter {
        private final ExportOptions options;
        private final String frontMatter;
        private String titlePage;
        private final List<Chapter> chapters = new ArrayList<>();
        private final List<StringBuilder> texts = new ArrayList<>();
        // the attachments go into the zip as they come, the notes at the end
        private final ZipOutputStream zip;
        private final String book;
        private int attachments;

        private VaultWriter(ExportOptions options, String frontMatter, OutputStream out) {
            this.options = options;
            this.frontMatter = frontMatter;
            this.zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
            this.book = noteName(options.bookName(), "Book");
        }

        @Override
        public void titlePage(String markdown) {
            titlePage = markdown.replaceAll("\\n{3,}", "\n\n").strip();
        }

        @Override
        public void message(String markdown, Chapter chapter) {
            if (chapter != null || texts.isEmpty()) {
                chapters.add(chapter);
                texts.add(new StringBuilder());
            }
            texts.getLast().append(markdown.strip()).append("\n\n");
        }

        @Override
        public void images(List<ExportImage> images) throws IOException {
            for (ExportImage image : images) {
                attachments++;
                String file = "image-" + attachments + "." + image.extension();
                zip.putNextEntry(new ZipEntry(book + "/Attachments/" + file));
                zip.write(image.data());
                zip.closeEntry();
                texts.getLast().append("![[").append(file).append("]]\n\n");
                if (!image.caption().isEmpty()) {
                    texts.getLast().append('*').append(image.caption().replaceAll("\\s+", " ")).append("*\n\n");
                }
            }
        }

        @Override
        public void finish() throws Exception {
            Map<String, byte[]> files = new LinkedHashMap<>();

            // the paths of the notes of the lore, by the name of the entry
            Set<String> taken = new HashSet<>();
            Map<ExportLoreEntry, String> lorePaths = new LinkedHashMap<>();
            Map<String, String> loreNotes = new LinkedHashMap<>();
            for (ExportLoreEntry entry : options.lore()) {
                if (StringUtils.isBlank(entry.name())) {
                    continue;
                }
                String folder = "Lore/" + noteName(entry.lorebook(), "Lorebook");
                String path = folder + "/" + noteName(entry.name(), "Entry");
                for (int n = 2; !taken.add(path.toLowerCase()); n++) {
                    path = folder + "/" + noteName(entry.name(), "Entry") + " (" + n + ")";
                }
                lorePaths.put(entry, path);
                if (entry.name().strip().length() >= MIN_LINKED_NAME && !loreNotes.containsKey(entry.name().strip())) {
                    loreNotes.put(entry.name().strip(), path);
                }
            }

            // the names of the chapter notes
            List<String> chapterPaths = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                String title = chapters.get(i) != null ? chapters.get(i).title()
                        : (texts.size() > 1 ? "Beginning" : "Story");
                String path = "Chapters/" + String.format("%03d", i + 1) + " " + noteName(title, "Chapter");
                chapterPaths.add(path);
            }

            for (int i = 0; i < texts.size(); i++) {
                StringBuilder note = new StringBuilder("---\nbook: \"[[" + book + "]]\"\nchapter: " + (i + 1) + "\ntags:\n  - chapter\n---\n\n");
                note.append(link(texts.get(i).toString().strip(), loreNotes, null)).append("\n\n---\n");
                List<String> navigation = new ArrayList<>();
                if (i > 0) {
                    navigation.add("← [[" + chapterPaths.get(i - 1) + "|Previous]]");
                }
                navigation.add("[[" + book + "|" + book + "]]");
                if (i + 1 < texts.size()) {
                    navigation.add("[[" + chapterPaths.get(i + 1) + "|Next]] →");
                }
                note.append(String.join(" | ", navigation)).append('\n');
                files.put(chapterPaths.get(i) + ".md", note.toString().getBytes(StandardCharsets.UTF_8));
            }

            Map<String, List<String>> loreIndex = new LinkedHashMap<>();
            for (Map.Entry<ExportLoreEntry, String> lore : lorePaths.entrySet()) {
                ExportLoreEntry entry = lore.getKey();
                StringBuilder note = new StringBuilder("---\nlorebook: \"" + yamlQuoted(entry.lorebook()) + "\"\n");
                List<String> tags = entry.tags().stream().map(ObsidianExporter::tag).filter(t -> t != null).distinct().toList();
                if (!tags.isEmpty()) {
                    note.append("tags:\n");
                    tags.forEach(t -> note.append("  - ").append(t).append('\n'));
                }
                note.append("---\n\n").append(link(StringUtils.defaultString(entry.content()).strip(), loreNotes, entry.name().strip())).append('\n');
                if (StringUtils.isNotBlank(entry.comment())) {
                    note.append("\n> [!note]\n");
                    entry.comment().strip().lines().forEach(l -> note.append("> ").append(l).append('\n'));
                }
                files.put(lore.getValue() + ".md", note.toString().getBytes(StandardCharsets.UTF_8));
                loreIndex.computeIfAbsent(StringUtils.defaultIfBlank(entry.lorebook(), "Lorebook"), k -> new ArrayList<>())
                        .add("- [[" + lore.getValue() + "|" + entry.name().strip().replace("|", "/") + "]]");
            }

            StringBuilder index = new StringBuilder(frontMatter.strip()).append("\n\n");
            index.append(titlePage != null ? titlePage : "# " + options.bookName().strip()).append("\n");
            if (!texts.isEmpty()) {
                index.append("\n## Chapters\n\n");
                for (int i = 0; i < texts.size(); i++) {
                    String title = chapters.get(i) != null ? chapters.get(i).title() : chapterPaths.get(i).substring("Chapters/".length() + 4);
                    index.append("- [[").append(chapterPaths.get(i)).append('|').append(title.replace("|", "/")).append("]]\n");
                }
            }
            if (!loreIndex.isEmpty()) {
                index.append("\n## Lore\n");
                loreIndex.forEach((lorebook, links) -> {
                    index.append("\n### ").append(lorebook.replaceAll("[\\r\\n]+", " ")).append("\n\n");
                    links.forEach(l -> index.append(l).append('\n'));
                });
            }
            files.put(book + ".md", index.toString().getBytes(StandardCharsets.UTF_8));

            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(book + "/" + file.getKey()));
                zip.write(file.getValue());
                zip.closeEntry();
            }
            // the stream itself is closed by the exporter
            zip.finish();
        }
    }
}
