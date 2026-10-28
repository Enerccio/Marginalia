package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The parts of the prompts of one part (the annotation, the redraft) that decide what a server can reuse of them.
 */
class StoryAroundTest {

    @Test
    void theRoomIsRoundedDownToAStepOfTheBudget() {
        // a sixteenth of the budget as a power of two, 256 at least
        assertThat(StoryAround.stableRoom(1300, 2600)).isEqualTo(1280);
        assertThat(StoryAround.stableRoom(1250, 2600)).isEqualTo(1024);
        assertThat(StoryAround.stableRoom(20_000, 24_000)).isEqualTo(19_456);
        assertThat(StoryAround.stableRoom(19_700, 24_000)).isEqualTo(19_456);
        assertThat(StoryAround.stableRoom(19_000, 24_000)).isEqualTo(18_432);
        // less than a step is left as it is, and a room never grows
        assertThat(StoryAround.stableRoom(100, 24_000)).isEqualTo(100);
        assertThat(StoryAround.stableRoom(0, 24_000)).isZero();
        for (long room = 0; room < 30_000; room += 37) {
            assertThat(StoryAround.stableRoom(room, 24_000)).isLessThanOrEqualTo(room);
        }
    }

    @Test
    void theStoryBecomesTheEarlierMessagesOfTheConversation() {
        List<LLMChatMessage> messages = StoryAround.messages("HEAD\nNOTE\n\n", List.of("ONE", "TWO"), "\nTAIL\n");

        assertThat(messages).extracting(LLMChatMessage::getRole).containsExactly(
                LLMRole.SYSTEM, LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER);
        assertThat(messages).extracting(LLMChatMessage::getContent).containsExactly(
                "HEAD\nNOTE", StoryAround.STORY_START, "ONE", StoryAround.STORY_MORE, "TWO", "TAIL");
    }

    @Test
    void aNewPartOnlyAddsMessagesAfterTheOnesThatWere() {
        // what a server that kept the first prompt can go on from: the longer one starts with all of it but its last message
        List<LLMChatMessage> first = StoryAround.messages("HEAD", List.of("ONE", "TWO"), "TAIL ONE");
        List<LLMChatMessage> second = StoryAround.messages("HEAD", List.of("ONE", "TWO", "THREE"), "TAIL TWO");

        List<LLMChatMessage> kept = first.subList(0, first.size() - 1);
        assertThat(second.subList(0, kept.size())).extracting(LLMChatMessage::getContent)
                .containsExactlyElementsOf(kept.stream().map(LLMChatMessage::getContent).toList());
    }

    @Test
    void withoutAStoryItIsASystemMessageAndAUserMessage() {
        assertThat(StoryAround.messages("HEAD", List.of(), "TAIL")).extracting(LLMChatMessage::getRole)
                .containsExactly(LLMRole.SYSTEM, LLMRole.USER);
    }

    @Test
    void theConversationEndsWithTheUserAndHasNoEmptySystemMessage() {
        List<LLMChatMessage> noTail = StoryAround.messages("HEAD", List.of("ONE"), "  ");
        assertThat(noTail.getLast().getRole()).isEqualTo(LLMRole.USER);
        assertThat(noTail.getLast().getContent()).isEqualTo(StoryAround.STORY_MORE);

        assertThat(StoryAround.messages("  ", List.of(), "TAIL")).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER);
    }
}
