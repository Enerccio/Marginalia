package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A lorebook of a book an agent works on has no editing screen: the lorebook view shows the read only phone view of it
 * with the name of the agent, and the editing is back when the run lets go.
 */
class LorebookLockViewTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Lorebook lorebook;
    private Agent agent;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        lorebook = new Lorebook();
        lorebook.setName(uniqueName("lore"));
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Harry");
        entryService.save(entry);

        Manuscript book = manuscript();
        book.setLorebook(lorebook);
        agent = agentService.save(newAgent(manuscriptService.save(book)));
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LorebookLockViewTest::all));
    }

    private LorebookView view() throws Exception {
        LorebookView view = new LorebookView(lorebook, true);
        view.create();
        return view;
    }

    private static boolean visibleButton(Component root, String text) {
        return all(root).filter(Button.class::isInstance).map(Button.class::cast)
                .anyMatch(b -> text.equals(b.getText()) && b.isVisible());
    }

    @Test
    void aFreeLorebookIsEditedInTheDesktopView() throws Exception {
        LorebookView view = view();

        assertThat(all(view).anyMatch(PhoneLorebookView.class::isInstance)).isFalse();
        assertThat(visibleButton(view, loc.getValue(L.LABEL_ADD_ENTRY))).isTrue();
    }

    @Test
    void aLockedLorebookIsShownByTheReadOnlyPhoneView() throws Exception {
        AgentRun run = agentRunService.start(agent);
        LorebookView view = view();

        PhoneLorebookView phone = all(view).filter(PhoneLorebookView.class::isInstance).map(PhoneLorebookView.class::cast)
                .findFirst().orElseThrow();
        assertThat(phone.isVisible()).isTrue();
        assertThat(visibleButton(view, loc.getValue(L.LABEL_ADD_ENTRY))).as("no editing").isFalse();
        String banner = String.format(loc.getValue(L.MSG_AGENT_LOREBOOK_LOCKED), agent.getName());
        assertThat(all(phone).filter(Span.class::isInstance).map(Span.class::cast)
                .anyMatch(s -> s.isVisible() && banner.equals(s.getText()))).as("the banner names the agent").isTrue();
        assertThat(all(phone).filter(com.vaadin.flow.component.checkbox.Checkbox.class::isInstance)
                .map(com.vaadin.flow.component.checkbox.Checkbox.class::cast)
                .filter(c -> c.getLabel() != null && c.getLabel().equals(loc.getValue(L.LABEL_ENABLED)))
                .allMatch(com.vaadin.flow.component.checkbox.Checkbox::isReadOnly)).isTrue();

        // when the run lets go, a refresh gives the editing back
        agentRunService.pause(run);
        view.refreshEntries();

        assertThat(all(view).anyMatch(PhoneLorebookView.class::isInstance)).isFalse();
        assertThat(visibleButton(view, loc.getValue(L.LABEL_ADD_ENTRY))).isTrue();
    }

    @Test
    void thePhoneViewAloneKnowsTheLockToo() throws Exception {
        agentRunService.start(agent);
        PhoneLorebookView phone = new PhoneLorebookView(lorebook);
        phone.create();

        String banner = String.format(loc.getValue(L.MSG_AGENT_LOREBOOK_LOCKED), agent.getName());
        assertThat(all(phone).filter(Span.class::isInstance).map(Span.class::cast)
                .anyMatch(s -> s.isVisible() && banner.equals(s.getText()))).isTrue();
        assertThat(all(phone).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> b.getIcon() != null && b.getTooltip() != null
                        && loc.getValue(L.LABEL_SEARCH_AND_REPLACE).equals(b.getTooltip().getText()))
                .noneMatch(Button::isVisible)).as("no search and replace").isTrue();
    }
}
