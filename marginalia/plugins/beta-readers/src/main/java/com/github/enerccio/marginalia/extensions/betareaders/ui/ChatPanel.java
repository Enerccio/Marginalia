package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.Asked;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.ContextUsage;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.Model;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Status;
import com.github.enerccio.marginalia.extensions.betareaders.service.ReplyReader.Reading;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
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
import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

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
    private final Checkbox includeParts = new Checkbox(T.localized(INCLUDE_THE_LATEST_PARTS));
    private final Button checkParts = new Button(Solid.ROTATE.create());
    private final Button send = new Button();
    private final Span context = new Span();
    private final Span read = new Span();

    /** The request of the user, for the services the panel calls when a reply ends (on a thread that has none). */
    private RequestAttributes attributes;
    private Component header;
    private boolean personasOpen;
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

        messages.setPadding(false);
        messages.addClassName(CssStyles.PADDING_COMPACT);
        messages.setAlignItems(Alignment.STRETCH);
        rebuildMessages();
        scroller.setSizeFull();
        scroller.addClassName(CssStyles.MIN_HEIGHT_0);

        header = header();
        add(header, scroller, composer());
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
        String none = T.localized(NONE);
        Div about = new Div(
                new Span(chat.getKind() == ChatKind.SINGLE ? T.localized(A_CHAT_WITH_ONE_PERSONA) : T.localized(A_GROUP_CHAT)),
                new Span(" · " + T.localized(MEDIUM_S).formatted(medium == null ? none : medium.getName())),
                new Span(" · " + T.localized(MODEL_S).formatted(model.ai() == null ? none : model.ai().getName())));
        if (chat.getSpoilerLimit() > 0) {
            about.add(new Span(" · " + T.localized(READS_UP_TO_PART).formatted(chat.getSpoilerLimit())));
        }
        if (chat.getFromPart() > 1) {
            about.add(new Span(" · " + T.localized(FROM_PART).formatted(chat.getFromPart())));
        }
        about.addClassName(BetaReadersStyles.HINT);

        VerticalLayout personas = new VerticalLayout();
        personas.setPadding(false);
        personas.addClassName(BetaReadersStyles.PERSONAS);
        int present = chat.present().size();
        for (ChatPersona member : chat.getPersonas()) {
            personas.add(row(member, present));
        }
        if (chat.getKind() == ChatKind.GROUP) {
            Button bring = new Button(T.localized(BRING_IN_A_PERSONA), Solid.USER_PLUS.create(), _ -> bringIn());
            bring.addThemeName("tertiary");
            bring.addThemeName("small");
            Div actions = new Div(bring);
            actions.addClassName(BetaReadersStyles.ACTIONS);
            personas.add(actions);
        }
        Details details = new Details(T.localized(PERSONAS_COUNT).formatted(present), personas);
        details.setOpened(personasOpen);
        details.addOpenedChangeListener(event -> personasOpen = event.isOpened());

        VerticalLayout header = new VerticalLayout(about, details);
        header.setPadding(false);
        header.setSpacing(false);
        header.addClassNames(BetaReadersStyles.BAR, CssStyles.PADDING_COMPACT);
        return header;
    }

    private void rebuildHeader() {
        try {
            Component fresh = header();
            replace(header, fresh);
            header = fresh;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private Div row(ChatPersona member, int present) {
        Image icon = new Image(PersonaIcons.source(member.getPersona()), member.getPersona().getName());
        icon.addClassName(BetaReadersStyles.ICON);

        Div text = new Div();
        Span name = new Span(member.getPersona().getName() + (member.isPresent() ? "" : " (" + T.localized(LEFT) + ")"));
        name.addClassName(CssStyles.TEXT_BOLD);
        text.add(name);
        if (member.getPersona().getDescription() != null && !member.getPersona().getDescription().isBlank()) {
            Div description = new Div(member.getPersona().getDescription());
            description.addClassName(BetaReadersStyles.HINT);
            text.add(description);
        }

        Div actions = new Div();
        actions.addClassName(BetaReadersStyles.ACTIONS);
        Button update = new Button(Solid.ROTATE.create(), _ -> updateFromLibrary(member.getPersona()));
        update.addThemeName("tertiary");
        update.addThemeName("small");
        update.setTooltipText(T.localized(UPDATE_FROM_THE_LIBRARY));
        update.setAriaLabel(T.localized(UPDATE_FROM_THE_LIBRARY));
        actions.add(update);
        if (chat.getKind() == ChatKind.GROUP && member.isPresent() && present > 1) {
            Button away = new Button(Solid.USER_MINUS.create(), _ -> sendAway(member.getPersona()));
            away.addThemeName("tertiary");
            away.addThemeName("small");
            away.setTooltipText(T.localized(SEND_AWAY));
            away.setAriaLabel(T.localized(SEND_AWAY));
            actions.add(away);
        }

        Div row = new Div(icon, text, actions);
        row.addClassName(BetaReadersStyles.PERSONA);
        if (!member.isPresent()) {
            row.addClassName(BetaReadersStyles.PERSONA_LEFT);
        }
        return row;
    }

    private Component composer() {
        input.setWidthFull();
        input.setPlaceholder(T.localized(WRITE_TO_THE_BETA_READERS));
        ResizableTextArea.install(loc, input, "64px");
        Shortcuts.addShortcutListener(input, this::send, Key.ENTER, KeyModifier.CONTROL);

        checkParts.addThemeName("tertiary");
        checkParts.addThemeName("small");
        checkParts.setTooltipText(T.localized(CHECK_FOR_NEW_PARTS));
        checkParts.setAriaLabel(T.localized(CHECK_FOR_NEW_PARTS));
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
        row.setSpacing(false);
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // what the personas have read and how full the context is, in one line (two when the sidebar is narrow)
        Div status = new Div(read, new Span(" "), context);
        status.addClassName(BetaReadersStyles.HINT);
        VerticalLayout composer = new VerticalLayout(input, row, status);
        composer.setPadding(false);
        composer.setSpacing(false);
        composer.addClassNames(BetaReadersStyles.TOP, CssStyles.PADDING_COMPACT);
        return composer;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Asking
    // ------------------------------------------------------------------------------------------------------------

    @FunctionalInterface
    private interface Start {
        Asked start(ChatRunner.Listener listener) throws Exception;
    }

    private void send() {
        begin(listener -> service.ask(manuscript, chat, input.getValue(), includeParts.getValue(), listener), true);
    }

    private void regenerate() {
        begin(listener -> service.regenerate(manuscript, chat, listener), false);
    }

    private void begin(Start start, boolean clearInput) {
        if (generating) {
            return;
        }
        UI ui = UI.getCurrent();
        attributes = ThreadCopyRequestAttributes.create();
        try {
            Asked asked = start.start(listener(ui));
            if (clearInput) {
                input.clear();
            }
            rebuildMessages();
            live = new BetaMessageCard.Turn(chat);
            messages.add(live);
            running = asked.token();
            setGenerating(true);
            refreshStatus(false);
            scrollToEnd();
        } catch (NoModelException e) {
            Notification.warning(T.localized(THERE_IS_NO_MODEL));
        } catch (IllegalArgumentException e) {
            Notification.warning(T.localized(WRITE_A_MESSAGE_OR_INCLUDE));
        } catch (UnsupportedOperationException e) {
            Notification.warning(e.getMessage());
        } catch (Exception e) {
            Notification.error(T.localized(CANT_ASK_THE_MODEL).formatted(e.getMessage() != null ? e.getMessage() : e.toString()));
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
        setGenerating(false);
        // the reply is in the history now (the last turn is the one that can be asked for again)
        rebuildMessages();
        if (outcome.error() instanceof EmptyReplyException) {
            Notification.warning(T.localized(THE_MODEL_DID_NOT_ANSWER));
        } else if (outcome.error() != null) {
            UIUtils.inferenceError(loc, outcome.error());
        }
        if (outcome.prompt().dropped() > 0) {
            Notification.warning(T.localized(OLDER_MESSAGES_WERE_LEFT_OUT).formatted(outcome.prompt().dropped()));
        }
        // the reply ends on a thread of the inference service: the services need the request of the user
        try (InRequestScope _ = new InRequestScope(attributes)) {
            refreshStatus(true);
        }
        scrollToEnd();
    }

    private void setGenerating(boolean value) {
        generating = value;
        if (value) {
            addClassName(BetaReadersStyles.BUSY);
        } else {
            removeClassName(BetaReadersStyles.BUSY);
        }
        input.setEnabled(!value);
        includeParts.setEnabled(!value);
        checkParts.setEnabled(!value);
        showButton();
        onBusy.accept(value);
    }

    private void showButton() {
        send.setText(generating ? T.localized(STOP) : T.localized(SEND));
        send.setIcon((generating ? Solid.STOP : Solid.PAPER_PLANE).create());
    }

    // ------------------------------------------------------------------------------------------------------------
    // The history
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Draws the history again: after a message was edited or deleted, a reply was taken back or one was added.
     */
    private void rebuildMessages() {
        messages.removeAll();
        List<BetaMessage> history = chat.getMessages();
        for (int i = 0; i < history.size(); i++) {
            messages.add(BetaMessageCard.create(chat, history.get(i), actions, i == history.size() - 1));
        }
        if (unanswered(history)) {
            Div none = new Div(new Span(T.localized(THERE_IS_NO_REPLY_YET)));
            none.addClassName(BetaReadersStyles.HEADLINE);
            Button again = new Button(T.localized(ASK_AGAIN), Solid.ROTATE.create(), _ -> regenerate());
            again.addThemeName("tertiary");
            again.addThemeName("small");
            Div row = new Div(again);
            row.addClassName(BetaReadersStyles.ACTIONS);
            none.add(row);
            none.addClassName(BetaReadersStyles.HINT);
            messages.add(none);
        }
    }

    /**
     * Whether the last thing said in the conversation is a message of the user that has no reply (a persona joining or the change of the
     * medium after it does not take the question away).
     */
    private static boolean unanswered(List<BetaMessage> history) {
        for (int i = history.size() - 1; i >= 0; i--) {
            switch (history.get(i).getKind()) {
                case USER -> {
                    return true;
                }
                case ASSISTANT -> {
                    return false;
                }
                default -> {
                }
            }
        }
        return false;
    }

    private final BetaMessageCard.Actions actions = new BetaMessageCard.Actions() {
        @Override
        public void edit(BetaMessage message) {
            if (!generating) {
                editMessage(message);
            }
        }

        @Override
        public void delete(BetaMessage message) {
            if (!generating) {
                ConfirmDialog.show(T.localized(DELETE_THIS_MESSAGE), () -> {
                    ChatEdits.delete(chat, message.getId());
                    changed(true);
                });
            }
        }

        @Override
        public void deleteSpoken(BetaMessage turn, int index) {
            if (!generating) {
                ConfirmDialog.show(T.localized(DELETE_THIS_MESSAGE), () -> {
                    ChatEdits.deleteSpoken(chat, turn.getId(), index);
                    changed(false);
                });
            }
        }

        @Override
        public void regenerate() {
            ChatPanel.this.regenerate();
        }
    };

    /**
     * Stores the chat after the user changed its history and draws it again.
     *
     * @param partsBack whether what was sent of the story may have changed (a message that sent parts was deleted)
     */
    private void changed(boolean partsBack) {
        store();
        rebuildMessages();
        refreshStatus(partsBack);
    }

    private void store() {
        try {
            service.saveChat(manuscript, chat);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void editMessage(BetaMessage message) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(EDIT_MESSAGE));
        dialog.setWidth("460px");
        dialog.setMaxWidth("96vw");
        TextArea text = new TextArea();
        text.setWidthFull();
        text.setValue(message.getText());
        Button save = new Button(T.localized(SAVE), _ -> {
            try {
                ChatEdits.edit(chat, message.getId(), text.getValue());
            } catch (IllegalArgumentException e) {
                Notification.warning(T.localized(WRITE_A_MESSAGE_OR_INCLUDE));
                return;
            }
            dialog.close();
            changed(false);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(text);
        dialog.getFooter().add(new Button(T.localized(CANCEL), _ -> dialog.close()), save);
        dialog.open();
        text.focus();
    }

    // ------------------------------------------------------------------------------------------------------------
    // The personas
    // ------------------------------------------------------------------------------------------------------------

    private void bringIn() {
        if (generating) {
            return;
        }
        try {
            List<String> here = chat.present().stream().map(persona -> BetaReadersStore.nameKey(persona.getName())).toList();
            List<Persona> candidates = service.available(manuscript).stream().map(available -> available.persona())
                    .filter(persona -> !here.contains(BetaReadersStore.nameKey(persona.getName()))).toList();
            if (candidates.isEmpty()) {
                Notification.warning(T.localized(THERE_IS_NO_PERSONA));
                return;
            }
            new PresenceDialog(T.localized(BRING_IN_A_PERSONA), T.localized(BRING), candidates, (persona, note) -> {
                ChatEdits.join(chat, persona, note);
                personaChanged();
            }).open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void sendAway(Persona persona) {
        if (generating) {
            return;
        }
        new PresenceDialog(T.localized(SEND_AWAY_S).formatted(persona.getName()), T.localized(SEND_AWAY), null, (_, note) -> {
            try {
                ChatEdits.leave(chat, persona.getUuid(), note);
            } catch (IllegalStateException e) {
                Notification.warning(T.localized(THE_LAST_PERSONA_CANT_LEAVE));
                return;
            }
            personaChanged();
        }).open();
    }

    private void personaChanged() {
        store();
        rebuildHeader();
        rebuildMessages();
        refreshStatus(false);
        scrollToEnd();
    }

    private void updateFromLibrary(Persona persona) {
        if (generating) {
            return;
        }
        try {
            if (service.refreshPersona(manuscript, chat, persona.getUuid())) {
                Notification.success(T.localized(IS_UPDATED_FROM_THE_LIBRARY).formatted(persona.getName()));
                rebuildHeader();
                refreshStatus(false);
            } else {
                Notification.warning(T.localized(IS_NOT_IN_THE_LIBRARY).formatted(persona.getName()));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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
            String sent = PartsTracker.read(chat);
            read.setText(sent.isEmpty() ? "" : T.localized(THE_PERSONAS_HAVE_READ_PARTS).formatted(sent));
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
                    ? T.localized(CONTEXT_OF_TOKENS).formatted(format.format(usage.tokens()), format.format(usage.limit()))
                    : T.localized(CONTEXT_TOKENS).formatted(format.format(usage.tokens())));
        } catch (NoModelException e) {
            context.setText(T.localized(THERE_IS_NO_MODEL));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static String partsLabel(Status status) {
        if (status.first() && status.count() > 0) {
            return T.localized(INCLUDE_THE_STORY_PARTS).formatted(status.count());
        }
        if (status.count() > 0) {
            return T.localized(INCLUDE_THE_LATEST_PARTS_NEW).formatted(status.count());
        }
        return status.pending() ? T.localized(INCLUDE_THE_LATEST_PARTS_THE_STORY)
                : T.localized(INCLUDE_THE_LATEST_PARTS_NONE);
    }

    private void scrollToEnd() {
        scroller.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    public BetaChat getChat() {
        return chat;
    }
}
