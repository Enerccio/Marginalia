package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The sounds the narration gives a line of speech, found by the code: {@code "You look funny," she giggled.}, {@code She burst out laughing. "Careful!"},
 * {@code with a long, contented moan. "Finally."}. The language model put a tag only where the narration just before a speech describes a sound being made
 * while speaking (a groan, panting): 0 of 9 samples tagged a giggle after the speech or a laugh, a moan or crying before it, whatever the instruction said
 * (TODO.md). So the narration around each speech is read here, in English, and the sound goes to the start of the speech it belongs to:
 * <ul>
 *     <li>a narration piece right after a speech in the same paragraph that starts in lower case ({@code she giggled.}), or that ends the paragraph, belongs to
 *     that speech (an attribution); otherwise a narration piece belongs to the speech right after it in the same paragraph;</li>
 *     <li>the first sound in it counts; none when it is denied ({@code didn't laugh}, {@code stifled a giggle}, {@code fought back a sob}), when it is a
 *     complaint ({@code moaned about the rain}), when a thing makes it ({@code the wind moaned}, {@code the floorboards groaned}) or when another known
 *     speaker is named as the one who makes it;</li>
 *     <li>a bare {@code she cried} is not crying (it is said of an exclamation): crying is {@code crying}, {@code cried quietly}, {@code burst into tears}...</li>
 * </ul>
 * Only for a speech the model gave no tag, and never for what the narrator reads. Moans, whimpers and panting are found as such also in intimate scenes, where they matter most.
 */
public final class SoundCues {

    /** The sounds in the order they are tried at the same position (panting before a gasp: "gasping for air"). */
    private static final List<Map.Entry<String, Pattern>> SOUNDS = List.of(
            sound("pant", "panted|panting|out of breath|breathless(?:ly)?|gasping for (?:air|breath)|catching (?:her|his|their|my) breath|catching breath"),
            sound("giggle", "giggl(?:e|es|ed|ing)|titter(?:s|ed|ing)?"),
            sound("chuckle", "chuckl(?:e|es|ed|ing)|snicker(?:s|ed|ing)?|chortl(?:e|es|ed|ing)"),
            sound("laugh", "burst out laughing|laugh(?:s|ed|ing|ter)?|cackl(?:e|es|ed|ing)|guffaw(?:s|ed|ing)?"),
            sound("sob", "sob(?:s|bed|bing)?|burst into tears|wept|weeping|in tears|through (?:her|his|their|my) tears"),
            sound("cry", "crying|cried (?:quietly|softly|silently|bitterly|harder|openly|again|out loud)|tears (?:streamed|ran|rolled|spilled)"),
            sound("moan", "moan(?:s|ed|ing)?|whimper(?:s|ed|ing)?|mewl(?:s|ed|ing)?"),
            sound("groan", "groan(?:s|ed|ing)?|grunt(?:s|ed|ing)?"),
            sound("sigh", "sigh(?:s|ed|ing)?"),
            sound("gasp", "gasp(?:s|ed)?"),
            sound("scream", "scream(?:s|ed|ing)?|shriek(?:s|ed|ing)?"),
            sound("sniff", "sniff(?:s|ed|ing)?|sniffl(?:e|es|ed|ing)"),
            sound("cough", "cough(?:s|ed|ing)?"),
            sound("sneeze", "sneez(?:e|es|ed|ing)"),
            sound("hum", "hum(?:s|med|ming)"),
            sound("clear throat", "clear(?:ed|ing|s)? (?:her|his|their|my) throat"),
            sound("shush", "shush(?:es|ed|ing)?|hushed (?:him|her|them|me)"));

    /** Words right before a sound that deny it. */
    private static final Pattern DENIED = Pattern.compile(
            "(?i)(?:\\b(?:not|never|without|no)|n't|\\b(?:stifl\\w*|suppress\\w*|swallow\\w*|held back|hold back|holding back|fought back|fight back|fighting back|bit back|"
                    + "biting back|choked back|tried not to|trying not to|refused to|about to|wanted to|resist\\w*))\\W+(?:\\w+\\W+){0,2}$");
    /** A sound that is a complaint, not a sound: "moaned about the rain", "groaned that it was late". */
    private static final Pattern COMPLAINT = Pattern.compile("(?i)^\\W*(?:about|that|on about|over)\\b");
    private static final Pattern SUBJECT = Pattern.compile("(\\w+)\\s+$");

    /** Things that moan, groan or sigh without being a speaker. */
    private static final Set<String> THINGS = Set.of("wind", "winds", "door", "doors", "floor", "floors", "floorboard", "floorboards", "board", "boards", "house",
            "ship", "hull", "engine", "engines", "timber", "timbers", "wood", "wall", "walls", "tree", "trees", "stair", "stairs", "hinge", "hinges", "storm", "building",
            "bed", "springs", "pipes", "pipe", "machine", "machinery", "roof", "beam", "beams", "chair", "branches", "branch", "ice", "metal", "steel", "radio", "kettle");

    private SoundCues() {
    }

    private static Map.Entry<String, Pattern> sound(String tag, String words) {
        return Map.entry(tag, Pattern.compile("(?i)\\b(?:" + words + ")\\b"));
    }

    /**
     * @param allowed the tags of the model: a sound it can't voice is not given
     * @return the tag of a sound for the id of each speech the narration gives one
     */
    public static Map<Integer, String> find(List<Piece> pieces, Map<Integer, PieceLabel> labels, List<String> allowed) {
        Set<String> speakers = new HashSet<>();
        for (PieceLabel label : labels.values()) {
            if (label.speaker() != null && !label.speaker().isBlank()) {
                speakers.add(label.speaker().strip().toLowerCase(Locale.ROOT));
            }
        }
        Map<Integer, String> cues = new HashMap<>();
        for (int i = 0; i < pieces.size(); i++) {
            Piece narration = pieces.get(i);
            if (narration.kind() != Piece.Kind.NARRATION || narration.heading()) {
                continue;
            }
            Piece target = target(pieces, i);
            if (target == null || cues.containsKey(target.id())) {
                continue;
            }
            PieceLabel label = labels.get(target.id());
            String speaker = label == null || label.speaker() == null ? "" : label.speaker().strip().toLowerCase(Locale.ROOT);
            if (speaker.isEmpty() || speaker.equals(ScriptFromLabels.NARRATOR.toLowerCase(Locale.ROOT))) {
                // the narrator reads plainly: a speech without a speaker of its own gets no sound
                continue;
            }
            String tag = first(narration.text(), speaker, speakers);
            if (tag != null && allowed.contains(tag)) {
                cues.put(target.id(), tag);
            }
        }
        return cues;
    }

    /** The speech a narration piece speaks of: the one before it when it is an attribution, else the one after it, both in its paragraph. */
    private static Piece target(List<Piece> pieces, int i) {
        Piece narration = pieces.get(i);
        Piece before = i > 0 && pieces.get(i - 1).kind() == Piece.Kind.SPEECH && pieces.get(i - 1).paragraph() == narration.paragraph() ? pieces.get(i - 1) : null;
        Piece after = i + 1 < pieces.size() && pieces.get(i + 1).kind() == Piece.Kind.SPEECH && pieces.get(i + 1).paragraph() == narration.paragraph()
                ? pieces.get(i + 1) : null;
        String text = narration.text().strip();
        boolean attribution = !text.isEmpty() && Character.isLowerCase(text.codePointAt(0));
        if (before != null && (attribution || after == null)) {
            return before;
        }
        return after;
    }

    /** The first sound the narration gives the speaker, null for none. */
    static String first(String narration, String speaker, Set<String> speakers) {
        String best = null;
        int bestAt = Integer.MAX_VALUE;
        for (Map.Entry<String, Pattern> sound : SOUNDS) {
            Matcher m = sound.getValue().matcher(narration);
            while (m.find()) {
                if (m.start() >= bestAt) {
                    break;
                }
                if (made(narration, m, speaker, speakers)) {
                    best = sound.getKey();
                    bestAt = m.start();
                    break;
                }
            }
        }
        return best;
    }

    /** Whether the sound at the match is made, by the speaker as far as the text tells. */
    private static boolean made(String narration, Matcher m, String speaker, Set<String> speakers) {
        String before = narration.substring(Math.max(0, narration.lastIndexOf('.', m.start() - 1) + 1), m.start());
        if (DENIED.matcher(before).find()) {
            return false;
        }
        if (COMPLAINT.matcher(narration.substring(m.end())).find()) {
            return false;
        }
        // the subject: the word right before the sound with nothing but a space between ("the wind moaned", "Tomas groaned"; not "to the floor, groaning")
        Matcher subject = SUBJECT.matcher(before);
        String last = subject.find() ? subject.group(1).toLowerCase(Locale.ROOT) : "";
        if (THINGS.contains(last)) {
            return false;
        }
        // "Tomas groaned" before a line of Mira's is Tomas's groan
        return last.isEmpty() || !speakers.contains(last) || last.equals(speaker) || speaker.isEmpty();
    }
}
