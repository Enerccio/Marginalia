package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.model.Cast;
import com.github.enerccio.marginalia.extensions.audiobook.model.CastMember;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The cast of a book: the actor that reads the narration, the actor for a speaker nobody knows, and an actor for each character. The actors are the
 * voices of the user (settings); a new one can be made here. The cast is stored with the book by <i>Save</i>.
 */
public class CastDialog extends Dialog {

    private final AudiobookService service;
    private final Manuscript manuscript;
    private final Cast cast;
    private final Runnable saved;

    private final Select<Actor> narrator = actorSelect(T.localized(NARRATOR));
    private final Select<Actor> fallback = actorSelect(T.localized(OTHER_SPEAKERS));
    private final VerticalLayout characters = new VerticalLayout();
    private final List<CharacterRow> rows = new ArrayList<>();
    private final Button save = new Button(T.localized(SAVE), e -> save());

    /** @param saved called after the cast is stored */
    public CastDialog(AudiobookService service, Manuscript manuscript, Runnable saved) {
        this.service = service;
        this.manuscript = manuscript;
        this.cast = service.castOf(manuscript);
        this.saved = saved;

        setHeaderTitle(T.localized(CAST));
        setWidth("min(64em, 95vw)");
        // the characters make it long: the content scrolls between the header and the buttons
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);

        Span help = new Span(T.localized(THE_CAST_HELP));
        help.addClassName(CssStyles.HINT_TEXT);
        Span fallbackHelp = new Span(T.localized(THE_FALLBACK_HELP));
        fallbackHelp.addClassName(CssStyles.HINT_TEXT);

        characters.setPadding(false);
        characters.setSpacing(false);
        cast.getMembers().forEach(this::addCharacter);
        Button addCharacter = new Button(T.localized(ADD_A_CHARACTER), Solid.PLUS.create(), e -> addCharacter(new CastMember()));
        Button addFromBook = new Button(T.localized(ADD_THE_CHARACTERS_OF_THE_BOOK), Solid.USERS.create(), e -> addBookCharacters());
        Button newActor = new Button(T.localized(NEW_ACTOR), Solid.MICROPHONE.create(), e -> newActor(null));

        reloadActors();
        narrator.setValue(service.getSettings().findActor(cast.getNarratorActorId()));
        fallback.setValue(service.getSettings().findActor(cast.getFallbackActorId()));
        HorizontalLayout special = new HorizontalLayout(narrator, previewOf(narrator), fallback, previewOf(fallback));
        special.setAlignItems(FlexComponent.Alignment.END);
        special.setWrap(true);

        VerticalLayout content = new VerticalLayout(help, new HorizontalLayout(newActor), special, fallbackHelp, new H4(T.localized(CHARACTERS)),
                characters, new HorizontalLayout(addCharacter, addFromBook));
        content.setPadding(false);
        add(content);

        save.addThemeName("primary");
        getFooter().add(new Button(T.localized(CANCEL), e -> close()), save);
    }

    /** A button that has the voice of the actor of the box spoken. */
    private VoicePreview previewOf(Select<Actor> select) {
        return new VoicePreview(service, () -> select.getValue() == null ? "" : select.getValue().getVoice(), () -> null);
    }

    private static Select<Actor> actorSelect(String label) {
        Select<Actor> select = new Select<>();
        select.setLabel(label);
        select.setEmptySelectionAllowed(true);
        select.setEmptySelectionCaption(T.localized(NOT_SET));
        select.setItemLabelGenerator(ActorDialog::nameOf);
        select.setWidth("16em");
        return select;
    }

    private List<Select<Actor>> selects() {
        List<Select<Actor>> all = new ArrayList<>(List.of(narrator, fallback));
        rows.forEach(r -> all.add(r.actor));
        return all;
    }

    /** Gives every box the actors of the user; each keeps its pick when the actor still exists. */
    private void reloadActors() {
        List<Actor> actors = service.getSettings().getActors();
        for (Select<Actor> select : selects()) {
            Actor picked = select.getValue();
            select.setItems(actors);
            select.setValue(picked == null ? null : actors.stream().filter(a -> a.getId().equals(picked.getId())).findFirst().orElse(null));
        }
    }

    /** Opens the dialog of a new actor; the one that is made is put into the box (null for none). */
    private void newActor(Select<Actor> into) {
        new ActorDialog(service, new Actor(), actor -> {
            reloadActors();
            if (into != null) {
                into.setValue(service.getSettings().findActor(actor.getId()));
            }
        }).open();
    }

    private void addCharacter(CastMember member) {
        CharacterRow row = new CharacterRow(member);
        rows.add(row);
        characters.add(row.layout);
    }

    private void addBookCharacters() {
        // a name the cast already resolves (by name, alias or a unique word of a name) is not added again
        Cast shown = new Cast();
        for (CharacterRow row : rows) {
            CastMember probe = new CastMember();
            probe.setName(row.name.getValue());
            probe.setAliases(row.aliases.getValue());
            shown.getMembers().add(probe);
        }
        int added = 0;
        for (String character : service.bookCharacters(manuscript)) {
            if (shown.find(character) == null) {
                CastMember member = new CastMember();
                member.setName(character);
                addCharacter(member);
                shown.getMembers().add(member);
                added++;
            }
        }
        Notification.success(T.localized(CHARACTERS_ADDED).formatted(added));
    }

    private void save() {
        cast.setNarratorActorId(narrator.getValue() == null ? null : narrator.getValue().getId());
        cast.setFallbackActorId(fallback.getValue() == null ? null : fallback.getValue().getId());
        List<CastMember> members = new ArrayList<>();
        for (CharacterRow row : rows) {
            if (!row.name.getValue().isBlank()) {
                members.add(row.read());
            }
        }
        cast.setMembers(members);
        try {
            service.saveCast(manuscript, cast);
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The cast could not be stored: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CAST_COULD_NOT_BE_STORED).formatted(e.getMessage()));
            return;
        }
        saved.run();
        close();
    }

    /** One character: its name, other names, and the actor that plays it. */
    private final class CharacterRow {
        private final CastMember member;
        private final TextField name = new TextField(T.localized(NAME));
        private final TextField aliases = new TextField(T.localized(OTHER_NAMES));
        private final Select<Actor> actor = actorSelect(T.localized(ACTOR));
        private HorizontalLayout layout;

        CharacterRow(CastMember member) {
            this.member = member;
            name.setValue(member.getName());
            name.setWidth("12em");
            aliases.setValue(member.getAliases());
            aliases.setWidth("12em");
            actor.setItems(service.getSettings().getActors());
            actor.setValue(service.getSettings().findActor(member.getActorId()));
            Button remove = new Button(Solid.TRASH.create(), e -> {
                rows.remove(this);
                characters.remove(this.layout);
            });
            remove.setThemeName("tertiary icon");
            remove.setTooltipText(T.localized(REMOVE));
            Button newActor = new Button(Solid.PLUS.create(), e -> newActor(actor));
            newActor.setThemeName("tertiary icon");
            newActor.setTooltipText(T.localized(NEW_ACTOR));
            layout = new HorizontalLayout(name, aliases, actor, previewOf(actor), newActor, remove);
            layout.setAlignItems(FlexComponent.Alignment.END);
            layout.setWrap(true);
            DialogShortcuts.submitWith(save, name, aliases);
        }

        CastMember read() {
            member.setName(name.getValue().strip());
            member.setAliases(aliases.getValue().strip());
            member.setActorId(actor.getValue() == null ? null : actor.getValue().getId());
            return member;
        }
    }
}
