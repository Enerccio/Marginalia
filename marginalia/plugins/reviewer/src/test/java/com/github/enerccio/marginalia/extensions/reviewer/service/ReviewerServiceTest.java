package com.github.enerccio.marginalia.extensions.reviewer.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewData;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerProfile;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSetting;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The reviewer profiles: which one a book uses, how they are stored, and the reviews kept on a story part.
 */
class ReviewerServiceTest {

    private static final String PROFILE_KEY = ReviewerSettings.KEY + ".profile";

    private final ReviewerService service = new ReviewerService();

    private static ReviewerSettings settings() {
        ReviewerSettings settings = new ReviewerSettings();
        ReviewerSetting harsh = new ReviewerSetting();
        harsh.setUuid("harsh-uuid");
        harsh.setReviewPrompt("Be harsh.");
        ReviewerSetting kind = new ReviewerSetting();
        kind.setUuid("kind-uuid");
        kind.setReviewPrompt("Be kind.");
        settings.getSettings().put("Kind", kind);
        settings.getSettings().put("Harsh", harsh);
        settings.setDefaultSetting("Kind");
        return settings;
    }

    private static Manuscript book(String profileUuid) {
        Manuscript manuscript = new Manuscript();
        JsonObject attributes = new JsonObject();
        if (profileUuid != null) {
            attributes.addProperty(PROFILE_KEY, profileUuid);
        }
        manuscript.setAttributes(attributes);
        return manuscript;
    }

    @Test
    void aBookUsesItsOwnProfileWhileItExistsElseTheSelectedOne() {
        ReviewerSettings settings = settings();
        assertEquals("Be harsh.", service.resolveSetting(settings, book("harsh-uuid")).getReviewPrompt());
        assertEquals("Be kind.", service.resolveSetting(settings, book(null)).getReviewPrompt());
        assertEquals("Be kind.", service.resolveSetting(settings, book("deleted-uuid")).getReviewPrompt());
        // the profile is found by uuid, so renaming it keeps the choice of the book
        ReviewerSetting harsh = settings.getSettings().remove("Harsh");
        settings.getSettings().put("Very harsh", harsh);
        assertEquals("Be harsh.", service.resolveSetting(settings, book("harsh-uuid")).getReviewPrompt());
    }

    @Test
    void theProfileOfABook() {
        assertEquals("harsh-uuid", service.getManuscriptProfile(book("harsh-uuid")));
        assertNull(service.getManuscriptProfile(book(null)));
        assertNull(service.getManuscriptProfile(new Manuscript()));
        Manuscript cleared = book(null);
        cleared.getAttributes().add(PROFILE_KEY, com.google.gson.JsonNull.INSTANCE);
        assertNull(service.getManuscriptProfile(cleared));
    }

    @Test
    void profilesAreListedByName() {
        assertEquals(List.of(new ReviewerProfile("harsh-uuid", "Harsh"), new ReviewerProfile("kind-uuid", "Kind")),
                service.getProfiles(settings()));
    }

    @Test
    void theDefaultSettingsHaveOneSelectedProfile() {
        ReviewerSettings settings = service.createDefault();
        assertEquals("_Default", settings.getDefaultSetting());
        ReviewerSetting profile = settings.getSettings().get("_Default");
        assertNotNull(profile.getUuid());
        assertFalse(profile.getReviewPrompt().isBlank());
    }

    @Test
    void settingsAreStoredInTheUserSettings() {
        UserSetting userSetting = new UserSetting();
        service.saveSettings(service.createDefault(), userSetting);
        service.saveSettings(settings(), userSetting);
        ReviewerSettings read = new Gson().fromJson(userSetting.getAttributes().get(ReviewerSettings.KEY), ReviewerSettings.class);
        assertEquals(List.of("Harsh", "Kind"), List.copyOf(read.getSettings().keySet()));
        assertEquals("harsh-uuid", read.getSettings().get("Harsh").getUuid());
    }

    @Test
    void profilesStoredBeforeTheyHadAUuidGetDistinctOnes() {
        ReviewerSettings read = new Gson().fromJson("""
                {"settings": {"A": {"reviewPrompt": "a"}, "B": {"reviewPrompt": "b"}}, "defaultSetting": "A"}
                """, ReviewerSettings.class);
        String a = read.getSettings().get("A").getUuid();
        String b = read.getSettings().get("B").getUuid();
        assertNotNull(a);
        assertNotNull(b);
        assertNotEquals(a, b);
    }

    @Test
    void theReviewsOfAPartAreReadFromItsAttributes() {
        ChatMessage part = new ChatMessage();
        assertNull(service.getReviewData(part));
        JsonObject attributes = new JsonObject();
        attributes.add(ReviewerSettings.KEY, JsonParser.parseString("""
                {"current": 1, "reviews": [{"text": "first"}, {"text": "second", "metadata": {"reasoning": "hm", "reasoningDone": true}}]}
                """));
        part.setAttributes(attributes);
        ReviewData data = service.getReviewData(part);
        assertEquals(1, data.getCurrent());
        assertEquals(2, data.getReviews().size());
        assertEquals("second", data.getReviews().get(1).getText());
        assertTrue(data.getReviews().get(1).getMetadata().isReasoningDone());
        assertEquals("", data.getReviews().get(0).getPrevious());
    }
}
