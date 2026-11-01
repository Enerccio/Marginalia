package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.styles.PartStyleEditor;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The editor of the text styles of the user settings: the list, duplicating a built in style, changing and saving the options with
 * the generated CSS following the fields, and making a style the default.
 */
class PartStyleEditorTest extends MarginaliaTestBase {

    @Autowired
    private PartStyleService service;

    @Autowired
    private Localization loc;

    /**
     * A UI without a session: what is run in the browser is recorded (the id and the CSS of the style elements).
     */
    private static class RecordingUI extends UI {

        final List<Object[]> styles = new ArrayList<>();

        private final Page page = new Page(this) {
            @Override
            public PendingJavaScriptResult executeJs(String expression, Object... params) {
                styles.add(params);
                return null;
            }
        };

        @Override
        public Page getPage() {
            return page;
        }
    }

    // UI.setCurrent keeps only a weak reference
    private RecordingUI ui;

    @BeforeEach
    void user() throws Exception {
        login();
        ui = new RecordingUI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
    }

    private static void click(Button button) {
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    private Button button(PartStyleEditor editor, String text) {
        return ComponentTree.button(editor, text);
    }

    private static TextField fontSize(PartStyleEditor editor) {
        return (TextField) editor.getOptionField(PartStyleOption.FONT_SIZE);
    }

    private static List<PartStyle> own(PartStyleService service) throws Exception {
        return service.list().stream().filter(style -> !style.isBuiltin()).toList();
    }

    @Test
    void theListHasTheBuiltInStylesAndTheyCanNotBeChangedOrDeleted() {
        PartStyleEditor editor = new PartStyleEditor(loc, service);

        Grid<PartStyle> grid = ComponentTree.all(editor, Grid.class).findFirst().orElseThrow();
        assertThat(grid.getListDataView().getItems().map(PartStyle::getId).toList()).containsExactly("book", "export-epub", "export-html", "normal", "export-pdf", "export-docx");
        PartStyle shown = grid.getSelectedItems().iterator().next();
        assertThat(shown.getId()).isEqualTo("book");
        assertThat(button(editor, "Delete").isEnabled()).isFalse();
        assertThat(fontSize(editor).isReadOnly()).isTrue();
        assertThat(fontSize(editor).getValue()).isEqualTo("1.04rem");
    }

    @Test
    void theEditorHasNoSaveOfItsOwn() {
        PartStyleEditor editor = new PartStyleEditor(loc, service);

        assertThat(ComponentTree.all(editor, Button.class).map(Button::getText)).contains("Duplicate", "Delete", "Set as default", "Export", "Import");
    }

    @Test
    void changesStayInMemoryUntilTheSettingsAreSavedAndTellThePage() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);

        click(button(editor, "Duplicate"));
        assertThat(changes.get()).isEqualTo(1);
        assertThat(own(service)).as("a duplicate is not stored yet").isEmpty();
        assertThat(fontSize(editor).isReadOnly()).isFalse();
        assertThat(button(editor, "Delete").isEnabled()).isTrue();

        fontSize(editor).setValue("3rem");
        assertThat(changes.get()).isEqualTo(2);
        assertThat(editor.getGeneratedCssField().getValue()).contains("font-size: 3rem;");
        Object[] preview = ui.styles.getLast();
        assertThat(preview[0]).isEqualTo("marginalia-part-style-preview");
        assertThat((String) preview[1]).contains(".part-style-preview .chat-message-markdown").contains("font-size: 3rem;");

        assertThat(editor.validate()).isTrue();
        editor.save();

        PartStyle stored = own(service).getFirst();
        assertThat(stored.getName()).isEqualTo("Book (copy)");
        assertThat(stored.get(PartStyleOption.FONT_SIZE)).isEqualTo("3rem");
        assertThat(service.find("book").get(PartStyleOption.FONT_SIZE)).isEqualTo("1.04rem");
        assertThat(changes.get()).as("saving is not a change").isEqualTo(2);
        assertThat(fontSize(editor).getValue()).as("the saved style stays selected").isEqualTo("3rem");
    }

    @Test
    void reloadIsTheDiscard() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        click(button(editor, "Duplicate"));
        editor.save();
        PartStyle copy = own(service).getFirst();

        fontSize(editor).setValue("9rem");
        editor.getCustomCssField().setValue("p { color: red; }");
        click(button(editor, "Duplicate"));
        editor.reload();

        assertThat(own(service)).hasSize(1);
        assertThat(fontSize(editor).getValue()).isEqualTo("1.04rem");
        assertThat(editor.getCustomCssField().getValue()).isEmpty();
        assertThat(ComponentTree.all(editor, Grid.class).findFirst().map(g -> ((Grid<?>) g).getListDataView().getItemCount()).orElseThrow()).isEqualTo(7);
        assertThat(service.find(copy.getId()).get(PartStyleOption.FONT_SIZE)).isEqualTo("1.04rem");
    }

    @Test
    void theCustomCssIsStoredAndUnbalancedCssIsRefused() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        click(button(editor, "Duplicate"));

        editor.getCustomCssField().setValue(".chat-message-markdown p { text-indent: 2em; }");
        assertThat(editor.validate()).isTrue();
        editor.save();
        assertThat(own(service).getFirst().getCustomCss()).isEqualTo(".chat-message-markdown p { text-indent: 2em; }");

        editor.getCustomCssField().setValue("p { color: red; } }");
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void aDeletedStyleIsDeletedWhenTheSettingsAreSaved() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        click(button(editor, "Duplicate"));
        editor.save();
        assertThat(own(service)).hasSize(1);

        click(button(editor, "Delete"));
        assertThat(own(service)).as("still stored").hasSize(1);
        editor.save();

        assertThat(own(service)).isEmpty();
    }

    private static Checkbox forExportBox(PartStyleEditor editor) {
        return ComponentTree.all(editor, Checkbox.class).filter(box -> box.getLabel() != null && box.getLabel().startsWith("For exports"))
                .findFirst().orElseThrow();
    }

    @Test
    void aStyleCanBeMarkedForExportsWhichTakesItOutOfTheDefaultOfBooks() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        assertThat(forExportBox(editor).isReadOnly()).as("built in").isTrue();
        click(button(editor, "Duplicate"));
        click(button(editor, "Set as default"));
        assertThat(button(editor, "Set as default").isEnabled()).isFalse();

        forExportBox(editor).setValue(true);
        assertThat(button(editor, "Set as default").isEnabled()).as("a style for exports is not one for books").isFalse();
        editor.save();

        PartStyle stored = own(service).getFirst();
        assertThat(stored.isForExport()).isTrue();
        assertThat(service.getDefaultId()).as("it was the default, and is not any more").isNull();
        assertThat(service.listForExport()).extracting(PartStyle::getId).contains(stored.getId());
        assertThat(service.listForBooks()).extracting(PartStyle::getId).doesNotContain(stored.getId());
    }

    @Test
    void aStyleCanBeMadeTheDefaultAndItsDeletionTakesTheDefaultBack() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        assertThat(button(editor, "Set as default").isEnabled()).isTrue();

        click(button(editor, "Set as default"));
        assertThat(button(editor, "Set as default").isEnabled()).isFalse();
        assertThat(service.getDefaultId()).as("not saved yet").isNull();
        editor.save();
        assertThat(service.getDefaultId()).isEqualTo("book");

        click(button(editor, "Duplicate"));
        click(button(editor, "Set as default"));
        editor.save();
        String copy = own(service).getFirst().getId();
        assertThat(service.getDefaultId()).isEqualTo(copy);

        click(button(editor, "Delete"));
        editor.save();
        assertThat(service.getDefaultId()).isNull();
        assertThat(service.getDefault().getId()).isEqualTo("normal");
    }

    @Test
    void aStyleIsExportedAsItIsOnTheScreenAndImportedAsANewStyleOfTheUser() throws Exception {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);
        click(button(editor, "Duplicate"));
        fontSize(editor).setValue("3rem");
        String file = editor.exportJson();
        assertThat(file).contains("\"marginalia-text-style\"").contains("\"name\": \"Book (copy)\"").contains("\"FONT_SIZE\": \"3rem\"");

        assertThat(editor.importJson(file)).isTrue();
        assertThat(changes.get()).as("duplicate, font size, import").isEqualTo(3);
        assertThat(fontSize(editor).getValue()).isEqualTo("3rem");
        assertThat(fontSize(editor).isReadOnly()).isFalse();
        assertThat(own(service)).as("not stored before the settings are saved").isEmpty();

        assertThat(editor.validate()).isTrue();
        editor.save();
        assertThat(own(service)).hasSize(2).extracting(PartStyle::getName).containsOnly("Book (copy)");
        assertThat(own(service).stream().map(PartStyle::getId).distinct()).hasSize(2);
    }

    @Test
    void aFileThatIsNotStylesChangesNothing() {
        PartStyleEditor editor = new PartStyleEditor(loc, service);
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);

        assertThat(editor.importJson("not json")).isFalse();
        assertThat(editor.importJson("{\"format\":\"marginalia-text-style\",\"version\":1,\"styles\":[{\"name\":\"x\",\"customCss\":\"p { color: red;\"}]}")).isFalse();
        assertThat(changes.get()).isZero();
    }
}
