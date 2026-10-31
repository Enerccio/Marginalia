package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatPersona;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.Model;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * The content of the tab of a chat: what the chat is made of (its kind, medium and model, and the personas in it).
 */
public class ChatPanel extends VerticalLayout {

    private final BetaChat chat;

    public ChatPanel(Manuscript manuscript, BetaChat chat, BetaReadersService service) throws Exception {
        this.chat = chat;
        setSizeFull();
        setPadding(true);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        Medium medium = service.resolveMedium(manuscript, chat);
        Model model = service.resolveModel(manuscript, chat);
        String none = T.localized("none");
        Div about = new Div(
                new Span(chat.getKind() == ChatKind.SINGLE ? T.localized("A chat with one persona") : T.localized("A group chat")),
                new Span(" · " + T.localized("Medium: %s").formatted(medium == null ? none : medium.getName())),
                new Span(" · " + T.localized("Model: %s").formatted(model.ai() == null ? none : model.ai().getName())));
        about.addClassName(BetaReadersStyles.HINT);
        add(about);

        for (ChatPersona member : chat.getPersonas()) {
            add(row(member));
        }
    }

    private static Div row(ChatPersona member) {
        Image icon = new Image(PersonaIcons.source(member.getPersona()), member.getPersona().getName());
        icon.addClassName(BetaReadersStyles.ICON);

        Div text = new Div();
        Span name = new Span(member.getPersona().getName() + (member.isPresent() ? "" : " (" + T.localized("left") + ")"));
        name.addClassName(CssStyles.TEXT_BOLD);
        text.add(name);
        if (member.getPersona().getDescription() != null && !member.getPersona().getDescription().isBlank()) {
            Div description = new Div(member.getPersona().getDescription());
            description.addClassName(BetaReadersStyles.HINT);
            text.add(description);
        }

        Div row = new Div(icon, text);
        row.addClassName(BetaReadersStyles.PERSONA);
        if (!member.isPresent()) {
            row.addClassName(BetaReadersStyles.PERSONA_LEFT);
        }
        return row;
    }

    public BetaChat getChat() {
        return chat;
    }
}
