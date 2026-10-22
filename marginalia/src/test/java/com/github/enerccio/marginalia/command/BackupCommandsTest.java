package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.phone.PhoneWorkspace;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code /take-backup}: backs up the book that is open, on a worker thread with the session of the caller.
 */
class BackupCommandsTest extends MarginaliaTestBase {

    @Autowired
    private CommandService commandService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private BackupService backupService;

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

    private Workspace workspaceWithOpenBook(Manuscript manuscript) throws Exception {
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();
        sessionPoint.setWorkspace(workspace);
        if (manuscript != null) {
            JsonObject state = JsonParser.parseString("{\"dialog\":{\"manuscript\":\"" + manuscript.getUuid()
                    + "\",\"tab\":\"prompt\"}}").getAsJsonObject();
            workspace.getManuscriptSection().restoreState(state);
        }
        return workspace;
    }

    @Test
    void takesABackupOfTheOpenBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript = manuscriptService.save(manuscript);
        workspaceWithOpenBook(manuscript);
        assertThat(backupService.getBackups(manuscript)).isEmpty();

        CommandRun run = commandService.executeLine("/take-backup");

        assertThat((String) run.get(30, TimeUnit.SECONDS)).contains(manuscript.getName());
        assertThat(backupService.getBackups(manuscript)).hasSize(1);
        assertThat(backupService.getBackups(manuscript).getFirst().getManuscriptName()).isEqualTo(manuscript.getName());
        // it reported what it was doing
        assertThat(run.getStatus()).contains(manuscript.getName());
        assertThat(run.getSubStatus()).isNotBlank();
    }

    @Test
    void failsWhenNoBookIsOpen() throws Exception {
        login();
        workspaceWithOpenBook(null);

        CommandRun run = commandService.executeLine("/take-backup");

        assertThatThrownBy(() -> run.get(30, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class);
    }

}
