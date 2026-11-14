package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.MergeTurnsDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TurnRow;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.function.Supplier;

/**
 * The Planner, a tab of the story editor: the scenes the author plans to write, a list they fill by hand. <b>+</b> adds a scene (a
 * title and the turn details), the box in front of a scene says it is written, a click on a scene puts its details into the new turn
 * form, the arrows move it, the plus in its row adds a scene under it, the merge button makes one scene of it and the next one
 * ({@link MergeTurnsDialog}), the pen edits it and the trash can deletes it. <b>Import from the branch</b> adds a scene for every
 * part of the branch the book is on. The list is read again when the scenes change ({@link PlannerService#onChange}), also when
 * a model added one.
 */
@Configurable(preConstruction = true)
public class PlannerPanel extends VerticalLayout {

    /**
     * What the story does with a scene the author clicked.
     */
    public interface Host {

        /**
         * Puts the details of the scene into the new turn form and shows the form.
         *
         * @return false when the story has no form to put it into
         */
        boolean open(PlannedScene scene);
    }

    @Autowired
    private Localization loc;

    @Autowired
    private PlannerService plannerService;

    private final Supplier<Manuscript> book;
    private final Host host;
    private final Span summary = new Span();
    private final VerticalLayout list = new VerticalLayout();

    // the book the listener below is registered for
    private String listeningTo;
    private Runnable stopListening;

    /**
     * @param book gives the book the story has open now (the story part loads other books into the same instance)
     */
    public PlannerPanel(Supplier<Manuscript> book, Host host) {
        this.book = book;
        this.host = host;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        Button add = new Button(loc.getValue(L.LABEL_ADD_SCENE), Solid.PLUS.create(), _ -> {
            Manuscript current = book.get();
            if (current != null) {
                new PlannedSceneDialog(loc, loc.getValue(L.LABEL_NEW_SCENE), new PlannedScene(), scene ->
                        run(() -> plannerService.add(current, scene))).open();
            }
        });
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        summary.addClassName(CssStyles.TEXT_SECONDARY);
        Button importBranch = new Button(loc.getValue(L.LABEL_IMPORT_FROM_BRANCH), Solid.FILE_IMPORT.create(), _ -> confirmImport());
        importBranch.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        importBranch.setTooltipText(loc.getValue(L.HELP_PLANNER_IMPORT_FROM_BRANCH));
        HorizontalLayout bar = new HorizontalLayout(add, importBranch, summary);
        bar.setWrap(true);
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.setPadding(true);

        list.setPadding(false);
        list.setSpacing(false);
        list.setWidthFull();
        add(bar, list);
        setFlexGrow(1, list);
        list.addClassName(CssStyles.OVERFLOW_AUTO);

        addDetachListener(_ -> stopListening());
        addAttachListener(_ -> refresh());
    }

    /** Draws the scenes of the book again. */
    public void refresh() {
        Manuscript current = book.get();
        listen(current);
        list.removeAll();
        if (current == null) {
            summary.setText("");
            return;
        }
        List<PlannedScene> scenes;
        try {
            scenes = plannerService.list(current);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        long done = scenes.stream().filter(PlannedScene::isDone).count();
        summary.setText(scenes.isEmpty() ? "" : loc.getValue(L.MSG_PLANNER_SUMMARY).formatted(done, scenes.size()));
        if (scenes.isEmpty()) {
            Span none = new Span(loc.getValue(L.MSG_PLANNER_NONE));
            none.addClassNames(CssStyles.HINT_TEXT, CssStyles.PRE_WRAP);
            Div hint = new Div(none);
            list.add(hint);
            return;
        }
        for (int i = 0; i < scenes.size(); i++) {
            list.add(row(current, scenes.get(i), i < scenes.size() - 1 ? scenes.get(i + 1) : null, i));
        }
    }

    /**
     * @param next the scene after this one, null for the last scene
     */
    private Component row(Manuscript current, PlannedScene scene, PlannedScene next, int index) {
        Span title = new Span(scene.getTitle());
        title.addClassNames(CssStyles.TEXT_BOLD, CssStyles.WRAP_ANYWHERE);
        Div content = new Div(title);
        String detail = StringUtils.firstNonBlank(scene.getInstructions(), scene.getSceneSetting());
        if (StringUtils.isNotBlank(detail)) {
            Div text = new Div(new Span(detail));
            text.addClassName(CssStyles.PLANNER_SCENE_TEXT);
            content.add(text);
        }
        String doneTooltip = loc.getValue(scene.isDone() ? L.LABEL_MARK_NOT_DONE : L.LABEL_MARK_DONE);
        return TurnRow.builder(content)
                .done(scene.isDone(), doneTooltip, value -> run(() -> plannerService.setDone(current, scene.getUuid(), value)))
                .onClick(loc.getValue(L.LABEL_USE_IN_NEXT_TURN), () -> {
                    if (!host.open(scene)) {
                        Notification.warning(loc.getValue(L.MSG_PLANNER_NO_TURN_FORM));
                    }
                })
                .moveUp(action(L.LABEL_MOVE_UP, index > 0, () -> run(() -> plannerService.move(current, scene.getUuid(), -1))))
                .moveDown(action(L.LABEL_MOVE_DOWN, next != null, () -> run(() -> plannerService.move(current, scene.getUuid(), 1))))
                .insert(action(L.LABEL_INSERT_SCENE_BELOW, true, () -> new PlannedSceneDialog(loc, loc.getValue(L.LABEL_NEW_SCENE),
                        new PlannedScene(), added -> run(() -> plannerService.insert(current, scene.getUuid(), added))).open()))
                .merge(action(L.LABEL_MERGE_WITH_NEXT_SCENE, next != null, () -> merge(current, scene, next)))
                .edit(action(L.LABEL_EDIT, true, () -> new PlannedSceneDialog(loc, loc.getValue(L.LABEL_EDIT_SCENE), scene,
                        changed -> run(() -> plannerService.update(current, changed))).open()))
                .delete(action(L.LABEL_DELETE, true, () -> confirmDelete(current, scene)))
                .build();
    }

    private TurnRow.Action action(L tooltip, boolean enabled, Runnable run) {
        return new TurnRow.Action(loc.getValue(tooltip), enabled, run);
    }

    private void confirmDelete(Manuscript current, PlannedScene scene) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(loc.getValue(L.LABEL_DELETE));
        confirm.setText(loc.getValue(L.MSG_PLANNER_DELETE_CONFIRM).formatted(scene.getTitle()));
        confirm.setCancelable(true);
        confirm.setConfirmText(loc.getValue(L.LABEL_DELETE));
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(_ -> run(() -> plannerService.delete(current, scene.getUuid())));
        confirm.open();
    }

    /** Shows the two scenes joined, for the author to edit, and makes one scene of them. */
    private void merge(Manuscript current, PlannedScene scene, PlannedScene next) {
        new MergeTurnsDialog(loc, loc.getValue(L.LABEL_MERGE_WITH_NEXT_SCENE), toTurn(scene), toTurn(next), PlannerService.MAX_TITLE_LENGTH,
                merged -> run(() -> plannerService.merge(current, scene.getUuid(), next.getUuid(), new PlannedScene(merged.title(),
                        merged.sceneSetting(), merged.povCharacter(), merged.presentCharacters(), merged.instructions())))).open();
    }

    private static MergeTurnsDialog.Turn toTurn(PlannedScene scene) {
        return new MergeTurnsDialog.Turn(StringUtils.defaultString(scene.getTitle()), scene.getSceneSetting(), scene.getPovCharacter(),
                scene.getPresentCharacters(), scene.getInstructions());
    }

    private void confirmImport() {
        Manuscript current = book.get();
        if (current == null) {
            return;
        }
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(loc.getValue(L.LABEL_IMPORT_FROM_BRANCH));
        confirm.setText(loc.getValue(L.MSG_PLANNER_IMPORT_CONFIRM));
        confirm.setCancelable(true);
        confirm.setConfirmText(loc.getValue(L.LABEL_IMPORT));
        confirm.addConfirmListener(_ -> run(() -> {
            int added = plannerService.importBranch(current);
            if (added == 0) {
                Notification.warning(loc.getValue(L.MSG_PLANNER_IMPORT_NONE));
            } else {
                Notification.success(loc.getValue(L.MSG_PLANNER_IMPORTED).formatted(added));
            }
        }));
        confirm.open();
    }

    /**
     * Runs a change of the scenes; the list is drawn again by the listener of the service.
     *
     * @return false when the change failed (the reason is shown)
     */
    private boolean run(Change change) {
        try {
            change.run();
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.warning(e.getMessage());
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            refresh();
        }
        return false;
    }

    private interface Change {
        void run() throws Exception;
    }

    // --- the changes made by someone else (a model) ---

    private void listen(Manuscript current) {
        String uuid = current == null ? null : current.getUuid();
        if (StringUtils.equals(uuid, listeningTo)) {
            return;
        }
        stopListening();
        if (current != null) {
            listeningTo = uuid;
            stopListening = plannerService.onChange(current, () -> UIUtils.accessComponent(this, this::refresh));
        }
    }

    private void stopListening() {
        if (stopListening != null) {
            stopListening.run();
        }
        stopListening = null;
        listeningTo = null;
    }

    /**
     * The state {@code NewTurnForm#restoreState} takes: the four fields of the turn details, marked as typed by the author so that
     * opening the form does not replace them with the ones of the last part.
     */
    public static JsonObject toFormState(PlannedScene scene) {
        JsonObject state = new JsonObject();
        state.addProperty("scene", StringUtils.defaultString(scene.getSceneSetting()));
        state.addProperty("pov", StringUtils.defaultString(scene.getPovCharacter()));
        state.addProperty("present", StringUtils.defaultString(scene.getPresentCharacters()));
        state.addProperty("instructions", StringUtils.defaultString(scene.getInstructions()));
        state.addProperty("edited", true);
        return state;
    }
}
