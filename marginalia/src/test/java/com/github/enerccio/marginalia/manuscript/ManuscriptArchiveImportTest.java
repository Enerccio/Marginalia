package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Parts added to a book from a zip archive of markdown files.
 */
class ManuscriptArchiveImportTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
    }

    @Test
    void filesBecomeAChainOfPartsInTheOrderOfTheirNames() throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("0010.md", "third");
        files.put("0002.md", "second");
        files.put("0001.md", "first");
        files.put("readme.txt", "skipped");

        assertThat(chatMessageService.appendFromArchive(book, zip(files))).isEqualTo(3);

        Manuscript saved = manuscriptService.find(book);
        assertThat(chatMessageService.getBranchFromLeaf(saved.getActiveLeaf()))
                .extracting(ChatMessage::getResponse).containsExactly("first", "second", "third");
        assertThat(chatMessageService.getAllMessages(saved)).hasSize(3);
        assertThat(chatMessageService.getBranchFromLeaf(saved.getActiveLeaf()))
                .allSatisfy(part -> assertThat(part.getWordCount()).isEqualTo(1));
    }

    @Test
    void partsContinueAfterTheActiveLeaf() throws Exception {
        ChatMessage start = chatMessageService.createPart(book, null, "start", null);

        chatMessageService.appendFromArchive(book, zip(Map.of("1.md", "next")));

        Manuscript saved = manuscriptService.find(book);
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(saved.getActiveLeaf());
        assertThat(branch).extracting(ChatMessage::getResponse).containsExactly("start", "next");
        assertThat(branch.getFirst().getId()).isEqualTo(start.getId());
    }

    @Test
    void badArchiveChangesNothing() throws Exception {
        assertThatThrownBy(() -> chatMessageService.appendFromArchive(book, zip(Map.of("a.txt", "text"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> chatMessageService.appendFromArchive(book, "plain".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(chatMessageService.hasAnyMessages(book)).isFalse();
    }

    private static byte[] zip(Map<String, String> files) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
