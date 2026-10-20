package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
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

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The link next to the token count of the lorebook view counts the tokens of the lorebook with those of its subbooks.
 */
class LorebookSubbookTokensTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LorebookSubbookTokensTest::all));
    }

    private Lorebook lorebook(String name, boolean enabled) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setEnabled(enabled);
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private long entry(Lorebook lorebook, String payload, boolean enabled) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Entry");
        entry.setPayload(payload);
        entry.setEnabled(enabled);
        entryService.save(entry);
        return enabled ? tokenizerService.countTokensApprox(payload) : 0;
    }

    @Test
    void theLinkCountsTheEnabledEntriesOfTheEnabledSubbooks() throws Exception {
        Lorebook root = lorebook("Root", true);
        Lorebook sub = lorebook("Sub", true);
        Lorebook deep = lorebook("Deep", true);
        Lorebook off = lorebook("Off", false);
        Lorebook underOff = lorebook("UnderOff", true);
        lorebookService.addSubbook(root, sub);
        lorebookService.addSubbook(sub, deep);
        lorebookService.addSubbook(root, off);
        lorebookService.addSubbook(off, underOff);
        // the lorebook that is a subbook twice counts once
        lorebookService.addSubbook(root, deep);
        long expected = entry(root, "The root has a few words of lore in it.", true)
                + entry(root, "A disabled entry is not counted at all.", false)
                + entry(sub, "The subbook has its own words as well, more of them.", true)
                + entry(deep, "Deep down.", true);
        entry(off, "A disabled subbook is not used by the generation.", true);
        entry(underOff, "Neither is what is under it.", true);

        LorebookView view = new LorebookView(root, true);
        view.create();
        Button link = all(view).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> loc.getValue(L.LABEL_COUNT_TOKENS_WITH_SUBBOOKS).equals(b.getText())).findFirst().orElseThrow();
        assertThat(link.isVisible()).isTrue();
        String text = String.format(loc.getValue(L.LABEL_TOKENS_WITH_SUBBOOKS), expected);
        assertThat(all(view).filter(Span.class::isInstance).map(Span.class::cast).map(Span::getText)).doesNotContain(text);

        link.click();

        assertThat(all(view).filter(Span.class::isInstance).map(Span.class::cast).map(Span::getText)).contains(text);
    }
}
