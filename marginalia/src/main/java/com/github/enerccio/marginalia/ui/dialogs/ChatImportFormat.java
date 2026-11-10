package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.LmStudioChats;
import com.github.enerccio.marginalia.utils.SillyTavernChats;

import java.util.function.BiFunction;

/**
 * A program whose saved chats can be imported as the tree of parts of a new book (the book import dialog, {@code BookImportDialog}): how its files
 * are read and what the dialogs say. Both formats are read into the {@link SillyTavernChats.Chat}, joined by
 * {@link SillyTavernChats#merge} and stored by {@code SillyTavernImportService}.
 */
public enum ChatImportFormat {

    SILLYTAVERN(L.LABEL_IMPORT_FROM_SILLYTAVERN, L.MSG_IMPORT_SILLYTAVERN, L.MSG_INVALID_SILLYTAVERN_CHAT, L.MSG_IMPORTING_SILLYTAVERN,
            RevisionDetail.SILLYTAVERN_IMPORT, SillyTavernChats::parse, new String[]{".jsonl", ".json", "application/json", "application/x-ndjson"}),
    LMSTUDIO(L.LABEL_IMPORT_FROM_LMSTUDIO, L.MSG_IMPORT_LMSTUDIO, L.MSG_INVALID_LMSTUDIO_CHAT, L.MSG_IMPORTING_LMSTUDIO,
            RevisionDetail.LMSTUDIO_IMPORT, LmStudioChats::parse, new String[]{".json", "application/json"});

    private final L title;
    private final L description;
    private final L invalid;
    private final L progress;
    private final RevisionDetail source;
    private final BiFunction<String, String, SillyTavernChats.Chat> parser;
    private final String[] fileTypes;

    ChatImportFormat(L title, L description, L invalid, L progress, RevisionDetail source,
                     BiFunction<String, String, SillyTavernChats.Chat> parser, String[] fileTypes) {
        this.title = title;
        this.description = description;
        this.invalid = invalid;
        this.progress = progress;
        this.source = source;
        this.parser = parser;
        this.fileTypes = fileTypes;
    }

    /**
     * @return the label of the menu item and the title of the dialog
     */
    public L title() {
        return title;
    }

    /**
     * @return what the user is asked to upload, in the dialog
     */
    public L description() {
        return description;
    }

    /**
     * @return the warning for a file that is not a chat of the format, with the name of the file
     */
    public L invalid() {
        return invalid;
    }

    /**
     * @return the title of the progress dialog of the import
     */
    public L progress() {
        return progress;
    }

    /**
     * @return what the revisions of the imported parts say
     */
    public RevisionDetail source() {
        return source;
    }

    public String[] fileTypes() {
        return fileTypes.clone();
    }

    /**
     * @param fileName the name of the uploaded file
     * @throws IllegalArgumentException when the text is not a chat of the format
     */
    public SillyTavernChats.Chat parse(String text, String fileName) {
        return parser.apply(text, fileName);
    }
}
