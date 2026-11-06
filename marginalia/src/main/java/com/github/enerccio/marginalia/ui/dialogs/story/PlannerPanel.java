package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
 * form, the arrows move it, the pen edits it and the trash can deletes it. The list is read again when the scenes change
 * ({@link PlannerService#onChange}), also when a model added one.
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
        HorizontalLayout bar = new HorizontalLayout(add, summary);
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
            list.add(row(current, scenes.get(i), i, scenes.size()));
        }
    }

    private Component row(Manuscript current, PlannedScene scene, int index, int count) {
        Checkbox done = new Checkbox(scene.isDone());
        done.setTooltipText(loc.getValue(scene.isDone() ? L.LABEL_MARK_NOT_DONE : L.LABEL_MARK_DONE));
        done.setAriaLabel(loc.getValue(scene.isDone() ? L.LABEL_MARK_NOT_DONE : L.LABEL_MARK_DONE));
        done.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                run(() -> plannerService.setDone(current, scene.getUuid(), e.getValue()));
            }
        });

        Span title = new Span(scene.getTitle());
        title.addClassNames(CssStyles.TEXT_BOLD, CssStyles.WRAP_ANYWHERE);
        Div main = new Div(title);
        main.addClassName(CssStyles.PLANNER_SCENE_MAIN);
        String detail = StringUtils.firstNonBlank(scene.getInstructions(), scene.getSceneSetting());
        if (StringUtils.isNotBlank(detail)) {
            Div text = new Div(new Span(detail));
            text.addClassName(CssStyles.PLANNER_SCENE_TEXT);
            main.add(text);
        }
        main.setTitle(loc.getValue(L.LABEL_USE_IN_NEXT_TURN));
        main.addClickListener(_ -> {
            if (!host.open(scene)) {
                Notification.warning(loc.getValue(L.MSG_PLANNER_NO_TURN_FORM));
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(
                button(Solid.ARROW_UP, L.LABEL_MOVE_UP, index > 0, _ -> run(() -> plannerService.move(current, scene.getUuid(), -1))),
                button(Solid.ARROW_DOWN, L.LABEL_MOVE_DOWN, index < count - 1, _ -> run(() -> plannerService.move(current, scene.getUuid(), 1))),
                button(Solid.PEN, L.LABEL_EDIT, true, _ -> new PlannedSceneDialog(loc, loc.getValue(L.LABEL_EDIT_SCENE), scene, changed ->
                        run(() -> plannerService.update(current, changed))).open()),
                button(Solid.TRASH, L.LABEL_DELETE, true, _ -> confirmDelete(current, scene)));
        buttons.setSpacing(false);
        buttons.setPadding(false);

        HorizontalLayout row = new HorizontalLayout(done, main, buttons);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.START);
        row.setSpacing(false);
        row.expand(main);
        row.addClassName(CssStyles.PLANNER_SCENE);
        if (scene.isDone()) {
            row.addClassName(CssStyles.PLANNER_SCENE_DONE);
        }
        return row;
    }

    private Button button(Solid icon, L tooltip, boolean enabled, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(icon.create(), listener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        button.setTooltipText(loc.getValue(tooltip));
        button.setAriaLabel(loc.getValue(tooltip));
        button.setEnabled(enabled);
        return button;
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

    /** Runs a change of the scenes; the list is drawn again by the listener of the service. */
    private void run(Change change) {
        try {
            change.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.warning(e.getMessage());
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            refresh();
        }
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
