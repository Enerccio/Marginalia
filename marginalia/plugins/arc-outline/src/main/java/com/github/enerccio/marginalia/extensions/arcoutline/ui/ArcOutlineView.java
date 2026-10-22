package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Arcs tab of the story sidebar: the arcs of the book with their chapters, colored by their state. The outline is
 * kept in memory while the tab is open and stored in the book after every change.
 */
@Configurable(preConstruction = true)
public class ArcOutlineView extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final ArcOutlineService service;
    private final Manuscript manuscript;
    private ArcOutline outline;
    /** Removes the listener that reads the outline again when a command changes it, set while the view is attached. */
    private Runnable changeRegistration;
    /**
     * Uuids of the arcs whose chapters are hidden; kept here because the list is rebuilt after every change.
     */
    private final Set<String> collapsed = new HashSet<>();
    /**
     * Uuids of the chapters whose note is shown under the description.
     */
    private final Set<String> notesShown = new HashSet<>();

    private final Div list = new Div();

    public ArcOutlineView(Manuscript manuscript, ArcOutlineService service) {
        this.manuscript = manuscript;
        this.service = service;
        this.outline = loadOutline();

        setSizeFull();
        // the list goes to the edges of the tab, the button above it keeps its padding
        setPadding(false);
        buildUI();
        refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // a command (the command bar, an agent) stores the outline behind the back of the view, which keeps it in memory
        changeRegistration = service.onChange(manuscript, () -> UIUtils.accessComponent(this, () -> {
            outline = loadOutline();
            refresh();
        }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (changeRegistration != null) {
            changeRegistration.run();
            changeRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    private ArcOutline loadOutline() {
        try {
            // the story view keeps the book it was opened with, the outline may have been saved since
            return service.load(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return service.read(manuscript);
        }
    }

    private void buildUI() {
        Button addArc = new Button("Add new arc", Solid.PLUS.create(), _ -> {
            Arc arc = new Arc();
            new OutlineNodeDialog("New arc", arc, () -> {
                outline.add(arc);
                persist();
            }).open();
        });
        addArc.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // the wand: a new arc from the specification, or one summarizing the written story
        Button generate = new Button(Solid.WAND_MAGIC_SPARKLES.create());
        generate.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        generate.setAriaLabel("Generate an arc");
        generate.setTooltipText("Generate an arc");
        ContextMenu generateMenu = new ContextMenu(generate);
        generateMenu.setOpenOnClick(true);
        generateMenu.addItem(UIUtils.menuItemWithIcon(Solid.WAND_MAGIC_SPARKLES.create(), "Generate an arc"), _ ->
                new ArcGeneratorDialog(service, manuscript, outline, this::addGenerated).open());
        generateMenu.addItem(UIUtils.menuItemWithIcon(Solid.FILE_IMPORT.create(), "Import from story"), _ -> importFromStory());

        HorizontalLayout buttons = new HorizontalLayout(addArc, generate);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setPadding(true);

        list.addClassName(ArcOutlineStyles.LIST);
        add(buttons, list);
        setFlexGrow(1, list);
    }

    private void addGenerated(Arc arc) {
        outline.add(arc);
        persist();
    }

    /**
     * Opens the dialog that summarizes messages of the active branch of the book as an arc.
     */
    private void importFromStory() {
        List<ArcOutlineService.StoryMessage> messages;
        try {
            // the branch as it is now, it may have been changed since the story was opened
            messages = service.activeBranch(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        if (messages.isEmpty()) {
            Notification.warning("The active branch has no text to import");
            return;
        }
        new StoryImportDialog(service, manuscript, outline, messages, this::addGenerated).open();
    }

    private void refresh() {
        list.removeAll();
        if (outline.arcs().isEmpty()) {
            Div empty = new Div(new Span("There are no arcs yet."));
            empty.addClassName(ArcOutlineStyles.EMPTY);
            list.add(empty);
            return;
        }
        for (Arc arc : outline.arcs()) {
            list.add(createArcCard(arc));
            if (!collapsed.contains(arc.getUuid())) {
                arc.getChapters().forEach(chapter -> list.add(createChapterCard(arc, chapter)));
            }
        }
    }

    /**
     * Stores the outline after a change and shows it.
     */
    private void persist() {
        try {
            service.save(manuscript, outline);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    private Component createArcCard(Arc arc) {
        boolean isCollapsed = collapsed.contains(arc.getUuid());
        Button toggle = iconButton((isCollapsed ? Solid.CHEVRON_DOWN : Solid.CHEVRON_UP).create(),
                isCollapsed ? "Show chapters" : "Hide chapters", _ -> {
                    if (!collapsed.remove(arc.getUuid())) {
                        collapsed.add(arc.getUuid());
                    }
                    refresh();
                });

        Button up = iconButton(Solid.ARROW_UP.create(), "Move arc up", _ -> {
            outline.moveUp(arc);
            persist();
        });
        up.setEnabled(outline.canMoveUp(arc));
        Button down = iconButton(Solid.ARROW_DOWN.create(), "Move arc down", _ -> {
            outline.moveDown(arc);
            persist();
        });
        down.setEnabled(outline.canMoveDown(arc));
        Button addChapter = iconButton(Solid.PLUS.create(), "Add chapter", _ -> {
            Chapter chapter = new Chapter();
            new OutlineNodeDialog("New chapter in " + arc.getTitle(), chapter, () -> {
                outline.add(arc, chapter);
                collapsed.remove(arc.getUuid());
                persist();
            }).open();
        });
        Button generateChapters = iconButton(Solid.WAND_MAGIC_SPARKLES.create(), "Generate chapters", _ ->
                new ChapterGeneratorDialog(service, manuscript, outline, arc, chapters -> {
                    chapters.forEach(chapter -> outline.add(arc, chapter));
                    collapsed.remove(arc.getUuid());
                    persist();
                }).open());
        Button edit = iconButton(Solid.PEN.create(), "Edit arc", _ ->
                new OutlineNodeDialog("Edit arc", arc, this::persist).open());
        Button delete = iconButton(Solid.TRASH.create(), "Delete arc", _ -> confirmDelete(arc));

        List<Component> actions = new ArrayList<>();
        // only an arc the model wrote has something to show
        if (wasGenerated(arc)) {
            actions.add(createGenerationInfo(arc));
        }
        actions.addAll(List.of(up, down, addChapter, generateChapters, edit, delete));
        return createCard(arc, ArcOutlineStyles.ARC, toggle, actions.toArray(new Component[0]));
    }

    private Component createChapterCard(Arc arc, Chapter chapter) {
        Button up = iconButton(Solid.ARROW_UP.create(), "Move chapter up", _ -> moveChapter(chapter, true));
        up.setEnabled(outline.canMoveUp(chapter));
        Button down = iconButton(Solid.ARROW_DOWN.create(), "Move chapter down", _ -> moveChapter(chapter, false));
        down.setEnabled(outline.canMoveDown(chapter));
        Button edit = iconButton(Solid.PEN.create(), "Edit chapter", _ ->
                new OutlineNodeDialog("Edit chapter", chapter, this::persist).open());
        Button delete = iconButton(Solid.TRASH.create(), "Delete chapter", _ -> confirmDelete(chapter));

        List<Component> actions = new ArrayList<>();
        if (wasGenerated(chapter)) {
            actions.add(createGenerationInfo(chapter));
        }
        actions.addAll(List.of(up, down, edit, delete));

        // the third level: the note of the chapter under its description, instead of the dialog of the chapter
        boolean hasNote = StringUtils.isNotBlank(chapter.getNote());
        boolean showNote = hasNote && notesShown.contains(chapter.getUuid());
        Button noteToggle = null;
        if (hasNote) {
            noteToggle = iconButton((showNote ? Solid.CHEVRON_UP : Solid.CHEVRON_DOWN).create(),
                    showNote ? "Hide the note" : "Show the note", _ -> {
                        if (!notesShown.remove(chapter.getUuid())) {
                            notesShown.add(chapter.getUuid());
                        }
                        refresh();
                    });
        }
        return new NodeCard(chapter, ArcOutlineStyles.CHAPTER, createStateButton(chapter), noteToggle, false, showNote,
                actions.toArray(new Component[0]));
    }

    private void moveChapter(Chapter chapter, boolean up) {
        Arc target = up ? outline.moveUp(chapter) : outline.moveDown(chapter);
        if (target != null) {
            // a chapter that moved into a collapsed arc would vanish from the list
            collapsed.remove(target.getUuid());
        }
        persist();
    }

    /**
     * A card with the state button, the title and the description of the node, the {@code toggle} next to them (an
     * arc only) and the {@code actions} under them.
     */
    private Component createCard(OutlineNode node, String kind, Button toggle, Component... actions) {
        return new NodeCard(node, kind, createStateButton(node), toggle, false, actions);
    }

    /** Whether the model wrote the node, so it has its reasoning and answer to show. */
    private static boolean wasGenerated(OutlineNode node) {
        return StringUtils.isNotBlank(node.getGenerationReasoning()) || StringUtils.isNotBlank(node.getGenerationResult());
    }

    /**
     * The button that shows what the model wrote when it generated the arc or chapter, in a popover so it stays out
     * of the way.
     */
    private Button createGenerationInfo(OutlineNode node) {
        Button button = iconButton(Solid.ROBOT.create(), "Generated by the model - show the reasoning and the answer", null);

        Div content = new Div();
        content.addClassName(ArcOutlineStyles.GENERATION);
        if (StringUtils.isNotBlank(node.getGenerationReasoning())) {
            Span heading = new Span("Reasoning");
            heading.addClassName(ArcOutlineStyles.GENERATION_HEADING);
            Markdown reasoning = new Markdown(node.getGenerationReasoning());
            reasoning.addClassName(ArcOutlineStyles.GENERATION_TEXT);
            content.add(heading, reasoning);
        }
        if (StringUtils.isNotBlank(node.getGenerationResult())) {
            Span heading = new Span("Answer");
            heading.addClassName(ArcOutlineStyles.GENERATION_HEADING);
            Div json = new Div(new Span(node.getGenerationResult()));
            json.addClassName(ArcOutlineStyles.GENERATION_JSON);
            content.add(heading, json);
        }
        Popover popover = new Popover(content);
        popover.setTarget(button);
        return button;
    }

    /**
     * The button showing the state of the node; it opens a menu to change it.
     */
    private Button createStateButton(OutlineNode node) {
        Icon icon = switch (node.getState()) {
            case PLANNED -> Solid.CALENDAR.create();
            case CURRENT -> Solid.PLAY.create();
            case FINISHED -> Solid.CHECK.create();
        };
        Button button = iconButton(icon, node.getState().getLabel() + " - change the state", null);

        ContextMenu menu = new ContextMenu(button);
        menu.setOpenOnClick(true);
        for (OutlineState state : OutlineState.values()) {
            menu.addItem(state.getLabel(), _ -> {
                if (state != node.getState()) {
                    node.setState(state);
                    persist();
                }
            }).setEnabled(state != node.getState());
        }
        return button;
    }

    private static Button iconButton(Icon icon, String label, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(icon);
        if (listener != null) {
            button.addClickListener(listener);
        }
        button.setAriaLabel(label);
        button.setTooltipText(label);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        return button;
    }

    private void confirmDelete(Arc arc) {
        int chapters = arc.getChapters().size();
        String message = "Delete the arc " + arc.getTitle()
                + (chapters > 0 ? " with its " + chapters + (chapters == 1 ? " chapter?" : " chapters?") : "?");
        ConfirmDialog.show(message, () -> {
            outline.delete(arc);
            collapsed.remove(arc.getUuid());
            persist();
        });
    }

    private void confirmDelete(Chapter chapter) {
        ConfirmDialog.show("Delete the chapter " + chapter.getTitle() + "?", () -> {
            outline.delete(chapter);
            notesShown.remove(chapter.getUuid());
            persist();
        });
    }
}
