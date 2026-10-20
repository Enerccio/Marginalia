package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.service.agent.RunToolsImpl;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * Puts the messages of a conversation of an agent into a box as the blocks the console shows: what was said as bubbles (the
 * markdown of it), the reasoning and the commands called folded, a command's result in the body of the command, the report and
 * the question of the agent as bubbles of their own. The console uses it for the transcript of the run, the tab of the
 * sub-agents for each of their conversations.
 */
class AgentTranscriptRenderer {

    /**
     * How much of a result of a command is shown; the model got it whole, up to its own cut.
     */
    static final int RESULT_LIMIT = 20_000;

    private static final int SUMMARY_LIMIT = 80;

    private final Localization loc;
    private final VerticalLayout box;
    // the bodies of the folded commands by the id of the call, where their results go when they come
    private final Map<String, VerticalLayout> callBodies = new HashMap<>();
    // the calls shown as the report or the question of the agent
    private final Set<String> specialCalls = new HashSet<>();
    private String userLabel;
    private String assistantLabel;
    // told the id of a spawn_agent call when its button is pressed, null when the calls have no button
    private Consumer<String> onSubAgent;

    /**
     * @param box the box the blocks are added to
     */
    AgentTranscriptRenderer(Localization loc, VerticalLayout box) {
        this.loc = loc;
        this.box = box;
        this.userLabel = loc.getValue(L.LABEL_YOU);
        this.assistantLabel = loc.getValue(L.ENUM_DOMAIN_AGENT);
    }

    VerticalLayout getBox() {
        return box;
    }

    /**
     * Who the messages of the user and of the model are shown as; the conversation of a sub-agent is not with the author.
     */
    void setLabels(String userLabel, String assistantLabel) {
        this.userLabel = userLabel;
        this.assistantLabel = assistantLabel;
    }

    /**
     * The calls of {@code spawn_agent} get a button that tells this the id of the call.
     */
    void setOnSubAgent(Consumer<String> onSubAgent) {
        this.onSubAgent = onSubAgent;
    }

    /**
     * Removes what was shown, the commands that wait for a result too.
     */
    void clear() {
        box.removeAll();
        callBodies.clear();
        specialCalls.clear();
    }

    void render(LLMChatMessage message, String reasoning, boolean summary, Date created) {
        switch (message.getRole()) {
            case USER -> box.add(stamped(summary ? summaryBubble(message.getContent())
                    : isAuthorCommand(message.getContent()) ? authorCommandBubble(message.getContent())
                    : bubble(userLabel, message.getContent(), true), created));
            case ASSISTANT -> {
                if (StringUtils.isNotBlank(reasoning)) {
                    box.add(stamped(reasoningBlock(reasoning), created));
                }
                if (StringUtils.isNotBlank(message.getContent())) {
                    box.add(stamped(bubble(assistantLabel, message.getContent(), false), created));
                }
                for (LLMToolCall call : message.getToolCalls()) {
                    Div special = specialCall(call);
                    if (special == null) {
                        box.add(stamped(callBlock(call), created));
                    } else {
                        specialCalls.add(call.getId());
                        box.add(stamped(special, created));
                    }
                }
            }
            case TOOL -> {
                VerticalLayout body = callBodies.get(message.getToolCallId());
                if (specialCalls.contains(message.getToolCallId()) && RunToolsImpl.isAcknowledgement(message.getContent())) {
                    // the report or the question is shown as itself, its acknowledgement says nothing more
                    return;
                }
                if (body != null) {
                    body.add(stamped(text(loc.getValue(L.LABEL_RESULT), true), created), text(cut(message.getContent()), false));
                } else {
                    box.add(stamped(bubble(loc.getValue(L.LABEL_RESULT), cut(message.getContent()), false), created));
                }
            }
            default -> {
                // the system message is made for every request and not part of the transcript
            }
        }
    }

    /**
     * Puts the time a message was written at the right of the first line of its block: the time of day, with the date when it was
     * another day (the whole time is the tooltip).
     */
    static <T extends Component> T stamped(T block, Date created) {
        if (created == null) {
            return block;
        }
        boolean today = DateUtils.isSameDay(created, new Date());
        Span time = new Span((today ? DateFormat.getTimeInstance(DateFormat.MEDIUM)
                : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)).format(created));
        time.addClassName(CssStyles.AGENT_TIME);
        time.getElement().setAttribute("title", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(created));
        block.getElement().insertChild(0, time.getElement());
        return block;
    }

    private static boolean isAuthorCommand(String content) {
        return content != null && content.startsWith(Defaults.AGENT_AUTHOR_COMMAND_NOTE);
    }

    /**
     * A command the user ran from the bar: the line and what it answered, not the note the agent reads with it.
     */
    private Div authorCommandBubble(String content) {
        String body = content.substring(Defaults.AGENT_AUTHOR_COMMAND_NOTE.length()).strip();
        int result = body.indexOf("\n\nResult:\n");
        String line = (result < 0 ? body : body.substring(0, result)).replaceFirst("^Command:\\s*", "");
        Div bubble = new Div(text(loc.getValue(L.LABEL_COMMAND) + ": " + line, true));
        if (result >= 0) {
            bubble.add(text(cut(body.substring(result + "\n\nResult:\n".length())), false));
        }
        bubble.addClassNames(CssStyles.HELP_MESSAGE, CssStyles.HELP_MESSAGE_USER);
        return bubble;
    }

    /**
     * What a compaction left of the earlier work: folded to one line, the summary is long and for looking up.
     */
    private Div summaryBubble(String content) {
        Details details = new Details(loc.getValue(L.LABEL_AGENT_SUMMARY), new Markdown(StringUtils.defaultString(content)));
        Div block = new Div(details);
        block.addClassNames(CssStyles.HELP_MESSAGE, CssStyles.HELP_MESSAGE_USER, CssStyles.HELP_MESSAGE_FOLDED);
        return block;
    }

    Div bubble(String who, String content, boolean user) {
        Div bubble = new Div(text(who, true), new Markdown(StringUtils.defaultString(content)));
        bubble.addClassName(CssStyles.HELP_MESSAGE);
        if (user) {
            bubble.addClassName(CssStyles.HELP_MESSAGE_USER);
        }
        return bubble;
    }

    /**
     * What the model thought before its answer, folded to one line: the reasoning is long and for looking up.
     */
    private Div reasoningBlock(String reasoning) {
        Details details = new Details(loc.getValue(L.LABEL_REASONING), text(reasoning, false));
        details.addClassName(CssStyles.HINT_TEXT);
        Div block = new Div(details);
        block.addClassName(CssStyles.HELP_MESSAGE);
        return block;
    }

    /**
     * The report ({@code finish}) or the question ({@code ask_user}) of the agent as a bubble of its own, where it was
     * written: it stays in the transcript when the run goes on, unlike the state of the run, which holds only the last.
     *
     * @return null for any other call, and for these when the call has no text (it failed, the result says why)
     */
    private Div specialCall(LLMToolCall call) {
        boolean report = "finish".equals(call.getName());
        if (!report && !"ask_user".equals(call.getName())) {
            return null;
        }
        String text;
        try {
            JsonObject arguments = JsonParser.parseString(StringUtils.defaultString(call.getArguments())).getAsJsonObject();
            if (report) {
                text = arguments.has("summary") ? arguments.get("summary").getAsString() : null;
            } else {
                List<String> options = new ArrayList<>();
                if (arguments.has("options") && arguments.get("options").isJsonArray()) {
                    arguments.getAsJsonArray("options").forEach(option -> options.add(option.getAsString()));
                }
                text = arguments.has("question") ? RunToolsImpl.questionText(arguments.get("question").getAsString(), options) : null;
            }
        } catch (RuntimeException e) {
            return null;
        }
        if (StringUtils.isBlank(text)) {
            return null;
        }
        Div bubble = new Div(text(loc.getValue(report ? L.LABEL_AGENT_REPORT : L.LABEL_AGENT_QUESTION), true), new Markdown(text.strip()));
        bubble.addClassName(CssStyles.HELP_MESSAGE);
        return bubble;
    }

    private Div callBlock(LLMToolCall call) {
        VerticalLayout body = new VerticalLayout(text(StringUtils.defaultString(call.getArguments()), false));
        body.setPadding(false);
        body.setSpacing(false);
        callBodies.put(call.getId(), body);
        Details details = new Details(call.getName() + " " + StringUtils.abbreviate(
                StringUtils.defaultString(call.getArguments()).replaceAll("\\s+", " "), SUMMARY_LIMIT), body);
        details.addClassName(CssStyles.TEXT_MONOSPACE);
        Div block = new Div(details);
        block.addClassName(CssStyles.HELP_MESSAGE);
        if (onSubAgent != null && "spawn_agent".equals(call.getName())) {
            Button open = new Button(loc.getValue(L.LABEL_AGENT_SUBAGENT), Solid.ROBOT.create(), event -> onSubAgent.accept(call.getId()));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            block.add(open);
        }
        return block;
    }

    /**
     * A block of text of a message; the heading (who said it) and the text are blocks, so they are on lines of their own.
     */
    Div text(String content, boolean heading) {
        Div block = new Div(content);
        block.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE);
        if (heading) {
            block.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_SECONDARY, CssStyles.TEXT_XS);
        }
        return block;
    }

    static String cut(String content) {
        String text = StringUtils.defaultString(content);
        return text.length() > RESULT_LIMIT ? text.substring(0, RESULT_LIMIT) + "\n..." : text;
    }
}
