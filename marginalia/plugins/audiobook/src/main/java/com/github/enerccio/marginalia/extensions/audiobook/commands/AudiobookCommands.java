package com.github.enerccio.marginalia.extensions.audiobook.commands;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.audiobook.model.*;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService.AudioState;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * The audiobook as commands, for the command bar, the {@code command} macro of a template and the agents: the state of the book (which part has audio,
 * what waits), converting a range of parts, stopping, marking a part final, and the cast. They call {@link AudiobookService}, which the dialogs call.
 * <p>
 * The parts are numbered as the <i>Convert to audio</i> dialog numbers them: the parts with text on the active branch, the first is 1; a part can also
 * be named by its handle. The audio is made for the book of the user and kept with it, so what changes it or reads it is refused to an agent, whose
 * book is a working copy that is thrown away: only the cast, which is data of the book, is read for an agent.
 */
public class AudiobookCommands {

    public static final String STATUS = "audiobook-status";
    public static final String CONVERT = "audiobook-convert";
    public static final String STOP = "audiobook-stop";
    public static final String SET_PART_STATUS = "audiobook-set-part-status";
    public static final String CAST = "audiobook-cast";

    private static final Gson GSON = new Gson();
    private static final String PARAM_FROM = "The first part: its number among the parts with text of the active branch (the first is 1, as the Convert to audio dialog counts), or its handle; the first part when left out";
    private static final String PARAM_TO = "The last part, numbered or named like 'from'; the last part of the book when left out (audiobook-convert with only 'from' does that one part)";
    private static final String PARAM_LABELLING = "true: the language model of the book labels who speaks and how, the cast gives the voices; false: the narrator reads everything";
    private static final String PARAM_WHAT = "audio = make the audio, script = only the script for the studio, nothing is spoken";
    private static final String PARAM_WAIT = "true: the command ends when the jobs are over and tells how each ended";
    private static final String PARAM_PART = "A part: its number among the parts with text of the active branch (the first is 1, as the Convert to audio dialog counts), or its handle";
    private static final int POLL_MILLIS = 400;

    private final AudiobookService service;

    public AudiobookCommands(AudiobookService service) {
        this.service = service;
    }

    // ---- reading

    @Command(commandName = STATUS, humanNameText = "Audiobook status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Tells which parts of the book have audio and what is in the queue.",
            llmDescriptionText = "Returns JSON: the speech server in use, whether the cast gives a narrator, how the book converts by itself, the counts of the parts with text by the state of their audio "
                    + "(none, ready, older-than-text, older-than-script), the status of each part in the studio, and the conversion jobs of the user (waiting, running, done, failed). Parts are numbered among the "
                    + "parts with text of the active branch. With from..to only those parts are listed one by one.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the state of the audiobook"))
    public void status0(CommandContext context) throws Exception {
        status(context);
    }

    @Command(commandName = STATUS, humanNameText = "Audiobook status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Tells which parts of the book have audio and what is in the queue.",
            llmDescriptionText = "Returns JSON: the speech server in use, whether the cast gives a narrator, how the book converts by itself, the counts of the parts with text by the state of their audio "
                    + "(none, ready, older-than-text, older-than-script), the status of each part in the studio, and the conversion jobs of the user (waiting, running, done, failed). Parts are numbered among the "
                    + "parts with text of the active branch. With from..to only those parts are listed one by one.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the state of the audiobook"))
    public void status1(CommandContext context) throws Exception {
        status(context);
    }

    @Command(commandName = STATUS, humanNameText = "Audiobook status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Tells which parts of the book have audio and what is in the queue.",
            llmDescriptionText = "Returns JSON: the speech server in use, whether the cast gives a narrator, how the book converts by itself, the counts of the parts with text by the state of their audio "
                    + "(none, ready, older-than-text, older-than-script), the status of each part in the studio, and the conversion jobs of the user (waiting, running, done, failed). Parts are numbered among the "
                    + "parts with text of the active branch. With from..to only those parts are listed one by one.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = PARAM_TO)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the state of the audiobook"))
    public void status2(CommandContext context) throws Exception {
        status(context);
    }

    private void status(CommandContext context) throws Exception {
        Manuscript book = userBook(context);
        List<ChatMessage> parts = service.speakableParts(book);
        int from = number(context, "from", parts, 1);
        int to = number(context, "to", parts, parts.size());
        AudiobookSettings settings = service.getSettings();
        BackendConfig backend = settings.selected();
        Cast cast = service.castOf(book);

        JsonObject result = new JsonObject();
        result.addProperty("book", book.getName());
        result.addProperty("parts", parts.size());
        result.addProperty("backend", backend == null ? null : backend.getName() + " (" + backend.getModel() + ")");
        result.addProperty("narrator", backend != null && (hasNarrator(cast, settings) || hasText(backend.getNarratorVoice())));
        result.addProperty("auto", service.autoModeOf(book).id());

        Map<AudioState, Integer> counts = new EnumMap<>(AudioState.class);
        JsonArray list = new JsonArray();
        for (int i = 0; i < parts.size(); i++) {
            ChatMessage part = parts.get(i);
            AudioState state = service.audioState(part.getUuid());
            counts.merge(state, 1, Integer::sum);
            if (i + 1 >= from && i + 1 <= to) {
                JsonObject row = new JsonObject();
                row.addProperty("part", i + 1);
                row.addProperty("handle", part.getUuid());
                row.addProperty("audio", stateName(state));
                StudioScript script = service.studio(part.getUuid());
                row.addProperty("studio", script == null ? "no script" : script.status().name().toLowerCase(Locale.ROOT));
                list.add(row);
            }
        }
        JsonObject states = new JsonObject();
        for (AudioState state : AudioState.values()) {
            states.addProperty(stateName(state), counts.getOrDefault(state, 0));
        }
        result.add("audio", states);
        result.add("list", list);
        result.add("queue", jobs());
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = CAST, humanNameText = "Audiobook cast", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Lists who reads which character in the audiobook of the book.",
            llmDescriptionText = "Returns JSON: the actor of the narrator, the fallback actor (who reads a character the cast does not name) and the members of the cast of the book "
                    + "(character, aliases the text may use for it, actor). Read only; the cast is edited in the Convert to audio dialog.",
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the cast"))
    public void cast(CommandContext context) throws Exception {
        Manuscript book = context.getScope().getManuscript();
        if (book == null) {
            throw new IllegalStateException("No book is open");
        }
        Cast cast = service.castOf(book);
        AudiobookSettings settings = service.getSettings();
        JsonObject result = new JsonObject();
        result.addProperty("narrator", actorName(settings, cast.getNarratorActorId()));
        result.addProperty("fallback", actorName(settings, cast.getFallbackActorId()));
        JsonArray members = new JsonArray();
        for (CastMember member : cast.getMembers()) {
            JsonObject row = new JsonObject();
            row.addProperty("character", member.getName());
            row.addProperty("aliases", member.getAliases());
            row.addProperty("actor", actorName(settings, member.getActorId()));
            members.add(row);
        }
        result.add("members", members);
        context.setReturn(GSON.toJson(result));
    }

    // ---- doing

    @Command(commandName = CONVERT, humanNameText = "Convert to audio", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Puts parts of the book into the audiobook queue, and can wait until they are made.",
            llmDescriptionText = "Puts the parts from..to into the audiobook queue (the whole book without them, one part when only 'from' is given) with the speech server the user picked in the audiobook settings. "
                    + "'labelling' reads the parts with the voices of the cast, the language model of the book labels who speaks and how first; otherwise the narrator reads everything. 'what' script makes only the script "
                    + "for the studio and speaks nothing. Parts marked final in the studio are left alone, pieces already spoken are reused. With 'wait' the command ends when the jobs are over (a part is minutes of "
                    + "the speech server's time) and the result tells how each ended; otherwise it returns at once with the queue. Returns the summary.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void convert0(CommandContext context) throws Exception {
        convert(context);
    }

    @Command(commandName = CONVERT, humanNameText = "Convert to audio", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Puts parts of the book into the audiobook queue, and can wait until they are made.",
            llmDescriptionText = "Puts the parts from..to into the audiobook queue (the whole book without them, one part when only 'from' is given) with the speech server the user picked in the audiobook settings. "
                    + "'labelling' reads the parts with the voices of the cast, the language model of the book labels who speaks and how first; otherwise the narrator reads everything. 'what' script makes only the script "
                    + "for the studio and speaks nothing. Parts marked final in the studio are left alone, pieces already spoken are reused. With 'wait' the command ends when the jobs are over (a part is minutes of "
                    + "the speech server's time) and the result tells how each ended; otherwise it returns at once with the queue. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void convert1(CommandContext context) throws Exception {
        convert(context);
    }

    @Command(commandName = CONVERT, humanNameText = "Convert to audio", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Puts parts of the book into the audiobook queue, and can wait until they are made.",
            llmDescriptionText = "Puts the parts from..to into the audiobook queue (the whole book without them, one part when only 'from' is given) with the speech server the user picked in the audiobook settings. "
                    + "'labelling' reads the parts with the voices of the cast, the language model of the book labels who speaks and how first; otherwise the narrator reads everything. 'what' script makes only the script "
                    + "for the studio and speaks nothing. Parts marked final in the studio are left alone, pieces already spoken are reused. With 'wait' the command ends when the jobs are over (a part is minutes of "
                    + "the speech server's time) and the result tells how each ended; otherwise it returns at once with the queue. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = PARAM_TO)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void convert2(CommandContext context) throws Exception {
        convert(context);
    }

    @Command(commandName = CONVERT, humanNameText = "Convert to audio", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Puts parts of the book into the audiobook queue, and can wait until they are made.",
            llmDescriptionText = "Puts the parts from..to into the audiobook queue (the whole book without them, one part when only 'from' is given) with the speech server the user picked in the audiobook settings. "
                    + "'labelling' reads the parts with the voices of the cast, the language model of the book labels who speaks and how first; otherwise the narrator reads everything. 'what' script makes only the script "
                    + "for the studio and speaks nothing. Parts marked final in the studio are left alone, pieces already spoken are reused. With 'wait' the command ends when the jobs are over (a part is minutes of "
                    + "the speech server's time) and the result tells how each ended; otherwise it returns at once with the queue. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = PARAM_TO),
                    @CommandParameter(name = "labelling", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_LABELLING)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void convert3(CommandContext context) throws Exception {
        convert(context);
    }

    @Command(commandName = CONVERT, humanNameText = "Convert to audio", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Puts parts of the book into the audiobook queue, and can wait until they are made.",
            llmDescriptionText = "Puts the parts from..to into the audiobook queue (the whole book without them, one part when only 'from' is given) with the speech server the user picked in the audiobook settings. "
                    + "'labelling' reads the parts with the voices of the cast, the language model of the book labels who speaks and how first; otherwise the narrator reads everything. 'what' script makes only the script "
                    + "for the studio and speaks nothing. Parts marked final in the studio are left alone, pieces already spoken are reused. With 'wait' the command ends when the jobs are over (a part is minutes of "
                    + "the speech server's time) and the result tells how each ended; otherwise it returns at once with the queue. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = PARAM_TO),
                    @CommandParameter(name = "labelling", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_LABELLING),
                    @CommandParameter(name = "what", type = String.class, optional = true, allowed = {"audio", "script"}, defaultValue = "audio", descriptionText = PARAM_WHAT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void convert4(CommandContext context) throws Exception {
        convert(context);
    }

    @Command(commandName = CONVERT, humanNameText = "Convert to audio", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Puts parts of the book into the audiobook queue, and can wait until they are made.",
            llmDescriptionText = "Puts the parts from..to into the audiobook queue (the whole book without them, one part when only 'from' is given) with the speech server the user picked in the audiobook settings. "
                    + "'labelling' reads the parts with the voices of the cast, the language model of the book labels who speaks and how first; otherwise the narrator reads everything. 'what' script makes only the script "
                    + "for the studio and speaks nothing. Parts marked final in the studio are left alone, pieces already spoken are reused. With 'wait' the command ends when the jobs are over (a part is minutes of "
                    + "the speech server's time) and the result tells how each ended; otherwise it returns at once with the queue. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = PARAM_FROM),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = PARAM_TO),
                    @CommandParameter(name = "labelling", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_LABELLING),
                    @CommandParameter(name = "what", type = String.class, optional = true, allowed = {"audio", "script"}, defaultValue = "audio", descriptionText = PARAM_WHAT),
                    @CommandParameter(name = "wait", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_WAIT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void convert5(CommandContext context) throws Exception {
        convert(context);
    }

    private void convert(CommandContext context) throws Exception {
        Manuscript book = userBook(context);
        List<ChatMessage> parts = service.speakableParts(book);
        if (parts.isEmpty()) {
            context.setReturn("{\"queued\":0,\"hint\":\"The active branch of the book has no part with text\"}");
            return;
        }
        boolean singlePart = context.has("from") && context.get("from") != null && !context.has("to");
        int from = number(context, "from", parts, 1);
        int to = singlePart ? from : number(context, "to", parts, parts.size());
        BackendConfig backend = service.getSettings().selected();
        if (backend == null) {
            throw new IllegalStateException("No speech server is set up: add one in the audiobook settings first");
        }
        boolean labelling = Boolean.TRUE.equals(context.get("labelling", Boolean.class));
        AudiobookService.LabelOptions options = null;
        if (labelling) {
            options = new AudiobookService.LabelOptions(service.bookAi(book), service.bookProtocol(book));
            if (options.ai() == null) {
                throw new IllegalStateException("The book has no language model to label with: the narrator reads when 'labelling' is left out");
            }
        }
        String what = context.get("what", String.class);
        ConversionJob.Mode mode = "script".equalsIgnoreCase(what) ? ConversionJob.Mode.SCRIPT_ONLY : ConversionJob.Mode.PART;
        List<ConversionJob> queued = service.convert(book, backend, from, to, options, mode);

        JsonObject result = new JsonObject();
        result.addProperty("from", from);
        result.addProperty("to", to);
        result.addProperty("queued", queued.size());
        result.addProperty("leftAlone", to - from + 1 - queued.size());
        if (!Boolean.TRUE.equals(context.get("wait", Boolean.class)) || queued.isEmpty()) {
            result.add("queue", jobs());
            context.setReturn(GSON.toJson(result));
            return;
        }
        Set<String> ids = new HashSet<>();
        queued.forEach(job -> ids.add(job.id()));
        ConversionQueue queue = service.queue();
        while (true) {
            List<ConversionJob.Snapshot> mine = queue.jobs().stream().filter(job -> ids.contains(job.id())).toList();
            long over = mine.stream().filter(ConversionJob.Snapshot::finished).count();
            context.setStatus(over + " / " + mine.size());
            mine.stream().filter(job -> job.state() == ConversionJob.State.RUNNING).findFirst()
                    .ifPresent(job -> context.setSubStatus(job.title() + " (" + job.done() + " / " + job.total() + ")"));
            if (over == mine.size()) {
                result.add("jobs", describe(mine));
                context.setReturn(GSON.toJson(result));
                return;
            }
            if (context.isTerminated()) {
                ids.forEach(queue::cancel);
                result.addProperty("hint", "Stopped: the jobs that had not started were cancelled");
                result.add("jobs", describe(queue.jobs().stream().filter(job -> ids.contains(job.id())).toList()));
                context.setReturn(GSON.toJson(result));
                return;
            }
            Thread.sleep(POLL_MILLIS);
        }
    }

    @Command(commandName = STOP, humanNameText = "Stop the audiobook queue", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Cancels the waiting conversions; the one that is running ends after its piece.",
            llmDescriptionText = "Cancels the jobs of the audiobook queue that wait and stops the one that runs after the piece it is speaking; the pieces already made are kept. Returns the queue.",
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the queue"))
    public void stop(CommandContext context) throws Exception {
        userBook(context);
        service.queue().cancelAll();
        JsonObject result = new JsonObject();
        result.add("queue", jobs());
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = SET_PART_STATUS, humanNameText = "Mark an audiobook part", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Marks a part draft, reviewed or final in the audiobook studio.",
            llmDescriptionText = "Sets the status of the script of a part in the audiobook studio: draft, reviewed or final. A final part is not converted again and is the only kind the export can be limited to. "
                    + "The part needs a script (the studio makes it with annotation, or audiobook-convert with what=script). Returns the part.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "status", type = String.class, allowed = {"draft", "reviewed", "final"}, descriptionText = "The status")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the part and its status"))
    public void setPartStatus(CommandContext context) throws Exception {
        Manuscript book = userBook(context);
        List<ChatMessage> parts = service.speakableParts(book);
        int number = number(context, "part", parts, -1);
        if (number < 1) {
            throw new IllegalArgumentException("Give the part");
        }
        StudioScript.Status status;
        try {
            status = StudioScript.Status.valueOf(String.valueOf(context.get("status", String.class)).strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The status is draft, reviewed or final");
        }
        ChatMessage part = parts.get(number - 1);
        StudioScript script = service.studio(part.getUuid());
        if (script == null) {
            throw new IllegalStateException("Part " + number + " has no script yet: annotate it in the studio, or run audiobook-convert with what=script");
        }
        service.saveStudio(part.getUuid(), script.withStatus(status));
        JsonObject result = new JsonObject();
        result.addProperty("part", number);
        result.addProperty("status", status.name().toLowerCase(Locale.ROOT));
        context.setReturn(GSON.toJson(result));
    }

    // ---- helpers

    /** The book of the user. Refused to an agent: its book is a working copy, the audio is made for the real one. */
    private Manuscript userBook(CommandContext context) throws Exception {
        if (context.getScope().isAgent()) {
            throw new IllegalStateException("The audiobook is made for the book of the user, not for the working copy of an agent; ask the user to convert it. " + CAST + " can be read");
        }
        Manuscript book = context.getScope().getManuscript();
        if (book == null) {
            throw new IllegalStateException("No book is open");
        }
        return book;
    }

    /** The number of the part an argument names (a number or a handle); {@code none} when the argument is not given. */
    private int number(CommandContext context, String name, List<ChatMessage> parts, int none) throws Exception {
        String text = context.has(name) && context.get(name) != null ? String.valueOf(context.get(name)).strip() : "";
        if (text.isEmpty()) {
            return none;
        }
        if (text.matches("\\d+")) {
            int number = Integer.parseInt(text);
            if (number < 1 || number > parts.size()) {
                throw new IllegalArgumentException("The book has " + parts.size() + " parts with text, not " + number);
            }
            return number;
        }
        ChatMessage found = context.getScope().resolveMessage(text);
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getUuid().equals(found.getUuid())) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("That part has no text on the active branch");
    }

    private JsonArray jobs() {
        return describe(service.queue().jobs());
    }

    private static JsonArray describe(List<ConversionJob.Snapshot> jobs) {
        JsonArray array = new JsonArray();
        for (ConversionJob.Snapshot job : jobs) {
            JsonObject row = new JsonObject();
            row.addProperty("title", job.title());
            row.addProperty("state", job.state().name().toLowerCase(Locale.ROOT));
            row.addProperty("done", job.done());
            row.addProperty("total", job.total());
            if (job.message() != null && !job.message().isBlank()) {
                row.addProperty("message", job.message());
            }
            array.add(row);
        }
        return array;
    }

    private static String stateName(AudioState state) {
        return switch (state) {
            case MISSING -> "none";
            case READY -> "ready";
            case OLDER_THAN_THE_TEXT -> "older-than-text";
            case OLDER_THAN_THE_SCRIPT -> "older-than-script";
        };
    }

    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private static boolean hasNarrator(Cast cast, AudiobookSettings settings) {
        Actor actor = cast.getNarratorActorId() == null ? null : settings.findActor(cast.getNarratorActorId());
        return actor != null && actor.getVoice() != null;
    }

    private static String actorName(AudiobookSettings settings, String actorId) {
        Actor actor = actorId == null ? null : settings.findActor(actorId);
        return actor == null ? null : actor.getName();
    }
}
