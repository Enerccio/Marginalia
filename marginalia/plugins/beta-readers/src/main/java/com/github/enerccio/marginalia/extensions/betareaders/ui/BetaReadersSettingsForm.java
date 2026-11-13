package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * The panel of the plugin in <i>Settings → Extension Settings</i>: the user level of what books and folders inherit (the two
 * prompts, the medium, the AI and the protocol of the chats) and the libraries of personas and mediums, which are edited in their own
 * windows. Nothing is stored until the Settings screen is saved ({@link #save()} gives the settings it stores); a library edited in a
 * window shows as changed in its summary field, which locks the other sections of the workspace until the settings are saved or
 * discarded, as any changed field does.
 */
@Configurable(preConstruction = true)
public class BetaReadersSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final BetaReadersService service;

    private final TextAreaPopoverComponent singlePrompt = new TextAreaPopoverComponent(T.localized(PROMPT_OF_A_CHAT));
    private final TextAreaPopoverComponent groupPrompt = new TextAreaPopoverComponent(T.localized(PROMPT_OF_A_GROUP_CHAT));
    private final TextAreaPopoverComponent scene = new TextAreaPopoverComponent(T.localized(SCENE));
    private final ComboBox<Medium> mediumCombo = new ComboBox<>(T.localized(MEDIUM));
    private final ComboBox<AI> aiCombo = new ComboBox<>(T.localized(MODEL_OF_THE_CHATS));
    private final ComboBox<Protocol> protocolCombo = new ComboBox<>(T.localized(PROTOCOL_OF_THE_CHATS));
    private final IntegerField maxMessages = new IntegerField(T.localized(MOST_MESSAGES_IN_A_TURN));
    private final TextField personaSummary = new TextField(T.localized(PERSONA_LIBRARY));
    private final TextField mediumSummary = new TextField(T.localized(MEDIUMS));

    private BetaReadersSettings settings;
    /** Whether a library was edited since the settings were loaded, shown in its summary so the Settings screen sees a change. */
    private boolean personasEdited;
    private boolean mediumsEdited;

    public BetaReadersSettingsForm(BetaReadersService service) {
        this.service = service;

        setWidthFull();
        setPadding(true);
        setSpacing(true);
        buildForm();
        refresh();
    }

    private void buildForm() {
        Span intro = new Span(T.localized(WHAT_BOOKS_AND_FOLDERS_INHERIT));
        intro.addClassName(BetaReadersStyles.HINT);

        singlePrompt.setWidthFull();
        singlePrompt.setPlaceholder(T.defaults().singlePrompt());
        singlePrompt.setHelperText(T.localized(EMPTY_THE_BUILT_IN_PROMPT));
        ResizableTextArea.install(loc, singlePrompt, "160px");
        singlePrompt.setPopoverContent(TemplateHints.create(loc, singlePrompt, singlePrompt.getPopover(), null, T.defaults().singlePrompt()));

        groupPrompt.setWidthFull();
        groupPrompt.setPlaceholder(T.defaults().groupPrompt());
        groupPrompt.setHelperText(T.localized(EMPTY_THE_BUILT_IN_PROMPT));
        ResizableTextArea.install(loc, groupPrompt, "160px");
        groupPrompt.setPopoverContent(TemplateHints.create(loc, groupPrompt, groupPrompt.getPopover(), null, T.defaults().groupPrompt()));

        scene.setWidthFull();
        scene.setPlaceholder(T.defaults().scene());
        scene.setHelperText(T.localized(EMPTY_THE_BUILT_IN_SCENE));
        ResizableTextArea.install(loc, scene, "100px");
        scene.setPopoverContent(TemplateHints.create(loc, scene, scene.getPopover(), null, T.defaults().scene()));

        mediumCombo.setWidthFull();
        mediumCombo.setItemLabelGenerator(Medium::getName);
        mediumCombo.setClearButtonVisible(true);
        mediumCombo.setAllowCustomValue(false);
        mediumCombo.setHelperText(T.localized(EMPTY_THE_BUILT_IN_DEFAULT));

        aiCombo.setWidthFull();
        aiCombo.setItemLabelGenerator(AI::getName);
        aiCombo.setClearButtonVisible(true);
        aiCombo.setHelperText(T.localized(EMPTY_THE_MODEL));

        protocolCombo.setWidthFull();
        protocolCombo.setItemLabelGenerator(Protocol::getName);
        protocolCombo.setClearButtonVisible(true);
        protocolCombo.setHelperText(T.localized(EMPTY_THE_PROTOCOL));

        maxMessages.setWidthFull();
        maxMessages.setMin(1);
        maxMessages.setMax(BetaReadersDefaults.MAX_MESSAGES_LIMIT);
        maxMessages.setStepButtonsVisible(true);
        maxMessages.setPlaceholder(T.localized(AUTOMATIC));
        maxMessages.setHelperText(T.localized(EMPTY_AUTOMATIC_THE_MOST_MESSAGES));

        FormLayout choices = new FormLayout(mediumCombo, aiCombo, protocolCombo, maxMessages);
        choices.setWidthFull();

        add(intro, choices, singlePrompt, groupPrompt, scene, libraryRow(personaSummary, T.localized(EDIT_PERSONAS), this::editPersonas),
                libraryRow(mediumSummary, T.localized(EDIT_MEDIUMS), this::editMediums));
    }

    private HorizontalLayout libraryRow(TextField summary, String button, Runnable action) {
        summary.setReadOnly(true);
        summary.setWidthFull();
        Button edit = new Button(button, Solid.PEN.create(), _ -> action.run());
        HorizontalLayout row = new HorizontalLayout(summary, edit);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setFlexGrow(1, summary);
        return row;
    }

    /**
     * Loads the settings again, throwing away what was changed and not saved.
     */
    public void refresh() {
        try {
            settings = service.getSettings();
            personasEdited = false;
            mediumsEdited = false;
            singlePrompt.setValue(settings.getSinglePrompt());
            groupPrompt.setValue(settings.getGroupPrompt());
            scene.setValue(settings.getScene());
            maxMessages.setValue(settings.getMaxMessages() > 0 ? settings.getMaxMessages() : null);
            loadCombos();
            refreshSummaries();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void loadCombos() throws Exception {
        List<AI> ais = service.getAvailableAis();
        List<Protocol> protocols = service.getAvailableProtocols();
        aiCombo.setItems(ais);
        aiCombo.setValue(ais.stream().filter(ai -> ai.getUuid().equals(settings.getAi())).findFirst().orElse(null));
        protocolCombo.setItems(protocols);
        protocolCombo.setValue(protocols.stream().filter(protocol -> protocol.getUuid().equals(settings.getProtocol())).findFirst().orElse(null));
        loadMedium();
    }

    private void loadMedium() {
        mediumCombo.setItems(settings.getMediums());
        mediumCombo.setValue(settings.getMediums().stream().filter(medium -> medium.getId().equals(settings.getMedium())).findFirst().orElse(null));
        mediumCombo.setPlaceholder(settings.getMediums().stream().filter(medium -> BetaReadersDefaults.DEFAULT_MEDIUM.equals(medium.getId()))
                .map(Medium::getName).findFirst().orElse(BetaReadersDefaults.DEFAULT_MEDIUM));
    }

    private void refreshSummaries() {
        personaSummary.setValue(T.localized(PERSONAS_D).formatted(settings.getPersonas().size())
                + (personasEdited ? " " + T.localized(EDITED) : ""));
        mediumSummary.setValue(T.localized(MEDIUMS_D).formatted(settings.getMediums().size())
                + (mediumsEdited ? " " + T.localized(EDITED) : ""));
    }

    private void editPersonas() {
        new PersonaLibraryDialog(T.localized(PERSONA_LIBRARY), settings.getPersonas(),
                (brief, taken, done) -> service.writePersona(null, brief, taken, done), personas -> {
            settings.setPersonas(personas);
            personasEdited = true;
            refreshSummaries();
        }).open();
    }

    private void editMediums() {
        new MediumLibraryDialog(settings.getMediums(), mediums -> {
            settings.setMediums(mediums);
            mediumsEdited = true;
            loadMedium();
            refreshSummaries();
        }).open();
    }

    /**
     * The settings with what the fields hold now, for the Settings screen to store.
     */
    public BetaReadersSettings save() {
        settings.setSinglePrompt(StringUtils.defaultString(singlePrompt.getValue()));
        settings.setGroupPrompt(StringUtils.defaultString(groupPrompt.getValue()));
        settings.setScene(StringUtils.defaultString(scene.getValue()));
        Integer most = maxMessages.getValue();
        settings.setMaxMessages(most == null ? 0 : Math.min(most, BetaReadersDefaults.MAX_MESSAGES_LIMIT));
        Medium medium = mediumCombo.getValue();
        settings.setMedium(medium == null ? "" : medium.getId());
        AI ai = aiCombo.getValue();
        settings.setAi(ai == null ? "" : ai.getUuid());
        Protocol protocol = protocolCombo.getValue();
        settings.setProtocol(protocol == null ? "" : protocol.getUuid());
        return settings;
    }
}
