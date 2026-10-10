package com.github.enerccio.marginalia.extensions.sidequery.ui;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.extensions.sidequery.model.SideQuerySetting;
import com.github.enerccio.marginalia.extensions.sidequery.model.SideQuerySettings;
import com.github.enerccio.marginalia.extensions.sidequery.service.SideQueryService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

@Configurable(preConstruction = true)
public class SideQuerySettingsForm extends VerticalLayout {

    public static final String DEFAULT_PROFILE = "_Default";

    @Autowired
    private Localization loc;

    private final SideQueryService sideQueryService;

    private ComboBox<String> profileCombo;
    private Button newProfileBtn;
    private Button renameProfileBtn;
    private Button deleteProfileBtn;

    private ComboBox<AI> overrideModelCombo;
    private ComboBox<Protocol> overrideProtocolCombo;
    private TextArea initialQueryArea;
    private TextArea instructionsBeforeUserArea;
    private Checkbox enableAiTabNamesBox;

    private SideQuerySettings settings;
    private String currentProfileName;

    public SideQuerySettingsForm(SideQueryService sideQueryService) {
        this.sideQueryService = sideQueryService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        buildForm();
        refresh();
    }

    private void buildForm() {
        HorizontalLayout profileLayout = new HorizontalLayout();
        profileLayout.setWidthFull();
        profileLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        profileCombo = new ComboBox<>("Profile");
        profileCombo.setWidthFull();
        profileCombo.setAllowCustomValue(false);
        profileCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null && !e.getValue().equals(currentProfileName)) {
                saveCurrentFieldsToProfile();
                currentProfileName = e.getValue();
                settings.setDefaultSetting(currentProfileName);
                loadProfileFields(currentProfileName);
                updateProfileButtonsState();
            }
        });

        newProfileBtn = new Button("New", e -> createNewProfile());
        renameProfileBtn = new Button("Rename", e -> renameCurrentProfile());
        deleteProfileBtn = new Button("Delete", e -> deleteCurrentProfile());

        profileLayout.add(profileCombo, newProfileBtn, renameProfileBtn, deleteProfileBtn);
        profileLayout.setFlexGrow(1, profileCombo);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        overrideModelCombo = new ComboBox<>("SideQuery Model (Default: Manuscript AI)");
        overrideModelCombo.setWidthFull();
        overrideModelCombo.setItemLabelGenerator(AI::getName);
        overrideModelCombo.setClearButtonVisible(true);

        overrideProtocolCombo = new ComboBox<>("SideQuery Protocol (Default: Manuscript Protocol)");
        overrideProtocolCombo.setWidthFull();
        overrideProtocolCombo.setItemLabelGenerator(Protocol::getName);
        overrideProtocolCombo.setClearButtonVisible(true);

        formLayout.add(overrideModelCombo, overrideProtocolCombo);

        initialQueryArea = new TextArea("Initial System Query");
        initialQueryArea.setWidthFull();
        ResizableTextArea.install(loc, initialQueryArea, "160px");

        instructionsBeforeUserArea = new TextArea("Instructions Before User Input");
        instructionsBeforeUserArea.setWidthFull();
        ResizableTextArea.install(loc, instructionsBeforeUserArea, "120px");

        enableAiTabNamesBox = new Checkbox("Enable AI Tab Naming");

        add(profileLayout, formLayout, initialQueryArea, instructionsBeforeUserArea, enableAiTabNamesBox);
    }

    public void refresh() {
        try {
            settings = sideQueryService.getSettings();
            if (settings == null) {
                settings = sideQueryService.createDefault();
            }
            if (settings.getSettings().isEmpty()) {
                settings.getSettings().put(DEFAULT_PROFILE, new SideQuerySetting());
            }

            List<String> profileNames = new ArrayList<>(settings.getSettings().keySet());
            profileCombo.setItems(profileNames);

            String activeProfile = settings.getDefaultSetting();
            if (activeProfile == null || !settings.getSettings().containsKey(activeProfile)) {
                activeProfile = settings.getSettings().containsKey(DEFAULT_PROFILE)
                        ? DEFAULT_PROFILE
                        : profileNames.get(0);
                settings.setDefaultSetting(activeProfile);
            }

            currentProfileName = activeProfile;
            profileCombo.setValue(currentProfileName);

            overrideModelCombo.setItems(sideQueryService.getAvailableAiModels());
            overrideProtocolCombo.setItems(sideQueryService.getAvailableProtocols());

            loadProfileFields(currentProfileName);
            updateProfileButtonsState();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void loadProfileFields(String profileName) {
        if (settings == null || profileName == null) return;
        SideQuerySetting profileSetting = settings.getSettings().get(profileName);
        if (profileSetting == null) {
            profileSetting = new SideQuerySetting();
            settings.getSettings().put(profileName, profileSetting);
        }

        try {
            if (profileSetting.getSelectedAiId() != null) {
                AI validAi = sideQueryService.findAi(profileSetting.getSelectedAiId());
                overrideModelCombo.setValue(validAi != null && !validAi.isDeleted() ? validAi : null);
            } else {
                overrideModelCombo.setValue(null);
            }

            if (profileSetting.getSelectedProtocolId() != null) {
                Protocol validProtocol = sideQueryService.findProtocol(profileSetting.getSelectedProtocolId());
                overrideProtocolCombo.setValue(validProtocol != null && !validProtocol.isDeleted() ? validProtocol : null);
            } else {
                overrideProtocolCombo.setValue(null);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        initialQueryArea.setValue(profileSetting.getInitialQuery() != null ? profileSetting.getInitialQuery() : "");
        instructionsBeforeUserArea.setValue(profileSetting.getInstructionsBeforeUser() != null ? profileSetting.getInstructionsBeforeUser() : "");
        enableAiTabNamesBox.setValue(profileSetting.isEnableAiTabNames());
    }

    private void saveCurrentFieldsToProfile() {
        if (settings == null || currentProfileName == null) return;
        SideQuerySetting profileSetting = settings.getSettings().get(currentProfileName);
        if (profileSetting == null) {
            profileSetting = new SideQuerySetting();
            settings.getSettings().put(currentProfileName, profileSetting);
        }

        AI selectedAi = overrideModelCombo.getValue();
        profileSetting.setSelectedAiId(selectedAi != null ? selectedAi.getId() : null);

        Protocol selectedProtocol = overrideProtocolCombo.getValue();
        profileSetting.setSelectedProtocolId(selectedProtocol != null ? selectedProtocol.getId() : null);

        profileSetting.setInitialQuery(initialQueryArea.getValue());
        profileSetting.setInstructionsBeforeUser(instructionsBeforeUserArea.getValue());
        profileSetting.setEnableAiTabNames(enableAiTabNamesBox.getValue());
    }

    private void updateProfileButtonsState() {
        boolean isDefault = DEFAULT_PROFILE.equalsIgnoreCase(currentProfileName);
        renameProfileBtn.setEnabled(!isDefault);
        deleteProfileBtn.setEnabled(!isDefault);
    }

    private void createNewProfile() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New SideQuery Profile");

        TextField nameField = new TextField("Profile Name");
        nameField.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(nameField);
        dialogLayout.setPadding(false);
        dialog.add(dialogLayout);

        Button createBtn = new Button("Create", e -> {
            String name = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (name.isEmpty()) {
                Notification.error("Profile name cannot be empty");
                return;
            }
            if (settings.getSettings().containsKey(name)) {
                Notification.error("Profile already exists");
                return;
            }

            saveCurrentFieldsToProfile();

            SideQuerySetting newSetting = new SideQuerySetting();
            settings.getSettings().put(name, newSetting);
            currentProfileName = name;
            settings.setDefaultSetting(name);

            profileCombo.setItems(new ArrayList<>(settings.getSettings().keySet()));
            profileCombo.setValue(name);
            loadProfileFields(name);
            updateProfileButtonsState();

            dialog.close();
        });
        createBtn.setThemeName("primary");

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, createBtn);
        dialog.open();
    }

    private void renameCurrentProfile() {
        if (DEFAULT_PROFILE.equalsIgnoreCase(currentProfileName)) {
            Notification.error("Cannot rename " + DEFAULT_PROFILE + " profile");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Rename SideQuery Profile");

        TextField nameField = new TextField("New Profile Name");
        nameField.setWidthFull();
        nameField.setValue(currentProfileName != null ? currentProfileName : "");

        VerticalLayout dialogLayout = new VerticalLayout(nameField);
        dialogLayout.setPadding(false);
        dialog.add(dialogLayout);

        Button renameBtn = new Button("Rename", e -> {
            String newName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (newName.isEmpty()) {
                Notification.error("Profile name cannot be empty");
                return;
            }
            if (newName.equals(currentProfileName)) {
                dialog.close();
                return;
            }
            if (settings.getSettings().containsKey(newName)) {
                Notification.error("Profile already exists");
                return;
            }

            saveCurrentFieldsToProfile();

            SideQuerySetting profileSetting = settings.getSettings().remove(currentProfileName);
            if (profileSetting == null) {
                profileSetting = new SideQuerySetting();
            }

            settings.getSettings().put(newName, profileSetting);
            currentProfileName = newName;
            settings.setDefaultSetting(newName);

            profileCombo.setItems(new ArrayList<>(settings.getSettings().keySet()));
            profileCombo.setValue(newName);
            updateProfileButtonsState();

            dialog.close();
        });
        renameBtn.setThemeName("primary");

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, renameBtn);
        dialog.open();
    }

    private void deleteCurrentProfile() {
        if (DEFAULT_PROFILE.equalsIgnoreCase(currentProfileName)) {
            Notification.error("Cannot delete " + DEFAULT_PROFILE + " profile");
            return;
        }

        settings.getSettings().remove(currentProfileName);

        String fallbackProfile = DEFAULT_PROFILE;
        if (!settings.getSettings().containsKey(fallbackProfile) && !settings.getSettings().isEmpty()) {
            fallbackProfile = settings.getSettings().firstKey();
        } else if (settings.getSettings().isEmpty()) {
            settings.getSettings().put(DEFAULT_PROFILE, new SideQuerySetting());
        }

        currentProfileName = fallbackProfile;
        settings.setDefaultSetting(currentProfileName);

        profileCombo.setItems(new ArrayList<>(settings.getSettings().keySet()));
        profileCombo.setValue(currentProfileName);
        loadProfileFields(currentProfileName);
        updateProfileButtonsState();
    }

    public SideQuerySettings save() {
        if (settings == null) {
            settings = sideQueryService.createDefault();
        }
        if (currentProfileName == null) {
            currentProfileName = DEFAULT_PROFILE;
        }

        saveCurrentFieldsToProfile();
        settings.setDefaultSetting(currentProfileName);
        return settings;
    }
}