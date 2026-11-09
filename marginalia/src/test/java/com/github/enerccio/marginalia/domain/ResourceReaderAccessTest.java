package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ResourceService#readForReader}: the files that the parts of a book carry are readable by whoever may view the book (the owner, and
 * everybody while it is published), and by nothing else.
 */
class ResourceReaderAccessTest extends MarginaliaTestBase {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    private User owner;
    private User reader;
    private Manuscript book;
    private Resource audio;
    private ChatMessage part;
    private byte[] content;

    @BeforeEach
    void setUp() throws Exception {
        owner = createUser();
        reader = createUser();
        loginAs(owner);
        book = manuscriptService.save(newBook("Book"));
        part = chatMessageService.createPart(manuscriptService.find(book.getId()), null, "text", null);
        content = uniqueName("audio").getBytes(StandardCharsets.UTF_8);
        audio = resourceService.link(resourceService.upload("part.mp3", content, "audio/mpeg"), ChatMessage.class, part.getId());
    }

    private static Manuscript newBook(String name) {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(name);
        return manuscript;
    }

    private void publish(boolean published) throws Exception {
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setPublished(published);
        manuscriptService.save(stored);
    }

    @Test
    void theOwnerReadsTheFileOfAPartOfTheirBook() throws Exception {
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isEqualTo(content);
    }

    @Test
    void anotherUserReadsItOnlyWhileTheBookIsPublished() throws Exception {
        loginAs(reader);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isNull();

        loginAs(owner);
        publish(true);
        loginAs(reader);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isEqualTo(content);

        loginAs(owner);
        publish(false);
        loginAs(reader);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isNull();
    }

    @Test
    void aResourceThatIsNotLinkedToAPartOfThatBookIsNotReadable() throws Exception {
        publish(true);
        Resource loose = resourceService.upload("loose.mp3", uniqueName("loose").getBytes(StandardCharsets.UTF_8), "audio/mpeg");
        Manuscript other = manuscriptService.save(newBook("Other"));
        ChatMessage otherPart = chatMessageService.createPart(manuscriptService.find(other.getId()), null, "text", null);
        Resource ofOther = resourceService.link(resourceService.upload("other.mp3", uniqueName("other").getBytes(StandardCharsets.UTF_8), "audio/mpeg"),
                ChatMessage.class, otherPart.getId());

        loginAs(reader);
        assertThat(resourceService.readForReader(loose.getUuid(), book.getUuid())).isNull();
        // the part belongs to another book, even a published one is not the book that was asked for
        assertThat(resourceService.readForReader(ofOther.getUuid(), book.getUuid())).isNull();
    }

    @Test
    void aDeletedResourceIsNotReadable() throws Exception {
        publish(true);
        loginAs(reader);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isEqualTo(content);

        loginAs(owner);
        resourceService.softDelete(List.of(audio.getUuid()));
        loginAs(reader);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isNull();
    }

    @Test
    void aDeletedBookIsNotReadable() throws Exception {
        publish(true);
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        loginAs(reader);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isNull();
        loginAs(owner);
        assertThat(resourceService.readForReader(audio.getUuid(), book.getUuid())).isNull();
    }

    @Test
    void blankOrUnknownArgumentsGiveNothing() throws Exception {
        assertThat(resourceService.readForReader(null, book.getUuid())).isNull();
        assertThat(resourceService.readForReader(audio.getUuid(), "")).isNull();
        assertThat(resourceService.readForReader("nope", book.getUuid())).isNull();
        assertThat(resourceService.readForReader(audio.getUuid(), "nope")).isNull();
    }

    // --- images: the setting of the book decides what a reader sees ---

    private Resource image() throws Exception {
        // a 1x1 png
        byte[] png = java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        return resourceService.uploadImage("pic.png", png, ChatMessage.class, part.getId());
    }

    private void showImages(Boolean show) throws Exception {
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setShowImagesToReaders(show);
        manuscriptService.save(stored);
    }

    @Test
    void imagesAreSeenByReadersOfAPublishedBookUnlessTheAuthorTurnedThemOff() throws Exception {
        Resource pic = image();
        publish(true);
        // not set means shown
        loginAs(reader);
        Resource found = resourceService.findImageForReader(pic.getUuid(), book.getUuid());
        assertThat(found).isNotNull();
        assertThat(resourceService.getResourceData(found)).isNotEmpty();

        loginAs(owner);
        showImages(false);
        loginAs(reader);
        assertThat(resourceService.findImageForReader(pic.getUuid(), book.getUuid())).isNull();
        // the owner keeps seeing them
        loginAs(owner);
        assertThat(resourceService.findImageForReader(pic.getUuid(), book.getUuid())).isNotNull();

        showImages(true);
        loginAs(reader);
        assertThat(resourceService.findImageForReader(pic.getUuid(), book.getUuid())).isNotNull();
    }

    @Test
    void imagesOfAnUnpublishedBookOrAnotherBookAreNotSeen() throws Exception {
        Resource pic = image();
        loginAs(reader);
        assertThat(resourceService.findImageForReader(pic.getUuid(), book.getUuid())).isNull();

        loginAs(owner);
        publish(true);
        Manuscript other = manuscriptService.save(newBook("Other"));
        loginAs(reader);
        // the book asked for is not the one the image is in
        assertThat(resourceService.findImageForReader(pic.getUuid(), other.getUuid())).isNull();
        assertThat(resourceService.findImageForReader(pic.getUuid(), null)).isNull();
    }

    @Test
    void anImageIsNeverReadThroughTheFileRoute() throws Exception {
        Resource pic = image();
        publish(true);
        loginAs(reader);

        assertThat(resourceService.readForReader(pic.getUuid(), book.getUuid())).isNull();
    }
}
