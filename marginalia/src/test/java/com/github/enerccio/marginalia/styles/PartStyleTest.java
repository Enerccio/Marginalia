package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A text style as data: the stylesheet it makes, the built in styles of the registry, and the stored (JSON) form.
 */
class PartStyleTest {

    private static PartStyleRegistry registry() {
        PartStyleRegistry registry = new PartStyleRegistry();
        registry.afterPropertiesSet();
        return registry;
    }

    @Test
    void theRegistryHasTheNormalAndTheBookStyle() {
        PartStyleRegistry registry = registry();

        assertThat(registry.getAll()).extracting(PartStyle::getId).containsExactly(PartStyleRegistry.NORMAL, PartStyleRegistry.BOOK);
        assertThat(registry.getAll()).allMatch(PartStyle::isBuiltin);
        assertThat(registry.isBuiltin("book")).isTrue();
        assertThat(registry.isBuiltin("someone-elses")).isFalse();
    }

    @Test
    void theNormalStyleMakesNoStylesheet() {
        PartStyle normal = registry().find(PartStyleRegistry.NORMAL);

        assertThat(normal.hasContent()).isFalse();
        assertThat(normal.toCss()).isEmpty();
    }

    @Test
    void theBookStyleKeepsTheLookOfTheReadingMode() {
        String css = registry().find(PartStyleRegistry.BOOK).toCss();

        assertThat(css).contains(".part-style-book .chat-message-markdown {");
        assertThat(css).contains("font-family: \"Garamond\", \"Georgia\", \"Times New Roman\", serif;");
        assertThat(css).contains("line-height: 1.8;");
        assertThat(css).contains("text-align: justify;");
        assertThat(css).contains("hyphens: auto;");
        assertThat(css).contains("font-variant: small-caps;");
        assertThat(css).contains("content: \"•  •  •\";");
        assertThat(css).contains("border-left: 3px solid var(--lumo-primary-color, #7e57c2);");
    }

    @Test
    void anOptionThatIsNotSetLeavesTheLookOfTheApplication() {
        PartStyle style = new PartStyle("s1", "Small");
        style.set(PartStyleOption.FONT_SIZE, "0.9rem");

        String css = style.toCss();

        assertThat(css).contains("font-size: 0.9rem;");
        assertThat(css).doesNotContain("font-family: \"Garamond\"");
        assertThat(css).doesNotContain("hyphens: auto");
        assertThat(css).doesNotContain("small-caps");
    }

    @Test
    void aValueCanNotLeaveItsDeclaration() {
        PartStyle style = new PartStyle("s1", "Evil");
        style.set(PartStyleOption.TEXT_COLOR, "red; } body { display: none");
        style.set(PartStyleOption.SCENE_BREAK_TEXT, "\"; } *{color:red");

        String css = style.toCss();

        assertThat(css).doesNotContain("body {");
        assertThat(css).contains("content: \"\\\"; } *{color:red\";");
    }

    @Test
    void theCustomCssComesLastInTheScopeOfTheStyle() {
        PartStyle style = new PartStyle("s1", "Indented");
        style.setCustomCss(".chat-message-markdown p { text-indent: 1.5em; }");

        String css = style.toCss();

        assertThat(css).endsWith(".part-style-s1 {\n.chat-message-markdown p { text-indent: 1.5em; }\n}\n");
        assertThat(style.hasContent()).isTrue();
    }

    @Test
    void aStyleWithOnlyCustomCssHasContent() {
        PartStyle style = new PartStyle("s1", "Css");
        assertThat(style.hasContent()).isFalse();
        style.setCustomCss("   ");
        assertThat(style.hasContent()).isFalse();
        style.setCustomCss("color: red;");
        assertThat(style.hasContent()).isTrue();
    }

    @Test
    void bracesOfTheCustomCssMustMatch() {
        assertThat(PartStyle.isBalanced("p { a: b; } q { c: d; }")).isTrue();
        assertThat(PartStyle.isBalanced("p { & q { a: b; } }")).isTrue();
        assertThat(PartStyle.isBalanced("p { a: b; } }")).isFalse();
        assertThat(PartStyle.isBalanced("p { a: b; ")).isFalse();
        assertThat(PartStyle.isBalanced("} p {")).isFalse();
    }

    @Test
    void aCopyIsNotBuiltInAndSharesNothing() {
        PartStyle book = registry().find(PartStyleRegistry.BOOK);

        PartStyle copy = book.copy("c1", "My book");
        copy.set(PartStyleOption.FONT_SIZE, "2rem");

        assertThat(copy.isBuiltin()).isFalse();
        assertThat(copy.getId()).isEqualTo("c1");
        assertThat(copy.get(PartStyleOption.FONT_SIZE)).isEqualTo("2rem");
        assertThat(book.get(PartStyleOption.FONT_SIZE)).isEqualTo("1.04rem");
        assertThat(copy.getCssClass()).isEqualTo("part-style-c1");
    }

    @Test
    void theStoredFormRoundTripsAndForgetsWhatIsNotStored() {
        PartStyle style = new PartStyle("s1", "Mine");
        style.set(PartStyleOption.HYPHENATION, "true").set(PartStyleOption.FONT_SIZE, " 1rem ");
        style.setCustomCss("p { color: red; }");

        String json = new Gson().toJson(style);
        PartStyle back = new Gson().fromJson(json, PartStyle.class);

        assertThat(json).doesNotContain("builtin");
        assertThat(back.getId()).isEqualTo("s1");
        assertThat(back.getName()).isEqualTo("Mine");
        assertThat(back.get(PartStyleOption.FONT_SIZE)).isEqualTo("1rem");
        assertThat(back.getBoolean(PartStyleOption.HYPHENATION)).isTrue();
        assertThat(back.getCustomCss()).isEqualTo("p { color: red; }");
        assertThat(back.toCss()).isEqualTo(style.toCss());
    }

    @Test
    void extensionsRegisterTheirStylesAndTakeThemBack() {
        PartStyleRegistry registry = registry();
        PartStyle sepia = new PartStyle("plugin-sepia", "Sepia").set(PartStyleOption.TEXT_COLOR, "#704214");

        registry.register("plugin", sepia);

        assertThat(registry.find("plugin-sepia").isBuiltin()).isTrue();
        assertThat(registry.find("plugin-sepia").get(PartStyleOption.TEXT_COLOR)).isEqualTo("#704214");

        registry.unregisterAll("plugin");

        assertThat(registry.find("plugin-sepia")).isNull();
        assertThat(registry.find(PartStyleRegistry.BOOK)).isNotNull();
    }
}
