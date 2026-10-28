package com.github.enerccio.marginalia.prompt;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.ExtendedContentMigration;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessagePromptService;
import com.github.enerccio.marginalia.domain.service.TrashService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The prompt of a part ({@link MessagePrompt}): kept apart from the part, copied with a branch, deleted and restored
 * with it, in a backup as it always was, and made from the extended content of the parts of an older version.
 */
class MessagePromptTest extends MarginaliaTestBase {

    private static final String PROMPT = "[{\"role\":\"SYSTEM\",\"content\":\"write\"}]";

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private Manuscript book;
    private ChatMessage first;
    private ChatMessage second;

    @BeforeEach
    void createStory() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        first = part(null, "First.");
        second = part(first, "Second.");
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    @Test
    void aPartWithoutAPromptHasNone() throws Exception {
        assertThat(messagePromptService.findFor(first)).isNull();
        assertThat(messagePromptService.getBuiltPrompt(first)).isNull();
        assertThat(messagePromptService.getBackgroundLore(first)).isNull();
    }

    @Test
    void theTextsAreStoredDeflatedAndComeBackAsTheyWere() throws Exception {
        String text = ("Příliš žluťoučký kůň úpěl ďábelské ódy. 吾輩は猫である。\n").repeat(5000);
        messagePromptService.store(first, text, "");

        assertThat(messagePromptService.getBuiltPrompt(first)).isEqualTo(text);
        assertThat(messagePromptService.getBackgroundLore(first)).isEmpty();
        MessagePrompt plain = new MessagePrompt();
        plain.setBuiltPrompt(null);
        assertThat(plain.getBuiltPrompt()).isNull();
    }

    @Test
    void thePromptIsStoredAndReplacedAndAnEmptyOneRemovesIt() throws Exception {
        messagePromptService.store(first, PROMPT, "lore");
        assertThat(messagePromptService.getBuiltPrompt(first)).isEqualTo(PROMPT);
        assertThat(messagePromptService.getBackgroundLore(first)).isEqualTo("lore");
        assertThat(messagePromptService.findFor(first).getOwner().getId()).isEqualTo(first.getOwner().getId());

        messagePromptService.store(first, "other", null);
        assertThat(messagePromptService.getBuiltPrompt(first)).isEqualTo("other");
        assertThat(messagePromptService.getBackgroundLore(first)).isNull();
        assertThat(messagePromptService.findAllFor(List.of(first, second))).containsOnlyKeys(first.getId());

        messagePromptService.store(first, "", null);
        assertThat(messagePromptService.findFor(first)).isNull();
    }

    @Test
    void theExtendedContentOfThePartHasNoPrompt() throws Exception {
        messagePromptService.store(first, PROMPT, "lore");
        chatMessageService.save(chatMessageService.find(first.getId()));

        String content = new String(chatMessageService.find(first.getId()).getExtendedContent(), StandardCharsets.UTF_8);
        assertThat(content).doesNotContain(MessagePrompt.PROMPT_KEY).doesNotContain(MessagePrompt.LORE_KEY);
    }

    @Test
    void aBranchHasTheSamePromptAndOwnsIt() throws Exception {
        messagePromptService.store(second, PROMPT, "lore");

        ChatMessage branch = chatMessageService.branch(book, chatMessageService.find(second.getId()));

        assertThat(messagePromptService.getBuiltPrompt(branch)).isEqualTo(PROMPT);
        assertThat(messagePromptService.getBackgroundLore(branch)).isEqualTo("lore");
        messagePromptService.store(branch, "changed", null);
        assertThat(messagePromptService.getBuiltPrompt(second)).isEqualTo(PROMPT);
    }

    @Test
    void deletingAPartDeletesItsPromptAndRestoringItBringsItBack() throws Exception {
        messagePromptService.store(first, PROMPT, "lore");
        messagePromptService.store(second, "second", null);

        chatMessageService.delete(chatMessageService.find(second.getId()), false);
        assertThat(messagePromptService.findFor(second)).isNull();
        assertThat(messagePromptService.findFor(first)).isNotNull();

        trashService.restore(List.of(new EntityKey(ChatMessage.class, second.getId())));
        assertThat(messagePromptService.getBuiltPrompt(second)).isEqualTo("second");
    }

    @Test
    void aHardDeleteRemovesThePrompt() throws Exception {
        MessagePrompt stored = null;
        messagePromptService.store(second, PROMPT, null);
        stored = messagePromptService.findFor(second);

        chatMessageService.delete(chatMessageService.find(second.getId()), true);

        assertThat(messagePromptService.find(stored.getId())).isNull();
    }

    @Test
    void deletingTheBookDeletesThePromptsAndAHardDeleteRemovesThem() throws Exception {
        messagePromptService.store(first, PROMPT, null);
        MessagePrompt stored = messagePromptService.findFor(first);

        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        assertThat(messagePromptService.find(stored.getId()).isDeleted()).isTrue();

        chatMessageService.hardDeleteForManuscript(manuscriptService.find(book.getId()));
        assertThat(messagePromptService.find(stored.getId())).isNull();
    }

    @Test
    void aBackupKeepsThePromptInTheExtendedContentAndARestoredBookGetsItBack() throws Exception {
        messagePromptService.store(first, PROMPT, "lore");

        JsonObject backup = manuscriptService.createBackup(book);

        JsonArray messages = backup.getAsJsonArray("messages");
        JsonObject extended = null;
        for (var element : messages) {
            if (element.getAsJsonObject().get("uuid").getAsString().equals(first.getUuid())) {
                extended = element.getAsJsonObject().getAsJsonObject("extendedContent");
            }
            if (element.getAsJsonObject().get("uuid").getAsString().equals(second.getUuid())) {
                assertThat(element.getAsJsonObject().getAsJsonObject("extendedContent").has(MessagePrompt.PROMPT_KEY)).isFalse();
            }
        }
        assertThat(extended.get(MessagePrompt.PROMPT_KEY).getAsString()).isEqualTo(PROMPT);
        assertThat(extended.get(MessagePrompt.LORE_KEY).getAsString()).isEqualTo("lore");

        Manuscript restored = manuscriptService.cloneFromBackup(backup);
        ChatMessage restoredFirst = chatMessageService.getAllMessages(restored).stream()
                .filter(message -> "First.".equals(message.getResponse())).findFirst().orElseThrow();
        ChatMessage restoredSecond = chatMessageService.getAllMessages(restored).stream()
                .filter(message -> "Second.".equals(message.getResponse())).findFirst().orElseThrow();
        assertThat(messagePromptService.getBuiltPrompt(restoredFirst)).isEqualTo(PROMPT);
        assertThat(messagePromptService.getBackgroundLore(restoredFirst)).isEqualTo("lore");
        assertThat(messagePromptService.findFor(restoredSecond)).isNull();
        assertThat(new String(chatMessageService.find(restoredFirst.getId()).getExtendedContent(), StandardCharsets.UTF_8))
                .doesNotContain(MessagePrompt.PROMPT_KEY);
    }

    @Test
    void theMigrationOfManyPartsNeedsNewIdsFromTheDatabaseAndStillWorks() throws Exception {
        // more rows than an id generator has ids in a block, so it has to update its sequence table (on another connection,
        // which SQLite refuses to a transaction that has read before) at least once during the migration
        ChatMessage parent = second;
        List<ChatMessage> parts = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) {
            parent = part(parent, "Part " + i);
            ChatMessage legacy = chatMessageService.find(parent.getId());
            legacy.setExtendedContent(("{\"modelUsed\":\"m\",\"builtPrompt\":\"prompt " + i + "\",\"backgroundLore\":\"lore " + i + "\"}")
                    .getBytes(StandardCharsets.UTF_8));
            chatMessageService.saveWithoutEvent(legacy);
            parts.add(parent);
        }

        ExtendedContentMigration migration = new ExtendedContentMigration();
        beanFactory.autowireBean(migration);
        assertThat(migration.migrate(5, MigrationType.APP)).isEqualTo(6);

        for (int i = 0; i < parts.size(); i++) {
            assertThat(messagePromptService.getBuiltPrompt(parts.get(i))).isEqualTo("prompt " + i);
            assertThat(messagePromptService.getBackgroundLore(parts.get(i))).isEqualTo("lore " + i);
        }
    }

    @Test
    void theMigrationMovesThePromptOutOfTheExtendedContentOnce() throws Exception {
        // a part as the version before had it: the prompt in its extended content
        ChatMessage legacy = chatMessageService.find(first.getId());
        legacy.setExtendedContent(("{\"response\":\"First.\",\"modelUsed\":\"m\",\"builtPrompt\":\"" + "x".repeat(2000)
                + "\",\"backgroundLore\":\"old lore\"}").getBytes(StandardCharsets.UTF_8));
        chatMessageService.saveWithoutEvent(legacy);

        ExtendedContentMigration migration = new ExtendedContentMigration();
        beanFactory.autowireBean(migration);
        assertThat(migration.migrate(5, MigrationType.APP)).isEqualTo(6);

        assertThat(messagePromptService.getBuiltPrompt(first)).isEqualTo("x".repeat(2000));
        assertThat(messagePromptService.getBackgroundLore(first)).isEqualTo("old lore");
        ChatMessage migrated = chatMessageService.find(first.getId());
        assertThat(new String(migrated.getExtendedContent(), StandardCharsets.UTF_8))
                .doesNotContain(MessagePrompt.PROMPT_KEY).doesNotContain(MessagePrompt.LORE_KEY).contains("modelUsed");
        assertThat(migrated.getModelUsed()).isEqualTo("m");
        assertThat(messagePromptService.findFor(second)).isNull();

        // once more changes nothing, an other version is not its business
        assertThat(migration.migrate(5, MigrationType.APP)).isEqualTo(6);
        assertThat(messagePromptService.getBuiltPrompt(first)).isEqualTo("x".repeat(2000));
        assertThat(migration.migrate(4, MigrationType.APP)).isEqualTo(4);
    }
}
