package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Copies the definition and the files of an agent to a new agent of another book. The number of agents a user may
 * have is limited by the administrator, so the service may refuse.
 */
@Configurable
public class AgentCloneDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final Agent source;
    private Runnable onSave;
    private TextField nameField;
    private ComboBox<Manuscript> manuscriptCombo;
    private Button cloneButton;

    public AgentCloneDialog(Agent source) {
        this.source = source;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_CLONE_AGENT));
        setWidth("460px");
        UIUtils.fitPhone(this, false);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        nameField = new TextField(loc.getValue(L.LABEL_NAME), source.getName(), "");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();

        manuscriptCombo = new ComboBox<>(loc.getValue(L.LABEL_BOOK));
        manuscriptCombo.setRequiredIndicatorVisible(true);
        manuscriptCombo.setItemLabelGenerator(Manuscript::getName);
        manuscriptCombo.setWidthFull();

        VerticalLayout content = new VerticalLayout(nameField, manuscriptCombo);
        content.setPadding(false);
        add(content);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        cloneButton = new Button(loc.getValue(L.LABEL_CLONE_AGENT), event -> cloneAgent());
        cloneButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, cloneButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        try {
            // a clone works on another book: the books without an agent
            List<Manuscript> books = new ArrayList<>();
            for (Manuscript manuscript : manuscriptService.findAllRegularForUser()) {
                if (agentService.findForManuscript(manuscript) == null) {
                    books.add(manuscript);
                }
            }
            books.sort(Comparator.comparing(Manuscript::getName, loc.createLocaleComparator()));
            manuscriptCombo.setItems(books);
            if (books.size() == 1) {
                manuscriptCombo.setValue(books.getFirst());
            }
            if (books.isEmpty()) {
                manuscriptCombo.setHelperText(loc.getValue(L.MSG_AGENT_NO_FREE_BOOK));
                cloneButton.setEnabled(false);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void cloneAgent() {
        if (StringUtils.isBlank(nameField.getValue()) || manuscriptCombo.getValue() == null) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }
        try {
            agentService.cloneAgent(source, nameField.getValue(), manuscriptCombo.getValue());
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            Notification.error(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            return;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        close();
        if (onSave != null) {
            onSave.run();
        }
    }

    // for the tests of the dialog

    TextField getNameField() {
        return nameField;
    }

    ComboBox<Manuscript> getManuscriptCombo() {
        return manuscriptCombo;
    }

    Button getCloneButton() {
        return cloneButton;
    }
}
