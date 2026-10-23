package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SillyTavernImportService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Chats of SillyTavern stored as the tree of parts of a book.
 */
class SillyTavernImportTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SillyTavernImportService importService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
    }

    private static String line(boolean user, String text, String date, String extra) {
        return "{\"name\":\"x\",\"is_user\":" + user + ",\"is_system\":false,\"send_date\":\"" + date + "\",\"mes\":\"" + text
                + "\",\"extra\":{" + extra + "}}";
    }

    private static SillyTavernChats.Chat chat(String name, String... lines) {
        return SillyTavernChats.parse("{\"user_name\":\"Me\",\"character_name\":\"C\",\"chat_metadata\":{}}\n" + String.join("\n", lines), name);
    }

    private ImportResult<Manuscript> store(boolean merge, SillyTavernChats.Chat... chats) throws Exception {
        return importService.importTree(book, SillyTavernChats.merge(List.of(chats), merge), () -> false, () -> {
        });
    }

    @Test
    void branchesAreStoredAsSiblingsAndTheNewestChatIsShown() throws Exception {
        SillyTavernChats.Chat main = chat("main", line(true, "u1", "2026-01-01T10:00:00.000Z", ""),
                line(false, "b1", "2026-01-01T10:00:01.000Z", "\"model\":\"m\",\"reasoning\":\"r\""),
                line(true, "u2", "2026-01-01T10:00:02.000Z", ""), line(false, "b2", "2026-01-01T10:00:03.000Z", ""));
        SillyTavernChats.Chat branch = chat("branch", line(true, "u1", "2026-01-01T10:00:00.000Z", ""),
                line(false, "b1", "2026-01-01T10:00:01.000Z", ""), line(true, "other", "2026-01-02T10:00:02.000Z", ""));

        ImportResult<Manuscript> result = store(false, main, branch);

        assertThat(result.isComplete()).isTrue();
        assertThat(result.imported()).isEqualTo(5);
        Manuscript saved = manuscriptService.find(book);
        assertThat(chatMessageService.getAllMessages(saved)).hasSize(5);
        List<ChatMessage> shown = chatMessageService.getBranchFromLeaf(saved.getActiveLeaf());
        assertThat(shown).extracting(ChatMessage::getResponse).containsExactly("u1", "b1", "other");
        ChatMessage b1 = shown.get(1);
        assertThat(b1.getModelUsed()).isEqualTo("m");
        assertThat(b1.getResponseReasoning()).isEqualTo("r");
        assertThat(b1.getWordCount()).isEqualTo(1);
        assertThat(b1.isEdited()).isFalse();
        assertThat(chatMessageService.getSwipesForMessage(shown.get(2))).extracting(ChatMessage::getResponse).containsExactly("u2", "other");
    }

    @Test
    void mergedTurnsKeepTheUserMessageAsInstructions() throws Exception {
        store(true, chat("main", line(true, "go north", "2026-01-01T10:00:00.000Z", ""), line(false, "You walk.", "2026-01-01T10:00:01.000Z", "")));

        Manuscript saved = manuscriptService.find(book);
        List<ChatMessage> shown = chatMessageService.getBranchFromLeaf(saved.getActiveLeaf());
        assertThat(shown).hasSize(1);
        assertThat(shown.getFirst().getInstructions()).isEqualTo("go north");
        assertThat(shown.getFirst().getResponse()).isEqualTo("You walk.");
    }

    @Test
    void stoppedImportKeepsWhatWasStoredAndTheShownBranch() throws Exception {
        SillyTavernChats.Chat main = chat("main", line(true, "u1", "2026-01-01T10:00:00.000Z", ""), line(false, "b1", "2026-01-01T10:00:01.000Z", ""),
                line(true, "u2", "2026-01-01T10:00:02.000Z", ""));
        AtomicInteger asked = new AtomicInteger();

        ImportResult<Manuscript> result = importService.importTree(book, SillyTavernChats.merge(List.of(main), false),
                () -> asked.incrementAndGet() > 2, () -> {
                });

        assertThat(result.isComplete()).isFalse();
        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.total()).isEqualTo(3);
        Manuscript saved = manuscriptService.find(book);
        assertThat(chatMessageService.getAllMessages(saved)).hasSize(2);
        assertThat(saved.getActiveLeaf().getResponse()).isEqualTo("b1");
    }
}
