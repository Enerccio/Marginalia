package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Who plays whom in one book: the actor of the narrator, the actor of a speaker nobody knows, and the actor of each character. It is kept with the
 * book; the actors (the voices) are the user's, in the audiobook settings.
 */
public class Cast {

    public static final String NARRATOR = "Narrator";

    private String narratorActorId;
    /** For a speaker the cast does not know; the narrator when it has none. */
    private String fallbackActorId;
    private List<CastMember> members = new ArrayList<>();

    public String getNarratorActorId() {
        return narratorActorId;
    }

    public void setNarratorActorId(String narratorActorId) {
        this.narratorActorId = narratorActorId;
    }

    public String getFallbackActorId() {
        return fallbackActorId;
    }

    public void setFallbackActorId(String fallbackActorId) {
        this.fallbackActorId = fallbackActorId;
    }

    public List<CastMember> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
        }
        return members;
    }

    public void setMembers(List<CastMember> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }

    /** The names of the characters, for the instruction of the labelling: the model then answers with the names of the cast. */
    public List<String> names() {
        return getMembers().stream().map(CastMember::getName).filter(n -> !n.isBlank()).toList();
    }

    /**
     * The voice for a piece.
     *
     * @param speaker  the name the labelling gave ({@link #NARRATOR} for narration)
     * @param mood     the name of the delivery
     * @param language the language of the piece when it is not the one of the book, else null
     * @param actors   finds an actor by its id (null for one that is gone)
     * @param fallback the voice when the cast has none for the piece (the voice of the backend)
     */
    public VoiceRef voice(String speaker, String mood, String language, Function<String, Actor> actors, VoiceRef fallback) {
        List<String> chain = new ArrayList<>();
        if (speaker != null && !speaker.isBlank() && !NARRATOR.equalsIgnoreCase(speaker.strip())) {
            CastMember member = find(speaker);
            if (member != null) {
                chain.add(member.getActorId());
            }
            chain.add(fallbackActorId);
        }
        chain.add(narratorActorId);
        for (String id : chain) {
            Actor actor = id == null ? null : actors.apply(id);
            VoiceRef voice = actor == null ? null : parse(actor.pick(mood, language));
            if (voice != null) {
                return voice;
            }
        }
        return fallback;
    }

    /**
     * The character a name belongs to: by its name or an alias, else by a name that is one word of exactly one character's name ("Tomáš" for
     * "Tomáš Novák"), ignoring case and accents.
     */
    public CastMember find(String speaker) {
        String wanted = normalize(speaker);
        if (wanted.isEmpty()) {
            return null;
        }
        for (CastMember member : getMembers()) {
            if (wanted.equals(normalize(member.getName())) || aliases(member).contains(wanted)) {
                return member;
            }
        }
        CastMember found = null;
        for (CastMember member : getMembers()) {
            if (List.of(normalize(member.getName()).split(" ")).contains(wanted)) {
                if (found != null) {
                    return null;
                }
                found = member;
            }
        }
        return found;
    }

    private static List<String> aliases(CastMember member) {
        return List.of(member.getAliases().split("[,;]")).stream().map(Cast::normalize).filter(a -> !a.isEmpty()).toList();
    }

    static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").strip();
    }

    private static VoiceRef parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return VoiceRef.parse(text.strip());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
