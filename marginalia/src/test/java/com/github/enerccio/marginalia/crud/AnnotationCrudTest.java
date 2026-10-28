package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationCrudTest extends ExtendableCrudContract<Annotation> {

    @Autowired
    private AnnotationService annotationService;

    @Override
    protected OwnedService<Annotation, ?> service() {
        return annotationService;
    }

    @Override
    protected Annotation newEntity() {
        Annotation annotation = new Annotation();
        annotation.setText("The timeline is wrong here.");
        annotation.setQuote("Anna met John");
        annotation.setContextBefore("Chapter one. ");
        annotation.setContextAfter(" at the bar.");
        annotation.setType("continuity");
        annotation.setLabel("Checker");
        annotation.setSource(AnnotationSource.PLUGIN);
        annotation.setSendToPrompt(true);
        annotation.setRange(13, 26);
        return annotation;
    }

    @Override
    protected void assertCreated(Annotation loaded) {
        assertThat(loaded.getText()).isEqualTo("The timeline is wrong here.");
        assertThat(loaded.getQuote()).isEqualTo("Anna met John");
        assertThat(loaded.getContextBefore()).isEqualTo("Chapter one. ");
        assertThat(loaded.getContextAfter()).isEqualTo(" at the bar.");
        assertThat(loaded.getType()).isEqualTo("continuity");
        assertThat(loaded.getLabel()).isEqualTo("Checker");
        assertThat(loaded.getSource()).isEqualTo(AnnotationSource.PLUGIN);
        assertThat(loaded.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(loaded.isSendToPrompt()).isTrue();
        assertThat(loaded.isNotLocated()).isFalse();
        assertThat(loaded.hasRange()).isTrue();
        assertThat(loaded.getStart()).isEqualTo(13);
        assertThat(loaded.getEnd()).isEqualTo(26);
    }

    @Override
    protected void modify(Annotation entity) {
        entity.setText("Fixed in the next draft.");
        entity.setStatus(AnnotationStatus.RESOLVED);
        entity.setSendToPrompt(false);
        entity.setType(null);
    }

    @Override
    protected void assertModified(Annotation loaded) {
        assertThat(loaded.getText()).isEqualTo("Fixed in the next draft.");
        assertThat(loaded.getStatus()).isEqualTo(AnnotationStatus.RESOLVED);
        assertThat(loaded.isSendToPrompt()).isFalse();
        assertThat(loaded.getType()).isNull();
        assertThat(loaded.getQuote()).isEqualTo("Anna met John");
    }
}
