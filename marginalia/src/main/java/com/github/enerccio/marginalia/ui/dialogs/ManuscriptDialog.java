package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class ManuscriptDialog extends Dialog {

    @Autowired
    protected Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AIService aiService;

    private Manuscript manuscript;
    private AI ai;
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
    private final Map<Component, ManuscriptDialogPart> c2p = new HashMap<>();

    public ManuscriptDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    public void create() throws Exception {
        ai = aiService.find(manuscript.getAi());
        setHeaderTitle(manuscript.getName());
        setWidth("95%");
        setHeight("95%");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.getStyle().set("overflow", "hidden");
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);

        tabs = new VTabSheet();
        tabs.setSizeFull();

        infoPartComponent = infoPart.create(tabs);
        c2p.put(infoPartComponent, infoPart);
        promptPartComponent = promptPart.create(tabs);
        c2p.put(promptPartComponent, promptPart);
        lorebookPartComponent = lorebookPart.create(tabs);
        c2p.put(lorebookPartComponent, lorebookPart);
        storyPartComponent = storyPart.create(tabs);
        c2p.put(storyPartComponent, storyPart);

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

        tabs.addSelectedChangeListener(event -> {
            ManuscriptDialogPart leavingPart = c2p.get(tabs.getComponent(event.getPreviousTab()));
            if (leavingPart != null) {
                try {
                    leavingPart.onTabLeave();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }
            ManuscriptDialogPart enteringPart = c2p.get(tabs.getComponent(event.getSelectedTab()));
            if (enteringPart != null) {
                try {
                    enteringPart.onTabEnter();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }
        });
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

    public AI getAi() {
        return ai;
    }
}