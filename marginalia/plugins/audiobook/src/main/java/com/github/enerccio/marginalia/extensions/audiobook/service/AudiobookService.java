package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.run.Runs;
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
import com.github.enerccio.marginalia.extensions.httphooks.HookKeys;
import com.github.enerccio.marginalia.extensions.httphooks.HookRunner;
import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.*;
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
    private GpuService gpuService;

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
    private TemplateService templateService;

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
            List<com.github.enerccio.marginalia.domain.security.model.User> users = new ArrayList<>(userService.findAll());
            users.sort(Comparator.comparing(u -> u.getLogin() == null ? "" : u.getLogin(), String.CASE_INSENSITIVE_ORDER));
            return users;
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
        return BackendConfig.byName(available);
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

    /**
     * Stores the entry in the book as it is stored now (the rest of the book is not written back from the copy the caller holds) and in
     * that copy too, so the caller reads it from there.
     */
    private void writeBook(Manuscript manuscript, String name, Object value) {
        try {
            writeBookEntry(manuscript, name, value);
            manuscriptService.update(manuscript, current -> writeBookEntry(current, name, value));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeBookEntry(Manuscript manuscript, String name, Object value) {
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        JsonObject book = attrs.has(BOOK_KEY) && attrs.get(BOOK_KEY).isJsonObject() ? attrs.getAsJsonObject(BOOK_KEY) : new JsonObject();
        book.add(name, gson.toJsonTree(value));
        attrs.add(BOOK_KEY, book);
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

    /**
     * A reference clip the user can give a character: an audio resource of theirs, not the audio this plugin made, with what the plugin keeps of it.
     *
     * @param name     the name the user gave it, else the file name
     * @param fileName the file name of the resource (the kind of audio)
     * @param folder   the folder of its original, null for none
     * @param parent   the clip it was made from (a version), null for an original
     * @param madeWith how it was made from the parent, null when uploaded
     * @param label    what a picker shows: {@code folder / original › name}
     */
    public record Clip(String uuid, String name, String fileName, String folder, String parent, String madeWith, String label) {

        public Clip(String uuid, String name) {
            this(uuid, name, name, null, null, null, name);
        }

        /** The order of the pickers: by folder (the clips without one last), then by label. */
        public static java.util.Comparator<Clip> order() {
            java.text.Collator collator = AudiobookSettings.collator();
            return java.util.Comparator.comparing((Clip c) -> c.folder() == null)
                    .thenComparing(c -> c.folder() == null ? "" : c.folder(), collator)
                    .thenComparing(Clip::label, collator);
        }
    }

    /** The audio resources of the user that are not the plugin's own pieces, by folder and name. */
    public List<Clip> clips() {
        try {
            AudiobookSettings settings = getSettings();
            Map<String, String> files = new LinkedHashMap<>();
            for (com.github.enerccio.marginalia.domain.model.impl.Resource resource : resourceService.findAllForUser()) {
                String name = resource.getOriginalName() == null ? "" : resource.getOriginalName();
                String lower = name.toLowerCase(java.util.Locale.ROOT);
                boolean audio = (resource.getMimeType() != null && resource.getMimeType().startsWith("audio/")) || lower.endsWith(".wav")
                        || lower.endsWith(".mp3");
                // the shifted copies made for unnamed speakers are not clips to pick
                if (audio && !name.startsWith(ResourceAudioStore.FILE_PREFIX) && !settings.isShifted(resource.getUuid())) {
                    files.put(resource.getUuid(), name);
                }
            }
            List<Clip> clips = new ArrayList<>();
            for (var file : files.entrySet()) {
                String uuid = file.getKey();
                ClipInfo info = settings.clipInfo(uuid);
                String name = info == null || info.getName() == null ? file.getValue() : info.getName();
                // a version of a clip that is gone is shown as an original
                String parent = info == null || info.getParent() == null || !files.containsKey(info.getParent()) ? null : info.getParent();
                String folder = settings.folderOf(uuid);
                String label = name;
                if (parent != null) {
                    String root = settings.rootOf(uuid);
                    ClipInfo rootInfo = settings.clipInfo(root);
                    String rootName = rootInfo == null || rootInfo.getName() == null ? files.getOrDefault(root, "?") : rootInfo.getName();
                    label = rootName + " › " + name;
                }
                if (folder != null) {
                    label = folder + " / " + label;
                }
                clips.add(new Clip(uuid, name, file.getValue(), folder, parent, info == null ? null : info.getMadeWith(), label));
            }
            clips.sort(Clip.order());
            return clips;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Changes the settings of the user at once (the clips, the layout of a window: what a window keeps apart from the settings form). */
    private void changeSettings(java.util.function.Consumer<AudiobookSettings> change) {
        try {
            UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
            AudiobookSettings settings = getSettings();
            change.accept(settings);
            saveSettings(settings, userSetting);
            settingService.save(userSetting);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Gives the clip a name (blank: the file name again). */
    public void renameClip(String uuid, String name) {
        changeSettings(settings -> settings.renameClip(uuid, name));
    }

    /** Puts the clip, its original and every version into the folder (blank: none). */
    public void moveClip(String uuid, String folder) {
        changeSettings(settings -> settings.moveClip(uuid, folder));
    }

    /** The folders of the user's clips, by name. */
    public List<String> clipFolders() {
        return getSettings().folders();
    }

    /** Keeps how the tabs of Voices and clips are arranged. */
    public void saveVoicesLayout(com.google.gson.JsonObject layout) {
        changeSettings(settings -> settings.setVoicesLayout(layout));
    }

    // --- the speech recognition ---

    /**
     * Runs the work with the speech recognition of the user while its GPU lock is held (the lock's steps make room for it, for example by restarting a
     * ComfyUI that still holds the speech model).
     *
     * @return false when the user has no speech recognition
     */
    public boolean withRecognizer(java.util.function.BooleanSupplier cancelled, ConversionQueue.WordChecker.Work work) throws BackendException {
        com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig config = getSettings().recognizer();
        if (config == null) {
            return false;
        }
        com.github.enerccio.marginalia.extensions.audiobook.recognize.SpeechRecognizer recognizer =
                com.github.enerccio.marginalia.extensions.audiobook.recognize.SpeechRecognizer.of(config,
                        config.getProtectedApiKey() == null ? null : reveal(config.getProtectedApiKey()));
        try (SpeechBatch.Lease _ = takeGpu(config.getGpuLock(), config.getModel(), cancelled)) {
            work.run(recognizer);
        }
        return true;
    }

    /**
     * Recognises the words spoken in a clip with the speech recognition of the user, from a thread of its own.
     *
     * @param done called with the text, or with the message of what failed
     */
    public void recognizeClip(String clipUuid, java.util.function.BiConsumer<String, String> done) {
        inBackground("audiobook-recognize", () -> {
            // one job of the live list (/jobs) and the log, the wait for the GPU included
            try (com.github.enerccio.marginalia.domain.service.job.Jobs.Handle logged = com.github.enerccio.marginalia.domain.service.job.Jobs.open(
                    "audiobook", "Recognition of a clip", com.github.enerccio.marginalia.domain.service.job.JobCallType.AUDIO)) {
                recognizeClipJob(clipUuid, done, logged);
            }
        });
    }

    private void recognizeClipJob(String clipUuid, java.util.function.BiConsumer<String, String> done,
                                  com.github.enerccio.marginalia.domain.service.job.Jobs.Handle logged) {
        try {
            byte[] bytes = clipBytes(clipUuid);
            if (bytes == null) {
                logged.fail();
                done.accept(null, "The clip is gone");
                return;
            }
            String ext = AudioKind.of(bytes).extension();
            String[] text = new String[1];
            boolean ran = withRecognizer(() -> false, recognizer -> text[0] = recognizer.transcribe(bytes, ext, null, () -> false));
            if (!ran) {
                logged.fail();
                done.accept(null, T.localized(NO_SPEECH_RECOGNITION));
                return;
            }
            done.accept(text[0], null);
        } catch (BackendException | RuntimeException e) {
            logged.fail();
            AudiobookLog.get().warn("Recognising a clip failed: {}", e.getMessage(), e);
            done.accept(null, com.github.enerccio.marginalia.extensions.httphooks.Http.describe(e));
        }
    }

    /**
     * Tries the speech recognition as it is in the form (under its GPU lock), from a thread of its own.
     *
     * @param done called with null when it works, else with the message of what failed
     */
    public void testRecognizer(com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig config, java.util.function.Consumer<String> done) {
        inBackground("audiobook-recognizer-test", () -> {
            try (SpeechBatch.Lease _ = takeGpu(config.getGpuLock(), config.getModel(), null)) {
                com.github.enerccio.marginalia.extensions.audiobook.recognize.SpeechRecognizer.of(config,
                        config.getProtectedApiKey() == null ? null : reveal(config.getProtectedApiKey())).test();
                done.accept(null);
            } catch (BackendException | RuntimeException e) {
                AudiobookLog.get().warn("Testing the speech recognition failed: {}", e.getMessage(), e);
                done.accept(com.github.enerccio.marginalia.extensions.httphooks.Http.describe(e));
            }
        });
    }

    /** The words spoken in the clip, null when they are not known. */
    public String clipText(String clipUuid) {
        return getSettings().clipText(clipUuid);
    }

    /** Stores (or with a blank text removes) the words spoken in the clip, at once. */
    public void saveClipText(String clipUuid, String text) {
        changeSettings(settings -> settings.setClipText(clipUuid, text));
    }

    /** The bytes of a clip of the user, null when it is gone. */
    public byte[] clipBytes(String uuid) {
        try {
            var resource = resourceService.findForUser(uuid);
            return resource == null ? null : resourceService.getResourceData(resource);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** The samples of a clip to draw; null when it is gone or can't be read here (an mp3 without ffmpeg). */
    public com.github.enerccio.marginalia.extensions.audiobook.audio.Waveform clipWaveform(String uuid) {
        byte[] bytes = clipBytes(uuid);
        try {
            return bytes == null ? null : com.github.enerccio.marginalia.extensions.audiobook.audio.Waveform.of(bytes);
        } catch (BackendException e) {
            AudiobookLog.get().warn("The waveform of the clip {} could not be read: {}", uuid, e.getMessage());
            return null;
        }
    }

    /** How fit the clip is to be a reference voice; null when it can't be measured here (an mp3 without ffmpeg) or is gone. */
    public com.github.enerccio.marginalia.extensions.audiobook.audio.ClipQuality.Result measureClip(String uuid) {
        byte[] bytes = clipBytes(uuid);
        if (bytes == null) {
            return null;
        }
        try {
            return com.github.enerccio.marginalia.extensions.audiobook.audio.ClipQuality.measure(bytes);
        } catch (BackendException e) {
            AudiobookLog.get().warn("The clip {} could not be measured: {}", uuid, e.getMessage());
            return null;
        }
    }

    /**
     * A cleaned copy of the clip as a new version of it (named "ffmpeg"), made with ffmpeg ({@code Ffmpeg.cleanVoice}); the clip itself is left as it is,
     * so the version can be heard before it replaces it.
     */
    public Clip cleanClip(String uuid) throws BackendException {
        byte[] bytes = clipBytes(uuid);
        if (bytes == null) {
            throw new BackendException("The clip is gone");
        }
        var quality = com.github.enerccio.marginalia.extensions.audiobook.audio.ClipQuality.measure(bytes);
        if (quality == null) {
            throw new BackendException("The clip can't be read without ffmpeg");
        }
        byte[] cleaned = com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg.cleanVoice(bytes, quality.floorDb());
        return storeVersion(uuid, cleaned, "wav", "ffmpeg", "ffmpeg");
    }

    /** Puts the audio among the resources as a version of the clip and returns it as the pickers show it. */
    private Clip storeVersion(String parentUuid, byte[] audio, String extension, String name, String madeWith) {
        String file = clips().stream().filter(c -> c.uuid().equals(parentUuid)).map(Clip::fileName).findFirst().orElse("clip");
        String base = file.replaceFirst("(?i)\\.(wav|mp3)$", "");
        Clip copy = uploadClip(base + " (" + name.replaceAll("[^\\p{L}\\p{N} _-]", "") + ")." + extension, audio,
                "mp3".equals(extension) ? "audio/mpeg" : "audio/wav");
        changeSettings(settings -> settings.addVersion(parentUuid, copy.uuid(), name, madeWith));
        return clips().stream().filter(c -> c.uuid().equals(copy.uuid())).findFirst().orElse(copy);
    }

    /**
     * Cleans the clip with a preset of the cleanup of the user under its GPU lock, from a thread of its own; the result is a new version of the clip.
     *
     * @param done called with the version, or with the message of what failed
     */
    public void cleanClipWith(String uuid, String presetName, java.util.function.BiConsumer<Clip, String> done) {
        inBackground("audiobook-clean", () -> {
            // one job of the live list (/jobs) and the log, for the user of the page, the wait for the GPU included; the cleanup on ComfyUI is its call
            try (com.github.enerccio.marginalia.domain.service.job.Jobs.Handle logged = com.github.enerccio.marginalia.domain.service.job.Jobs.open(
                    "audiobook", "Cleanup of a clip (" + presetName + ")", com.github.enerccio.marginalia.domain.service.job.JobCallType.AUDIO)) {
                try {
                    com.github.enerccio.marginalia.extensions.audiobook.model.CleanupConfig config = getSettings().cleanup();
                    if (config == null) {
                        logged.fail();
                        done.accept(null, T.localized(NO_CLEANUP));
                        return;
                    }
                    com.github.enerccio.marginalia.extensions.audiobook.model.CleanupPreset preset = config.preset(presetName);
                    byte[] bytes = clipBytes(uuid);
                    if (preset == null || bytes == null) {
                        logged.fail();
                        done.accept(null, preset == null ? "No cleanup " + presetName : "The clip is gone");
                        return;
                    }
                    com.github.enerccio.marginalia.extensions.audiobook.clean.ComfyCleaner.Result result;
                    // its own lock: the cleanup models must not be loaded next to the speech or the recognition models
                    // a preset of ffmpeg steps only needs no GPU (taking the lock could restart ComfyUI for nothing)
                    try (SpeechBatch.Lease _ = takeGpu(preset.usesComfy() ? config.getGpuLock() : null, null, null)) {
                        result = new com.github.enerccio.marginalia.extensions.audiobook.clean.ComfyCleaner(config,
                                config.getProtectedApiKey() == null ? null : reveal(config.getProtectedApiKey()))
                                .clean(bytes, AudioKind.of(bytes).extension(), preset, null);
                    }
                    done.accept(storeVersion(uuid, result.audio(), result.extension(), preset.getName(), preset.describe()), null);
                } catch (BackendException | RuntimeException e) {
                    logged.fail();
                    AudiobookLog.get().warn("Cleaning a clip failed: {}", e.getMessage(), e);
                    done.accept(null, com.github.enerccio.marginalia.extensions.httphooks.Http.describe(e));
                }
            }
        });
    }

    /**
     * Tries the cleanup as it is in the form (under its GPU lock: reading the nodes may start the server), from a thread of its own.
     *
     * @param done called with the separation models ComfyUI offers when it works, else with the message of what failed
     */
    public void testCleanup(com.github.enerccio.marginalia.extensions.audiobook.model.CleanupConfig config,
                            java.util.function.BiConsumer<List<String>, String> done) {
        inBackground("audiobook-cleanup-test", () -> {
            try (SpeechBatch.Lease _ = takeGpu(config.getGpuLock(), null, null)) {
                var cleaner = new com.github.enerccio.marginalia.extensions.audiobook.clean.ComfyCleaner(config,
                        config.getProtectedApiKey() == null ? null : reveal(config.getProtectedApiKey()));
                cleaner.test();
                done.accept(cleaner.separationModels(), null);
            } catch (BackendException | RuntimeException e) {
                AudiobookLog.get().warn("Testing the cleanup failed: {}", e.getMessage(), e);
                done.accept(null, com.github.enerccio.marginalia.extensions.httphooks.Http.describe(e));
            }
        });
    }

    /**
     * A version of the clip with only the range kept, or with the range cut out ({@code ClipTrim}).
     *
     * @param from the start of the range in seconds
     * @param to   the end of the range in seconds
     */
    public Clip trimClip(String uuid, double from, double to, boolean keep) throws BackendException {
        byte[] bytes = clipBytes(uuid);
        if (bytes == null) {
            throw new BackendException("The clip is gone");
        }
        byte[] trimmed = com.github.enerccio.marginalia.extensions.audiobook.audio.ClipTrim.trim(bytes, from, to, keep);
        String range = String.format(java.util.Locale.ROOT, "%.1f-%.1f s", from, to);
        String name = (keep ? T.localized(VERSION_PART) : T.localized(VERSION_WITHOUT)).formatted(range);
        return storeVersion(uuid, trimmed, "wav", name, (keep ? "trim " : "cut ") + range);
    }

    /** Puts the version wherever its parent is used (see {@link #replaceClip}); 0 for an original. */
    public int useVersion(String versionUuid) {
        String parent = clips().stream().filter(c -> c.uuid().equals(versionUuid)).map(Clip::parent).findFirst().orElse(null);
        return parent == null ? 0 : replaceClip(parent, versionUuid);
    }

    /** The places of the actors and own backends that use the clip. */
    public List<AudiobookSettings.VoiceUse> clipUses(String uuid) {
        return getSettings().usesOf("clip:" + uuid);
    }

    /**
     * Puts the clip {@code to} wherever the clip {@code from} is used (actors with their mood and language voices, own backends), at once. The old clip
     * stays among the resources.
     *
     * @return how many places changed
     */
    public int replaceClip(String from, String to) {
        try {
            UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
            AudiobookSettings settings = getSettings();
            int changed = settings.replaceVoice("clip:" + from, "clip:" + to);
            boolean hadText = settings.clipText(to) == null && settings.clipText(from) != null;
            // the replacement says the same words (a cleaned copy) unless it has a text of its own
            settings.moveClipText(from, to);
            if (changed > 0 || hadText) {
                saveSettings(settings, userSetting);
                settingService.save(userSetting);
            }
            return changed;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Deletes a clip nobody uses (soft: the cleanup of the resources removes the file later).
     *
     * @return false when an actor or a backend still uses it (nothing is deleted)
     */
    public boolean deleteClip(String uuid) {
        if (!clipUses(uuid).isEmpty()) {
            return false;
        }
        try {
            List<String> doomed = new ArrayList<>(List.of(uuid));
            // its shifted copies (unnamed speakers) go with it; they are made again from another clip when needed
            doomed.addAll(getSettings().shiftedOf(uuid));
            boolean deleted = resourceService.softDelete(doomed) > 0;
            if (deleted) {
                // its versions move up to its parent, its name and words go
                changeSettings(settings -> settings.forgetClip(uuid));
            }
            return deleted;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A new clip (an original, not a version) of the clips one after the other ({@code ClipMerge}). It is named after them and goes into their folder
     * when they share one; its words are theirs when every clip has them. The clips themselves stay.
     *
     * @param uuids the clips in the order they are joined
     */
    public Clip mergeClips(List<String> uuids) throws BackendException {
        List<Clip> known = clips();
        List<Clip> parts = new ArrayList<>();
        List<byte[]> audio = new ArrayList<>();
        for (String uuid : uuids) {
            Clip clip = known.stream().filter(c -> c.uuid().equals(uuid)).findFirst().orElse(null);
            byte[] bytes = clip == null ? null : clipBytes(uuid);
            if (bytes == null) {
                throw new BackendException("A clip is gone");
            }
            parts.add(clip);
            audio.add(bytes);
        }
        byte[] merged = com.github.enerccio.marginalia.extensions.audiobook.audio.ClipMerge.merge(audio);
        String name = parts.stream().map(Clip::name).collect(java.util.stream.Collectors.joining(" + "));
        if (name.length() > 120) {
            name = name.substring(0, 119) + "…";
        }
        String file = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").replaceAll("(?i)\\.(wav|mp3)", "");
        Clip copy = uploadClip(file + ".wav", merged, "audio/wav");
        String folder = parts.stream().map(Clip::folder).distinct().count() == 1 ? parts.getFirst().folder() : null;
        List<String> texts = parts.stream().map(c -> clipText(c.uuid())).toList();
        String text = texts.stream().anyMatch(Objects::isNull) ? null : String.join(" ", texts);
        String clipName = name;
        changeSettings(settings -> {
            settings.renameClip(copy.uuid(), clipName);
            if (folder != null) {
                settings.moveClip(copy.uuid(), folder);
            }
            if (text != null) {
                settings.setClipText(copy.uuid(), text);
            }
        });
        return clips().stream().filter(c -> c.uuid().equals(copy.uuid())).findFirst().orElse(copy);
    }

    /**
     * Deletes the clips nobody uses ({@link #deleteClip}).
     *
     * @return the clips that were left because an actor or a backend uses them
     */
    public List<Clip> deleteClips(List<String> uuids) {
        List<Clip> known = clips();
        List<Clip> used = new ArrayList<>();
        for (String uuid : uuids) {
            Clip clip = known.stream().filter(c -> c.uuid().equals(uuid)).findFirst().orElse(null);
            if (clip != null && !deleteClip(uuid) && !clipUses(uuid).isEmpty()) {
                used.add(clip);
            }
        }
        return used;
    }

    /** Whether this installation can make shifted clips (ffmpeg with the {@code rubberband} filter). */
    public static boolean canMakeShiftedClips() {
        return Ffmpeg.hasFilter("rubberband");
    }

    /**
     * Which voices a script may use shifted (an unnamed speaker): any when shifted clips can be made here, else only those whose shifted copy is
     * stored (made on a machine with ffmpeg; the others are the base clip, and their cache key says so, so ffmpeg later makes them again).
     */
    public java.util.function.Predicate<VoiceRef> shiftable() {
        if (canMakeShiftedClips()) {
            return v -> true;
        }
        AudiobookSettings settings = getSettings();
        return v -> v.shift() == null || settings.shiftedClip(v.id(), v.shift().spec()) != null;
    }

    /** The shifted copies of the clips of the user: hidden versions of the base clip, made with ffmpeg when first needed. */
    ClipVoices.ShiftedClips shiftedClips() {
        return new ClipVoices.ShiftedClips() {
            @Override
            public String find(String baseUuid, VoiceShift shift) {
                return getSettings().shiftedClip(baseUuid, shift.spec());
            }

            @Override
            public boolean canMake() {
                return canMakeShiftedClips();
            }

            @Override
            public byte[] make(byte[] base, VoiceShift shift) throws BackendException {
                return Ffmpeg.filter(base, shift.filter());
            }

            @Override
            public String store(String baseUuid, VoiceShift shift, byte[] audio, String baseFileName) {
                String stem = (baseFileName == null ? "clip" : baseFileName).replaceFirst("(?i)\\.(wav|mp3)$", "");
                Clip copy = uploadClip(stem + " (" + shift.spec() + ").wav", audio, "audio/wav");
                changeSettings(settings -> settings.addShifted(baseUuid, shift.spec(), copy.uuid(), "shift " + shift.describe()));
                AudiobookLog.get().info("Made the clip {} shifted {} (for an unnamed speaker) as {}", baseUuid, shift.spec(), copy.uuid());
                return copy.uuid();
            }
        };
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
        String key = config.getProtectedApiKey() == null ? null : reveal(config.getProtectedApiKey());
        return config.isHiggs() ? new HiggsComfyBackend(config, key) : new ChatterboxBackend(config, key);
    }

    /**
     * Runs a batch of pieces on the server of the backend: the requests before it, the work, the requests after it. Batches on one server
     * GPU take turns through the GPU lock of the backend (see {@code GpuService}), whoever's and whatever extension's they are.
     */
    public <T> SpeechBatch.Outcome<T> runBatch(BackendConfig config, BooleanSupplier cancelled, SpeechBatch.Work<T> work) throws BackendException {
        // the lease of the batch, for a restart in the middle of it (the steps of the GPU lock stop and start the server)
        java.util.concurrent.atomic.AtomicReference<SpeechBatch.Lease> held = new java.util.concurrent.atomic.AtomicReference<>();
        SpeechBatch.Gpu gpu = gpu(config);
        SpeechBatch.Gpu remembering = c -> {
            SpeechBatch.Lease lease = gpu.take(c);
            held.set(lease);
            return lease;
        };
        SpeechBackend backend = new RecoveringBackend(createBackend(config), () -> restart(config, held.get()), config.getRestartEveryPieces());
        return SpeechBatch.run(backend, remembering, before(config), after(config), cancelled, work);
    }

    /** The GPU lock of the backend, taken for a batch (or a test). */
    private SpeechBatch.Gpu gpu(BackendConfig config) {
        return gpu(config.getGpuLock(), config.getModel());
    }

    /** Takes the GPU lock (blank or null: none), blocking; {@code model} is {{model}} of its steps (null: none). */
    private SpeechBatch.Lease takeGpu(String lock, String model, java.util.function.BooleanSupplier cancelled) throws BackendException {
        return gpu(lock, model).take(cancelled);
    }

    /** The GPU lock of the uuid, taken for a batch, a test or a recognition; {@code model} fills {{model}} of the steps. */
    private SpeechBatch.Gpu gpu(String lock, String model) {
        if (gpuService == null || lock == null || lock.isBlank()) {
            return SpeechBatch.NO_GPU;
        }
        return cancelled -> {
            com.github.enerccio.marginalia.domain.service.gpu.GpuLease lease;
            try {
                lease = gpuService.acquire(lock, com.github.enerccio.marginalia.domain.service.GpuService.model(model), cancelled);
            } catch (com.github.enerccio.marginalia.domain.service.gpu.GpuException e) {
                throw new BackendException(e.getMessage(), e);
            }
            return new SpeechBatch.Lease() {
                @Override
                public void close() {
                    lease.close();
                }

                @Override
                public void reload() throws BackendException {
                    try {
                        lease.reload();
                    } catch (com.github.enerccio.marginalia.domain.service.gpu.GpuException e) {
                        throw new BackendException(e.getMessage(), e);
                    }
                }
            };
        };
    }

    /**
     * Stops the server and starts it again in the middle of a batch (the batch keeps its place on the GPU): the requests after a batch, all of
     * them and whatever they answer (a dead server answers nothing), the release and acquire steps of the GPU lock (which stop and start the
     * server), then the requests before it; a failure of the acquire steps or of the requests before fails the restart.
     */
    private void restart(BackendConfig config, SpeechBatch.Lease lease) throws BackendException {
        AudiobookLog.get().info("Restarting the speech server in the middle of a batch");
        try {
            after(config).run();
        } catch (BackendException e) {
            AudiobookLog.get().warn("The request after the batch failed, the server is started anyway: {}", e.getMessage());
        }
        if (lease != null) {
            lease.reload();
        }
        before(config).run();
    }

    /** The requests before a batch: in order, the first failure stops the batch. */
    private SpeechBatch.Hooks before(BackendConfig config) {
        return () -> {
            String error = runHooks("before a batch", withKey(config.getBeforeHooks(), config), config.isLocal(), true);
            if (error != null) {
                throw new BackendException(error);
            }
        };
    }

    /** The requests after a batch: every request is made, also when one failed (every container must be stopped); the first failure is reported. */
    private SpeechBatch.Hooks after(BackendConfig config) {
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
        return HookKeys.withBearer(hooks, protectedKey == null ? null : reveal(protectedKey), this::reveal);
    }

    /** Runs {@code work} on a thread of its own in the request context of the caller (the services work for the user who asked). */
    private void inBackground(String name, Runnable work) {
        inScope(ThreadCopyRequestAttributes.create(), name, work);
    }

    /** Runs the work on a thread of its own in the request scope given (taken earlier from a page). */
    private static void inScope(RequestAttributes scope, String name, Runnable work) {
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
            try (SpeechBatch.Lease _ = gpu(config).take(null)) {
                before(config).run();
                try {
                    result = ServerCheck.check(config, this::reveal);
                } finally {
                    String afterError = null;
                    try {
                        after(config).run();
                    } catch (BackendException e) {
                        afterError = e.getMessage();
                    }
                    if (afterError != null) {
                        AudiobookLog.get().warn("The request after the test failed: {}", afterError);
                        if (result != null) {
                            error = T.localized(THE_REQUEST_AFTER_THE_TEST_FAILED).formatted(afterError);
                        }
                    }
                }
            } catch (BackendException e) {
                AudiobookLog.get().warn("Testing the connection failed: {}", e.getMessage(), e);
                result = null;
                error = com.github.enerccio.marginalia.extensions.httphooks.Http.describe(e);
            } catch (RuntimeException e) {
                AudiobookLog.get().warn("Testing the connection failed: {}", e.getMessage(), e);
                result = null;
                error = com.github.enerccio.marginalia.extensions.httphooks.Http.describe(e);
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
    /** The sample of the language of the book: long enough to hear the voice settle, with a question and a pause. */
    static final String SAMPLE = "Hello, I am a voice of this book. This is how I sound when I read to you: the quiet parts, the questions, and the "
            + "moments when something unexpected happens. Shall we begin? The story is waiting, and I would very much like to hear how it ends.";
    /** The sample with sounds: the tags are voiced by the models that can ({@link Tags#forModel}), left out for the others. */
    static final String SAMPLE_WITH_SOUNDS = "[sigh] What a long day it has been. And then she told me the whole story, from the very beginning, and I "
            + "simply could not keep a straight face. [laugh] Can you believe it? [gasp] Wait... did you hear that? [chuckle] Never mind. It was only the wind.";
    /** How strongly a mood of a sample is voiced (Higgs): strong enough to be heard. */
    static final double SAMPLE_MOOD_INTENSITY = 0.8;

    /**
     * What a sample says.
     *
     * @param model  the speech model of the backend
     * @param code   the language of a voice of another language (its own short sentence, read plainly), null for the language of the book
     * @param sounds with sounds (laugh, sigh, gasp, chuckle) where the model voices them
     * @param mood   how it is said (Higgs Audio v3 only), null or neutral for plainly
     */
    static String sampleText(String model, String code, boolean sounds, Delivery mood) {
        if (code != null) {
            return SAMPLES.getOrDefault(code, SAMPLE);
        }
        String text = sounds ? SAMPLE_WITH_SOUNDS : SAMPLE;
        if (BackendConfig.MODEL_HIGGS.equals(model)) {
            return HiggsText.render(text, true, mood == null ? Delivery.NEUTRAL : mood, SAMPLE_MOOD_INTENSITY);
        }
        return Tags.forModel(model).isEmpty() ? Tags.strip(text) : text;
    }

    /** The backend a sample of a voice of the language is made with: the multilingual one for another language when there is one, else the selected one. */
    private BackendConfig sampleBackend(String language) {
        BackendConfig multilingual = language == null ? null : backendForModel(BackendConfig.MODEL_MULTILINGUAL);
        return multilingual != null ? multilingual : selectedBackend();
    }

    /** The speech model a sample of a voice of the language is made with (what the choices of the sample depend on), null without a backend. */
    public String sampleModel(String language) {
        BackendConfig backend = sampleBackend(language);
        return backend == null ? null : backend.getModel();
    }

    /** {@link #preview(String, String, boolean, Delivery, BiConsumer)} read plainly. */
    public void preview(String voice, String language, BiConsumer<byte[], String> done) {
        preview(voice, language, false, null, done);
    }

    /**
     * Speaks a sample with a voice so the user can hear it before using it. Each sample is a batch: when the GPU holds something else, the
     * steps of the GPU lock load the speech server first, so the first sample takes a while and the next ones (the GPU stays loaded) do not. A voice for a language speaks a sentence in it, with
     * the multilingual backend when there is one.
     *
     * @param voice    {@code clip:<uuid>} or {@code predefined:<file>}
     * @param language the code, null for the language of the book
     * @param sounds   the sample with sounds the model can voice
     * @param mood     how it is said (Higgs Audio v3), null for plainly
     * @param done     called from a thread of its own with the mp3, or with the message of what failed
     */
    public void preview(String voice, String language, boolean sounds, Delivery mood, BiConsumer<byte[], String> done) {
        inBackground("audiobook-preview", () -> {
            try {
                BackendConfig backend = sampleBackend(language);
                String code = backend != null && language != null && BackendConfig.MODEL_MULTILINGUAL.equals(backend.getModel()) ? language : null;
                if (backend == null) {
                    done.accept(null, T.localized(NO_BACKEND_YET));
                    return;
                }
                VoiceRef ref = VoiceRef.parse(voice);
                String sentence = sampleText(backend.getModel(), code, sounds, mood);
                ClipVoices clips = new ClipVoices(resourceService, uuid -> null, shiftedClips());
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

    /**
     * Speaks a text with a voice as it is typed, for a test: no labelling, no sound cues, no splitting (the tags of the model, such as Higgs'
     * {@code <|...|>}, reach it as they are). The backend in use speaks it under its GPU lock, as a job of its own; the words of a clip go with it when
     * the backend sends them.
     *
     * @param seed null: the seed of the conversions (the same take as a part would get)
     * @param done called with the audio and its extension, or with the message of what failed
     */
    public void speakTest(String voice, String text, Integer seed, java.util.function.BiConsumer<byte[], String> done) {
        inBackground("audiobook-try", () -> {
            try (com.github.enerccio.marginalia.domain.service.job.Jobs.Handle logged = com.github.enerccio.marginalia.domain.service.job.Jobs.open(
                    "audiobook", "Test of a voice", com.github.enerccio.marginalia.domain.service.job.JobCallType.AUDIO)) {
                try {
                    BackendConfig backend = selectedBackend();
                    if (backend == null) {
                        logged.fail();
                        done.accept(null, T.localized(NO_BACKEND_YET));
                        return;
                    }
                    if (text == null || text.isBlank()) {
                        logged.fail();
                        done.accept(null, T.localized(TYPE_A_TEXT));
                        return;
                    }
                    VoiceRef ref = VoiceRef.parse(voice);
                    ClipVoices clips = new ClipVoices(resourceService, uuid -> getSettings().clipText(uuid), shiftedClips());
                    byte[] audio = runBatch(backend, null, speaker -> {
                        Voices.Resolved resolved = clips.resolve(speaker, ref);
                        SpeechRequest request = new SpeechRequest(text.strip(), resolved.mode(), resolved.name(), null, 0.5, backend.getCfgWeight(),
                                backend.getTemperature(), backend.getSpeed(), seed == null ? SEED : seed, "mp3", backend.getChunkSize());
                        if (backend.isSendClipText() && resolved.text() != null) {
                            request = request.withReferenceText(resolved.text());
                        }
                        return speaker.synthesize(request, () -> false);
                    }).value();
                    done.accept(audio, null);
                } catch (IllegalArgumentException | BackendException e) {
                    logged.fail();
                    done.accept(null, e.getMessage());
                } catch (RuntimeException e) {
                    logged.fail();
                    AudiobookLog.get().warn("A test of a voice failed: {}", e.getMessage(), e);
                    done.accept(null, String.valueOf(e.getMessage()));
                }
            }
        });
    }

    /**
     * Loads the speech server of the selected backend on its GPU now (a window of samples opens), so the first sample does not wait for it: the GPU
     * lock of the backend is taken and given back at once. The GPU stays loaded until a job of another lock needs it. Call from the page (it
     * reads the user); the work runs on a thread of its own.
     */
    public void warmUp() {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        inScope(scope, "audiobook-warm-up", () -> {
            try {
                BackendConfig backend = selectedBackend();
                if (backend != null) {
                    gpu(backend).take(null).close();
                }
            } catch (BackendException | RuntimeException e) {
                AudiobookLog.get().warn("The speech server could not be loaded for the samples: {}", e.getMessage());
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
            ConversionQueue queue = new ConversionQueue(new ConversionQueue.Environment(new ResourceAudioStore(resourceService, chatMessageService),
                    new ClipVoices(resourceService, uuid -> getSettings().clipText(uuid), shiftedClips()), this::backendForModel, this::runBatch,
                    () -> tracker != null && tracker.isRunning(id), 1000, this::loudnessTarget, Ffmpeg::normalizeLoudness, this::withRecognizer), worker);
            queue.setRunWatch((titles, stop) -> watchRun(id, titles, stop));
            return queue;
        });
    }

    /**
     * A run of the queue is a run of the registry: the panel of the runs of the user shows it on every page, also after the window that started
     * it is closed, with Stop and what the language model writes while it labels (the requests are made on this thread, which is bound to it).
     */
    private ConversionQueue.Ended watchRun(Long userId, List<String> titles, Runnable stop) {
        Runs.Run run = Runs.run(com.github.enerccio.marginalia.extensions.audiobook.ui.BatchNotifier.SOURCE,
                        () -> T.localized(CONVERT_TO_AUDIO) + (titles.size() == 1 ? ": " + titles.getFirst() : " (" + titles.size() + ")"))
                .owner(userId).quiet().onStop(stop).start();
        Runs.Binding binding = run.bind();
        return (stopped, failed) -> {
            binding.close();
            run.end(stopped ? Runs.Outcome.STOPPED : failed ? Runs.Outcome.FAILED : Runs.Outcome.FINISHED);
        };
    }

    public Long currentUserId() {
        try {
            return settingService.getOrCreate(UserSetting.class).getOwner().getId();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The loudness in LUFS the parts of the user are levelled to when they are joined, null when they are not: the user turned it off, or ffmpeg is not there.
     */
    private Double loudnessTarget() {
        AudiobookSettings user = getSettings();
        return user.isNormalizeLoudness() && Ffmpeg.available() ? user.getTargetLufs() : null;
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

    /** What a part has of audio. */
    public enum StoredAudio {
        /** Nothing was made, or the joined audio is gone. */
        NONE,
        /** Joined audio made from the text the part has now. */
        CURRENT,
        /** Joined audio made from an older text of the part: it is not played, and is made again. */
        OUTDATED
    }

    /**
     * What the Convert window shows of a part.
     *
     * @param number      1 based, as {@link #convertParts} counts the parts
     * @param labelled    the language model's labels are stored with the part
     * @param audio       the joined audio, see {@link StoredAudio}
     * @param finalStatus the studio marks the part final: conversion and the actions of the window leave it alone
     */
    public record PartState(int number, String uuid, String title, boolean labelled, StoredAudio audio, boolean finalStatus) {
    }

    /** The state of a part, from what is stored with it. */
    static PartState stateOf(int number, ChatMessage part, boolean finalStatus) {
        PlayableParts.Ref ref = PlayableParts.ref(part);
        StoredAudio audio = ref == null ? StoredAudio.NONE
                : PartScript.stale(ref.sourceHash(), part.getResponse()) ? StoredAudio.OUTDATED : StoredAudio.CURRENT;
        return new PartState(number, part.getUuid(), title(number, part), PartLabelStore.has(part), audio, finalStatus);
    }

    private static String title(int number, ChatMessage part) {
        String text = part.getResponse() == null ? "" : part.getResponse().strip().replaceAll("\\s+", " ");
        return "Part " + number + ": " + (text.length() > 50 ? text.substring(0, 50) + "..." : text);
    }

    /** The parts of the book with text and what each has (labels, audio, final), in order. */
    public List<PartState> partStates(Manuscript manuscript) throws Exception {
        List<ChatMessage> parts = speakableParts(manuscript);
        StudioStore studio = studioStore();
        List<PartState> states = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            StudioScript stored = studio.find(parts.get(i).getUuid());
            states.add(stateOf(i + 1, parts.get(i), stored != null && stored.status() == StudioScript.Status.FINAL));
        }
        return states;
    }

    /** The state of one part again (a job of it is over), null when the part is gone. */
    public PartState partState(int number, String partUuid) throws Exception {
        ChatMessage part = chatMessageService.findForUser(partUuid);
        if (part == null) {
            return null;
        }
        StudioScript stored = studioStore().find(partUuid);
        return stateOf(number, part, stored != null && stored.status() == StudioScript.Status.FINAL);
    }

    /** True when the part waits in the queue or is being made: its audio and labels can't be taken away under the job. */
    public boolean busy(String partUuid) {
        return queue().jobs().stream().anyMatch(j -> j.partId().equals(partUuid) && !j.finished());
    }

    /** Deletes the audio of the part: the pieces and the joined audio (the labels and the script stay). Not for a part that is in the queue. */
    public void deleteAudio(String partUuid) {
        if (busy(partUuid)) {
            throw new IllegalStateException("The part is in the queue");
        }
        new ResourceAudioStore(resourceService, chatMessageService).discardAudio(partUuid);
    }

    /** Forgets the labels of the part, so that the model is asked again when it is made. Not for a part that is in the queue. */
    public void removeLabels(String partUuid) {
        if (busy(partUuid)) {
            throw new IllegalStateException("The part is in the queue");
        }
        new PartLabelStore(chatMessageService).clear(partUuid);
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
        if (from < 1 || to < from) {
            throw new IllegalArgumentException("The range " + from + " to " + to + " is not in the book");
        }
        return convertParts(manuscript, backend, java.util.stream.IntStream.rangeClosed(from, to).boxed().toList(), labelling, mode);
    }

    /**
     * Like {@link #convert(Manuscript, BackendConfig, int, int, LabelOptions, ConversionJob.Mode)} for chosen parts instead of a range.
     *
     * @param numbers the numbers (1 based, as {@link #partStates} counts them) of the parts, in the order they are wanted
     */
    public List<ConversionJob> convertParts(Manuscript manuscript, BackendConfig backend, List<Integer> numbers, LabelOptions labelling,
                                            ConversionJob.Mode mode) throws Exception {
        Cast cast = castOf(manuscript);
        if (narrator(backend, cast, getSettings()::findActor) == null) {
            throw new IllegalArgumentException("There is no narrator voice, neither in the cast nor in the backend");
        }
        List<ChatMessage> parts = speakableParts(manuscript);
        if (numbers.stream().anyMatch(n -> n < 1 || n > parts.size())) {
            throw new IllegalArgumentException("The book has " + parts.size() + " parts with text");
        }
        if (labelling != null && labelling.ai() == null) {
            throw new IllegalArgumentException("No language model is chosen for the labelling");
        }
        String bookId = manuscript.getUuid();
        List<ConversionQueue.Task> tasks = new ArrayList<>();
        for (int i : numbers) {
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
        return convertFinishedPart(manuscript, part, null, onFinished);
    }

    /**
     * As {@link #convertFinishedPart(Manuscript, ChatMessage, Consumer)}, and told the id of the job when it is queued (to stop it).
     *
     * @param onQueued called with the id of the job before it can end; null for none
     */
    public AutoResult convertFinishedPart(Manuscript manuscript, ChatMessage part, Consumer<String> onQueued,
                                          Consumer<ConversionJob.Snapshot> onFinished) throws Exception {
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
        if (onQueued != null) {
            onQueued.accept(jobs.getFirst().id());
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
        // an unnamed speaker keeps the gender and the age the cast remembers of them
        return castOf(manuscript).voice(speaker, delivery, language, null, null, manuscript.getUuid(), settings::findActor, own, shiftable());
    }

    /** The voice an unnamed speaker of the cast gets (for the cast window: hearing the speaker), null when no actor has a voice for them. */
    public VoiceRef extraVoice(Manuscript manuscript, Cast cast, CastExtra extra) {
        return cast.voice(extra.getName(), Delivery.NEUTRAL.name(), null, extra.getGender(), extra.getAge(), manuscript.getUuid(),
                getSettings()::findActor, null, shiftable());
    }

    /** What the labelling model said about the part, every run that was kept, oldest first. */
    public List<PartReasoningStore.Run> reasoning(String partUuid) {
        return new PartReasoningStore(resourceService, chatMessageService).find(partUuid);
    }

    /** True when the part has stored reasoning (only the part is read, not the file). */
    public boolean hasReasoning(String partUuid) {
        try {
            return PartReasoningStore.has(chatMessageService.findForUser(partUuid));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** Deletes the stored reasoning of the part. @return whether there was one */
    public boolean clearReasoning(String partUuid) {
        return new PartReasoningStore(resourceService, chatMessageService).clear(partUuid);
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
            // the file of the part is read by the export where it is, it is never loaded here
            java.io.File file = new ResourceAudioStore(resourceService, chatMessageService).partFile(partUuid);
            return file == null || !file.isFile() ? AudiobookExporter.PartAudio.missing(T.localized(NO_AUDIO)) : AudiobookExporter.PartAudio.of(file.toPath());
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
                BackendConfig.MODEL_HIGGS.equals(entry.model()) ? HiggsComfyBackend.workflowOf(request) : ChatterboxBackend.bodyOf(request, entry.model())));
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

    /**
     * The labelling instruction for a book: its own setting, else the nearest folder's, else the user's, else the built-in one
     * ({@link LabelPrompt#SETTING}), with the characters and the sound tags of the speech model filled in.
     */
    private String labelPrompt(String bookId, List<String> characters, String model) throws Exception {
        Manuscript book = manuscriptService.findForUser(bookId);
        String template = book == null ? null : manuscriptService.resolveExtension(book, LabelPrompt.SETTING).value();
        // the gender and the age of unnamed speakers are asked only for a cast that uses them (the labels are cached by this text)
        boolean extras = book != null && castOf(book).usesExtras();
        return LabelPrompt.system(templateService, template, characters, model, extras);
    }

    /**
     * Keeps the unnamed speakers a script met in the cast of the book (the gender and the age they got first), so a later part does not give them
     * another voice and the cast window lists them. The cast is read again: the user may have changed it while the job ran.
     */
    private void keepExtras(String bookId, Cast learned) throws Exception {
        Manuscript book = manuscriptService.findForUser(bookId);
        if (book == null) {
            return;
        }
        Cast current = castOf(book);
        boolean changed = false;
        for (CastExtra extra : learned.getExtras()) {
            changed |= current.learnExtra(extra.getName(), extra.getGender(), extra.getAge());
        }
        if (changed) {
            saveCast(book, current);
        }
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
        // a run without the model still reads the labels the part has for this text (made by an earlier run): plain narration would give the
        // dialogue the voice of the narrator, and the keys of those pieces would no longer match the audio that was made
        List<PieceLabel> kept = null;
        if (options == null && !pieces.isEmpty()) {
            PartLabelStore labelStore = new PartLabelStore(chatMessageService);
            kept = labelStore.findForText(partUuid, text);
            if (kept == null) {
                // labels stored before the text was remembered with them: found by the key they were made under, when nothing changed since
                kept = labelStore.find(partUuid, LabelService.cacheKey(text, labelPrompt(bookId, characters, backend.getModel())));
            }
        }
        if (pieces.isEmpty() || (options == null && kept == null)) {
            fresh = ScriptBuilder.studio(text, narrator, backend, SEED, form);
        } else {
            Map<Integer, PieceLabel> labels = new LinkedHashMap<>();
            if (kept != null) {
                AudiobookLog.get().info("Labels of part {}: the stored ones are read, the model is not asked ({} pieces)", partUuid, pieces.size());
                kept.forEach(l -> labels.putIfAbsent(l.id(), l));
            } else {
                String system = labelPrompt(bookId, characters, backend.getModel());
                LabelService.Result asked = LabelService.labels(partUuid, text, pieces, system,
                        new LlmLabelModel(inferenceServices.forAIOrThrow(options.ai()), options.protocol()), new PartLabelStore(chatMessageService));
                AudiobookLog.get().info("Labels of part {}: {} ({} pieces)", partUuid, asked.fromStore() ? "from the store"
                        : "asked of the model (none stored for this text and instruction, the instruction has the characters of the book up to the part)", pieces.size());
                if (asked.missing() > 0) {
                    AudiobookLog.get().warn("The model left {} of {} pieces of a part without a label: the narrator reads them", asked.missing(), pieces.size());
                }
                labels.putAll(asked.labels());
            }
            Set<String> models = backends().stream().map(BackendConfig::getModel).collect(Collectors.toSet());
            java.util.function.Predicate<VoiceRef> shiftable = shiftable();
            boolean[] learned = {false};
            ScriptFromLabels.VoiceChooser chooser = new ScriptFromLabels.VoiceChooser() {
                @Override
                public VoiceRef choose(String speaker, Delivery delivery, String language) {
                    return choose(speaker, delivery, language, null);
                }

                @Override
                public VoiceRef choose(String speaker, Delivery delivery, String language, PieceLabel label) {
                    Gender gender = label == null ? null : label.gender();
                    Age age = label == null ? null : label.age();
                    // an unnamed speaker keeps what the first label said of them (also within this part)
                    if (cast.usesExtras() && cast.learnExtra(speaker, gender, age)) {
                        learned[0] = true;
                    }
                    return cast.voice(speaker, delivery.name(), language, gender, age, bookId, actors, narrator, shiftable);
                }
            };
            fresh = ScriptFromLabels.studio(text, pieces, labels, backend, chooser, models, SEED, form);
            if (learned[0]) {
                keepExtras(bookId, cast);
            }
        }
        StudioScript merged = StudioScript.merge(stored, fresh);
        store.put(partUuid, merged);
        return merged.toPartScript();
    }

    /**
     * The bytes of the audio of a part, read for the user who listens: the owner of the book hears their own resource, any other user the audio of
     * a published book (the core checks that the book can be viewed and that the resource is linked to a part of it, at every request).
     *
     * @param manuscriptUuid the book the player is shown for
     */
    public byte[] audioBytes(String resourceUuid, String manuscriptUuid) {
        try {
            com.github.enerccio.marginalia.domain.model.impl.Resource resource = resourceService.findForUser(resourceUuid);
            if (resource != null) {
                return resourceService.getResourceData(resource);
            }
            return resourceService.readForReader(resourceUuid, manuscriptUuid);
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
