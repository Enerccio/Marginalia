package com.github.enerccio.marginalia.utils.docimport;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads an Obsidian vault, a zip of the folder: the notes ({@code .md}) in the order of their paths, numbers in a name
 * counting as numbers ({@code 2} before {@code 10}). A note is a chapter named after the note (or its {@code title}), the
 * folders around it are headings above it (a part), and the headings inside a note go below its own (the shallowest one right under it).
 * Hidden folders ({@code .obsidian}, {@code .trash}), the attachments and the notes with no text are left out. What only Obsidian
 * knows is turned into text: a link {@code [[Note|text]]} is its text, an embed {@code ![[file]]}, a comment
 * {@code %%...%%}, a block id {@code ^abc} and the marker of a callout are removed, a highlight {@code ==text==} is the text.
 */
final class ObsidianReader {

    private static final Pattern COMMENT = Pattern.compile("%%.*?%%", Pattern.DOTALL);
    private static final Pattern EMBED = Pattern.compile("!\\[\\[[^\\]\\n]*]]");
    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]\\n]*]\\([^)\\n]*\\)");
    private static final Pattern LINK = Pattern.compile("\\[\\[([^\\]\\n|]*)(?:\\|([^\\]\\n]*))?]]");
    private static final Pattern HIGHLIGHT = Pattern.compile("==(?=\\S)([^=\\n]+?)(?<=\\S)==");
    private static final Pattern BLOCK_ID = Pattern.compile("[ \\t]+\\^[A-Za-z0-9-]+[ \\t]*$", Pattern.MULTILINE);
    private static final Pattern CALLOUT = Pattern.compile("^(\\s*>+[ \\t]?)\\[![^\\]\\n]*][+-]?[ \\t]*", Pattern.MULTILINE);
    private static final Pattern FENCE = Pattern.compile("^ {0,3}(```|~~~)");
    private static final Pattern DIGITS = Pattern.compile("\\d+|\\D+");

    private ObsidianReader() {
    }

    static TextDocument read(byte[] data) {
        Map<String, byte[]> files = ZipFiles.read(data, ObsidianReader::wanted);
        List<String[]> notes = new ArrayList<>();
        files.keySet().forEach(path -> notes.add(path.split("/")));
        // all in one folder (the vault itself, as zipped from outside): that folder is not a part
        while (!notes.isEmpty() && notes.stream().allMatch(segments -> segments.length > 1 && segments[0].equals(notes.getFirst()[0]))) {
            notes.replaceAll(segments -> java.util.Arrays.copyOfRange(segments, 1, segments.length));
        }
        // the zip's own paths again, for the content: the same order of the keys
        List<String> paths = new ArrayList<>(files.keySet());
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            order.add(i);
        }
        order.sort(Comparator.comparing(notes::get, ObsidianReader::compare));

        List<TextBlock> blocks = new ArrayList<>();
        List<String> openFolders = new ArrayList<>();
        for (int index : order) {
            String[] segments = notes.get(index);
            TextDocument note = MarkdownReader.read(clean(TextFiles.decode(files.get(paths.get(index)))));
            if (note.blocks().isEmpty()) {
                continue;
            }
            List<String> folders = List.of(segments).subList(0, segments.length - 1);
            int common = 0;
            while (common < folders.size() && common < openFolders.size() && folders.get(common).equals(openFolders.get(common))) {
                common++;
            }
            for (int level = common; level < folders.size(); level++) {
                blocks.add(new TextBlock(Math.min(TextBlock.MAX_LEVEL, level + 1), folders.get(level)));
            }
            openFolders = new ArrayList<>(folders);
            int level = folders.size() + 1;
            String name = note.title() != null && !note.title().isBlank() ? note.title() : ZipFiles.baseName(segments[segments.length - 1]);
            blocks.add(new TextBlock(Math.min(TextBlock.MAX_LEVEL, level), name));
            // the shallowest heading of the note goes right below the heading of the note
            int shallowest = note.blocks().stream().filter(TextBlock::isHeading).mapToInt(TextBlock::level).min().orElse(1);
            note.blocks().forEach(block -> blocks.add(block.shifted(level + 1 - shallowest)));
        }
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException("The vault has no notes with text");
        }
        return new TextDocument(null, blocks);
    }

    private static boolean wanted(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".md") && !lower.endsWith(".markdown")) {
            return false;
        }
        for (String segment : name.split("/")) {
            if (segment.startsWith(".") || segment.equals("__MACOSX")) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the text of a note with what only Obsidian knows turned into text
     */
    static String clean(String text) {
        String result = COMMENT.matcher(text).replaceAll("");
        StringBuilder out = new StringBuilder();
        boolean fenced = false;
        for (String line : result.split("\n", -1)) {
            if (FENCE.matcher(line).find()) {
                fenced = !fenced;
            }
            if (out.length() > 0) {
                out.append('\n');
            }
            out.append(fenced ? line : cleanLine(line));
        }
        return out.toString();
    }

    private static String cleanLine(String line) {
        String result = EMBED.matcher(line).replaceAll("");
        result = IMAGE.matcher(result).replaceAll("");
        result = LINK.matcher(result).replaceAll(match -> Matcher.quoteReplacement(linkText(match.group(1), match.group(2))));
        result = HIGHLIGHT.matcher(result).replaceAll("$1");
        result = BLOCK_ID.matcher(result).replaceAll("");
        return CALLOUT.matcher(result).replaceAll("$1");
    }

    // [[Note#Heading|text]] is the text, [[Note#Heading]] is the note (and the heading), a block link [[Note#^id]] is the note
    private static String linkText(String target, String alias) {
        if (alias != null && !alias.isBlank()) {
            return alias.strip();
        }
        String text = target.strip();
        int block = text.indexOf("#^");
        if (block >= 0) {
            return text.substring(0, block);
        }
        int heading = text.indexOf('#');
        if (heading == 0) {
            return text.substring(1);
        }
        return heading > 0 ? text.substring(0, heading) + " " + text.substring(heading + 1) : text;
    }

    // the folders and files path by path, case-insensitive, runs of digits as numbers
    private static int compare(String[] a, String[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int result = compareNatural(a[i], b[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(a.length, b.length);
    }

    private static int compareNatural(String a, String b) {
        Matcher left = DIGITS.matcher(a.toLowerCase(Locale.ROOT));
        Matcher right = DIGITS.matcher(b.toLowerCase(Locale.ROOT));
        while (left.find() && right.find()) {
            String x = left.group();
            String y = right.group();
            int result;
            if (Character.isDigit(x.charAt(0)) && Character.isDigit(y.charAt(0))) {
                String xs = x.replaceFirst("^0+(?=.)", "");
                String ys = y.replaceFirst("^0+(?=.)", "");
                result = xs.length() != ys.length() ? Integer.compare(xs.length(), ys.length()) : xs.compareTo(ys);
            } else {
                result = x.compareTo(y);
            }
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(a.length(), b.length());
    }
}
