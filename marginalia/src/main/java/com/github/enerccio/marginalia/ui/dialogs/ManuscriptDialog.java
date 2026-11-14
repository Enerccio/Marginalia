package com.github.enerccio.marginalia.ui.dialogs;

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
import com.github.enerccio.marginalia.ui.dialogs.manuscript.*;
import com.github.enerccio.marginalia.ui.components.RunsOnPage;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.*;

@Configurable
@Extendable
public class ManuscriptDialog extends Dialog implements ManuscriptWindow {

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
    // names of the tabs in the stored state
    private final Map<String, ManuscriptDialogPart> partsByKey = new LinkedHashMap<>();
    {
        partsByKey.put("info", infoPart);
        partsByKey.put("prompt", promptPart);
        partsByKey.put("lorebook", lorebookPart);
        partsByKey.put("story", storyPart);
        partsByKey.put("tree", treePart);
        partsByKey.put("backup", backupPart);
    }

    public ManuscriptDialog(Manuscript manuscript) {
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
        addClassName(CssStyles.MANUSCRIPT_DIALOG);

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

        exitButton = new Button(loc.getValue(L.LABEL_EXIT), event -> leave());

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
            stateChanged();
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
            // the story may be behind another tab of its sheet
            storyPart.showStory();
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
            locals = {@APILocals(name = "part", type = ManuscriptDialogPart.class)})
    private void updateFreezeStatus() {
        if (tabs == null) {
            return;
        }
        boolean whole = hold != null && hold.isWhole();
        for (int i=0; i<tabs.getTabCount(); i++)
            tabs.getTabAt(i).setEnabled(!whole);
        for (ManuscriptDialogPart part : parts) {
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
                    @APILocals(name = "part", type = ManuscriptDialogPart.class)})
    public void reload() throws Exception {
        Manuscript refreshed = refreshManuscript();
        for (ManuscriptDialogPart part : parts) {
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

    /**
     * The book, the selected tab and the state of the tabs that have some.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "parts", type = JsonObject.class),
                    @APILocals(name = "selected", type = ManuscriptDialogPart.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        state.addProperty("manuscript", manuscript.getUuid());
        JsonObject parts = new JsonObject();
        ManuscriptDialogPart selected = tabs == null || tabs.getSelectedTab() == null ? null : c2p.get(tabs.getComponent(tabs.getSelectedTab()));
        partsByKey.forEach((key, part) -> {
            if (part == selected) {
                state.addProperty("tab", key);
            }
            RestorableState.put(parts, key, part.storeState());
        });
        RestorableState.put(state, "parts", parts);
        return state;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "selected", type = Tab.class)})
    @Override
    public String getLLMState() {
        Tab selected = tabs == null ? null : tabs.getSelectedTab();
        return "The story editor of the book \"" + manuscript.getName() + "\", a full screen dialog with tabs "
                + "(book settings, prompt, lorebook, story, tree, backup). The open tab is **"
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
        List<Tab> all = new ArrayList<>();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            all.add(tabs.getTabAt(i));
        }
        return LLMInteract.tabs(all, tabs.getSelectedTab(), tabs::setSelectedTab);
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "deep", type = boolean.class)},
            locals = {@APILocals(name = "selected", type = ManuscriptDialogPart.class),
                    @APILocals(name = "components", type = Map.class),
                    @APILocals(name = "part", type = ManuscriptDialogPart.class)})
    @Override
    public List<LLMInteract> getLLMChildren(boolean deep) {
        ManuscriptDialogPart selected = tabs == null || tabs.getSelectedTab() == null ? null : c2p.get(tabs.getComponent(tabs.getSelectedTab()));
        Map<ManuscriptDialogPart, Component> components = new LinkedHashMap<>();
        for (ManuscriptDialogPart part : parts) {
            c2p.forEach((component, candidate) -> {
                if (candidate == part) {
                    components.put(part, component);
                }
            });
        }
        return LLMInteract.parts(components, selected, deep);
    }

    /**
     * Restores the tabs and selects the stored one. To be called after {@link #open()}, the tabs are loaded then.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "parts", type = JsonObject.class),
                    @APILocals(name = "part", type = ManuscriptDialogPart.class)})
    @Override
    public void restoreState(JsonObject self) {
        JsonObject parts = RestorableState.object(self, "parts");
        partsByKey.forEach((key, tabPart) -> {
            JsonObject partState = RestorableState.object(parts, key);
            if (partState != null) {
                tabPart.restoreState(partState);
            }
        });

        ManuscriptDialogPart part = partsByKey.get(RestorableState.string(self, "tab"));
        if (part != null) {
            c2p.forEach((component, candidate) -> {
                if (candidate == part) {
                    tabs.setSelectedTab(tabs.getTab(component));
                }
            });
        }
    }
}