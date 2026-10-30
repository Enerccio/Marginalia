package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit.Origin;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.permissions.VersionHistoryPermissionComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The places where the limit of the version history is set: the box of a book, a folder and the user, the permission of the
 * administrator, and what a folder saves.
 */
class HistoryLimitSettingsTest extends MarginaliaTestBase {

    @Autowired
    private Localization loc;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = login();
    }

    private HistoryLimitField field() {
        return new HistoryLimitField("Versions");
    }

    @Test
    void anEmptyBoxShowsWhatTheLevelsAboveSetAndWhoSetsIt() {
        HistoryLimitField field = field();
        Folder folder = new Folder();
        folder.setName("Sci-fi");

        field.load(null, new EffectiveHistoryLimit(20, Origin.FOLDER, folder));
        assertThat(field.getValue()).isNull();
        assertThat(field.getPlaceholder()).isEqualTo("20");
        assertThat(field.getHelperText()).isEqualTo(String.format(loc.getValue(L.MSG_HISTORY_LIMIT_FOLDER), 20, "Sci-fi"));

        field.load(null, new EffectiveHistoryLimit(7, Origin.USER, null));
        assertThat(field.getHelperText()).isEqualTo(String.format(loc.getValue(L.MSG_HISTORY_LIMIT_USER), 7));

        field.load(null, new EffectiveHistoryLimit(50, Origin.PERMISSION, null));
        assertThat(field.getHelperText()).isEqualTo(String.format(loc.getValue(L.MSG_HISTORY_LIMIT_PERMISSION), 50));
    }

    @Test
    void aNumberAboveTheLimitOfTheLevelsAboveHasNoEffectAndTheBoxSaysSo() {
        HistoryLimitField field = field();
        field.load(100, new EffectiveHistoryLimit(50, Origin.PERMISSION, null));
        assertThat(field.getHelperText()).isEqualTo(String.format(loc.getValue(L.MSG_HISTORY_LIMIT_PERMISSION), 50));

        field.setValue(10);
        assertThat(field.getHelperText()).as("a stricter number is the limit").isEqualTo(loc.getValue(L.HELP_HISTORY_MAX_VERSIONS));
    }

    @Test
    void whenNothingAboveLimitsTheBoxTellsThat() {
        HistoryLimitField field = field();

        field.load(null, new EffectiveHistoryLimit(null, Origin.NONE, null));

        assertThat(field.getPlaceholder()).isEmpty();
        assertThat(field.getHelperText()).isEqualTo(loc.getValue(L.MSG_HISTORY_LIMIT_NONE));
        field.setValue(5);
        assertThat(field.getHelperText()).isEqualTo(loc.getValue(L.HELP_HISTORY_MAX_VERSIONS));
    }

    @Test
    void theUserSettingsBoxSeesTheAdministratorsLimit() throws Exception {
        EffectiveHistoryLimit limit = manuscriptService.resolvePermissionHistoryLimit();

        assertThat(limit.value()).isEqualTo(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS);
        assertThat(limit.origin()).isEqualTo(Origin.PERMISSION);

        loginAdmin();
        permissionService.setGranted(user, PermissionType.VERSION_HISTORY, true);
        Permission permission = permissionService.getPermission(user, PermissionType.VERSION_HISTORY);
        permission.setHistoryMaxVersions(9);
        permissionService.save(permission);
        loginAs(user);
        assertThat(manuscriptService.resolvePermissionHistoryLimit().value()).isEqualTo(9);
    }

    @Test
    void aFolderSavesTheLimitItIsGiven() throws Exception {
        Folder folder = folderService.create("Limited", null);

        folderService.update(folder, new FolderService.Details("Limited", "", null, Map.of(), 12));
        assertThat(folderService.find(folder.getId()).getHistoryMaxVersions()).isEqualTo(12);

        folderService.update(folder, new FolderService.Details("Limited", "", null, Map.of(), -3));
        assertThat(folderService.find(folder.getId()).getHistoryMaxVersions()).as("a negative number is no limit").isNull();

        folderService.update(folder, new FolderService.Details("Limited", "", null, Map.of(), 0));
        assertThat(folderService.find(folder.getId()).getHistoryMaxVersions()).as("zero is a limit: no history").isZero();

        folderService.update(folder, new FolderService.Details("Limited", "", null, Map.of()));
        assertThat(folderService.find(folder.getId()).getHistoryMaxVersions()).isNull();
    }

    // --- the permission of the administrator ---

    private static IntegerField numberOf(VersionHistoryPermissionComponent component) {
        Component root = component.getComponent();
        return (IntegerField) root.getChildren().filter(IntegerField.class::isInstance).findFirst().orElseThrow();
    }

    @Test
    void aPermissionAddedByTheAdministratorStartsWithTheDefault() {
        VersionHistoryPermissionComponent component = new VersionHistoryPermissionComponent();
        Permission added = new Permission();
        added.setPermissionType(PermissionType.VERSION_HISTORY);
        added.setGranted(true);

        component.model2component(added);

        assertThat(numberOf(component).getValue()).isEqualTo(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS);
        component.component2model(added);
        assertThat(added.getHistoryMaxVersions()).isEqualTo(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS);
    }

    @Test
    void aSavedPermissionWithoutANumberStaysWithoutOne() throws Exception {
        loginAdmin();
        permissionService.setGranted(user, PermissionType.VERSION_HISTORY, true);
        Permission saved = permissionService.getPermission(user, PermissionType.VERSION_HISTORY);
        saved.setHistoryMaxVersions(null);
        VersionHistoryPermissionComponent component = new VersionHistoryPermissionComponent();

        component.model2component(saved);

        assertThat(numberOf(component).getValue()).isNull();
    }

    @Test
    void theNumberCanBeChangedOnlyWhileThePermissionIsGrantedAndTellsTheDialog() {
        VersionHistoryPermissionComponent component = new VersionHistoryPermissionComponent();
        Permission permission = new Permission();
        permission.setGranted(true);
        permission.setHistoryMaxVersions(30);
        AtomicInteger edits = new AtomicInteger();
        component.onChange(edits::incrementAndGet);
        component.model2component(permission);
        assertThat(edits).as("putting the model in is not an edit").hasValue(0);
        assertThat(numberOf(component).isEnabled()).isTrue();

        numberOf(component).setValue(40);
        assertThat(edits).hasValue(1);
        component.component2model(permission);
        assertThat(permission.getHistoryMaxVersions()).isEqualTo(40);

        component.onGrantedChanged(false);
        assertThat(numberOf(component).isEnabled()).isFalse();
    }
}
