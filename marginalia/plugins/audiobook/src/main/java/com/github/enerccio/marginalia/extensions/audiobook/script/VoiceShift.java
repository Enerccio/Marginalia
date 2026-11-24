package com.github.enerccio.marginalia.extensions.audiobook.script;

import com.github.enerccio.marginalia.extensions.audiobook.label.Age;
import com.github.enerccio.marginalia.extensions.audiobook.label.Gender;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A change of a reference clip that makes another person of it: a speaker the cast does not name gets a shifted copy of a base clip (made with
 * ffmpeg's {@code rubberband}, kept as a hidden version of the clip), so one recording voices many minor characters, each the same in every chapter.
 * <p>
 * The steps are a small fixed grid, not free values: a speaker gets one cell of it by a hash of the book and the name ({@link #pick}), so the same
 * speaker is the same voice on every run, and each cell is one clip made once. What the grid and the offsets are was tried on Higgs Audio v3
 * (2026-11-16, one listener): lowering the pitch holds up well (−3 semitones still a natural, different man), raising it does not (+2 on a male clip
 * sounded odd, +5 on a female clip was the most a child could take, +7 was alien); the formants alone (±1), the tempo (±7 %) and an EQ tilt were all
 * usable. Changing a constant here changes the voices of the speakers it touches: their pieces are made again (the shift is in the cache key).
 *
 * @param pitch   semitones, the formants moving with the pitch (a bigger or smaller person)
 * @param formant semitones of the formants alone, the pitch kept (another throat)
 * @param tempo   percent of the speed of the clip (100: as it is)
 * @param eq      the tilt of the sound
 */
public record VoiceShift(int pitch, int formant, int tempo, Eq eq) {

    /** The tilt of the sound: darker (less treble, more bass), as it is, brighter. */
    public enum Eq {
        DARK("D", "treble=g=-4:f=3000,bass=g=2:f=150"), FLAT("F", null), BRIGHT("B", "treble=g=4:f=3000,bass=g=-2:f=150");

        private final String code;
        private final String filter;

        Eq(String code, String filter) {
            this.code = code;
            this.filter = filter;
        }

        static Eq ofCode(String code) {
            for (Eq eq : values()) {
                if (eq.code.equals(code)) {
                    return eq;
                }
            }
            throw new IllegalArgumentException("Not a tilt: " + code);
        }
    }

    /** The pitch steps of an adult (more down than up: up sounds wrong sooner). */
    static final int[] ADULT_PITCH = {-3, -2, -1, 0, 1};
    /** The pitch steps of a child or a teenager, on top of the offset of the age. */
    static final int[] YOUNG_PITCH = {0, 1};
    static final int[] ADULT_FORMANT = {-1, 0, 1};
    static final int[] YOUNG_FORMANT = {-1, 0};
    static final int[] TEMPO = {93, 100, 107};
    static final Eq[] EQ = {Eq.DARK, Eq.FLAT, Eq.BRIGHT};

    /** A child from the women's clip: a girl this much higher, a boy this much (and darker formants). */
    static final int GIRL_PITCH = 3;
    static final int BOY_PITCH = 2;
    static final int BOY_FORMANT = -1;
    /** A teenage girl from the women's clip (a teenage boy is the men's clip as it is). */
    static final int TEEN_GIRL_PITCH = 1;
    /** An old speaker from the clip of their gender: lower and slower. */
    static final int ELDERLY_PITCH = -1;
    static final double ELDERLY_TEMPO = 0.93;

    static final int MIN_PITCH = -4;
    /** The highest pitch of an adult (and of a teenage boy, from the men's clip). */
    static final int MAX_PITCH_ADULT = 1;
    static final int MAX_PITCH_TEEN_GIRL = 2;
    /** The highest pitch of a child from the women's clip. */
    static final int MAX_PITCH_CHILD = 4;
    static final int MIN_FORMANT = -2;
    static final int MAX_FORMANT = 1;
    static final int MIN_TEMPO = 85;
    static final int MAX_TEMPO = 110;

    private static final Pattern SPEC = Pattern.compile("p([+-]\\d+)\\.f([+-]\\d+)\\.t(\\d+)\\.e([DFB])");

    public VoiceShift {
        if (eq == null) {
            eq = Eq.FLAT;
        }
    }

    /** Whether the clip is left as it is. */
    public boolean isNone() {
        return pitch == 0 && formant == 0 && tempo == 100 && eq == Eq.FLAT;
    }

    /** The text form, part of the text of a voice and so of the cache key of a piece: {@code p-2.f+1.t107.eB}. */
    public String spec() {
        return String.format(Locale.ROOT, "p%+d.f%+d.t%d.e%s", pitch, formant, tempo, eq.code);
    }

    /** @throws IllegalArgumentException when the text is not one made by {@link #spec()} */
    public static VoiceShift parse(String spec) {
        Matcher matcher = SPEC.matcher(spec == null ? "" : spec.strip());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a voice shift: " + spec);
        }
        return new VoiceShift(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)),
                Eq.ofCode(matcher.group(4)));
    }

    /** For the log and the name of a stored copy: {@code -2 st, formants +1, tempo 107 %, brighter}; the parts that change nothing are left out. */
    public String describe() {
        return describe("formants", "tempo", "darker", "brighter");
    }

    /** The same with the words of the language of the user. */
    public String describe(String formants, String tempoWord, String darker, String brighter) {
        List<String> parts = new ArrayList<>();
        if (pitch != 0) {
            parts.add(String.format(Locale.ROOT, "%+d st", pitch));
        }
        if (formant != 0) {
            parts.add(String.format(Locale.ROOT, "%s %+d", formants, formant));
        }
        if (tempo != 100) {
            parts.add(tempoWord + " " + tempo + " %");
        }
        if (eq != Eq.FLAT) {
            parts.add(eq == Eq.DARK ? darker : brighter);
        }
        return parts.isEmpty() ? "-" : String.join(", ", parts);
    }

    /**
     * The audio filter of ffmpeg that makes the shifted clip. The formants alone are two passes: up (or down) with the formants moving, back with
     * them kept, so only the formants stay moved.
     */
    public String filter() {
        List<String> steps = new ArrayList<>();
        if (pitch != 0) {
            steps.add("rubberband=pitch=" + ratio(pitch) + ":formant=shifted");
        }
        if (formant != 0) {
            steps.add("rubberband=pitch=" + ratio(formant) + ":formant=shifted");
            steps.add("rubberband=pitch=" + ratio(-formant) + ":formant=preserved");
        }
        if (tempo != 100) {
            steps.add(String.format(Locale.ROOT, "rubberband=tempo=%.2f", tempo / 100.0));
        }
        if (eq.filter != null) {
            steps.add(eq.filter);
        }
        return steps.isEmpty() ? "anull" : String.join(",", steps);
    }

    private static String ratio(int semitones) {
        return String.format(Locale.ROOT, "%.6f", Math.pow(2, semitones / 12.0));
    }

    /**
     * The shift of a speaker the cast does not name: a cell of the grid by a hash of the book, the name and the reroll, plus what the age and the
     * gender add (a girl or a boy from the women's clip, an old speaker lower and slower), held in what was tried to sound right.
     *
     * @param bookId         the book: the same name in another book is another voice
     * @param speaker        the name as the labelling gives it (case and accents do not count)
     * @param reroll         0, or how often the user asked for another voice for the speaker
     * @param ownChildVoice  the cast has an actor for children: a child is that voice as it is (a boy a little darker), not the women's clip raised
     * @return null when the clip stays as it is
     */
    public static VoiceShift pick(String bookId, String speaker, int reroll, Gender gender, Age age, boolean ownChildVoice) {
        Gender g = Gender.orUnknown(gender);
        Age a = Age.orUnknown(age);
        boolean young = a == Age.CHILD || a == Age.TEEN;
        int[] pitches = young ? YOUNG_PITCH : ADULT_PITCH;
        int[] formants = young ? YOUNG_FORMANT : ADULT_FORMANT;
        long cell = Long.remainderUnsigned(hash(bookId, speaker, reroll), (long) pitches.length * formants.length * TEMPO.length * EQ.length);
        int pitch = pitches[(int) (cell % pitches.length)];
        cell /= pitches.length;
        int formant = formants[(int) (cell % formants.length)];
        cell /= formants.length;
        double tempo = TEMPO[(int) (cell % TEMPO.length)];
        cell /= TEMPO.length;
        Eq eq = EQ[(int) (cell % EQ.length)];

        int maxPitch = MAX_PITCH_ADULT;
        switch (a) {
            case CHILD -> {
                if (g == Gender.MALE) {
                    pitch += ownChildVoice ? BOY_PITCH - GIRL_PITCH : BOY_PITCH;
                    formant += BOY_FORMANT;
                } else {
                    pitch += ownChildVoice ? 0 : GIRL_PITCH;
                }
                maxPitch = ownChildVoice ? MAX_PITCH_ADULT : MAX_PITCH_CHILD;
            }
            case TEEN -> {
                if (g != Gender.MALE) {
                    pitch += TEEN_GIRL_PITCH;
                    maxPitch = MAX_PITCH_TEEN_GIRL;
                }
            }
            case ELDERLY -> {
                pitch += ELDERLY_PITCH;
                tempo *= ELDERLY_TEMPO;
            }
            default -> {
            }
        }
        VoiceShift shift = new VoiceShift(clamp(pitch, MIN_PITCH, maxPitch), clamp(formant, MIN_FORMANT, MAX_FORMANT),
                clamp((int) Math.round(tempo), MIN_TEMPO, MAX_TEMPO), eq);
        return shift.isNone() ? null : shift;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long hash(String bookId, String speaker, int reroll) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((nullToEmpty(bookId) + "\n" + normalize(speaker) + "\n" + reroll)
                    .getBytes(StandardCharsets.UTF_8));
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (digest[i] & 0xff);
            }
            return value;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    /** A name as the cast compares it: no case, no accents, single spaces. */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").strip();
    }
}
