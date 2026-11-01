package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.styles.PartStyleApplier;
import com.github.enerccio.marginalia.ui.styles.ReaderStyleChooser;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The style button of the reader: only the styles of the reader are offered, the choice is kept in the (cookie) store.
 */
class ReaderStyleChooserTest extends MarginaliaTestBase {

    @Autowired
    private PartStyleService service;

    @Autowired
    private Localization loc;

    private static class MemoryStore implements ReaderStyleChooser.ChoiceStore {
        final AtomicReference<String> value = new AtomicReference<>();

        MemoryStore(String initial) {
            value.set(initial);
        }

        @Override
        public String read() {
            return value.get();
        }

        @Override
        public void write(String styleId) {
            value.set(styleId);
        }
    }

    private static void click(MenuItem item) {
        ComponentUtil.fireEvent(item, new ClickEvent<>(item));
    }

    @Test
    void theBookStyleIsShownUntilTheReaderChooses() throws Exception {
        login();
        List<String> shown = new ArrayList<>();

        ReaderStyleChooser chooser = new ReaderStyleChooser(loc, service, style -> shown.add(style.getId()), new MemoryStore(null));

        assertThat(chooser.getCurrent().getId()).isEqualTo("book");
        assertThat(shown).containsExactly("book");
        assertThat(chooser.getItem("book").isChecked()).isTrue();
        assertThat(chooser.getItem("normal").isChecked()).isFalse();
    }

    @Test
    void aChoiceIsAppliedKeptAndRememberedTheNextTime() throws Exception {
        login();
        PartStyle mine = service.save(new PartStyle(null, "Mine").set(PartStyleOption.FONT_SIZE, "2rem"));
        MemoryStore store = new MemoryStore(null);
        Div host = new Div();
        ReaderStyleChooser chooser = new ReaderStyleChooser(loc, service, style -> PartStyleApplier.apply(host, style), store);
        assertThat(host.getClassNames()).containsExactly("part-style-book");

        click(chooser.getItem(mine.getId()));

        assertThat(store.read()).isEqualTo(mine.getId());
        assertThat(host.getClassNames()).containsExactly(mine.getCssClass());
        assertThat(chooser.getItem(mine.getId()).isChecked()).isTrue();
        assertThat(chooser.getItem("book").isChecked()).isFalse();

        // the next book read in the browser starts with it
        ReaderStyleChooser next = new ReaderStyleChooser(loc, service, style -> {
        }, store);
        assertThat(next.getCurrent().getId()).isEqualTo(mine.getId());

        click(next.getItem("normal"));
        assertThat(store.read()).isEqualTo("normal");
    }

    @Test
    void onlyTheStylesOfTheReaderAreOfferedNotOfThePersonWhoseCookieIsStillThere() throws Exception {
        login();
        PartStyle theirs = service.save(new PartStyle(null, "Theirs").set(PartStyleOption.FONT_SIZE, "2rem"));
        MemoryStore store = new MemoryStore(theirs.getId());

        // another user reads (a published book of the first one, or just in the same browser)
        login();
        PartStyle own = service.save(new PartStyle(null, "Own").set(PartStyleOption.FONT_SIZE, "1rem"));
        ReaderStyleChooser chooser = new ReaderStyleChooser(loc, service, style -> {
        }, store);

        assertThat(chooser.getItem(theirs.getId())).isNull();
        assertThat(chooser.getItem(own.getId())).isNotNull();
        assertThat(chooser.getCurrent().getId()).as("the style of the cookie is not the reader's: the Book style").isEqualTo("book");
    }
}
