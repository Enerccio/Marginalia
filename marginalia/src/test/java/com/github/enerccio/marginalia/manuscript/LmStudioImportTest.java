package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService.ImportResult;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SillyTavernImportService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.utils.LmStudioChats;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A conversation of LM Studio stored as the parts of a book.
 */
class LmStudioImportTest extends MarginaliaTestBase {

    private static final String CONVERSATION = """
            {"name":"Duel","createdAt":1700000000000,"messages":[
              {"versions":[{"type":"singleStep","role":"user","content":[{"type":"text","text":"Write a duel"}]}],"currentlySelected":0},
              {"versions":[
                {"type":"multiStep","role":"assistant","steps":[
                  {"type":"contentBlock","style":{"type":"thinking"},"content":[{"type":"text","text":"think first"}]},
                  {"type":"contentBlock","content":[{"type":"text","text":"Swords rang."}],"genInfo":{"identifier":"local-model"}}]},
                {"type":"multiStep","role":"assistant","steps":[
                  {"type":"contentBlock","content":[{"type":"text","text":"Pistols cracked."}],"genInfo":{"identifier":"local-model"}}]}
              ],"currentlySelected":1}
            ]}
            """;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SillyTavernImportService importService;

    @Test
    void theConversationBecomesTheTreeOfAnewBook() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        Manuscript book = manuscriptService.save(m);

        SillyTavernChats.Tree tree = SillyTavernChats.merge(List.of(LmStudioChats.parse(CONVERSATION, "1.conversation.json")), false);
        ImportResult<Manuscript> result = importService.importTree(book, tree, RevisionDetail.LMSTUDIO_IMPORT, () -> false, () -> {
        });

        assertThat(result.isComplete()).isTrue();
        assertThat(result.imported()).isEqualTo(3);
        Manuscript saved = manuscriptService.find(book);
        assertThat(chatMessageService.getAllMessages(saved)).hasSize(3);
        List<ChatMessage> shown = chatMessageService.getBranchFromLeaf(saved.getActiveLeaf());
        assertThat(shown).extracting(ChatMessage::getResponse).containsExactly("Write a duel", "Pistols cracked.");
        assertThat(shown.get(1).getModelUsed()).isEqualTo("local-model");
        assertThat(chatMessageService.getSwipesForMessage(shown.get(1))).extracting(ChatMessage::getResponse)
                .containsExactly("Swords rang.", "Pistols cracked.");
        ChatMessage swords = chatMessageService.getSwipesForMessage(shown.get(1)).getFirst();
        assertThat(swords.getResponseReasoning()).isEqualTo("think first");
    }
}
