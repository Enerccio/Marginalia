package com.github.enerccio.marginalia.ui.dialogs.phone;

import com.github.enerccio.marginalia.ui.dialogs.BookWindows;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.GenerationHold;
import com.github.enerccio.marginalia.ui.components.RunsOnPage;
import com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
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
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * The book dialog for phones: the {@link com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog} laid out for a
 * small screen. It fills the whole screen, the exit button is an icon in the header (no footer) and the tabs are a
 * single scrolling row. Every tab is a phone specific part from {@code ui.dialogs.phone.manuscript}. It is a separate
 * class and not a subclass of the desktop dialog, so extensions know which of them they decorate.
 */
@Configurable
@Extendable
public class PhoneManuscriptDialog extends Dialog implements ManuscriptWindow {

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
    private Runnable onStateChange;
    // held by freeze() from the click that starts a generation until unfreeze(); the run of the generation says what is held meanwhile
    private boolean frozen;
    // what a generation of the book holds in the window now, null when none runs
    private GenerationHold hold;
    private Registration runsRegistration;

    private Button exitButton;
    private Tabs tabs;
    private final List<PhoneManuscriptDialogPart> parts = new ArrayList<>();
    private final PhoneManuscriptInfoPart infoPart = new PhoneManuscriptInfoPart(this);
    private final PhoneManuscriptPromptPart promptPart = new PhoneManuscriptPromptPart(this);
    private final PhoneManuscriptLorebookPart lorebookPart = new PhoneManuscriptLorebookPart(this);
    private final PhoneManuscriptStoryPart storyPart = new PhoneManuscriptStoryPart(this);
    private final PhoneManuscriptTreePart treePart = new PhoneManuscriptTreePart(this);
    private final PhoneManuscriptBackupPart backupPart = new PhoneManuscriptBackupPart(this);
    // names of the tabs in the stored state, the same as in the desktop dialog
    private final Map<String, PhoneManuscriptDialogPart> partsByKey = new LinkedHashMap<>();
    {
        partsByKey.put("info", infoPart);
        partsByKey.put("prompt", promptPart);
        partsByKey.put("lorebook", lorebookPart);
        partsByKey.put("story", storyPart);
        partsByKey.put("tree", treePart);
        partsByKey.put("backup", backupPart);
    }
    private Tab infoTab;
    private Tab storyTab;
    private Component storyPartComponent;
    private final Map<Tab, Component> tabToContent = new HashMap<>();
    private final Map<Tab, PhoneManuscriptDialogPart> tabToPart = new HashMap<>();

    public PhoneManuscriptDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
        // one window per book: opened in another page, it closes here as by its exit button
        BookWindows.hold(this, () -> this.manuscript, () -> {
            if (onClose != null) {
                onClose.run();
            }
            close();
        });
        // the window follows the generation of its book, whoever started it (the story, a command, a plugin)
        addAttachListener(event -> {
            runsRegistration = RunsOnPage.listen(event.getUI(), this::followGeneration);
            followGeneration();
        });
        addDetachListener(event -> {
            if (runsRegistration != null) {
                runsRegistration.remove();
                runsRegistration = null;
            }
        });
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "content", type = VerticalLayout.class),
                    @APILocals(name = "mainLayout", type = VerticalLayout.class)})
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

        exitButton = new Button(VaadinIcon.CLOSE.create(), event -> leave());
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
            stateChanged();
        });
        // only the content of the selected tab is shown; selecting the already selected tab fires no event
        tabToContent.forEach((tab, component) -> component.setVisible(tab == tabs.getSelectedTab()));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Tab.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "part", type = PhoneManuscriptDialogPart.class),
                    @APIParameter(name = "content", type = VerticalLayout.class)},
            locals = {@APILocals(name = "component", type = Component.class),
                    @APILocals(name = "tab", type = Tab.class)})
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "part", type = PhoneManuscriptDialogPart.class),
                    @APILocals(name = "e", type = Exception.class)})
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void selectStoryPart() {
        if (tabs != null && storyPartComponent != null) {
            tabs.setSelectedTab(storyTab);
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

    /**
     * Holds the whole window: a generation is being started. What it holds from then on comes from its run ({@link #followGeneration}).
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void freeze() {
        if (!frozen) {
            this.frozen = true;
            followGeneration();
        }
    }

    /**
     * The generation the window started is over and shown.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void unfreeze() {
        if (frozen) {
            this.frozen = false;
            followGeneration();
        }
    }

    /**
     * Reads what the generation of the book holds now and gives it to the tabs when it changed.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "generation", type = Runs.Run.class),
                    @APILocals(name = "next", type = GenerationHold.class)})
    private void followGeneration() {
        Runs.Run generation = manuscript == null ? null : Runs.generationOf(manuscript.getId());
        GenerationHold next = generation != null ? GenerationHold.of(generation) : frozen ? GenerationHold.WHOLE : null;
        if (!Objects.equals(next, hold)) {
            hold = next;
            updateFreezeStatus();
        }
    }

    /**
     * What a generation of the book holds in the window now, null when none runs.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = GenerationHold.class))
    public GenerationHold getHold() {
        return hold;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "part", type = PhoneManuscriptDialogPart.class)})
    private void updateFreezeStatus() {
        if (tabs == null) {
            return;
        }
        boolean whole = hold != null && hold.isWhole();
        tabs.getChildren().filter(Tab.class::isInstance).map(Tab.class::cast).forEach(tab -> tab.setEnabled(!whole));
        for (PhoneManuscriptDialogPart part : parts) {
            part.setHold(hold);
        }
        // closing the tab stops the generation: the browser asks first
        JavascriptLibrary.warnOnLeave(getElement(), hold != null);
    }

    /**
     * The exit button: while a generation of the book runs, leaving stops it, so the author is asked first.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "generation", type = Runs.Run.class)})
    private void leave() {
        Runs.Run generation = manuscript == null ? null : Runs.generationOf(manuscript.getId());
        if (generation == null && hold == null) {
            closeWindow();
            return;
        }
        ConfirmDialog.show(loc.getValue(L.MSG_LEAVE_STOPS_GENERATION), () -> {
            if (generation != null) {
                // the Stop the author knows: what arrived is kept
                generation.stop();
            }
            closeWindow();
        });
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void closeWindow() {
        if (onClose != null) {
            onClose.run();
        }
        close();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    public Manuscript getManuscript() {
        return manuscript;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "refreshed", type = Manuscript.class),
                    @APILocals(name = "part", type = PhoneManuscriptDialogPart.class)})
    public void reload() throws Exception {
        Manuscript refreshed = refreshManuscript();
        for (PhoneManuscriptDialogPart part : parts) {
            part.load(refreshed);
        }
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

    /**
     * Called after the user navigated or changed what {@link #storeState()} stores, passes it on to the workspace
     * autosave.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onStateChange", type = Runnable.class)})
    public void setOnStateChange(Runnable onStateChange) {
        this.onStateChange = onStateChange;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    public void stateChanged() {
        if (onStateChange != null) {
            onStateChange.run();
        }
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "selected", type = Tab.class)})
    @Override
    public String getLLMState() {
        Tab selected = tabs == null ? null : tabs.getSelectedTab();
        return "The story editor of the book \"" + manuscript.getName() + "\" in the phone layout, a full screen dialog "
                + "with a row of tabs (book settings, prompt, lorebook, story, tree, backup). The open tab is **"
                + (selected == null ? "" : selected.getLabel()) + "**."
                + (hold == null ? "" : hold.isWhole() ? " A part is being generated now and its prompt is prepared: the tabs are locked."
                : " A part is being generated now: that part and the parts before it cannot be deleted, merged or moved; the rest can be edited.");
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = Map.class),
            locals = {@APILocals(name = "all", type = List.class)})
    @Override
    public Map<String, LLMInteraction> getInteraction() {
        if (tabs == null) {
            return Map.of();
        }
        List<Tab> all = tabs.getChildren().filter(Tab.class::isInstance).map(Tab.class::cast).toList();
        return LLMInteract.tabs(all, tabs.getSelectedTab(), tabs::setSelectedTab);
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "deep", type = boolean.class)},
            locals = {@APILocals(name = "selected", type = PhoneManuscriptDialogPart.class),
                    @APILocals(name = "components", type = Map.class),
                    @APILocals(name = "part", type = PhoneManuscriptDialogPart.class)})
    @Override
    public List<LLMInteract> getLLMChildren(boolean deep) {
        PhoneManuscriptDialogPart selected = tabs == null ? null : tabToPart.get(tabs.getSelectedTab());
        Map<PhoneManuscriptDialogPart, Component> components = new LinkedHashMap<>();
        for (PhoneManuscriptDialogPart part : parts) {
            tabToPart.forEach((tab, candidate) -> {
                if (candidate == part) {
                    components.put(part, tabToContent.get(tab));
                }
            });
        }
        return LLMInteract.parts(components, selected, deep);
    }

    /**
     * The book, the selected tab and the state of the tabs that have some.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "parts", type = JsonObject.class),
                    @APILocals(name = "selected", type = PhoneManuscriptDialogPart.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        state.addProperty("manuscript", manuscript.getUuid());
        JsonObject parts = new JsonObject();
        PhoneManuscriptDialogPart selected = tabs == null ? null : tabToPart.get(tabs.getSelectedTab());
        partsByKey.forEach((key, part) -> {
            if (part == selected) {
                state.addProperty("tab", key);
            }
            RestorableState.put(parts, key, part.storeState());
        });
        RestorableState.put(state, "parts", parts);
        return state;
    }

    /**
     * Restores the tabs and selects the stored one. To be called after {@link #open()}, the tabs are loaded then.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "parts", type = JsonObject.class),
                    @APILocals(name = "part", type = PhoneManuscriptDialogPart.class)})
    @Override
    public void restoreState(JsonObject self) {
        JsonObject parts = RestorableState.object(self, "parts");
        partsByKey.forEach((key, tabPart) -> {
            JsonObject partState = RestorableState.object(parts, key);
            if (partState != null) {
                tabPart.restoreState(partState);
            }
        });

        PhoneManuscriptDialogPart part = partsByKey.get(RestorableState.string(self, "tab"));
        if (part != null) {
            tabToPart.forEach((tab, candidate) -> {
                if (candidate == part) {
                    tabs.setSelectedTab(tab);
                }
            });
        }
    }
}
