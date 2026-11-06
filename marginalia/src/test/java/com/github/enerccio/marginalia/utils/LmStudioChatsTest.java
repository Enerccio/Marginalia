package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.utils.SillyTavernChats.Chat;
import com.github.enerccio.marginalia.utils.SillyTavernChats.Tree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reading the conversations of LM Studio and joining them into a tree.
 */
class LmStudioChatsTest {

    private static String user(String text) {
        return "{\"versions\":[{\"type\":\"singleStep\",\"role\":\"user\",\"content\":[{\"type\":\"text\",\"text\":\"" + text + "\"}]}],\"currentlySelected\":0}";
    }

    private static String assistantVersion(String model, String thinking, String text) {
        String think = thinking == null ? "" : "{\"type\":\"contentBlock\",\"style\":{\"type\":\"thinking\",\"ended\":true},"
                + "\"content\":[{\"type\":\"text\",\"text\":\"" + thinking + "\"}]},";
        return "{\"type\":\"multiStep\",\"role\":\"assistant\",\"senderInfo\":{\"senderName\":\"" + model + "\"},\"steps\":[" + think
                + "{\"type\":\"contentBlock\",\"content\":[{\"type\":\"text\",\"text\":\"" + text + "\"}],\"genInfo\":{\"identifier\":\"id-" + model + "\"}}]}";
    }

    private static String assistant(int selected, String... versions) {
        return "{\"versions\":[" + String.join(",", versions) + "],\"currentlySelected\":" + selected + "}";
    }

    private static String conversation(String name, long createdAt, String... messages) {
        return "{\"name\":" + (name == null ? "null" : "\"" + name + "\"") + ",\"createdAt\":" + createdAt + ",\"systemPrompt\":\"be nice\",\"messages\":["
                + String.join(",", messages) + "]}";
    }

    @Test
    void readsTheMessagesTheReasoningAndTheModel() {
        Chat chat = LmStudioChats.parse(conversation("Story", 1000, user("hello"), assistant(0, assistantVersion("m1", "why", "hi"))),
                "1000.conversation.json");

        assertThat(chat.name()).isEqualTo("Story");
        assertThat(chat.messages()).hasSize(2);
        assertThat(chat.messages().get(0).user()).isTrue();
        assertThat(chat.messages().get(0).shown().text()).isEqualTo("hello");
        SillyTavernChats.Alternative answer = chat.messages().get(1).shown();
        assertThat(chat.messages().get(1).user()).isFalse();
        assertThat(answer.text()).isEqualTo("hi");
        assertThat(answer.reasoning()).isEqualTo("why");
        assertThat(answer.model()).isEqualTo("id-m1");
    }

    @Test
    void theNameOfTheFileNamesAConversationWithoutName() {
        Chat chat = LmStudioChats.parse(conversation(null, 1000, user("hello")), "1793958287215.conversation.json");

        assertThat(chat.name()).isEqualTo("1793958287215");
    }

    @Test
    void theVersionsOfAMessageAreAlternativesAndTheSelectedOneIsShown() {
        Chat chat = LmStudioChats.parse(conversation("S", 1, user("go"),
                assistant(1, assistantVersion("m", null, "first try"), assistantVersion("m", null, "second try"))), "s.json");

        SillyTavernChats.Message answer = chat.messages().get(1);
        assertThat(answer.alternatives()).extracting(SillyTavernChats.Alternative::text).containsExactly("first try", "second try");
        assertThat(answer.shown().text()).isEqualTo("second try");

        Tree tree = SillyTavernChats.merge(List.of(chat), false);
        assertThat(tree.size()).isEqualTo(3);
        assertThat(tree.activeLeaf().alternative().text()).isEqualTo("second try");
    }

    @Test
    void anAnswerWithTheTextDirectlyInTheVersionIsRead() {
        String old = "{\"versions\":[{\"type\":\"singleStep\",\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"plain\"}]}],\"currentlySelected\":0}";
        Chat chat = LmStudioChats.parse(conversation("S", 1, old), "s.json");

        assertThat(chat.messages().getFirst().shown().text()).isEqualTo("plain");
        assertThat(chat.messages().getFirst().shown().reasoning()).isNull();
    }

    @Test
    void theSystemPromptAndTheToolsAreNotMessagesOfTheStory() {
        String system = "{\"versions\":[{\"type\":\"singleStep\",\"role\":\"system\",\"content\":[{\"type\":\"text\",\"text\":\"rules\"}]}],\"currentlySelected\":0}";
        Chat chat = LmStudioChats.parse(conversation("S", 1, system, user("hello")), "s.json");

        assertThat(chat.messages()).extracting(m -> m.shown().text()).containsExactly("hello");
    }

    @Test
    void conversationsThatShareTheirBeginningJoinIntoOneTree() {
        Chat a = LmStudioChats.parse(conversation("a", 1, user("u1"), assistant(0, assistantVersion("m", null, "b1")), user("u2")), "a.json");
        Chat b = LmStudioChats.parse(conversation("b", 2, user("u1"), assistant(0, assistantVersion("m", null, "b1")), user("other")), "b.json");

        Tree tree = SillyTavernChats.merge(List.of(a, b), false);

        assertThat(tree.size()).isEqualTo(4);
        // the conversation that was written last decides what is shown
        assertThat(tree.activeLeaf().alternative().text()).isEqualTo("other");
    }

    @Test
    void mergeTurnsMakesTheUserMessageTheInstructions() {
        Chat chat = LmStudioChats.parse(conversation("S", 1, user("write a duel"), assistant(0, assistantVersion("m", null, "Swords."))), "s.json");

        Tree tree = SillyTavernChats.merge(List.of(chat), true);

        assertThat(tree.size()).isEqualTo(1);
        assertThat(tree.roots().getFirst().instructions()).isEqualTo("write a duel");
        assertThat(tree.roots().getFirst().alternative().text()).isEqualTo("Swords.");
    }

    @Test
    void somethingThatIsNotAConversationIsRefused() {
        assertThatThrownBy(() -> LmStudioChats.parse("not json at all {", "x.json")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LmStudioChats.parse("[1,2]", "x.json")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LmStudioChats.parse("{\"name\":\"x\"}", "x.json")).isInstanceOf(IllegalArgumentException.class);
        // a chat of SillyTavern is a line of JSON per message
        assertThatThrownBy(() -> LmStudioChats.parse("{\"user_name\":\"Me\"}\n{\"mes\":\"hi\"}", "x.jsonl")).isInstanceOf(IllegalArgumentException.class);
    }
}
