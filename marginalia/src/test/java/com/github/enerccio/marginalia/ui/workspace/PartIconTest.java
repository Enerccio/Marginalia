package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The icons of the parts of the desktop workspace are well formed SVG on a 24 x 24 grid, each one different.
 */
class PartIconTest {

    @Test
    void everyIconIsWellFormedSvg() throws Exception {
        for (PartIcon icon : PartIcon.values()) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(icon.svg().getBytes(StandardCharsets.UTF_8)));
            assertThat(doc.getDocumentElement().getTagName()).as(icon.name()).isEqualTo("svg");
            assertThat(doc.getDocumentElement().getAttribute("viewBox")).as(icon.name()).isEqualTo("0 0 24 24");
            assertThat(doc.getDocumentElement().getChildNodes().getLength()).as(icon.name()).isPositive();
        }
    }

    @Test
    void theIconsDiffer() {
        assertThat(java.util.Arrays.stream(PartIcon.values()).map(PartIcon::markup).distinct().count())
                .isEqualTo(PartIcon.values().length);
    }

    @Test
    void theComponentCarriesTheStyleClassAndTheDrawing() {
        Span span = PartIcon.TRASH.create();

        assertThat(span.getClassNames()).contains(CssStyles.WORKSPACE_PART_ICON);
        assertThat(span.getElement().getProperty("innerHTML")).isEqualTo(PartIcon.TRASH.svg());
    }

}
