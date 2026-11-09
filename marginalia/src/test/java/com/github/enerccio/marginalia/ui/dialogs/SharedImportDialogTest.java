package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.github.enerccio.marginalia.ui.components.LorebookPreview;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Import from other user": the window lists what the others share (not the user's own), says so when there is nothing, and
 * its Import button makes the copy.
 */
class SharedImportDialogTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private User author;
    private User reader;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        author = createUser();
        reader = createUser();
        loginAs(author);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    private Lorebook sharedLorebook(String name) throws Exception {
        Lorebook book = new Lorebook();
        book.setName(name);
        book.setDescription("about " + name);
        book.setShared(true);
        return lorebookService.save(book);
    }

    private Protocol sharedProtocol(String name) throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setName(name);
        protocol.setShared(true);
        return protocolService.save(protocol);
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(SharedImportDialogTest::all));
    }

    @SuppressWarnings("unchecked")
    private static <T> Grid<T> grid(Component dialog) {
        return (Grid<T>) all(dialog).filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    private static <T> List<T> items(Component dialog) {
        Grid<T> grid = grid(dialog);
        return grid.getGenericDataView().getItems().toList();
    }

    private boolean saysNothingIsShared(Component dialog, L text) {
        return all(dialog).filter(Span.class::isInstance).map(Span.class::cast)
                .anyMatch(span -> span.isVisible() && loc.getValue(text).equals(span.getText()));
    }

    /** The button of the last column (Import) of the row of the item. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Button importButton(Component dialog, Object item) {
        Grid grid = grid(dialog);
        List<Grid.Column> columns = new ArrayList<>(grid.getColumns());
        ComponentRenderer renderer = (ComponentRenderer) columns.getLast().getRenderer();
        return (Button) renderer.createComponent(item);
    }

    @Test
    void listsWhatOthersShareAndNotTheUsersOwn() throws Exception {
        Lorebook shared = sharedLorebook(uniqueName("Shared"));
        loginAs(reader);
        Lorebook own = sharedLorebook(uniqueName("Own"));

        SharedImportDialog<Lorebook, Lorebook> dialog = SharedImportDialog.forLorebooks(loc, lorebookService, copy -> {
        });

        List<Lorebook> listed = items(dialog);
        assertThat(listed).extracting(Lorebook::getUuid).contains(shared.getUuid()).doesNotContain(own.getUuid());
        assertThat(grid(dialog).isVisible()).isTrue();
        assertThat(saysNothingIsShared(dialog, L.MSG_NO_SHARED_LOREBOOKS)).isFalse();
        assertThat(dialog.authorOf(listed.stream().filter(l -> l.getUuid().equals(shared.getUuid())).findFirst().orElseThrow()))
                .isEqualTo(author.getFullName() == null || author.getFullName().isBlank() ? author.getLogin() : author.getFullName());
    }

    @Test
    void saysSoWhenNobodySharesAnything() throws Exception {
        loginAs(reader);

        SharedImportDialog<Lorebook, Lorebook> lorebooks = SharedImportDialog.forLorebooks(loc, lorebookService, copy -> {
        });
        SharedImportDialog<Protocol, Protocol> protocols = SharedImportDialog.forProtocols(loc, protocolService, copy -> {
        });

        assertThat(grid(lorebooks).isVisible()).isFalse();
        assertThat(saysNothingIsShared(lorebooks, L.MSG_NO_SHARED_LOREBOOKS)).isTrue();
        assertThat(grid(protocols).isVisible()).isFalse();
        assertThat(saysNothingIsShared(protocols, L.MSG_NO_SHARED_PROTOCOLS)).isTrue();
    }

    @Test
    void theImportButtonMakesTheCopyAndHandsItOver() throws Exception {
        Lorebook shared = sharedLorebook(uniqueName("Shared"));
        loginAs(reader);
        List<Lorebook> imported = new ArrayList<>();
        SharedImportDialog<Lorebook, Lorebook> dialog = SharedImportDialog.forLorebooks(loc, lorebookService, imported::add);
        Lorebook listed = this.<Lorebook>items(dialog).stream().filter(l -> l.getUuid().equals(shared.getUuid())).findFirst().orElseThrow();

        importButton(dialog, listed).click();

        assertThat(imported).hasSize(1);
        assertThat(imported.getFirst().getName()).isEqualTo(shared.getName());
        assertThat(imported.getFirst().getOwner().getId()).isEqualTo(reader.getId());
        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getUuid).containsExactly(imported.getFirst().getUuid());
    }

    @Test
    void aLorebookThatIsNotSharedAnyMoreIsNotImported() throws Exception {
        Lorebook shared = sharedLorebook(uniqueName("Shared"));
        loginAs(reader);
        List<Lorebook> imported = new ArrayList<>();
        SharedImportDialog<Lorebook, Lorebook> dialog = SharedImportDialog.forLorebooks(loc, lorebookService, imported::add);
        Lorebook listed = this.<Lorebook>items(dialog).stream().filter(l -> l.getUuid().equals(shared.getUuid())).findFirst().orElseThrow();

        loginAs(author);
        shared.setShared(false);
        lorebookService.save(shared);
        loginAs(reader);
        importButton(dialog, listed).click();

        assertThat(imported).isEmpty();
        assertThat(lorebookService.findAllRegularForUser()).isEmpty();
        assertThat(this.<Lorebook>items(dialog)).extracting(Lorebook::getUuid).doesNotContain(shared.getUuid());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void theOpenedRowShowsOnlyTheSubLorebooksThatAreShared() throws Exception {
        Lorebook root = sharedLorebook(uniqueName("World"));
        Lorebook shared = lorebookService.createSubbook(root, "Shared sub");
        shared.setShared(true);
        lorebookService.save(shared);
        lorebookService.createSubbook(root, "Hidden sub");
        loginAs(reader);
        SharedImportDialog<Lorebook, Lorebook> dialog = SharedImportDialog.forLorebooks(loc, lorebookService, copy -> {
        });
        Lorebook listed = this.<Lorebook>items(dialog).stream().filter(l -> l.getUuid().equals(root.getUuid())).findFirst().orElseThrow();

        Component details = dialog.createDetails(listed);
        LorebookPreview preview = all(details).filter(LorebookPreview.class::isInstance).map(LorebookPreview.class::cast).findFirst().orElseThrow();
        all(preview).filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow().setValue(true);
        preview.refresh(listed);

        TreeGrid<?> tree = all(preview).filter(TreeGrid.class::isInstance).map(TreeGrid.class::cast).findFirst().orElseThrow();
        // one top level row for each lorebook that is shown
        assertThat(((TreeDataProvider<?>) (Object) tree.getDataProvider()).getTreeData().getRootItems()).hasSize(2);
    }

    @Test
    void protocolsAreImportedTheSameWay() throws Exception {
        Protocol shared = sharedProtocol(uniqueName("Precise"));
        loginAs(reader);
        List<Protocol> imported = new ArrayList<>();
        SharedImportDialog<Protocol, Protocol> dialog = SharedImportDialog.forProtocols(loc, protocolService, imported::add);
        Protocol listed = this.<Protocol>items(dialog).stream().filter(p -> p.getUuid().equals(shared.getUuid())).findFirst().orElseThrow();

        importButton(dialog, listed).click();

        assertThat(imported).hasSize(1);
        assertThat(imported.getFirst().getName()).isEqualTo(shared.getName());
        assertThat(protocolService.findAllForUser()).extracting(Protocol::getUuid).containsExactly(imported.getFirst().getUuid());
    }
}
