package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.*;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryHost;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
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
@Extendable
public class ManuscriptDialog extends Dialog implements StoryHost {

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
    private final ManuscriptTreePart treePart = new ManuscriptTreePart(this);
    private Component treePartComponent;
    private final ManuscriptBackupPart backupPart = new ManuscriptBackupPart(this);
    private Component backupPartComponent;
    private final Map<Component, ManuscriptDialogPart> c2p = new HashMap<>();

    public ManuscriptDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "footerLayout", type = HorizontalLayout.class)})
    public void create() throws Exception {
        ai = aiService.find(manuscript.getAi());
        setHeaderTitle(manuscript.getName());
        setSizeFull();
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);
        addThemeVariants(DialogVariant.NO_PADDING);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.addClassName(CssStyles.OVERFLOW_HIDDEN);
        mainLayout.setPadding(false);
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
        treePartComponent = treePart.create(tabs);
        c2p.put(treePartComponent, treePart);
        backupPartComponent = backupPart.create(tabs);
        c2p.put(backupPartComponent, backupPart);

        parts.add(infoPart);
        parts.add(promptPart);
        parts.add(lorebookPart);
        parts.add(storyPart);
        parts.add(treePart);
        parts.add(backupPart);

        mainLayout.add(tabs);
        mainLayout.setFlexGrow(1, tabs);

        add(mainLayout);

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        exitButton = new Button(loc.getValue(L.LABEL_EXIT), event -> {
            if (onClose != null)
                onClose.run();
            close();
        });

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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "part", type = ManuscriptDialogPart.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void open() {
        super.open();
        try {
            refreshManuscript();
            save();
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void selectStoryPart() {
        if (tabs != null && storyPartComponent != null) {
            tabs.setSelectedTab(tabs.getTab(storyPartComponent));
        }
    }

    /**
     * Switches to the story tab and scrolls to the part. The part has to be on the active branch.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "messageId", type = Long.class)})
    public void showMessage(Long messageId) {
        storyPart.scrollToMessageOnNextRender(messageId);
        selectStoryPart();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void freeze() {
        if (!frozen) {
            this.frozen = true;
            updateFreezeStatus();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void unfreeze() {
        if (frozen) {
            this.frozen = false;
            updateFreezeStatus();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "part", type = ManuscriptDialogPart.class)})
    private void updateFreezeStatus() {
        exitButton.setEnabled(!frozen);
        for (int i=0; i<tabs.getTabCount(); i++)
            tabs.getTabAt(i).setEnabled(!frozen);
        for (ManuscriptDialogPart part : parts) {
            part.setFrozen(frozen);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    public Manuscript getManuscript() {
        return manuscript;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    public Manuscript refreshManuscript() throws Exception {
        return manuscript = manuscriptService.find(manuscript);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    public Manuscript save() throws Exception {
        return manuscript = manuscriptService.save(manuscript);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Runnable.class))
    public Runnable getOnClose() {
        return onClose;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onClose", type = Runnable.class)})
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = AI.class))
    public AI getAi() {
        return ai;
    }
}