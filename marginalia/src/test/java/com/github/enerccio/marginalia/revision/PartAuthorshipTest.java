package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.BookAuthorship;
import com.github.enerccio.marginalia.domain.components.PartAuthorship;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Who wrote the words of a part, worked out from the versions of its history ({@link MessageRevisionService#authorship}).
 */
class PartAuthorshipTest extends MarginaliaTestBase {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
    }

    /**
     * A part the model generated: the text, its original and the first version.
     */
    private ChatMessage generated(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setOriginalResponse(text);
        message.setModelUsed("model-a");
        ChatMessage saved = parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
        messageRevisionService.record(saved, new RevisionData(RevisionSource.GENERATE, "model-a · protocol", false, text, null, null, null, null));
        return chatMessageService.find(saved.getId());
    }

    private static List<String> runs(PartAuthorship authorship) {
        List<String> runs = new ArrayList<>();
        for (int[] range : authorship.generatedRanges()) {
            runs.add(authorship.text().substring(range[0], range[1]));
        }
        return runs;
    }

    @Test
    void aGeneratedPartIsTheModelsWhole() throws Exception {
        ChatMessage part = generated(null, "The night was dark. Rain fell.");

        PartAuthorship authorship = messageRevisionService.authorship(part);

        assertThat(runs(authorship)).containsExactly("The night was dark. Rain fell.");
        assertThat(authorship.generatedWords()).isEqualTo(6);
        assertThat(authorship.writtenWords()).isZero();
    }

    @Test
    void aPartTheAuthorTypedIsTheirsWhole() throws Exception {
        ChatMessage part = chatMessageService.createPart(book, null, "Written by hand.", null);

        PartAuthorship authorship = messageRevisionService.authorship(part);

        assertThat(authorship.generatedRanges()).isEmpty();
        assertThat(authorship.writtenWords()).isEqualTo(3);
    }

    @Test
    void whatTheAuthorAddedStaysTheirsThroughTheLaterVersions() throws Exception {
        ChatMessage part = generated(null, "The night was dark. Rain fell.");
        chatMessageService.updateResponse(book, part, "The night was dark and cold. Rain fell.");
        chatMessageService.storeResponse(book, chatMessageService.find(part.getId()), "The night was dark and cold. Thunder rolled.",
                RevisionOrigin.model(RevisionSource.REDRAFT, "model-a · protocol", false, null));
        chatMessageService.updateResponse(book, chatMessageService.find(part.getId()), "The night was dark and cold. Thunder rolled on.");

        PartAuthorship authorship = messageRevisionService.authorship(chatMessageService.find(part.getId()));

        assertThat(runs(authorship)).containsExactly("The night was dark", ". Thunder rolled", ".");
        assertThat(authorship.generatedWords()).isEqualTo(6);
        assertThat(authorship.writtenWords()).isEqualTo(3);
    }

    @Test
    void aGenerationInPlaceStartsOver() throws Exception {
        ChatMessage part = generated(null, "The night was dark.");
        chatMessageService.updateResponse(book, part, "The night was very dark.");

        chatMessageService.storeResponse(book, chatMessageService.find(part.getId()), "A bright day began.",
                RevisionOrigin.model(RevisionSource.REGENERATE, "model-a · protocol", false, null));

        PartAuthorship authorship = messageRevisionService.authorship(chatMessageService.find(part.getId()));
        assertThat(authorship.writtenWords()).isZero();
        assertThat(authorship.generatedWords()).isEqualTo(4);
    }

    @Test
    void textAnAgentWroteIsGenerated() throws Exception {
        ChatMessage part = chatMessageService.createPart(book, null, "He drew his sword.", null);
        chatMessageService.storeResponse(book, chatMessageService.find(part.getId()), "He drew his sword slowly.",
                RevisionOrigin.human(RevisionDetail.REPLACE, true));

        PartAuthorship authorship = messageRevisionService.authorship(chatMessageService.find(part.getId()));

        assertThat(runs(authorship)).containsExactly("slowly");
        assertThat(authorship.writtenWords()).isEqualTo(4);
        assertThat(authorship.generatedWords()).isEqualTo(1);
    }

    @Test
    void aRestoredVersionHasTheAuthorshipItHadBefore() throws Exception {
        ChatMessage part = generated(null, "The night was dark.");
        MessageRevision first = messageRevisionService.findLatest(part);
        chatMessageService.updateResponse(book, part, "The night was very dark.");

        messageRevisionService.restoreOnTop(book, chatMessageService.find(part.getId()), first);

        PartAuthorship authorship = messageRevisionService.authorship(chatMessageService.find(part.getId()));
        assertThat(authorship.text()).isEqualTo("The night was dark.");
        assertThat(authorship.writtenWords()).isZero();
        assertThat(authorship.generatedWords()).isEqualTo(4);
    }

    @Test
    void aTextChangedWithoutAVersionIsTheAuthorsEdit() throws Exception {
        ChatMessage part = generated(null, "The night was dark.");
        ChatMessage stored = chatMessageService.find(part.getId());
        stored.setResponse("The night was dark and long.");
        stored = chatMessageService.save(stored);

        PartAuthorship authorship = messageRevisionService.authorship(stored);

        assertThat(authorship.generatedWords()).isEqualTo(4);
        assertThat(authorship.writtenWords()).isEqualTo(2);
    }

    @Test
    void aPartWithoutHistoryIsToldApartFromTheTextTheModelGenerated() throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse("The night was very dark.");
        message.setOriginalResponse("The night was dark.");
        ChatMessage part = chatMessageService.createRoot(book, message);
        assertThat(messageRevisionService.list(part, true)).isEmpty();

        PartAuthorship authorship = messageRevisionService.authorship(part);

        assertThat(authorship.generatedWords()).isEqualTo(4);
        assertThat(authorship.writtenWords()).isEqualTo(1);
    }

    @Test
    void theBranchAddsUpThePartsAndNamesTheModels() throws Exception {
        ChatMessage one = generated(null, "The night was dark.");
        ChatMessage two = chatMessageService.createPart(book, one, "She said nothing.", null);
        ChatMessage three = generated(two, "Rain fell on the roof.");
        chatMessageService.updateResponse(book, three, "Rain fell on the old roof.");

        BookAuthorship authorship = messageRevisionService.authorship(List.of(
                chatMessageService.find(one.getId()), chatMessageService.find(two.getId()), chatMessageService.find(three.getId())));

        assertThat(authorship.parts()).isEqualTo(3);
        assertThat(authorship.partsWithGeneratedText()).isEqualTo(2);
        assertThat(authorship.generatedWords()).isEqualTo(9);
        assertThat(authorship.writtenWords()).isEqualTo(4);
        assertThat(authorship.models()).containsExactly("model-a");
        assertThat(authorship.generatedShare()).isEqualTo(9.0 / 13);
    }
}
