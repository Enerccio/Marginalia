package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/** Edits an actor: its name, its voice (a clip or a voice of the server) and the voices for a mood or a language. Stored in the settings by <i>Save</i>. */
public class ActorDialog extends Dialog {

    private final AudiobookService service;
    private final Actor actor;
    private final Consumer<Actor> saved;

    private final TextField name = new TextField(T.localized(NAME));
    private final TextField tagline = new TextField(T.localized(TAGLINE));
    private final RoleEditor role;
    private final ClipTextField clipText;

    /**
     * @param actor the actor to edit (a copy is edited; the one given is not changed)
     * @param saved called with the stored actor
     */
    public ActorDialog(AudiobookService service, Actor actor, Consumer<Actor> saved) {
        this.service = service;
        Gson gson = new Gson();
        this.actor = gson.fromJson(gson.toJson(actor), Actor.class);
        this.saved = saved;

        setHeaderTitle(T.localized(ACTOR));
        setWidth("min(50em, 95vw)");
        setCloseOnOutsideClick(false);

        name.setWidth("20em");
        name.setValue(this.actor.getName());
        tagline.setWidth("20em");
        tagline.setValue(this.actor.getTagline());
        tagline.setPlaceholder(T.localized(A_CHARACTER_THEY_PLAY_OR_ANY_TAG));
        HorizontalLayout names = new HorizontalLayout(name, tagline);
        names.setPadding(false);
        role = new RoleEditor(this.actor, new VoiceChoices(service));
        Span help = new Span(T.localized(THE_ACTOR_HELP));
        help.addClassName(CssStyles.HINT_TEXT);

        // the words of the clip of the actor's voice (Higgs can be given them with the clip)
        clipText = new ClipTextField(service);
        clipText.show(this.actor.getVoice());
        role.onVoiceChange(clipText::show);

        VerticalLayout content = new VerticalLayout(names, help, role, clipText);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized(SAVE), e -> save());
        save.addThemeName("primary");
        getFooter().add(new Button(T.localized(CANCEL), e -> close()), save);
        DialogFocus.onOpen(this, name);
        DialogShortcuts.submitWith(save, name, tagline, clipText.textArea());
    }

    private void save() {
        actor.setName(name.getValue().strip().isEmpty() ? T.localized(NEW_ACTOR) : name.getValue().strip());
        actor.setTagline(tagline.getValue().strip());
        role.read(actor);
        try {
            service.saveActor(actor);
            clipText.saveIfChanged();
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The actor could not be stored: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_ACTOR_COULD_NOT_BE_STORED).formatted(e.getMessage()));
            return;
        }
        saved.accept(actor);
        close();
    }

    /** Asks, then deletes the actor at once; {@code done} runs after the delete. */
    static void confirmDelete(AudiobookService service, Actor doomed, Runnable done) {
        if (doomed == null) {
            return;
        }
        com.vaadin.flow.component.confirmdialog.ConfirmDialog confirm = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        confirm.setHeader(T.localized(DELETE_THE_ACTOR));
        confirm.setText(T.localized(THE_ACTOR_IS_DELETED).formatted(nameOf(doomed)));
        confirm.setCancelable(true);
        confirm.setConfirmText(T.localized(DELETE));
        confirm.addConfirmListener(e -> {
            service.deleteActor(doomed.getId());
            done.run();
        });
        confirm.open();
    }

    /**
     * Shows the actors of a picker with their tagline under the name (smaller, grey); the picked actor shows only its name. The empty choice
     * shows the caption of the picker.
     */
    static void showTaglines(Select<Actor> select) {
        select.setRenderer(new ComponentRenderer<>(actor -> actor == null ? new Span(select.getEmptySelectionCaption()) : nameWithTagline(actor)));
    }

    /** The name of an actor with its tagline under it, for a list or a grid. */
    static Component nameWithTagline(Actor actor) {
        Div item = new Div(new Div(nameOf(actor)));
        if (!actor.getTagline().isBlank()) {
            Div line = new Div(actor.getTagline());
            line.addClassNames(CssStyles.TEXT_XS, CssStyles.TEXT_SECONDARY);
            item.add(line);
        }
        return item;
    }

    /** What the user sees for an actor in a box; the empty choice of a box has none. */
    static String nameOf(Actor actor) {
        return actor == null ? "" : actor.getName().isBlank() ? T.localized(NEW_ACTOR) : actor.getName();
    }
}
