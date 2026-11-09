package com.github.enerccio.marginalia.extensions.storynotes.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.storynotes.model.NoteBook;
import com.github.enerccio.marginalia.extensions.storynotes.model.NotePage;
import com.github.enerccio.marginalia.extensions.storynotes.service.StoryNotesService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.storynotes.StoryNotesLocalization.DELETE_THE_NOTE_PAGE;
import static com.github.enerccio.marginalia.extensions.storynotes.StoryNotesLocalization.T;

/**
 * The notes of the book as commands, for the command bar, for macros, for agents and for the plot wizard of the Arc
 * Outline: {@code list-notes}, {@code get-note}, {@code search-notes}, {@code create-note}, {@code update-note},
 * {@code append-to-note}, {@code edit-note}, {@code delete-note} and {@code note-text}, the text of a page for the
 * {@code {{command::note-text}}} macro.
 * <p>
 * A page is addressed by its <b>title</b> (the case does not matter), which is unique in the book. <b>The pages the
 * author hid from the assistants do not exist for these commands</b>: they are not listed, found, read, linked or changed,
 * and a title that only a hidden page has is refused like any other that can't be used. Reading and adding is
 * {@code NON_DESTRUCTIVE}; a command that changes or removes text that exists ({@code update-note}, {@code edit-note},
 * {@code delete-note}) is {@code DESTRUCTIVE}, so an agent or the plot wizard asks the author first when it is set to.
 * A page a command changed is marked, the open Notes tab shows it and the change at once.
 */
public class StoryNotesCommands extends ManuscriptReadSupport {

    public static final String LIST = "list-notes";
    public static final String GET = "get-note";
    public static final String SEARCH = "search-notes";
    public static final String CREATE = "create-note";
    public static final String UPDATE = "update-note";
    public static final String APPEND = "append-to-note";
    public static final String EDIT = "edit-note";
    public static final String DELETE = "delete-note";
    public static final String TEXT = "note-text";

    private static final String LIST_TEXT = "Lists the pages of the notes of the book with their tags. Reads only.";
    private static final String LIST_LLM = "Lists the pages of the author's notes of the book (their own wiki about plots, ideas, rules of the "
            + "world, plans) ordered by title, with the tags, the first line and the length of each, as JSON. Pass a tag to see only "
            + "the pages with it. Pages are addressed by title in the other commands. get-note returns the text. Reads only.";
    private static final String GET_TEXT = "Returns a page of the notes with its text and links. Reads only.";
    private static final String GET_LLM = "Returns one page of the author's notes: title, tags, the full text (Markdown) and the pages it links "
            + "to with [[Title]] (whether they exist) and the pages that link to it. Follow a link by calling get-note with its title. Reads only.";
    private static final String SEARCH_TEXT = "Finds the pages of the notes that contain a text. Reads only.";
    private static final String SEARCH_LLM = "Finds the pages of the author's notes whose title, tags or text contain the query (the case does "
            + "not matter), the title matches first, with a piece of the text around the match. Reads only.";
    private static final String CREATE_TEXT = "Adds a page to the notes of the book.";
    private static final String CREATE_LLM = "Adds a page to the author's notes and returns it. The title is unique (the case does not matter) "
            + "and has no brackets or bars; the text is Markdown, and [[Another title]] in it links to another page ([[Another title|text shown]] shows other text). Tags are one text, separated by "
            + "commas. Use it when the author asks you to keep something in the notes.";
    private static final String UPDATE_TEXT = "Changes the title, text or tags of a page of the notes.";
    private static final String UPDATE_LLM = "Changes a page of the author's notes. Pass only what to change (null leaves it as it is): the new "
            + "title (the [[links]] to the page in the other pages follow), the new FULL text (it replaces the old one; to add to a page use "
            + "append-to-note, to change a sentence edit-note), the tags (one text, separated by commas, replacing the old ones). Returns the page.";
    private static final String APPEND_TEXT = "Adds text at the end of a page of the notes.";
    private static final String APPEND_LLM = "Adds a paragraph at the end of a page of the author's notes and leaves the rest of the page as it is. "
            + "Returns the page.";
    private static final String EDIT_TEXT = "Replaces a piece of text in a page of the notes, once or everywhere.";
    private static final String EDIT_LLM = "Replaces a piece of text of a page of the author's notes with another one, the rest of the page "
            + "stays. 'find' must be in the page exactly as written (the case matters). By default it must be there once: if it is not, or "
            + "is more than once, nothing is changed and you get an error, so include enough around it. Pass all true to replace every "
            + "occurrence (to rename a name or a term all through the page). An empty 'replace' deletes the piece. Returns the page and how "
            + "many pieces were replaced.";
    private static final String DELETE_TEXT = "Deletes a page of the notes.";
    private static final String DELETE_LLM = "Deletes a page of the author's notes. Destructive.";
    private static final String NOTE_TEXT_TEXT = "The text of a page of the notes as plain text, to put it where you want in a template with the "
            + "{{command::note-text}} macro. Empty when there is no such page. Reads only.";

    private static final String PARAM_NOTE = "Title of the page (the case does not matter)";
    private static final String PARAM_TITLE = "The title: unique in the book, no brackets, bars or line breaks";
    private static final String PARAM_CONTENT = "The text of the page, Markdown; [[Title]] links to another page, [[Title|shown text]] links with other text";
    private static final String PARAM_TAGS = "Tags separated by commas";
    private static final String RESULT_NOTE = "JSON: the page as it is now";
    private static final String PARAM_FIND = "The text to replace, exactly as it is in the page (the case matters)";
    private static final String PARAM_REPLACE = "The new text; empty deletes the piece";
    private static final String RESULT_EDIT = "JSON: the page as it is now, and how many pieces were replaced";

    private static final int SNIPPET = 160;

    private final StoryNotesService service;

    public StoryNotesCommands(StoryNotesService service) {
        this.service = service;
    }

    // --- list-notes / get-note / search-notes ---

    @Command(commandName = LIST, linkedCommand = GET, humanNameText = "List notes", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the pages"))
    public void list(CommandContext context) throws Exception {
        runList(context);
    }

    @Command(commandName = LIST, linkedCommand = GET, humanNameText = "List notes", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "tag", type = String.class, optional = true, descriptionText = "Only the pages with this tag")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the pages"))
    public void listTag(CommandContext context) throws Exception {
        runList(context);
    }

    private void runList(CommandContext context) throws Exception {
        Book book = new Book(context);
        NoteBook notes = service.read(book.manuscript);
        String tag = StringUtils.trimToNull(context.get("tag", String.class));
        JsonArray list = new JsonArray();
        for (NotePage page : notes.visible()) {
            if (tag == null || NoteBook.hasTag(page, tag)) {
                JsonObject row = new JsonObject();
                row.addProperty("title", page.getTitle());
                row.add("tags", tags(page));
                row.addProperty("firstLine", NoteBook.firstLine(page.getContent(), 100));
                row.addProperty("length", page.getContent().length());
                list.add(row);
            }
        }
        JsonObject result = new JsonObject();
        result.add("notes", list);
        if (list.isEmpty()) {
            result.addProperty("hint", tag == null ? "The book has no notes yet, create-note adds a page"
                    : "No page has the tag " + tag);
        }
        answer(context, result);
    }

    @Command(commandName = GET, linkedCommand = LIST, humanNameText = "Get note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = GET_TEXT, llmDescriptionText = GET_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void get(CommandContext context) throws Exception {
        Book book = new Book(context);
        NoteBook notes = service.read(book.manuscript);
        answer(context, pageRow(notes, page(notes, context.get("note", String.class))));
    }

    @Command(commandName = SEARCH, linkedCommand = GET, humanNameText = "Search notes", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = SEARCH_TEXT, llmDescriptionText = SEARCH_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "query", type = String.class, descriptionText = "The text to look for")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the pages that contain it"))
    public void search(CommandContext context) throws Exception {
        Book book = new Book(context);
        NoteBook notes = service.read(book.manuscript);
        String query = context.get("query", String.class);
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Give the text to look for");
        }
        JsonArray list = new JsonArray();
        for (NotePage page : notes.search(query, true)) {
            JsonObject row = new JsonObject();
            row.addProperty("title", page.getTitle());
            row.add("tags", tags(page));
            row.addProperty("match", NoteBook.snippet(page.getContent(), query, SNIPPET));
            list.add(row);
        }
        JsonObject result = new JsonObject();
        result.add("notes", list);
        if (list.isEmpty()) {
            result.addProperty("hint", "No page contains " + query.strip());
        }
        answer(context, result);
    }

    // --- create-note ---

    @Command(commandName = CREATE, humanNameText = "Create note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void create(CommandContext context) throws Exception {
        runCreate(context);
    }

    @Command(commandName = CREATE, humanNameText = "Create note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "content", type = String.class, optional = true, descriptionText = PARAM_CONTENT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void createContent(CommandContext context) throws Exception {
        runCreate(context);
    }

    @Command(commandName = CREATE, humanNameText = "Create note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = CREATE_TEXT, llmDescriptionText = CREATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "content", type = String.class, optional = true, descriptionText = PARAM_CONTENT),
                    @CommandParameter(name = "tags", type = String.class, optional = true, descriptionText = PARAM_TAGS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void createTags(CommandContext context) throws Exception {
        runCreate(context);
    }

    private void runCreate(CommandContext context) throws Exception {
        Book book = new Book(context);
        String title = NoteBook.cleanTitle(context.get("title", String.class));
        String content = NoteBook.cleanContent(context.get("content", String.class));
        List<String> tags = NoteBook.parseTags(context.get("tags", String.class));
        NotePage created = service.update(book.manuscript, notes -> {
            // a hidden page has its title too, the message must not tell
            if (notes.titleTaken(title, null)) {
                throw new IllegalArgumentException("The title " + title + " can't be used, choose another one");
            }
            NotePage page = new NotePage();
            page.setTitle(title);
            page.setContent(content);
            page.setTags(NoteBook.cleanTags(tags));
            page.setChangedByCommand(true);
            notes.put(page);
            return page;
        });
        answer(context, pageRow(service.read(book.manuscript), created));
    }

    // --- update-note ---

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update note", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void update(CommandContext context) throws Exception {
        runUpdate(context);
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update note", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "content", type = String.class, optional = true, descriptionText = PARAM_CONTENT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void updateContent(CommandContext context) throws Exception {
        runUpdate(context);
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update note", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = UPDATE_TEXT, llmDescriptionText = UPDATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "content", type = String.class, optional = true, descriptionText = PARAM_CONTENT),
                    @CommandParameter(name = "tags", type = String.class, optional = true, descriptionText = PARAM_TAGS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void updateTags(CommandContext context) throws Exception {
        runUpdate(context);
    }

    private void runUpdate(CommandContext context) throws Exception {
        Book book = new Book(context);
        String reference = context.get("note", String.class);
        String title = context.get("title", String.class);
        String content = context.get("content", String.class);
        String tags = context.get("tags", String.class);
        if (title == null && content == null && tags == null) {
            throw new IllegalArgumentException("Nothing to change: pass the title, the content or the tags");
        }
        String newContent = content == null ? null : NoteBook.cleanContent(content);
        List<String> newTags = tags == null ? null : NoteBook.parseTags(tags);
        NotePage changed = service.update(book.manuscript, notes -> {
            NotePage page = page(notes, reference);
            if (title != null) {
                checkTitle(notes, page, title);
                notes.rename(page, title);
            }
            if (newContent != null) {
                page.setContent(newContent);
            }
            if (newTags != null) {
                page.setTags(NoteBook.cleanTags(newTags));
            }
            changedByCommand(page);
            return page;
        });
        answer(context, pageRow(service.read(book.manuscript), changed));
    }

    // --- append-to-note / edit-note ---

    @Command(commandName = APPEND, linkedCommand = GET, humanNameText = "Append to note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = APPEND_TEXT, llmDescriptionText = APPEND_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "text", type = String.class, descriptionText = "The text to add after the existing one")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NOTE))
    public void append(CommandContext context) throws Exception {
        Book book = new Book(context);
        String reference = context.get("note", String.class);
        String text = context.get("text", String.class);
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Give the text to add");
        }
        NotePage changed = service.update(book.manuscript, notes -> {
            NotePage page = page(notes, reference);
            String old = page.getContent();
            page.setContent(NoteBook.cleanContent(old.isBlank() ? text.strip() : old.stripTrailing() + "\n\n" + text.strip()));
            changedByCommand(page);
            return page;
        });
        answer(context, pageRow(service.read(book.manuscript), changed));
    }

    @Command(commandName = EDIT, linkedCommand = GET, humanNameText = "Edit note", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = EDIT_TEXT, llmDescriptionText = EDIT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "find", type = String.class, descriptionText = PARAM_FIND),
                    @CommandParameter(name = "replace", type = String.class, descriptionText = PARAM_REPLACE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_EDIT))
    public void edit(CommandContext context) throws Exception {
        runEdit(context);
    }

    @Command(commandName = EDIT, linkedCommand = GET, humanNameText = "Edit note", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = EDIT_TEXT, llmDescriptionText = EDIT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "find", type = String.class, descriptionText = PARAM_FIND),
                    @CommandParameter(name = "replace", type = String.class, descriptionText = PARAM_REPLACE),
                    @CommandParameter(name = "all", type = Boolean.class, optional = true, defaultValue = "false",
                            descriptionText = "true replaces every occurrence of the text; false (default) needs it to be in the page exactly once")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_EDIT))
    public void editAll(CommandContext context) throws Exception {
        runEdit(context);
    }

    private void runEdit(CommandContext context) throws Exception {
        Book book = new Book(context);
        String reference = context.get("note", String.class);
        String find = context.get("find", String.class);
        String replace = StringUtils.defaultString(context.get("replace", String.class));
        boolean all = Boolean.TRUE.equals(context.get("all", Boolean.class));
        if (StringUtils.isEmpty(find)) {
            throw new IllegalArgumentException("Give the text to replace");
        }
        int[] replaced = new int[1];
        NotePage changed = service.update(book.manuscript, notes -> {
            NotePage page = page(notes, reference);
            NoteBook.Replacement result;
            try {
                result = NoteBook.replace(page.getContent(), find, replace, all);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage() + " (note " + page.getTitle() + ")");
            }
            page.setContent(NoteBook.cleanContent(result.content()));
            replaced[0] = result.count();
            changedByCommand(page);
            return page;
        });
        JsonObject row = pageRow(service.read(book.manuscript), changed);
        row.addProperty("replaced", replaced[0]);
        answer(context, row);
    }

    // --- delete-note ---

    @Command(commandName = DELETE, linkedCommand = LIST, humanNameText = "Delete note", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = DELETE_TEXT, llmDescriptionText = DELETE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void delete(CommandContext context) throws Exception {
        Book book = new Book(context);
        NoteBook notes = service.read(book.manuscript);
        NotePage page = page(notes, context.get("note", String.class));
        String title = page.getTitle();
        if (!context.confirm(T.localized(DELETE_THE_NOTE_PAGE).formatted(title))) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        String uuid = page.getUuid();
        service.update(book.manuscript, current -> {
            NotePage stored = current.get(uuid);
            if (stored != null) {
                current.remove(stored);
            }
            return null;
        });
        context.setReturn("Deleted the note " + title);
    }

    // --- note-text (macro) ---

    @Command(commandName = TEXT, humanNameText = "Text of a note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            availableInMacros = true, availableForAgent = false, descriptionText = NOTE_TEXT_TEXT,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "note", type = String.class, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: the text of the page; empty when there is none"))
    public void text(CommandContext context) throws Exception {
        Book book = new Book(context);
        NoteBook notes = service.read(book.manuscript);
        NotePage page = notes.byTitle(context.get("note", String.class));
        context.setReturn(page == null || !page.isAiVisible() ? "" : page.getContent().strip());
    }

    // --- helpers ---

    /** The page with that title, as long as the assistants may see it. */
    private static NotePage page(NoteBook notes, String title) {
        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Give the title of the page");
        }
        NotePage page = notes.byTitle(title);
        if (page == null || !page.isAiVisible()) {
            throw new IllegalArgumentException("There is no note called " + title.strip() + " (list-notes shows the titles)");
        }
        return page;
    }

    private static void checkTitle(NoteBook notes, NotePage page, String title) {
        String clean = NoteBook.cleanTitle(title);
        if (notes.titleTaken(clean, page.getUuid())) {
            throw new IllegalArgumentException("The title " + clean + " can't be used, choose another one");
        }
    }

    private static void changedByCommand(NotePage page) {
        page.setChangedByCommand(true);
        page.touch();
    }

    private static JsonArray tags(NotePage page) {
        JsonArray tags = new JsonArray();
        page.getTags().forEach(tags::add);
        return tags;
    }

    /** The page with its text, the pages it links to (only the ones the assistants may see exist) and the ones that link to it. */
    private static JsonObject pageRow(NoteBook notes, NotePage page) {
        JsonObject row = new JsonObject();
        row.addProperty("title", page.getTitle());
        row.add("tags", tags(page));
        row.addProperty("content", page.getContent());
        JsonArray links = new JsonArray();
        for (String title : NoteBook.linksIn(page.getContent())) {
            NotePage target = notes.byTitle(title);
            JsonObject link = new JsonObject();
            boolean exists = target != null && target.isAiVisible();
            link.addProperty("title", exists ? target.getTitle() : title);
            link.addProperty("exists", exists);
            links.add(link);
        }
        row.add("links", links);
        JsonArray linkedFrom = new JsonArray();
        notes.backlinks(page).stream().filter(NotePage::isAiVisible).forEach(p -> linkedFrom.add(p.getTitle()));
        row.add("linkedFrom", linkedFrom);
        return row;
    }
}
