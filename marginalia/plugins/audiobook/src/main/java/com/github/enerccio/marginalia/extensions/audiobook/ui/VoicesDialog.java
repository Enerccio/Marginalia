package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.ClipQuality;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupConfig;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupPreset;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.*;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * Voices and clips: the actors of the user and the reference clips their voices are cloned from, in a {@link TabDock} whose arrangement is kept in the
 * settings of the user. The clips are a tree of folders, clips and their versions (copies cleaned with ffmpeg or with a cleanup preset on ComfyUI); the
 * Clip tab shows the selected clip: its name and folder, its quality ({@link ClipQuality}), where it is used, its words, and what can be done with it
 * (play, clean, use a version in place of its original, replace, delete when unused). A voice model copies the recording, so a clip with background
 * sound is marked and the user is told how to clean it.
 */
public class VoicesDialog extends Dialog {

    static final String ACTORS_TAB = "actors";
    static final String CLIPS_TAB = "clips";
    static final String CLIP_TAB = "clip";

    /** A row of the tree: a folder (no clip) or a clip. */
    record Node(String folder, AudiobookService.Clip clip) {
    }

    private final AudiobookService service;
    private final Runnable actorsChanged;
    private final BiConsumer<String, String> voiceReplaced;
    private final Map<String, Optional<ClipQuality.Result>> qualities = new HashMap<>();
    private final Grid<Actor> actors = new Grid<>();
    private final TreeGrid<Node> clips = new TreeGrid<>();
    private final Button editActor = new Button(Solid.PEN.create(), e -> editActor(actors.asSingleSelect().getValue()));
    private final Button deleteActor = new Button(Solid.TRASH.create(), e -> deleteActor());
    private final ClipEditor editor = new ClipEditor();
    private final Select<Boolean> trimKeep = new Select<>();
    private final Button trim = new Button(T.localized(TRIM), Solid.SCISSORS.create(), e -> trim());
    private final TabDock dock = new TabDock();
    private AudiobookSettings settings;
    private List<AudiobookService.Clip> all = List.of();

    // the Clip tab
    private final Span clipTitle = new Span();
    private final Span pickAClip = new Span(T.localized(PICK_A_CLIP));
    private final TextField clipName = new TextField(T.localized(NAME));
    private final ComboBox<String> folder = new ComboBox<>(T.localized(FOLDER));
    private final Span detail = new Span();
    private final Span madeFrom = new Span();
    private final Span usedBy = new Span();
    private final Button useVersion = new Button(T.localized(USE_THIS_VERSION), Solid.CHECK.create(), e -> useVersion());
    private final Button replace = new Button(T.localized(REPLACE), Solid.RIGHT_LEFT.create(), e -> openReplace());
    private final Button clean = new Button(T.localized(CLEAN_WITH_FFMPEG), Solid.WAND_MAGIC_SPARKLES.create(), e -> clean());
    private final Select<CleanupPreset> preset = new Select<>();
    private final Button cleanWithAi = new Button(T.localized(CLEAN_WITH_AI), Solid.WAND_MAGIC_SPARKLES.create(), e -> cleanWithAi());
    private final Button deleteClip = new Button(T.localized(DELETE), Solid.TRASH.create(), e -> deleteClip());
    private final Anchor download = new Anchor();
    private final ClipTextField clipText;
    private final VoiceTry voiceTry;
    private final VerticalLayout clipDetail = new VerticalLayout();
    private boolean binding;

    /**
     * @param actorsChanged called when an actor was added, changed or deleted (the settings panel shows them)
     * @param voiceReplaced called with the old and the new voice ({@code clip:<uuid>}) after a replacement, so a panel with unsaved backends follows
     */
    public VoicesDialog(AudiobookService service, Runnable actorsChanged, BiConsumer<String, String> voiceReplaced) {
        this.service = service;
        this.actorsChanged = actorsChanged;
        this.voiceReplaced = voiceReplaced;
        this.clipText = new ClipTextField(service);
        this.voiceTry = new VoiceTry(service);
        setHeaderTitle(T.localized(VOICES_AND_CLIPS));
        // no warm-up of the speech server here: the window is used for the cleanup and the recognition of clips too, which take locks of their own,
        // and loading the speech model on opening switched the GPU back and forth (a restart of ComfyUI each time)
        setSizeFull();
        setCloseOnOutsideClick(false);

        dock.setSizeFull();
        dock.add(ACTORS_TAB, T.localized(ACTORS), actorsTab());
        dock.place(CLIPS_TAB, null, ACTORS_TAB);
        dock.add(CLIPS_TAB, T.localized(CLIPS), clipsTab());
        dock.place(CLIP_TAB, null, CLIPS_TAB);
        dock.add(CLIP_TAB, T.localized(CLIP), clipTab());
        dock.setMinimizable(CLIPS_TAB, false);

        Span help = new Span(T.localized(VOICES_AND_CLIPS_HELP));
        help.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout content = new VerticalLayout(help, dock);
        content.setFlexGrow(1, dock);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassNames(CssStyles.OVERFLOW_HIDDEN, CssStyles.COMPACT_SIZES);
        add(content);
        getFooter().add(new Button(T.localized(CLOSE), e -> close()));

        refresh();
        if (settings.getVoicesLayout() != null) {
            dock.setLayout(settings.getVoicesLayout());
        }
        dock.setOnLayoutChange(() -> service.saveVoicesLayout(dock.getLayout()));
    }

    // --- the tabs ---

    private Component actorsTab() {
        actors.addComponentColumn(ActorDialog::nameWithTagline).setHeader(T.localized(NAME)).setFlexGrow(2);
        actors.addColumn(a -> voiceLabel(a.getVoice())).setHeader(T.localized(VOICE)).setFlexGrow(2);
        actors.addColumn(a -> a.getVariants().size()).setHeader(T.localized(OTHER_VOICES)).setFlexGrow(0).setAutoWidth(true);
        actors.addColumn(this::worstOf).setHeader(T.localized(QUALITY)).setFlexGrow(1);
        actors.setSizeFull();
        actors.addItemDoubleClickListener(e -> editActor(e.getItem()));
        actors.asSingleSelect().addValueChangeListener(e -> updateActorButtons());
        Button newActor = new Button(Solid.PLUS.create(), e -> editActor(new Actor()));
        newActor.setTooltipText(T.localized(NEW_ACTOR));
        editActor.setTooltipText(T.localized(EDIT_THE_ACTOR));
        deleteActor.setTooltipText(T.localized(DELETE_THE_ACTOR));
        for (Button b : List.of(editActor, deleteActor, newActor)) {
            b.setThemeName("tertiary small");
        }
        return panel(bar(new H4(T.localized(ACTORS)), newActor, editActor, deleteActor), actors);
    }

    private Component clipsTab() {
        clips.addHierarchyColumn(this::nodeName).setHeader(T.localized(NAME)).setFlexGrow(3);
        clips.addColumn(n -> n.clip() == null ? "" : quality(n.clip().uuid()).map(q -> "%.0f s".formatted(q.seconds())).orElse(""))
                .setHeader(T.localized(LENGTH)).setFlexGrow(0).setAutoWidth(true);
        clips.addColumn(n -> n.clip() == null ? "" : quality(n.clip().uuid()).map(q -> grade(q.grade())).orElse(T.localized(NOT_MEASURED)))
                .setHeader(T.localized(QUALITY)).setFlexGrow(1);
        clips.addColumn(n -> n.clip() == null ? "" : usedBy(n.clip().uuid())).setHeader(T.localized(USED_BY)).setFlexGrow(2);
        clips.setSizeFull();
        clips.asSingleSelect().addValueChangeListener(e -> {
            if (!binding) {
                showClip(e.getValue() == null ? null : e.getValue().clip());
            }
        });
        Upload upload = new Upload(new InMemoryUploadHandler((metadata, data) -> getUI().ifPresent(ui -> ui.access(() -> added(metadata.fileName(), data,
                metadata.contentType())))));
        upload.setAcceptedFileTypes("audio/wav", "audio/x-wav", "audio/mpeg", ".wav", ".mp3");
        upload.setDropAllowed(false);
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button(T.localized(ADD_A_CLIP), Solid.UPLOAD.create()));
        return panel(bar(new H4(T.localized(CLIPS)), upload), clips);
    }

    private Component clipTab() {
        clipTitle.addClassName(CssStyles.TEXT_SECONDARY);
        pickAClip.addClassName(CssStyles.HINT_TEXT);
        clipName.setWidthFull();
        clipName.addValueChangeListener(e -> {
            AudiobookService.Clip clip = selected();
            if (!binding && clip != null && e.isFromClient()) {
                service.renameClip(clip.uuid(), e.getValue());
                refresh();
            }
        });
        folder.setWidthFull();
        folder.setAllowCustomValue(true);
        folder.setClearButtonVisible(true);
        folder.setPlaceholder(T.localized(NO_FOLDER));
        folder.addCustomValueSetListener(e -> moveTo(e.getDetail()));
        folder.addValueChangeListener(e -> {
            if (!binding && e.isFromClient()) {
                moveTo(e.getValue());
            }
        });
        detail.addClassName(CssStyles.HINT_TEXT);
        madeFrom.addClassName(CssStyles.HINT_TEXT);
        usedBy.addClassName(CssStyles.HINT_TEXT);
        clean.setTooltipText(T.localized(CLEAN_WITH_FFMPEG_HELP));
        clean.setVisible(Ffmpeg.available());
        useVersion.setTooltipText(T.localized(USE_THIS_VERSION_HELP));
        preset.setItemLabelGenerator(CleanupPreset::getName);
        preset.setWidth("16em");
        cleanWithAi.setTooltipText(T.localized(CLEAN_WITH_AI_HELP));
        for (Button b : List.of(useVersion, replace, clean, cleanWithAi, deleteClip, trim)) {
            b.setThemeName("tertiary small");
        }
        Button save = new Button(T.localized(DOWNLOAD), Solid.DOWNLOAD.create());
        save.setThemeName("tertiary small");
        download.getElement().setAttribute("download", true);
        download.add(save);
        HorizontalLayout actions = bar(download, useVersion, replace, deleteClip);
        HorizontalLayout cleaning = bar(clean, preset, cleanWithAi);
        trimKeep.setItems(true, false);
        trimKeep.setItemLabelGenerator(k -> k ? T.localized(KEEP_THIS_PART) : T.localized(CUT_THIS_PART_OUT));
        trimKeep.setValue(false);
        trimKeep.setWidth("14em");
        trim.setTooltipText(T.localized(TRIM_HELP));
        HorizontalLayout trimming = bar(trimKeep, trim);
        trimming.setAlignItems(FlexComponent.Alignment.BASELINE);
        clipDetail.setPadding(false);
        clipDetail.add(clipTitle, editor, trimming, clipName, folder, detail, madeFrom, usedBy, actions, cleaning, clipText, voiceTry);
        VerticalLayout tab = new VerticalLayout(pickAClip, clipDetail);
        tab.setSizeFull();
        tab.setPadding(true);
        tab.addClassName(CssStyles.OVERFLOW_AUTO);
        return tab;
    }

    private static VerticalLayout panel(Component header, Component body) {
        VerticalLayout panel = new VerticalLayout(header, body);
        panel.setSizeFull();
        panel.setPadding(true);
        panel.setFlexGrow(1, body);
        return panel;
    }

    private static HorizontalLayout bar(Component... parts) {
        HorizontalLayout bar = new HorizontalLayout(parts);
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.setWrap(true);
        return bar;
    }

    // --- reading ---

    private void refresh() {
        settings = service.getSettings();
        Actor pickedActor = actors.asSingleSelect().getValue();
        AudiobookService.Clip picked = selected();
        actors.setItems(settings.getActors());
        if (pickedActor != null) {
            settings.getActors().stream().filter(a -> a.getId().equals(pickedActor.getId())).findFirst().ifPresent(actors::select);
        }
        all = service.clips();
        fillTree(picked == null ? null : picked.uuid());
        updateActorButtons();
        CleanupConfig cleanup = settings.cleanup();
        List<CleanupPreset> presets = cleanup == null ? List.of() : cleanup.getPresets();
        CleanupPreset chosen = preset.getValue();
        preset.setItems(presets);
        preset.setValue(chosen == null ? presets.stream().findFirst().orElse(null)
                : presets.stream().filter(p -> p.getName().equals(chosen.getName())).findFirst().orElse(presets.stream().findFirst().orElse(null)));
        preset.setVisible(cleanup != null);
        cleanWithAi.setVisible(cleanup != null);
        showClip(selected());
    }

    /** Folders (the clips without one last), their originals, the versions under their original. */
    private void fillTree(String select) {
        TreeData<Node> data = new TreeData<>();
        Map<String, Node> byUuid = new HashMap<>();
        List<String> folders = all.stream().map(AudiobookService.Clip::folder).filter(Objects::nonNull).distinct()
                .sorted(AudiobookSettings.collator()).collect(Collectors.toCollection(ArrayList::new));
        Map<String, Node> folderNodes = new LinkedHashMap<>();
        for (String name : folders) {
            Node node = new Node(name, null);
            folderNodes.put(name, node);
            data.addItem(null, node);
        }
        boolean loose = all.stream().anyMatch(c -> c.folder() == null && c.parent() == null);
        Node noFolder = new Node(null, null);
        if (loose && !folders.isEmpty()) {
            data.addItem(null, noFolder);
        }
        // the clips are sorted by folder and label, so an original comes before its versions
        java.text.Collator collator = AudiobookSettings.collator();
        List<AudiobookService.Clip> ordered = new ArrayList<>(all);
        ordered.sort(Comparator.comparing((AudiobookService.Clip c) -> c.parent() != null).thenComparing(AudiobookService.Clip::name, collator));
        List<AudiobookService.Clip> pending = new ArrayList<>(ordered);
        boolean progress = true;
        while (!pending.isEmpty() && progress) {
            progress = false;
            for (Iterator<AudiobookService.Clip> it = pending.iterator(); it.hasNext(); ) {
                AudiobookService.Clip clip = it.next();
                Node parent;
                if (clip.parent() != null) {
                    parent = byUuid.get(clip.parent());
                    if (parent == null) {
                        continue;
                    }
                } else if (clip.folder() != null) {
                    parent = folderNodes.get(clip.folder());
                } else {
                    parent = folders.isEmpty() ? null : noFolder;
                }
                Node node = new Node(null, clip);
                data.addItem(parent, node);
                byUuid.put(clip.uuid(), node);
                it.remove();
                progress = true;
            }
        }
        binding = true;
        try {
            clips.setDataProvider(new TreeDataProvider<>(data));
            clips.expandRecursively(data.getRootItems(), 3);
            Node picked = select == null ? null : byUuid.get(select);
            if (picked != null) {
                clips.select(picked);
            } else {
                clips.deselectAll();
            }
        } finally {
            binding = false;
        }
    }

    private String nodeName(Node node) {
        if (node.clip() == null) {
            return node.folder() == null ? T.localized(NO_FOLDER) : "📁 " + node.folder();
        }
        return node.clip().parent() == null ? node.clip().name() : "↳ " + node.clip().name();
    }

    private AudiobookService.Clip selected() {
        Node node = clips.asSingleSelect().getValue();
        return node == null ? null : node.clip();
    }

    private AudiobookService.Clip clip(String uuid) {
        return all.stream().filter(c -> c.uuid().equals(uuid)).findFirst().orElse(null);
    }

    private void updateActorButtons() {
        boolean actor = actors.asSingleSelect().getValue() != null;
        editActor.setEnabled(actor);
        deleteActor.setEnabled(actor);
    }

    /** The quality of a clip, measured once per window. */
    private Optional<ClipQuality.Result> quality(String uuid) {
        return qualities.computeIfAbsent(uuid, u -> Optional.ofNullable(service.measureClip(u)));
    }

    private String voiceLabel(String voice) {
        if (voice == null || voice.isBlank()) {
            return "";
        }
        if (voice.startsWith("clip:")) {
            AudiobookService.Clip clip = clip(voice.substring("clip:".length()));
            return clip == null ? "♪ ?" : "♪ " + clip.label();
        }
        return voice.startsWith("predefined:") ? voice.substring("predefined:".length()) : voice;
    }

    /** The worst quality of the clips the actor's voices use. */
    private String worstOf(Actor actor) {
        List<String> voices = new ArrayList<>();
        voices.add(actor.getVoice());
        voices.addAll(actor.getVariants().values());
        ClipQuality.Grade worst = null;
        for (String voice : voices) {
            if (voice != null && voice.startsWith("clip:")) {
                ClipQuality.Grade grade = quality(voice.substring("clip:".length())).map(ClipQuality.Result::grade).orElse(null);
                if (grade != null && (worst == null || grade.ordinal() > worst.ordinal())) {
                    worst = grade;
                }
            }
        }
        return worst == null ? "" : grade(worst);
    }

    static String grade(ClipQuality.Grade grade) {
        return switch (grade) {
            case CLEAN -> T.localized(GRADE_CLEAN);
            case FAIR -> T.localized(GRADE_FAIR);
            case POOR -> T.localized(GRADE_POOR);
        };
    }

    /** What is wrong with a clip, in words; empty for a clean one. */
    static String problems(ClipQuality.Result q) {
        return q.problems().stream().map(p -> switch (p) {
            case BACKGROUND -> T.localized(PROBLEM_BACKGROUND).formatted(q.marginDb());
            case SOME_BACKGROUND -> T.localized(PROBLEM_SOME_BACKGROUND).formatted(q.marginDb());
            case CLIPPED -> T.localized(PROBLEM_CLIPPED);
            case TOO_SHORT -> T.localized(PROBLEM_TOO_SHORT).formatted(q.seconds());
            case TOO_LONG -> T.localized(PROBLEM_TOO_LONG).formatted(q.seconds());
        }).collect(Collectors.joining("; "));
    }

    private String usedBy(String uuid) {
        return settings.usesOf("clip:" + uuid).stream().map(u -> u.slot() == null ? u.owner() : u.owner() + " (" + slotLabel(u.slot()) + ")")
                .distinct().collect(Collectors.joining(", "));
    }

    private static String slotLabel(String slot) {
        return slot.replaceFirst("^mood:", "").replaceFirst("^lang:", "").toLowerCase(Locale.ROOT);
    }

    // --- the Clip tab ---

    private void showClip(AudiobookService.Clip clip) {
        clipText.show(clip == null ? null : clip.uuid());
        voiceTry.show(clip == null ? null : "clip:" + clip.uuid());
        pickAClip.setVisible(clip == null);
        clipDetail.setVisible(clip != null);
        if (clip == null) {
            return;
        }
        binding = true;
        try {
            clipTitle.setText(clip.fileName());
            clipName.setValue(clip.name());
            folder.setItems(settings.folders());
            folder.setValue(clip.folder());
        } finally {
            binding = false;
        }
        // a version is shown under its original, in its folder
        folder.setEnabled(clip.parent() == null);
        folder.setHelperText(clip.parent() == null ? null : T.localized(A_VERSION_IS_IN_THE_FOLDER_OF_ITS_ORIGINAL));
        detail.setText(quality(clip.uuid()).map(q -> grade(q.grade()) + (q.problems().isEmpty() ? "" : ": " + problems(q))).orElse(T.localized(NOT_MEASURED)));
        AudiobookService.Clip parent = clip.parent() == null ? null : clip(clip.parent());
        madeFrom.setVisible(parent != null);
        if (parent != null) {
            madeFrom.setText(T.localized(MADE_FROM).formatted(parent.name(), clip.madeWith() == null ? "?" : clip.madeWith()));
        }
        download.setHref(downloadOf(clip));
        load(clip);
        String uses = usedBy(clip.uuid());
        usedBy.setText(uses.isEmpty() ? T.localized(NOT_USED) : T.localized(USED_BY) + ": " + uses);
        useVersion.setVisible(parent != null);
        cleanWithAi.setEnabled(preset.getValue() != null);
    }

    /** The clip as a file named after its label (a version: the clip, the version), with the kind of audio of its file. */
    private DownloadHandler downloadOf(AudiobookService.Clip clip) {
        String uuid = clip.uuid();
        boolean mp3 = clip.fileName().toLowerCase(Locale.ROOT).endsWith(".mp3");
        String base = clip.label().replace(" › ", " - ").replace(" / ", " - ").replaceFirst("(?i)\\.(wav|mp3)$", "");
        String fileName = base.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_") + (mp3 ? ".mp3" : ".wav");
        return new InputStreamDownloadHandler((InputStreamDownloadCallback) event -> {
            byte[] bytes = service.clipBytes(uuid);
            if (bytes == null) {
                bytes = new byte[0];
            }
            return new DownloadResponse(new ByteArrayInputStream(bytes), fileName, mp3 ? "audio/mpeg" : "audio/wav", bytes.length);
        });
    }

    private void moveTo(String target) {
        AudiobookService.Clip clip = selected();
        if (clip == null || clip.parent() != null) {
            return;
        }
        service.moveClip(clip.uuid(), target);
        refresh();
    }

    // --- the actors ---

    private void editActor(Actor actor) {
        if (actor == null) {
            return;
        }
        new ActorDialog(service, actor, saved -> {
            refresh();
            actorsChanged.run();
        }).open();
    }

    private void deleteActor() {
        Actor doomed = actors.asSingleSelect().getValue();
        ActorDialog.confirmDelete(service, doomed, () -> {
            refresh();
            actorsChanged.run();
        });
    }

    // --- the clips ---

    private void added(String name, byte[] data, String type) {
        try {
            AudiobookService.Clip clip = service.uploadClip(name, data, type);
            refresh();
            selectClip(clip.uuid());
            warnAbout(clip.name(), quality(clip.uuid()).orElse(null));
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The clip could not be added: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_ADDED).formatted(e.getMessage()));
        }
    }

    private void selectClip(String uuid) {
        fillTree(uuid);
        showClip(selected());
        dock.select(CLIP_TAB);
    }

    /** Tells about a clip that is not clean, with the advice to clean it. */
    static void warnAbout(String name, ClipQuality.Result quality) {
        if (quality != null && !quality.problems().isEmpty()) {
            Notification.warning(T.localized(A_CLIP_WITH_PROBLEMS).formatted(name, problems(quality)));
        }
    }

    /** Puts the clip into the editor (it does not start). */
    private void load(AudiobookService.Clip clip) {
        String type = clip.fileName().toLowerCase(Locale.ROOT).endsWith(".mp3") ? "audio/mpeg" : "audio/wav";
        String uuid = clip.uuid();
        StreamResource resource = new StreamResource("clip-" + System.nanoTime() + (type.equals("audio/mpeg") ? ".mp3" : ".wav"), () -> {
            byte[] bytes = service.clipBytes(uuid);
            return new ByteArrayInputStream(bytes == null ? new byte[0] : bytes);
        });
        resource.setContentType(type);
        editor.show(service.clipWaveform(uuid), resource);
    }

    private void trim() {
        AudiobookService.Clip clip = selected();
        Double from = editor.from();
        Double to = editor.to();
        if (clip == null || from == null || to == null || to <= from) {
            Notification.warning(T.localized(GIVE_A_RANGE));
            return;
        }
        try {
            cleaned(service.trimClip(clip.uuid(), from, to, Boolean.TRUE.equals(trimKeep.getValue())));
        } catch (BackendException | RuntimeException e) {
            AudiobookLog.get().warn("The clip could not be trimmed: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_TRIMMED).formatted(e.getMessage()));
        }
    }

    private void clean() {
        AudiobookService.Clip clip = selected();
        if (clip == null) {
            return;
        }
        try {
            cleaned(service.cleanClip(clip.uuid()));
        } catch (BackendException | RuntimeException e) {
            AudiobookLog.get().warn("The clip could not be cleaned: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_CLEANED).formatted(e.getMessage()));
        }
    }

    private void cleanWithAi() {
        AudiobookService.Clip clip = selected();
        CleanupPreset chosen = preset.getValue();
        if (clip == null || chosen == null) {
            return;
        }
        UI ui = UI.getCurrent();
        cleanWithAi.setEnabled(false);
        cleanWithAi.setText(T.localized(CLEANING));
        service.cleanClipWith(clip.uuid(), chosen.getName(), (version, error) -> ui.access(() -> {
            cleanWithAi.setText(T.localized(CLEAN_WITH_AI));
            cleanWithAi.setEnabled(true);
            if (error != null) {
                Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_CLEANED).formatted(error));
            } else if (isOpened()) {
                cleaned(version);
            } else {
                Notification.success(T.localized(THE_CLEANED_CLIP_IS_READY).formatted(version.label(), ""));
            }
            UIPushGuard.push(ui);
        }));
    }

    private void cleaned(AudiobookService.Clip version) {
        refresh();
        selectClip(version.uuid());
        String result = quality(version.uuid()).map(q -> grade(q.grade()) + (q.problems().isEmpty() ? "" : ": " + problems(q))).orElse("");
        Notification.success(T.localized(THE_CLEANED_CLIP_IS_READY).formatted(version.label(), result));
    }

    private void useVersion() {
        AudiobookService.Clip version = selected();
        AudiobookService.Clip parent = version == null || version.parent() == null ? null : clip(version.parent());
        if (parent == null) {
            return;
        }
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(T.localized(USE_THIS_VERSION));
        confirm.setText(T.localized(USE_THIS_VERSION_CONFIRM).formatted(version.name(), parent.name()));
        confirm.setCancelable(true);
        confirm.setConfirmText(T.localized(USE_THIS_VERSION));
        confirm.addConfirmListener(e -> {
            int changed = service.useVersion(version.uuid());
            voiceReplaced.accept("clip:" + parent.uuid(), "clip:" + version.uuid());
            actorsChanged.run();
            Notification.success(T.localized(THE_CLIP_WAS_REPLACED).formatted(changed, version.label()));
            refresh();
        });
        confirm.open();
    }

    private void deleteClip() {
        AudiobookService.Clip clip = selected();
        if (clip == null) {
            return;
        }
        String uses = usedBy(clip.uuid());
        if (!uses.isEmpty()) {
            Notification.error(T.localized(THE_CLIP_IS_USED).formatted(uses));
            return;
        }
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(T.localized(DELETE_THE_CLIP));
        confirm.setText(clip.label());
        confirm.setCancelable(true);
        confirm.setConfirmText(T.localized(DELETE));
        confirm.addConfirmListener(e -> {
            if (service.deleteClip(clip.uuid())) {
                qualities.remove(clip.uuid());
                Notification.success(T.localized(THE_CLIP_IS_DELETED).formatted(clip.label()));
            }
            refresh();
        });
        confirm.open();
    }

    /** The clip that takes the place of the selected one: another clip of the user, or one uploaded here. */
    private void openReplace() {
        AudiobookService.Clip old = selected();
        if (old == null) {
            return;
        }
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(REPLACE_THE_CLIP) + ": " + old.label());
        dialog.setWidth("min(40em, 95vw)");
        ComboBox<AudiobookService.Clip> with = new ComboBox<>(T.localized(REPLACE_WITH));
        with.setItems(service.clips().stream().filter(c -> !c.uuid().equals(old.uuid())).toList());
        with.setItemLabelGenerator(c -> c.label() + quality(c.uuid()).map(q -> " (" + grade(q.grade()) + ")").orElse(""));
        with.setWidthFull();
        Upload upload = new Upload(new InMemoryUploadHandler((metadata, data) -> dialog.getUI().ifPresent(ui -> ui.access(() -> {
            try {
                AudiobookService.Clip clip = service.uploadClip(metadata.fileName(), data, metadata.contentType());
                with.setItems(service.clips().stream().filter(c -> !c.uuid().equals(old.uuid())).toList());
                with.setValue(service.clips().stream().filter(c -> c.uuid().equals(clip.uuid())).findFirst().orElse(null));
                warnAbout(clip.name(), quality(clip.uuid()).orElse(null));
            } catch (RuntimeException e) {
                Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_ADDED).formatted(e.getMessage()));
            }
        }))));
        upload.setAcceptedFileTypes("audio/wav", "audio/x-wav", "audio/mpeg", ".wav", ".mp3");
        upload.setDropAllowed(false);
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button(T.localized(ADD_A_CLIP), Solid.UPLOAD.create()));
        Span help = new Span(T.localized(REPLACE_HELP));
        help.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout content = new VerticalLayout(with, upload, help);
        content.setPadding(false);
        dialog.add(content);
        Button apply = new Button(T.localized(REPLACE_THE_CLIP), e -> {
            AudiobookService.Clip chosen = with.getValue();
            if (chosen == null) {
                return;
            }
            int changed = service.replaceClip(old.uuid(), chosen.uuid());
            voiceReplaced.accept("clip:" + old.uuid(), "clip:" + chosen.uuid());
            actorsChanged.run();
            Notification.success(T.localized(THE_CLIP_WAS_REPLACED).formatted(changed, chosen.label()));
            dialog.close();
            refresh();
        });
        apply.addThemeName("primary");
        dialog.getFooter().add(new Button(T.localized(CANCEL), e -> dialog.close()), apply);
        com.github.enerccio.marginalia.ui.widgets.DialogFocus.onOpen(dialog, with);
        dialog.open();
    }
}
