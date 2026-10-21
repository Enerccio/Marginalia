package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ArchiveImportRunner;
import com.github.enerccio.marginalia.ui.dialogs.ArchiveUploadDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Asks for a name, creates a book with the user's default model and protocol and hands it to the book list to open,
 * so that the list knows which book window is open ({@link ManuscriptSection#getOpenWindow()}). Shared by the book
 * lists of the desktop and phone workspaces, each opens its own kind of window.
 */
@Configurable
@Extendable
public class NewBookAction {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final Opener opener;

    /**
     * What opens the new book in the window of the list.
     */
    @FunctionalInterface
    public interface Opener {
        void open(Manuscript manuscript) throws Exception;
    }

    /**
     * @param opener opens the created book, the way the list opens a book from its rows
     */
    public NewBookAction(Opener opener) {
        this.opener = opener;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = TextInputDialog.class)})
    public void run() {
        TextInputDialog dialog = new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_BOOK), name -> {
            try {
                opener.open(createBook(name));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }).messageRequired().showCancel(true).build();
        dialog.open();
    }

    /**
     * Asks for a zip archive of {@code .md} files and opens a new book named after it, with a part for each file, see
     * {@link ArchiveImportService#appendParts}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    public void runFromArchive() {
        ArchiveUploadDialog.show(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE), loc.getValue(L.MSG_IMPORT_ARCHIVE_NEW_BOOK),
                (archive, fileName) -> {
                    // the archive is checked before the book is made, a book is not left empty by a bad file
                    MarkdownArchive.Content content = MarkdownArchive.read(archive);
                    String name = fileName.toLowerCase().endsWith(".zip") ? fileName.substring(0, fileName.length() - 4) : fileName;
                    Manuscript manuscript = createBook(StringUtils.defaultIfBlank(name.trim(), loc.getValue(L.LABEL_NEW_BOOK)));
                    // a stopped import leaves the book with the parts until then, it is opened too
                    new ArchiveImportRunner().appendParts(manuscript, content, imported -> opener.open(manuscriptService.find(manuscript)));
                });
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Manuscript.class),
            parameters = {@APIParameter(name = "name", type = String.class)},
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "userSetting", type = UserSetting.class),
                    @APILocals(name = "defaultModelId", type = Long.class),
                    @APILocals(name = "ai", type = AI.class),
                    @APILocals(name = "defaultProtocolId", type = Long.class),
                    @APILocals(name = "protocol", type = Protocol.class)})
    private Manuscript createBook(String name) throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(name);

        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        Long defaultModelId = userSetting.getDefaultModel();
        if (defaultModelId != null) {
            AI ai = aiService.find(defaultModelId);
            if (ai != null && !ai.isDeleted()) {
                manuscript.setAi(ai);
            }
        }
        Long defaultProtocolId = userSetting.getDefaultProtocol();
        if (defaultProtocolId != null) {
            Protocol protocol = protocolService.find(defaultProtocolId);
            if (protocol != null && !protocol.isDeleted()) {
                manuscript.setProtocol(protocol);
            }
        }

        return manuscriptService.save(manuscript);
    }

}
