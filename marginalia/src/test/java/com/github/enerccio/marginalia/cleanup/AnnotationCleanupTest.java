package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.TrashService.DeleteResult;
import com.github.enerccio.marginalia.domain.service.TrashService.RestoreResult;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An annotation belongs to its part: the part takes it to the trash, brings it back and takes it to the grave.
 */
class AnnotationCleanupTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private CleanupService cleanupService;

    private Manuscript book;
    private ChatMessage root;
    private ChatMessage child;
    private Annotation inRoot;
    private Annotation inChild;

    @BeforeEach
    void createStory() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        root = message(null, "Anna met John at the bar.");
        child = message(root, "They talked for hours.");
        inRoot = annotationService.create(book, root, 0, 4, "Name?", AnnotationSource.AUTHOR, null, null);
        inChild = annotationService.create(book, child, 5, 11, "Weak", AnnotationSource.AUTHOR, null, null);
    }

    private ChatMessage message(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private static EntityKey key(Annotation annotation) {
        return new EntityKey(Annotation.class, annotation.getId());
    }

    private boolean isDeleted(Annotation annotation) throws Exception {
        return annotationService.find(annotation.getId()).isDeleted();
    }

    @Test
    void deletedPartTakesItsAnnotationsAndRestoredPartBringsThemBack() throws Exception {
        chatMessageService.delete(chatMessageService.find(child.getId()), false);

        assertThat(isDeleted(inChild)).isTrue();
        assertThat(isDeleted(inRoot)).isFalse();

        RestoreResult result = trashService.restore(List.of(new EntityKey(ChatMessage.class, child.getId())));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getRestored()).contains(key(inChild));
        assertThat(isDeleted(inChild)).isFalse();
    }

    @Test
    void deletedBookTakesItsAnnotationsAndRestoredBookBringsThemBack() throws Exception {
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        assertThat(isDeleted(inRoot)).isTrue();
        assertThat(isDeleted(inChild)).isTrue();

        RestoreResult result = trashService.restore(List.of(new EntityKey(Manuscript.class, book.getId())));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getRestored()).contains(key(inRoot), key(inChild));
        assertThat(annotationService.listForManuscript(book)).extracting(Annotation::getId)
                .containsExactlyInAnyOrder(inRoot.getId(), inChild.getId());
    }

    @Test
    void anAnnotationDeletedAloneCanBeRestoredAlone() throws Exception {
        annotationService.remove(book, inChild);

        assertThat(isDeleted(inChild)).isTrue();
        assertThat(annotationService.listForManuscript(book)).extracting(Annotation::getId).containsExactly(inRoot.getId());

        RestoreResult result = trashService.restore(List.of(key(inChild)));

        assertThat(result.isBlocked()).isFalse();
        assertThat(isDeleted(inChild)).isFalse();
    }

    @Test
    void permanentDeleteOfAPartTakesItsAnnotationsWithIt() throws Exception {
        chatMessageService.delete(chatMessageService.find(child.getId()), false);

        DeleteResult result = trashService.deletePermanently(List.of(new EntityKey(ChatMessage.class, child.getId())));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getDeleted()).contains(new EntityKey(ChatMessage.class, child.getId()), key(inChild));
        assertThat(chatMessageService.find(child.getId())).isNull();
        assertThat(annotationService.find(inChild.getId())).isNull();
        assertThat(annotationService.find(inRoot.getId())).isNotNull();
    }

    @Test
    void cleanupPurgesTheAnnotationsOfADeletedBook() throws Exception {
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(annotationService.find(inRoot.getId())).isNull();
        assertThat(annotationService.find(inChild.getId())).isNull();
        assertThat(chatMessageService.find(child.getId())).isNull();
    }
}
