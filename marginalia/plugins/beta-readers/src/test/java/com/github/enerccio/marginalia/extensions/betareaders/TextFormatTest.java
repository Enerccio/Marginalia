package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaPart;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaSpoken;
import com.github.enerccio.marginalia.extensions.betareaders.model.PartType;
import com.github.enerccio.marginalia.extensions.betareaders.service.TextFormat;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFormatTest {

    @Test
    void anActionIsBetweenAsterisksAndSpeechIsPlain() {
        BetaSpoken spoken = new BetaSpoken("Athena", List.of(
                new BetaPart(PartType.ACTION, " nods slowly "), new BetaPart(PartType.SPEECH, "I liked it."),
                new BetaPart(PartType.ACTION, "smiles")));

        assertEquals("**Athena:** *nods slowly* I liked it. *smiles*", TextFormat.render(spoken));
    }

    @Test
    void theMessagesOfATurnAreParagraphs() {
        List<BetaSpoken> turn = List.of(
                new BetaSpoken("Narrator", List.of(new BetaPart(PartType.ACTION, "The room goes quiet."))),
                new BetaSpoken("Vesper", List.of(new BetaPart(PartType.SPEECH, "Finally."))));

        assertEquals("**Narrator:** *The room goes quiet.*\n\n**Vesper:** Finally.", TextFormat.render(turn));
    }
}
