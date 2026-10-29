package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting.Origin;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A text setting of a book: the book, else the nearest folder above it, else the user settings, else the default.
 */
class SettingsInheritanceTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    private Folder scifi;
    private Folder expanse;

    @BeforeEach
    void createFolders() throws Exception {
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
    }

    private Manuscript book(Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName("book");
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private Folder change(Folder folder, java.util.function.Consumer<Folder> change) throws Exception {
        Folder stored = folderService.find(folder.getId());
        change.accept(stored);
        return folderService.save(stored);
    }

    @Test
    void aBookWithoutAnythingUsesTheDefault() throws Exception {
        EffectiveSetting<String> style = manuscriptService.resolve(book(null), PromptField.STYLE);

        assertThat(style.value()).isEqualTo(Defaults.DEFAULT_STYLE);
        assertThat(style.origin()).isEqualTo(Origin.DEFAULT);
        assertThat(style.folder()).isNull();
        assertThat(style.isCustom()).isFalse();
    }

    @Test
    void theUserSettingsComeBeforeTheDefault() throws Exception {
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setDefaultStyle("the style of the user");
        settingService.save(settings);

        EffectiveSetting<String> style = manuscriptService.resolve(book(expanse), PromptField.STYLE);

        assertThat(style.value()).isEqualTo("the style of the user");
        assertThat(style.origin()).isEqualTo(Origin.USER);
    }

    @Test
    void aFolderSettingIsInheritedByTheBooksBelowIt() throws Exception {
        change(scifi, folder -> folder.setStyle("hard science fiction"));
        Manuscript direct = book(scifi);
        Manuscript nested = book(expanse);

        EffectiveSetting<String> inDirect = manuscriptService.resolve(direct, PromptField.STYLE);
        EffectiveSetting<String> inNested = manuscriptService.resolve(nested, PromptField.STYLE);

        assertThat(inDirect.value()).isEqualTo("hard science fiction");
        assertThat(inDirect.origin()).isEqualTo(Origin.FOLDER);
        assertThat(inDirect.folder().getName()).isEqualTo("Sci-fi");
        assertThat(inDirect.isCustom()).isTrue();
        assertThat(inNested.value()).isEqualTo("hard science fiction");
        assertThat(inNested.folder().getName()).isEqualTo("Sci-fi");
        assertThat(manuscriptService.getStyle(nested)).isEqualTo("hard science fiction");
    }

    @Test
    void theNearestFolderWinsAndABlankOneIsSkipped() throws Exception {
        change(scifi, folder -> {
            folder.setStyle("hard science fiction");
            folder.setPov("First person");
        });
        change(expanse, folder -> {
            folder.setStyle("gritty");
            folder.setPov("   ");
        });
        Manuscript nested = book(expanse);

        assertThat(manuscriptService.resolve(nested, PromptField.STYLE).folder().getName()).isEqualTo("Expanse");
        assertThat(manuscriptService.getStyle(nested)).isEqualTo("gritty");
        assertThat(manuscriptService.resolve(nested, PromptField.POV).folder().getName()).isEqualTo("Sci-fi");
        assertThat(manuscriptService.getPov(nested)).isEqualTo("First person");
        // not set anywhere
        assertThat(manuscriptService.getTense(nested)).isEqualTo(Defaults.DEFAULT_TENSE);
    }

    @Test
    void theBookWinsOverEveryFolder() throws Exception {
        change(scifi, folder -> folder.setStyle("hard science fiction"));
        change(expanse, folder -> folder.setStyle("gritty"));
        Manuscript nested = book(expanse);
        nested.setStyle("the style of the book");
        nested = manuscriptService.save(nested);

        EffectiveSetting<String> style = manuscriptService.resolve(nested, PromptField.STYLE);

        assertThat(style.value()).isEqualTo("the style of the book");
        assertThat(style.origin()).isEqualTo(Origin.BOOK);
    }

    @Test
    void aChangeOfAFolderOrOfTheFolderOfTheBookIsSeenAtOnce() throws Exception {
        Manuscript nested = book(expanse);
        assertThat(manuscriptService.getStyle(nested)).isEqualTo(Defaults.DEFAULT_STYLE);

        change(scifi, folder -> folder.setStyle("hard science fiction"));
        assertThat(manuscriptService.getStyle(nested)).isEqualTo("hard science fiction");

        manuscriptService.moveToFolder(List.of(nested), null);
        Manuscript moved = manuscriptService.find(nested.getId());
        assertThat(manuscriptService.getStyle(moved)).isEqualTo(Defaults.DEFAULT_STYLE);
    }

    @Test
    void aDeletedFolderIsNotInherited() throws Exception {
        change(expanse, folder -> folder.setStyle("gritty"));
        Manuscript nested = book(expanse);
        // the book is left pointing at it (a cleanup race), the settings of a deleted folder are not applied
        folderService.delete(expanse, false);

        assertThat(manuscriptService.getStyle(manuscriptService.find(nested.getId()))).isEqualTo(Defaults.DEFAULT_STYLE);
    }

    @Test
    void everyFieldIsResolvedThroughTheChain() throws Exception {
        change(scifi, folder -> {
            folder.setTemplate("t");
            folder.setPov("p");
            folder.setTense("te");
            folder.setStyle("s");
            folder.setUserPrompt("u");
            folder.setSummaryPrompt("su");
            folder.setMetaSummaryPrompt("m");
            folder.setRedraftSystemPrompt("rs");
            folder.setRedraftPrompt("r");
            folder.setAnnotateSystemPrompt("as");
            folder.setAnnotatePrompt("a");
            folder.setImpersonatePrompt("i");
        });
        Manuscript nested = book(expanse);

        assertThat(manuscriptService.getMasterTemplate(nested)).isEqualTo("t");
        assertThat(manuscriptService.getPov(nested)).isEqualTo("p");
        assertThat(manuscriptService.getTense(nested)).isEqualTo("te");
        assertThat(manuscriptService.getStyle(nested)).isEqualTo("s");
        assertThat(manuscriptService.getUserPrompt(nested)).isEqualTo("u");
        assertThat(manuscriptService.getSummaryPrompt(nested)).isEqualTo("su");
        assertThat(manuscriptService.getMetaSummaryPrompt(nested)).isEqualTo("m");
        assertThat(manuscriptService.getRedraftSystemPrompt(nested)).isEqualTo("rs");
        assertThat(manuscriptService.getRedraftPrompt(nested)).isEqualTo("r");
        assertThat(manuscriptService.getAnnotateSystemPrompt(nested)).isEqualTo("as");
        assertThat(manuscriptService.getAnnotatePrompt(nested)).isEqualTo("a");
        assertThat(manuscriptService.getImpersonatePrompt(nested)).isEqualTo("i");
        for (PromptField field : PromptField.values()) {
            assertThat(manuscriptService.resolve(nested, field).origin()).as(field.name()).isEqualTo(Origin.FOLDER);
        }
    }

    @Test
    void languageFollowsTheSameChain() throws Exception {
        Manuscript plain = book(null);
        assertThat(manuscriptService.resolveLanguage(plain).origin()).isEqualTo(Origin.DEFAULT);
        assertThat(manuscriptService.getLanguage(plain)).isEqualTo(Language.ENGLISH);

        change(scifi, folder -> folder.setLanguage(Language.JAPANESE));
        Manuscript nested = book(expanse);
        assertThat(manuscriptService.getLanguage(nested)).isEqualTo(Language.JAPANESE);
        assertThat(manuscriptService.resolveLanguage(nested).folder().getName()).isEqualTo("Sci-fi");
        assertThat(nested.getOwnLanguage()).isNull();

        nested.setLanguage(Language.ENGLISH);
        nested = manuscriptService.save(nested);
        assertThat(manuscriptService.resolveLanguage(nested).origin()).isEqualTo(Origin.BOOK);
        assertThat(manuscriptService.getLanguage(nested)).isEqualTo(Language.ENGLISH);
    }
}
