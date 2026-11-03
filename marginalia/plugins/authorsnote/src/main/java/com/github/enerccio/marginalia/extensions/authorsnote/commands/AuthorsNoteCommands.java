package com.github.enerccio.marginalia.extensions.authorsnote.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.extensions.authorsnote.model.AuthorsNoteData;
import com.github.enerccio.marginalia.extensions.authorsnote.service.AuthorsNoteService;
import org.apache.commons.lang3.StringUtils;

/**
 * The Author's Note as a command: {@code authors-note}, the text of the note for the
 * {@code {{command::authors-note}}} macro, an alternative to its automatic insertion by the depth and the role.
 * It answers with the note as it is stored now, whether or not <i>Insert into prompt</i> is switched on, so a template
 * that places the note itself does not depend on the switch (and the note is in the prompt twice when both are used).
 */
public class AuthorsNoteCommands extends ManuscriptReadSupport {

    public static final String AUTHORS_NOTE = "authors-note";

    private static final String AUTHORS_NOTE_TEXT = "The Author's Note of the book as plain text, to put it where you want in a "
            + "template with the {{command::authors-note}} macro. Empty when the note is empty. Reads only.";

    private final AuthorsNoteService service;

    public AuthorsNoteCommands(AuthorsNoteService service) {
        this.service = service;
    }

    @Command(commandName = AUTHORS_NOTE, humanNameText = "Author's Note", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            availableInMacros = true, availableForAgent = false, descriptionText = AUTHORS_NOTE_TEXT,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: the Author's Note; empty when there is none"))
    public void authorsNote(CommandContext context) throws Exception {
        Book book = new Book(context);
        AuthorsNoteData data = service.loadCurrent(book.manuscript);
        context.setReturn(StringUtils.trimToEmpty(data.getNote()));
    }
}
