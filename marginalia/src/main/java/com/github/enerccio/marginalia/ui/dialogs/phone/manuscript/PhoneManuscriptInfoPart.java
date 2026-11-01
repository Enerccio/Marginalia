package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.ui.main.Viewer;
import com.github.enerccio.marginalia.ui.widgets.HistoryLimitField;
import com.github.enerccio.marginalia.ui.widgets.InheritedLanguageCombo;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TagMultiComboBox;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable
@Extendable
/**
 * The about tab of the {@link PhoneManuscriptDialog}: a single column of fields, the statistics are plain lines.
 */
public class PhoneManuscriptInfoPart implements PhoneManuscriptDialogPart {

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

    private final PhoneManuscriptDialog parent;

    private TextField nameField;
    private TagMultiComboBox tags;
    private TextArea descriptionField;
    private InheritedLanguageCombo languageCombo;
    private HistoryLimitField historyLimitField;
    private ComboBox<AI> aiCombo;
    private ComboBox<Protocol> protocolCombo;
    private Checkbox publishedCheckbox;

    private Span totalWordCountSpan;
    private Span totalTokenCountSpan;
    private Span branchWordCountSpan;
    private Span branchTokenCountSpan;

    private boolean loading = false;

    public PhoneManuscriptInfoPart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "statistics", type = VerticalLayout.class)})
    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.addClassName(CssStyles.OVERFLOW_AUTO);

        createFields();

        VerticalLayout statistics = new VerticalLayout(totalWordCountSpan, totalTokenCountSpan, branchWordCountSpan, branchTokenCountSpan);
        statistics.setWidthFull();
        statistics.setPadding(false);
        statistics.setSpacing(false);
        statistics.addClassName(CssStyles.SUBTLE_BOX);

        mainLayout.add(nameField, tags, descriptionField, languageCombo, historyLimitField, aiCombo, protocolCombo, publishedCheckbox, statistics);
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void createFields() {
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setWidthFull();
        nameField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                if (StringUtils.isBlank(e.getValue())) {
                    nameField.setInvalid(true);
                    nameField.setErrorMessage(loc.getValue(L.ERROR_NAME_CANT_BE_EMPTY));
                } else {
                    nameField.setInvalid(false);
                    nameField.setErrorMessage("");
                }
                autosave();
            }
        });

        tags = new TagMultiComboBox(loc.getValue(L.LABEL_TAGS));

        descriptionField = new TextArea(loc.getValue(L.LABEL_DESCRIPTION));
        descriptionField.setWidthFull();
        ResizableTextArea.install(loc, descriptionField, "160px");
        descriptionField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        languageCombo = new InheritedLanguageCombo(loc.getValue(L.LABEL_LANGUAGE));
        languageCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        historyLimitField = new HistoryLimitField(loc.getValue(L.LABEL_HISTORY_MAX_VERSIONS));
        historyLimitField.addValueChangeListener(e -> {
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

        publishedCheckbox = new Checkbox(loc.getValue(L.LABEL_PUBLISHED));
        publishedCheckbox.setHelperComponent(new Span(loc.getValue(L.MSG_PUBLISHED_HELP)));
        publishedCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
            if (e.getValue()) {
                String relativeUrl = RouteConfiguration.forSessionScope().getUrl(Viewer.class) + "/" + parent.getManuscript().getUuid();
                RouterLink routerLink = new RouterLink(relativeUrl, Viewer.class, parent.getManuscript().getUuid());
                VerticalLayout verticalLayout = new VerticalLayout(new Span(loc.getValue(L.MSG_PUBLISHED_HELP)),
                        routerLink);
                verticalLayout.setWidthFull();
                verticalLayout.setSpacing(false);
                verticalLayout.setPadding(false);
                verticalLayout.setMargin(false);
                publishedCheckbox.setHelperComponent(verticalLayout);
                UI.getCurrent().getPage().fetchCurrentURL(url -> {
                    String baseUri = url.getProtocol() + "://" + url.getAuthority();
                    String fullPath = baseUri + VaadinRequest.getCurrent().getContextPath() + "/" + relativeUrl;
                    routerLink.setText(fullPath);
                });
            } else {
                publishedCheckbox.setHelperComponent(new Span(loc.getValue(L.MSG_PUBLISHED_HELP)));
            }
        });

        totalWordCountSpan = new Span();
        totalTokenCountSpan = new Span();
        branchWordCountSpan = new Span();
        branchTokenCountSpan = new Span();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    private Manuscript refreshModel() throws Exception {
        return parent.refreshManuscript();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void autosave() {
        if (loading) {
            return;
        }
        try {
            Manuscript manuscript = refreshModel();
            if (manuscript == null) {
                return;
            }

            if (StringUtils.isNotBlank(nameField.getValue()))
                manuscript.setName(nameField.getValue());
            manuscript.setDescription(descriptionField.getValue());
            manuscript.setLanguage(languageCombo.getValue());
            manuscript.setHistoryMaxVersions(historyLimitField.getValue());
            manuscript.setAi(aiCombo.getValue());
            manuscript.setProtocol(protocolCombo.getValue());
            manuscript.setPublished(Boolean.TRUE.equals(publishedCheckbox.getValue()));

            manuscript = parent.save();
            parent.setHeaderTitle(manuscript.getName());
            updateSyntheticFields(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    private void updateSyntheticFields(Manuscript manuscript) throws Exception {
        int totalWords = 0;
        int totalTokens = 0;
        int branchWords = 0;
        long branchTokens = 0;
        if (manuscript != null && manuscript.getId() != null) {
            totalWords = chatMessageService.getTotalWordCount(manuscript);
            totalTokens = chatMessageService.getTotalTokenCount(manuscript);
            branchWords = chatMessageService.getBranchWordCount(manuscript.getActiveLeaf());
            branchTokens = chatMessageService.getBranchTokenCount(manuscript.getActiveLeaf());
        }

        totalWordCountSpan.setText(loc.getValue(L.LABEL_TOTAL_WORD_COUNT) + ": " + totalWords);
        totalTokenCountSpan.setText(loc.getValue(L.LABEL_TOTAL_TOKEN_COUNT) + ": " + totalTokens);
        branchWordCountSpan.setText(loc.getValue(L.LABEL_BRANCH_WORD_COUNT) + ": " + branchWords);
        branchTokenCountSpan.setText(loc.getValue(L.LABEL_BRANCH_TOKEN_COUNT) + ": " + branchTokens);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "frozen", type = boolean.class)})
    @Override
    public void setFrozen(boolean frozen) {

    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "models", type = List.class),
                    @APILocals(name = "protocols", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
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
            publishedCheckbox.setValue(manuscript.isPublished());
            descriptionField.setValue(StringUtils.defaultString(manuscript.getDescription()));
            languageCombo.load(manuscript.getOwnLanguage(), manuscript.getFolder());
            historyLimitField.load(manuscript.getHistoryMaxVersions(), manuscriptService.resolveInheritedHistoryLimit(manuscript.getFolder()));
            aiCombo.setValue(manuscript.getAi());
            protocolCombo.setValue(manuscript.getProtocol());
            tags.setForEntity(manuscript);

            updateSyntheticFields(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabLeave() throws Exception {

    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabEnter() throws Exception {
        updateSyntheticFields(parent.getManuscript());
    }
}