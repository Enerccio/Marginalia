package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The difference between two languages: what is a change, what is a conflict, and what applying writes.
 */
class LanguageDiffTest {

    private static Language base() {
        Language language = new Language("Vrakht");
        language.setDescription("An imperial tongue");
        language.getRules().add(new RuleSection("Grammar", "Mood particle first."));
        language.getRules().add(new RuleSection("Sounds", "Hard consonants."));
        language.getRules().add(new RuleSection("Names", "Names do not change."));
        language.getCategories().add(new Category("Things"));
        language.getCategories().add(new Category("War"));
        language.getRegisters().add(new Register("formal", "court"));
        LexiconEntry drak = LexiconEntry.ofAuthor("drak", "war");
        drak.setCategory(language.getCategories().get(1).getId());
        language.addEntry(drak);
        language.addEntry(LexiconEntry.ofAuthor("rota", "turn"));
        language.addEntry(LexiconEntry.ofAuthor("mela", "breast"));
        return language;
    }

    private static Language copy(Language language) {
        return ConlangStore.copy(language);
    }

    private static LexiconEntry word(Language language, String form) {
        return language.getLexicon().stream().filter(entry -> entry.getWord().equals(form)).findFirst().orElseThrow();
    }

    private static Change change(ChangeSet set, Target target, Change.Kind kind) {
        return set.changes().stream().filter(c -> c.target() == target && c.kind() == kind).findFirst().orElseThrow();
    }

    private static FieldChange field(Change change, String name) {
        return change.fields().stream().filter(f -> f.field().equals(name)).findFirst().orElseThrow();
    }

    // ---- nothing, add, change, delete

    @Test
    void equalLanguagesHaveNoDifference() {
        Language base = base();
        assertTrue(LanguageDiff.diff(base, copy(base), copy(base)).isEmpty());
        assertTrue(LanguageDiff.compare(base, copy(base)).isEmpty());
    }

    @Test
    void aNewWordIsAnAddThatPutsTheWordAtTheEnd() {
        Language base = base();
        Language incoming = copy(base);
        incoming.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        assertEquals(1, set.changes().size());
        Change add = set.changes().getFirst();
        assertEquals(Change.Kind.ADD, add.kind());
        assertEquals("vael (mouth)", add.label());
        assertNull(add.conflict());
        assertTrue(add.isPicked());

        LanguageDiff.Result result = LanguageDiff.apply(current, set);
        assertEquals(1, result.applied().size());
        assertEquals("vael", current.getLexicon().getLast().getWord());
        assertEquals("mouth", current.getLexicon().getLast().getSenses().getFirst().getGloss());
        assertEquals(incoming.getLexicon().getLast().getId(), current.getLexicon().getLast().getId(), "the identity comes along");
    }

    @Test
    void aFieldOnlyTheOtherLanguageChangedIsAChangeAndAppliesTheirValue() {
        Language base = base();
        Language incoming = copy(base);
        word(incoming, "rota").setRationale("borrowed whole");
        word(incoming, "rota").setStatus(EntryStatus.LOCKED);
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change change = change(set, Target.WORD, Change.Kind.CHANGE);
        assertEquals(2, change.fields().size());
        assertEquals("", field(change, "rationale").before());
        assertEquals("borrowed whole", field(change, "rationale").after());
        assertFalse(change.hasConflict());

        LanguageDiff.apply(current, set);
        assertEquals("borrowed whole", word(current, "rota").getRationale());
        assertEquals(EntryStatus.LOCKED, word(current, "rota").getStatus());
        assertEquals("turn", word(current, "rota").getSenses().getFirst().getGloss(), "what did not change is left alone");
    }

    @Test
    void aFieldWeChangedAndTheyDidNotIsNotInTheSet() {
        Language base = base();
        Language incoming = copy(base);
        Language current = copy(base);
        word(current, "rota").setRationale("mine");
        assertTrue(LanguageDiff.diff(base, incoming, current).isEmpty());
    }

    @Test
    void bothChangingAFieldToTheSameValueIsNothing() {
        Language base = base();
        Language incoming = copy(base);
        Language current = copy(base);
        word(incoming, "rota").setRationale("same");
        word(current, "rota").setRationale("same");
        assertTrue(LanguageDiff.diff(base, incoming, current).isEmpty());
    }

    @Test
    void aDeletedWordIsADeleteAndTheLinksToItGo() {
        Language base = base();
        LexiconEntry rota = word(base, "rota");
        word(base, "mela").getVariants().add(rota.getId());
        Language incoming = copy(base);
        incoming.deleteEntry(rota.getId());
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        // the delete, and the change of the word that linked to it (the link went with the word in the other language)
        assertEquals(1, set.changes().stream().filter(c -> c.kind() == Change.Kind.DELETE).count());
        assertEquals("variants", change(set, Target.WORD, Change.Kind.CHANGE).fields().getFirst().field());
        LanguageDiff.apply(current, set);
        assertEquals(2, current.getLexicon().size());
        assertTrue(word(current, "mela").getVariants().isEmpty());
    }

    // ---- conflicts

    @Test
    void aFieldBothChangedDifferentlyIsAConflictThatKeepsOurValueUntilTheUserTakesTheirs() {
        Language base = base();
        Language incoming = copy(base);
        Language current = copy(base);
        word(incoming, "rota").setRationale("theirs");
        word(incoming, "rota").setNotes("their note");
        word(current, "rota").setRationale("ours");

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change change = change(set, Target.WORD, Change.Kind.CHANGE);
        assertTrue(change.hasConflict());
        assertNull(change.conflict(), "the change is not in conflict, one field is");
        assertTrue(field(change, "rationale").isConflict());
        assertEquals("ours", field(change, "rationale").value());
        assertFalse(field(change, "notes").isConflict());

        LanguageDiff.apply(current, set);
        assertEquals("ours", word(current, "rota").getRationale(), "a conflicting field keeps our value");
        assertEquals("their note", word(current, "rota").getNotes(), "what is not in conflict comes over");

        // the user takes theirs
        Language again = copy(base);
        word(again, "rota").setRationale("ours");
        ChangeSet second = LanguageDiff.diff(base, incoming, again);
        field(change(second, Target.WORD, Change.Kind.CHANGE), "rationale").setValue("theirs");
        LanguageDiff.apply(again, second);
        assertEquals("theirs", word(again, "rota").getRationale());
    }

    @Test
    void aWordChangedHereAndDeletedThereIsAConflictThatIsNotPickedByDefault() {
        Language base = base();
        Language incoming = copy(base);
        incoming.deleteEntry(word(base, "rota").getId());
        Language current = copy(base);
        word(current, "rota").setRationale("I use this");

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change delete = change(set, Target.WORD, Change.Kind.DELETE);
        assertEquals(Conflict.CHANGED_HERE_DELETED_THERE, delete.conflict());
        assertFalse(delete.isPicked());
        LanguageDiff.apply(current, set);
        assertEquals(3, current.getLexicon().size(), "an unpicked change is not applied");
    }

    @Test
    void aWordDeletedHereAndChangedThereComesBackWhenPicked() {
        Language base = base();
        Language incoming = copy(base);
        word(incoming, "rota").setRationale("their improvement");
        Language current = copy(base);
        current.deleteEntry(word(base, "rota").getId());

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change change = change(set, Target.WORD, Change.Kind.CHANGE);
        assertEquals(Conflict.DELETED_HERE_CHANGED_THERE, change.conflict());
        assertFalse(change.isPicked());

        set.pick(change, true);
        LanguageDiff.apply(current, set);
        assertEquals("their improvement", word(current, "rota").getRationale());
    }

    @Test
    void aWordThatIsNewThereAndHereUnderAnotherIdentityIsFoundByTheWord() {
        Language base = base();
        Language incoming = copy(base);
        incoming.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));
        Language current = copy(base);
        current.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change add = change(set, Target.WORD, Change.Kind.ADD);
        assertEquals(Conflict.SAME_WORD_EXISTS, add.conflict());
        assertFalse(add.isPicked());
    }

    @Test
    void aHomophoneWithAnotherMeaningIsNotAConflict() {
        Language base = base();
        Language incoming = copy(base);
        incoming.addEntry(LexiconEntry.ofAuthor("drak", "to hammer"));
        ChangeSet set = LanguageDiff.diff(base, incoming, copy(base));
        assertNull(change(set, Target.WORD, Change.Kind.ADD).conflict());
    }

    // ---- senses and other lists

    @Test
    void sensesAreMergedAsASetBothSidesAddingDifferentOnes() {
        Language base = base();
        Language incoming = copy(base);
        Language current = copy(base);
        word(incoming, "rota").getSenses().add(new Sense("shift", "noun"));
        word(current, "rota").getSenses().add(new Sense("tour", "noun"));

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change change = change(set, Target.WORD, Change.Kind.CHANGE);
        assertFalse(change.hasConflict());
        LanguageDiff.apply(current, set);
        assertEquals(List.of("turn", "tour", "shift"), word(current, "rota").getSenses().stream().map(Sense::getGloss).toList());
    }

    @Test
    void aSenseTheOtherLanguageRemovedIsRemovedUnlessWeChangedIt() {
        Language base = base();
        word(base, "rota").getSenses().add(new Sense("shift"));
        Language incoming = copy(base);
        word(incoming, "rota").getSenses().removeIf(sense -> sense.getGloss().equals("shift"));
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        LanguageDiff.apply(current, set);
        assertEquals(1, word(current, "rota").getSenses().size());

        Language edited = copy(base);
        word(edited, "rota").getSenses().get(1).setPartOfSpeech("noun");
        ChangeSet conflicting = LanguageDiff.diff(base, incoming, edited);
        assertTrue(change(conflicting, Target.WORD, Change.Kind.CHANGE).hasConflict());
    }

    @Test
    void aSenseBothChangedDifferentlyIsAConflictAndOurSenseStays() {
        Language base = base();
        Language incoming = copy(base);
        Language current = copy(base);
        word(incoming, "rota").getSenses().getFirst().setPartOfSpeech("noun");
        word(current, "rota").getSenses().getFirst().setPartOfSpeech("verb");

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        assertTrue(field(change(set, Target.WORD, Change.Kind.CHANGE), "senses").isConflict());
        LanguageDiff.apply(current, set);
        assertEquals("verb", word(current, "rota").getSenses().getFirst().getPartOfSpeech());
    }

    @Test
    void theAliasesOfACharacterAreAListToo() {
        Language base = base();
        CharacterNote ron = new CharacterNote("Ron");
        base.getSpeakers().getCharacters().add(ron);
        Language incoming = copy(base);
        incoming.getSpeakers().getCharacters().getFirst().getAliases().add("Weasley");
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        LanguageDiff.apply(current, set);
        assertTrue(current.getSpeakers().getCharacters().getFirst().isCalled("Weasley"));
    }

    // ---- the order

    @Test
    void aMovedSectionIsAMoveToItsPlace() {
        Language base = base();
        Language incoming = copy(base);
        RuleSection names = incoming.getRules().remove(2);
        incoming.getRules().addFirst(names);
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change move = change(set, Target.RULE, Change.Kind.MOVE);
        assertEquals("Names", move.label());
        assertNull(move.anchor(), "first");
        LanguageDiff.apply(current, set);
        assertEquals(List.of("Names", "Grammar", "Sounds"), current.getRules().stream().map(RuleSection::getTitle).toList());
    }

    @Test
    void aSectionMovedOnBothSidesIsAConflict() {
        Language base = base();
        Language incoming = copy(base);
        incoming.getRules().addFirst(incoming.getRules().remove(2));
        Language current = copy(base);
        current.getRules().addFirst(current.getRules().remove(2));

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        assertEquals(Conflict.BOTH_MOVED, change(set, Target.RULE, Change.Kind.MOVE).conflict());
    }

    @Test
    void aNewSectionIsInsertedAfterTheOneBeforeIt() {
        Language base = base();
        Language incoming = copy(base);
        incoming.getRules().add(1, new RuleSection("Numbers", "Base twelve."));
        Language current = copy(base);

        LanguageDiff.apply(current, LanguageDiff.diff(base, incoming, current));
        assertEquals(List.of("Grammar", "Numbers", "Sounds", "Names"), current.getRules().stream().map(RuleSection::getTitle).toList());
    }

    // ---- dependencies, categories, the language itself

    @Test
    void aNewWordInANewCategoryDependsOnItAndPickingTakesItAlong() {
        Language base = base();
        Language incoming = copy(base);
        Category kin = new Category("Kinship");
        incoming.getCategories().add(kin);
        LexiconEntry mother = LexiconEntry.ofAuthor("ama", "mother");
        mother.setCategory(kin.getId());
        incoming.addEntry(mother);
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change add = set.changes().stream().filter(c -> c.target() == Target.WORD).findFirst().orElseThrow();
        assertTrue(add.dependsOn().contains("CATEGORY:" + kin.getId()));

        set.pickNone();
        set.pick(add, true);
        assertTrue(change(set, Target.CATEGORY, Change.Kind.ADD).isPicked(), "the category comes along");
        set.pick(change(set, Target.CATEGORY, Change.Kind.ADD), false);
        assertFalse(add.isPicked(), "and the word goes with it");

        // a word picked without its category is not applied
        set.pick(add, true);
        set.pick(change(set, Target.CATEGORY, Change.Kind.ADD), false);
        add.setPicked(true);
        LanguageDiff.Result result = LanguageDiff.apply(current, set);
        assertEquals(1, result.unmet().size());
        assertEquals(3, current.getLexicon().size());

        set.pickAll();
        LanguageDiff.apply(current, set);
        assertEquals("Kinship", current.findCategory(kin.getId()).getName());
        assertEquals(kin.getId(), word(current, "ama").getCategory());
    }

    @Test
    void aDeletedCategoryLeavesItsWordsWithoutOne() {
        Language base = base();
        String war = base.getCategories().get(1).getId();
        Language incoming = copy(base);
        incoming.deleteCategory(war);
        Language current = copy(base);

        LanguageDiff.apply(current, LanguageDiff.diff(base, incoming, current));
        assertNull(current.findCategory(war));
        assertNull(word(current, "drak").getCategory());
    }

    @Test
    void theNameTheDescriptionAndTheOptionsOfTheLanguageAreChangesOfTheLanguage() {
        Language base = base();
        Language incoming = copy(base);
        incoming.setDescription("A tongue of legions");
        incoming.setHyphenStructural(true);
        incoming.getSpeakers().setMode(SpeakersMode.ONLY);
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        Change change = change(set, Target.LANGUAGE, Change.Kind.CHANGE);
        assertEquals(3, change.fields().size());
        LanguageDiff.apply(current, set);
        assertEquals("A tongue of legions", current.getDescription());
        assertTrue(current.isHyphenStructural());
        assertEquals(SpeakersMode.ONLY, current.getSpeakers().getMode());
        assertEquals("Vrakht", current.getName());
    }

    // ---- applying: picks, edits, stale

    @Test
    void anUnpickedChangeIsNotApplied() {
        Language base = base();
        Language incoming = copy(base);
        incoming.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));
        word(incoming, "rota").setRationale("x");
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        set.pickNone();
        set.pick(change(set, Target.WORD, Change.Kind.CHANGE), true);
        LanguageDiff.apply(current, set);
        assertEquals(3, current.getLexicon().size());
        assertEquals("x", word(current, "rota").getRationale());
    }

    @Test
    void theUserCanEditAValueBeforeApplying() {
        Language base = base();
        Language incoming = copy(base);
        word(incoming, "rota").setRationale("borrowed");
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        field(change(set, Target.WORD, Change.Kind.CHANGE), "rationale").setValue("borrowed whole, no root fits");
        LanguageDiff.apply(current, set);
        assertEquals("borrowed whole, no root fits", word(current, "rota").getRationale());
    }

    @Test
    void aChangeIsSkippedWhenTheLanguageChangedAfterTheSetWasMade() {
        Language base = base();
        Language incoming = copy(base);
        word(incoming, "rota").setRationale("theirs");
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        word(current, "rota").setRationale("typed meanwhile");
        LanguageDiff.Result result = LanguageDiff.apply(current, set);

        assertEquals(1, result.stale().size());
        assertTrue(result.applied().isEmpty());
        assertEquals("typed meanwhile", word(current, "rota").getRationale());
    }

    @Test
    void anAddIsSkippedWhenTheWordAppearedMeanwhile() {
        Language base = base();
        Language incoming = copy(base);
        incoming.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));
        Language current = copy(base);

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        current.addEntry(copy(incoming).getLexicon().getLast());
        assertEquals(1, LanguageDiff.apply(current, set).stale().size());
        assertEquals(4, current.getLexicon().size());
    }

    // ---- two-way compare (a copy, an import)

    @Test
    void comparingACopyShowsWhatTheCopyDiffersInAndApplyingBringsItOver() {
        Language original = base();
        Language experiment = original.duplicate("Vrakht 2");
        word(experiment, "rota").setRationale("tried this");
        experiment.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));
        experiment.deleteEntry(word(experiment, "mela").getId());
        experiment.getRules().getFirst().setText("Mood particle last.");

        ChangeSet set = LanguageDiff.compare(original, experiment);
        assertEquals(4, set.changes().size(), "the word, the added word, the deleted word and the section (the name is not compared)");
        assertEquals(0, set.conflicts());

        LanguageDiff.apply(original, set);
        assertEquals("tried this", word(original, "rota").getRationale());
        assertNotNull(word(original, "vael"));
        assertEquals(List.of("drak", "rota", "vael"), original.getLexicon().stream().map(LexiconEntry::getWord).toList());
        assertEquals("Mood particle last.", original.getRules().getFirst().getText());
        assertEquals("Vrakht", original.getName(), "a comparison never renames");
        assertTrue(LanguageDiff.compare(original, experiment).isEmpty(), "after everything was taken over the two are the same");
    }

    @Test
    void anImportOfALanguageWithOtherIdentitiesAddsEverythingAndFlagsTheWordsThatAreHereAlready() {
        Language existing = base();
        Language foreign = new Language("Vrakht");
        foreign.addEntry(LexiconEntry.ofAuthor("drak", "war"));
        foreign.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));

        ChangeSet set = LanguageDiff.compare(existing, foreign);
        long adds = set.changes().stream().filter(c -> c.target() == Target.WORD && c.kind() == Change.Kind.ADD).count();
        assertEquals(2, adds);
        assertEquals(1, set.changes().stream().filter(c -> c.conflict() == Conflict.SAME_WORD_EXISTS).count());
        // everything that exists here and not there is a delete in a plain compare, which the user simply does not pick
        assertTrue(set.changes().stream().anyMatch(c -> c.kind() == Change.Kind.DELETE));
    }

    @Test
    void pickCleanSelectsTheChangesWithoutAConflict() {
        Language base = base();
        Language incoming = copy(base);
        incoming.deleteEntry(word(base, "rota").getId());
        word(incoming, "mela").setRationale("x");
        Language current = copy(base);
        word(current, "rota").setRationale("mine");

        ChangeSet set = LanguageDiff.diff(base, incoming, current);
        set.pickAll();
        set.pickClean();
        assertEquals(1, set.pickedCount());
        assertEquals(1, set.conflicts());
    }

    @Test
    void displayShowsTheNamesOfACategoryAndARegister() {
        Language base = base();
        Language incoming = copy(base);
        LexiconEntry vael = LexiconEntry.ofAuthor("vael", "mouth");
        vael.setCategory(base.getCategories().getFirst().getId());
        vael.setRegister(base.getRegisters().getFirst().getId());
        incoming.addEntry(vael);

        ChangeSet set = LanguageDiff.diff(base, incoming, copy(base));
        assertEquals("Things", set.display("category", base.getCategories().getFirst().getId()));
        assertEquals("formal", set.display("register", base.getRegisters().getFirst().getId()));
        assertEquals("", set.display("category", ""));
        assertEquals("turn|noun|", set.display("senses", "turn\tnoun\t"));
    }

    @Test
    void inAComparisonWithNoBaseSelectingTheCleanOnesNeverPicksADelete() {
        Language existing = base();
        Language foreign = copy(existing);
        foreign.deleteEntry(foreign.getLexicon().getFirst().getId());
        foreign.addEntry(LexiconEntry.ofAuthor("vael", "mouth"));

        ChangeSet compare = LanguageDiff.compare(existing, foreign);
        compare.pickAll();
        compare.pickClean();
        assertTrue(compare.changes().stream().filter(Change::isPicked).noneMatch(c -> c.kind() == Change.Kind.DELETE));
        assertEquals(1, compare.pickedCount(), "the new word");

        // a three-way set with a base: a delete the other language made is a wish
        Language base = base();
        Language incoming = copy(base);
        incoming.deleteEntry(incoming.getLexicon().getFirst().getId());
        ChangeSet three = LanguageDiff.diff(base, incoming, copy(base));
        three.pickNone();
        three.pickClean();
        assertEquals(1, three.pickedCount());
    }
}
