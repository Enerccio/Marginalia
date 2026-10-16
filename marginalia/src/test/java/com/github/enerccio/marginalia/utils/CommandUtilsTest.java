package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.parts.ManuscriptPart;
import com.github.enerccio.marginalia.ui.workspace.phone.PhoneWorkspace;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneManuscriptPart;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CommandUtils}: the open book and the workspace, as a command sees them through the session.
 */
class CommandUtilsTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SessionPoint sessionPoint;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    @BeforeEach
    void currentUI() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void noCurrentUI() {
        sessionPoint.setWorkspace(null);
        UI.setCurrent(null);
        ui = null;
    }

    private Manuscript manuscript() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        return manuscriptService.save(manuscript);
    }

    private static JsonObject openState(Manuscript manuscript) {
        return JsonParser.parseString("{\"dialog\":{\"manuscript\":\"" + manuscript.getUuid() + "\",\"tab\":\"prompt\"}}")
                .getAsJsonObject();
    }

    @Test
    void nothingIsReturnedBeforeTheWorkspaceExists() {
        assertThat(CommandUtils.getSessionPoint()).isNotNull();
        assertThat(CommandUtils.getWorkspace()).isNull();
        assertThat(CommandUtils.getManuscriptSection()).isNull();
        assertThat(CommandUtils.getManuscriptWindow()).isNull();
        assertThat(CommandUtils.getCurrentManuscript()).isNull();
        assertThat(CommandUtils.getCurrentAI()).isNull();
    }

    @Test
    void nothingIsReturnedWithoutASession() throws Exception {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        RequestContextHolder.resetRequestAttributes();
        try {
            assertThat(CommandUtils.getSessionPoint()).isNull();
            assertThat(CommandUtils.getWorkspace()).isNull();
            assertThat(CommandUtils.getCurrentManuscript()).isNull();
        } finally {
            RequestContextHolder.setRequestAttributes(attributes);
        }
    }

    @Test
    void phoneWorkspaceWithoutAnOpenBookHasNoCurrentManuscript() throws Exception {
        login();
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();
        sessionPoint.setWorkspace(workspace);

        assertThat(CommandUtils.getWorkspace()).isSameAs(workspace);
        assertThat(CommandUtils.getManuscriptSection()).isInstanceOf(PhoneManuscriptPart.class);
        assertThat(CommandUtils.getManuscriptWindow()).isNull();
        assertThat(CommandUtils.getCurrentManuscript()).isNull();
    }

    @Test
    void phoneWorkspaceHandsOutTheOpenBook() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();
        sessionPoint.setWorkspace(workspace);

        workspace.getManuscriptSection().restoreState(openState(manuscript));

        ManuscriptWindow window = CommandUtils.getManuscriptWindow();
        assertThat(window).isInstanceOf(PhoneManuscriptDialog.class);
        assertThat(window.isOpened()).isTrue();
        assertThat(CommandUtils.getCurrentManuscript().getUuid()).isEqualTo(manuscript.getUuid());
        assertThat(window.asDialog()).isSameAs(window);
    }

    @Test
    void desktopBookListHandsOutTheOpenBook() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        ManuscriptPart part = new ManuscriptPart(null);
        part.create();
        assertThat(part.getOpenWindow()).isNull();

        part.restoreState(openState(manuscript));

        assertThat(part.getOpenWindow()).isInstanceOf(ManuscriptDialog.class);
        assertThat(part.getOpenWindow().getManuscript().getUuid()).isEqualTo(manuscript.getUuid());
    }

    @Test
    void closingTheBookClearsTheCurrentManuscript() throws Exception {
        login();
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();
        sessionPoint.setWorkspace(workspace);
        workspace.getManuscriptSection().restoreState(openState(manuscript()));
        assertThat(CommandUtils.getCurrentManuscript()).isNotNull();

        CommandUtils.getManuscriptWindow().getOnClose().run();

        assertThat(CommandUtils.getCurrentManuscript()).isNull();
    }
}
