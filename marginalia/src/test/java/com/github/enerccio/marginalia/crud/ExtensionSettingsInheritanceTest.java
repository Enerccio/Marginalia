package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting.Origin;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A setting an extension adds to the books: the book, else the nearest folder above it, else the user level of the extension, else
 * the default of the extension ({@link InheritedSetting}); the values live in the attributes of the book and the folders.
 */
class ExtensionSettingsInheritanceTest extends MarginaliaTestBase {

    private static final String OWNER = "test-extension";
    private static final String MEDIUM = "test.medium";

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private InheritedSettingRegistry registry;

    private final AtomicReference<String> userLevel = new AtomicReference<>();

    private Folder scifi;
    private Folder expanse;

    @BeforeEach
    void register() throws Exception {
        login();
        userLevel.set(null);
        registry.register(OWNER, InheritedSetting.choice(MEDIUM, () -> "Medium",
                        () -> List.of(new InheritedSetting.Choice("book", "Book"), new InheritedSetting.Choice("movie", "Movie")))
                .group(() -> "Test extension").userValue(userLevel::get).fallback(() -> "book"));
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
    }

    @AfterEach
    void unregister() {
        registry.unregisterAll(OWNER);
    }

    private Manuscript book(Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName("book");
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private Folder set(Folder folder, String key, String value) throws Exception {
        return folderService.update(folder, new FolderService.Details(folder.getName(), null, null, Map.of(), null, Map.of(key, value)));
    }

    @Test
    void aBookWithoutAnythingUsesTheDefaultOfTheExtension() throws Exception {
        EffectiveSetting<String> medium = manuscriptService.resolveExtension(book(null), MEDIUM);

        assertThat(medium.value()).isEqualTo("book");
        assertThat(medium.origin()).isEqualTo(Origin.DEFAULT);
        assertThat(medium.folder()).isNull();
    }

    @Test
    void theUserLevelOfTheExtensionComesBeforeTheDefault() throws Exception {
        userLevel.set("movie");

        EffectiveSetting<String> medium = manuscriptService.resolveExtension(book(expanse), MEDIUM);

        assertThat(medium.value()).isEqualTo("movie");
        assertThat(medium.origin()).isEqualTo(Origin.USER);
    }

    @Test
    void aFolderSettingIsInheritedAndTheNearestFolderWins() throws Exception {
        scifi = set(scifi, MEDIUM, "movie");
        Manuscript nested = book(expanse);

        EffectiveSetting<String> inherited = manuscriptService.resolveExtension(nested, MEDIUM);
        assertThat(inherited.value()).isEqualTo("movie");
        assertThat(inherited.origin()).isEqualTo(Origin.FOLDER);
        assertThat(inherited.folder().getName()).isEqualTo("Sci-fi");

        expanse = set(expanse, MEDIUM, "book");
        EffectiveSetting<String> nearest = manuscriptService.resolveExtension(nested, MEDIUM);
        assertThat(nearest.value()).isEqualTo("book");
        assertThat(nearest.folder().getName()).isEqualTo("Expanse");
    }

    @Test
    void theBookComesBeforeTheFolders() throws Exception {
        set(scifi, MEDIUM, "movie");
        Manuscript book = book(scifi);
        InheritedSettingValues.set(book, MEDIUM, "book");
        book = manuscriptService.save(book);

        EffectiveSetting<String> medium = manuscriptService.resolveExtension(manuscriptService.find(book.getId()), MEDIUM);

        assertThat(medium.value()).isEqualTo("book");
        assertThat(medium.origin()).isEqualTo(Origin.BOOK);
        assertThat(medium.isCustom()).isTrue();
    }

    @Test
    void whatAFolderWouldGiveItsBooksIsTheInheritedValue() throws Exception {
        set(scifi, MEDIUM, "movie");

        // what the settings dialog shows as the placeholder of an empty field
        assertThat(manuscriptService.resolveExtensionInherited(expanse, MEDIUM).value()).isEqualTo("movie");
        assertThat(manuscriptService.resolveExtensionInherited(null, MEDIUM).origin()).isEqualTo(Origin.DEFAULT);
    }

    @Test
    void aBlankValueClearsTheSettingAndAMissingKeyLeavesItAlone() throws Exception {
        scifi = set(scifi, MEDIUM, "movie");
        scifi = set(scifi, "test.other", "x");

        scifi = folderService.update(scifi, new FolderService.Details("Sci-fi", null, null, Map.of(), null, Map.of("test.other", "  ")));

        Folder stored = folderService.find(scifi.getId());
        assertThat(InheritedSettingValues.get(stored, MEDIUM)).isEqualTo("movie");
        assertThat(InheritedSettingValues.get(stored, "test.other")).isNull();
        assertThat(InheritedSettingValues.all(stored)).containsExactly(Map.entry(MEDIUM, "movie"));
    }

    @Test
    void theValuesStayWhenTheExtensionIsNotLoaded() throws Exception {
        set(scifi, MEDIUM, "movie");
        Manuscript book = book(expanse);

        registry.unregisterAll(OWNER);

        // nothing is registered, so no user level and no default, but the folder's value is still there
        assertThat(registry.find(MEDIUM)).isNull();
        EffectiveSetting<String> medium = manuscriptService.resolveExtension(book, MEDIUM);
        assertThat(medium.value()).isEqualTo("movie");
        assertThat(manuscriptService.resolveExtension(book(null), MEDIUM).value()).isEmpty();
    }

    @Test
    void theRegistryGroupsTheSettingsOfAnExtensionUnderOneTitle() {
        registry.register(OWNER, InheritedSetting.text("test.second", () -> "Second"));
        registry.register("another-extension", InheritedSetting.textArea("another.prompt", () -> "Prompt"));

        List<InheritedSettingRegistry.Group> groups = registry.groups();

        assertThat(groups).extracting(InheritedSettingRegistry.Group::owner).containsExactly(OWNER, "another-extension");
        // the first setting that names a group names it, an extension that names none is shown by its own name
        assertThat(groups.get(0).title()).isEqualTo("Test extension");
        assertThat(groups.get(0).settings()).extracting(InheritedSetting::getKey).containsExactly(MEDIUM, "test.second");
        assertThat(groups.get(1).title()).isEqualTo("another-extension");

        registry.unregisterAll("another-extension");
        assertThat(registry.groups()).hasSize(1);
    }

    @Test
    void aSupplierThatFailsDoesNotBreakTheResolution() throws Exception {
        registry.register(OWNER, InheritedSetting.text("test.broken", () -> "Broken")
                .userValue(() -> {
                    throw new IllegalStateException("no user");
                }).fallback(() -> "fine"));

        EffectiveSetting<String> broken = manuscriptService.resolveExtension(book(null), "test.broken");

        assertThat(broken.value()).isEqualTo("fine");
        assertThat(broken.origin()).isEqualTo(Origin.DEFAULT);
    }
}
