package com.github.enerccio.marginalia.notifications;

import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.NotificationService.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.notifications.NotificationSettingsEditor;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The notifications tab of the user settings: the switch, the list with its filter, and the buttons that check what the filter shows.
 */
class NotificationSettingsEditorTest extends MarginaliaTestBase {

    private static final String OWNER = "test-notification-editor";

    @Autowired
    private NotificationService service;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference
    private UI ui;

    @BeforeEach
    void user() throws Exception {
        login();
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void cleanUp() {
        UI.setCurrent(null);
        service.unregisterAll(OWNER);
    }

    private NotificationSettingsEditor editor() throws Exception {
        NotificationSettingsEditor editor = new NotificationSettingsEditor(loc, service);
        editor.reload();
        return editor;
    }

    private static List<String> keys(List<Entry> entries) {
        return entries.stream().map(Entry::key).toList();
    }

    @Test
    void everySourceIsListedAndNothingIsCheckedForANewUser() throws Exception {
        NotificationSettingsEditor editor = editor();

        assertThat(keys(editor.shown())).containsSubsequence(GENERATION, ANNOTATE, ANNOTATE + AUTOMATIC_SUFFIX, REDRAFT,
                REDRAFT + AUTOMATIC_SUFFIX, IMPERSONATE, SUMMARY, RESUMMARIZE, HELP_ASSISTANT);
        assertThat(editor.getCheckedKeys()).isEmpty();
        assertThat(editor.getEnabledField().getValue()).isFalse();
    }

    @Test
    void theCheckBoxOnlyEnablesTheButtonsThatRegisterAndTestThisBrowser() throws Exception {
        NotificationSettingsEditor editor = editor();

        assertThat(editor.getRegisterButton().isEnabled()).isFalse();
        assertThat(editor.getTestButton().isEnabled()).isFalse();

        editor.getEnabledField().setValue(true);

        assertThat(editor.getRegisterButton().isEnabled()).isTrue();
        assertThat(editor.getTestButton().isEnabled()).isTrue();

        editor.getEnabledField().setValue(false);

        assertThat(editor.getRegisterButton().isEnabled()).isFalse();
    }

    @Test
    void theStoredSwitchEnablesTheButtonsOnLoad() throws Exception {
        service.save(true, Set.of());

        NotificationSettingsEditor editor = editor();

        assertThat(editor.getRegisterButton().isEnabled()).isTrue();
        assertThat(editor.getTestButton().isEnabled()).isTrue();
    }

    @Test
    void theStoredChoiceIsShown() throws Exception {
        service.save(true, Set.of(ANNOTATE + AUTOMATIC_SUFFIX, SUMMARY));

        NotificationSettingsEditor editor = editor();

        assertThat(editor.getEnabledField().getValue()).isTrue();
        assertThat(editor.getCheckedKeys()).containsExactlyInAnyOrder(ANNOTATE + AUTOMATIC_SUFFIX, SUMMARY);
    }

    @Test
    void theFilterShowsTheEntriesWithThatTextInTheNameInAnyCase() throws Exception {
        NotificationSettingsEditor editor = editor();

        editor.getFilterField().setValue("ANNOTATE");

        assertThat(keys(editor.shown())).containsExactly(ANNOTATE, ANNOTATE + AUTOMATIC_SUFFIX);

        editor.getFilterField().clear();

        assertThat(editor.shown()).hasSizeGreaterThan(2);
    }

    @Test
    void checkingWhatIsShownChecksOnlyTheFilteredEntries() throws Exception {
        NotificationSettingsEditor editor = editor();
        editor.getFilterField().setValue("redraft");

        editor.checkShown();

        assertThat(editor.getCheckedKeys()).containsExactlyInAnyOrder(REDRAFT, REDRAFT + AUTOMATIC_SUFFIX);

        editor.getFilterField().setValue("annotate");
        editor.checkShown();

        assertThat(editor.getCheckedKeys()).as("the ones hidden by the new filter stay checked")
                .containsExactlyInAnyOrder(REDRAFT, REDRAFT + AUTOMATIC_SUFFIX, ANNOTATE, ANNOTATE + AUTOMATIC_SUFFIX);

        editor.getFilterField().setValue("redraft");
        editor.uncheckShown();

        assertThat(editor.getCheckedKeys()).containsExactlyInAnyOrder(ANNOTATE, ANNOTATE + AUTOMATIC_SUFFIX);
    }

    @Test
    void anExtensionSourceIsListedAndCanBeFound() throws Exception {
        service.register(OWNER, new Source("test.editor.review", () -> "Zebra reviewer", true));
        NotificationSettingsEditor editor = editor();

        editor.getFilterField().setValue("zebra");

        assertThat(keys(editor.shown())).containsExactly("test.editor.review", "test.editor.review" + AUTOMATIC_SUFFIX);
    }

    @Test
    void editsAreStoredOnlyOnSaveAndTellThePage() throws Exception {
        NotificationSettingsEditor editor = editor();
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);

        editor.getEnabledField().setValue(true);
        editor.getFilterField().setValue("generation");
        editor.checkShown();

        assertThat(changes.get()).isEqualTo(2);
        assertThat(service.isEnabled()).isFalse();
        assertThat(service.getChosen()).isEmpty();

        editor.save();

        assertThat(service.isEnabled()).isTrue();
        assertThat(service.getChosen()).containsExactly(GENERATION);
        assertThat(changes.get()).as("saving is not a change").isEqualTo(2);
    }

    @Test
    void reloadDropsTheEditsAndFillingTheFieldsIsNotAChange() throws Exception {
        service.save(true, Set.of(GENERATION));
        NotificationSettingsEditor editor = editor();
        editor.getEnabledField().setValue(false);
        editor.getFilterField().setValue("summary");
        editor.checkShown();
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);

        editor.reload();

        assertThat(editor.getEnabledField().getValue()).isTrue();
        assertThat(editor.getCheckedKeys()).containsExactly(GENERATION);
        assertThat(editor.getFilterField().getValue()).isEmpty();
        assertThat(changes.get()).isZero();
    }

    @Test
    void savingKeepsWhatWasChosenForAnExtensionThatIsNotLoaded() throws Exception {
        service.register(OWNER, new Source("test.editor.gone", () -> "Gone"));
        service.save(true, Set.of("test.editor.gone"));
        service.unregisterAll(OWNER);
        NotificationSettingsEditor editor = editor();

        editor.getFilterField().setValue("generation");
        editor.checkShown();
        editor.save();

        assertThat(service.getChosen()).containsExactlyInAnyOrder("test.editor.gone", GENERATION);
    }
}
