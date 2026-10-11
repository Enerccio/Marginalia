package com.github.enerccio.marginalia.ui.dialogs.phone;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The book dialog for phones: the {@link com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog} laid out for a
 * small screen. It fills the whole screen, the exit button is an icon in the header (no footer) and the tabs are a
 * single scrolling row. Every tab is a phone specific part from {@code ui.dialogs.phone.manuscript}. It is a separate
 * class and not a subclass of the desktop dialog, so extensions know which of them they decorate.
 */
@Configurable
@Extendable
public class PhoneManuscriptDialog extends Dialog {

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
    private Tabs tabs;
    private final List<PhoneManuscriptDialogPart> parts = new ArrayList<>();
    private final PhoneManuscriptInfoPart infoPart = new PhoneManuscriptInfoPart(this);
    private final PhoneManuscriptPromptPart promptPart = new PhoneManuscriptPromptPart(this);
    private final PhoneManuscriptLorebookPart lorebookPart = new PhoneManuscriptLorebookPart(this);
    private final PhoneManuscriptStoryPart storyPart = new PhoneManuscriptStoryPart(this);
    private final PhoneManuscriptTreePart treePart = new PhoneManuscriptTreePart(this);
    private final PhoneManuscriptBackupPart backupPart = new PhoneManuscriptBackupPart(this);
    private Tab infoTab;
    private Tab storyTab;
    private Component storyPartComponent;
    private final Map<Tab, Component> tabToContent = new HashMap<>();
    private final Map<Tab, PhoneManuscriptDialogPart> tabToPart = new HashMap<>();

    public PhoneManuscriptDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    public void create() throws Exception {
        ai = aiService.find(manuscript.getAi());
        setHeaderTitle(manuscript.getName());
        setSizeFull();
        addClassName(CssStyles.PHONE_DIALOG);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setDraggable(false);
        setResizable(false);
        setModality(ModalityMode.STRICT);
        addThemeVariants(DialogVariant.NO_PADDING);

        exitButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (onClose != null)
                onClose.run();
            close();
        });
        exitButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        UIUtils.addTooltip(exitButton, loc.getValue(L.LABEL_EXIT));
        getHeader().add(exitButton);

        tabs = new Tabs();
        tabs.setWidthFull();
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL, TabsVariant.LUMO_HIDE_SCROLL_BUTTONS);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName(CssStyles.OVERFLOW_HIDDEN);

        infoTab = addPart(L.LABEL_INFO_PART, infoPart, content);
        addPart(L.LABEL_PROMPT_PART, promptPart, content);
        addPart(L.LABEL_LOREBOOK, lorebookPart, content);
        storyTab = addPart(L.LABEL_STORY_PART, storyPart, content);
        storyPartComponent = tabToContent.get(storyTab);
        addPart(L.LABEL_BRANCH_TREE, treePart, content);
        addPart(L.LABEL_BACKUPS, backupPart, content);

        VerticalLayout mainLayout = new VerticalLayout(tabs, content);
        mainLayout.setSizeFull();
        mainLayout.addClassName(CssStyles.OVERFLOW_HIDDEN);
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.setFlexGrow(1, content);
        add(mainLayout);

        tabs.addSelectedChangeListener(event -> {
            PhoneManuscriptDialogPart leavingPart = tabToPart.get(event.getPreviousTab());
            if (leavingPart != null) {
                try {
                    leavingPart.onTabLeave();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }
            tabToContent.forEach((tab, component) -> component.setVisible(tab == event.getSelectedTab()));
            PhoneManuscriptDialogPart enteringPart = tabToPart.get(event.getSelectedTab());
            if (enteringPart != null) {
                try {
                    enteringPart.onTabEnter();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }
        });
        // only the content of the selected tab is shown; selecting the already selected tab fires no event
        tabToContent.forEach((tab, component) -> component.setVisible(tab == tabs.getSelectedTab()));
    }

    private Tab addPart(L label, PhoneManuscriptDialogPart part, VerticalLayout content) throws Exception {
        Component component = part.create();
        Tab tab = new Tab(loc.getValue(label));
        tabs.add(tab);
        content.add(component);
        content.setFlexGrow(1, component);
        tabToContent.put(tab, component);
        tabToPart.put(tab, part);
        parts.add(part);
        return tab;
    }

    @Override
    public void open() {
        super.open();
        try {
            refreshManuscript();
            save();
            if (chatMessageService.hasAnyMessages(manuscript)) {
                tabs.setSelectedTab(storyTab);
            } else {
                tabs.setSelectedTab(infoTab);
            }
            for (PhoneManuscriptDialogPart part : parts) {
                part.load(getManuscript());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    public void selectStoryPart() {
        if (tabs != null && storyPartComponent != null) {
            tabs.setSelectedTab(storyTab);
        }
    }

    /**
     * Switches to the story tab and scrolls to the part. The part has to be on the active branch.
     */
    public void showMessage(Long messageId) {
        storyPart.scrollToMessageOnNextRender(messageId);
        selectStoryPart();
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
        tabs.getChildren().filter(Tab.class::isInstance).map(Tab.class::cast).forEach(tab -> tab.setEnabled(!frozen));
        for (PhoneManuscriptDialogPart part : parts) {
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
