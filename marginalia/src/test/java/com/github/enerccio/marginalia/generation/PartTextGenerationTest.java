package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.ModelText.Substitution;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An extension may keep the story in another form than a model should read it: what the prompt of a generation holds is what the
 * {@link PartTextRegistry} says the model reads, not the stored text. Here the stored text has speech in a made-up language and the
 * "extension" gives the model the English it was made from.
 */
class PartTextGenerationTest extends GenerationTestBase {

    private static final String OWNER = "part-text-generation-test";
    private static final String STORED = "Da ek se za skag.";
    private static final String ENGLISH = "I see the enemy.";

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private PartTextRegistry partTextRegistry;

    @Autowired
    private LorebookActivationService lorebookActivationService;

    private Manuscript manuscript;
    private List<ChatMessage> story;

    /** The registry is one bean for every test of the JVM: whatever a test registers is taken away. */
    @AfterEach
    void unregister() {
        partTextRegistry.unregisterAll(OWNER);
    }

    private void story(String template) throws Exception {
        Manuscript book = newManuscript();
        if (template != null) {
            book.setTemplate(template);
        }
        manuscript = manuscriptService.save(book);
        story = new ArrayList<>();
        for (String text : new String[]{"Anna left the camp.", "\"" + STORED + "\" said the guard.", "Night came."}) {
            ChatMessage message = new ChatMessage();
            message.setResponse(text);
            message.setTokenCount(5);
            story.add(story.isEmpty() ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(story.getLast(), message));
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private void translateSpeech() {
        partTextRegistry.register(OWNER, new PartTextRegistry.Transformer() {
            @Override
            public List<Substitution> substitutions(ChatMessage part) {
                int at = part.getResponse().indexOf(STORED);
                return at < 0 ? List.of() : List.of(new Substitution(at, at + STORED.length(), ENGLISH));
            }
        });
    }

    /** Everything the model was sent. */
    private String promptOf(GenerationRequest request) throws Exception {
        llm.enqueue(MockLLMResponse.text("generated"));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()), new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getSimpleErrors()).isEmpty();
        var sent = llm.lastCompletionRequest();
        return String.join("\n", sent.getMessages("system")) + "\n" + String.join("\n", sent.getMessages("user"))
                + "\n" + String.join("\n", sent.getMessages("assistant"));
    }

    @Test
    void withoutAnExtensionTheModelReadsTheStoredText() throws Exception {
        story(null);

        String prompt = promptOf(GenerationRequest.newMessage());

        assertThat(prompt).contains(STORED).doesNotContain(ENGLISH);
    }

    @Test
    void theStoryOfThePromptIsWhatTheModelReads() throws Exception {
        story(null);
        translateSpeech();

        String prompt = promptOf(GenerationRequest.newMessage());

        assertThat(prompt).contains("Anna left the camp.").contains(ENGLISH).contains("Night came.");
        assertThat(prompt).doesNotContain(STORED);
        // what is stored is the stored text
        assertThat(chatMessageService.find(story.get(1).getId()).getResponse()).contains(STORED).doesNotContain(ENGLISH);
    }

    @Test
    void theClosestMessageMacroIsWhatTheModelReads() throws Exception {
        story("[{{closest-message::2}}]");
        translateSpeech();

        assertThat(promptOf(GenerationRequest.newMessage())).contains("[\"" + ENGLISH + "\" said the guard.]");
    }

    @Test
    void theCurrentMessageOfARegenerateIsWhatTheModelReads() throws Exception {
        story("[{{current-message}}]");
        // the guard's part is the last one now: it is regenerated
        ChatMessage guard = story.get(1);
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(chatMessageService.find(guard.getId()));
        manuscript = manuscriptService.save(loaded);
        translateSpeech();

        assertThat(promptOf(GenerationRequest.regenerate(chatMessageService.find(guard.getId())))).contains("[\"" + ENGLISH + "\" said the guard.]");
    }

    @Test
    void theLorebookIsScannedInWhatTheModelReads() throws Exception {
        story(null);
        translateSpeech();

        // the scan of the recent parts is made of the parts as the model reads them: the English speech, not the made-up language
        List<String> recent = lorebookActivationService.recentParts(chatMessageService.find(story.getLast().getId()), 3, false);

        assertThat(recent).containsExactly("Anna left the camp.", "\"" + ENGLISH + "\" said the guard.", "Night came.");
    }
}
