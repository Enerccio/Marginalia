package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.audiobook.label.Age;
import com.github.enerccio.marginalia.extensions.audiobook.label.Gender;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceShift;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Who plays whom in one book: the actor of the narrator, the actor of a speaker nobody knows, and the actor of each character. It is kept with the
 * book; the actors (the voices) are the user's, in the audiobook settings.
 * <p>
 * Speakers the cast does not name can get base voices by gender and age (<i>Other men</i>, <i>Other women</i>, <i>Children</i>): then each of them
 * is a shifted version of the base clip of their kind ({@link VoiceShift}), the same in every chapter, and the cast remembers them ({@link #getExtras()}).
 */
public class Cast {

    public static final String NARRATOR = "Narrator";

    private String narratorActorId;
    /** For a speaker the cast does not know; the narrator when it has none. */
    private String fallbackActorId;
    /** The base voice of the men the cast does not name; null: none (they get the fallback). */
    private String menActorId;
    /** The base voice of the women the cast does not name; null: none. */
    private String womenActorId;
    /** The voice of the children the cast does not name; null: the women's base, raised. */
    private String childrenActorId;
    private List<CastMember> members = new ArrayList<>();
    /** The unnamed speakers met so far, with the gender and the age they got first. */
    private List<CastExtra> extras = new ArrayList<>();

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

    public String getMenActorId() {
        return menActorId;
    }

    public void setMenActorId(String menActorId) {
        this.menActorId = blankToNull(menActorId);
    }

    public String getWomenActorId() {
        return womenActorId;
    }

    public void setWomenActorId(String womenActorId) {
        this.womenActorId = blankToNull(womenActorId);
    }

    public String getChildrenActorId() {
        return childrenActorId;
    }

    public void setChildrenActorId(String childrenActorId) {
        this.childrenActorId = blankToNull(childrenActorId);
    }

    public List<CastExtra> getExtras() {
        if (extras == null) {
            extras = new ArrayList<>();
        }
        return extras;
    }

    public void setExtras(List<CastExtra> extras) {
        this.extras = extras == null ? new ArrayList<>() : new ArrayList<>(extras);
    }

    /**
     * Whether the cast gives unnamed speakers voices of their own: it has a base voice for men, women or children. Without one nothing of it is used
     * (the labelling is not asked for gender and age, every unnamed speaker gets the fallback as before).
     */
    public boolean usesExtras() {
        return menActorId != null || womenActorId != null || childrenActorId != null;
    }

    /** Whether the name is a speaker the cast does not name (not the narrator, not a character). */
    public boolean isExtra(String speaker) {
        return speaker != null && !speaker.isBlank() && !NARRATOR.equalsIgnoreCase(speaker.strip()) && find(speaker) == null;
    }

    /** The unnamed speaker of that name (case and accents do not count), null when the cast has not met them. */
    public CastExtra findExtra(String speaker) {
        String wanted = normalize(speaker);
        if (wanted.isEmpty()) {
            return null;
        }
        return getExtras().stream().filter(e -> wanted.equals(normalize(e.getName()))).findFirst().orElse(null);
    }

    /**
     * Remembers an unnamed speaker with what a label says of them: a new one is added, one already known only gets what was unknown of them.
     *
     * @return whether the cast changed
     */
    public boolean learnExtra(String speaker, Gender gender, Age age) {
        if (!isExtra(speaker)) {
            return false;
        }
        CastExtra known = findExtra(speaker);
        if (known != null) {
            return known.learn(gender, age);
        }
        getExtras().add(new CastExtra(speaker.strip(), Gender.orUnknown(gender), Age.orUnknown(age)));
        return true;
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
        return voice(speaker, mood, language, null, null, null, actors, fallback, v -> false);
    }

    /**
     * The voice for a piece, with the base voices of unnamed speakers ({@link #usesExtras()}): an unnamed speaker gets the base of their gender and
     * age (what the cast remembers of them first, else what the label says), shifted into a person of their own when the voice is a clip.
     *
     * @param gender    what the label says of the speaker, null when nothing
     * @param age       what the label says of the speaker, null when nothing
     * @param bookId    the book (the same name in another book is another voice)
     * @param shiftable whether the shifted clip can be had (ffmpeg is there, or the shifted clip is stored); when not, the base clip is used
     */
    public VoiceRef voice(String speaker, String mood, String language, Gender gender, Age age, String bookId, Function<String, Actor> actors,
                          VoiceRef fallback, Predicate<VoiceRef> shiftable) {
        List<String> chain = new ArrayList<>();
        boolean extra = usesExtras() && isExtra(speaker);
        Gender g = Gender.orUnknown(gender);
        Age a = Age.orUnknown(age);
        CastExtra known = extra ? findExtra(speaker) : null;
        if (known != null) {
            g = known.getGender() != Gender.UNKNOWN ? known.getGender() : g;
            a = known.getAge() != Age.UNKNOWN ? known.getAge() : a;
        }
        if (extra) {
            chain.addAll(basesOf(g, a));
            chain.add(fallbackActorId);
        } else if (speaker != null && !speaker.isBlank() && !NARRATOR.equalsIgnoreCase(speaker.strip())) {
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
            if (voice == null) {
                continue;
            }
            // the narrator reading for a speaker nobody gave a voice stays the narrator
            if (extra && voice.kind() == VoiceRef.Kind.CLIP && !id.equals(narratorActorId)) {
                VoiceShift shift = VoiceShift.pick(bookId, speaker, known == null ? 0 : known.getReroll(), g, a, id.equals(childrenActorId));
                VoiceRef shifted = voice.withShift(shift);
                if (shift != null && shiftable.test(shifted)) {
                    return shifted;
                }
            }
            return voice;
        }
        return fallback;
    }

    /** The base voices for an unnamed speaker, best first: a child the children's (else the women's), a teenage boy the men's, else by gender. */
    private List<String> basesOf(Gender gender, Age age) {
        List<String> bases = new ArrayList<>();
        if (age == Age.CHILD) {
            bases.add(childrenActorId);
            bases.add(womenActorId);
        } else if (gender == Gender.MALE) {
            bases.add(menActorId);
        } else if (gender == Gender.FEMALE || age == Age.TEEN) {
            bases.add(womenActorId);
        }
        return bases;
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
        return VoiceShift.normalize(name);
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
