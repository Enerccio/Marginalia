package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.manual.BookDataSource;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * What the help assistant reads of the books of the user when they allowed it (see {@link BookDataSource}): folder
 * names, titles, descriptions and the settings a book resolves to, never a text of a story (a search says only where it
 * found the words). The functions run on threads of their own, so every read runs in the request of the user that asked.
 */
final class HelpBookData implements BookDataSource {

    /** The most entries of a kind in one answer; the rest is counted. */
    static final int MAX_ENTRIES = 100;
    static final int MAX_FOUND = 20;
    static final int MAX_DESCRIPTION = 200;
    static final int MAX_VALUE = 300;

    private final FolderService folders;
    private final ManuscriptService books;
    private final ChatMessageService parts;
    private final RequestAttributes request;

    /**
     * @param request the request of the user, to run the reads in
     */
    HelpBookData(FolderService folders, ManuscriptService books, ChatMessageService parts, RequestAttributes request) {
        this.folders = folders;
        this.books = books;
        this.parts = parts;
        this.request = request;
    }

    @Override
    public String listFolder(String path) throws Exception {
        try (InRequestScope _ = new InRequestScope(request)) {
            Folder folder = folderOf(path);
            StringBuilder sb = new StringBuilder("## ").append(folder == null ? "The top level" : "Folder " + folders.getPath(folder)).append('\n');
            if (folder != null && StringUtils.isNotBlank(folder.getDescription())) {
                sb.append(cut(folder.getDescription(), MAX_DESCRIPTION)).append('\n');
            }
            List<Folder> subfolders = folders.findChildren(folder);
            Map<Long, Long> counts = folders.countBooksByFolder();
            sb.append("\nSubfolders (").append(subfolders.size()).append("):\n");
            int shown = 0;
            for (Folder sub : subfolders) {
                if (shown++ == MAX_ENTRIES) {
                    sb.append("- and ").append(subfolders.size() - MAX_ENTRIES).append(" more\n");
                    break;
                }
                sb.append("- ").append(sub.getName()).append(" (").append(counts.getOrDefault(sub.getId(), 0L)).append(" books)");
                if (StringUtils.isNotBlank(sub.getDescription())) {
                    sb.append(" - ").append(cut(sub.getDescription(), MAX_DESCRIPTION));
                }
                sb.append('\n');
            }
            List<Manuscript> inside = new ArrayList<>(books.findRegularInFolder(folder, false));
            inside.sort(Comparator.comparing(b -> StringUtils.defaultString(b.getName()).toLowerCase()));
            sb.append("\nBooks (").append(inside.size()).append("):\n");
            shown = 0;
            for (Manuscript book : inside) {
                if (shown++ == MAX_ENTRIES) {
                    sb.append("- and ").append(inside.size() - MAX_ENTRIES).append(" more\n");
                    break;
                }
                sb.append(bookLine(book)).append('\n');
            }
            return sb.toString().stripTrailing();
        }
    }

    @Override
    public String searchBooks(String query, String folder) throws Exception {
        try (InRequestScope _ = new InRequestScope(request)) {
            Folder scope = folderOf(folder);
            List<String> words = Arrays.stream(query.toLowerCase().split("[\\s\"]+")).filter(w -> !w.isBlank()).toList();
            List<Manuscript> candidates = books.findRegularInFolder(scope, true);
            Set<Long> inText = parts.findManuscriptsWithText(candidates.stream().map(Manuscript::getId).toList(), query, true);
            Map<Long, String> paths = folders.getPaths();

            record Hit(Manuscript book, String where, int rank) {
            }
            List<Hit> hits = new ArrayList<>();
            for (Manuscript book : candidates) {
                String name = StringUtils.defaultString(book.getName()).toLowerCase();
                String description = StringUtils.defaultString(book.getDescription()).toLowerCase();
                List<String> where = new ArrayList<>();
                int rank = 3;
                if (containsAll(name, words)) {
                    where.add("title");
                    rank = 0;
                } else if (containsAll(description, words)) {
                    where.add("description");
                    rank = 1;
                } else if (containsAll(name + " " + description, words)) {
                    where.add("title and description");
                    rank = 1;
                }
                if (inText.contains(book.getId())) {
                    where.add("story text");
                    rank = Math.min(rank, 2);
                }
                if (!where.isEmpty()) {
                    hits.add(new Hit(book, String.join(", ", where), rank));
                }
            }
            hits.sort(Comparator.comparingInt(Hit::rank).thenComparing(h -> StringUtils.defaultString(h.book().getName()).toLowerCase()));

            StringBuilder sb = new StringBuilder("## ").append(hits.size()).append(hits.size() == 1 ? " book" : " books").append(" for \"")
                    .append(query).append("\"").append(scope == null ? "" : " in " + folders.getPath(scope)).append('\n');
            int shown = 0;
            for (Hit hit : hits) {
                if (shown++ == MAX_FOUND) {
                    sb.append("- and ").append(hits.size() - MAX_FOUND).append(" more, narrow the search\n");
                    break;
                }
                Long folderId = hit.book().getFolder() == null ? null : hit.book().getFolder().getId();
                sb.append(bookLine(hit.book())).append("; folder: ").append(folderId == null ? "the top level" : paths.getOrDefault(folderId, "?"))
                        .append("; found in: ").append(hit.where()).append('\n');
            }
            return sb.toString().stripTrailing();
        }
    }

    @Override
    public String bookSettings(String reference) throws Exception {
        try (InRequestScope _ = new InRequestScope(request)) {
            Manuscript book = bookOf(reference);
            StringBuilder sb = new StringBuilder("## ").append(book.getName()).append('\n');
            sb.append("Folder: ").append(book.getFolder() == null ? "the top level" : folders.getPath(book.getFolder())).append('\n');
            EffectiveSetting<Language> language = books.resolveLanguage(book);
            sb.append("- Language: ").append(language.value().getDisplayName()).append(" - ").append(origin(language)).append('\n');
            for (PromptField field : PromptField.values()) {
                EffectiveSetting<String> setting = books.resolve(book, field);
                sb.append("- ").append(label(field)).append(": ");
                if (showsValue(field)) {
                    sb.append(cut(setting.value(), MAX_VALUE)).append(" - ");
                } else {
                    sb.append(setting.isCustom() ? "a custom text - " : "the built-in text - ");
                }
                sb.append(origin(setting)).append('\n');
            }
            return sb.toString().stripTrailing();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private String origin(EffectiveSetting<?> setting) throws Exception {
        return switch (setting.origin()) {
            case BOOK -> "set on the book itself";
            case FOLDER -> "inherited from the folder \"" + folders.getPath(setting.folder()) + "\"";
            case USER -> "from the user's settings";
            case DEFAULT -> "built-in default";
        };
    }

    /** The short settings are told, the prompts only where they come from. */
    private static boolean showsValue(PromptField field) {
        return field == PromptField.POV || field == PromptField.TENSE || field == PromptField.STYLE;
    }

    private static String label(PromptField field) {
        return switch (field) {
            case MASTER_TEMPLATE -> "Master template";
            case POV -> "Point of view";
            case TENSE -> "Tense";
            case STYLE -> "Style";
            case USER_PROMPT -> "User prompt";
            case SUMMARY_PROMPT -> "Summary prompt";
            case META_SUMMARY_PROMPT -> "Meta summary prompt";
            case REDRAFT_SYSTEM_PROMPT -> "Redraft system prompt";
            case REDRAFT_PROMPT -> "Redraft prompt";
            case ANNOTATE_SYSTEM_PROMPT -> "Annotate system prompt";
            case ANNOTATE_PROMPT -> "Annotate prompt";
            case IMPERSONATE_PROMPT -> "Impersonate prompt";
        };
    }

    private static String bookLine(Manuscript book) {
        StringBuilder sb = new StringBuilder("- \"").append(book.getName()).append("\" (id ").append(book.getUuid()).append(')');
        if (StringUtils.isNotBlank(book.getDescription())) {
            sb.append(" - ").append(cut(book.getDescription(), MAX_DESCRIPTION));
        }
        return sb.toString();
    }

    private static boolean containsAll(String text, List<String> words) {
        return !words.isEmpty() && words.stream().allMatch(text::contains);
    }

    /** One line, cut with an ellipsis. */
    private static String cut(String text, int max) {
        String line = text == null ? "" : text.strip().replaceAll("\\s+", " ");
        return line.length() > max ? line.substring(0, max).stripTrailing() + "…" : line;
    }

    /**
     * The folder of a path of names from the top (case insensitive), null for the top level.
     */
    private Folder folderOf(String path) throws Exception {
        Folder current = null;
        StringBuilder walked = new StringBuilder();
        for (String name : StringUtils.defaultString(path).split(String.valueOf(FolderService.SEPARATOR))) {
            if (name.isBlank()) {
                continue;
            }
            Folder next = folders.findChild(current, name);
            if (next == null) {
                String there = folders.findChildren(current).stream().map(Folder::getName).limit(30).collect(Collectors.joining(", "));
                throw new IllegalArgumentException("There is no folder \"" + name.strip() + "\" in "
                        + (current == null ? "the top level" : "\"" + walked + "\"") + ". "
                        + (there.isEmpty() ? "It has no folders." : "The folders there are: " + there + "."));
            }
            if (!walked.isEmpty()) {
                walked.append(FolderService.SEPARATOR);
            }
            walked.append(next.getName());
            current = next;
        }
        return current;
    }

    /**
     * The book of an id, a title, or {@code Folder/Sub/Title}.
     */
    private Manuscript bookOf(String reference) throws Exception {
        Manuscript byId = books.findForUser(reference);
        if (byId != null && !byId.isDeleted() && !byId.isAgentic()) {
            return byId;
        }
        Map<Long, String> paths = folders.getPaths();
        List<Manuscript> found = new ArrayList<>();
        for (Manuscript book : books.findRegularInFolder(null, true)) {
            String title = StringUtils.defaultString(book.getName()).strip();
            Long folderId = book.getFolder() == null ? null : book.getFolder().getId();
            String full = folderId == null ? title : paths.getOrDefault(folderId, "") + FolderService.SEPARATOR + title;
            if (title.equalsIgnoreCase(reference) || full.equalsIgnoreCase(reference)) {
                found.add(book);
            }
        }
        if (found.isEmpty()) {
            throw new IllegalArgumentException("There is no book \"" + reference + "\". search_books finds a book by words of its title.");
        }
        if (found.size() > 1) {
            throw new IllegalArgumentException("Several books are called \"" + reference + "\", name one by its id: "
                    + found.stream().limit(10).map(b -> bookLine(b)).collect(Collectors.joining(" ")));
        }
        return found.getFirst();
    }
}
