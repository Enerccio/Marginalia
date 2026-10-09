package com.github.enerccio.marginalia.templates;

import com.github.enerccio.marginalia.domain.service.TemplateService.ValidationResult;
import com.github.enerccio.marginalia.domain.templates.*;
import com.github.jknack.handlebars.HandlebarsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Plain Handlebars features, property resolution, validation and render isolation of {@code TemplateServiceImpl}.
 */
class TemplateServiceTest extends TemplateTestBase {

    @Test
    void rendersDeclaredProperties() throws Exception {
        assertThat(render("POV {{povCharacter}} at {{sceneSetting}}, {{narrativeTense}}"))
                .isEqualTo("POV Alice at A rainy harbour at dusk, Past Tense");
    }

    @Test
    void doesNotEscapeHtml() throws Exception {
        data.setInstructions("<b>\"Run\" & 'hide'</b>");

        assertThat(render("{{instructions}}")).isEqualTo("<b>\"Run\" & 'hide'</b>");
    }

    @Test
    void nullDeclaredPropertyRendersEmpty() throws Exception {
        MasterTemplateData master = new MasterTemplateData();
        master.setTemplateContext(context);

        assertThat(render("[{{backgroundLore}}][{{summaries}}]", master)).isEqualTo("[][]");
    }

    @Test
    void unknownVariableRendersError() throws Exception {
        assertThat(render("[{{noSuchVariable}}]")).isEqualTo("[Error]");
    }

    @Test
    void contextPropertiesAreAvailableInEveryTemplate() throws Exception {
        MasterTemplateData master = new MasterTemplateData();
        master.setTemplateContext(context);

        assertThat(render("{{povCharacter}} / {{manuscriptName}} / {{manuscriptDescription}} / {{presentCharacters}}", master))
                .isEqualTo("Alice / Harbour Lights / A smuggling story / Alice, Bob, Carol");
    }

    @Test
    void declaredPropertyWinsOverContext() throws Exception {
        MasterTemplateData master = new MasterTemplateData();
        master.setStyle("Pulp");
        master.setTemplateContext(context);

        assertThat(render("{{style}}", master)).isEqualTo("Pulp");
        master.setStyle(null);
        assertThat(render("{{style}}", master)).isEqualTo("Noir");
    }

    @Test
    void templateContextIsNotExposed() throws Exception {
        assertThat(render("[{{templateContext}}]")).isEqualTo("[Error]");
        assertThat(render("[{{templateContext.variables}}][{{hasProperty}}][{{variables}}]")).isEqualTo("[Error][Error][Error]");
    }

    @Test
    void argumentlessMacrosWorkAsHandlebarsValues() throws Exception {
        assertThat(render("{{#if user}}{{user}}{{/if}}|{{#lastMessage}}last: {{.}}{{/lastMessage}}|{{#unless summary}}x{{else}}has summary{{/unless}}"))
                .isEqualTo("Alice|last: Alice waited on the pier.|has summary");
        context.setPovCharacter(null);
        assertThat(render("{{#if user}}WRONG{{else}}no pov{{/if}}")).isEqualTo("no pov");
    }

    @Test
    void sectionsAndInvertedSections() throws Exception {
        String template = "{{#povCharacter}}POV: {{.}}{{/povCharacter}}{{^povCharacter}}no POV{{/povCharacter}}";

        assertThat(render(template)).isEqualTo("POV: Alice");
        data.setPovCharacter("");
        context.setPovCharacter(null);
        assertThat(render(template)).isEqualTo("no POV");
    }

    @Test
    void handlebarsIfElse() throws Exception {
        String template = "{{#if sceneSetting}}at {{sceneSetting}}{{else}}nowhere{{/if}}";

        assertThat(render(template)).isEqualTo("at A rainy harbour at dusk");
        data.setSceneSetting(null);
        context.setSceneSetting(null);
        assertThat(render(template)).isEqualTo("nowhere");
    }

    @Test
    void handlebarsCommentsAndEscapes() throws Exception {
        assertThat(render("a{{!-- hidden {{user}} --}}b{{! short }}c")).isEqualTo("abc");
        assertThat(render("\\{{povCharacter}}")).isEqualTo("{{povCharacter}}");
        assertThat(render("{{{instructions}}}")).isEqualTo("Alice meets the smuggler");
    }

    @Test
    void textWithoutTagsIsUnchanged() throws Exception {
        String text = "Plain text with { single } braces, <tags>, \\ backslashes and ünïcödé.\n\tTabbed.";

        assertThat(render(text)).isEqualTo(text);
        assertThat(render("")).isEmpty();
    }

    @Test
    void nullTemplateRendersEmpty() throws Exception {
        assertThat(templateService.processTemplate(null, "test", data)).isEmpty();
    }

    @Test
    void sameTemplateWithDifferentDataIsNotCachedAsOutput() throws Exception {
        String template = "{{user}} - {{instructions}}";
        assertThat(render(template)).isEqualTo("Alice - Alice meets the smuggler");

        LorebookTemplateData other = new LorebookTemplateData();
        TemplateContext otherContext = newContext();
        otherContext.setPovCharacter("Bob");
        other.setInstructions("Bob hides");
        other.setTemplateContext(otherContext);

        assertThat(render(template, other)).isEqualTo("Bob - Bob hides");
        assertThat(render(template)).isEqualTo("Alice - Alice meets the smuggler");
    }

    @Test
    void macroArgumentsCannotInjectHandlebars() throws Exception {
        // arguments go through a literal table, never into the handlebars source
        assertThat(render("{{setvar::x::a \"quoted\" ) ( value}}{{getvar::x}}")).isEqualTo("a \"quoted\" ) ( value");
        // values are never evaluated as templates
        data.setInstructions("{{#each}} {{user}} {{/if}}");
        assertThat(render("{{setvar::x::{{instructions}}}}{{getvar::x}} | {{instructions}}"))
                .isEqualTo("{{#each}} {{user}} {{/if}} | {{#each}} {{user}} {{/if}}");
    }

    @Test
    void validation() throws Exception {
        assertThat(isValid("Hello {{user}} {{#if sceneSetting}}x{{/if}}")).isTrue();
        assertThat(isValid("{{if user}}unterminated st-if is kept as text")).isTrue();
        assertThat(isValid("{{#each}} {{user}}")).isFalse();
        assertThat(isValid("{{#if user}} never closed")).isFalse();
        assertThat(isValid("{{/if}}")).isFalse();

        ValidationResult empty = templateService.isValidTemplate("  ", "test");
        assertThat(empty.isValid()).isFalse();
        assertThat(empty.errorMessage()).isNotBlank();
        assertThat(templateService.isValidTemplate(null, "test").isValid()).isFalse();

        ValidationResult broken = templateService.isValidTemplate("{{#if user}}", "test");
        assertThat(broken.errorMessage()).isNotBlank();
    }

    @Test
    void validationReportsUnknownNames() throws Exception {
        ValidationResult result = templateService.isValidTemplate("""
                {{instruction}} {{povCharachter}} {{{style}}} {{manuscriptName}} {{user}} {{lastMessage}}
                {{#if sceneSetting}}{{scenario}}{{else}}{{typoInElse}}{{/if}}
                {{#each storyMessages}}{{this}} {{.}} {{@index}}{{/each}}
                {{templateContext.variables}} {{getvar::x}} {{.local}} {{$global}} {{noSuchMacro::a}}""",
                "userPrompt", UserPromptData.class);

        assertThat(result.isValid()).isTrue();
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.unknownNames()).containsExactly("instruction", "povCharachter", "typoInElse", "templateContext.variables");
    }

    @Test
    void unknownNamesDependOnTemplateData() throws Exception {
        String template = "{{backgroundLore}} {{instructions}}";

        assertThat(templateService.isValidTemplate(template, "masterTemplate", MasterTemplateData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate(template, "summaryPrompt", SummaryTemplateData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate("{{text}}", "masterTemplate", MasterTemplateData.class).unknownNames())
                .containsExactly("text");
        assertThat(templateService.isValidTemplate("{{noSuchVariable}}", "test").unknownNames()).isEmpty();
    }

    @Test
    void brokenTemplateThrowsOnRender() {
        assertThatThrownBy(() -> render("{{#each}} {{user}}")).isInstanceOf(HandlebarsException.class);
    }

    @Test
    void variablesAreSharedThroughData() throws Exception {
        render("{{setvar::mood::tense}}{{.count = 1}}");

        assertThat(render("{{getvar::mood}} {{.count}}")).isEqualTo("tense 1");
    }

    @Test
    void variablesAreSharedThroughContextAcrossTemplateTypes() throws Exception {
        render("{{setvar::fromLore::yes}}{{setglobalvar::world::Harbour}}");

        UserPromptData userPrompt = new UserPromptData();
        userPrompt.setTemplateContext(context);
        MasterTemplateData master = new MasterTemplateData();
        master.setTemplateContext(context);

        assertThat(render("{{getvar::fromLore}}", userPrompt)).isEqualTo("yes");
        assertThat(render("{{$world}}", master)).isEqualTo("Harbour");
    }

    @Test
    void forkedContextDoesNotLeakVariables() throws Exception {
        render("{{.hp = 10}}");
        LorebookTemplateData dry = new LorebookTemplateData();
        dry.setTemplateContext(context.fork());

        assertThat(render("{{.hp -= 3}}{{.hp}}{{.added = 1}}", dry)).isEqualTo("7");

        assertThat(render("{{.hp}} [{{.added}}]")).isEqualTo("10 []");
    }

    @Test
    void separateContextsAreIsolated() throws Exception {
        render("{{setvar::secret::1}}");
        LorebookTemplateData other = new LorebookTemplateData();
        other.setTemplateContext(newContext());

        assertThat(render("[{{getvar::secret}}]", other)).isEqualTo("[]");
    }
}
