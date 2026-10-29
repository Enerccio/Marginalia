package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria.TagMatch;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The advanced search dialog of the book list: what the fields ask for, and the books it lists.
 */
class ManuscriptSearchDialogTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private FolderService folderService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private final List<Manuscript> opened = new ArrayList<>();
    private ManuscriptSearchDialog dialog;
    private String suffix;
    private Manuscript noir, plain;
    private Tag tag;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        suffix = uniqueName("");
        noir = book("Noir " + suffix, "A storm over the pier");
        plain = book("Plain " + suffix, "Quiet morning");
        tag = tagService.getOrCreateForUser("dark-" + suffix);
        tagRelationService.createRelation(tag, noir);
        dialog = new ManuscriptSearchDialog(opened::add);
        dialog.name.setValue(suffix);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private Manuscript book(String name, String text) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book = manuscriptService.save(book);
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        ChatMessage root = chatMessageService.createRoot(book, message);
        book.setActiveLeaf(root);
        return manuscriptService.save(book);
    }

    @Test
    void emptyFieldsAskForNothing() {
        ManuscriptSearchCriteria criteria = new ManuscriptSearchDialog(opened::add).criteria();

        assertThat(criteria.getName()).isNull();
        assertThat(criteria.getIncludeTags()).isEmpty();
        assertThat(criteria.getIncludeMatch()).isEqualTo(TagMatch.ALL);
        assertThat(criteria.getExcludeTags()).isEmpty();
        assertThat(criteria.getExcludeMatch()).isEqualTo(TagMatch.ANY);
        assertThat(criteria.getCreatedFrom()).isNull();
        assertThat(criteria.getCreatedTo()).isNull();
        assertThat(criteria.getMinParts()).isNull();
        assertThat(criteria.getFulltext()).isNull();
        assertThat(criteria.isAllBranches()).isFalse();
    }

    @Test
    void theDayOfAToDateIsIncluded() {
        LocalDate day = LocalDate.of(2026, 3, 14);
        dialog.createdTo.setValue(day);

        assertThat(dialog.criteria().getCreatedTo().toInstant())
                .isEqualTo(day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void listsTheBooksFoundWithoutAnythingBeforeTheSearch() {
        assertThat(dialog.resultNames()).isEmpty();

        dialog.search();

        assertThat(dialog.resultNames()).containsExactlyInAnyOrder(noir.getName(), plain.getName());
    }

    @Test
    void aSearchInAFolderFindsItsBooksOnly() throws Exception {
        Folder scifi = folderService.create("Sci-fi " + suffix, null);
        Folder expanse = folderService.create("Expanse", scifi);
        manuscriptService.moveToFolder(List.of(noir), scifi);
        manuscriptService.moveToFolder(List.of(plain), expanse);

        ManuscriptSearchDialog inFolder = new ManuscriptSearchDialog(scifi, opened::add, opened::add);
        inFolder.name.setValue(suffix);
        inFolder.search();
        assertThat(inFolder.resultNames()).containsExactly(noir.getName());

        inFolder.subfolders.setValue(true);
        inFolder.search();
        assertThat(inFolder.resultNames()).containsExactlyInAnyOrder(noir.getName(), plain.getName());

        ManuscriptSearchDialog top = new ManuscriptSearchDialog(null, null, opened::add);
        top.name.setValue(suffix);
        top.search();
        assertThat(top.resultNames()).isEmpty();
    }

    @Test
    void searchesByTagsAndText() {
        dialog.includeTags.setValue(Set.of(tag));
        dialog.search();
        assertThat(dialog.resultNames()).containsExactly(noir.getName());

        dialog.includeTags.clear();
        dialog.excludeTags.setValue(Set.of(tag));
        dialog.search();
        assertThat(dialog.resultNames()).containsExactly(plain.getName());

        dialog.excludeTags.clear();
        dialog.fulltext.setValue("storm");
        dialog.search();
        assertThat(dialog.resultNames()).containsExactly(noir.getName());

        dialog.fulltext.setValue("nothing like this");
        dialog.search();
        assertThat(dialog.resultNames()).isEmpty();
    }
}
