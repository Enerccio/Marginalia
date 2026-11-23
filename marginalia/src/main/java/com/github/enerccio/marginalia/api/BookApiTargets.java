package com.github.enerccio.marginalia.api;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.BaseCommandScope;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * The targets of the HTTP API are the regular books of the user: a call holds its book in an {@link AgenticSessionPoint}, and a
 * command for a book runs in its {@link CommandScopes#forApi} scope.
 */
public class BookApiTargets implements ApiTargets {

    @Autowired
    @Lazy
    private CommandScopes commandScopes;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private FolderService folderService;

    @Override
    public SessionPoint newSessionPoint() {
        return new AgenticSessionPoint();
    }

    @Override
    public JsonArray list() throws Exception {
        JsonArray list = new JsonArray();
        for (Manuscript book : manuscriptService.findAllRegularForUser()) {
            JsonObject json = new JsonObject();
            json.addProperty("uuid", book.getUuid());
            json.addProperty("name", book.getName());
            // the folder is a lazy proxy outside of a transaction: its id is there, the rest is loaded
            Folder folder = book.getFolder() == null ? null : folderService.find(book.getFolder().getId());
            json.addProperty("folder", folder == null ? null : folder.getUuid());
            list.add(json);
        }
        return list;
    }

    @Override
    public BaseCommandScope scope(String uuid, SessionPoint sessionPoint, boolean confirm) throws Exception {
        Manuscript book = null;
        if (uuid != null) {
            book = manuscriptService.findForUser(uuid);
            if (book == null || book.isAgentic()) {
                throw new ApiException(404, "No such book: " + uuid);
            }
            ((AgenticSessionPoint) sessionPoint).setManuscript(book);
        }
        return commandScopes.forApi(book, confirm);
    }
}
