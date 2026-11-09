package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/** Edits an actor: its name, its voice (a clip or a voice of the server) and the voices for a mood or a language. Stored in the settings by <i>Save</i>. */
public class ActorDialog extends Dialog {

    private final AudiobookService service;
    private final Actor actor;
    private final Consumer<Actor> saved;

    private final TextField name = new TextField(T.localized(NAME));
    private final RoleEditor role;

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
        role = new RoleEditor(this.actor, new VoiceChoices(service));
        Span help = new Span(T.localized(THE_ACTOR_HELP));
        help.addClassName(CssStyles.HINT_TEXT);

        VerticalLayout content = new VerticalLayout(name, help, role);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized(SAVE), e -> save());
        save.addThemeName("primary");
        getFooter().add(new Button(T.localized(CANCEL), e -> close()), save);
        DialogFocus.onOpen(this, name);
        DialogShortcuts.submitWith(save, name);
    }

    private void save() {
        actor.setName(name.getValue().strip().isEmpty() ? T.localized(NEW_ACTOR) : name.getValue().strip());
        role.read(actor);
        try {
            service.saveActor(actor);
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The actor could not be stored: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_ACTOR_COULD_NOT_BE_STORED).formatted(e.getMessage()));
            return;
        }
        saved.accept(actor);
        close();
    }

    /** What the user sees for an actor in a box; the empty choice of a box has none. */
    static String nameOf(Actor actor) {
        return actor == null ? "" : actor.getName().isBlank() ? T.localized(NEW_ACTOR) : actor.getName();
    }
}
