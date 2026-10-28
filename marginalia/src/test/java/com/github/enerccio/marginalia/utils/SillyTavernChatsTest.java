package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.utils.SillyTavernChats.Chat;
import com.github.enerccio.marginalia.utils.SillyTavernChats.Node;
import com.github.enerccio.marginalia.utils.SillyTavernChats.Tree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reading the chats of SillyTavern and joining them into a tree.
 */
class SillyTavernChatsTest {

    private static final String HEADER = "{\"user_name\":\"Me\",\"character_name\":\"Narrator\",\"chat_metadata\":{}}";

    private static String user(String text, String date) {
        return "{\"name\":\"Me\",\"is_user\":true,\"is_system\":false,\"send_date\":\"" + date + "\",\"mes\":\"" + text + "\",\"extra\":{}}";
    }

    private static String bot(String text, String date) {
        return "{\"name\":\"Narrator\",\"is_user\":false,\"is_system\":false,\"send_date\":\"" + date + "\",\"mes\":\"" + text + "\",\"extra\":{\"model\":\"m1\",\"reasoning\":\"why\"}}";
    }

    private static String swiped(String active, int id, String date, String... swipes) {
        StringBuilder array = new StringBuilder();
        for (String swipe : swipes) {
            array.append(array.isEmpty() ? "" : ",").append('"').append(swipe).append('"');
        }
        return "{\"name\":\"Narrator\",\"is_user\":false,\"is_system\":false,\"send_date\":\"" + date + "\",\"mes\":\"" + active
                + "\",\"swipes\":[" + array + "],\"swipe_id\":" + id + ",\"swipe_info\":[],\"extra\":{}}";
    }

    private static Chat chat(String name, String... lines) {
        return SillyTavernChats.parse(HEADER + "\n" + String.join("\n", lines) + "\n", name + ".jsonl");
    }

    private static List<String> texts(Tree tree) {
        return tree.inStoringOrder().stream().map(n -> n.alternative().text()).toList();
    }

    @Test
    void readsTheHeaderAndTheMessages() {
        Chat chat = chat("Story", user("hello", "2026-01-01T10:00:00.000Z"), bot("hi", "2026-01-01T10:00:05.000Z"));

        assertThat(chat.name()).isEqualTo("Story");
        assertThat(chat.characterName()).isEqualTo("Narrator");
        assertThat(chat.messages()).hasSize(2);
        assertThat(chat.messages().getFirst().user()).isTrue();
        assertThat(chat.messages().getLast().alternatives().getFirst().model()).isEqualTo("m1");
        assertThat(chat.messages().getLast().alternatives().getFirst().reasoning()).isEqualTo("why");
        assertThat(chat.lastSent()).isNotNull();
    }

    @Test
    void notAChatIsRefused() {
        assertThatThrownBy(() -> SillyTavernChats.parse("not json", "x.jsonl")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SillyTavernChats.parse("", "x.jsonl")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SillyTavernChats.parse("{\"a\":1}", "x.jsonl")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void systemMessagesAreLeftOut() {
        Chat chat = chat("s", "{\"name\":\"note\",\"is_system\":true,\"mes\":\"hidden\"}", bot("shown", "2026-01-01T10:00:00.000Z"));

        assertThat(chat.messages()).hasSize(1);
    }

    @Test
    void everyMessageIsAPartWithoutMerging() {
        Tree tree = SillyTavernChats.merge(List.of(chat("a", user("u1", "2026-01-01T10:00:00.000Z"), bot("b1", "2026-01-01T10:00:01.000Z"))), false);

        assertThat(tree.size()).isEqualTo(2);
        assertThat(texts(tree)).containsExactly("u1", "b1");
        assertThat(tree.inStoringOrder()).allSatisfy(n -> assertThat(n.instructions()).isNull());
        assertThat(tree.activeLeaf().alternative().text()).isEqualTo("b1");
    }

    @Test
    void userMessageBecomesTheInstructionsOfTheAnswerWhenMerging() {
        Tree tree = SillyTavernChats.merge(List.of(chat("a",
                bot("greeting", "2026-01-01T10:00:00.000Z"),
                user("u1", "2026-01-01T10:00:01.000Z"), user("u2", "2026-01-01T10:00:02.000Z"), bot("b1", "2026-01-01T10:00:03.000Z"),
                user("last", "2026-01-01T10:00:04.000Z"))), true);

        List<Node> nodes = tree.inStoringOrder();
        assertThat(nodes).extracting(n -> n.instructions() + "|" + n.alternative().text())
                .containsExactly("null|greeting", "u1\n\nu2|b1", "last|");
        assertThat(tree.activeLeaf()).isSameAs(nodes.getLast());
    }

    @Test
    void swipesAreSiblingsAndTheChatContinuesUnderTheSelectedOne() {
        Tree tree = SillyTavernChats.merge(List.of(chat("a",
                user("u1", "2026-01-01T10:00:00.000Z"),
                swiped("second", 1, "2026-01-01T10:00:02.000Z", "first", "second", "", "third"),
                user("u2", "2026-01-01T10:00:03.000Z"))), false);

        Node u1 = tree.roots().getFirst();
        assertThat(u1.children()).extracting(n -> n.alternative().text()).containsExactly("first", "second", "third");
        Node second = u1.children().get(1);
        assertThat(second.children()).extracting(n -> n.alternative().text()).containsExactly("u2");
        assertThat(u1.children().get(0).children()).isEmpty();
        assertThat(tree.activeLeaf().parent()).isSameAs(second);
        assertThat(tree.size()).isEqualTo(5);
    }

    @Test
    void chatsWithACommonBeginningShareItAndBranch() {
        Chat main = chat("main", user("u1", "2026-01-01T10:00:00.000Z"), bot("b1", "2026-01-01T10:00:01.000Z"),
                user("u2", "2026-01-01T10:00:02.000Z"), bot("b2", "2026-01-01T10:00:03.000Z"));
        Chat branch = chat("branch", user("u1", "2026-01-01T10:00:00.000Z"), bot("b1", "2026-01-01T10:00:01.000Z"),
                user("other", "2026-01-02T10:00:02.000Z"), bot("b2x", "2026-01-02T10:00:03.000Z"));

        Tree tree = SillyTavernChats.merge(List.of(main, branch), false);

        assertThat(tree.roots()).hasSize(1);
        assertThat(tree.size()).isEqualTo(6);
        Node b1 = tree.roots().getFirst().children().getFirst();
        assertThat(b1.children()).extracting(n -> n.alternative().text()).containsExactly("u2", "other");
        // the branch was written last
        assertThat(tree.activeLeaf().alternative().text()).isEqualTo("b2x");
        // every part is stored after its parent, the siblings in the order of the chats
        assertThat(texts(tree)).containsExactly("u1", "b1", "u2", "b2", "other", "b2x");
    }

    @Test
    void theSameChatTwiceAddsNothing() {
        Chat chat = chat("a", user("u1", "2026-01-01T10:00:00.000Z"), bot("b1", "2026-01-01T10:00:01.000Z"));

        assertThat(SillyTavernChats.merge(List.of(chat, chat), true).size()).isEqualTo(1);
    }

    @Test
    void chatsThatStartDifferentlyHaveSeveralRoots() {
        Tree tree = SillyTavernChats.merge(List.of(chat("a", bot("x", "2026-01-01T10:00:00.000Z")), chat("b", bot("y", "2026-01-01T10:00:00.000Z"))), false);

        assertThat(tree.roots()).hasSize(2);
        assertThat(tree.roots()).allSatisfy(n -> assertThat(n.parent()).isNull());
    }

    @Test
    void noChatsNoTree() {
        Tree tree = SillyTavernChats.merge(List.of(), false);

        assertThat(tree.size()).isZero();
        assertThat(tree.activeLeaf()).isNull();
        assertThat(tree.inStoringOrder()).isEmpty();
    }
}
