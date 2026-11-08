package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
