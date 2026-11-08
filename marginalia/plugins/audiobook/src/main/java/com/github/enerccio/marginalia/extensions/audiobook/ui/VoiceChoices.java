package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;

import java.util.ArrayList;
import java.util.List;

/** The reference clips of the user, read once for all the voice fields of a window; a field that adds a clip tells the others. */
public class VoiceChoices {

    private final AudiobookService service;
    private final List<AudiobookService.Clip> clips;
    private final List<Runnable> listeners = new ArrayList<>();

    public VoiceChoices(AudiobookService service) {
        this.service = service;
        this.clips = new ArrayList<>(service.clips());
    }

    public List<AudiobookService.Clip> clips() {
        return List.copyOf(clips);
    }

    /** Puts a clip among the resources of the user and into the choices. */
    public AudiobookService.Clip upload(String filename, byte[] content, String contentType) {
        AudiobookService.Clip clip = service.uploadClip(filename, content, contentType);
        clips.add(clip);
        clips.sort(java.util.Comparator.comparing(AudiobookService.Clip::name, String.CASE_INSENSITIVE_ORDER));
        new ArrayList<>(listeners).forEach(Runnable::run);
        return clip;
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /** What the user sees for a voice: the name of the clip or the file of the voice of the server. */
    public String label(String voice) {
        if (voice == null || voice.isBlank()) {
            return "";
        }
        if (voice.startsWith("clip:")) {
            String uuid = voice.substring("clip:".length());
            return clips.stream().filter(c -> c.uuid().equals(uuid)).map(c -> "♪ " + c.name()).findFirst().orElse("♪ ?");
        }
        return voice.startsWith("predefined:") ? voice.substring("predefined:".length()) : voice;
    }
}
