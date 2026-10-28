package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Duplicating a lorebook, a protocol and an inference provider: the settings are copied (the API key and the
 * attributes of extensions too), the original is left as it was and nobody can clone what is not theirs.
 */
class CloneSettingsTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private User user;

    @BeforeEach
    void logIn() throws Exception {
        user = login();
    }

    // lorebook

    private Lorebook lorebookWithEntries() throws Exception {
        Lorebook book = new Lorebook();
        book.setName(uniqueName("Places"));
        book.setEnabled(false);
        book.getAttributes().addProperty("myplugin", "data");
        book = lorebookService.save(book);
        tagRelationService.createRelation(tagService.getOrCreateForUser("setting"), book);

        LorebookEntry harbour = new LorebookEntry();
        harbour.setLorebook(book);
        harbour.setName("Harbour");
        harbour.setPayload("A grey harbour");
        harbour.setComment("note");
        harbour.setOrder(7);
        harbour.setFiltering("harbour|port");
        harbour.setFilteringMode(FilteringMode.REGEX);
        harbour.setInsertionMode(InsertionMode.IN_LORE_BLOCK);
        harbour.getAttributes().addProperty("myplugin", "entry");
        harbour = lorebookEntryService.save(harbour);
        tagRelationService.createRelation(tagService.getOrCreateForUser("water"), harbour, false);
        tagRelationService.createRelation(tagService.getOrCreateForUser("inland"), harbour, true);

        LorebookEntry inn = new LorebookEntry();
        inn.setLorebook(book);
        inn.setName("Inn");
        inn.setPayload("A warm inn");
        inn.setEnabled(false);
        lorebookEntryService.save(inn);
        return lorebookService.find(book);
    }

    @Test
    void clonedLorebookHasTheEntriesTagsAndAttributes() throws Exception {
        Lorebook original = lorebookWithEntries();

        Lorebook clone = lorebookService.cloneLorebook(original, "Copy of places");

        assertThat(clone.getId()).isNotEqualTo(original.getId());
        assertThat(clone.getUuid()).isNotEqualTo(original.getUuid());
        assertThat(clone.getName()).isEqualTo("Copy of places");
        assertThat(clone.isEnabled()).isFalse();
        assertThat(clone.isAgentic()).isFalse();
        assertThat(clone.getAttributes().get("myplugin").getAsString()).isEqualTo("data");
        assertThat(tagRelationService.getTagsForObject(clone)).extracting(Tag::getValue).containsExactly("setting");

        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(clone);
        assertThat(entries).extracting(LorebookEntry::getName).containsExactlyInAnyOrder("Harbour", "Inn");
        LorebookEntry harbour = entries.stream().filter(e -> e.getName().equals("Harbour")).findFirst().orElseThrow();
        assertThat(harbour.getPayload()).isEqualTo("A grey harbour");
        assertThat(harbour.getComment()).isEqualTo("note");
        assertThat(harbour.getOrder()).isEqualTo(7);
        assertThat(harbour.getFiltering()).isEqualTo("harbour|port");
        assertThat(harbour.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(harbour.getAttributes().get("myplugin").getAsString()).isEqualTo("entry");
        assertThat(tagRelationService.getTagsForObject(harbour)).extracting(Tag::getValue).containsExactly("water");
        assertThat(tagRelationService.getTagsForObject(harbour, true)).extracting(Tag::getValue).containsExactly("inland");
        assertThat(entries.stream().filter(e -> e.getName().equals("Inn")).findFirst().orElseThrow().isEnabled()).isFalse();
        assertThat(entries).extracting(LorebookEntry::getUuid)
                .doesNotContainAnyElementsOf(lorebookEntryService.getEntriesForLorebook(original).stream().map(LorebookEntry::getUuid).toList());
    }

    @Test
    void clonedLorebookLeavesTheOriginalAlone() throws Exception {
        Lorebook original = lorebookWithEntries();

        lorebookService.cloneLorebook(original, null);

        assertThat(lorebookEntryService.getEntriesForLorebook(original)).hasSize(2);
        assertThat(tagRelationService.getTagsForObject(original)).hasSize(1);
        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getName)
                .as("a blank name keeps the original name").filteredOn(original.getName()::equals).hasSize(2);
    }

    @Test
    void clonedLorebookIsLinkedToTheSameSubbooks() throws Exception {
        Lorebook original = lorebookWithEntries();
        Lorebook sub = lorebookService.createSubbook(original, "Sub");

        Lorebook clone = lorebookService.cloneLorebook(original, "Clone");

        assertThat(lorebookService.getSubbooks(clone)).extracting(Lorebook::getId).containsExactly(sub.getId());
        assertThat(lorebookEntryService.getEntriesForLorebook(sub)).isEmpty();
    }

    @Test
    void lorebookOfSomebodyElseCanNotBeCloned() throws Exception {
        Lorebook original = lorebookWithEntries();
        loginAs(createUser());

        assertThatThrownBy(() -> lorebookService.cloneLorebook(original, "Mine"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // protocol

    @Test
    void clonedProtocolHasAllTheSettings() throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setName(uniqueName("Precise"));
        protocol.setMaxTokens(8000);
        protocol.setReplyTokens(700);
        protocol.setTemperatureEnabled(true);
        protocol.setTemperature(0.4);
        protocol.setTopPEnabled(true);
        protocol.setTopP(0.9);
        protocol.setFrequencyPenaltyEnabled(true);
        protocol.setFrequencyPenalty(0.3);
        protocol.setPresencePenaltyEnabled(false);
        protocol.setPresencePenalty(0.1);
        protocol.getAttributes().addProperty("myplugin", "p");
        Protocol original = protocolService.save(protocol);

        Protocol clone = protocolService.cloneProtocol(original, "Precise 2");

        assertThat(clone.getId()).isNotEqualTo(original.getId());
        assertThat(clone.getUuid()).isNotEqualTo(original.getUuid());
        assertThat(clone).isInstanceOf(ChatCompletionProtocol.class);
        assertThat(clone.getName()).isEqualTo("Precise 2");
        assertThat(clone.getProtocolType()).isEqualTo(ProtocolType.CHAT_COMPLETION);
        assertThat(clone.getMaxTokens()).isEqualTo(8000);
        assertThat(clone.getReplyTokens()).isEqualTo(700);
        assertThat(clone.getTemperatureEnabled()).isTrue();
        assertThat(clone.getTemperature()).isEqualTo(0.4);
        assertThat(clone.getTopPEnabled()).isTrue();
        assertThat(clone.getTopP()).isEqualTo(0.9);
        assertThat(clone.getFrequencyPenaltyEnabled()).isTrue();
        assertThat(clone.getFrequencyPenalty()).isEqualTo(0.3);
        assertThat(clone.getPresencePenaltyEnabled()).isFalse();
        assertThat(clone.getPresencePenalty()).isEqualTo(0.1);
        assertThat(clone.getAttributes().get("myplugin").getAsString()).isEqualTo("p");

        // what is stored, not just what the service returned
        Protocol stored = protocolService.findForUser(clone.getUuid());
        assertThat(stored.getTemperature()).isEqualTo(0.4);
        assertThat(stored.getAttributes().get("myplugin").getAsString()).isEqualTo("p");
        assertThat(protocolService.cloneProtocol(original, " ").getName()).isEqualTo(original.getName());
    }

    @Test
    void protocolOfSomebodyElseCanNotBeCloned() throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setName(uniqueName("Mine"));
        Protocol original = protocolService.save(protocol);
        loginAs(createUser());

        assertThatThrownBy(() -> protocolService.cloneProtocol(original, "Stolen"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // inference provider

    @Test
    void clonedProviderHasTheApiKeyAndTheSettings() throws Exception {
        OpenAICompatible ai = createAI();
        ai.setApiKey("secret-key");
        ai.setJailbreak("jb");
        ai.setNeedsJailbreak(true);
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.HIGH);
        ai.setMaxConcurrentRequests(2);
        ai.setRequestTimeoutSeconds(90);
        ai.setMaxRetries(4);
        JsonObject extra = new JsonObject();
        extra.addProperty("top_k", 40);
        ai.setAdditionalParameters(extra);
        ai.getAttributes().addProperty("myplugin", "ai");
        OpenAICompatible original = (OpenAICompatible) aiService.save(ai);

        OpenAICompatible clone = (OpenAICompatible) aiService.cloneAI(original, "Provider 2");

        assertThat(clone.getId()).isNotEqualTo(original.getId());
        assertThat(clone.getUuid()).isNotEqualTo(original.getUuid());
        assertThat(clone.getName()).isEqualTo("Provider 2");
        assertThat(clone.getAiType()).isEqualTo(AIType.OPEN_AI_COMPATIBLE);
        assertThat(clone.getUri()).isEqualTo(original.getUri());
        assertThat(clone.getModel()).isEqualTo(original.getModel());
        assertThat(clone.getModelName()).isEqualTo(original.getModelName());
        assertThat(clone.getJailbreak()).isEqualTo("jb");
        assertThat(clone.getNeedsJailbreak()).isTrue();
        assertThat(clone.getEnabledReasoning()).isTrue();
        assertThat(clone.getReasoningEffort()).isEqualTo(ReasoningEffort.HIGH);
        assertThat(clone.getMaxContext()).isEqualTo(original.getMaxContext());
        assertThat(clone.getMaxCompletionTokens()).isEqualTo(original.getMaxCompletionTokens());
        assertThat(clone.getMaxConcurrentRequests()).isEqualTo(2);
        assertThat(clone.getRequestTimeoutSeconds()).isEqualTo(90);
        assertThat(clone.getMaxRetries()).isEqualTo(4);
        assertThat(clone.isShared()).isFalse();
        assertThat(clone.getLimits()).isNull();

        // what is stored (the key is encrypted in the column), and the copy of the parameters is not the same object
        OpenAICompatible stored = (OpenAICompatible) aiService.findForUser(clone.getUuid());
        assertThat(stored.getApiKey()).isEqualTo("secret-key");
        assertThat(stored.getAdditionalParameters().get("top_k").getAsInt()).isEqualTo(40);
        assertThat(stored.getAttributes().get("myplugin").getAsString()).isEqualTo("ai");
        assertThat(stored.getAdditionalParameters()).isNotSameAs(original.getAdditionalParameters());
    }

    @Test
    void clonedExtensionProviderKeepsItsSettings() throws Exception {
        ExtensionAI ai = new ExtensionAI();
        ai.setAiType(AIType.EXTENSION);
        ai.setName(uniqueName("ext"));
        ai.setExtensionId("test-extension");
        ai.getSettings().addProperty("model", "m1");
        ai.getSettings().addProperty("apiKey", "protected-secret");
        ExtensionAI original = (ExtensionAI) aiService.save(ai);

        ExtensionAI clone = (ExtensionAI) aiService.cloneAI(original, "Ext 2");

        assertThat(clone.getUuid()).isNotEqualTo(original.getUuid());
        assertThat(clone.getExtensionId()).isEqualTo("test-extension");
        ExtensionAI stored = (ExtensionAI) aiService.findForUser(clone.getUuid());
        assertThat(stored.getSettings().get("model").getAsString()).isEqualTo("m1");
        assertThat(stored.getSettings().get("apiKey").getAsString()).isEqualTo("protected-secret");
        assertThat(stored.getSettings()).isNotSameAs(original.getSettings());
    }

    @Test
    void sharedProviderOfSomebodyElseCanNotBeCloned() throws Exception {
        loginAdmin();
        OpenAICompatible shared = createAI();
        shared.setShared(true);
        OpenAICompatible original = (OpenAICompatible) aiService.save(shared);
        loginAs(user);

        assertThat(aiService.findAllForUser()).extracting(AI::getId).contains(original.getId());
        assertThatThrownBy(() -> aiService.cloneAI(original, "Mine")).isInstanceOf(SecurityException.class);
    }

    @Test
    void cloningAProviderNeedsThePermissionToCreateOne() throws Exception {
        OpenAICompatible original = createAI();
        setPermission(user, PermissionType.CREATE_AI, false);

        assertThatThrownBy(() -> aiService.cloneAI(original, "Another")).isInstanceOf(SecurityException.class);
    }
}
