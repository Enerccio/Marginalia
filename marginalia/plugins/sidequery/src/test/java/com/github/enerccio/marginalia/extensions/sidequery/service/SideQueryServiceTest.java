package com.github.enerccio.marginalia.extensions.sidequery.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.extensions.sidequery.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The name the model gives a tab and when it is asked for one, and the settings and conversations as they are stored.
 */
class SideQueryServiceTest {

    private final SideQueryService service = new SideQueryService();

    private static SideQueryMessage message(boolean fromUser, String contents) {
        SideQueryMessage message = new SideQueryMessage();
        message.setFromUser(fromUser);
        message.setContents(contents);
        return message;
    }

    private static SideQuerySetting namingOn() {
        SideQuerySetting setting = new SideQuerySetting();
        setting.setEnableAiTabNames(true);
        return setting;
    }

    private static SideQuerySession answered() {
        SideQuerySession session = new SideQuerySession();
        session.getMessages().add(message(true, "Who is the villain?"));
        session.getMessages().add(message(false, "The duke."));
        return session;
    }

    @Test
    void theTabNameIsTheFirstLineWithoutQuotesAndMarkdown() {
        assertEquals("The Duke's Secret", SideQueryService.cleanTabName("\n\n  **\"The Duke's Secret\".**  \nSecond line"));
        assertEquals("Villains", SideQueryService.cleanTabName("# Villains:"));
        assertEquals("code", SideQueryService.cleanTabName("``code``"));
        assertEquals("", SideQueryService.cleanTabName(null));
        assertEquals("", SideQueryService.cleanTabName(" \n \"\" \n"));
    }

    @Test
    void aLongTabNameIsCutAtFortyCharacters() {
        String name = SideQueryService.cleanTabName("A very long title that the model wrote instead of five words");
        assertEquals(40, name.length());
        assertTrue(name.endsWith("..."), name);
    }

    @Test
    void theTabIsNamedOnceAfterTheFirstQuestionAndAnswer() {
        assertTrue(service.shouldNameTab(namingOn(), answered()));
        assertFalse(service.shouldNameTab(new SideQuerySetting(), answered()), "switched off in the profile");
        assertFalse(service.shouldNameTab(null, answered()));

        SideQuerySession renamed = answered();
        renamed.setManuallyRenamed(true);
        assertFalse(service.shouldNameTab(namingOn(), renamed));

        SideQuerySession named = answered();
        named.setAutoNamed(true);
        assertFalse(service.shouldNameTab(namingOn(), named));

        SideQuerySession unanswered = new SideQuerySession();
        unanswered.getMessages().add(message(true, "Who is the villain?"));
        unanswered.getMessages().add(message(false, "  "));
        assertFalse(service.shouldNameTab(namingOn(), unanswered));

        SideQuerySession noQuestion = new SideQuerySession();
        noQuestion.getMessages().add(message(false, "Hello."));
        assertFalse(service.shouldNameTab(namingOn(), noQuestion));
    }

    @Test
    void theDefaultSettingsHaveOneProfileThatIsSelected() {
        SideQuerySettings settings = service.createDefault();
        assertEquals("_Default", settings.getDefaultSetting());
        assertEquals(1, settings.getSettings().size());
        assertNotNull(settings.getSettings().get("_Default"));
        assertTrue(settings.getSavedQueries().isEmpty());
    }

    @Test
    void settingsAreStoredInTheUserSettingsAndReplaceTheOldOnes() {
        UserSetting userSetting = new UserSetting();
        SideQuerySettings first = service.createDefault();
        first.getSavedQueries().put("old", "old query");
        service.saveSettings(first, userSetting);

        SideQuerySettings second = service.createDefault();
        second.getSettings().get("_Default").setInitialQuery("What happened?");
        second.getSavedQueries().put("plot holes", "List the plot holes.");
        service.saveSettings(second, userSetting);

        SideQuerySettings read = new Gson().fromJson(userSetting.getAttributes().get(SideQuerySettings.KEY), SideQuerySettings.class);
        assertEquals("What happened?", read.getSettings().get("_Default").getInitialQuery());
        assertEquals(java.util.List.of("plot holes"), java.util.List.copyOf(read.getSavedQueries().keySet()));
    }

    @Test
    void settingsStoredWithoutSavedQueriesHaveNone() {
        SideQuerySettings read = new Gson().fromJson("{\"settings\": {}, \"defaultSetting\": \"_Default\", \"savedQueries\": null}",
                SideQuerySettings.class);
        assertNotNull(read.getSavedQueries());
        assertTrue(read.getSavedQueries().isEmpty());
    }

    @Test
    void theConversationsAreReadFromTheBook() {
        Manuscript manuscript = new Manuscript();
        SideQueryData fresh = service.getSideQueryData(manuscript);
        assertEquals(1, fresh.getSessions().size(), "nothing stored is one empty tab");
        assertEquals("Tab 1", fresh.getSessions().getFirst().getName());

        JsonObject attributes = new JsonObject();
        attributes.add(SideQuerySettings.KEY, JsonParser.parseString("""
                {"activeTab": 1, "sessions": [
                  {"name": "First", "messages": []},
                  {"name": "Villains", "autoNamed": true, "messages": [{"fromUser": true, "contents": "Who?"}],
                   "options": {"includeLorebook": true, "partsFrom": 2, "partsTo": 5, "manuscriptSources": null}}]}
                """));
        manuscript.setAttributes(attributes);
        SideQueryData data = service.getSideQueryData(manuscript);
        assertEquals(1, data.getActiveTab());
        assertEquals(2, data.getSessions().size());
        SideQuerySession villains = data.getSessions().get(1);
        assertTrue(villains.isAutoNamed());
        assertEquals("Who?", villains.getMessages().getFirst().getContents());
        assertTrue(villains.getOptions().isIncludeLorebook());
        assertEquals(5, villains.getOptions().getPartsTo());
        assertTrue(villains.getOptions().getManuscriptSources().isEmpty(), "a missing list reads as an empty one");
    }

    @Test
    void aSourceIsIdentifiedByItsBundleAndKey() {
        SideQueryService.Source source = new SideQueryService.Source("story-notes", "notes", "Story notes");
        assertEquals("story-notes::notes", source.id());
        assertEquals(SideQueryOptions.sourceId("story-notes", "notes"), source.id());
    }
}
