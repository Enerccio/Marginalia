package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.write;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService.EntryProblem;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read.LorebookReadSupport;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.CommandUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * What the commands that change the lorebooks of the open book have in common. Like the parts of the story they are
 * reached only through the book (its root lorebook and the subbooks of it), and they are addressed by <b>handle</b> only:
 * the number of a lorebook or of an entry moves when lorebooks or entries are added or deleted. The handle is the
 * {@code handle} of the rows of {@code list-lorebooks}, {@code list-lore-entries} and {@code get-lore-entry}.
 */
@Configurable
public abstract class LorebookWriteSupport extends LorebookReadSupport {

    @Autowired
    protected TagService tagService;

    /**
     * The lorebooks of the book for a command that adds something. A book that has no lorebook gets its root first, named
     * like the book: the lore of such a book can only be started from here, nothing else makes the root.
     */
    protected Lore loreToAddTo(Book book) throws Exception {
        Manuscript manuscript = book.manuscript;
        Lorebook root = manuscript.getLorebook() == null ? null : lorebookService.find(manuscript.getLorebook());
        if (root == null || root.isDeleted()) {
            Lorebook created = new Lorebook();
            created.setName(manuscript.getName());
            created.setEnabled(true);
            created.setAgentic(manuscript.isAgentic());
            created.setSubbooks(new ArrayList<>());
            manuscript.setLorebook(lorebookService.save(created));
            manuscriptService.save(manuscript);
        }
        return new Lore(book);
    }

    /**
     * The lorebook of the book with that handle. A number is refused, see the class description.
     */
    protected Lorebook book(Lore lore, String handle) throws Exception {
        if (!isHandle(handle)) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_HANDLE_REQUIRED));
        }
        return lore.book(handle);
    }

    /**
     * The entry of one of the lorebooks of the book with that handle.
     */
    protected Lore.Found entry(Lore lore, String handle) throws Exception {
        if (!isHandle(handle)) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_HANDLE_REQUIRED));
        }
        return lore.find(null, handle);
    }

    protected static JsonArray problems(List<EntryProblem> problems) {
        JsonArray rows = new JsonArray();
        for (EntryProblem problem : problems) {
            JsonObject row = new JsonObject();
            row.addProperty("kind", problem.kind().name());
            row.addProperty("field", problem.field());
            row.addProperty("message", problem.message());
            rows.add(row);
        }
        return rows;
    }

    /**
     * The values of a list given as one text, separated by commas, semicolons or line breaks.
     */
    protected static List<String> list(String text) {
        List<String> values = new ArrayList<>();
        if (text != null) {
            for (String value : text.split("[,;\\n]")) {
                if (!value.isBlank()) {
                    values.add(value.strip());
                }
            }
        }
        return values;
    }

    /**
     * Shows the book as it is now to the user whose command changed it; an agent has no window.
     */
    protected static void changed() {
        CommandUtils.reloadManuscriptWindow();
    }

    /**
     * Asks whoever called the command to confirm; false (and the answer that nothing was changed) when they do not.
     */
    protected boolean confirmed(CommandContext context, String question) throws InterruptedException {
        if (context.confirm(question)) {
            return true;
        }
        if (!context.isTerminated()) {
            context.setReturn(loc.getValue(L.MSG_COMMAND_NOTHING_CHANGED));
        }
        return false;
    }
}
