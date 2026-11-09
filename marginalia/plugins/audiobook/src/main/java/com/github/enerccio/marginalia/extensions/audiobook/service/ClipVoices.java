package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.audiobook.queue.Voices;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest.VoiceMode;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.Locale;

/** A clip of the user is a resource: its bytes are put on the speech server (once, under a name made from the bytes) when a batch needs it. */
public class ClipVoices implements Voices {

    private final ResourceService resources;

    public ClipVoices(ResourceService resources) {
        this.resources = resources;
    }

    @Override
    public Resolved resolve(SpeechBackend backend, VoiceRef voice) throws BackendException {
        if (voice.kind() == VoiceRef.Kind.PREDEFINED) {
            return new Resolved(VoiceMode.PREDEFINED, voice.id());
        }
        Resource clip;
        byte[] bytes;
        try {
            clip = resources.findForUser(voice.id());
            bytes = clip == null ? null : resources.getResourceData(clip);
        } catch (Exception e) {
            throw new BackendException("The reference clip could not be read: " + e.getMessage(), e);
        }
        if (bytes == null) {
            throw new BackendException("The reference clip " + voice.id() + " is not among your resources any more");
        }
        String name = clip.getOriginalName() == null ? "" : clip.getOriginalName().toLowerCase(Locale.ROOT);
        return new Resolved(VoiceMode.CLONE, backend.ensureReference(bytes, name.endsWith(".mp3") ? "mp3" : "wav"));
    }
}
