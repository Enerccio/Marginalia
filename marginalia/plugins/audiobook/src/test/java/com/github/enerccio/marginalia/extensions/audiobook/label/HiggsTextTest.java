package com.github.enerccio.marginalia.extensions.audiobook.label;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HiggsTextTest {

    @Test
    void everySoundHasATokenOrIsWrittenOut() {
        for (String tag : Tags.HIGGS) {
            String rendered = HiggsText.render("Well [" + tag + "] then.", true, Delivery.NEUTRAL, 0.5);
            assertEquals(false, rendered.contains("["), tag + " is left as a tag: " + rendered);
        }
        assertEquals("<|sfx:screaming|>Aaah! Get it away!", HiggsText.render("[scream] Get it away!", true, Delivery.NEUTRAL, 0.5));
        assertEquals("Fine. Ahem. Where was I?", HiggsText.render("Fine. [clear throat] Where was I?", true, Delivery.NEUTRAL, 0.5));
    }

    @Test
    void theDeliveryIsOnlyForSpeechAndAboveAHint() {
        assertEquals("<|style:shouting|>Run!", HiggsText.render("Run!", true, Delivery.SHOUT, 0.5));
        assertEquals("Run!", HiggsText.render("Run!", false, Delivery.SHOUT, 0.5), "narration");
        assertEquals("Run!", HiggsText.render("Run!", true, Delivery.SHOUT, 0.1), "a hint of a shout");
        assertEquals("Run!", HiggsText.render("Run!", true, Delivery.NEUTRAL, 1.0));
        assertEquals("<|emotion:bitterness|><|prosody:expressive_high|>Lovely.", HiggsText.render("Lovely.", true, Delivery.SARCASTIC, 0.9));
    }

    @Test
    void anotherDeliveryReplacesTheTokensAtTheStartOnly() {
        String rendered = HiggsText.render("Please. [sob] Stay.", true, Delivery.PLEADING, 0.9);

        assertEquals("<|emotion:sadness|>Please. <|sfx:crying|> Stay.", HiggsText.withDelivery(rendered, Delivery.SAD, 0.5));
        assertEquals("Please. <|sfx:crying|> Stay.", HiggsText.withDelivery(rendered, Delivery.NEUTRAL, 0.5));
    }
}
