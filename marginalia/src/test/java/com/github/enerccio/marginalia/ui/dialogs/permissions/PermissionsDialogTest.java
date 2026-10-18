package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The dialog an administrator edits the permissions of a user in: nothing is stored before Save, Save is off while
 * nothing changed, Exit asks when something did.
 */
class PermissionsDialogTest extends MarginaliaTestBase {

    @Autowired
    private JdbcTemplate jdbc;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        user = createUser();
        loginAdmin();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private PermissionsDialog dialog() {
        PermissionsDialog dialog = new PermissionsDialog(user);
        dialog.create();
        dialog.open();
        return dialog;
    }

    private static Stream<Component> all(Component root) {
        Stream<Component> children = root.getChildren();
        if (root instanceof Dialog dialog) {
            // the footer is not among the children of the dialog
            children = Stream.concat(children, dialog.getFooter().getElement().getChildren()
                    .map(e -> e.getComponent().orElseThrow()));
        }
        return Stream.concat(Stream.of(root), children.flatMap(PermissionsDialogTest::all));
    }

    private static Button button(Component root, String text) {
        return all(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<PermissionType> combo(Component root) {
        return all(root).filter(ComboBox.class::isInstance).map(c -> (ComboBox<PermissionType>) c).findFirst().orElseThrow();
    }

    private int rows() {
        return jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ?", Integer.class, user.getId());
    }

    @Test
    void showsThePermissionsOfTheUserAndOffersTheOthers() throws Exception {
        permissionService.setGranted(user, PermissionType.HELPER, false);

        PermissionsDialog dialog = dialog();

        // the test user was given CREATE_AI
        assertThat(dialog.getTypes()).containsExactlyInAnyOrder(PermissionType.HELPER, PermissionType.CREATE_AI);
        assertThat(dialog.getAvailableTypes()).containsExactly(PermissionType.AGENTS);
        assertThat(combo(dialog).isEnabled()).isTrue();
        assertThat(button(dialog, "Save").isEnabled()).isFalse();
        assertThat(dialog.isDirty()).isFalse();
        assertThat(dialog.getSettings(PermissionType.CREATE_AI)).as("no settings of its own").isNull();
        assertThat(dialog.getSettings(PermissionType.HELPER)).isNotNull();
    }

    @Test
    void changingAPermissionEnablesSaveAndSaveStoresIt() throws Exception {
        PermissionsDialog dialog = dialog();
        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).isTrue();

        dialog.setGranted(PermissionType.CREATE_AI, false);

        assertThat(button(dialog, "Save").isEnabled()).isTrue();
        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).as("not stored yet").isTrue();

        button(dialog, "Save").click();

        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).isFalse();
        assertThat(button(dialog, "Save").isEnabled()).isFalse();
        assertThat(dialog.isDirty()).isFalse();
    }

    @Test
    void togglingBackIsNotAChange() throws Exception {
        PermissionsDialog dialog = dialog();

        dialog.setGranted(PermissionType.CREATE_AI, false);
        dialog.setGranted(PermissionType.CREATE_AI, true);

        assertThat(button(dialog, "Save").isEnabled()).isFalse();
    }

    @Test
    void deletingIsHardAndHappensOnSave() throws Exception {
        PermissionsDialog dialog = dialog();
        assertThat(rows()).isEqualTo(1);

        dialog.remove(PermissionType.CREATE_AI);

        assertThat(dialog.getTypes()).isEmpty();
        assertThat(dialog.getAvailableTypes()).contains(PermissionType.CREATE_AI);
        assertThat(rows()).as("not before Save").isEqualTo(1);
        assertThat(button(dialog, "Save").isEnabled()).isTrue();

        button(dialog, "Save").click();

        assertThat(rows()).as("deleted for good, not only marked").isZero();
        assertThat(permissionService.getPermission(user, PermissionType.CREATE_AI)).isNull();
    }

    @Test
    void addingOffersOnlyTheTypesTheUserHasNoneOf() throws Exception {
        PermissionsDialog dialog = dialog();
        assertThat(dialog.getAvailableTypes()).containsExactlyInAnyOrder(PermissionType.HELPER, PermissionType.AGENTS);
        assertThat(button(dialog, "Add permission").isEnabled()).isFalse();

        combo(dialog).setValue(PermissionType.HELPER);
        assertThat(button(dialog, "Add permission").isEnabled()).isTrue();
        button(dialog, "Add permission").click();

        assertThat(dialog.getTypes()).contains(PermissionType.HELPER);
        assertThat(dialog.getAvailableTypes()).containsExactly(PermissionType.AGENTS);
        assertThat(button(dialog, "Save").isEnabled()).isTrue();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER)).as("not stored yet").isNull();

        button(dialog, "Save").click();

        Permission helper = permissionService.getPermission(user, PermissionType.HELPER);
        assertThat(helper.isGranted()).isTrue();
        assertThat(helper.getOwner().getId()).as("given by the administrator").isEqualTo(currentUser.getId());
    }

    @Test
    void settingsOfThePermissionTypeAreEditedInTheDialog() throws Exception {
        permissionService.setGranted(user, PermissionType.HELPER, true);
        PermissionsDialog dialog = dialog();
        dialog.setExpanded(PermissionType.HELPER, true);
        HelperPermissionComponent settings = (HelperPermissionComponent) dialog.getSettings(PermissionType.HELPER);
        Checkbox applyLimits = all(settings.getComponent()).filter(Checkbox.class::isInstance).map(Checkbox.class::cast)
                .filter(c -> "Apply limits".equals(c.getLabel())).findFirst().orElseThrow();
        assertThat(applyLimits.getValue()).isFalse();
        assertThat(dialog.isDirty()).as("showing the settings is not an edit").isFalse();

        applyLimits.setValue(true);

        assertThat(button(dialog, "Save").isEnabled()).isTrue();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER).getHelperApplyLimits()).isFalse();

        button(dialog, "Save").click();

        assertThat(permissionService.getPermission(user, PermissionType.HELPER).getHelperApplyLimits()).isTrue();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER).getHelperInteractions()).isFalse();
    }

    @Test
    void settingsOfTheAgentsPermissionAreTheLimitsOfTheUser() throws Exception {
        permissionService.setGranted(user, PermissionType.AGENTS, true);
        PermissionsDialog dialog = dialog();
        dialog.setExpanded(PermissionType.AGENTS, true);
        AgentsPermissionComponent settings = (AgentsPermissionComponent) dialog.getSettings(PermissionType.AGENTS);
        IntegerField maxAgents = field(settings, "Most agents");
        IntegerField runs = field(settings, "Concurrent runs");
        IntegerField budget = field(settings, "Token budget of a run");
        assertThat(maxAgents.getValue()).as("empty is the default").isNull();
        assertThat(dialog.isDirty()).isFalse();

        maxAgents.setValue(2);
        runs.setValue(3);
        budget.setValue(100_000);

        assertThat(button(dialog, "Save").isEnabled()).isTrue();
        button(dialog, "Save").click();

        Permission stored = permissionService.getPermission(user, PermissionType.AGENTS);
        assertThat(stored.getAgentsMaxAgents()).isEqualTo(2);
        assertThat(stored.getAgentsMaxConcurrentRuns()).isEqualTo(3);
        assertThat(stored.getAgentsRunTokenBudget()).isEqualTo(100_000L);
    }

    private static IntegerField field(AgentsPermissionComponent settings, String label) {
        return all(settings.getComponent()).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .filter(f -> label.equals(f.getLabel())).findFirst().orElseThrow();
    }

    @Test
    void settingsAreFoldedUntilUnfolded() throws Exception {
        permissionService.setGranted(user, PermissionType.HELPER, true);
        PermissionsDialog dialog = dialog();

        assertThat(dialog.isExpanded(PermissionType.HELPER)).isFalse();
        dialog.setExpanded(PermissionType.HELPER, true);
        assertThat(dialog.isExpanded(PermissionType.HELPER)).isTrue();
        dialog.setExpanded(PermissionType.HELPER, false);
        assertThat(dialog.isExpanded(PermissionType.HELPER)).isFalse();
        assertThat(dialog.isDirty()).as("folding is not an edit").isFalse();

        // a type without settings has nothing to unfold
        dialog.setExpanded(PermissionType.CREATE_AI, true);
        assertThat(dialog.isExpanded(PermissionType.CREATE_AI)).isFalse();
    }

    @Test
    void aNewPermissionIsUnfoldedAndTheFoldSurvivesSave() throws Exception {
        PermissionsDialog dialog = dialog();
        combo(dialog).setValue(PermissionType.HELPER);
        button(dialog, "Add permission").click();
        assertThat(dialog.isExpanded(PermissionType.HELPER)).isTrue();

        button(dialog, "Save").click();

        assertThat(dialog.isExpanded(PermissionType.HELPER)).isTrue();
    }

    @Test
    void exitClosesWithoutChangesAndAsksWithThem() throws Exception {
        PermissionsDialog dialog = dialog();
        button(dialog, "Exit").click();
        assertThat(dialog.isOpened()).isFalse();

        PermissionsDialog edited = dialog();
        edited.setGranted(PermissionType.CREATE_AI, false);
        button(edited, "Exit").click();
        assertThat(edited.isOpened()).as("asks first").isTrue();
        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).isTrue();
    }
}
