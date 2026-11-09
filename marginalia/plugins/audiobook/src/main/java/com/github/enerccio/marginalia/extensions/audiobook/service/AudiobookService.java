package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookExporter;
import com.github.enerccio.marginalia.extensions.audiobook.label.*;
import com.github.enerccio.marginalia.extensions.audiobook.model.*;
import com.github.enerccio.marginalia.extensions.audiobook.queue.AudioStore;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.extensions.audiobook.queue.Voices;
import com.github.enerccio.marginalia.extensions.audiobook.script.*;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioStore;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import com.github.enerccio.marginalia.extensions.httphooks.HookRunner;
import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;
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
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
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
    private com.github.enerccio.marginalia.domain.security.AdminGuard adminGuard;

    @Autowired
    private StoryGenerationService generationService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private com.github.enerccio.marginalia.domain.security.service.UserService userService;

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

    // --- the storage of the audio (an administrator decides it for the installation) ---

    /** The key of the plugin in the settings of the installation; the storage is under {@code storage}. */
    private static final String APP_KEY = "audiobook";
    /** Under {@link #APP_KEY}: the speech servers of the administrator ({@link SharedSettings}). */
    private static final String SHARED_KEY = "shared";

    /** Whether the current user is an administrator (the storage of the audio is theirs to change). */
    public boolean isAdmin() {
        try {
            return adminGuard.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    /** What the administrator chose, whether it can be used or not. */
    public AudioKind chosenStorage() {
        try {
            JsonObject attrs = settingService.findOrBlank(com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings.class, null).getAttributes();
            if (attrs != null && attrs.has(APP_KEY) && attrs.getAsJsonObject(APP_KEY).has("storage")) {
                return AudioKind.ofName(attrs.getAsJsonObject(APP_KEY).get("storage").getAsString());
            }
        } catch (Exception e) {
            AudiobookLog.get().warn("The storage of the audio could not be read: {}", e.getMessage());
        }
        return AudioKind.MP3;
    }

    /** What the audio is stored as now: mp3, or wav when the administrator chose it and ffmpeg is there (the wav can't be encoded for the export without it). */
    public AudioKind storage() {
        return chosenStorage() == AudioKind.WAV && Ffmpeg.available() ? AudioKind.WAV : AudioKind.MP3;
    }

    /** Stores the choice; only for an administrator. */
    public void saveStorage(AudioKind kind) {
        try {
            adminGuard.requireAdmin();
            var app = settingService.getOrCreateApp(com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings.class);
            JsonObject attrs = app.getAttributes();
            if (attrs == null) {
                attrs = new JsonObject();
                app.setAttributes(attrs);
            }
            JsonObject mine = attrs.has(APP_KEY) ? attrs.getAsJsonObject(APP_KEY) : new JsonObject();
            mine.addProperty("storage", kind.extension());
            attrs.add(APP_KEY, mine);
            settingService.save(app);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // --- the backends: the shared ones of the administrator, then the user's own ---

    /** The section of the plugin in the settings of the installation, null when there is none. */
    private JsonObject appSection() throws Exception {
        JsonObject attrs = settingService.findOrBlank(com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings.class, null).getAttributes();
        return attrs != null && attrs.has(APP_KEY) && attrs.get(APP_KEY).isJsonObject() ? attrs.getAsJsonObject(APP_KEY) : null;
    }

    /** What the administrator decided about the speech servers; readable by every user (it is what decides what they may use). */
    public SharedSettings sharedSettings() {
        try {
            JsonObject mine = appSection();
            if (mine != null && mine.has(SHARED_KEY)) {
                SharedSettings shared = gson.fromJson(mine.get(SHARED_KEY), SharedSettings.class);
                if (shared != null) {
                    return shared;
                }
            }
        } catch (Exception e) {
            AudiobookLog.get().warn("The shared speech servers could not be read: {}", e.getMessage());
        }
        return new SharedSettings();
    }

    /** Stores the shared speech servers and who may use them; only for an administrator. */
    public void saveSharedSettings(SharedSettings shared) {
        try {
            adminGuard.requireAdmin();
            var app = settingService.getOrCreateApp(com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings.class);
            JsonObject attrs = app.getAttributes();
            if (attrs == null) {
                attrs = new JsonObject();
                app.setAttributes(attrs);
            }
            JsonObject mine = attrs.has(APP_KEY) ? attrs.getAsJsonObject(APP_KEY) : new JsonObject();
            mine.add(SHARED_KEY, gson.toJsonTree(shared));
            attrs.add(APP_KEY, mine);
            settingService.save(app);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** The users an administrator can give a speech server to (the ones that are not deleted). */
    public List<com.github.enerccio.marginalia.domain.security.model.User> usersToShareWith() {
        try {
            adminGuard.requireAdmin();
            return userService.findAll();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** The uuid of the current user, null when there is none. */
    public String currentUserUuid() {
        try {
            var owner = settingService.getOrCreate(UserSetting.class).getOwner();
            return owner == null ? null : owner.getUuid();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Whether the user may use the speech servers they added themselves. */
    public boolean usersMayAddOwn() {
        return sharedSettings().isUsersMayAddOwn();
    }

    /**
     * The speech servers the current user may use now: the shared ones they are allowed, then their own while the administrator lets users have
     * their own. A server whose access was taken away is not here, and so can't be used by a job that names it.
     */
    public List<BackendConfig> backends() {
        SharedSettings shared = sharedSettings();
        List<BackendConfig> available = new ArrayList<>(shared.availableTo(currentUserUuid()));
        if (shared.isUsersMayAddOwn()) {
            available.addAll(getSettings().getBackends());
        }
        return available;
    }

    /** The backend of the id among {@link #backends()}, null when there is none or it is not for the user. */
    public BackendConfig findBackend(String id) {
        if (id == null) {
            return null;
        }
        return backends().stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }

    /** The backend the user picked, else the first they may use, null when they have none. */
    public BackendConfig selectedBackend() {
        List<BackendConfig> available = backends();
        BackendConfig picked = findIn(available, getSettings().getSelectedBackendId());
        return picked != null ? picked : available.stream().findFirst().orElse(null);
    }

    private static BackendConfig findIn(List<BackendConfig> backends, String id) {
        return id == null ? null : backends.stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }

    /** Whether the backend is one of the administrator's (its address, keys and requests are not the user's to see). */
    public boolean isShared(BackendConfig backend) {
        return backend != null && sharedSettings().find(backend.getId()) != null;
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
        return SpeechBatch.run(createBackend(config), GpuGate.forAddress(config.getBaseUrl()), before(config), after(config), cancelled, work);
    }

    /** The requests before a batch: in order, the first failure stops the batch. */
    private GpuGate.Step before(BackendConfig config) {
        return () -> {
            String error = runHooks("before a batch", withKey(config.getBeforeHooks(), config), config.isLocal(), true);
            if (error != null) {
                throw new BackendException(error);
            }
        };
    }

    /** The requests after a batch: every request is made, also when one failed (every container must be stopped); the first failure is reported. */
    private GpuGate.Step after(BackendConfig config) {
        return () -> {
            String error = runHooks("after a batch", withKey(config.getAfterHooks(), config), config.isLocal(), false);
            if (error != null) {
                throw new BackendException(error);
            }
        };
    }

    /**
     * Makes the requests of a list in order and writes each into the log of the extension (what was sent, when and how it was answered), so a
     * server that does something at the wrong time can be compared with it.
     *
     * @param stopAtFirstFailure true for the requests before a batch (the next one must not start a model on a full GPU)
     * @return the message of the first failure, null when all went well
     */
    private String runHooks(String phase, List<HttpHook> hooks, boolean local, boolean stopAtFirstFailure) {
        List<HttpHook> todo = hooks == null ? List.of() : hooks.stream().filter(h -> h != null && !h.isEmpty()).toList();
        String first = null;
        for (int i = 0; i < todo.size(); i++) {
            HttpHook hook = todo.get(i);
            long started = System.currentTimeMillis();
            AudiobookLog.get().info("Request {} of {} {}: {} {}", i + 1, todo.size(), phase, hook.getMethod(), hook.getUrl());
            try {
                HookRunner.run(hook, local, this::reveal);
                AudiobookLog.get().info("Request {} of {} {} answered in {} ms", i + 1, todo.size(), phase, System.currentTimeMillis() - started);
            } catch (BackendException e) {
                String message = todo.size() == 1 ? e.getMessage() : "Request " + (i + 1) + " of " + todo.size() + ": " + e.getMessage();
                AudiobookLog.get().warn("Request {} of {} {} failed after {} ms: {}", i + 1, todo.size(), phase, System.currentTimeMillis() - started, e.getMessage());
                if (stopAtFirstFailure) {
                    return message;
                }
                if (first == null) {
                    first = message;
                }
            }
        }
        return first;
    }

    /**
     * The requests with the key of the backend as {@code Authorization: Bearer} unless one has an {@code Authorization} header of its own: the key
     * of the requests, else the api key (one key for a proxy that serves everything). The copies live only for the batch.
     */
    private List<HttpHook> withKey(List<HttpHook> hooks, BackendConfig config) {
        String protectedKey = config.getProtectedHooksKey() != null && !config.getProtectedHooksKey().isBlank() ? config.getProtectedHooksKey() : config.getProtectedApiKey();
        String key = protectedKey == null ? null : reveal(protectedKey);
        if (hooks == null || key == null || key.isBlank()) {
            return hooks;
        }
        List<HttpHook> copies = new ArrayList<>();
        for (HttpHook hook : hooks) {
            if (hook == null || hook.isEmpty()) {
                continue;
            }
            String headers = hook.headersText(this::reveal);
            if (headers != null && headers.lines().anyMatch(line -> line.strip().toLowerCase(java.util.Locale.ROOT).startsWith("authorization:"))) {
                copies.add(hook);
                continue;
            }
            HttpHook copy = new HttpHook();
            copy.setUrl(hook.getUrl());
            copy.setMethod(hook.getMethod());
            copy.setBody(hook.getBody());
            copy.setTimeoutSeconds(hook.getTimeoutSeconds());
            copy.setWaitSeconds(hook.getWaitSeconds());
            copy.setHeaders((headers == null || headers.isBlank() ? "" : headers + "\n") + "Authorization: Bearer " + key.strip());
            copies.add(copy);
        }
        return copies;
    }

    /** Runs {@code work} on a thread of its own in the request context of the caller (the services work for the user who asked). */
    private void inBackground(String name, Runnable work) {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        Thread.ofVirtual().name(name).start(() -> {
            try (ThreadCopyRequestAttributes.InRequestScope ignored = new ThreadCopyRequestAttributes.InRequestScope(scope)) {
                work.run();
            }
        });
    }

    /**
     * Tries a backend as it is in the form: the requests before a batch (which start a server that is stopped), the question whether the server is
     * there and has a model, the requests after the batch. Nothing is spoken. The answer is given from a thread of its own.
     *
     * @param done called with the answer, or with the message of what failed
     */
    public void test(BackendConfig config, BiConsumer<ServerCheck.Result, String> done) {
        inBackground("audiobook-test", () -> {
            ServerCheck.Result result = null;
            String error = null;
            GpuGate gate = GpuGate.forAddress(config.getBaseUrl());
            try {
                gate.acquire(before(config));
                try {
                    result = gate.exclusive(null, () -> ServerCheck.check(config, this::reveal));
                } finally {
                    String afterError = gate.release(after(config));
                    if (afterError != null) {
                        AudiobookLog.get().warn("The request after the test failed: {}", afterError);
                        if (result != null) {
                            error = T.localized(THE_REQUEST_AFTER_THE_TEST_FAILED).formatted(afterError);
                        }
                    }
                }
            } catch (BackendException e) {
                result = null;
                error = e.getMessage();
            } catch (RuntimeException e) {
                AudiobookLog.get().warn("Testing the connection failed: {}", e.getMessage(), e);
                result = null;
                error = String.valueOf(e.getMessage());
            }
            done.accept(result, error);
        });
    }

    private static final java.util.Map<String, String> SAMPLES = java.util.Map.of(
            "ja", "こんにちは、この本の声です。こんなふうに話します。",
            "zh", "你好，我是这本书的声音。我就是这样说话的。",
            "ko", "안녕하세요, 이 책의 목소리입니다. 이렇게 말합니다.",
            "ru", "Здравствуйте, я голос этой книги. Вот так я говорю.",
            "el", "Γεια σας, είμαι η φωνή αυτού του βιβλίου. Έτσι μιλάω.",
            "he", "שלום, אני הקול של הספר הזה. ככה אני מדבר.",
            "ar", "مرحبًا، أنا صوت هذا الكتاب. هكذا أتحدث.",
            "hi", "नमस्ते, मैं इस किताब की आवाज़ हूँ। मैं ऐसे बोलता हूँ।");
    private static final String SAMPLE = "Hello, I am a voice of this book. This is how I sound when I read to you.";

    /**
     * Speaks a short sentence with a voice so the user can hear it before using it (the requests around a batch start and stop the server, so it
     * takes a while when the server is stopped). A voice for a language speaks a sentence in it, with the multilingual backend when there is one.
     *
     * @param voice    {@code clip:<uuid>} or {@code predefined:<file>}
     * @param language the code, null for the language of the book
     * @param done     called from a thread of its own with the mp3, or with the message of what failed
     */
    public void preview(String voice, String language, BiConsumer<byte[], String> done) {
        inBackground("audiobook-preview", () -> {
            try {
                BackendConfig multilingual = language == null ? null : backendForModel(BackendConfig.MODEL_MULTILINGUAL);
                BackendConfig backend = multilingual != null ? multilingual : selectedBackend();
                String code = multilingual != null ? language : null;
                if (backend == null) {
                    done.accept(null, T.localized(NO_BACKEND_YET));
                    return;
                }
                VoiceRef ref = VoiceRef.parse(voice);
                String sentence = code == null ? SAMPLE : SAMPLES.getOrDefault(code, SAMPLE);
                ClipVoices clips = new ClipVoices(resourceService);
                byte[] audio = runBatch(backend, null, speaker -> {
                    Voices.Resolved resolved = clips.resolve(speaker, ref);
                    SpeechRequest request = new SpeechRequest(sentence, resolved.mode(), resolved.name(), code, 0.5, backend.getCfgWeight(),
                            backend.getTemperature(), backend.getSpeed(), SEED, "mp3", backend.getChunkSize());
                    return speaker.synthesize(request, () -> false);
                }).value();
                done.accept(audio, null);
            } catch (IllegalArgumentException | BackendException e) {
                done.accept(null, e.getMessage());
            } catch (RuntimeException e) {
                AudiobookLog.get().warn("A voice could not be heard: {}", e.getMessage(), e);
                done.accept(null, String.valueOf(e.getMessage()));
            }
        });
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
        BackendConfig picked = selectedBackend();
        if (picked != null && picked.getModel().equals(model)) {
            return picked;
        }
        return backends().stream().filter(b -> b.getModel().equals(model)).findFirst().orElse(null);
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
        BackendConfig backend = selectedBackend();
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

    /** What the audio of a part is worth: there is none, it was made from an older text or script, or it is what the part says now. */
    public enum AudioState {
        MISSING, OLDER_THAN_THE_TEXT, OLDER_THAN_THE_SCRIPT, READY
    }

    /** Whether the part has audio and whether it is still the audio of the text and of the script of the studio. For the current user. */
    public AudioState audioState(String partUuid) throws Exception {
        ChatMessage stored = chatMessageService.findForUser(partUuid);
        AudioStore.PartAudio made = stored == null ? null : new ResourceAudioStore(resourceService, chatMessageService).partAudio(partUuid);
        if (made == null) {
            return AudioState.MISSING;
        }
        if (PartScript.stale(made.sourceHash(), stored.getResponse())) {
            return AudioState.OLDER_THAN_THE_TEXT;
        }
        StudioScript script = studioStore().find(partUuid);
        if (script != null && !script.toPartScript().joinKey().equals(made.joinKey())) {
            return AudioState.OLDER_THAN_THE_SCRIPT;
        }
        return AudioState.READY;
    }

    /**
     * The joined audio of a part for the export, or why there is none: not converted, made from an older text, or older than the script of the studio
     * (a segment was changed and the part not made again). For the current user.
     */
    public AudiobookExporter.PartAudio exportableAudio(String partUuid) {
        try {
            AudioState state = audioState(partUuid);
            if (state == AudioState.MISSING) {
                return AudiobookExporter.PartAudio.missing(T.localized(NO_AUDIO));
            }
            if (state == AudioState.OLDER_THAN_THE_TEXT) {
                return AudiobookExporter.PartAudio.missing(T.localized(AUDIO_OLDER_THAN_THE_TEXT));
            }
            if (state == AudioState.OLDER_THAN_THE_SCRIPT) {
                return AudiobookExporter.PartAudio.missing(T.localized(AUDIO_OLDER_THAN_THE_SCRIPT));
            }
            byte[] bytes = new ResourceAudioStore(resourceService, chatMessageService).partBytes(partUuid);
            return bytes == null ? AudiobookExporter.PartAudio.missing(T.localized(NO_AUDIO)) : AudiobookExporter.PartAudio.of(bytes);
        } catch (Exception e) {
            AudiobookLog.get().warn("The audio of a part could not be read for the export: {}", e.getMessage(), e);
            return AudiobookExporter.PartAudio.missing(String.valueOf(e.getMessage()));
        }
    }

    /**
     * The whole book as one mp3 with a chapter for each part (the ID3 chapter marks): the parts of the active branch that have audio made from their
     * text and script as they are now. Can take a while for a long book, call it from a thread of its own.
     */
    public AudiobookExporter.Whole wholeAudiobook(Manuscript manuscript) throws Exception {
        String author = "";
        try {
            var owner = settingService.getOrCreate(UserSetting.class).getOwner();
            author = StringUtils.defaultIfBlank(owner.getFullName(), owner.getLogin());
        } catch (Exception e) {
            AudiobookLog.get().debug("The author of the audiobook could not be read: {}", e.getMessage());
        }
        return AudiobookExporter.wholeBook(speakableParts(manuscript), part -> exportableAudio(part.getUuid()), manuscript.getName(), author);
    }

    /** {@link #wholeAudiobook} on a thread of its own; {@code done} is called from it with the result or the message of what failed. */
    public void wholeAudiobookInBackground(Manuscript manuscript, BiConsumer<AudiobookExporter.Whole, String> done) {
        inBackground("audiobook-whole", () -> {
            try {
                done.accept(wholeAudiobook(manuscript), null);
            } catch (Exception e) {
                AudiobookLog.get().warn("The whole audiobook could not be made: {}", e.getMessage(), e);
                done.accept(null, String.valueOf(e.getMessage()));
            }
        });
    }

    /** The audio of one piece of a part by its cache key, null when it is not made. */
    public byte[] segmentAudio(String partUuid, String key) {
        return new ResourceAudioStore(resourceService, chatMessageService).segment(partUuid, key);
    }

    /** The name a clip of the user has on the speech server (what a request carries), for showing; the name of the clip when it can't be read. */
    public String serverNameOfClip(String resourceUuid) {
        try {
            var clip = resourceService.findForUser(resourceUuid);
            byte[] bytes = clip == null ? null : resourceService.getResourceData(clip);
            if (bytes == null) {
                return resourceUuid;
            }
            String name = clip.getOriginalName() == null ? "" : clip.getOriginalName().toLowerCase(java.util.Locale.ROOT);
            return ChatterboxBackend.referenceName(bytes, name.endsWith(".mp3") ? "mp3" : "wav");
        } catch (Exception e) {
            return resourceUuid;
        }
    }

    /** The request the speech server gets for the entry, as readable JSON: a clip is shown by its name (the server's name for it is made when it is uploaded). */
    public String requestOf(ScriptEntry entry, java.util.function.UnaryOperator<String> clipName) {
        boolean clip = entry.voice().kind() == VoiceRef.Kind.CLIP;
        SpeechRequest request = new SpeechRequest(entry.text(), clip ? SpeechRequest.VoiceMode.CLONE : SpeechRequest.VoiceMode.PREDEFINED,
                clip ? clipName.apply(entry.voice().id()) : entry.voice().id(), entry.language(), entry.exaggeration(), entry.cfgWeight(),
                entry.temperature(), entry.speed(), entry.seed(), entry.format(), entry.chunkSize());
        return new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(com.google.gson.JsonParser.parseString(
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
        BackendConfig backend = findBackend(backendId);
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
        // the audio is made in the format the administrator chose for the installation
        backend = backend.withFormat(storage().extension());
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
            Set<String> models = backends().stream().map(BackendConfig::getModel).collect(Collectors.toSet());
            fresh = ScriptFromLabels.studio(text, pieces, labels.labels(), backend,
                    (speaker, delivery, language) -> cast.voice(speaker, delivery.name(), language, actors, narrator), models, SEED, form);
        }
        StudioScript merged = StudioScript.merge(stored, fresh);
        store.put(partUuid, merged);
        return merged.toPartScript();
    }

    /** The kind of audio a resource of the user is (by the type it was stored with), mp3 when it is not known. */
    public AudioKind audioKindOf(String resourceUuid) {
        try {
            com.github.enerccio.marginalia.domain.model.impl.Resource resource = resourceService.findForUser(resourceUuid);
            return resource != null && AudioKind.WAV.mime().equals(resource.getMimeType()) ? AudioKind.WAV : AudioKind.MP3;
        } catch (Exception e) {
            return AudioKind.MP3;
        }
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
