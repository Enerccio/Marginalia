package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.service.LoreImages;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The images in the content of lorebook entries: found, stripped for the model, pointed at other resources.
 */
class LoreImagesTest {

    private static final String A = "0e5a8a6e-1b8f-4f7e-9f00-000000000001";
    private static final String B = "0e5a8a6e-1b8f-4f7e-9f00-000000000002";

    @Test
    void theImagesAreFoundInOrder() {
        String text = "Intro ![Portrait](resource:" + A + ") and ![](resource:" + B + ") ![x](https://example.com/a.png) ![Again](resource:" + A + ")";

        assertThat(LoreImages.references(text)).containsExactly(
                new LoreImages.Reference("Portrait", A), new LoreImages.Reference("", B), new LoreImages.Reference("Again", A));
        assertThat(LoreImages.uuids(text)).containsExactly(A, B);
    }

    @Test
    void theModelReadsTheTextWithoutTheImages() {
        String text = "Varro is a captain.\n\n" + LoreImages.markup("Varro", A) + "\n\nHe drinks. ![web](https://e.com/a.png)";

        assertThat(LoreImages.strip(text)).isEqualTo("Varro is a captain.\n\nHe drinks. ![web](https://e.com/a.png)");
        assertThat(LoreImages.strip("no images")).isEqualTo("no images");
        assertThat(LoreImages.strip(null)).isNull();
    }

    @Test
    void theImagesCanPointAtOtherResources() {
        String text = "![One](resource:" + A + ") ![Two](resource:" + B + ")";

        assertThat(LoreImages.replace(text, Map.of(A, B))).isEqualTo("![One](resource:" + B + ") ![Two](resource:" + B + ")");
        assertThat(LoreImages.replace(text, uuid -> null)).isEqualTo(text);
    }

    @Test
    void theMarkupKeepsTheCaptionOnOneLine() {
        assertThat(LoreImages.markup("a [b]\nc", A)).isEqualTo("![a  b  c](resource:" + A + ")");
    }
}
