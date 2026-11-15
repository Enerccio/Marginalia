package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.queue.Voices;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceShift;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest.VoiceMode;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.Locale;

/**
 * A clip of the user is a resource: its bytes are put on the speech server (once, under a name made from the bytes) when a batch needs it. A shifted
 * clip (an unnamed speaker, {@link VoiceShift}) is the stored copy when there is one, else it is made from the base clip once and stored
 * ({@link ShiftedClips}); without a way to make it the base clip is sent.
 */
public class ClipVoices implements Voices {

    /** Where the shifted copies of clips are kept, and how one is made. */
    public interface ShiftedClips {

        /** The resource of the clip shifted that way, null when it was not made. */
        String find(String baseUuid, VoiceShift shift);

        /** Whether a shifted copy can be made here (ffmpeg with {@code rubberband}). */
        boolean canMake();

        /** Makes the shifted copy of the bytes. */
        byte[] make(byte[] base, VoiceShift shift) throws BackendException;

        /** Stores the copy as a hidden version of the base clip; returns its uuid. */
        String store(String baseUuid, VoiceShift shift, byte[] audio, String baseFileName);
    }

    /** No shifted clips: a shifted voice is its base clip. */
    public static final ShiftedClips NONE = new ShiftedClips() {
        @Override
        public String find(String baseUuid, VoiceShift shift) {
            return null;
        }

        @Override
        public boolean canMake() {
            return false;
        }

        @Override
        public byte[] make(byte[] base, VoiceShift shift) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String store(String baseUuid, VoiceShift shift, byte[] audio, String baseFileName) {
            throw new UnsupportedOperationException();
        }
    };

    private final ResourceService resources;
    private final java.util.function.UnaryOperator<String> clipText;
    private final ShiftedClips shifted;

    public ClipVoices(ResourceService resources) {
        this(resources, uuid -> null);
    }

    /** @param clipText the words spoken in a clip by its uuid, null when they are not known */
    public ClipVoices(ResourceService resources, java.util.function.UnaryOperator<String> clipText) {
        this(resources, clipText, NONE);
    }

    /** @param shifted the shifted copies of clips */
    public ClipVoices(ResourceService resources, java.util.function.UnaryOperator<String> clipText, ShiftedClips shifted) {
        this.resources = resources;
        this.clipText = clipText;
        this.shifted = shifted == null ? NONE : shifted;
    }

    @Override
    public Resolved resolve(SpeechBackend backend, VoiceRef voice) throws BackendException {
        if (voice.kind() == VoiceRef.Kind.PREDEFINED) {
            return new Resolved(VoiceMode.PREDEFINED, voice.id());
        }
        Resource clip = read(voice.id());
        byte[] bytes = clip == null ? null : data(clip);
        if (bytes == null) {
            throw new BackendException("The reference clip " + voice.id() + " is not among your resources any more");
        }
        String name = clip.getOriginalName() == null ? "" : clip.getOriginalName().toLowerCase(Locale.ROOT);
        String extension = name.endsWith(".mp3") ? "mp3" : "wav";
        if (voice.shift() != null) {
            byte[] copy = shiftedBytes(voice.id(), voice.shift(), bytes, clip.getOriginalName());
            if (copy != null) {
                bytes = copy;
                // the copy is always a wav
                extension = "wav";
            }
        }
        // the shifted copy says the words of its base
        return new Resolved(VoiceMode.CLONE, backend.ensureReference(bytes, extension), clipText.apply(voice.id()));
    }

    /** The bytes of the shifted clip: the stored copy, else made and stored now; null when it can't be had (the base clip is used). */
    private byte[] shiftedBytes(String baseUuid, VoiceShift shift, byte[] base, String baseFileName) throws BackendException {
        String stored = shifted.find(baseUuid, shift);
        if (stored != null) {
            Resource copy = read(stored);
            byte[] bytes = copy == null ? null : data(copy);
            if (bytes != null) {
                return bytes;
            }
        }
        if (!shifted.canMake()) {
            AudiobookLog.get().info("The clip {} shifted {} is not stored and can't be made here (ffmpeg with rubberband): the base clip speaks", baseUuid,
                    shift.spec());
            return null;
        }
        byte[] made = shifted.make(base, shift);
        shifted.store(baseUuid, shift, made, baseFileName);
        return made;
    }

    private Resource read(String uuid) throws BackendException {
        try {
            return resources.findForUser(uuid);
        } catch (Exception e) {
            throw new BackendException("The reference clip could not be read: " + e.getMessage(), e);
        }
    }

    private byte[] data(Resource clip) throws BackendException {
        try {
            return resources.getResourceData(clip);
        } catch (Exception e) {
            throw new BackendException("The reference clip could not be read: " + e.getMessage(), e);
        }
    }
}
