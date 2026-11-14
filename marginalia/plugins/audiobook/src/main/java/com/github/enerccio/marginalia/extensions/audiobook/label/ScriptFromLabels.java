package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.script.*;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Makes the script of a part from its pieces and their labels. This is where the book is protected from the model: the words of a piece are the
 * words of the book, a tagged text is taken only when it is the piece plus allowed tags, and a label that is missing or odd becomes the narrator
 * reading neutrally. Who speaks a piece is the model's call, also for narration (a letter in its writer's voice). What the labelling says is turned into what the engine has: the sound tags only for the models that voice them
 * (Turbo, Higgs), the intensity into {@code exaggeration} only for the models that have one, the delivery into Higgs's emotion tokens ({@link HiggsText}).
 */
public final class ScriptFromLabels {

    public static final String NARRATOR = "Narrator";
    public static final double PAUSE_AFTER_HEADING = 1.0;
    public static final double PAUSE_BETWEEN_PARAGRAPHS = 0.6;
    public static final double PAUSE_BEFORE_ATTRIBUTION = 0.12;
    public static final double PAUSE_DEFAULT = 0.25;

    /** "he said", "Marta whispered": speech followed by it is one breath. */
    private static final Pattern ATTRIBUTION = Pattern.compile("^(he|she|they|I|[A-Z][a-zá-ž]+) (said|asked|answered|whispered|murmured)\\b.*");

    private ScriptFromLabels() {
    }

    /** The voice of a piece: who says it, how, and in which language (null when it is the language of the book). The cast of the book answers. */
    @FunctionalInterface
    public interface VoiceChooser {
        VoiceRef choose(String speaker, Delivery delivery, String language);
    }

    /**
     * @param text            the text of the part (its hash is what tells that the audio has become stale)
     * @param voice           the voice of a piece (the narrator is {@link #NARRATOR}); the cast of the book
     * @param availableModels the models the user has a backend for: a piece in another script (Japanese in an English book) goes to the multilingual model
     *                        when it is among them, and stays on the model of the book when it is not
     * @param seed            the seed of every piece
     */
    public static PartScript build(String text, List<Piece> pieces, Map<Integer, PieceLabel> labels, BackendConfig backend, VoiceChooser voice,
                                   Set<String> availableModels, int seed) {
        return studio(text, pieces, labels, backend, voice, availableModels, seed).toPartScript();
    }

    public static StudioScript studio(String text, List<Piece> pieces, Map<Integer, PieceLabel> labels, BackendConfig backend, VoiceChooser voice,
                                      Set<String> availableModels, int seed) {
        return studio(text, pieces, labels, backend, voice, availableModels, seed, SpokenForm.DEFAULT);
    }

    /** The same script with what the studio keeps of each segment: the words of the piece, the delivery and the intensity. */
    public static StudioScript studio(String text, List<Piece> pieces, Map<Integer, PieceLabel> labels, BackendConfig backend, VoiceChooser voice,
                                      Set<String> availableModels, int seed, SpokenForm form) {
        List<StudioSegment> entries = new ArrayList<>();
        // Higgs only: Turbo books keep the script (and the audio) they had
        Map<Integer, String> cues = backend.isHiggs() ? SoundCues.find(pieces, labels, Tags.HIGGS) : Map.of();
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            PieceLabel label = labels.get(piece.id());
            String speaker = speaker(piece, label);
            double intensity = label == null ? 0.5 : Math.max(0, Math.min(1, label.intensity()));

            String model = backend.getModel();
            String language = Languages.ofScript(piece.text());
            // Higgs reads many languages itself: the piece stays with the voice of the book
            if (language != null && !backend.isHiggs() && availableModels.contains(BackendConfig.MODEL_MULTILINGUAL)) {
                model = BackendConfig.MODEL_MULTILINGUAL;
            } else {
                language = null;
            }
            boolean turbo = BackendConfig.MODEL_TURBO.equals(model);
            boolean higgs = BackendConfig.MODEL_HIGGS.equals(model);
            Delivery delivery = label == null || label.delivery() == null ? Delivery.NEUTRAL : label.delivery();
            // on Higgs whatever the narrator reads is read plainly: no sounds, no mood (also a speech the model left to the narrator)
            boolean plain = higgs && NARRATOR.equals(speaker);
            String said = form.apply(plain ? piece.text() : withCue(spoken(piece, label, Tags.forModel(model)), higgs ? cues.get(piece.id()) : null),
                    piece.heading());
            if (higgs && !said.isEmpty()) {
                // Higgs voices the delivery and the sounds as tokens in the text
                said = HiggsText.render(said, piece.kind() == Piece.Kind.SPEECH && !plain, delivery, intensity);
            }
            if (said.isEmpty()) {
                // a scene break: nothing is read, the pause before it is longer (and none when nothing follows)
                if (!entries.isEmpty()) {
                    StudioSegment last = entries.getLast();
                    double pause = i == pieces.size() - 1 ? 0 : Math.max(last.entry().pauseAfter(), ScriptBuilder.PAUSE_SCENE_BREAK);
                    entries.set(entries.size() - 1, new StudioSegment(last.entry().withPause(pause), last.source(), last.delivery(), last.intensity(),
                            last.locked(), last.takes()));
                }
                continue;
            }
            entries.add(StudioSegment.made(new ScriptEntry(speaker, said, voice.choose(speaker, delivery, language), model, language,
                    // Turbo and Higgs have no exaggeration: a constant keeps the cache from missing for a value that changes nothing
                    turbo || higgs ? 0.5 : exaggeration(intensity), backend.getCfgWeight(), backend.getTemperature(),
                    backend.getSpeed(), seed, backend.getChunkSize(), backend.getFormat(), pause(pieces, i)), piece.text(), delivery.name(), intensity));
        }
        return new StudioScript(entries, Hashes.sha256(text == null ? "" : text), StudioScript.Status.DRAFT);
    }

    /** A piece that starts with an italic span: {@code *...*}, {@code _..._} (also bold): the form of a thought or a letter, and of an emphasis. */
    private static final Pattern STARTS_ITALIC = Pattern.compile("^[*_]{1,3}[^*_\\s].*?[*_]{1,3}");

    /**
     * Who says a piece. The model decides who says speech. Narration is the narrator, also when it is told from a character's point of view
     * ("She watched as Percy..."): it is somebody else only when the model names a character <i>and</i> the piece starts in italics, which is how a
     * thought or a letter is written. Without that mark the answer of the model is not trusted (it takes the character the narration is about).
     */
    static String speaker(Piece piece, PieceLabel label) {
        if (label == null || label.speaker() == null || label.speaker().isBlank() || NARRATOR.equalsIgnoreCase(label.speaker().strip())) {
            return NARRATOR;
        }
        if (piece.kind() == Piece.Kind.NARRATION && !STARTS_ITALIC.matcher(piece.text()).find()) {
            return NARRATOR;
        }
        return label.speaker().strip();
    }

    /** The exaggeration of a piece of that intensity, for the models that have one. */
    public static double exaggeration(double intensity) {
        return Math.round((0.4 + 0.5 * Math.max(0, Math.min(1, intensity))) * 100) / 100.0;
    }

    /**
     * The words of the piece, with the tags of the model when they are valid and the engine can use them. A tagged text that differs in any word
     * from the piece (or has a bracket that is not an allowed tag) is thrown away.
     */
    static String spoken(Piece piece, PieceLabel label, List<String> tags) {
        if (tags.isEmpty() || piece.kind() != Piece.Kind.SPEECH || label == null || label.taggedText() == null || label.taggedText().isBlank()) {
            return piece.text();
        }
        String tagged = label.taggedText().strip();
        if (Tags.count(tagged, tags) == 0 || !Tags.onlyAllowed(tagged, tags)) {
            return piece.text();
        }
        return PieceSplitter.normalize(tagged).equals(PieceSplitter.normalize(piece.text())) ? tagged : piece.text();
    }

    /** The words with the sound the narration gives them ({@link SoundCues}) at their start, unless the model put a tag in already. */
    static String withCue(String spoken, String cue) {
        return cue == null || Tags.count(spoken, Tags.ALL) > 0 ? spoken : "[" + cue + "] " + spoken;
    }

    /** The pause after piece {@code i}, by where it is: after a heading, between paragraphs, before "he said", else a breath. */
    static double pause(List<Piece> pieces, int i) {
        if (i == pieces.size() - 1) {
            return 0;
        }
        Piece piece = pieces.get(i);
        Piece next = pieces.get(i + 1);
        if (piece.heading()) {
            return PAUSE_AFTER_HEADING;
        }
        if (next.paragraph() != piece.paragraph()) {
            return PAUSE_BETWEEN_PARAGRAPHS;
        }
        if (piece.kind() == Piece.Kind.SPEECH && next.kind() == Piece.Kind.NARRATION && ATTRIBUTION.matcher(next.text()).matches()) {
            return PAUSE_BEFORE_ATTRIBUTION;
        }
        return PAUSE_DEFAULT;
    }
}
