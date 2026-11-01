package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.domain.styles.PartStyleTransfer;
import com.github.enerccio.marginalia.domain.styles.PartStyleTransfer.ImportException;
import com.github.enerccio.marginalia.domain.styles.PartStyleTransfer.Imported;
import com.github.enerccio.marginalia.domain.styles.PartStyleTransfer.Reason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The JSON file of text styles: what it holds, and what an import accepts.
 */
class PartStyleTransferTest {

    private static PartStyle sample() {
        return new PartStyle("old-id", "Horror").set(PartStyleOption.FONT_FAMILY, "\"Garamond\", serif")
                .set(PartStyleOption.BACKGROUND_COLOR, "#101010").set(PartStyleOption.DROP_CAP, "true").setForExport(true);
    }

    @Test
    void aStyleSurvivesTheRoundTripWithANewId() {
        PartStyle style = sample();
        style.setCustomCss("& blockquote { border-left: 4px solid #600; }\n& a:hover { color: #c00; }");

        Imported imported = PartStyleTransfer.fromJson(PartStyleTransfer.toJson(List.of(style)));

        assertThat(imported.styles()).hasSize(1);
        PartStyle back = imported.styles().getFirst();
        assertThat(back.getId()).isNotEqualTo("old-id").isNotBlank();
        assertThat(back.isBuiltin()).isFalse();
        assertThat(back.getName()).isEqualTo("Horror");
        assertThat(back.isForExport()).isTrue();
        assertThat(back.getOptions()).isEqualTo(style.getOptions());
        assertThat(back.getCustomCss()).isEqualTo(style.getCustomCss());
        assertThat(back.toCss()).isEqualTo(style.copy(back.getId(), "Horror").toCss());
        assertThat(imported.ignoredOptions()).isZero();
    }

    @Test
    void severalStylesAndTheBuiltInOnesAreExportedInOneFile() {
        PartStyleRegistry registry = new PartStyleRegistry();
        registry.afterPropertiesSet();

        String json = PartStyleTransfer.toJson(List.of(registry.find(PartStyleRegistry.BOOK), sample()));
        Imported imported = PartStyleTransfer.fromJson(json);

        assertThat(imported.styles()).hasSize(2).noneMatch(PartStyle::isBuiltin);
        assertThat(imported.styles().getFirst().getOptions()).isEqualTo(registry.find(PartStyleRegistry.BOOK).getOptions());
        assertThat(json).contains("\"format\": \"marginalia-text-style\"").contains("\"version\": 1").doesNotContain("old-id");
    }

    @Test
    void anOptionThisVersionDoesNotKnowIsLeftOutAndCounted() {
        Imported imported = PartStyleTransfer.fromJson("""
                {"format":"marginalia-text-style","version":1,"styles":[
                  {"name":"New","options":{"FONT_SIZE":"2rem","FROM_THE_FUTURE":"1","ALSO_NEW":"x"}}]}""");

        assertThat(imported.styles().getFirst().getOptions()).containsOnlyKeys("FONT_SIZE");
        assertThat(imported.ignoredOptions()).isEqualTo(2);
    }

    @Test
    void aSingleStyleObjectIsTakenAndAMissingNameGetsOne() {
        Imported imported = PartStyleTransfer.fromJson("{\"options\":{\"TEXT_COLOR\":\"#222\"}}");

        assertThat(imported.styles()).hasSize(1);
        assertThat(imported.styles().getFirst().getName()).isNotBlank();
        assertThat(imported.styles().getFirst().get(PartStyleOption.TEXT_COLOR)).isEqualTo("#222");
    }

    @Test
    void aFileThatIsNotStylesIsRefused() {
        for (String bad : new String[]{"", "not json", "[]", "42", "{}", "{\"styles\":[]}", "{\"styles\":5}",
                "{\"format\":\"something-else\",\"styles\":[{\"name\":\"x\"}]}", "{\"version\":\"two\",\"styles\":[{\"name\":\"x\"}]}"}) {
            assertThatThrownBy(() -> PartStyleTransfer.fromJson(bad)).as(bad).isInstanceOfSatisfying(ImportException.class,
                    e -> assertThat(e.getReason()).isEqualTo(Reason.NOT_STYLES));
        }
        assertThatThrownBy(() -> PartStyleTransfer.fromJson(null)).isInstanceOf(ImportException.class);
    }

    @Test
    void aNewerVersionOfTheFormatIsRefused() {
        assertThatThrownBy(() -> PartStyleTransfer.fromJson("{\"format\":\"marginalia-text-style\",\"version\":2,\"styles\":[{\"name\":\"x\"}]}"))
                .isInstanceOfSatisfying(ImportException.class, e -> assertThat(e.getReason()).isEqualTo(Reason.NEWER_VERSION));
    }

    @Test
    void customCssWithUnbalancedBracesIsRefusedWithTheNameOfTheStyle() {
        assertThatThrownBy(() -> PartStyleTransfer.fromJson("{\"styles\":[{\"name\":\"Broken\",\"customCss\":\"} body { color: red; \"}]}"))
                .isInstanceOfSatisfying(ImportException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(Reason.UNBALANCED_CSS);
                    assertThat(e.getStyleName()).isEqualTo("Broken");
                });
    }

    @Test
    void aFileOverTheLimitIsRefused() {
        assertThatThrownBy(() -> PartStyleTransfer.fromJson("x".repeat(PartStyleTransfer.MAX_BYTES + 1))).isInstanceOf(ImportException.class);
    }

    /**
     * The example of the styling guide of the manual, which readers download: it must load, and set every option the editor has.
     */
    @Test
    void theHorrorExampleOfTheManualLoadsAndSetsEveryOption() throws Exception {
        java.nio.file.Path file = java.nio.file.Path.of("..", "manual", "files", "horror-book.style.json");
        Imported imported = PartStyleTransfer.fromJson(java.nio.file.Files.readString(file));

        assertThat(imported.styles()).hasSize(1);
        PartStyle horror = imported.styles().getFirst();
        assertThat(horror.getName()).isEqualTo("Horror book");
        assertThat(imported.ignoredOptions()).isZero();
        for (PartStyleOption option : PartStyleOption.values()) {
            assertThat(horror.get(option)).as(option.name()).isNotNull();
        }
        assertThat(horror.getCustomCss()).isNotBlank();
        assertThat(horror.toCss()).contains("background-color: #0c0a0a;").contains("box-shadow: inset 0 0 140px");
    }
}
