package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.ClipQuality;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.model.Actor;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * Voices and clips: the actors of the user and the reference clips their voices are cloned from, with the quality of each clip ({@link ClipQuality}),
 * where it is used, and what can be done with it: play, replace everywhere it is used, clean a copy with ffmpeg, delete when unused. A voice model copies
 * the recording, so a clip with background sound is marked and the user is told how to clean it.
 */
public class VoicesDialog extends Dialog {

    private final AudiobookService service;
    private final Runnable actorsChanged;
    private final BiConsumer<String, String> voiceReplaced;
    private final Map<String, Optional<ClipQuality.Result>> qualities = new HashMap<>();
    private final Grid<Actor> actors = new Grid<>();
    private final Grid<AudiobookService.Clip> clips = new Grid<>();
    private final Button editActor = new Button(Solid.PEN.create(), e -> editActor(actors.asSingleSelect().getValue()));
    private final Button deleteActor = new Button(Solid.TRASH.create(), e -> deleteActor());
    private final Button play = new Button(T.localized(PLAY), Solid.PLAY.create(), e -> play());
    private final Button replace = new Button(T.localized(REPLACE), Solid.RIGHT_LEFT.create(), e -> openReplace());
    private final Button clean = new Button(T.localized(CLEAN_WITH_FFMPEG), Solid.WAND_MAGIC_SPARKLES.create(), e -> clean());
    private final Button deleteClip = new Button(Solid.TRASH.create(), e -> deleteClip());
    private final Span detail = new Span();
    private final Element audio = new Element("audio");
    private AudiobookSettings settings;

    /**
     * @param actorsChanged called when an actor was added, changed or deleted (the settings panel shows them)
     * @param voiceReplaced called with the old and the new voice ({@code clip:<uuid>}) after a replacement, so a panel with unsaved backends follows
     */
    public VoicesDialog(AudiobookService service, Runnable actorsChanged, BiConsumer<String, String> voiceReplaced) {
        this.service = service;
        this.actorsChanged = actorsChanged;
        this.voiceReplaced = voiceReplaced;
        setHeaderTitle(T.localized(VOICES_AND_CLIPS));
        setWidth("min(70em, 96vw)");
        setCloseOnOutsideClick(false);

        Span help = new Span(T.localized(VOICES_AND_CLIPS_HELP));
        help.addClassName(com.github.enerccio.marginalia.CssStyles.HINT_TEXT);

        actors.addColumn(ActorDialog::nameOf).setHeader(T.localized(NAME)).setFlexGrow(2);
        actors.addColumn(a -> voiceLabel(a.getVoice())).setHeader(T.localized(VOICE)).setFlexGrow(2);
        actors.addColumn(a -> a.getVariants().size()).setHeader(T.localized(OTHER_VOICES)).setFlexGrow(0).setAutoWidth(true);
        actors.addColumn(this::worstOf).setHeader(T.localized(QUALITY)).setFlexGrow(1);
        actors.setAllRowsVisible(true);
        actors.addItemDoubleClickListener(e -> editActor(e.getItem()));
        actors.asSingleSelect().addValueChangeListener(e -> updateButtons());
        Button newActor = new Button(Solid.PLUS.create(), e -> editActor(new Actor()));
        newActor.setTooltipText(T.localized(NEW_ACTOR));
        editActor.setTooltipText(T.localized(EDIT_THE_ACTOR));
        deleteActor.setTooltipText(T.localized(DELETE_THE_ACTOR));
        HorizontalLayout actorBar = bar(new H4(T.localized(ACTORS)), newActor, editActor, deleteActor);

        clips.addColumn(AudiobookService.Clip::name).setHeader(T.localized(NAME)).setFlexGrow(3);
        clips.addColumn(c -> quality(c.uuid()).map(q -> "%.0f s".formatted(q.seconds())).orElse("")).setHeader(T.localized(LENGTH)).setFlexGrow(0)
                .setAutoWidth(true);
        clips.addColumn(c -> quality(c.uuid()).map(q -> grade(q.grade())).orElse(T.localized(NOT_MEASURED))).setHeader(T.localized(QUALITY)).setFlexGrow(1);
        clips.addColumn(c -> usedBy(c.uuid())).setHeader(T.localized(USED_BY)).setFlexGrow(3);
        clips.setAllRowsVisible(true);
        clips.asSingleSelect().addValueChangeListener(e -> {
            updateButtons();
            showDetail(e.getValue());
        });
        Upload upload = new Upload(new InMemoryUploadHandler((metadata, data) -> getUI().ifPresent(ui -> ui.access(() -> added(metadata.fileName(), data,
                metadata.contentType())))));
        upload.setAcceptedFileTypes("audio/wav", "audio/x-wav", "audio/mpeg", ".wav", ".mp3");
        upload.setDropAllowed(false);
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button(T.localized(ADD_A_CLIP), Solid.UPLOAD.create()));
        for (Button b : List.of(play, replace, clean, deleteClip, editActor, deleteActor, newActor)) {
            b.setThemeName("tertiary small");
        }
        clean.setTooltipText(T.localized(CLEAN_WITH_FFMPEG_HELP));
        clean.setVisible(Ffmpeg.available());
        deleteClip.setTooltipText(T.localized(DELETE_THE_CLIP));
        HorizontalLayout clipBar = bar(new H4(T.localized(CLIPS)), upload, play, replace, clean, deleteClip);
        detail.addClassName(com.github.enerccio.marginalia.CssStyles.HINT_TEXT);
        play.getElement().appendChild(audio);

        VerticalLayout content = new VerticalLayout(help, actorBar, actors, clipBar, clips, detail);
        content.setPadding(false);
        add(content);
        getFooter().add(new Button(T.localized(CLOSE), e -> close()));
        refresh();
    }

    private static HorizontalLayout bar(com.vaadin.flow.component.Component... parts) {
        HorizontalLayout bar = new HorizontalLayout(parts);
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.setWrap(true);
        return bar;
    }

    private void refresh() {
        settings = service.getSettings();
        Actor pickedActor = actors.asSingleSelect().getValue();
        AudiobookService.Clip pickedClip = clips.asSingleSelect().getValue();
        actors.setItems(settings.getActors());
        List<AudiobookService.Clip> all = service.clips();
        clips.setItems(all);
        if (pickedActor != null) {
            settings.getActors().stream().filter(a -> a.getId().equals(pickedActor.getId())).findFirst().ifPresent(a -> actors.select(a));
        }
        if (pickedClip != null) {
            all.stream().filter(c -> c.uuid().equals(pickedClip.uuid())).findFirst().ifPresent(c -> clips.select(c));
        }
        updateButtons();
        showDetail(clips.asSingleSelect().getValue());
    }

    private void updateButtons() {
        boolean actor = actors.asSingleSelect().getValue() != null;
        editActor.setEnabled(actor);
        deleteActor.setEnabled(actor);
        boolean clip = clips.asSingleSelect().getValue() != null;
        play.setEnabled(clip);
        replace.setEnabled(clip);
        clean.setEnabled(clip);
        deleteClip.setEnabled(clip);
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
            String uuid = voice.substring("clip:".length());
            return service.clips().stream().filter(c -> c.uuid().equals(uuid)).map(c -> "♪ " + c.name()).findFirst().orElse("♪ ?");
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

    private void showDetail(AudiobookService.Clip clip) {
        if (clip == null) {
            detail.setText("");
            return;
        }
        detail.setText(quality(clip.uuid()).map(q -> grade(q.grade()) + (q.problems().isEmpty() ? "" : ": " + problems(q))).orElse(T.localized(NOT_MEASURED)));
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
            clips.select(clip);
            warnAbout(clip.name(), quality(clip.uuid()).orElse(null));
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The clip could not be added: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_ADDED).formatted(e.getMessage()));
        }
    }

    /** Tells about a clip that is not clean, with the advice to clean it. */
    static void warnAbout(String name, ClipQuality.Result quality) {
        if (quality != null && !quality.problems().isEmpty()) {
            Notification.warning(T.localized(A_CLIP_WITH_PROBLEMS).formatted(name, problems(quality)));
        }
    }

    private void play() {
        AudiobookService.Clip clip = clips.asSingleSelect().getValue();
        byte[] bytes = clip == null ? null : service.clipBytes(clip.uuid());
        if (bytes == null) {
            return;
        }
        String type = clip.name().toLowerCase(Locale.ROOT).endsWith(".mp3") ? "audio/mpeg" : "audio/wav";
        StreamResource resource = new StreamResource("clip-" + System.nanoTime() + (type.equals("audio/mpeg") ? ".mp3" : ".wav"),
                () -> new ByteArrayInputStream(bytes));
        resource.setContentType(type);
        audio.setAttribute("src", resource);
        audio.callJsFunction("play");
    }

    private void clean() {
        AudiobookService.Clip clip = clips.asSingleSelect().getValue();
        if (clip == null) {
            return;
        }
        try {
            AudiobookService.Clip cleaned = service.cleanClip(clip.uuid());
            refresh();
            clips.select(cleaned);
            String result = quality(cleaned.uuid()).map(q -> grade(q.grade()) + (q.problems().isEmpty() ? "" : ": " + problems(q))).orElse("");
            Notification.success(T.localized(THE_CLEANED_CLIP_IS_READY).formatted(cleaned.name(), result));
        } catch (BackendException | RuntimeException e) {
            AudiobookLog.get().warn("The clip could not be cleaned: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_CLIP_COULD_NOT_BE_CLEANED).formatted(e.getMessage()));
        }
    }

    private void deleteClip() {
        AudiobookService.Clip clip = clips.asSingleSelect().getValue();
        if (clip == null) {
            return;
        }
        String uses = usedBy(clip.uuid());
        if (!uses.isEmpty()) {
            Notification.error(T.localized(THE_CLIP_IS_USED).formatted(uses));
            return;
        }
        com.vaadin.flow.component.confirmdialog.ConfirmDialog confirm = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        confirm.setHeader(T.localized(DELETE_THE_CLIP));
        confirm.setText(clip.name());
        confirm.setCancelable(true);
        confirm.setConfirmText(T.localized(DELETE));
        confirm.addConfirmListener(e -> {
            if (service.deleteClip(clip.uuid())) {
                qualities.remove(clip.uuid());
                Notification.success(T.localized(THE_CLIP_IS_DELETED).formatted(clip.name()));
            }
            refresh();
        });
        confirm.open();
    }

    /** The clip that takes the place of the selected one: another clip of the user, or one uploaded here. */
    private void openReplace() {
        AudiobookService.Clip old = clips.asSingleSelect().getValue();
        if (old == null) {
            return;
        }
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(REPLACE_THE_CLIP) + ": " + old.name());
        dialog.setWidth("min(40em, 95vw)");
        ComboBox<AudiobookService.Clip> with = new ComboBox<>(T.localized(REPLACE_WITH));
        with.setItems(service.clips().stream().filter(c -> !c.uuid().equals(old.uuid())).toList());
        with.setItemLabelGenerator(c -> c.name() + quality(c.uuid()).map(q -> " (" + grade(q.grade()) + ")").orElse(""));
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
        help.addClassName(com.github.enerccio.marginalia.CssStyles.HINT_TEXT);
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
            Notification.success(T.localized(THE_CLIP_WAS_REPLACED).formatted(changed, chosen.name()));
            dialog.close();
            refresh();
        });
        apply.addThemeName("primary");
        dialog.getFooter().add(new Button(T.localized(CANCEL), e -> dialog.close()), apply);
        com.github.enerccio.marginalia.ui.widgets.DialogFocus.onOpen(dialog, with);
        dialog.open();
    }
}
