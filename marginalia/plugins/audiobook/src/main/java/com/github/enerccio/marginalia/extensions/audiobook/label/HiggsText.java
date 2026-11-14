package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text Higgs Audio v3 reads for a piece: its own inline tokens in place of what the labelling said. The delivery of a piece of speech becomes an
 * emotion or a style at its start ({@code <|emotion:sadness|>}, {@code <|style:whispering|>}), a strong one also {@code <|prosody:expressive_high|>}, and
 * each sound tag ({@link Tags#HIGGS}) a sound token followed by the sound written out ({@code <|sfx:laughter|>Haha!}), which the model's documentation asks
 * for; the sounds Higgs has no token for (a groan, panting, a moan) are only written out, with the emotion of the piece around them. That is what
 * worked on the author's server, voice by voice (TODO.md). The result is the text of the script entry, so the studio shows exactly what is sent.
 */
public final class HiggsText {

    /** Below this intensity a delivery is not voiced (a hint of a mood is better read plainly). */
    static final double MIN_INTENSITY = 0.2;
    /** From this intensity the model is asked to be more expressive. */
    static final double STRONG_INTENSITY = 0.8;

    private static final Map<Delivery, String> DELIVERY = Map.ofEntries(
            Map.entry(Delivery.CALM, "<|emotion:contentment|>"),
            Map.entry(Delivery.TENSE, "<|emotion:fear|>"),
            Map.entry(Delivery.SAD, "<|emotion:sadness|>"),
            Map.entry(Delivery.ANGRY, "<|emotion:anger|>"),
            Map.entry(Delivery.JOYFUL, "<|emotion:elation|>"),
            Map.entry(Delivery.AMUSED, "<|emotion:amusement|>"),
            Map.entry(Delivery.WHISPER, "<|style:whispering|>"),
            Map.entry(Delivery.SHOUT, "<|style:shouting|>"),
            Map.entry(Delivery.FEARFUL, "<|emotion:fear|>"),
            Map.entry(Delivery.SURPRISED, "<|emotion:surprise|>"),
            Map.entry(Delivery.TENDER, "<|emotion:affection|>"),
            Map.entry(Delivery.SARCASTIC, "<|emotion:bitterness|>"),
            Map.entry(Delivery.PLEADING, "<|emotion:helplessness|>"),
            Map.entry(Delivery.DESPERATE, "<|emotion:helplessness|>"),
            Map.entry(Delivery.DISGUSTED, "<|emotion:disgust|>"),
            Map.entry(Delivery.AROUSED, "<|emotion:arousal|>"));

    private static final Map<String, String> SOUND = Map.ofEntries(
            Map.entry("laugh", "<|sfx:laughter|>Haha!"),
            Map.entry("chuckle", "<|sfx:laughter|>Heh."),
            Map.entry("giggle", "<|sfx:laughter|>Hehe!"),
            Map.entry("sigh", "<|sfx:sigh|>Haah."),
            Map.entry("cough", "<|sfx:cough|>Khh."),
            Map.entry("sniff", "<|sfx:sniff|>"),
            Map.entry("sneeze", "<|sfx:sneeze|>Achoo!"),
            Map.entry("cry", "<|sfx:crying|>"),
            Map.entry("sob", "<|sfx:crying|>"),
            Map.entry("scream", "<|sfx:screaming|>Aaah!"),
            Map.entry("hum", "<|sfx:humming|>Mmm-hmm."),
            // no token for these: the sound written out, voiced in the emotion of the piece
            Map.entry("groan", "Ugh..."),
            Map.entry("pant", "Haah... haah..."),
            Map.entry("moan", "Mmmh..."),
            Map.entry("gasp", "Hah!"),
            Map.entry("clear throat", "Ahem."),
            Map.entry("shush", "Shh."));

    private static final Pattern TAG = Pattern.compile("\\s*\\[(" + String.join("|", Tags.HIGGS.stream().map(Pattern::quote).toList()) + ")]\\s*");

    private HiggsText() {
    }

    /**
     * @param text      the words of the piece as they are read (the spoken form applied), with the sound tags the labelling put in
     * @param speech    direct speech: only speech gets the delivery (the narrator reads plainly)
     * @param delivery  how the piece is said
     * @param intensity how strongly, 0 to 1
     */
    public static String render(String text, boolean speech, Delivery delivery, double intensity) {
        String body = sounds(text == null ? "" : text);
        if (!speech) {
            return body;
        }
        return prefix(delivery, intensity) + body;
    }

    /** The tokens put in front of a piece of speech of that delivery and intensity; empty when it is read plainly. */
    public static String prefix(Delivery delivery, double intensity) {
        String voiced = delivery == null || intensity < MIN_INTENSITY ? null : DELIVERY.get(delivery);
        if (voiced == null) {
            return "";
        }
        return intensity >= STRONG_INTENSITY ? voiced + "<|prosody:expressive_high|>" : voiced;
    }

    /** The text with a new delivery: the tokens at its start are replaced (the studio, when the delivery or the intensity of a segment changes). */
    public static String withDelivery(String rendered, Delivery delivery, double intensity) {
        return prefix(delivery, intensity) + LEADING.matcher(rendered == null ? "" : rendered).replaceFirst("");
    }

    private static final Pattern LEADING = Pattern.compile("^(?:<\\|(?:emotion|style|prosody):[a-z_]+\\|>)+");

    private static String sounds(String text) {
        Matcher m = TAG.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            boolean start = m.start() == 0;
            boolean end = m.end() == text.length();
            String sound = SOUND.get(m.group(1));
            m.appendReplacement(out, Matcher.quoteReplacement((start ? "" : " ") + sound + (end ? "" : " ")));
        }
        m.appendTail(out);
        return out.toString().strip();
    }
}
