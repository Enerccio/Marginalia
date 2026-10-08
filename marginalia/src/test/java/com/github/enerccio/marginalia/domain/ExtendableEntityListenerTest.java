package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.test.ExpectedLog;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Serialization of {@code @ExtendedAttribute} fields and injected maps into {@code extendedContent}, without database.
 */
class ExtendableEntityListenerTest {

    private final ExtendableEntityListener listener = new ExtendableEntityListener();

    private static JsonObject stored(com.github.enerccio.marginalia.domain.model.ExtendableEntity entity) {
        return JsonParser.parseString(new String(entity.getExtendedContent(), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void setStored(com.github.enerccio.marginalia.domain.model.ExtendableEntity entity, String json) {
        entity.setExtendedContent(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void roundTripsAllSupportedTypes() throws Exception {
        Date request = new Date(1_700_000_000_123L);
        ChatMessage message = new ChatMessage();
        message.setInstructions("Write \"more\"\nwith ünïcödé ✓");
        message.setPromptTokens(123_456_789_012L);
        message.setScrollPosition(42);
        message.setRequest(request);

        LorebookEntry entry = new LorebookEntry();
        entry.setFilteringMode(FilteringMode.REGEX);
        entry.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);

        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setTemperature(0.75);
        protocol.setTemperatureEnabled(true);
        protocol.setTopPEnabled(false);

        listener.serialize(message);
        listener.serialize(entry);
        listener.serialize(protocol);

        ChatMessage loadedMessage = new ChatMessage();
        loadedMessage.setExtendedContent(message.getExtendedContent());
        listener.deserialize(loadedMessage);
        assertThat(loadedMessage.getInstructions()).isEqualTo("Write \"more\"\nwith ünïcödé ✓");
        assertThat(loadedMessage.getPromptTokens()).isEqualTo(123_456_789_012L);
        assertThat(loadedMessage.getScrollPosition()).isEqualTo(42);
        assertThat(loadedMessage.getRequest()).isEqualTo(request);

        LorebookEntry loadedEntry = new LorebookEntry();
        loadedEntry.setExtendedContent(entry.getExtendedContent());
        listener.deserialize(loadedEntry);
        assertThat(loadedEntry.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(loadedEntry.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);

        // fields declared on the abstract Protocol superclass
        ChatCompletionProtocol loadedProtocol = new ChatCompletionProtocol();
        loadedProtocol.setExtendedContent(protocol.getExtendedContent());
        listener.deserialize(loadedProtocol);
        assertThat(loadedProtocol.getTemperature()).isEqualTo(0.75);
        assertThat(loadedProtocol.getTemperatureEnabled()).isTrue();
        assertThat(loadedProtocol.getTopPEnabled()).isFalse();
        assertThat(loadedProtocol.getTopP()).isNull();
    }

    @Test
    void nullFieldsAreNotStored() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setPov("first person");

        listener.serialize(manuscript);

        JsonObject root = stored(manuscript);
        assertThat(root.get("pov").getAsString()).isEqualTo("first person");
        assertThat(root.has("tense")).isFalse();
        assertThat(root.has("backupStrategy")).isFalse();
    }

    @Test
    void enumsAreStoredByName() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setBackupStrategy(BackupStrategy.AFTER_N_MESSAGES);

        listener.serialize(manuscript);

        assertThat(stored(manuscript).get("backupStrategy").getAsString()).isEqualTo("AFTER_N_MESSAGES");
    }

    @Test
    void injectedMapIsStoredWithPrefix() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.getAttributes().addProperty("plugin.key", "value");

        listener.serialize(manuscript);

        JsonObject root = stored(manuscript);
        assertThat(root.get("attributes_plugin.key").getAsString()).isEqualTo("value");
        assertThat(root.has("attributes")).isFalse();
    }

    @Test
    void injectedMapKeepsJsonStructure() throws Exception {
        JsonObject nested = new JsonObject();
        nested.addProperty("depth", 2);
        JsonArray array = new JsonArray();
        array.add("a");
        array.add(1);
        array.add(true);

        Manuscript manuscript = new Manuscript();
        manuscript.getAttributes().add("nested", nested);
        manuscript.getAttributes().add("array", array);
        manuscript.getAttributes().addProperty("number", 1.5);
        manuscript.getAttributes().addProperty("flag", false);
        manuscript.getAttributes().add("nothing", JsonNull.INSTANCE);

        listener.serialize(manuscript);
        Manuscript loaded = new Manuscript();
        loaded.setExtendedContent(manuscript.getExtendedContent());
        listener.deserialize(loaded);

        assertThat(loaded.getAttributes()).isEqualTo(manuscript.getAttributes());
        assertThat(loaded.getAttributes().get("nothing").isJsonNull()).isTrue();
    }

    @Test
    void multipleInjectedMapsAreKeptApart() throws Exception {
        OpenAICompatible ai = new OpenAICompatible();
        ai.getAttributes().addProperty("shared", "from attributes");
        ai.getAdditionalParameters().addProperty("shared", "from parameters");
        ai.getAdditionalParameters().addProperty("top_k", 40);

        listener.serialize(ai);
        OpenAICompatible loaded = new OpenAICompatible();
        loaded.setExtendedContent(ai.getExtendedContent());
        listener.deserialize(loaded);

        assertThat(loaded.getAttributes().keySet()).containsExactly("shared");
        assertThat(loaded.getAttributes().get("shared").getAsString()).isEqualTo("from attributes");
        assertThat(loaded.getAdditionalParameters().keySet()).containsExactlyInAnyOrder("shared", "top_k");
        assertThat(loaded.getAdditionalParameters().get("shared").getAsString()).isEqualTo("from parameters");
        assertThat(loaded.getAdditionalParameters().get("top_k").getAsInt()).isEqualTo(40);
    }

    @Test
    void deserializeReplacesInjectedMap() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.getAttributes().addProperty("stale", true);
        setStored(manuscript, "{\"attributes_fresh\": 1}");

        listener.deserialize(manuscript);

        assertThat(manuscript.getAttributes().keySet()).containsExactly("fresh");
    }

    @Test
    void emptyContentLeavesEntityUntouched() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setPov("kept");
        manuscript.getAttributes().addProperty("kept", true);

        listener.deserialize(manuscript);
        setStored(manuscript, "");
        listener.deserialize(manuscript);

        assertThat(manuscript.getPov()).isEqualTo("kept");
        assertThat(manuscript.getAttributes().has("kept")).isTrue();
    }

    @Test
    void invalidValueIsSkippedNotThrown() throws Exception {
        ChatMessage message = new ChatMessage();
        setStored(message, "{\"promptTokens\": \"not a number\", \"backgroundLore\": \"ok\", \"unknownField\": 1}");

        try (ExpectedLog log = ExpectedLog.capture(ExtendableEntityListener.class)) {
            listener.deserialize(message);

            assertThat(log.errors()).singleElement().asString().contains("ChatMessage").contains("not a number");
        }

        assertThat(message.getPromptTokens()).isNull();
        assertThat(message.getBackgroundLore()).isEqualTo("ok");
    }

    @Test
    void invalidEnumIsSkipped() throws Exception {
        LorebookEntry entry = new LorebookEntry();
        setStored(entry, "{\"filteringMode\": \"NO_SUCH_MODE\"}");

        try (ExpectedLog log = ExpectedLog.capture(ExtendableEntityListener.class)) {
            listener.deserialize(entry);

            assertThat(log.errors()).singleElement().asString().contains("NO_SUCH_MODE");
        }

        assertThat(entry.getFilteringMode()).isEqualTo(FilteringMode.TEXT);
    }

    @Test
    void emptyStringValues() throws Exception {
        ChatMessage message = new ChatMessage();
        message.setInstructions("");

        listener.serialize(message);
        ChatMessage loaded = new ChatMessage();
        loaded.setExtendedContent(message.getExtendedContent());
        loaded.setPromptTokens(5L);
        setStored(loaded, "{\"instructions\": \"\", \"promptTokens\": \"\"}");
        listener.deserialize(loaded);

        assertThat(loaded.getInstructions()).isEmpty();
        // empty value for a non-string field is ignored
        assertThat(loaded.getPromptTokens()).isEqualTo(5L);
    }
}
