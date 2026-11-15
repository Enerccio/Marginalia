package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What the user keeps for the plugin, in the attributes of the user settings. */
public class AudiobookSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.audiobook";

    private List<BackendConfig> backends = new ArrayList<>();
    private String selectedBackendId;
    /** The id of the {@link AutoMode} for the books that neither they nor a folder say (the user level of the inherited setting). */
    private String autoMode;
    /** The labelling instruction of the user (a template, see {@code LabelPrompt}), the user level of the inherited setting; blank: the built-in one. */
    private String labelPrompt;
    /** Whether the joined audio of a part is levelled to one loudness (needs ffmpeg); null: yes, the default. */
    private Boolean normalizeLoudness;
    /** The loudness in LUFS the parts are levelled to; null: the default. */
    private Double targetLufs;
    /** The voices of the user; the cast of a book names them by id. */
    private List<Actor> actors = new ArrayList<>();
    /** The speech recognition (checks the pieces, recognises the text of a clip); null: none. */
    private RecognizerConfig recognizer;
    /** What the plugin keeps of the reference clips (name, folder, the clip a version was made from, the words), by the uuid of the resource. */
    private Map<String, ClipInfo> clips = new LinkedHashMap<>();
    /** The cleanup of clips on a ComfyUI; null: none. */
    private CleanupConfig cleanup;
    /** The arrangement of the tabs of Voices and clips ({@code TabDock.getLayout()}); null: the default. */
    private com.google.gson.JsonObject voicesLayout;

    public List<BackendConfig> getBackends() {
        if (backends == null) {
            backends = new ArrayList<>();
        }
        return backends;
    }

    public void setBackends(List<BackendConfig> backends) {
        this.backends = backends;
    }

    public String getSelectedBackendId() {
        return selectedBackendId;
    }

    public void setSelectedBackendId(String selectedBackendId) {
        this.selectedBackendId = selectedBackendId;
    }

    /** The loudness of an audiobook (EBU R128) that players and the platforms aim at: speech at about -18 LUFS. */
    public static final double DEFAULT_TARGET_LUFS = -18;
    public static final double MIN_TARGET_LUFS = -30;
    public static final double MAX_TARGET_LUFS = -10;

    public boolean isNormalizeLoudness() {
        return normalizeLoudness == null || normalizeLoudness;
    }

    public void setNormalizeLoudness(boolean normalizeLoudness) {
        this.normalizeLoudness = normalizeLoudness;
    }

    public double getTargetLufs() {
        return targetLufs == null ? DEFAULT_TARGET_LUFS : Math.max(MIN_TARGET_LUFS, Math.min(MAX_TARGET_LUFS, targetLufs));
    }

    public void setTargetLufs(Double targetLufs) {
        this.targetLufs = targetLufs;
    }

    public String getLabelPrompt() {
        return labelPrompt;
    }

    public void setLabelPrompt(String labelPrompt) {
        this.labelPrompt = labelPrompt == null || labelPrompt.isBlank() ? null : labelPrompt;
    }

    public AutoMode getAutoMode() {
        return AutoMode.of(autoMode);
    }

    public void setAutoMode(AutoMode mode) {
        this.autoMode = mode == null ? null : mode.id();
    }

    public List<Actor> getActors() {
        if (actors == null) {
            actors = new ArrayList<>();
        }
        // by name, as the user reads them (the case and the accents do not count)
        java.text.Collator collator = collator();
        actors.sort((a, b) -> collator.compare(a.getName() == null ? "" : a.getName(), b.getName() == null ? "" : b.getName()));
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public Actor findActor(String id) {
        if (id == null) {
            return null;
        }
        return getActors().stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
    }

    /** Puts the actor in place of the one with its id, or at the end. */
    public void putActor(Actor actor) {
        List<Actor> list = getActors();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(actor.getId())) {
                list.set(i, actor);
                return;
            }
        }
        list.add(actor);
    }

    /**
     * One place that names a voice: an actor's own voice ({@code slot} null), an actor's voice for a mood or a language ({@code slot} is the key of
     * {@link Role#getVariants()}, {@code mood:WHISPER}, {@code lang:ja}), or the narrator of a backend.
     */
    public record VoiceUse(boolean backend, String owner, String slot) {
    }

    /** Every place of the actors and the user's own backends that names the voice ({@code clip:<uuid>}). */
    public List<VoiceUse> usesOf(String voice) {
        List<VoiceUse> uses = new ArrayList<>();
        if (voice == null || voice.isBlank()) {
            return uses;
        }
        for (Actor actor : getActors()) {
            if (voice.equals(actor.getVoice())) {
                uses.add(new VoiceUse(false, actor.getName(), null));
            }
            actor.getVariants().forEach((slot, v) -> {
                if (voice.equals(v)) {
                    uses.add(new VoiceUse(false, actor.getName(), slot));
                }
            });
        }
        for (BackendConfig backend : getBackends()) {
            if (voice.equals(backend.getNarratorVoice().strip())) {
                uses.add(new VoiceUse(true, backend.getName(), null));
            }
        }
        return uses;
    }

    /**
     * Puts the voice {@code to} wherever {@code from} is named: an actor's voice and its mood and language voices, a backend's narrator. The casts of the
     * books name actors, not voices, so they follow. The cache keys of the audio name the voice, so what was made with the old one is made again.
     *
     * @return how many places changed
     */
    public int replaceVoice(String from, String to) {
        if (from == null || from.isBlank() || to == null || to.isBlank() || from.equals(to)) {
            return 0;
        }
        int changed = 0;
        for (Actor actor : getActors()) {
            if (from.equals(actor.getVoice())) {
                actor.setVoice(to);
                changed++;
            }
            for (var entry : actor.getVariants().entrySet()) {
                if (from.equals(entry.getValue())) {
                    entry.setValue(to);
                    changed++;
                }
            }
        }
        for (BackendConfig backend : getBackends()) {
            if (from.equals(backend.getNarratorVoice().strip())) {
                backend.setNarratorVoice(to);
                changed++;
            }
        }
        return changed;
    }

    public RecognizerConfig getRecognizer() {
        return recognizer;
    }

    public void setRecognizer(RecognizerConfig recognizer) {
        this.recognizer = recognizer;
    }

    /** The speech recognition when it is set up, else null. */
    public RecognizerConfig recognizer() {
        return recognizer != null && recognizer.isConfigured() ? recognizer : null;
    }

    public CleanupConfig getCleanup() {
        return cleanup;
    }

    public void setCleanup(CleanupConfig cleanup) {
        this.cleanup = cleanup;
    }

    /** The cleanup of clips when it is set up, else null. */
    public CleanupConfig cleanup() {
        return cleanup != null && cleanup.isConfigured() ? cleanup : null;
    }

    public com.google.gson.JsonObject getVoicesLayout() {
        return voicesLayout;
    }

    public void setVoicesLayout(com.google.gson.JsonObject voicesLayout) {
        this.voicesLayout = voicesLayout;
    }

    /** What is kept of the clip, null when nothing is. */
    public ClipInfo clipInfo(String clipUuid) {
        return clipUuid == null || clips == null ? null : clips.get(clipUuid);
    }

    /** The entry of the clip, made when there is none. */
    private ClipInfo infoFor(String clipUuid) {
        if (clips == null) {
            clips = new LinkedHashMap<>();
        }
        return clips.computeIfAbsent(clipUuid, u -> new ClipInfo());
    }

    /** Drops the entry when nothing is kept in it any more. */
    private void tidy(String clipUuid) {
        ClipInfo info = clipInfo(clipUuid);
        if (info != null && info.isEmpty()) {
            clips.remove(clipUuid);
        }
    }

    /** The words spoken in the clip, null when they are not known. */
    public String clipText(String clipUuid) {
        ClipInfo info = clipInfo(clipUuid);
        return info == null ? null : info.getText();
    }

    /** Sets (or with a blank text removes) the words spoken in the clip. */
    public void setClipText(String clipUuid, String text) {
        if (clipUuid == null) {
            return;
        }
        infoFor(clipUuid).setText(text);
        tidy(clipUuid);
    }

    /** The text of a clip goes with its replacement (a cleaned copy says the same words). */
    public void moveClipText(String fromUuid, String toUuid) {
        String text = clipText(fromUuid);
        if (text != null && clipText(toUuid) == null) {
            setClipText(toUuid, text);
        }
    }

    /** Gives the clip a name of the user's (blank: back to the name of the file). */
    public void renameClip(String clipUuid, String name) {
        if (clipUuid == null) {
            return;
        }
        infoFor(clipUuid).setName(name);
        tidy(clipUuid);
    }

    /** The original the clip is a version of (itself for an original); a broken or circular chain ends where it breaks. */
    public String rootOf(String clipUuid) {
        String current = clipUuid;
        java.util.Set<String> seen = new java.util.HashSet<>();
        seen.add(current);
        while (true) {
            ClipInfo info = clipInfo(current);
            String parent = info == null ? null : info.getParent();
            if (parent == null || !seen.add(parent)) {
                return current;
            }
            current = parent;
        }
    }

    /** The folder of the clip: that of its original (a version is shown under it); null for none. */
    public String folderOf(String clipUuid) {
        ClipInfo root = clipInfo(rootOf(clipUuid));
        return root == null ? null : root.getFolder();
    }

    /** Puts the clip (its original, with every version) into the folder; blank: no folder. */
    public void moveClip(String clipUuid, String folder) {
        if (clipUuid == null) {
            return;
        }
        String root = rootOf(clipUuid);
        infoFor(root).setFolder(folder);
        tidy(root);
    }

    /** Marks the clip as a version of the parent, made the way {@code madeWith} says; it takes the parent's words. */
    public void addVersion(String parentUuid, String versionUuid, String name, String madeWith) {
        ClipInfo info = infoFor(versionUuid);
        info.setParent(parentUuid);
        info.setName(name);
        info.setMadeWith(madeWith);
        if (info.getText() == null) {
            info.setText(clipText(parentUuid));
        }
    }

    /** The folders of the clips, by name (the case and the accents do not count). */
    public List<String> folders() {
        java.util.TreeSet<String> folders = new java.util.TreeSet<>(collator());
        if (clips != null) {
            clips.values().stream().map(ClipInfo::getFolder).filter(java.util.Objects::nonNull).forEach(folders::add);
        }
        return new ArrayList<>(folders);
    }

    /** Forgets a deleted clip: its versions become versions of its parent (originals when it had none) and keep their folder. */
    public void forgetClip(String clipUuid) {
        if (clipUuid == null || clips == null) {
            return;
        }
        ClipInfo gone = clips.remove(clipUuid);
        String parent = gone == null ? null : gone.getParent();
        String folder = gone == null ? null : gone.getFolder();
        for (var entry : clips.entrySet()) {
            if (clipUuid.equals(entry.getValue().getParent())) {
                entry.getValue().setParent(parent);
                if (parent == null && entry.getValue().getFolder() == null) {
                    entry.getValue().setFolder(folder);
                }
            }
        }
    }

    /** Compares names as the user reads them (the case and the accents do not count). */
    public static java.text.Collator collator() {
        java.text.Collator collator = java.text.Collator.getInstance();
        collator.setStrength(java.text.Collator.PRIMARY);
        return collator;
    }

    public BackendConfig find(String id) {
        if (id == null) {
            return null;
        }
        return getBackends().stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }

    /** The backend picked, else the first one, null when there is none. */
    public BackendConfig selected() {
        BackendConfig picked = find(selectedBackendId);
        return picked != null ? picked : getBackends().stream().findFirst().orElse(null);
    }
}
