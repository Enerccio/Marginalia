package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookExporter;
import com.github.enerccio.marginalia.extensions.audiobook.label.*;
import com.github.enerccio.marginalia.extensions.audiobook.model.*;
import com.github.enerccio.marginalia.extensions.audiobook.queue.AudioStore;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.extensions.audiobook.script.*;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioStore;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import com.github.enerccio.marginalia.extensions.httphooks.HookRunner;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/** What the audiobook plugin does with the data of the user: keeps the backends in the user settings, protects the secrets. */
@Configurable
public class AudiobookService {

    @Autowired
    private SettingService settingService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private StoryGenerationService generationService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    /** The inherited setting (book, folders, user) that says whether a new part is made into audio by itself. */
    public static final String AUTO_SETTING = "audiobook.auto";

    /** The key of the plugin in the attributes of a book; the cast is under {@code cast}. */
    static final String BOOK_KEY = "audiobook";

    /** The seed of every piece of the plain script: a fixed one makes the audio of a text the same every time, so it can be cached. */
    public static final int SEED = 42;

    private final Map<Long, ConversionQueue> queues = new ConcurrentHashMap<>();
    private GenerationTracker tracker;

    private final Gson gson = new Gson();

    // --- settings ---

    public AudiobookSettings getSettings() {
        try {
            JsonObject attrs = settingService.getOrCreate(UserSetting.class).getAttributes();
            if (attrs != null && attrs.has(AudiobookSettings.KEY)) {
                AudiobookSettings settings = gson.fromJson(attrs.get(AudiobookSettings.KEY), AudiobookSettings.class);
                if (settings != null) {
                    return settings;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new AudiobookSettings();
    }

    /** Puts the settings into the user settings that are being edited; the caller saves them. */
    public void saveSettings(AudiobookSettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(AudiobookSettings.KEY, gson.toJsonTree(settings));
    }

    public String protect(String secret) {
        return StringUtils.isBlank(secret) ? null : inferenceServices.protectSecret(secret);
    }

    /** Opens a text made by {@link #protect}; null when it was protected by another installation. */
    public String reveal(String protectedText) {
        return StringUtils.isBlank(protectedText) ? null : inferenceServices.revealSecret(protectedText);
    }

    // --- the actors and the cast ---

    /** Stores the actor (a new one is added, one with the same id replaced) in the settings of the user, at once. */
    public void saveActor(Actor actor) {
        try {
            UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
            AudiobookSettings settings = getSettings();
            settings.putActor(actor);
            saveSettings(settings, userSetting);
            settingService.save(userSetting);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Removes the actor from the settings of the user, at once; a cast that names it has no voice for it then (the fallback reads). */
    public void deleteActor(String actorId) {
        try {
            UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
            AudiobookSettings settings = getSettings();
            settings.getActors().removeIf(a -> a.getId().equals(actorId));
            saveSettings(settings, userSetting);
            settingService.save(userSetting);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** What the plugin keeps in the book under {@code audiobook}: the cast and the spoken form. */
    private <T> T readBook(Manuscript manuscript, String name, Class<T> type, java.util.function.Supplier<T> none) {
        JsonObject attrs = manuscript.getAttributes();
        if (attrs != null && attrs.has(BOOK_KEY) && attrs.get(BOOK_KEY).isJsonObject() && attrs.getAsJsonObject(BOOK_KEY).has(name)) {
            try {
                T value = gson.fromJson(attrs.getAsJsonObject(BOOK_KEY).get(name), type);
                if (value != null) {
                    return value;
                }
            } catch (RuntimeException e) {
                AudiobookLog.get().warn("The {} of the book could not be read, it is empty: {}", name, e.getMessage());
            }
        }
        return none.get();
    }

    private void writeBook(Manuscript manuscript, String name, Object value) {
        try {
            JsonObject attrs = manuscript.getAttributes();
            if (attrs == null) {
                attrs = new JsonObject();
                manuscript.setAttributes(attrs);
            }
            JsonObject book = attrs.has(BOOK_KEY) && attrs.get(BOOK_KEY).isJsonObject() ? attrs.getAsJsonObject(BOOK_KEY) : new JsonObject();
            book.add(name, gson.toJsonTree(value));
            attrs.add(BOOK_KEY, book);
            manuscriptService.save(manuscript);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** The cast of the book (it is in the book, so it travels with it), an empty one when it has none. */
    public Cast castOf(Manuscript manuscript) {
        return readBook(manuscript, "cast", Cast.class, Cast::new);
    }

    /** Stores the cast with the book. */
    public void saveCast(Manuscript manuscript, Cast cast) {
        writeBook(manuscript, "cast", cast);
    }

    /** How the text of the book is spelled for the ear (numbers, abbreviations, the dictionary), the defaults when the book has none. */
    public SpokenSettings spokenOf(Manuscript manuscript) {
        return readBook(manuscript, "spoken", SpokenSettings.class, SpokenSettings::new);
    }

    /** Stores the spoken form with the book; the parts are spelled that way the next time their script is made. */
    public void saveSpoken(Manuscript manuscript, SpokenSettings spoken) {
        writeBook(manuscript, "spoken", spoken);
    }

    /** A reference clip the user can give a character: an audio resource of theirs, not the audio this plugin made. */
    public record Clip(String uuid, String name) {
    }

    /** The audio resources of the user that are not the plugin's own pieces. */
    public List<Clip> clips() {
        try {
            List<Clip> clips = new ArrayList<>();
            for (com.github.enerccio.marginalia.domain.model.impl.Resource resource : resourceService.findAllForUser()) {
                String name = resource.getOriginalName() == null ? "" : resource.getOriginalName();
                String lower = name.toLowerCase(java.util.Locale.ROOT);
                boolean audio = (resource.getMimeType() != null && resource.getMimeType().startsWith("audio/")) || lower.endsWith(".wav")
                        || lower.endsWith(".mp3");
                if (audio && !name.startsWith(ResourceAudioStore.FILE_PREFIX)) {
                    clips.add(new Clip(resource.getUuid(), name));
                }
            }
            clips.sort(java.util.Comparator.comparing(Clip::name, String.CASE_INSENSITIVE_ORDER));
            return clips;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Puts a clip among the resources of the user; the result is what a voice names ({@code clip:<uuid>}). */
    public Clip uploadClip(String filename, byte[] content, String contentType) {
        try {
            com.github.enerccio.marginalia.domain.model.impl.Resource resource = resourceService.upload(filename, content, contentType);
            return new Clip(resource.getUuid(), filename);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // --- the speech ---

    /** The backend for the settings of a server; its key is opened here. */
    public SpeechBackend createBackend(BackendConfig config) {
        return new ChatterboxBackend(config, config.getProtectedApiKey() == null ? null : reveal(config.getProtectedApiKey()));
    }

    /**
     * Runs a batch of pieces on the server of the backend: the requests before it, the work, the requests after it. Batches on one server
     * address go through one {@link GpuGate}, whoever's they are.
     */
    public <T> SpeechBatch.Outcome<T> runBatch(BackendConfig config, BooleanSupplier cancelled, SpeechBatch.Work<T> work) throws BackendException {
        UnaryOperator<String> reveal = this::reveal;
        return SpeechBatch.run(createBackend(config), GpuGate.forAddress(config.getBaseUrl()),
                // before: in order, the first failure stops the batch
                () -> HookRunner.runAll(config.getBeforeHooks(), config.isLocal(), reveal),
                // after: every request is made, also when one failed (every container must be stopped); the first failure is reported
                () -> {
                    String error = HookRunner.runAllRegardless(config.getAfterHooks(), config.isLocal(), reveal);
                    if (error != null) {
                        throw new BackendException(error);
                    }
                },
                cancelled, work);
    }

    // --- the conversion ---

    /** Starts watching the generations (see {@link GenerationTracker}); called when the extension loads. */
    public synchronized void start() {
        if (tracker == null) {
            tracker = new GenerationTracker(generationService);
        }
    }

    /** Stops the queues (what is being made stops after its piece) and the watching; called when the extension unloads. */
    public synchronized void shutdown() {
        queues.values().forEach(ConversionQueue::close);
        queues.clear();
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
    }

    /**
     * The conversion queue of the current user. Made on the first call, with the request context of the page that asked (a copy the worker thread
     * runs in: the services work for the user it names, also after the page is gone).
     */
    public ConversionQueue queue() {
        Long userId = currentUserId();
        return queues.computeIfAbsent(userId, id -> {
            RequestAttributes scope = ThreadCopyRequestAttributes.create();
            ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(() -> {
                    try (ThreadCopyRequestAttributes.InRequestScope ignored = new ThreadCopyRequestAttributes.InRequestScope(scope)) {
                        r.run();
                    }
                }, "audiobook-queue");
                thread.setDaemon(true);
                return thread;
            });
            return new ConversionQueue(new ConversionQueue.Environment(new ResourceAudioStore(resourceService, chatMessageService),
                    new ClipVoices(resourceService), this::backendForModel, this::runBatch, () -> tracker != null && tracker.isRunning(id), 1000), worker);
        });
    }

    public Long currentUserId() {
        try {
            return settingService.getOrCreate(UserSetting.class).getOwner().getId();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** The backend that makes a model: the one the user picked when it has that model, else the first one that has. */
    private BackendConfig backendForModel(String model) {
        AudiobookSettings settings = getSettings();
        BackendConfig picked = settings.selected();
        if (picked != null && picked.getModel().equals(model)) {
            return picked;
        }
        return settings.getBackends().stream().filter(b -> b.getModel().equals(model)).findFirst().orElse(null);
    }

    /** The parts of the active branch of the book that have text, in order: what {@link #convert} counts. */
    public List<ChatMessage> speakableParts(Manuscript manuscript) throws Exception {
        List<ChatMessage> parts = new ArrayList<>();
        for (ChatMessage message : chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf())) {
            if (message.getResponse() != null && !message.getResponse().isBlank()) {
                parts.add(message);
            }
        }
        return parts;
    }

    /** The language model that labels the parts: the AI and the protocol (sampling, limits) it is asked with. */
    public record LabelOptions(AI ai, Protocol protocol) {
    }

    /** The AI the book is written with, what the labelling uses unless the user picks another. Null when it has none. */
    public AI bookAi(Manuscript manuscript) throws Exception {
        return manuscript.getAi() == null ? null : aiService.find(manuscript.getAi());
    }

    public Protocol bookProtocol(Manuscript manuscript) throws Exception {
        return manuscript.getProtocol() == null ? null : protocolService.find(manuscript.getProtocol());
    }

    /**
     * Puts the parts {@code from} to {@code to} (1 based, of the parts with text on the active branch of the book) into the queue of the user.
     * Without {@code labelling} the narrator reads every paragraph; with it the language model labels the pieces of the part (who speaks, how, sound
     * tags) first and the cast of the book gives each speaker its actor. The parts go in together: one run, one swap of the language model for the
     * speech. The cast is read again when a job runs, so a change made while the job waits is heard.
     *
     * @param labelling null for the plain script
     * @return the jobs, a part that already waits or runs has its job returned
     * @throws IllegalArgumentException when there is no narrator voice (in the cast or the backend) or the range is outside the book
     */
    public List<ConversionJob> convert(Manuscript manuscript, BackendConfig backend, int from, int to, LabelOptions labelling) throws Exception {
        return convert(manuscript, backend, from, to, labelling, ConversionJob.Mode.PART);
    }

    /**
     * Like {@link #convert(Manuscript, BackendConfig, int, int, LabelOptions)} with a mode: {@link ConversionJob.Mode#SCRIPT_ONLY} only makes (and
     * stores) the script of the parts, the annotation of the studio, nothing is spoken. A part whose status is <i>final</i> is left alone in both, so
     * the jobs returned are fewer than the parts asked for.
     */
    public List<ConversionJob> convert(Manuscript manuscript, BackendConfig backend, int from, int to, LabelOptions labelling, ConversionJob.Mode mode)
            throws Exception {
        Cast cast = castOf(manuscript);
        if (narrator(backend, cast, getSettings()::findActor) == null) {
            throw new IllegalArgumentException("There is no narrator voice, neither in the cast nor in the backend");
        }
        List<ChatMessage> parts = speakableParts(manuscript);
        if (from < 1 || to < from || to > parts.size()) {
            throw new IllegalArgumentException("The book has " + parts.size() + " parts with text");
        }
        if (labelling != null && labelling.ai() == null) {
            throw new IllegalArgumentException("No language model is chosen for the labelling");
        }
        String bookId = manuscript.getUuid();
        List<ConversionQueue.Task> tasks = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            ChatMessage part = parts.get(i - 1);
            String uuid = part.getUuid();
            StudioScript stored = studioStore().find(uuid);
            if (stored != null && stored.status() == StudioScript.Status.FINAL) {
                continue;
            }
            String id = backend.getId();
            String text = part.getResponse().strip().replaceAll("\\s+", " ");
            String title = "Part " + i + ": " + (text.length() > 50 ? text.substring(0, 50) + "..." : text);
            // the names the cast has and the book tells up to this part: the point of view and the present characters
            List<String> fields = new ArrayList<>(cast.names());
            for (ChatMessage earlier : parts.subList(0, i)) {
                fields.add(earlier.getPovCharacter());
                fields.add(earlier.getPresentCharacters());
            }
            List<String> characters = Characters.of(fields);
            tasks.add(new ConversionQueue.Task(uuid, title, () -> annotatedScript(uuid, id, bookId, labelling, characters), mode));
        }
        if (tasks.isEmpty()) {
            return List.of();
        }
        // all together: one run, one batch per model
        return queue().submitAll(tasks);
    }

    // --- the automatic conversion ---

    /** What the book says about making a new part into audio by itself: its own setting, else the nearest folder's, else the user's, else nothing. */
    public AutoMode autoModeOf(Manuscript manuscript) {
        try {
            EffectiveSetting<String> value = manuscriptService.resolveExtension(manuscript, AUTO_SETTING);
            return value == null ? AutoMode.NO : AutoMode.of(value.value());
        } catch (Exception e) {
            AudiobookLog.get().warn("The automatic conversion of the book could not be read: {}", e.getMessage(), e);
            return AutoMode.NO;
        }
    }

    /** The result of {@link #convertFinishedPart}. */
    public enum AutoResult {
        /** The book does not convert by itself, or the part is not on the active branch, or it is marked final in the studio. */
        NOTHING,
        /** The part is in the queue. */
        QUEUED,
        /** The part is in the queue as plain narration: the labelling was asked for and the book has no model. */
        QUEUED_PLAIN,
        NO_BACKEND
    }

    /**
     * A new part of the book is final: when the book converts by itself it goes into the queue of the user (the speech waits while a generation of
     * theirs runs). Plain, or labelled by the model and protocol of the book; a book without a model gets the plain narration. Uses the backend the
     * user has picked. For the current user.
     *
     * @param onFinished called from the worker thread with the job when it is over (done, failed or stopped); null for none
     * @throws IllegalArgumentException when there is no narrator voice, neither in the cast nor in the backend
     */
    public AutoResult convertFinishedPart(Manuscript manuscript, ChatMessage part, Consumer<ConversionJob.Snapshot> onFinished) throws Exception {
        AutoMode mode = autoModeOf(manuscript);
        if (mode == AutoMode.NO || part == null || part.getResponse() == null || part.getResponse().isBlank()) {
            return AutoResult.NOTHING;
        }
        BackendConfig backend = getSettings().selected();
        if (backend == null) {
            return AutoResult.NO_BACKEND;
        }
        List<ChatMessage> parts = speakableParts(manuscript);
        int number = -1;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getUuid().equals(part.getUuid())) {
                number = i + 1;
            }
        }
        if (number < 0) {
            return AutoResult.NOTHING;
        }
        AI ai = mode == AutoMode.LABELLED ? bookAi(manuscript) : null;
        LabelOptions options = ai == null ? null : new LabelOptions(ai, bookProtocol(manuscript));
        List<ConversionJob> jobs = convert(manuscript, backend, number, number, options, ConversionJob.Mode.PART);
        if (jobs.isEmpty()) {
            return AutoResult.NOTHING;
        }
        if (onFinished != null) {
            String jobId = jobs.getFirst().id();
            ConversionQueue queue = queue();
            queue.addListener(new Consumer<>() {
                @Override
                public void accept(ConversionJob.Snapshot snapshot) {
                    if (snapshot.id().equals(jobId) && snapshot.finished()) {
                        queue.removeListener(this);
                        onFinished.accept(snapshot);
                    }
                }
            });
        }
        return mode == AutoMode.LABELLED && ai == null ? AutoResult.QUEUED_PLAIN : AutoResult.QUEUED;
    }

    // --- the studio ---

    /** The voice the cast of the book gives a speaker who reads in this mood and language (the studio re-chooses it when the delivery of a segment changes). */
    public VoiceRef voiceFor(Manuscript manuscript, BackendConfig backend, String speaker, String delivery, String language) {
        AudiobookSettings settings = getSettings();
        VoiceRef own = backend == null || backend.getNarratorVoice().isBlank() ? null : VoiceRef.parse(backend.getNarratorVoice().strip());
        return castOf(manuscript).voice(speaker, delivery, language, settings::findActor, own);
    }

    private StudioStore studioStore() {
        return new PartStudioStore(chatMessageService);
    }

    /** The script of the part in the studio, null when there is none. */
    public StudioScript studio(String partUuid) {
        return studioStore().find(partUuid);
    }

    public void saveStudio(String partUuid, StudioScript script) {
        studioStore().put(partUuid, script);
    }

    /**
     * Has the part made from its stored script exactly as it is (what the studio shows is what is spoken, nothing is annotated again).
     *
     * @return the job; one that already waits or runs for the part is returned as it is
     * @throws IllegalArgumentException when the part has no script, or the text changed since it was made
     */
    public ConversionJob render(String partUuid, String title) throws Exception {
        ChatMessage part = chatMessageService.findForUser(partUuid);
        StudioScript stored = part == null ? null : studioStore().find(partUuid);
        if (stored == null) {
            throw new IllegalArgumentException("The part has no script yet: annotate it first");
        }
        if (PartScript.stale(stored.sourceHash(), part.getResponse())) {
            throw new IllegalArgumentException("The text of the part changed since its script was made: annotate it again");
        }
        return queue().submitAll(List.of(new ConversionQueue.Task(partUuid, title, () -> {
            StudioScript now = studioStore().find(partUuid);
            if (now == null) {
                throw new IllegalStateException("The script of the part is gone");
            }
            return now.toPartScript();
        }))).getFirst();
    }

    /**
     * Has some pieces of the part made (a hearing in the studio): their audio is kept with the part, the part itself is not joined. Nothing is made
     * for a piece whose audio is there.
     */
    public ConversionJob hear(String partUuid, String title, List<ScriptEntry> entries, String sourceHash) {
        return queue().submitAll(List.of(new ConversionQueue.Task(partUuid, title, () -> new PartScript(entries, sourceHash),
                ConversionJob.Mode.SEGMENTS_ONLY))).getFirst();
    }

    /** Whether the part is marked final in the studio. */
    public boolean isFinal(String partUuid) {
        StudioScript script = studioStore().find(partUuid);
        return script != null && script.status() == StudioScript.Status.FINAL;
    }

    /**
     * The joined audio of a part for the export, or why there is none: not converted, made from an older text, or older than the script of the studio
     * (a segment was changed and the part not made again). For the current user.
     */
    public AudiobookExporter.PartAudio exportableAudio(String partUuid) {
        try {
            ChatMessage stored = chatMessageService.findForUser(partUuid);
            ResourceAudioStore store = new ResourceAudioStore(resourceService, chatMessageService);
            AudioStore.PartAudio made = stored == null ? null : store.partAudio(partUuid);
            if (made == null) {
                return AudiobookExporter.PartAudio.missing(T.localized(NO_AUDIO));
            }
            if (PartScript.stale(made.sourceHash(), stored.getResponse())) {
                return AudiobookExporter.PartAudio.missing(T.localized(AUDIO_OLDER_THAN_THE_TEXT));
            }
            StudioScript script = studioStore().find(partUuid);
            if (script != null && !script.toPartScript().joinKey().equals(made.joinKey())) {
                return AudiobookExporter.PartAudio.missing(T.localized(AUDIO_OLDER_THAN_THE_SCRIPT));
            }
            byte[] bytes = store.partBytes(partUuid);
            return bytes == null ? AudiobookExporter.PartAudio.missing(T.localized(NO_AUDIO)) : AudiobookExporter.PartAudio.of(bytes);
        } catch (Exception e) {
            AudiobookLog.get().warn("The audio of a part could not be read for the export: {}", e.getMessage(), e);
            return AudiobookExporter.PartAudio.missing(String.valueOf(e.getMessage()));
        }
    }

    /** The audio of one piece of a part by its cache key, null when it is not made. */
    public byte[] segmentAudio(String partUuid, String key) {
        return new ResourceAudioStore(resourceService, chatMessageService).segment(partUuid, key);
    }

    /** The request the speech server gets for the entry, as readable JSON: a clip is shown by its name (the server's name for it is made when it is uploaded). */
    public String requestOf(ScriptEntry entry, java.util.function.UnaryOperator<String> clipName) {
        boolean clip = entry.voice().kind() == VoiceRef.Kind.CLIP;
        SpeechRequest request = new SpeechRequest(entry.text(), clip ? SpeechRequest.VoiceMode.CLONE : SpeechRequest.VoiceMode.PREDEFINED,
                clip ? "<" + clipName.apply(entry.voice().id()) + ">" : entry.voice().id(), entry.language(), entry.exaggeration(), entry.cfgWeight(),
                entry.temperature(), entry.speed(), entry.seed(), entry.format(), entry.chunkSize());
        return new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(com.google.gson.JsonParser.parseString(
                ChatterboxBackend.bodyOf(request, entry.model())));
    }

    /** The names of the characters the book tells: the point of view and the present characters of its parts. */
    public List<String> bookCharacters(Manuscript manuscript) {
        try {
            List<String> fields = new ArrayList<>();
            for (ChatMessage part : speakableParts(manuscript)) {
                fields.add(part.getPovCharacter());
                fields.add(part.getPresentCharacters());
            }
            return Characters.of(fields);
        } catch (Exception e) {
            AudiobookLog.get().warn("The characters of the book could not be read: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /** The voice of the narrator: the one of the actor of the cast, else the one of the backend; null when there is none. */
    private static VoiceRef narrator(BackendConfig backend, Cast cast, java.util.function.Function<String, Actor> actors) {
        VoiceRef own = backend.getNarratorVoice().isBlank() ? null : VoiceRef.parse(backend.getNarratorVoice().strip());
        return cast.voice(Cast.NARRATOR, Delivery.NEUTRAL.name(), null, actors, own);
    }

    private static VoiceRef requireNarrator(BackendConfig backend, Cast cast, java.util.function.Function<String, Actor> actors) {
        VoiceRef narrator = narrator(backend, cast, actors);
        if (narrator == null) {
            throw new IllegalStateException("There is no narrator voice any more, neither in the cast nor in the backend");
        }
        return narrator;
    }

    /** The spoken form of the book as it is when a job runs. */
    private SpokenForm spokenFormOfBook(String bookId) throws Exception {
        Manuscript book = manuscriptService.findForUser(bookId);
        return SpokenForm.of(book == null ? new SpokenSettings() : spokenOf(book));
    }

    /** The cast of the book as it is when a job runs. */
    private Cast castOfBook(String bookId) throws Exception {
        Manuscript book = manuscriptService.findForUser(bookId);
        return book == null ? new Cast() : castOf(book);
    }

    /**
     * The script of a part as it is when a job runs: made fresh (with the labels of the language model, or the plain narration without them) and
     * merged with the script of the studio: the segments a person changed (locked) stay as they are, the rest is the fresh one. The result is stored
     * with the part (a draft) and is what is spoken. A part whose status is final keeps its script untouched.
     */
    private PartScript annotatedScript(String partUuid, String backendId, String bookId, LabelOptions options, List<String> characters) throws Exception {
        ChatMessage part = chatMessageService.findForUser(partUuid);
        AudiobookSettings settings = getSettings();
        BackendConfig backend = settings.find(backendId);
        if (part == null) {
            throw new IllegalStateException("The part is gone");
        }
        if (backend == null) {
            throw new IllegalStateException("The backend is gone");
        }
        StudioStore store = studioStore();
        StudioScript stored = store.find(partUuid);
        if (stored != null && stored.status() == StudioScript.Status.FINAL) {
            return stored.toPartScript();
        }
        Cast cast = castOfBook(bookId);
        java.util.function.Function<String, Actor> actors = settings::findActor;
        VoiceRef narrator = requireNarrator(backend, cast, actors);
        String text = part.getResponse();
        List<Piece> pieces = PieceSplitter.split(text);
        SpokenForm form = spokenFormOfBook(bookId);
        StudioScript fresh;
        if (options == null || pieces.isEmpty()) {
            fresh = ScriptBuilder.studio(text, narrator, backend, SEED, form);
        } else {
            LabelService.Result labels = LabelService.labels(partUuid, text, pieces, characters,
                    new LlmLabelModel(inferenceServices.forAIOrThrow(options.ai()), options.protocol()), new PartLabelStore(chatMessageService));
            if (labels.missing() > 0) {
                AudiobookLog.get().warn("The model left {} of {} pieces of a part without a label: the narrator reads them", labels.missing(), pieces.size());
            }
            Set<String> models = settings.getBackends().stream().map(BackendConfig::getModel).collect(Collectors.toSet());
            fresh = ScriptFromLabels.studio(text, pieces, labels.labels(), backend,
                    (speaker, delivery, language) -> cast.voice(speaker, delivery.name(), language, actors, narrator), models, SEED, form);
        }
        StudioScript merged = StudioScript.merge(stored, fresh);
        store.put(partUuid, merged);
        return merged.toPartScript();
    }

    /** The bytes of a resource of the user (a joined audio found by {@link PlayableParts}), null when it is not theirs or gone. */
    public byte[] audioBytes(String resourceUuid) {
        try {
            com.github.enerccio.marginalia.domain.model.impl.Resource resource = resourceService.findForUser(resourceUuid);
            return resource == null ? null : resourceService.getResourceData(resource);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** The joined audio of a part, null when there is none. For the page of the user (the request context is theirs). */
    public byte[] partAudio(String partUuid) {
        return new ResourceAudioStore(resourceService, chatMessageService).partBytes(partUuid);
    }
}
