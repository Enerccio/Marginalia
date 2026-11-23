package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.M_L;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The annotations with localized texts are found by their meta-annotation, whichever enum of keys they hold.
 */
class LocalizedAnnotationsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CommandTextSource
    @interface Incomplete {
        L[] humanName() default {};
    }

    static class Data {
        @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_STYLE)
        String platform;

        @MarginaliaTemplateDescription(loc = M_L.DESC_TEMPLATE_SUMMARIES)
        String application;

        String none;

        @Incomplete(humanName = L.LABEL_CLOSE)
        public void incomplete() {
        }
    }

    @Test
    void theTemplateDescriptionOfEitherLayerIsRead() throws Exception {
        assertThat(LocalizedAnnotations.templateDescription(Data.class.getDeclaredField("platform"))).isEqualTo(L.DESC_TEMPLATE_STYLE);
        assertThat(LocalizedAnnotations.templateDescription(Data.class.getDeclaredField("application"))).isEqualTo(M_L.DESC_TEMPLATE_SUMMARIES);
        assertThat(LocalizedAnnotations.templateDescription(Data.class.getDeclaredField("none"))).isNull();
    }

    @Test
    void aTextAnnotationOfACommandHasEveryElement() {
        assertThatThrownBy(() -> LocalizedAnnotations.commandTexts(Data.class.getMethod("incomplete")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("without the element description");
    }

    @Test
    void theTextAnnotationsOfTheLayersHaveEveryElement() {
        for (Class<?> type : new Class<?>[]{CommandTexts.class, MarginaliaCommandTexts.class}) {
            assertThat(type.isAnnotationPresent(CommandTextSource.class)).as(type.getSimpleName()).isTrue();
            for (String element : LocalizedAnnotations.COMMAND_TEXT_ELEMENTS) {
                assertThat(type.getDeclaredMethods()).as(type.getSimpleName()).anyMatch(m -> m.getName().equals(element));
            }
        }
    }
}
