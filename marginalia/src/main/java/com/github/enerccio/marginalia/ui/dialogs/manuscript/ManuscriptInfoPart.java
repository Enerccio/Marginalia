package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.List;

@Configurable
public class ManuscriptInfoPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final ManuscriptDialog parent;

    private TextField nameField;
    private TextArea descriptionField;
    private ComboBox<AI> aiCombo;
    private ComboBox<Protocol> protocolCombo;

    private IntegerField totalWordCountField;
    private IntegerField totalTokenCountField;
    private IntegerField branchWordCountField;
    private IntegerField branchTokenCountField;

    private boolean loading = false;

    public ManuscriptInfoPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        createFields();

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.add(nameField, descriptionField);
        formLayout.setColspan(descriptionField, 2);
        formLayout.add(aiCombo, protocolCombo);

        FormLayout syntheticFormLayout = new FormLayout();
        syntheticFormLayout.setWidthFull();
        syntheticFormLayout.add(totalWordCountField, totalTokenCountField, branchWordCountField, branchTokenCountField);

        mainLayout.add(formLayout, syntheticFormLayout);

        container.add(loc.getValue(L.LABEL_INFO_PART), mainLayout);
        return mainLayout;
    }

    private void createFields() {
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setWidthFull();
        nameField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        descriptionField = new TextArea(loc.getValue(L.LABEL_DESCRIPTION));
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("100px");
        descriptionField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        aiCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        aiCombo.setWidthFull();
        aiCombo.setItemLabelGenerator(AI::getName);
        aiCombo.setClearButtonVisible(true);
        aiCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        protocolCombo = new ComboBox<>(loc.getValue(L.LABEL_PROTOCOLS));
        protocolCombo.setWidthFull();
        protocolCombo.setItemLabelGenerator(Protocol::getName);
        protocolCombo.setClearButtonVisible(true);
        protocolCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        totalWordCountField = new IntegerField(loc.getValue(L.LABEL_TOTAL_WORD_COUNT));
        totalWordCountField.setWidthFull();
        totalWordCountField.setReadOnly(true);

        totalTokenCountField = new IntegerField(loc.getValue(L.LABEL_TOTAL_TOKEN_COUNT));
        totalTokenCountField.setWidthFull();
        totalTokenCountField.setReadOnly(true);

        branchWordCountField = new IntegerField(loc.getValue(L.LABEL_BRANCH_WORD_COUNT));
        branchWordCountField.setWidthFull();
        branchWordCountField.setReadOnly(true);

        branchTokenCountField = new IntegerField(loc.getValue(L.LABEL_BRANCH_TOKEN_COUNT));
        branchTokenCountField.setWidthFull();
        branchTokenCountField.setReadOnly(true);
    }

    private Manuscript refreshModel() throws Exception {
        return parent.refreshManuscript();
    }

    private void autosave() {
        if (loading) {
            return;
        }
        try {
            Manuscript manuscript = refreshModel();
            if (manuscript == null) {
                return;
            }

            manuscript.setName(nameField.getValue());
            manuscript.setDescription(descriptionField.getValue());
            manuscript.setAi(aiCombo.getValue());
            manuscript.setProtocol(protocolCombo.getValue());

            manuscript = parent.save();
            parent.setHeaderTitle(manuscript.getName());
            updateSyntheticFields(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void updateSyntheticFields(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            totalWordCountField.setValue(0);
            totalTokenCountField.setValue(0);
            branchWordCountField.setValue(0);
            branchTokenCountField.setValue(0);
            return;
        }

        int totalWords = chatMessageService.getTotalWordCount(manuscript);
        int totalTokens = chatMessageService.getTotalTokenCount(manuscript);
        int branchWords = chatMessageService.getBranchWordCount(manuscript.getActiveLeaf());
        int branchTokens = chatMessageService.getBranchTokenCount(manuscript.getActiveLeaf());

        totalWordCountField.setValue(totalWords);
        totalTokenCountField.setValue(totalTokens);
        branchWordCountField.setValue(branchWords);
        branchTokenCountField.setValue(branchTokens);
    }

    @Override
    public void setFrozen(boolean frozen) {

    }

    @Override
    public void load(Manuscript manuscript) {
        if (manuscript == null) {
            return;
        }
        loading = true;
        try {
            List<AI> models = aiService.findAllForUser();
            aiCombo.setItems(models);

            List<Protocol> protocols = protocolService.findAllForUser();
            protocolCombo.setItems(protocols);

            nameField.setValue(StringUtils.defaultString(manuscript.getName()));
            descriptionField.setValue(StringUtils.defaultString(manuscript.getDescription()));
            aiCombo.setValue(manuscript.getAi());
            protocolCombo.setValue(manuscript.getProtocol());

            updateSyntheticFields(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }
}