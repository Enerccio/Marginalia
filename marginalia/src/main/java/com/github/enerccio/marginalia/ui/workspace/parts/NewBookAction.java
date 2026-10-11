package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Asks for a name, creates a book with the user's default model and protocol and opens it in the book dialog.
 * Shared by the book lists of the desktop and phone workspaces.
 */
@Configurable
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

    private final Runnable onClose;

    /**
     * @param onClose called when the book dialog is closed
     */
    public NewBookAction(Runnable onClose) {
        this.onClose = onClose;
    }

    public void run() {
        TextInputDialog dialog = new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_BOOK), name -> {
            try {
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

                manuscript = manuscriptService.save(manuscript);
                manuscriptService.markOpened(manuscript);
                ManuscriptDialog manuscriptDialog = new ManuscriptDialog(manuscript);
                manuscriptDialog.setOnClose(onClose);
                manuscriptDialog.create();
                manuscriptDialog.open();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }).messageRequired().showCancel(true).build();
        dialog.open();
    }

}
