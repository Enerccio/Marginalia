package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem.Choice;
import com.github.enerccio.marginalia.ui.styles.PartStyleApplier;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Giving a text style to the element that holds the parts, and the entries of the "Change styles" menu of the story.
 */
class PartStyleApplierTest extends MarginaliaTestBase {

    @Autowired
    private PartStyleService service;

    @Autowired
    private Localization loc;

    @BeforeEach
    void user() throws Exception {
        login();
    }

    @Test
    void applyMakesTheStyleTheOnlyOneOfTheElement() throws Exception {
        Div host = new Div();
        host.addClassName("keep-me");

        PartStyleApplier.apply(host, service.find("book"));
        assertThat(host.getClassNames()).containsExactlyInAnyOrder("keep-me", "part-style-book");

        PartStyle mine = service.save(new PartStyle(null, "Mine").set(PartStyleOption.FONT_SIZE, "2rem"));
        PartStyleApplier.apply(host, mine);
        assertThat(host.getClassNames()).containsExactlyInAnyOrder("keep-me", mine.getCssClass());

        PartStyleApplier.apply(host, service.find("normal"));
        assertThat(host.getClassNames()).containsExactly("keep-me");
    }

    @Test
    void theEntriesAreTheDefaultThenTheStylesSortedByNameWithTheChosenOneTicked() throws Exception {
        service.save(new PartStyle(null, "zeta").set(PartStyleOption.FONT_SIZE, "1rem"));
        PartStyle alpha = service.save(new PartStyle(null, "alpha").set(PartStyleOption.FONT_SIZE, "1rem"));
        service.setDefault("book");
        List<String> chosen = new ArrayList<>();

        Manuscript book = new Manuscript();
        List<Choice> choices = PartStyleApplier.choices(loc, service, book, chosen::add);

        assertThat(choices).extracting(Choice::text).containsExactly("Default (Book)", "alpha", "Book", "Normal", "zeta");
        assertThat(choices).extracting(Choice::checked).containsExactly(true, false, false, false, false);

        book.setPartStyle(alpha.getId());
        choices = PartStyleApplier.choices(loc, service, book, chosen::add);
        assertThat(choices).extracting(Choice::checked).containsExactly(false, true, false, false, false);

        choices.get(0).action().run();
        choices.get(2).action().run();
        assertThat(chosen).containsExactly(null, "book");
    }
}
