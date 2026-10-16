package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.ui.workspace.parts.AIPart;
import com.github.enerccio.marginalia.ui.workspace.parts.ManuscriptPart;
import com.github.enerccio.marginalia.ui.workspace.parts.ProtocolPart;
import com.github.enerccio.marginalia.ui.workspace.parts.UserPart;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneAIPart;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneManuscriptPart;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneProtocolPart;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the sections of the workspace tell the help assistant about themselves and what they let it open.
 */
class WorkspacePartsLLMTest extends MarginaliaTestBase {

    @Autowired
    private Localization loc;

    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private WorkspaceComponent created(WorkspaceComponent part) throws Exception {
        part.create();
        part.refresh();
        return part;
    }

    private String add(L label) {
        return "open-dialog:" + loc.getValue(label);
    }

    @Test
    void theModelsSectionDescribesItselfAndOffersTheDialogToAddOne() throws Exception {
        login();
        AIPart part = (AIPart) created(new AIPart(null));

        assertThat(part.getLLMState()).startsWith("A table of the user's models").contains("0 rows")
                .contains("\"" + loc.getValue(L.LABEL_ADD_MODEL) + "\" opens a dialog");
        assertThat(part.getInteraction()).containsOnlyKeys(add(L.LABEL_ADD_MODEL));
    }

    @Test
    void theAddDialogIsNotOfferedWhileADialogIsOpen() throws Exception {
        login();
        AIPart part = (AIPart) created(new AIPart(null));
        Dialog open = new Dialog();
        open.open();
        ui.add(open);

        assertThat(part.getInteraction()).isEmpty();
    }

    @Test
    void everyListSectionOffersItsAddDialog() throws Exception {
        login();
        Map<WorkspaceComponent, L> sections = new java.util.LinkedHashMap<>();
        sections.put(new PhoneAIPart(null), L.LABEL_ADD_MODEL);
        sections.put(new ProtocolPart(null), L.LABEL_ADD_PROTOCOL);
        sections.put(new PhoneProtocolPart(null), L.LABEL_ADD_PROTOCOL);
        sections.put(new ManuscriptPart(null), L.LABEL_ADD_BOOK);
        sections.put(new PhoneManuscriptPart(null), L.LABEL_ADD_BOOK);

        for (Map.Entry<WorkspaceComponent, L> section : sections.entrySet()) {
            WorkspaceComponent part = created(section.getKey());

            assertThat(part.getInteraction()).as(part.getLLMName()).containsOnlyKeys(add(section.getValue()));
            assertThat(part.getLLMState()).as(part.getLLMName()).startsWith("A table of the user's")
                    .contains(loc.getValue(section.getValue()));
        }
    }

    @Test
    void aSectionThatIsNotBuiltYetOffersNothing() {
        assertThat(new AIPart(null).getInteraction()).isEmpty();
        assertThat(new ManuscriptPart(null).getInteraction()).isEmpty();
        assertThat(new UserPart(null).getInteraction()).isEmpty();
        assertThat(new UserPart(null).getLLMState()).isEmpty();
    }

    @Test
    void theSettingsNameTheirTabsAndLetTheModelSelectOne() throws Exception {
        login();
        UserPart part = (UserPart) created(new UserPart(null));
        String general = loc.getValue(L.LABEL_GENERAL_SETTINGS);

        assertThat(part.getLLMState()).startsWith("The settings of the user").contains(general)
                .contains("The open tab is **" + general + "**");
        Map<String, LLMInteraction> offered = part.getInteraction();
        assertThat(offered).isNotEmpty().doesNotContainKey("open-tab:" + general);

        String other = offered.keySet().iterator().next();
        part.applyInteraction(other);

        assertThat(part.getLLMState()).contains("The open tab is **" + other.substring("open-tab:".length()) + "**");
        assertThat(part.getInteraction()).containsKey("open-tab:" + general);
    }
}
