package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The model reads an earlier part when it is unsure ({@code read_part}): what it may read, what the request says about where an
 * item was learned.
 */
class ReadingPartsTest {

    private static List<Part> branch() {
        List<Part> branch = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            branch.add(new Part("m" + i, "Miyu", "Miyu, Laethia", "Text of part " + i + "."));
        }
        return branch;
    }

    // --- the reader ---

    @Test
    void onlyThePartsBeforeTheRunCanBeRead() {
        PartReader reader = new PartReader(branch(), 3, 1000, null);

        assertEquals("Part 2:\nText of part 2.", reader.read(2));
        assertTrue(reader.read(4).contains("1 to 3"));
        assertTrue(reader.read(0).contains("1 to 3"));
        assertTrue(reader.read(-5).contains("1 to 3"));
        assertEquals(List.of(2), reader.reads());
    }

    @Test
    void aPartIsReadOnceAndAtMostFiveTimes() {
        List<Part> long_ = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            long_.add(new Part("m" + i, null, "t" + i));
        }
        List<Integer> told = new ArrayList<>();
        PartReader reader = new PartReader(long_, 8, 1000, told::add);

        assertTrue(reader.read(1).startsWith("Part 1:"));
        assertTrue(reader.read(1).contains("already"));
        for (int i = 2; i <= 5; i++) {
            assertTrue(reader.read(i).startsWith("Part " + i + ":"));
        }
        assertTrue(reader.read(6).contains("that is all you may read"));
        assertEquals(List.of(1, 2, 3, 4, 5), told);
        assertEquals(told, reader.reads());
    }

    @Test
    void aLongPartIsCutAndSaysSo() {
        List<Part> parts = List.of(new Part("m1", null, "x".repeat(5000)), new Part("m2", null, "next"));
        PartReader reader = new PartReader(parts, 1, 1000, null);

        String text = reader.read(1);

        assertTrue(text.length() < 1200);
        assertTrue(text.contains("the rest is cut"));
    }

    @Test
    void thereIsNothingToReadBeforeTheFirstPart() {
        assertNull(ExtractionPrompt.Readable.of(branch(), branch().subList(0, 2)));
        assertFalse(new PartReader(branch(), 0, 1000, null).isUseful());
        assertTrue(new PartReader(branch(), 0, 1000, null).read(1).contains("no earlier part"));
    }

    @Test
    void theFunctionAnswersFromTheReader() {
        PartReader reader = new PartReader(branch(), 3, 1000, null);
        List<String> answers = new ArrayList<>();
        new StoryFunctionsImpl(reader).readPart(new com.github.enerccio.marginalia.domain.service.function.FunctionContinuation<>() {
            @Override
            public void complete(String result) {
                answers.add(result);
            }

            @Override
            public void fail(String message) {
                Assertions.fail(message);
            }

            @Override
            public void fail(Throwable error) {
                Assertions.fail(error);
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        }, 3);

        assertEquals(List.of("Part 3:\nText of part 3."), answers);
    }

    @Test
    void theApplicationAcceptsTheFunctionAndItsParameter() {
        var definitions = new com.github.enerccio.marginalia.domain.service.impl.FunctionRegistryImpl().definitionsOf(StoryFunctions.class);

        assertEquals(1, definitions.size());
        assertEquals("read_part", definitions.getFirst().getName());
        assertTrue(definitions.getFirst().getParametersSchema().path("properties").has("part"));
    }

    // --- the request ---

    private static KnowledgeState state() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter miyu = new TrackedCharacter("Miyu");
        miyu.getItems().add(KnowledgeItem.learned("Laethia is an elite succubus specialist", ItemKind.KNOWS, "m2", "what the clerk said"));
        miyu.getItems().add(KnowledgeItem.learned("learned in the run itself", ItemKind.KNOWS, "m5", "q"));
        miyu.getItems().add(KnowledgeItem.initial("written by hand", ItemKind.KNOWS));
        state.getCharacters().add(miyu);
        return state;
    }

    @Test
    void theRequestSaysWhereAnItemWasLearnedAndWhichPartTheMessagesAre() {
        List<Part> branch = branch();
        List<Part> run = branch.subList(3, 5);
        ExtractionPrompt.Request request = ExtractionPrompt.build(state(), "", run, null, ExtractionPrompt.Readable.of(branch, run));

        assertTrue(request.user().contains("Laethia is an elite succubus specialist (learned in part 2)"));
        // an item learned in the run, or written by hand, has no earlier part to read
        assertTrue(request.user().contains("learned in the run itself\n"));
        assertTrue(request.user().contains("written by hand\n"));
        assertTrue(request.user().contains("#1 (part 4; POV"));
        assertTrue(request.user().contains("#2 (part 5; POV"));
        assertTrue(request.system().contains("read_part"));
    }

    @Test
    void withoutReadingTheRequestIsTheOneThatWasTested() {
        List<Part> run = branch().subList(3, 5);
        ExtractionPrompt.Request plain = ExtractionPrompt.build(state(), "", run, null);

        assertEquals(ExtractionPrompt.SYSTEM, plain.system());
        assertFalse(plain.user().contains("learned in part"));
        assertTrue(plain.user().contains("#1 (POV"));
        assertEquals(plain.user(), ExtractionPrompt.build(state(), "", run, null, null).user());
    }
}
