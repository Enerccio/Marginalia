package com.github.enerccio.marginalia.templates;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.domain.templates.SummaryTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.UserPromptData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The built-in templates must be valid and render cleanly both with full data and with nothing filled in.
 */
class DefaultTemplatesTest extends TemplateTestBase {

    private static <T extends TemplateData> T withContext(T data, boolean full) {
        data.setTemplateContext(full ? newContext() : new com.github.enerccio.marginalia.domain.templates.TemplateContext());
        return data;
    }

    private static void assertClean(String output) {
        assertThat(output).doesNotContain("{{").doesNotContain("}}").doesNotContain("Error");
    }

    @Test
    void defaultsAreValid() throws Exception {
        for (String template : new String[]{Defaults.DEFAULT_MASTER_TEMPLATE, Defaults.DEFAULT_USER_PROMPT,
                Defaults.DEFAULT_SUMMARY_PROMPT}) {
            assertThat(templateService.isValidTemplate(template, "default").isValid()).isTrue();
        }
    }

    @Test
    void masterTemplateWithAllSections() throws Exception {
        MasterTemplateData master = withContext(new MasterTemplateData(), true);
        master.setBackgroundLore("Dragons exist.");
        master.setNarrativePov(Defaults.DEFAULT_POV);
        master.setNarrativeTense(Defaults.DEFAULT_TENSE);
        master.setStyle(Defaults.DEFAULT_STYLE);
        master.setSummaries("Chapter one happened.");

        String output = render(Defaults.DEFAULT_MASTER_TEMPLATE, master);

        assertClean(output);
        assertThat(output)
                .contains("===== WORLD & LORE CONTEXT =====\nDragons exist.")
                .contains("Narrative Voice: Third-Person Limited")
                .contains("Tense: Past Tense")
                .contains("Style & Tone Guidelines:\n" + Defaults.DEFAULT_STYLE)
                .contains("===== SUMMARY OF STORY SO FAR =====\nChapter one happened.");
    }

    @Test
    void masterTemplateOmitsEmptySections() throws Exception {
        MasterTemplateData master = withContext(new MasterTemplateData(), false);

        String output = render(Defaults.DEFAULT_MASTER_TEMPLATE, master);

        assertClean(output);
        assertThat(output).contains("===== SYSTEM ROLE =====")
                .doesNotContain("WORLD & LORE CONTEXT")
                .doesNotContain("Narrative Voice:")
                .doesNotContain("SUMMARY OF STORY SO FAR");
    }

    @Test
    void masterTemplateFallsBackToContextStyle() throws Exception {
        // MasterTemplateData without explicit style uses the generation context
        MasterTemplateData master = withContext(new MasterTemplateData(), true);

        String output = render(Defaults.DEFAULT_MASTER_TEMPLATE, master);

        assertThat(output).contains("Narrative Voice: Third-Person Limited").contains("Style & Tone Guidelines:\nNoir");
    }

    @Test
    void userPrompt() throws Exception {
        UserPromptData prompt = withContext(new UserPromptData(), true);
        prompt.setPovCharacter("Alice");
        prompt.setSceneSetting("Harbour");
        prompt.setPresentCharacters("Bob");
        prompt.setInstructions("Alice & Bob talk about \"cargo\"");

        String output = render(Defaults.DEFAULT_USER_PROMPT, prompt);

        assertClean(output);
        assertThat(output)
                .contains("Point of View Character: Alice")
                .contains("Current Location & Time: Harbour")
                .contains("Characters Currently Present / State: Bob")
                .contains("**Plot Specifications for the Next Part:**\nAlice & Bob talk about \"cargo\"");
    }

    @Test
    void userPromptWithoutInput() throws Exception {
        UserPromptData prompt = withContext(new UserPromptData(), false);

        String output = render(Defaults.DEFAULT_USER_PROMPT, prompt);

        assertClean(output);
        assertThat(output).doesNotContain("Point of View Character").doesNotContain("Plot Specifications")
                .contains("**Strict Execution Constraints:**");
    }

    @Test
    void summaryPrompt() throws Exception {
        SummaryTemplateData summary = withContext(new SummaryTemplateData(), true);
        summary.setBackgroundLore("Dragons exist.");
        summary.setText("Alice walked to the harbour.");

        String output = render(Defaults.DEFAULT_SUMMARY_PROMPT, summary);

        assertClean(output);
        assertThat(output).contains("=== BACKGROUND LORE ARCHIVE START ===\nDragons exist.")
                .contains("Alice walked to the harbour.");
    }

    @Test
    void summaryPromptWithoutLore() throws Exception {
        SummaryTemplateData summary = withContext(new SummaryTemplateData(), false);
        summary.setText("Alice walked to the harbour.");

        String output = render(Defaults.DEFAULT_SUMMARY_PROMPT, summary);

        assertClean(output);
        assertThat(output).doesNotContain("BACKGROUND LORE ARCHIVE START").contains("Alice walked to the harbour.");
    }
}
