package com.github.enerccio.marginalia.extensions.conlangtranslator.designer;

import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.Change;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.Target;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The designer's answer, its requests and the proposals made of an answer.
 */
class DesignerTest {

    private static Language vrakht() {
        Language language = new Language("Vrakht");
        language.setDescription("An imperial tongue");
        language.getRules().add(new RuleSection("Sounds", "Hard consonants, short vowels."));
        language.getRules().add(new RuleSection("Private", "my notes", Audience.NONE));
        language.getCategories().add(new Category("War"));
        language.getRegisters().add(new Register("formal", "court speech"));
        LexiconEntry drak = LexiconEntry.ofAuthor("drak", "war");
        drak.setCategory(language.getCategories().getFirst().getId());
        language.addEntry(drak);
        return language;
    }

    private static final Origin ORIGIN = Origin.model(null, "test-model", 42L);

    // ---- the answer

    @Test
    void theAnswerIsReadFromAFenceAndWithWordsAround() {
        String raw = "Here you go:\n```json\n{\"message\":\"ok\",\"sections\":[{\"title\":\"Numbers\",\"text\":\"Base 12\",\"audience\":\"translator\",\"replaces\":\"\"}],"
                + "\"words\":[{\"word\":\"vael\",\"gloss\":\"mouth\"}]}\n```";
        DesignerAnswer answer = DesignerAnswer.parse(raw);
        assertNotNull(answer);
        assertEquals("ok", answer.message());
        assertEquals("Numbers", answer.sections().getFirst().title());
        assertEquals("vael", answer.words().getFirst().word());
        assertTrue(answer.findings().isEmpty(), "what is missing is empty, never null");
        assertTrue(answer.hasChanges());
    }

    @Test
    void anAnswerWithNoObjectIsNull() {
        assertNull(DesignerAnswer.parse("I could not do that."));
        assertNull(DesignerAnswer.parse(null));
        assertNull(DesignerAnswer.parse("{not json"));
        assertFalse(DesignerAnswer.parse("{\"message\":\"nothing wrong\"}").hasChanges());
    }

    // ---- the requests

    @Test
    void everyRequestCarriesTheLanguageAsItIsButNotTheNotesOfTheAuthor() {
        Request request = DesignerPrompts.words(vrakht(), "War", 5, "siege camp", "Draconia: the empress");
        assertTrue(request.system().contains("THE LANGUAGE: VRAKHT"));
        assertTrue(request.system().contains("Hard consonants"));
        assertTrue(request.system().contains("drak - war") || request.system().contains("drak"));
        assertTrue(request.system().contains("formal - court speech"));
        assertTrue(request.system().contains("Draconia: the empress"), "the lorebook is the world");
        assertFalse(request.system().contains("my notes"), "a section for nobody is never sent");
        assertTrue(request.user().contains("invent 5 new words"));
        assertTrue(request.user().contains("\"War\""));
        assertTrue(request.user().contains("siege camp"));
    }

    @Test
    void aLanguageWithNoRulesSaysSo() {
        Request request = DesignerPrompts.section(new Language("Empty"), "", "");
        assertTrue(request.system().contains("no rules yet"));
        assertFalse(request.system().contains("=== LEXICON ==="));
    }

    @Test
    void aRewriteNamesTheSectionAndGivesItsText() {
        Language language = vrakht();
        Request request = DesignerPrompts.rewrite(language, language.getRules().getFirst(), "make the vowels longer");
        assertTrue(request.user().contains("\"replaces\" set to \"Sounds\""));
        assertTrue(request.user().contains("Hard consonants, short vowels."));
        assertTrue(request.user().contains("make the vowels longer"));
    }

    @Test
    void aStageAsksForOneSectionWithItsTitleAndKeepsTheProposalBeingRefined() {
        Request first = DesignerPrompts.stage(new Language("New"), DesignerStage.GRAMMAR, "A mountain people", "", "", "");
        assertTrue(first.user().contains("THE BRIEF"));
        assertTrue(first.user().contains("\"Grammar\""));
        assertFalse(first.user().contains("REFINING"));
        Request again = DesignerPrompts.stage(new Language("New"), DesignerStage.GRAMMAR, "A mountain people", "SOV order.", "add plurals", "");
        assertTrue(again.user().contains("SOV order."));
        assertTrue(again.user().contains("add plurals"));
    }

    @Test
    void theConversationAndTheSamplesAreInTheUserMessage() {
        Request ask = DesignerPrompts.ask(vrakht(), "why do den and fall clash?", List.of("Author: hi", "Designer: hello"), "");
        assertTrue(ask.user().contains("Designer: hello"));
        assertTrue(ask.user().contains("why do den and fall clash?"));
        Request samples = DesignerPrompts.samples(vrakht(), List.of("The legion marches.", "Hold the line!"));
        assertTrue(samples.user().contains("1. The legion marches."));
        assertTrue(samples.user().contains("2. Hold the line!"));
    }

    // ---- the proposal

    @Test
    void aProposalIsADifferenceAndTheLanguageIsNotChanged() {
        Language language = vrakht();
        DesignerAnswer answer = DesignerAnswer.parse("{\"message\":\"two words\",\"words\":[{\"word\":\"ghuk\",\"gloss\":\"shield\",\"category\":\"War\","
                + "\"rationale\":\"hard root\",\"kind\":\"root\"},{\"word\":\"drak\",\"gloss\":\"war\",\"category\":\"War\"}]}");

        Proposal proposal = ProposalBuilder.build(language, answer, ORIGIN);

        assertEquals(1, language.getLexicon().size(), "nothing is written");
        assertEquals(1, proposal.set().changes().stream().filter(c -> c.target() == Target.WORD && c.kind() == Change.Kind.ADD).count());
        assertEquals(1, proposal.dropped().size());
        assertTrue(proposal.dropped().getFirst().contains("drak (war)"));
        assertEquals("two words", proposal.message());

        LanguageDiff.apply(language, proposal.set());
        LexiconEntry added = language.getLexicon().getLast();
        assertEquals("ghuk", added.getWord());
        assertEquals(EntryStatus.NEW, added.getStatus());
        assertEquals(OriginKind.MODEL, added.getOrigin().getKind());
        assertEquals("test-model", added.getOrigin().getModel());
        assertEquals(language.getCategories().getFirst().getId(), added.getCategory());
    }

    @Test
    void aWordWithANewCategoryBringsTheCategoryAlong() {
        Language language = vrakht();
        DesignerAnswer answer = DesignerAnswer.parse("{\"words\":[{\"word\":\"pol\",\"gloss\":\"pot\",\"category\":\"Cookware\"}]}");
        Proposal proposal = ProposalBuilder.build(language, answer, ORIGIN);

        Change word = proposal.set().changes().stream().filter(c -> c.target() == Target.WORD).findFirst().orElseThrow();
        assertFalse(word.dependsOn().isEmpty());
        LanguageDiff.apply(language, proposal.set());
        assertEquals(2, language.getCategories().size());
        assertEquals("Cookware", language.getCategories().getLast().getName());
    }

    @Test
    void aHomophoneWithAnotherMeaningIsKeptAndTwoEqualWordsInTheAnswerAreOne() {
        Language language = vrakht();
        DesignerAnswer answer = DesignerAnswer.parse("{\"words\":[{\"word\":\"drak\",\"gloss\":\"to hammer\"},{\"word\":\"ghuk\",\"gloss\":\"shield\"},"
                + "{\"word\":\"ghuk\",\"gloss\":\"Shield\"},{\"word\":\"\",\"gloss\":\"x\"}]}");
        Proposal proposal = ProposalBuilder.build(language, answer, ORIGIN);
        assertEquals(2, proposal.set().changes().stream().filter(c -> c.kind() == Change.Kind.ADD).count());
        assertEquals(2, proposal.dropped().size());
    }

    @Test
    void aSectionIsNewOrARewriteOfTheOneOfItsTitle() {
        Language language = vrakht();
        DesignerAnswer answer = DesignerAnswer.parse("{\"sections\":[{\"title\":\"Numbers\",\"text\":\"Base 24.\",\"audience\":\"designer\",\"replaces\":\"\"},"
                + "{\"title\":\"Sounds\",\"text\":\"Hard consonants, long vowels.\",\"audience\":\"\",\"replaces\":\"sounds\"},"
                + "{\"title\":\"\",\"text\":\"no title\"}]}");
        Proposal proposal = ProposalBuilder.build(language, answer, ORIGIN);

        assertEquals(1, proposal.dropped().size());
        LanguageDiff.apply(language, proposal.set());
        assertEquals(3, language.getRules().size());
        assertEquals("Hard consonants, long vowels.", language.getRules().getFirst().getText());
        RuleSection numbers = language.getRules().stream().filter(s -> s.getTitle().equals("Numbers")).findFirst().orElseThrow();
        assertEquals(Audience.DESIGNER, numbers.getAudience());
    }

    @Test
    void anAnswerThatProposesNothingIsAnEmptySet() {
        Proposal proposal = ProposalBuilder.build(vrakht(), DesignerAnswer.parse("{\"message\":\"all is well\"}"), ORIGIN);
        assertTrue(proposal.set().isEmpty());
        assertEquals("all is well", proposal.message());
    }
}
