package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.bound.ProblemLog;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.bound.UiProblem;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService.Info;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.manual.UiStateSource;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMSnapshot;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.github.enerccio.marginalia.utils.UiProblems;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ElementUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The help assistant: a round robot button fixed in the bottom right corner of the page that opens a chat panel above
 * it. The questions are answered from the manual by the AI an administrator chose ({@link HelpAssistantService}); the
 * panel says which AI that is and warns when the user's limit applies to the questions. The conversation is kept in
 * the session ({@link SessionPoint#getHelpChat()}), so it survives closing the panel and reloading the page, and is
 * shown again when the panel is opened. The button is not shown at all while the assistant is off.
 */
@Configurable(preConstruction = true)
@Extendable
public class HelpAssistantWidget extends Div {
    private static final Logger log = LoggerFactory.getLogger(HelpAssistantWidget.class);

    /** How many of the last messages of the conversation are sent with a question. */
    private static final int MAX_HISTORY = 20;

    /** The longest state of the screen the model gets in one answer; it asks for a path below for the rest. */
    private static final int MAX_UI_STATE_CHARS = 12000;

    /** The most problems of the session the model can read at once. */
    private static final int MAX_PROBLEMS = 10;

    private static final String PAGE_STATE = "Everything in the browser window of the user: the main window and the "
            + "dialogs that are open on top of it.";

    @Autowired
    private Localization loc;

    @Autowired
    private HelpAssistantService helpService;

    @Autowired
    private SessionPoint sessionPoint;

    // the list of the session, kept here: the answers arrive on threads that have no session
    private final List<LLMChatMessage> history;
    // the same for the problems the user was shown
    private final ProblemLog problems;

    private final Button fab;
    private Div panel;
    private Div messages;
    private Span infoSpan;
    private Div warning;
    private Checkbox booksBox;
    private boolean loadingBooks;
    private TextField input;
    private Button sendButton;
    private Button clearButton;

    private volatile boolean generating;
    private volatile boolean cancelled;
    private volatile CancellationToken token;

    public HelpAssistantWidget() {
        history = sessionPoint.getHelpChat();
        problems = sessionPoint.getProblems();
        addClassName(CssStyles.HELP_ROOT);

        fab = new Button(Solid.ROBOT.create(), e -> toggle());
        fab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fab.addClassName(CssStyles.HELP_FAB);
        fab.setTooltipText(loc.getValue(L.LABEL_HELP_ASSISTANT));

        buildPanel();
        add(fab, panel);
        setVisible(isAvailable());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private boolean isAvailable() {
        try {
            return helpService.isAvailable();
        } catch (Exception e) {
            log.warn("Could not find out whether the help assistant is available", e);
            return false;
        }
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "title", type = Span.class),
                    @APILocals(name = "close", type = Button.class),
                    @APILocals(name = "header", type = HorizontalLayout.class),
                    @APILocals(name = "infoRow", type = Div.class),
                    @APILocals(name = "booksNotice", type = Span.class),
                    @APILocals(name = "booksRow", type = Div.class),
                    @APILocals(name = "inputRow", type = HorizontalLayout.class)})
    private void buildPanel() {
        Span title = new Span(loc.getValue(L.LABEL_HELP_ASSISTANT));

        clearButton = new Button(Solid.BROOM.create(), e -> clearChat());
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        clearButton.setTooltipText(loc.getValue(L.LABEL_CLEAR));

        Button close = new Button(Solid.TIMES.create(), e -> panel.setVisible(false));
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        close.setTooltipText(loc.getValue(L.LABEL_CLOSE));

        HorizontalLayout header = new HorizontalLayout(title, clearButton, close);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.addClassName(CssStyles.HELP_HEADER);

        infoSpan = new Span();
        infoSpan.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);
        Div infoRow = new Div(infoSpan);
        infoRow.addClassName(CssStyles.PADDING_COMPACT);

        // the books of the user go to the AI that answers, so it is the user's choice, kept in their settings
        booksBox = new Checkbox(loc.getValue(L.LABEL_HELP_BOOKS_ACCESS));
        booksBox.addValueChangeListener(e -> {
            if (!loadingBooks) {
                saveBooksAllowed(e.getValue());
            }
        });
        Span booksNotice = new Span(loc.getValue(L.MSG_HELP_BOOKS_NOTICE));
        booksNotice.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);
        Div booksRow = new Div(booksBox, booksNotice);
        booksRow.addClassName(CssStyles.PADDING_COMPACT);

        warning = new Div(new Span(loc.getValue(L.MSG_HELP_LIMITS_APPLY)));
        warning.addClassName(CssStyles.HELP_WARNING);
        warning.setVisible(false);

        messages = new Div();
        messages.addClassName(CssStyles.HELP_MESSAGES);

        input = new TextField();
        input.setPlaceholder(loc.getValue(L.LABEL_ASK_ABOUT_MARGINALIA));
        input.setClearButtonVisible(true);
        input.setWidthFull();
        input.addKeyDownListener(Key.ENTER, e -> send());

        sendButton = new Button(Solid.PAPER_PLANE.create(), e -> send());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Tooltip.forComponent(sendButton).setText(loc.getValue(L.LABEL_SEND));

        HorizontalLayout inputRow = new HorizontalLayout(input, sendButton);
        inputRow.setWidthFull();
        inputRow.setAlignItems(FlexComponent.Alignment.CENTER);
        inputRow.expand(input);
        inputRow.addClassName(CssStyles.HELP_INPUT);

        panel = new Div(header, infoRow, booksRow, warning, messages, inputRow);
        panel.addClassName(CssStyles.HELP_PANEL);
        panel.setVisible(false);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void toggle() {
        if (panel.isVisible()) {
            panel.setVisible(false);
        } else {
            open();
        }
    }

    /**
     * Shows the panel with the conversation. Asked again at each opening, so that a change an administrator made
     * (the AI, the limits, turned off) is seen without a reload.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "info", type = HelpAssistantService.Info.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "ai", type = String.class)})
    private void open() {
        Info info;
        try {
            info = helpService.getInfo();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        if (info == null) {
            setVisible(false);
            return;
        }
        String ai = info.model() == null || info.model().equals(info.aiName())
                ? info.aiName() : info.aiName() + " (" + info.model() + ")";
        infoSpan.setText(String.format(loc.getValue(L.MSG_HELP_ANSWERING_WITH), ai));
        warning.setVisible(info.metered());
        loadBooksAllowed();
        panel.setVisible(true);
        if (!generating) {
            renderHistory();
        }
        JavascriptLibrary.scrollToBottomAfterLayout(messages.getElement());
        input.focus();
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void loadBooksAllowed() {
        loadingBooks = true;
        try {
            booksBox.setValue(helpService.isBooksAllowed());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loadingBooks = false;
        }
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "allowed", type = boolean.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void saveBooksAllowed(boolean allowed) {
        try {
            helpService.setBooksAllowed(allowed);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "copy", type = List.class),
                    @APILocals(name = "message", type = LLMChatMessage.class)})
    private void renderHistory() {
        messages.removeAll();
        List<LLMChatMessage> copy;
        synchronized (history) {
            copy = new ArrayList<>(history);
        }
        if (copy.isEmpty()) {
            messages.add(assistantCard(loc.getValue(L.MSG_HELP_WELCOME)));
            return;
        }
        for (LLMChatMessage message : copy) {
            if (message.getRole() == LLMRole.USER) {
                messages.add(userCard(message.getContent()));
            } else if (message.getRole() == LLMRole.ASSISTANT) {
                messages.add(assistantCard(message.getContent()));
            }
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Div.class),
            parameters = {@APIParameter(name = "text", type = String.class)},
            locals = {@APILocals(name = "card", type = Div.class)})
    private Div userCard(String text) {
        Div card = new Div();
        card.setText(text);
        card.addClassNames(CssStyles.HELP_MESSAGE, CssStyles.HELP_MESSAGE_USER);
        return card;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Markdown.class),
            parameters = {@APIParameter(name = "text", type = String.class)},
            locals = {@APILocals(name = "card", type = Markdown.class)})
    private Markdown assistantCard(String text) {
        Markdown card = new Markdown(text);
        card.addClassName(CssStyles.HELP_MESSAGE);
        return card;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void clearChat() {
        if (generating) {
            stop();
        }
        history.clear();
        renderHistory();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "current", type = CancellationToken.class)})
    private void stop() {
        cancelled = true;
        CancellationToken current = token;
        if (current != null) {
            current.cancel();
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "text", type = String.class),
                    @APILocals(name = "question", type = LLMChatMessage.class),
                    @APILocals(name = "questionCard", type = Div.class),
                    @APILocals(name = "answerCard", type = Markdown.class),
                    @APILocals(name = "answer", type = HelpAssistantWidget.Answer.class),
                    @APILocals(name = "ui", type = UI.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void send() {
        if (generating) {
            stop();
            return;
        }
        String text = StringUtils.strip(input.getValue());
        if (StringUtils.isEmpty(text)) {
            return;
        }
        input.clear();
        if (history.isEmpty()) {
            messages.removeAll();
        }
        LLMChatMessage question = LLMChatMessage.of(LLMRole.USER, text);
        history.add(question);
        Div questionCard = userCard(text);
        Markdown answerCard = assistantCard("…");
        messages.add(questionCard, answerCard);
        JavascriptLibrary.scrollToBottom(messages.getElement());
        setGenerating(true);
        cancelled = false;

        Answer answer = new Answer(UI.getCurrent(), question, text, questionCard, answerCard);
        try {
            UI ui = UI.getCurrent();
            token = helpService.ask(recentHistory(), screenOf(ui, problems), answer);
        } catch (Exception e) {
            answer.fail(e);
        }
    }

    /**
     * The screen of the page for the model: it reads and moves through the components on the thread of the page, under
     * its lock, whichever thread the function runs on.
     */
    private static UiStateSource screenOf(UI ui, ProblemLog problems) {
        return new UiStateSource() {
            @Override
            public void read(String path, boolean deep, boolean withInteractions, FunctionContinuation<String> done) {
                onPage(ui, done, "The screen could not be read.",
                        () -> readUiStateNow(ui, path, deep, withInteractions));
            }

            @Override
            public void interact(String path, List<String> interactions, FunctionContinuation<String> done) {
                onPage(ui, done, "The screen could not be operated.", () -> interactNow(ui, path, interactions));
            }

            @Override
            public void readProblems(int count, FunctionContinuation<String> done) {
                try {
                    ui.access(() -> {
                        List<UiProblem> session = problems.last(MAX_PROBLEMS);
                        // the browser answers later; what it kept is added to what the session knows
                        JavascriptLibrary.readRememberedProblems(ui.getPage()).then(String.class,
                                json -> done.complete(problemsText(session, json, count)),
                                error -> done.complete(problemsText(session, null, count)));
                    });
                } catch (UIDetachedException e) {
                    done.fail("The page of the user is closed.");
                }
            }
        };
    }

    /** Runs the work on the page's thread and answers the continuation with its text, or with why it failed. */
    private static void onPage(UI ui, FunctionContinuation<String> done, String failure, Supplier<String> work) {
        try {
            ui.access(() -> {
                try {
                    done.complete(work.get());
                } catch (IllegalArgumentException e) {
                    done.fail(e.getMessage());
                } catch (RuntimeException e) {
                    log.warn("The help assistant could not use the screen", e);
                    done.fail(failure);
                }
            });
        } catch (UIDetachedException e) {
            done.fail("The page of the user is closed.");
        }
    }

    /**
     * The state of the page as Markdown, or of the element at the path. Must run on the thread of the page.
     *
     * @throws IllegalArgumentException when there is no element with the path (the message lists the ones there are)
     */
    static String readUiStateNow(UI ui, String path, boolean deep) {
        return readUiStateNow(ui, path, deep, false);
    }

    /** As {@link #readUiStateNow(UI, String, boolean)}, with what the elements offer to do when asked. */
    static String readUiStateNow(UI ui, String path, boolean deep, boolean withInteractions) {
        return page(ui, deep).read(path, MAX_UI_STATE_CHARS, withInteractions);
    }

    /**
     * Applies the interactions to the element at the path, and returns the state of the whole page after them (a move
     * can open something elsewhere), with the interactions that are offered now. Must run on the thread of the page.
     *
     * @throws IllegalArgumentException when there is no such element or interaction (the message says what there is)
     */
    static String interactNow(UI ui, String path, List<String> interactions) {
        page(ui, false).interact(path, interactions);
        return readUiStateNow(ui, "", false, true);
    }

    /**
     * The last problems as Markdown: the ones of the session and the ones the browser kept (the text of its JSON), each
     * once, newest first.
     */
    static String problemsText(List<UiProblem> session, String browserJson, int count) {
        int wanted = count < 1 ? UiProblems.defaultCount() : count;
        return UiProblems.toMarkdown(UiProblems.merge(session, UiProblems.fromBrowser(browserJson), wanted),
                System.currentTimeMillis());
    }

    private static LLMSnapshot page(UI ui, boolean deep) {
        return LLMSnapshot.ofComponentTree("UI", PAGE_STATE, ui, deep);
    }

    /** The last messages of the conversation, starting with a question of the user. */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class),
            locals = {@APILocals(name = "copy", type = List.class)})
    private List<LLMChatMessage> recentHistory() {
        List<LLMChatMessage> copy;
        synchronized (history) {
            copy = new ArrayList<>(history.subList(Math.max(0, history.size() - MAX_HISTORY), history.size()));
        }
        while (!copy.isEmpty() && copy.getFirst().getRole() != LLMRole.USER) {
            copy.removeFirst();
        }
        return copy;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "value", type = boolean.class)})
    private void setGenerating(boolean value) {
        generating = value;
        sendButton.setIcon((value ? Solid.STOP : Solid.PAPER_PLANE).create());
        Tooltip.forComponent(sendButton).setText(loc.getValue(value ? L.LABEL_STOP : L.LABEL_SEND));
        clearButton.setEnabled(!value);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "attachEvent", type = AttachEvent.class)})
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        cancelled = false;
        UiProblems.attach(sessionPoint);
        // a link of an answer (a page of the manual) opens in a new tab, the application stays where it is
        JavascriptLibrary.openLinksInNewTab(messages.getElement());
        // a dialog (a book is one) blocks the page, the assistant has to stay usable above it: on the server (a strict
        // dialog makes everything else inert, the clicks and the typing would be dropped) and in the browser (it is
        // in the top layer, over the button and the panel)
        ElementUtil.setIgnoreParentInert(getElement(), true);
        JavascriptLibrary.keepOnTopOfDialogs(getElement());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "detachEvent", type = DetachEvent.class)})
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        stop();
    }

    /**
     * One answer being streamed into its card. The callbacks come from other threads, so every change of the page
     * goes through {@code ui.access}.
     */
    private final class Answer implements InferenceAsyncCallback {
        private final UI ui;
        private final LLMChatMessage question;
        private final String questionText;
        private final Div questionCard;
        private final Markdown answerCard;
        private final StringBuilder text = new StringBuilder();

        Answer(UI ui, LLMChatMessage question, String questionText, Div questionCard, Markdown answerCard) {
            this.ui = ui;
            this.question = question;
            this.questionText = questionText;
            this.questionCard = questionCard;
            this.answerCard = answerCard;
        }

        @Override
        public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String chunk) {
            ui.access(() -> {
                if (chunkType == ChunkType.RESPONSE) {
                    text.append(chunk);
                    answerCard.setContent(text.toString());
                    JavascriptLibrary.scrollToBottomIfAtBottom(messages.getElement());
                    UIPushGuard.push(ui);
                }
                controller.continueInference();
            });
        }

        @Override
        public void onCompletion() {
            ui.access(this::finish);
        }

        @Override
        public void onCancel() {
            ui.access(this::finish);
        }

        @Override
        public void onError(Throwable exception) {
            ui.access(() -> fail(exception));
        }

        @Override
        public boolean isDead() {
            return cancelled;
        }

        /** The answer ends, whole or stopped: a stopped one that said something stays, an empty one is taken back. */
        private void finish() {
            String answer = text.toString();
            if (answer.isBlank()) {
                takeBack(null);
                return;
            }
            history.add(LLMChatMessage.of(LLMRole.ASSISTANT, answer));
            done();
        }

        /** The request failed or was refused (the limit): the question is taken back and the reason shown. */
        void fail(Throwable exception) {
            if (UI.getCurrent() == null) {
                ui.access(() -> fail(exception));
                return;
            }
            takeBack(InferenceErrors.messageOf(loc, exception));
        }

        private void takeBack(String error) {
            history.remove(question);
            messages.remove(questionCard);
            if (error == null) {
                messages.remove(answerCard);
            } else {
                answerCard.setContent(error);
                answerCard.addClassName(CssStyles.HELP_MESSAGE_ERROR);
            }
            // so that it can be sent again
            if (input.isEmpty()) {
                input.setValue(questionText);
            }
            done();
        }

        private void done() {
            token = null;
            setGenerating(false);
            JavascriptLibrary.scrollToBottomIfAtBottom(messages.getElement());
            UIPushGuard.push(ui);
        }
    }
}
