package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatPersona;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.Asked;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.ContextUsage;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.Model;
import com.github.enerccio.marginalia.extensions.betareaders.service.ChatRunner;
import com.github.enerccio.marginalia.extensions.betareaders.service.EmptyReplyException;
import com.github.enerccio.marginalia.extensions.betareaders.service.NoModelException;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Status;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.github.enerccio.marginalia.extensions.betareaders.service.ReplyReader.Reading;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.text.NumberFormat;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * The content of the tab of a chat: what the chat is made of (the kind, the medium, the model and the personas), the conversation, and
 * the box to write to the personas. A message can bring the parts of the story that are new; the reply of the model is shown as it
 * streams, a message at a time (a message that is still being written is not shown).
 */
@Configurable(preConstruction = true)
public class ChatPanel extends VerticalLayout {

    /** The shortest time between two redraws of the turn that streams. */
    private static final long REDRAW_NANOS = 100_000_000L;

    @Autowired
    private Localization loc;

    private final Manuscript manuscript;
    private final BetaChat chat;
    private final BetaReadersService service;
    private final Consumer<Boolean> onBusy;

    private final VerticalLayout messages = new VerticalLayout();
    private final Scroller scroller = new Scroller(messages);
    private final TextArea input = new TextArea();
    private final Checkbox includeParts = new Checkbox(T.localized("Include the latest parts"));
    private final Button checkParts = new Button(Solid.ROTATE.create());
    private final Button send = new Button();
    private final Span context = new Span();

    /** The request of the user, for the services the panel calls when a reply ends (on a thread that has none). */
    private RequestAttributes attributes;
    private BetaMessageCard.Turn live;
    private CancellationToken running;
    private boolean generating;

    /**
     * @param onBusy told when a reply is being written (true) and when it is done (false), so the tabs can be locked meanwhile
     */
    public ChatPanel(Manuscript manuscript, BetaChat chat, BetaReadersService service, Consumer<Boolean> onBusy) throws Exception {
        this.manuscript = manuscript;
        this.chat = chat;
        this.service = service;
        this.onBusy = onBusy;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);

        messages.setPadding(true);
        messages.setAlignItems(Alignment.STRETCH);
        for (BetaMessage message : chat.getMessages()) {
            messages.add(BetaMessageCard.create(chat, message));
        }
        scroller.setSizeFull();
        scroller.addClassName(CssStyles.MIN_HEIGHT_0);

        add(header(), scroller, composer());
        setFlexGrow(1, scroller);
        refreshStatus(true);
        scrollToEnd();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------------------------------------------------

    private Component header() throws Exception {
        Medium medium = service.resolveMedium(manuscript, chat);
        Model model = service.resolveModel(manuscript, chat);
        String none = T.localized("none");
        Div about = new Div(
                new Span(chat.getKind() == ChatKind.SINGLE ? T.localized("A chat with one persona") : T.localized("A group chat")),
                new Span(" · " + T.localized("Medium: %s").formatted(medium == null ? none : medium.getName())),
                new Span(" · " + T.localized("Model: %s").formatted(model.ai() == null ? none : model.ai().getName())));
        about.addClassName(BetaReadersStyles.HINT);

        VerticalLayout personas = new VerticalLayout();
        personas.setPadding(false);
        for (ChatPersona member : chat.getPersonas()) {
            personas.add(row(member));
        }
        Details details = new Details(T.localized("Personas (%d)").formatted(chat.present().size()), personas);

        VerticalLayout header = new VerticalLayout(about, details);
        header.setPadding(true);
        header.setSpacing(false);
        header.addClassName(BetaReadersStyles.BAR);
        return header;
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

    private Component composer() {
        input.setWidthFull();
        input.setPlaceholder(T.localized("Write to the beta readers..."));
        ResizableTextArea.install(loc, input, "90px");
        Shortcuts.addShortcutListener(input, this::send, Key.ENTER, KeyModifier.CONTROL);

        checkParts.addThemeName("tertiary");
        checkParts.addThemeName("small");
        checkParts.setTooltipText(T.localized("Check for new parts"));
        checkParts.setAriaLabel(T.localized("Check for new parts"));
        checkParts.addClickListener(_ -> refreshStatus(true));

        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        send.addClickListener(_ -> {
            if (generating) {
                stop();
            } else {
                send();
            }
        });
        showButton();

        HorizontalLayout row = new HorizontalLayout(includeParts, checkParts, send);
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.setWrap(true);
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);

        context.addClassName(BetaReadersStyles.HINT);
        VerticalLayout composer = new VerticalLayout(input, row, context);
        composer.setPadding(true);
        composer.setSpacing(false);
        composer.addClassName(BetaReadersStyles.TOP);
        return composer;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Asking
    // ------------------------------------------------------------------------------------------------------------

    private void send() {
        if (generating) {
            return;
        }
        UI ui = UI.getCurrent();
        attributes = ThreadCopyRequestAttributes.create();
        try {
            Asked asked = service.ask(manuscript, chat, input.getValue(), includeParts.getValue(), listener(ui));
            input.clear();
            messages.add(BetaMessageCard.create(chat, asked.message()));
            live = new BetaMessageCard.Turn(chat);
            messages.add(live);
            running = asked.token();
            setGenerating(true);
            refreshStatus(false);
            scrollToEnd();
        } catch (NoModelException e) {
            Notification.warning(T.localized("There is no model to ask. Choose one for the chat, the book or in the settings."));
        } catch (IllegalArgumentException e) {
            Notification.warning(T.localized("Write a message or include the latest parts."));
        } catch (UnsupportedOperationException e) {
            Notification.warning(e.getMessage());
        } catch (Exception e) {
            Notification.error(T.localized("Can't ask the model: %s").formatted(e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private void stop() {
        CancellationToken token = running;
        if (token != null) {
            token.cancel();
        }
    }

    /**
     * What happens when the model writes: called on the threads of the inference service, so everything goes through the UI.
     */
    private ChatRunner.Listener listener(UI ui) {
        return new ChatRunner.Listener() {
            private long lastRedraw;

            @Override
            public void onProgress(Reading reading) {
                long now = System.nanoTime();
                if (now - lastRedraw < REDRAW_NANOS) {
                    // the end of the reply is drawn when it finishes
                    return;
                }
                lastRedraw = now;
                ui.access(() -> {
                    if (live != null) {
                        live.show(reading.reasoning(), reading.messages(), reading.rawText());
                        scrollToEnd();
                        UIPushGuard.push(ui);
                    }
                });
            }

            @Override
            public void onFinished(ChatRunner.Outcome outcome) {
                ui.access(() -> {
                    finished(outcome);
                    UIPushGuard.push(ui);
                });
            }
        };
    }

    private void finished(ChatRunner.Outcome outcome) {
        if (live != null) {
            messages.remove(live);
            live = null;
        }
        running = null;
        if (outcome.reply() != null) {
            messages.add(BetaMessageCard.create(chat, outcome.reply()));
        }
        setGenerating(false);
        if (outcome.error() instanceof EmptyReplyException) {
            Notification.warning(T.localized("The model did not answer."));
        } else if (outcome.error() != null) {
            UIUtils.inferenceError(loc, outcome.error());
        }
        if (outcome.prompt().dropped() > 0) {
            Notification.warning(T.localized("%d older messages were left out of the request to fit the context.").formatted(outcome.prompt().dropped()));
        }
        // the reply ends on a thread of the inference service: the services need the request of the user
        try (InRequestScope _ = new InRequestScope(attributes)) {
            refreshStatus(true);
        }
        scrollToEnd();
    }

    private void setGenerating(boolean value) {
        generating = value;
        input.setEnabled(!value);
        includeParts.setEnabled(!value);
        checkParts.setEnabled(!value);
        showButton();
        onBusy.accept(value);
    }

    private void showButton() {
        send.setText(generating ? T.localized("Stop") : T.localized("Send"));
        send.setIcon((generating ? Solid.STOP : Solid.PAPER_PLANE).create());
    }

    // ------------------------------------------------------------------------------------------------------------
    // What the chat has read, and how full it is
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Shows how many parts the next message would bring and how much of the context the chat uses.
     *
     * @param check whether the box is ticked when there is something to bring
     */
    private void refreshStatus(boolean check) {
        try {
            Status status = service.partsStatus(manuscript, chat);
            includeParts.setLabel(partsLabel(status));
            if (check) {
                includeParts.setValue(status.pending());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        try {
            ContextUsage usage = service.contextUsage(manuscript, chat);
            NumberFormat format = NumberFormat.getIntegerInstance();
            context.setText(usage.limit() > 0
                    ? T.localized("Context: %1$s of %2$s tokens").formatted(format.format(usage.tokens()), format.format(usage.limit()))
                    : T.localized("Context: %s tokens").formatted(format.format(usage.tokens())));
        } catch (NoModelException e) {
            context.setText(T.localized("There is no model to ask. Choose one for the chat, the book or in the settings."));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static String partsLabel(Status status) {
        if (status.first() && status.count() > 0) {
            return T.localized("Include the story (%d parts)").formatted(status.count());
        }
        if (status.count() > 0) {
            return T.localized("Include the latest parts (%d new)").formatted(status.count());
        }
        return status.pending() ? T.localized("Include the latest parts (the story changed)")
                : T.localized("Include the latest parts (none new)");
    }

    private void scrollToEnd() {
        scroller.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    public BetaChat getChat() {
        return chat;
    }
}
