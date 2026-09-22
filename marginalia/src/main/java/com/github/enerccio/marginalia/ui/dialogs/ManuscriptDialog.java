package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.ArrayList;
import java.util.List;

@Configurable
public class ManuscriptDialog extends Dialog {

    @Autowired
    protected Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;

    private Manuscript manuscript;
    private Runnable onClose;
    private boolean frozen;

    private Button exitButton;
    private VTabSheet tabs;
    private final List<ManuscriptDialogPart> parts = new ArrayList<>();
    private final ManuscriptInfoPart infoPart = new ManuscriptInfoPart(this);
    private Component infoPartComponent;
    private final ManuscriptPromptPart promptPart = new ManuscriptPromptPart(this);
    private Component promptPartComponent;
    private final ManuscriptLorebookPart lorebookPart = new ManuscriptLorebookPart(this);
    private Component lorebookPartComponent;
    private final ManuscriptStoryPart storyPart = new ManuscriptStoryPart(this);
    private Component storyPartComponent;

    public ManuscriptDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    public void create() throws Exception {
        setHeaderTitle(manuscript.getName());
        setSizeFull();
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        tabs = new VTabSheet();
        tabs.setSizeFull();

        infoPartComponent = infoPart.create(tabs);
        promptPartComponent = promptPart.create(tabs);
        lorebookPartComponent = lorebookPart.create(tabs);
        storyPartComponent = storyPart.create(tabs);

        parts.add(infoPart);
        parts.add(promptPart);
        parts.add(lorebookPart);
        parts.add(storyPart);

        mainLayout.add(tabs);
        mainLayout.setFlexGrow(1, tabs);

        add(mainLayout);

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        exitButton = new Button(loc.getValue(L.LABEL_EXIT), event -> close());

        footerLayout.add(exitButton);
        getFooter().add(footerLayout);
    }

    @Override
    public void open() {
        super.open();
        try {
            refreshManuscript();
            if (chatMessageService.hasAnyMessages(manuscript)) {
                tabs.setSelectedTab(tabs.getTab(storyPartComponent));
            } else {
                tabs.setSelectedTab(tabs.getTab(infoPartComponent));
            }
            for (ManuscriptDialogPart part : parts) {
                part.load(getManuscript());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    public void freeze() {
        if (!frozen) {
            this.frozen = true;
            updateFreezeStatus();
        }
    }

    public void unfreeze() {
        if (frozen) {
            this.frozen = false;
            updateFreezeStatus();
        }
    }

    private void updateFreezeStatus() {
        exitButton.setEnabled(!frozen);
        for (int i=0; i<tabs.getTabCount(); i++)
            tabs.getTabAt(i).setEnabled(!frozen);
        for (ManuscriptDialogPart part : parts) {
            part.setFrozen(frozen);
        }
    }

    public Manuscript getManuscript() {
        return manuscript;
    }

    public Manuscript refreshManuscript() throws Exception {
        return manuscript = manuscriptService.find(manuscript);
    }

    public Manuscript save() throws Exception {
        return manuscript = manuscriptService.save(manuscript);
    }

    public Runnable getOnClose() {
        return onClose;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
}