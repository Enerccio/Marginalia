package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * An AI of an extension: one column of its own (the extension id), the settings of the provider live in the
 * attributes under that id.
 */
class ExtensionAICrudTest extends ExtendableCrudContract<AI> {

    @Override
    protected OwnedService<AI, ?> service() {
        return aiService;
    }

    @Override
    protected AI newEntity() {
        ExtensionAI ai = new ExtensionAI();
        ai.setAiType(AIType.EXTENSION);
        ai.setExtensionId("test-provider");
        ai.setName("Extension model");
        ai.setMaxContext(4096);
        ai.setMaxCompletionTokens(256);
        ai.getSettings().addProperty("model", "model-a");
        ai.getSettings().addProperty("apiKey", inferenceServices.protectSecret("secret-key"));
        return ai;
    }

    @Override
    protected void assertCreated(AI loaded) {
        assertThat(loaded).isInstanceOf(ExtensionAI.class);
        ExtensionAI ai = (ExtensionAI) loaded;
        assertThat(ai.getAiType()).isEqualTo(AIType.EXTENSION);
        assertThat(ai.getExtensionId()).isEqualTo("test-provider");
        assertThat(ai.getName()).isEqualTo("Extension model");
        assertThat(ai.getMaxContext()).isEqualTo(4096);
        assertThat(ai.getMaxCompletionTokens()).isEqualTo(256);
        assertThat(ai.getSettings().get("model").getAsString()).isEqualTo("model-a");
        assertThat(inferenceServices.revealSecret(ai.getSettings().get("apiKey").getAsString())).isEqualTo("secret-key");
    }

    @Override
    protected void modify(AI entity) {
        ExtensionAI ai = (ExtensionAI) entity;
        ai.setName("Renamed");
        ai.getSettings().addProperty("model", "model-b");
        ai.getSettings().addProperty("temperatureScale", 2);
    }

    @Override
    protected void assertModified(AI loaded) {
        ExtensionAI ai = (ExtensionAI) loaded;
        assertThat(ai.getName()).isEqualTo("Renamed");
        assertThat(ai.getExtensionId()).isEqualTo("test-provider");
        assertThat(ai.getSettings().get("model").getAsString()).isEqualTo("model-b");
        assertThat(ai.getSettings().get("temperatureScale").getAsInt()).isEqualTo(2);
        assertThat(inferenceServices.revealSecret(ai.getSettings().get("apiKey").getAsString())).isEqualTo("secret-key");
    }

    @Test
    void secretIsNotStoredInPlainText() throws Exception {
        AI saved = create();

        String stored = new String(reload(saved).getExtendedContent(), java.nio.charset.StandardCharsets.UTF_8);

        assertThat(stored).contains("test-provider").doesNotContain("secret-key");
    }

    @Test
    void settingsOfTwoExtensionsDoNotMix() throws Exception {
        ExtensionAI ai = (ExtensionAI) newEntity();
        ai.getAttributes().add("other-provider", new com.google.gson.JsonObject());
        ExtensionAI saved = (ExtensionAI) aiService.save(ai);

        ExtensionAI loaded = (ExtensionAI) reload(saved);

        assertThat(loaded.getAttributes().keySet()).containsExactlyInAnyOrder("test-provider", "other-provider");
        assertThat(loaded.getSettings().keySet()).containsExactlyInAnyOrder("model", "apiKey");
    }

    @Test
    void settingsNeedTheExtensionId() {
        ExtensionAI ai = new ExtensionAI();

        assertThatThrownBy(ai::getSettings).isInstanceOf(IllegalStateException.class);
    }

    /** The contract expects empty attributes to start with, the settings of the provider are always in them. */
    @Override
    @Test
    void attributesCanBeChangedAndRemoved() throws Exception {
        ExtensionAI entity = (ExtensionAI) newEntity();
        entity.getAttributes().addProperty("keep", 1);
        entity.getAttributes().addProperty("change", "old");
        entity.getAttributes().addProperty("remove", "gone");
        AI saved = service().save(entity);

        ExtensionAI loaded = (ExtensionAI) reload(saved);
        loaded.getAttributes().addProperty("change", "new");
        loaded.getAttributes().remove("remove");
        loaded.getAttributes().addProperty("add", false);
        loaded.getSettings().addProperty("model", "model-c");
        service().save(loaded);

        ExtensionAI reloaded = (ExtensionAI) reload(saved);
        assertThat(reloaded.getAttributes().keySet()).containsExactlyInAnyOrder("keep", "change", "add", "test-provider");
        assertThat(reloaded.getAttributes().get("change").getAsString()).isEqualTo("new");
        assertThat(reloaded.getSettings().get("model").getAsString()).isEqualTo("model-c");
    }

    /** Like {@link AICrudTest}: only the owner saves an AI. */
    @Override
    @Test
    void savingKeepsExistingOwner() throws Exception {
        AI saved = create();
        login();

        AI loaded = reload(saved);
        modify(loaded);

        assertThatThrownBy(() -> service().save(loaded)).isInstanceOf(SecurityException.class);
        assertThat(reload(saved).getOwner().getId()).isEqualTo(owner.getId());
        assertThat(reload(saved).getName()).isEqualTo(saved.getName());
    }
}
