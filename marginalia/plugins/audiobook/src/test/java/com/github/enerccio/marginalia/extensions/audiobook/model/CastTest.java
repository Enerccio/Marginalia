package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.audiobook.label.Age;
import com.github.enerccio.marginalia.extensions.audiobook.label.Gender;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceShift;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CastTest {

    private static final VoiceRef BACKEND = VoiceRef.predefined("backend.wav");

    private final Map<String, Actor> actors = new HashMap<>();

    private Actor actor(String id, String voice) {
        Actor actor = new Actor();
        actor.setId(id);
        actor.setName(id);
        actor.setVoice(voice);
        actors.put(id, actor);
        return actor;
    }

    private static CastMember member(String name, String aliases, String actorId) {
        CastMember member = new CastMember();
        member.setName(name);
        member.setAliases(aliases);
        member.setActorId(actorId);
        return member;
    }

    private Cast cast() {
        actor("narrator", "clip:narrator").getVariants().put(Role.languageKey("ja"), "clip:narrator-ja");
        Actor marta = actor("marta", "clip:marta");
        marta.getVariants().put(Role.moodKey("whisper"), "clip:marta-whisper");
        marta.getVariants().put(Role.languageKey("ja"), "clip:marta-ja");
        actor("tomas", "clip:tomas");
        actor("tomas2", "clip:tomas2");
        actor("stranger", "predefined:Stranger.wav");
        Cast cast = new Cast();
        cast.setNarratorActorId("narrator");
        cast.setFallbackActorId("stranger");
        cast.getMembers().add(member("Marta Kovar", "Mrs Kovar, Marta", "marta"));
        cast.getMembers().add(member("Tomáš Novák", "", "tomas"));
        cast.getMembers().add(member("Tomáš Kovar", "", "tomas2"));
        return cast;
    }

    private String voice(Cast cast, String speaker, String mood, String language) {
        return cast.voice(speaker, mood, language, actors::get, BACKEND).toString();
    }

    @Test
    void theNarratorReadsNarrationAndAnUnknownSpeakerGetsTheFallback() {
        Cast cast = cast();

        assertEquals("clip:narrator", voice(cast, "Narrator", "NEUTRAL", null));
        assertEquals("clip:narrator", voice(cast, "narrator", "CALM", null), "case does not matter");
        assertEquals("predefined:Stranger.wav", voice(cast, "A guard", "NEUTRAL", null));
    }

    @Test
    void withoutAFallbackAnUnknownSpeakerIsTheNarratorAndWithoutACastTheBackend() {
        Cast cast = cast();
        cast.setFallbackActorId(null);
        assertEquals("clip:narrator", voice(cast, "A guard", "NEUTRAL", null));

        assertEquals(BACKEND.toString(), voice(new Cast(), "Marta", "NEUTRAL", null));
        assertEquals(BACKEND.toString(), voice(new Cast(), "Narrator", "NEUTRAL", null));
    }

    @Test
    void aCharacterIsFoundByNameAliasOrAUniqueWordOfItsNameIgnoringCaseAndAccents() {
        Cast cast = cast();

        assertEquals("clip:marta", voice(cast, "Marta Kovar", "NEUTRAL", null));
        assertEquals("clip:marta", voice(cast, "mrs kovar", "NEUTRAL", null), "an alias");
        assertEquals("clip:marta", voice(cast, "Marta", "NEUTRAL", null), "a word of the name, also an alias");
        assertEquals("clip:tomas", voice(cast, "Tomas Novak", "NEUTRAL", null), "accents");
        assertEquals("predefined:Stranger.wav", voice(cast, "Tomáš", "NEUTRAL", null), "two Tomáš: not guessed");
        assertEquals("clip:tomas", voice(cast, "Novák", "NEUTRAL", null), "a word of exactly one name");
    }

    @Test
    void aMoodAndALanguageHaveTheirOwnVoiceAndTheLanguageWins() {
        Cast cast = cast();

        assertEquals("clip:marta-whisper", voice(cast, "Marta", "WHISPER", null));
        assertEquals("clip:marta", voice(cast, "Marta", "SHOUT", null), "no clip for the mood: the voice");
        assertEquals("clip:marta-ja", voice(cast, "Marta", "WHISPER", "ja"), "language before mood");
        assertEquals("clip:narrator-ja", voice(cast, "Narrator", "NEUTRAL", "ja"));
        assertEquals("clip:tomas", voice(cast, "Tomáš Novák", "NEUTRAL", "ja"), "no Japanese clip: the voice");
    }

    @Test
    void aCharacterWithoutAnActorOrWithOneThatIsGoneIsReadByTheFallbackAndABrokenVoiceIsSkipped() {
        Cast cast = cast();
        cast.getMembers().add(member("Ghost", "", null));
        cast.getMembers().add(member("Gone", "", "deleted-actor"));
        actor("broken", "not a voice");
        cast.getMembers().add(member("Broken", "", "broken"));

        assertEquals("predefined:Stranger.wav", voice(cast, "Ghost", "NEUTRAL", null));
        assertEquals("predefined:Stranger.wav", voice(cast, "Gone", "NEUTRAL", null));
        assertEquals("predefined:Stranger.wav", voice(cast, "Broken", "NEUTRAL", null));
    }

    /** The cast with base voices for unnamed speakers: men, women, and (when {@code children}) a voice of its own for children. */
    private Cast withBases(boolean children) {
        Cast cast = cast();
        actor("men", "clip:men");
        actor("women", "clip:women");
        cast.setMenActorId("men");
        cast.setWomenActorId("women");
        if (children) {
            actor("kids", "clip:kids");
            cast.setChildrenActorId("kids");
        }
        return cast;
    }

    private VoiceRef extra(Cast cast, String speaker, Gender gender, Age age, boolean shiftable) {
        return cast.voice(speaker, "NEUTRAL", null, gender, age, "book-1", actors::get, BACKEND, v -> shiftable);
    }

    @Test
    void withoutBasesNothingChangesForUnnamedSpeakers() {
        Cast cast = cast();
        assertFalse(cast.usesExtras());
        assertEquals("predefined:Stranger.wav", extra(cast, "The guard", Gender.MALE, Age.ADULT, true).toString());
    }

    @Test
    void anUnnamedSpeakerGetsTheBaseOfTheirGenderShiftedIntoAPersonOfTheirOwn() {
        Cast cast = withBases(false);
        assertTrue(cast.usesExtras());

        VoiceRef guard = extra(cast, "The guard", Gender.MALE, Age.ADULT, true);
        assertEquals("men", guard.id());
        assertEquals(VoiceShift.pick("book-1", "The guard", 0, Gender.MALE, Age.ADULT, false), guard.shift());
        assertEquals("women", extra(cast, "The innkeeper", Gender.FEMALE, Age.ADULT, true).id());
        // a teenage boy is the men's clip, a teenage girl the women's
        assertEquals("men", extra(cast, "The squire", Gender.MALE, Age.TEEN, true).id());
        assertEquals("women", extra(cast, "The maid", Gender.FEMALE, Age.TEEN, true).id());
        // no gender: the fallback, also varied (it is a clip only when it is one)
        assertEquals("predefined:Stranger.wav", extra(cast, "Someone", Gender.UNKNOWN, Age.ADULT, true).toString());
        // named characters and the narrator are never shifted
        assertEquals("clip:marta", extra(cast, "Marta", Gender.FEMALE, Age.ADULT, true).toString());
        assertEquals("clip:narrator", extra(cast, "Narrator", Gender.MALE, Age.ADULT, true).toString());
    }

    @Test
    void aChildIsTheChildrensVoiceElseTheWomensRaised() {
        VoiceRef fromWomen = extra(withBases(false), "The girl", Gender.FEMALE, Age.CHILD, true);
        assertEquals("women", fromWomen.id());
        assertTrue(fromWomen.shift().pitch() >= 3, fromWomen.toString());

        VoiceRef own = extra(withBases(true), "The girl", Gender.FEMALE, Age.CHILD, true);
        assertEquals("kids", own.id());
        assertTrue(own.shift() == null || own.shift().pitch() <= 1, own.toString());
        // a boy too: children's voices are the women's when the cast has none
        assertEquals("women", extra(withBases(false), "The boy", Gender.MALE, Age.CHILD, true).id());
    }

    @Test
    void whenTheShiftedClipCantBeHadTheBaseClipSpeaks() {
        VoiceRef guard = extra(withBases(false), "The guard", Gender.MALE, Age.ADULT, false);
        assertEquals("clip:men", guard.toString());
    }

    @Test
    void theNarratorStandingInForAnUnnamedSpeakerIsNotShifted() {
        Cast cast = withBases(false);
        cast.setMenActorId(null);
        cast.setFallbackActorId(null);
        assertEquals("clip:narrator", extra(cast, "The guard", Gender.MALE, Age.ADULT, true).toString());
    }

    @Test
    void theFirstGenderAndAgeOfAnUnnamedSpeakerStickAndAnUnknownOneIsFilledInLater() {
        Cast cast = withBases(false);
        assertTrue(cast.learnExtra("The guard", Gender.UNKNOWN, Age.ADULT));
        assertTrue(cast.learnExtra("the guard", Gender.MALE, Age.ELDERLY), "the unknown gender is filled in");
        assertFalse(cast.learnExtra("THE GUARD", Gender.FEMALE, Age.CHILD), "known values do not flip");
        assertEquals(1, cast.getExtras().size());
        CastExtra guard = cast.findExtra("The Guard");
        assertEquals(Gender.MALE, guard.getGender());
        assertEquals(Age.ADULT, guard.getAge());
        // a later label that says otherwise does not change the voice
        assertEquals("men", extra(cast, "The guard", Gender.FEMALE, Age.CHILD, true).id());
        // characters and the narrator are not remembered as unnamed speakers
        assertFalse(cast.learnExtra("Marta", Gender.FEMALE, Age.ADULT));
        assertFalse(cast.learnExtra("Narrator", Gender.MALE, Age.ADULT));
    }

    @Test
    void anotherVariationIsAnotherShiftOfTheSameBase() {
        Cast cast = withBases(false);
        cast.learnExtra("The guard", Gender.MALE, Age.ADULT);
        VoiceRef before = extra(cast, "The guard", null, null, true);
        int differs = 0;
        for (int i = 1; i <= 5; i++) {
            cast.findExtra("The guard").setReroll(i);
            VoiceRef after = extra(cast, "The guard", null, null, true);
            assertEquals("men", after.id());
            differs += after.equals(before) ? 0 : 1;
        }
        assertTrue(differs >= 4, "rerolls that changed the voice: " + differs);
    }

    @Test
    void theBasesAndTheUnnamedSpeakersSurviveTheJsonOfTheBookAndAnOldCastHasNone() {
        Gson gson = new Gson();
        Cast cast = withBases(true);
        cast.learnExtra("The guard", Gender.MALE, Age.ELDERLY);
        Cast back = gson.fromJson(gson.toJson(cast), Cast.class);
        assertEquals("men", back.getMenActorId());
        assertEquals("kids", back.getChildrenActorId());
        assertEquals(Age.ELDERLY, back.findExtra("the guard").getAge());

        Cast old = gson.fromJson("{\"narratorActorId\":\"narrator\"}", Cast.class);
        assertFalse(old.usesExtras());
        assertTrue(old.getExtras().isEmpty());
    }

    @Test
    void theNamesForTheLabellingSkipBlankOnes() {
        Cast cast = cast();
        cast.getMembers().add(member("  ", "", "marta"));

        assertEquals(java.util.List.of("Marta Kovar", "Tomáš Novák", "Tomáš Kovar"), cast.names());
    }

    @Test
    void theActorsSurviveTheJsonOfTheSettingsAndTheCastTheJsonOfTheBook() {
        Gson gson = new Gson();
        AudiobookSettings settings = new AudiobookSettings();
        Cast cast = cast();
        actors.values().forEach(settings::putActor);

        AudiobookSettings back = gson.fromJson(gson.toJson(settings), AudiobookSettings.class);
        Cast backCast = gson.fromJson(gson.toJson(cast), Cast.class);

        assertEquals("clip:marta-whisper", backCast.voice("Marta", "WHISPER", null, back::findActor, BACKEND).toString());
        assertEquals(3, backCast.getMembers().size());
        // an old setting has no actors at all
        assertTrue(gson.fromJson("{}", AudiobookSettings.class).getActors().isEmpty());
    }

    @Test
    void putActorReplacesTheOneWithTheSameId() {
        AudiobookSettings settings = new AudiobookSettings();
        Actor one = new Actor();
        one.setName("a");
        settings.putActor(one);
        Actor changed = new Actor();
        changed.setId(one.getId());
        changed.setName("b");
        settings.putActor(changed);

        assertEquals(1, settings.getActors().size());
        assertEquals("b", settings.findActor(one.getId()).getName());
    }
}
