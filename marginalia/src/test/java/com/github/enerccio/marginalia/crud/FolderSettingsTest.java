package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting.Origin;
import com.github.enerccio.marginalia.domain.service.FolderBookLockedException;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Details;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the settings dialog of a folder relies on: writing the details of a folder, where an empty setting is inherited
 * from, and the lock of a book an agent works on.
 */
class FolderSettingsTest extends AgentTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private SettingService settingService;

    private Folder scifi;
    private Folder expanse;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
    }

    private Details details(String name, String description, Map<PromptField, String> prompts) {
        return new Details(name, description, null, prompts);
    }

    private Details withLanguage(Folder folder, Language language) throws Exception {
        Folder saved = stored(folder);
        return new Details(saved.getName(), saved.getDescription(), language, Map.of());
    }

    private Folder stored(Folder folder) throws Exception {
        return folderService.find(folder.getId());
    }

    @Test
    void theDetailsAreWrittenAndABlankSettingIsInherited() throws Exception {
        folderService.update(scifi, details("Science fiction", "Space", Map.of(PromptField.STYLE, "Terse", PromptField.POV, "First person")));

        Folder saved = stored(scifi);
        assertThat(saved.getName()).isEqualTo("Science fiction");
        assertThat(saved.getDescription()).isEqualTo("Space");
        assertThat(saved.getStyle()).isEqualTo("Terse");
        assertThat(saved.getPov()).isEqualTo("First person");

        folderService.update(saved, details("Science fiction", "Space", Map.of(PromptField.STYLE, "  ")));

        assertThat(stored(scifi).getStyle()).as("a blank text clears the setting").isNull();
        assertThat(stored(scifi).getPov()).as("a field missing from the map is left as it is").isEqualTo("First person");
    }

    @Test
    void theSavedSettingsReachTheBooksBelow() throws Exception {
        Manuscript book = new Manuscript();
        book.setName("Leviathan");
        book.setFolder(expanse);
        book = manuscriptService.save(book);

        folderService.update(scifi, details("Sci-fi", null, Map.of(PromptField.TENSE, "Present")));

        assertThat(manuscriptService.getTense(book)).isEqualTo("Present");
        assertThat(manuscriptService.resolve(book, PromptField.TENSE).origin()).isEqualTo(Origin.FOLDER);
    }

    @Test
    void aNameThatIsTakenIsRefusedAndNothingIsWritten() throws Exception {
        folderService.create("Fantasy", null);

        assertThatThrownBy(() -> folderService.update(scifi, details("fantasy", "x", Map.of(PromptField.STYLE, "Terse"))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stored(scifi).getName()).isEqualTo("Sci-fi");
        assertThat(stored(scifi).getStyle()).isNull();
    }

    @Test
    void aFolderThatIsGoneGivesNull() throws Exception {
        folderService.delete(scifi, false);

        assertThat(folderService.update(scifi, details("Sci-fi", null, Map.of()))).isNull();
    }

    @Test
    void anEmptySettingIsInheritedFromTheNearestFolderThenTheUserThenTheDefault() throws Exception {
        assertThat(manuscriptService.resolveInherited(expanse, PromptField.STYLE).origin()).isEqualTo(Origin.DEFAULT);

        UserSetting user = settingService.getOrCreate(UserSetting.class);
        user.setDefaultStyle("From the user");
        settingService.save(user);
        assertThat(manuscriptService.resolveInherited(expanse, PromptField.STYLE).origin()).isEqualTo(Origin.USER);

        folderService.update(scifi, details("Sci-fi", null, Map.of(PromptField.STYLE, "From sci-fi")));
        var fromAbove = manuscriptService.resolveInherited(stored(expanse).getParent(), PromptField.STYLE);
        assertThat(fromAbove.origin()).isEqualTo(Origin.FOLDER);
        assertThat(fromAbove.folder().getName()).isEqualTo("Sci-fi");
        assertThat(fromAbove.value()).isEqualTo("From sci-fi");

        folderService.update(expanse, details("Expanse", null, Map.of(PromptField.STYLE, "From the expanse")));
        assertThat(manuscriptService.resolveInherited(expanse, PromptField.STYLE).value()).isEqualTo("From the expanse");
        assertThat(manuscriptService.resolveInherited(null, PromptField.STYLE).origin()).as("the top level").isEqualTo(Origin.USER);
    }

    @Test
    void theLanguageIsInheritedFromTheNearestFolderThenEnglishAndABookCanSetItsOwn() throws Exception {
        Manuscript book = new Manuscript();
        book.setName("Leviathan");
        book.setFolder(expanse);
        book = manuscriptService.save(book);
        assertThat(manuscriptService.resolveLanguage(book).origin()).isEqualTo(Origin.DEFAULT);
        assertThat(manuscriptService.getLanguage(book)).isEqualTo(Language.ENGLISH);

        folderService.update(scifi, withLanguage(scifi, Language.GERMAN));
        assertThat(manuscriptService.getLanguage(book)).isEqualTo(Language.GERMAN);
        assertThat(manuscriptService.resolveLanguage(book).folder().getName()).isEqualTo("Sci-fi");
        assertThat(manuscriptService.resolveInheritedLanguage(expanse).value()).isEqualTo(Language.GERMAN);
        assertThat(manuscriptService.resolveInheritedLanguage(null).origin()).as("the top level").isEqualTo(Origin.DEFAULT);

        folderService.update(expanse, withLanguage(expanse, Language.CZECH));
        assertThat(manuscriptService.getLanguage(book)).as("the nearest folder wins").isEqualTo(Language.CZECH);

        book = manuscriptService.find(book.getId());
        book.setLanguage(Language.JAPANESE);
        book = manuscriptService.save(book);
        assertThat(manuscriptService.resolveLanguage(book).origin()).isEqualTo(Origin.BOOK);
        assertThat(manuscriptService.getLanguage(book)).isEqualTo(Language.JAPANESE);

        book.setLanguage(null);
        manuscriptService.save(book);
        book = manuscriptService.find(book.getId());
        assertThat(manuscriptService.getLanguage(book)).as("cleared, the book inherits again").isEqualTo(Language.CZECH);

        folderService.update(expanse, withLanguage(expanse, null));
        assertThat(manuscriptService.getLanguage(book)).isEqualTo(Language.GERMAN);
    }

    @Test
    void aLanguageIsRefusedWhileAnAgentWorksOnABookBelow() throws Exception {
        Manuscript book = new Manuscript();
        book.setName("Leviathan");
        book.setFolder(expanse);
        book = manuscriptService.save(book);
        agentRunService.start(agentService.save(newAgent(book)));

        assertThatThrownBy(() -> folderService.update(scifi, withLanguage(scifi, Language.GERMAN)))
                .isInstanceOf(FolderBookLockedException.class);
        assertThat(stored(scifi).getLanguage()).isNull();
    }

    @Test
    void aSettingIsRefusedWhileAnAgentWorksOnABookBelowButANameIsNot() throws Exception {
        Manuscript book = new Manuscript();
        book.setName("Leviathan");
        book.setFolder(expanse);
        book = manuscriptService.save(book);
        Agent agent = agentService.save(newAgent(book));
        agentRunService.start(agent);

        assertThatThrownBy(() -> folderService.update(scifi, details("Sci-fi", null, Map.of(PromptField.STYLE, "Terse"))))
                .isInstanceOfSatisfying(FolderBookLockedException.class, e -> {
                    assertThat(e.getBookName()).isEqualTo("Leviathan");
                    assertThat(e.getAgentName()).isEqualTo(agent.getName());
                });
        assertThat(stored(scifi).getStyle()).isNull();

        folderService.update(scifi, details("Science fiction", "Renamed while locked", Map.of(PromptField.STYLE, "")));

        assertThat(stored(scifi).getName()).isEqualTo("Science fiction");
        assertThat(stored(scifi).getDescription()).isEqualTo("Renamed while locked");
    }
}
