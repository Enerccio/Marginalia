package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The text styles of a user: their own styles next to the built in ones, the default style, and which style a book shows.
 */
class PartStyleServiceTest extends MarginaliaTestBase {

    @Autowired
    private PartStyleService service;

    @BeforeEach
    void user() throws Exception {
        login();
    }

    private PartStyle created(String name) throws Exception {
        return service.save(new PartStyle(null, name).set(PartStyleOption.FONT_SIZE, "1.2rem"));
    }

    @Test
    void aUserWithoutStylesHasTheBuiltInOnes() throws Exception {
        assertThat(service.list()).extracting(PartStyle::getId).containsExactly("normal", "book");
        assertThat(service.getDefault().getId()).isEqualTo("normal");
        assertThat(service.getDefaultId()).isNull();
    }

    @Test
    void aSavedStyleIsListedSortedByNameAfterTheBuiltInOnes() throws Exception {
        created("zebra");
        created("Apple");

        assertThat(service.list()).extracting(PartStyle::getName).containsExactly("Normal", "Book", "Apple", "zebra");
    }

    @Test
    void saveGivesAnIdAndKeepsTheOptions() throws Exception {
        PartStyle saved = service.save(new PartStyle(null, "  Mine ")
                .set(PartStyleOption.FONT_FAMILY, "serif").set(PartStyleOption.HYPHENATION, "true"));
        saved.setCustomCss("p { color: red; }");
        service.save(saved);

        PartStyle found = service.find(saved.getId());

        assertThat(saved.getId()).isNotBlank();
        assertThat(found.getName()).isEqualTo("Mine");
        assertThat(found.get(PartStyleOption.FONT_FAMILY)).isEqualTo("serif");
        assertThat(found.getBoolean(PartStyleOption.HYPHENATION)).isTrue();
        assertThat(found.getCustomCss()).isEqualTo("p { color: red; }");
        assertThat(found.isBuiltin()).isFalse();
    }

    @Test
    void savingWithAnIdReplacesTheStyle() throws Exception {
        PartStyle saved = created("Mine");
        saved.setName("Renamed");
        saved.set(PartStyleOption.FONT_SIZE, null);

        service.save(saved);

        assertThat(service.list()).hasSize(3);
        assertThat(service.find(saved.getId()).getName()).isEqualTo("Renamed");
        assertThat(service.find(saved.getId()).get(PartStyleOption.FONT_SIZE)).isNull();
    }

    @Test
    void aBuiltInStyleCanNotBeChangedOrDeleted() throws Exception {
        PartStyle book = service.find("book");

        assertThatThrownBy(() -> service.save(book)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.save(new PartStyle("book", "Mine"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.delete("book")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.delete("normal")).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.list()).hasSize(2);
    }

    @Test
    void aStyleNeedsANameAndBalancedCss() {
        assertThatThrownBy(() -> service.save(new PartStyle(null, "  "))).isInstanceOf(IllegalArgumentException.class);
        PartStyle unbalanced = new PartStyle(null, "Broken");
        unbalanced.setCustomCss("p { color: red; } }");
        assertThatThrownBy(() -> service.save(unbalanced)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void duplicateCopiesABuiltInStyleIntoAStyleOfTheUser() throws Exception {
        PartStyle copy = service.duplicate("book", "My book");

        assertThat(copy.isBuiltin()).isFalse();
        assertThat(copy.getId()).isNotEqualTo("book");
        assertThat(service.find(copy.getId()).get(PartStyleOption.FONT_SIZE)).isEqualTo("1.04rem");
        assertThat(service.find(copy.getId()).toCss()).contains("font-size: 1.04rem;");

        copy.set(PartStyleOption.FONT_SIZE, "2rem");
        service.save(copy);
        assertThat(service.find("book").get(PartStyleOption.FONT_SIZE)).isEqualTo("1.04rem");
    }

    @Test
    void deleteRemovesTheStyleAndTheDefaultThatPointedToIt() throws Exception {
        PartStyle mine = created("Mine");
        service.setDefault(mine.getId());
        assertThat(service.getDefault().getId()).isEqualTo(mine.getId());

        service.delete(mine.getId());

        assertThat(service.find(mine.getId())).isNull();
        assertThat(service.getDefaultId()).isNull();
        assertThat(service.getDefault().getId()).isEqualTo("normal");
        assertThatThrownBy(() -> service.delete(mine.getId())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theDefaultCanBeABuiltInStyleAndUnknownOnesAreRefused() throws Exception {
        service.setDefault("book");
        assertThat(service.getDefault().getId()).isEqualTo("book");

        assertThatThrownBy(() -> service.setDefault("no-such-style")).isInstanceOf(IllegalArgumentException.class);

        service.setDefault(null);
        assertThat(service.getDefaultId()).isNull();
    }

    @Test
    void aBookShowsItsOwnStyleElseTheDefaultElseTheNormalLook() throws Exception {
        PartStyle mine = created("Mine");
        Manuscript book = new Manuscript();

        assertThat(service.resolve(book).getId()).isEqualTo("normal");
        assertThat(service.findChosen(book)).isNull();

        service.setDefault("book");
        assertThat(service.resolve(book).getId()).isEqualTo("book");
        assertThat(service.findChosen(book)).isNull();

        book.setPartStyle(mine.getId());
        assertThat(service.resolve(book).getId()).isEqualTo(mine.getId());
        assertThat(service.findChosen(book).getId()).isEqualTo(mine.getId());

        service.delete(mine.getId());
        assertThat(service.resolve(book).getId()).as("a deleted style falls back to the default").isEqualTo("book");
    }

    @Test
    void aBookWithTheOldBookStyleSwitchStillShowsTheBookStyle() throws Exception {
        Manuscript book = new Manuscript();
        book.setShowBookStyles(true);

        assertThat(service.resolve(book).getId()).isEqualTo(PartStyleRegistry.BOOK);

        PartStyle mine = created("Mine");
        book.setPartStyle(mine.getId());
        assertThat(service.resolve(book).getId()).as("a choice wins over the old switch").isEqualTo(mine.getId());
    }

    @Test
    void theStylesBelongToTheirUser() throws Exception {
        PartStyle mine = created("Mine");
        service.setDefault(mine.getId());

        login();

        assertThat(service.list()).extracting(PartStyle::getName).containsExactly("Normal", "Book");
        assertThat(service.find(mine.getId())).isNull();
        assertThat(service.getDefaultId()).isNull();
    }
}
