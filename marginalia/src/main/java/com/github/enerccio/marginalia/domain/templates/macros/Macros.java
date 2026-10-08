package com.github.enerccio.marginalia.domain.templates.macros;

import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import com.github.enerccio.marginalia.loc.L;

import java.util.*;

/**
 * Registry of SillyTavern compatible macros supported by Marginalia.
 * <p>
 * Macros that make sense for a story writing tool are implemented in {@link TemplateData}. Macros that only make sense
 * for SillyTavern chats (instruct sequences, swipes, author's notes, character card fields Marginalia does not have...)
 * are registered as "ignored" - they render to an empty string, so lorebooks imported from SillyTavern do not leak raw
 * macros into the prompt.
 */
public final class Macros {

    /**
     * Name of the conditional macro. It is handled by the translator/helpers directly.
     */
    public static final String IF = "if";

    @FunctionalInterface
    public interface MacroFunction {
        String apply(TemplateData data, MacroCall call);
    }

    /**
     * @param site        index of the macro call in the template (used for stable {{pick}})
     * @param templateKey identifier of the template the call is in
     * @param args        evaluated arguments
     */
    public record MacroCall(int site, String templateKey, List<String> args) {

        public String arg(int i) {
            return i < args.size() ? args.get(i) : null;
        }

        public String argOrEmpty(int i) {
            return Objects.toString(arg(i), "");
        }
    }

    /**
     * @param name        canonical (lower case) name
     * @param signature   how the macro is shown in the UI
     * @param minArgs     minimal number of arguments
     * @param maxArgs     maximal number of arguments, -1 for unlimited
     * @param description localized description, {@code null} for aliases/ignored macros not shown in the UI
     */
    public record MacroDefinition(String name, String signature, int minArgs, int maxArgs, L description,
                                  MacroFunction function) {

        public boolean acceptsArguments() {
            return maxArgs != 0;
        }
    }

    /**
     * Entry for the UI hint list.
     */
    public record MacroHint(String signature, L description) {
    }

    private static final Map<String, MacroDefinition> MACROS = new LinkedHashMap<>();
    private static final List<MacroHint> HINTS = new ArrayList<>();

    private static void define(String signature, int min, int max, L description, MacroFunction function, String... names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i].toLowerCase(Locale.ROOT);
            MACROS.put(name, new MacroDefinition(name, signature, min, max, i == 0 ? description : null, function));
        }
        if (description != null) {
            HINTS.add(new MacroHint(signature, description));
        }
    }

    private static void ignored(String result, int max, String... names) {
        for (String name : names) {
            String lower = name.toLowerCase(Locale.ROOT);
            MACROS.put(lower, new MacroDefinition(lower, "{{" + name + "}}", 0, max, null, (_, _) -> result));
        }
    }

    private static void variable(Scope scope, String prefix, L get, L set, L add, L inc, L dec, L has, L delete) {
        define("{{get" + prefix + "var::name}}", 1, 1, get, (d, c) -> d.getVar(scope, c.argOrEmpty(0)), "get" + prefix + "var");
        define("{{set" + prefix + "var::name::value}}", 2, 2, set, (d, c) -> d.setVar(scope, c.argOrEmpty(0), c.argOrEmpty(1)), "set" + prefix + "var");
        define("{{add" + prefix + "var::name::value}}", 2, 2, add, (d, c) -> d.addVar(scope, c.argOrEmpty(0), c.argOrEmpty(1)), "add" + prefix + "var");
        define("{{inc" + prefix + "var::name}}", 1, 1, inc, (d, c) -> d.incVar(scope, c.argOrEmpty(0)), "inc" + prefix + "var");
        define("{{dec" + prefix + "var::name}}", 1, 1, dec, (d, c) -> d.decVar(scope, c.argOrEmpty(0)), "dec" + prefix + "var");
        define("{{has" + prefix + "var::name}}", 1, 1, has, (d, c) -> d.hasVar(scope, c.argOrEmpty(0)), "has" + prefix + "var");
        define("{{delete" + prefix + "var::name}}", 1, 1, delete, (d, c) -> d.deleteVar(scope, c.argOrEmpty(0)), "delete" + prefix + "var", "flush" + prefix + "var");
    }

    static {
        // names & participants
        define("{{user}}", 0, 0, L.DESC_MACRO_USER, (d, _) -> d.user(), "user");
        define("{{char}}", 0, 0, L.DESC_MACRO_CHAR, (d, _) -> d.character(), "char", "charIfNotGroup");
        define("{{group}}", 0, 0, L.DESC_MACRO_GROUP, (d, _) -> d.group(), "group", "groupNotMuted");
        define("{{notChar}}", 0, 0, L.DESC_MACRO_NOT_CHAR, (d, _) -> d.notCharacter(), "notChar");
        define("{{description}}", 0, 0, L.DESC_MACRO_DESCRIPTION, (d, _) -> d.description(), "description");
        define("{{scenario}}", 0, 0, L.DESC_MACRO_SCENARIO, (d, _) -> d.scenario(), "scenario");
        define("{{input}}", 0, 0, L.DESC_MACRO_INPUT, (d, _) -> d.input(), "input");

        // story history
        define("{{lastMessage}}", 0, 0, L.DESC_MACRO_LAST_MESSAGE, (d, _) -> d.lastMessage(), "lastMessage", "lastCharMessage");
        define("{{lastMessageId}}", 0, 0, L.DESC_MACRO_LAST_MESSAGE_ID, (d, _) -> d.lastMessageId(), "lastMessageId");
        define("{{lastUserMessage}}", 0, 0, L.DESC_MACRO_LAST_USER_MESSAGE, (d, _) -> d.lastUserMessage(), "lastUserMessage");
        define("{{summary}}", 0, 0, L.DESC_MACRO_SUMMARY, (d, _) -> d.summary(), "summary");

        // time & date
        define("{{time}} / {{time::UTC+2}}", 0, 1, L.DESC_MACRO_TIME, (d, c) -> d.time(c.arg(0)), "time");
        define("{{date}}", 0, 0, L.DESC_MACRO_DATE, (d, _) -> d.date(), "date");
        define("{{weekday}}", 0, 0, L.DESC_MACRO_WEEKDAY, (d, _) -> d.weekday(), "weekday");
        define("{{isotime}}", 0, 0, L.DESC_MACRO_ISOTIME, (d, _) -> d.isoTime(), "isotime");
        define("{{isodate}}", 0, 0, L.DESC_MACRO_ISODATE, (d, _) -> d.isoDate(), "isodate");
        define("{{datetimeformat::YYYY-MM-DD HH:mm}}", 1, 1, L.DESC_MACRO_DATETIMEFORMAT, (d, c) -> d.dateTimeFormat(c.arg(0)), "datetimeformat");
        define("{{idleDuration}}", 0, 0, L.DESC_MACRO_IDLE_DURATION, (d, _) -> d.idleDuration(), "idleDuration", "idle_duration");
        define("{{timeDiff::left::right}}", 2, 2, L.DESC_MACRO_TIME_DIFF, (d, c) -> d.timeDiff(c.arg(0), c.arg(1)), "timeDiff");

        // variables
        variable(Scope.LOCAL, "", L.DESC_MACRO_GETVAR, L.DESC_MACRO_SETVAR, L.DESC_MACRO_ADDVAR, L.DESC_MACRO_INCVAR,
                L.DESC_MACRO_DECVAR, L.DESC_MACRO_HASVAR, L.DESC_MACRO_DELETEVAR);
        variable(Scope.GLOBAL, "global", L.DESC_MACRO_GETGLOBALVAR, L.DESC_MACRO_SETGLOBALVAR, L.DESC_MACRO_ADDGLOBALVAR,
                L.DESC_MACRO_INCGLOBALVAR, L.DESC_MACRO_DECGLOBALVAR, L.DESC_MACRO_HASGLOBALVAR, L.DESC_MACRO_DELETEGLOBALVAR);

        // randomization
        define("{{random::a::b::c}}", 1, -1, L.DESC_MACRO_RANDOM, (d, c) -> d.random(c.args()), "random");
        define("{{pick::a::b::c}}", 1, -1, L.DESC_MACRO_PICK, (d, c) -> d.pick(c.args(), c.templateKey() + "#" + c.site()), "pick");
        define("{{roll::1d20}}", 1, 1, L.DESC_MACRO_ROLL, (d, c) -> d.roll(c.arg(0)), "roll");

        // runtime state
        define("{{maxPrompt}}", 0, 0, L.DESC_MACRO_MAX_PROMPT, (d, _) -> d.maxPrompt(), "maxPrompt");
        define("{{maxContextTokens}}", 0, 0, L.DESC_MACRO_MAX_CONTEXT_TOKENS, (d, _) -> d.maxContextTokens(), "maxContextTokens", "maxContext");
        define("{{maxResponseTokens}}", 0, 0, L.DESC_MACRO_MAX_RESPONSE_TOKENS, (d, _) -> d.maxResponseTokens(), "maxResponseTokens", "maxResponse");
        define("{{model}}", 0, 0, L.DESC_MACRO_MODEL, (d, _) -> d.model(), "model");
        define("{{lastGenerationType}}", 0, 0, L.DESC_MACRO_LAST_GENERATION_TYPE, (d, _) -> d.lastGenerationType(), "lastGenerationType");

        // utility
        define("{{newline}} / {{newline::2}}", 0, 1, L.DESC_MACRO_NEWLINE, (d, c) -> d.newline(c.arg(0)), "newline");
        define("{{space}} / {{space::2}}", 0, 1, L.DESC_MACRO_SPACE, (d, c) -> d.space(c.arg(0)), "space");
        define("{{noop}}", 0, 0, L.DESC_MACRO_NOOP, (d, _) -> d.noop(), "noop");
        define("{{trim}}", 0, 0, L.DESC_MACRO_TRIM, (d, _) -> d.trim(), "trim");
        define("{{reverse::text}}", 1, 1, L.DESC_MACRO_REVERSE, (d, c) -> d.reverse(c.arg(0)), "reverse");

        // syntax (handled by the translator, listed only for the UI)
        MACROS.put(IF, new MacroDefinition(IF, "{{if cond}}…{{else}}…{{/if}}", 2, 2, L.DESC_MACRO_IF, (_, _) -> ""));
        HINTS.add(new MacroHint("{{if cond}}…{{else}}…{{/if}}", L.DESC_MACRO_IF));
        HINTS.add(new MacroHint("{{.name}} {{.name = value}} {{.n++}} {{$name}}", L.DESC_MACRO_VARIABLE_SHORTHAND));
        HINTS.add(new MacroHint("{{// comment}}", L.DESC_MACRO_COMMENT));

        // SillyTavern macros that have no meaning in Marginalia
        ignored("", -1, "persona", "personality", "charPrompt", "charInstruction", "charJailbreak", "charDepthPrompt",
                "charCreatorNotes", "creatorNotes", "charVersion", "version", "char_version", "mesExamples",
                "mesExamplesRaw", "exampleSeparator", "charFirstMessage", "original", "firstIncludedMessageId",
                "firstDisplayedMessageId", "lastSwipeId", "currentSwipeId", "allChatRange", "systemPrompt",
                "defaultSystemPrompt", "authorsNote", "charAuthorsNote", "defaultAuthorsNote", "chatSeparator",
                "chatStart", "reasoningPrefix", "reasoningSuffix", "reasoningSeparator", "charPrefix",
                "charNegativePrefix", "banned", "outlet", "instructStoryStringPrefix", "instructStoryStringSuffix",
                "instructUserPrefix", "instructInput", "instructUserSuffix", "instructAssistantPrefix",
                "instructOutput", "instructAssistantSuffix", "instructSeparator", "instructSystemPrefix",
                "instructSystemSuffix", "instructFirstAssistantPrefix", "instructFirstOutputPrefix",
                "instructLastAssistantPrefix", "instructLastOutputPrefix", "instructFirstUserPrefix",
                "instructFirstInput", "instructLastUserPrefix", "instructLastInput", "instructStop",
                "instructUserFiller", "instructSystemInstructionPrefix", "instructSystemPromptPrefix",
                "instructSystemPromptSuffix");
        ignored("false", -1, "isMobile", "hasExtension");
    }

    private Macros() {
    }

    /**
     * @param name macro name, case-insensitive
     * @return macro definition or {@code null}
     */
    public static MacroDefinition find(String name) {
        if (name == null) {
            return null;
        }
        return MACROS.get(name.toLowerCase(Locale.ROOT));
    }

    public static List<MacroHint> hints() {
        return Collections.unmodifiableList(HINTS);
    }

    /**
     * Name of Handlebars helper implementing given macro.
     */
    public static String helperName(String name) {
        return MacroTranslator.HELPER_PREFIX + name;
    }

    public static Collection<MacroDefinition> all() {
        return Collections.unmodifiableCollection(MACROS.values());
    }
}
