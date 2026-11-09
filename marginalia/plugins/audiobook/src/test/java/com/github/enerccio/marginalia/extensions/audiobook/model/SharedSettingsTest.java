package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SharedSettingsTest {

    private static final Gson GSON = new Gson();

    private static SharedBackend shared(String name, boolean allUsers, String... uuids) {
        SharedBackend backend = new SharedBackend();
        backend.getConfig().setName(name);
        backend.setAllUsers(allUsers);
        backend.setUserUuids(Set.of(uuids));
        return backend;
    }

    @Test
    void nothingIsSharedByDefaultAndUsersMayAddTheirOwn() {
        SharedSettings settings = GSON.fromJson("{}", SharedSettings.class);

        assertTrue(settings.getBackends().isEmpty());
        assertTrue(settings.isUsersMayAddOwn());
        assertTrue(settings.availableTo("u1").isEmpty());
    }

    @Test
    void aBackendForEveryoneIsAvailableToEveryUserEvenOneThatIsNotKnown() {
        SharedSettings settings = new SharedSettings();
        settings.getBackends().add(shared("GPU", true));

        assertEquals(1, settings.availableTo("u1").size());
        assertEquals(1, settings.availableTo(null).size());
    }

    @Test
    void aBackendForChosenUsersIsAvailableOnlyToThem() {
        SharedSettings settings = new SharedSettings();
        settings.getBackends().add(shared("Everyone", true));
        settings.getBackends().add(shared("Writers", false, "u1", "u2"));

        assertEquals(List.of("Everyone", "Writers"), settings.availableTo("u1").stream().map(BackendConfig::getName).toList());
        assertEquals(List.of("Everyone"), settings.availableTo("u3").stream().map(BackendConfig::getName).toList());
        assertEquals(List.of("Everyone"), settings.availableTo(null).stream().map(BackendConfig::getName).toList());
    }

    @Test
    void findLooksAtAllSharedBackendsWhoeverMayUseThem() {
        SharedSettings settings = new SharedSettings();
        SharedBackend writers = shared("Writers", false, "u1");
        settings.getBackends().add(writers);

        assertSame(writers, settings.find(writers.getConfig().getId()));
        assertNull(settings.find("another"));
        assertNull(settings.find(null));
    }

    @Test
    void theSettingsSurviveJson() {
        SharedSettings settings = new SharedSettings();
        SharedBackend writers = shared("Writers", false, "u1", "u2");
        writers.getConfig().setBaseUrl("http://gpu:8004");
        writers.getConfig().setProtectedApiKey("secret-protected");
        settings.getBackends().add(writers);
        settings.setUsersMayAddOwn(false);

        SharedSettings back = GSON.fromJson(GSON.toJson(settings), SharedSettings.class);

        assertFalse(back.isUsersMayAddOwn());
        SharedBackend read = back.getBackends().getFirst();
        assertFalse(read.isAllUsers());
        assertEquals(Set.of("u1", "u2"), read.getUserUuids());
        assertEquals(writers.getConfig().getId(), read.getConfig().getId());
        assertEquals("http://gpu:8004", read.getConfig().getBaseUrl());
        assertEquals("secret-protected", read.getConfig().getProtectedApiKey());
        assertTrue(read.allows("u2"));
        assertFalse(read.allows("u3"));
    }
}
