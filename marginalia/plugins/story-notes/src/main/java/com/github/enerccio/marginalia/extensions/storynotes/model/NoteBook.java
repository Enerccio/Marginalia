package com.github.enerccio.marginalia.extensions.storynotes.model;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The pages of a book in memory, by uuid, with what a wiki needs: titles that are unique (ignoring the case), the
 * {@code [[Title]]} links between the pages and the pages that link to one, the tags and the search. Pure logic, the
 * owner stores it with {@link #toData()}.
 */
public class NoteBook {

    public static final int MAX_TITLE = 120;
    public static final int MAX_CONTENT = 100_000;
    public static final int MAX_TAGS = 20;
    public static final int MAX_TAG = 40;

    /** {@code [[Title]]} or {@code [[Title|shown text]]}: the group is all that is between the brackets. */
    private static final Pattern LINK = Pattern.compile("\\[\\[([^\\[\\]\\r\\n]{1," + (2 * MAX_TITLE + 1) + "}?)]]");

    /** The title a link names (the part before the first {@code |}, stripped) and the text it shows, null when it has none. */
    private record LinkParts(String title, String shown) {
        static LinkParts of(String inner) {
            int bar = inner.indexOf('|');
            if (bar < 0) {
                return new LinkParts(inner.strip(), null);
            }
            String shown = inner.substring(bar + 1).strip();
            return new LinkParts(inner.substring(0, bar).strip(), shown.isEmpty() ? null : shown);
        }
    }

    private static final Comparator<NotePage> BY_TITLE = Comparator
            .comparing((NotePage p) -> p.getTitle().toLowerCase(Locale.ROOT))
            .thenComparing(NotePage::getUuid);

    private final Map<String, NotePage> pages = new LinkedHashMap<>();

    public NoteBook() {
    }

    public NoteBook(StoryNotesData data) {
        if (data != null && data.getPages() != null) {
            data.getPages().stream().filter(p -> p != null && p.getUuid() != null)
                    .forEach(p -> pages.put(p.getUuid(), p));
        }
    }

    public StoryNotesData toData() {
        StoryNotesData data = new StoryNotesData();
        data.setPages(new ArrayList<>(pages.values()));
        return data;
    }

    // --- pages ---

    public NotePage get(String uuid) {
        return pages.get(uuid);
    }

    /** All pages ordered by title (ignoring the case). */
    public List<NotePage> sorted() {
        return pages.values().stream().sorted(BY_TITLE).toList();
    }

    /** The pages the assistants may see, ordered by title. */
    public List<NotePage> visible() {
        return sorted().stream().filter(NotePage::isAiVisible).toList();
    }

    public int size() {
        return pages.size();
    }

    /** The page with that title (the case and the spaces around it do not matter), null when there is none. */
    public NotePage byTitle(String title) {
        if (title == null) {
            return null;
        }
        String wanted = title.strip();
        return pages.values().stream().filter(p -> wanted.equalsIgnoreCase(p.getTitle().strip())).findFirst().orElse(null);
    }

    /** Whether another page than {@code exceptUuid} (may be null) has that title. */
    public boolean titleTaken(String title, String exceptUuid) {
        NotePage other = byTitle(title);
        return other != null && !other.getUuid().equals(exceptUuid);
    }

    /** Adds a new page or replaces the one with the same uuid. */
    public void put(NotePage page) {
        pages.put(page.getUuid(), page);
    }

    public void remove(NotePage page) {
        pages.remove(page.getUuid());
    }

    // --- links ---

    /**
     * The titles {@code [[linked]]} in the text (for {@code [[Title|shown text]]} the part before the bar), once each (ignoring the
     * case), in the order they first appear.
     */
    public static List<String> linksIn(String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        Map<String, String> found = new LinkedHashMap<>();
        Matcher matcher = LINK.matcher(content);
        while (matcher.find()) {
            String title = LinkParts.of(matcher.group(1)).title();
            if (!title.isEmpty()) {
                found.putIfAbsent(title.toLowerCase(Locale.ROOT), title);
            }
        }
        return new ArrayList<>(found.values());
    }

    /** What the address of a link of the preview starts with; the title follows, encoded. */
    public static final String HREF_PREFIX = "#note:";

    /**
     * The text with every {@code [[Title]]} turned into a Markdown link ({@code [Title](#note:Title)}, the title encoded in the
     * address) that the preview shows as a link; {@code [[Title|shown text]]} shows the text after the bar and leads to the title.
     * {@link #titleOfHref} reads the title back from the address.
     */
    public static String linkify(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        Matcher matcher = LINK.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            LinkParts parts = LinkParts.of(matcher.group(1));
            String title = parts.title();
            String replacement = title.isEmpty() ? matcher.group()
                    : "[" + (parts.shown() != null ? parts.shown() : title) + "](" + HREF_PREFIX
                    + URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20") + ")";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** The title in the address of a link made by {@link #linkify}, null when the address is something else. */
    public static String titleOfHref(String href) {
        if (href == null || !href.startsWith(HREF_PREFIX) || href.length() == HREF_PREFIX.length()) {
            return null;
        }
        return URLDecoder.decode(href.substring(HREF_PREFIX.length()), StandardCharsets.UTF_8);
    }

    /** The pages that link to {@code page} by its title, ordered by title; the page itself is not one of them. */
    public List<NotePage> backlinks(NotePage page) {
        String title = page.getTitle().strip();
        if (title.isEmpty()) {
            return List.of();
        }
        return sorted().stream()
                .filter(p -> !p.getUuid().equals(page.getUuid()))
                .filter(p -> linksIn(p.getContent()).stream().anyMatch(title::equalsIgnoreCase))
                .toList();
    }

    /**
     * Gives the page a new title and changes the {@code [[links]]} to its old title in the other pages.
     *
     * @return the other pages whose text changed (already touched)
     * @throws IllegalArgumentException when the title is not valid or another page has it
     */
    public List<NotePage> rename(NotePage page, String newTitle) {
        String title = cleanTitle(newTitle);
        if (titleTaken(title, page.getUuid())) {
            throw new IllegalArgumentException("Another page already has the title " + title);
        }
        String old = page.getTitle().strip();
        page.setTitle(title);
        if (old.isEmpty() || old.equals(title)) {
            return List.of();
        }
        List<NotePage> changed = new ArrayList<>();
        for (NotePage other : pages.values()) {
            if (other.getUuid().equals(page.getUuid())) {
                continue;
            }
            String text = other.getContent();
            Matcher matcher = LINK.matcher(text);
            StringBuilder result = new StringBuilder();
            boolean any = false;
            while (matcher.find()) {
                LinkParts parts = LinkParts.of(matcher.group(1));
                if (old.equalsIgnoreCase(parts.title())) {
                    // the text the link shows stays
                    matcher.appendReplacement(result, Matcher.quoteReplacement("[[" + title
                            + (parts.shown() != null ? "|" + parts.shown() : "") + "]]"));
                    any = true;
                } else {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                }
            }
            if (any) {
                matcher.appendTail(result);
                other.setContent(result.toString());
                other.touch();
                changed.add(other);
            }
        }
        return changed;
    }

    // --- tags and search ---

    /** Every tag of the pages, once each (ignoring the case), ordered. */
    public List<String> allTags() {
        Map<String, String> tags = new TreeMap<>();
        pages.values().forEach(p -> p.getTags().forEach(t -> tags.putIfAbsent(t.toLowerCase(Locale.ROOT), t)));
        return new ArrayList<>(tags.values());
    }

    public static boolean hasTag(NotePage page, String tag) {
        return page.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag));
    }

    /**
     * The pages whose title, tags or text contain the query (ignoring the case): the ones with it in the title first,
     * then in the tags, then in the text; each group ordered by title.
     *
     * @param visibleOnly leave out the pages the assistants may not see
     */
    public List<NotePage> search(String query, boolean visibleOnly) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String q = query.strip().toLowerCase(Locale.ROOT);
        List<NotePage> byTitle = new ArrayList<>();
        List<NotePage> byTag = new ArrayList<>();
        List<NotePage> byText = new ArrayList<>();
        for (NotePage page : sorted()) {
            if (visibleOnly && !page.isAiVisible()) {
                continue;
            }
            if (page.getTitle().toLowerCase(Locale.ROOT).contains(q)) {
                byTitle.add(page);
            } else if (page.getTags().stream().anyMatch(t -> t.toLowerCase(Locale.ROOT).contains(q))) {
                byTag.add(page);
            } else if (page.getContent().toLowerCase(Locale.ROOT).contains(q)) {
                byText.add(page);
            }
        }
        List<NotePage> result = new ArrayList<>(byTitle);
        result.addAll(byTag);
        result.addAll(byText);
        return result;
    }

    /** A short piece of the text around the first place it contains the query, the start of the text when it does not. */
    public static String snippet(String content, String query, int length) {
        String text = content == null ? "" : content.replaceAll("\\s+", " ").strip();
        int at = query == null || query.isBlank() ? -1 : text.toLowerCase(Locale.ROOT).indexOf(query.strip().toLowerCase(Locale.ROOT));
        int start = Math.max(0, at < 0 ? 0 : at - length / 4);
        if (start > 0) {
            // do not cut a word in half
            int space = text.indexOf(' ', start);
            start = space < 0 || space > start + 20 ? start : space + 1;
        }
        String piece = text.substring(Math.min(start, text.length()), Math.min(text.length(), start + length));
        return (start > 0 ? "..." : "") + piece + (start + length < text.length() ? "..." : "");
    }

    /** The first line of the text that is not empty or a Markdown heading mark alone, cut to {@code length}. */
    public static String firstLine(String content, int length) {
        if (content == null) {
            return "";
        }
        for (String line : content.split("\\R")) {
            String stripped = line.strip().replaceFirst("^#{1,6}\\s*", "");
            if (!stripped.isEmpty()) {
                return stripped.length() > length ? stripped.substring(0, length - 1) + "…" : stripped;
            }
        }
        return "";
    }

    // --- replacing text ---

    /** The text after a replacement and how many pieces were replaced. */
    public record Replacement(String content, int count) {
    }

    /**
     * Replaces the piece {@code find} of the text with {@code replace}, exactly as written (the case matters).
     *
     * @param all replace every occurrence; otherwise {@code find} must occur exactly once
     * @throws IllegalArgumentException when {@code find} is empty or not there, or is there more than once and {@code all} is false
     */
    public static Replacement replace(String content, String find, String replace, boolean all) {
        String text = content == null ? "" : content;
        if (find == null || find.isEmpty()) {
            throw new IllegalArgumentException("Give the text to replace");
        }
        String with = replace == null ? "" : replace;
        int first = text.indexOf(find);
        if (first < 0) {
            throw new IllegalArgumentException("The text to replace is not in the page, it has to be exactly as written there (get-note shows the page)");
        }
        int count = 1;
        for (int at = text.indexOf(find, first + find.length()); at >= 0; at = text.indexOf(find, at + find.length())) {
            count++;
        }
        if (count > 1 && !all) {
            throw new IllegalArgumentException("The text to replace is in the page " + count
                    + " times: include more of the text around it, or pass all true to replace every one");
        }
        String result = all ? text.replace(find, with) : text.substring(0, first) + with + text.substring(first + find.length());
        return new Replacement(result, count);
    }

    // --- values ---

    /**
     * The title as it is stored: without the spaces around it.
     *
     * @throws IllegalArgumentException when it is empty, too long or has a bracket or a line break (a link could not name it)
     */
    public static String cleanTitle(String title) {
        String clean = title == null ? "" : title.strip();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("The title is required");
        }
        if (clean.length() > MAX_TITLE) {
            throw new IllegalArgumentException("The title is at most " + MAX_TITLE + " characters");
        }
        if (clean.indexOf('[') >= 0 || clean.indexOf(']') >= 0 || clean.indexOf('|') >= 0 || clean.indexOf('\n') >= 0 || clean.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("The title can't have brackets, a bar or line breaks, [[links]] could not name it");
        }
        return clean;
    }

    /**
     * The text a page holds.
     *
     * @throws IllegalArgumentException when it is longer than {@link #MAX_CONTENT}
     */
    public static String cleanContent(String content) {
        String clean = content == null ? "" : content;
        if (clean.length() > MAX_CONTENT) {
            throw new IllegalArgumentException("A page is at most " + MAX_CONTENT + " characters");
        }
        return clean;
    }

    /** The tags of a comma (or line) separated text, stripped, without the empty ones and the repeated ones. */
    public static List<String> parseTags(String text) {
        return text == null ? List.of() : cleanTags(Arrays.asList(text.split("[,\\r\\n]")));
    }

    /**
     * Strips the tags and drops the empty ones and the repeated ones (ignoring the case).
     *
     * @throws IllegalArgumentException when there are too many or one is too long
     */
    public static List<String> cleanTags(Collection<String> tags) {
        Map<String, String> clean = new LinkedHashMap<>();
        for (String tag : tags) {
            String stripped = tag == null ? "" : tag.strip();
            if (stripped.isEmpty()) {
                continue;
            }
            if (stripped.length() > MAX_TAG) {
                throw new IllegalArgumentException("A tag is at most " + MAX_TAG + " characters");
            }
            clean.putIfAbsent(stripped.toLowerCase(Locale.ROOT), stripped);
        }
        if (clean.size() > MAX_TAGS) {
            throw new IllegalArgumentException("A page has at most " + MAX_TAGS + " tags");
        }
        return new ArrayList<>(clean.values());
    }
}
