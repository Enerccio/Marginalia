package com.github.enerccio.marginalia.templates;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.templates.*;
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
                Defaults.DEFAULT_SUMMARY_PROMPT, Defaults.DEFAULT_META_SUMMARY, Defaults.DEFAULT_REDRAFT_PROMPT, Defaults.DEFAULT_ANNOTATE_PROMPT}) {
            assertThat(templateService.isValidTemplate(template, "default").isValid()).isTrue();
        }
        assertThat(templateService.isValidTemplate(Defaults.DEFAULT_MASTER_TEMPLATE, "default", MasterTemplateData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate(Defaults.DEFAULT_USER_PROMPT, "default", UserPromptData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate(Defaults.DEFAULT_SUMMARY_PROMPT, "default", SummaryTemplateData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate(Defaults.DEFAULT_META_SUMMARY, "default", MetaSummaryTemplateData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate(Defaults.DEFAULT_REDRAFT_PROMPT, "default", RedraftTemplateData.class).unknownNames()).isEmpty();
        assertThat(templateService.isValidTemplate(Defaults.DEFAULT_ANNOTATE_PROMPT, "default", AnnotateTemplateData.class).unknownNames()).isEmpty();
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

    @Test
    void metaSummaryPrompt() throws Exception {
        MetaSummaryTemplateData meta = withContext(new MetaSummaryTemplateData(), true);
        meta.setBackgroundLore("Dragons exist.");
        meta.setSummaryBlocks("First ledger.\n\nSecond ledger.");

        String output = render(Defaults.DEFAULT_META_SUMMARY, meta);

        assertClean(output);
        assertThat(output).contains("=== BACKGROUND LORE ARCHIVE START ===\nDragons exist.")
                .contains("First ledger.\n\nSecond ledger.");
    }

    @Test
    void metaSummaryPromptWithoutLore() throws Exception {
        MetaSummaryTemplateData meta = withContext(new MetaSummaryTemplateData(), false);
        meta.setSummaryBlocks("First ledger.");

        String output = render(Defaults.DEFAULT_META_SUMMARY, meta);

        assertClean(output);
        assertThat(output).doesNotContain("BACKGROUND LORE ARCHIVE START").contains("First ledger.");
    }

    @Test
    void redraftPromptWithAllSections() throws Exception {
        RedraftTemplateData redraft = withContext(new RedraftTemplateData(), true);
        redraft.setBackgroundLore("Dragons exist.");
        redraft.setNarrativePov(Defaults.DEFAULT_POV);
        redraft.setSummaries("Chapter one happened.");
        redraft.setPreviousStory("Alice sailed.");
        redraft.setFollowingStory("Bob waited.");
        redraft.setSceneSetting("the harbour");
        redraft.setText("Alice walked to the harbour.");
        redraft.setRedraftInstructions("Add more dialogue.");

        String output = render(Defaults.DEFAULT_REDRAFT_SYSTEM_PROMPT, redraft) + render(Defaults.DEFAULT_REDRAFT_PROMPT, redraft);

        assertClean(output);
        assertThat(output).contains("Narrative Voice: Third-Person Limited")
                .contains("=== BACKGROUND LORE START ===\nDragons exist.")
                .contains("=== SUMMARY OF THE STORY BEFORE THE PART START ===\nChapter one happened.")
                .contains("=== STORY BEFORE THE PART START ===\nAlice sailed.")
                .contains("=== STORY AFTER THE PART START ===\nBob waited.")
                .contains("Location & Time of the part: the harbour")
                .contains("=== PART TO REDRAFT START ===\nAlice walked to the harbour.\n=== PART TO REDRAFT END ===")
                .contains("=== AUTHOR'S INSTRUCTIONS START ===\nAdd more dialogue.\n=== AUTHOR'S INSTRUCTIONS END ===");
    }

    @Test
    void redraftPromptOmitsEmptySections() throws Exception {
        RedraftTemplateData redraft = withContext(new RedraftTemplateData(), false);
        redraft.setText("Alice walked to the harbour.");

        String output = render(Defaults.DEFAULT_REDRAFT_SYSTEM_PROMPT, redraft) + render(Defaults.DEFAULT_REDRAFT_PROMPT, redraft);

        assertClean(output);
        assertThat(output).doesNotContain("BACKGROUND LORE START").doesNotContain("SUMMARY OF THE STORY")
                .doesNotContain("STORY BEFORE THE PART").doesNotContain("STORY AFTER THE PART")
                .doesNotContain("Location & Time").doesNotContain("Characters present")
                .contains("Alice walked to the harbour.").contains("Write the part again");
    }

    @Test
    void redraftPromptWithTheNotesOfTheAuthor() throws Exception {
        RedraftTemplateData redraft = withContext(new RedraftTemplateData(), false);
        redraft.setText("Alice walked to the harbour.");
        redraft.setAnnotations("- \"walked\": too slow\n- (the whole part): no weather");

        String output = render(Defaults.DEFAULT_REDRAFT_SYSTEM_PROMPT, redraft) + render(Defaults.DEFAULT_REDRAFT_PROMPT, redraft);

        assertClean(output);
        assertThat(output).contains("=== AUTHOR'S NOTES ON PASSAGES OF THE PART START ===\n- \"walked\": too slow")
                .contains("- (the whole part): no weather")
                // the notes are the instructions when the author gave none
                .doesNotContain("Write the part again");
    }

    @Test
    void annotatePromptWithAllSections() throws Exception {
        AnnotateTemplateData annotate = withContext(new AnnotateTemplateData(), true);
        annotate.setBackgroundLore("Dragons exist.");
        annotate.setSummaries("Chapter one happened.");
        annotate.setPreviousStory("Alice sailed.");
        annotate.setFollowingStory("Bob waited.");
        annotate.setText("Alice walked to the harbour.");
        annotate.setAnnotations("- \"harbour\": which one?");
        annotate.setAnnotateInstructions("Flag continuity errors.");
        annotate.setOutputFormat("### OUTPUT FORMAT\nJSON please.");

        String output = render(Defaults.DEFAULT_ANNOTATE_SYSTEM_PROMPT, annotate) + render(Defaults.DEFAULT_ANNOTATE_PROMPT, annotate);

        assertClean(output);
        assertThat(output).contains("=== BACKGROUND LORE START ===\nDragons exist.")
                .contains("=== STORY BEFORE THE PART START ===\nAlice sailed.")
                .contains("=== STORY AFTER THE PART START ===\nBob waited.")
                .contains("=== PART TO REVIEW START ===\nAlice walked to the harbour.\n=== PART TO REVIEW END ===")
                .contains("=== EXISTING NOTES START ===\n- \"harbour\": which one?")
                .contains("=== EDITOR'S TASK START ===\nFlag continuity errors.\n=== EDITOR'S TASK END ===")
                .contains("### OUTPUT FORMAT\nJSON please.");
    }

    @Test
    void annotatePromptOmitsEmptySectionsAndHasATaskOfItsOwn() throws Exception {
        AnnotateTemplateData annotate = withContext(new AnnotateTemplateData(), false);
        annotate.setText("Alice walked to the harbour.");

        String output = render(Defaults.DEFAULT_ANNOTATE_SYSTEM_PROMPT, annotate) + render(Defaults.DEFAULT_ANNOTATE_PROMPT, annotate);

        assertClean(output);
        assertThat(output).doesNotContain("=== EXISTING NOTES START").doesNotContain("BACKGROUND LORE START").doesNotContain("STORY AFTER THE PART")
                .contains("Alice walked to the harbour.").contains("continuity errors");
    }
    @Test
    void impersonatePromptWithAllSections() throws Exception {
        ImpersonateTemplateData impersonate = withContext(new ImpersonateTemplateData(), true);
        impersonate.setBackgroundLore("Dragons exist.");
        impersonate.setSummaries("Chapter one happened.");
        impersonate.setPreviousStory("Alice sailed.");
        impersonate.setSceneSetting("the harbour");
        impersonate.setPovCharacter("Alice");
        impersonate.setPresentCharacters("Alice, Bob");
        impersonate.setInstructionExamples("Alice finds the map.");
        impersonate.setOutline("Chapter 2: the siege");
        impersonate.setImpersonateInstructions("She confronts him.");

        String output = render(Defaults.DEFAULT_IMPERSONATE_PROMPT, impersonate);

        assertClean(output);
        assertThat(output).contains("=== BACKGROUND LORE START ===\nDragons exist.")
                .contains("=== SUMMARY OF THE STORY START ===\nChapter one happened.")
                .contains("=== STORY SO FAR START ===\nAlice sailed.")
                .contains("Location & Time of the next part: the harbour").contains("Point of View Character of the next part: Alice")
                .contains("Characters present in the next part: Alice, Bob")
                .contains("=== EXAMPLES OF THE AUTHOR'S INSTRUCTIONS START ===\nAlice finds the map.")
                .contains("=== PLAN OF THE STORY START ===\nChapter 2: the siege")
                .contains("=== AUTHOR'S GUIDANCE START ===\nShe confronts him.\n=== AUTHOR'S GUIDANCE END ===");
    }

    @Test
    void impersonatePromptOmitsEmptySectionsAndContinuesWithoutGuidance() throws Exception {
        ImpersonateTemplateData impersonate = withContext(new ImpersonateTemplateData(), false);
        impersonate.setPreviousStory("Alice sailed.");

        String output = render(Defaults.DEFAULT_IMPERSONATE_PROMPT, impersonate);

        assertClean(output);
        assertThat(output).doesNotContain("BACKGROUND LORE START").doesNotContain("SUMMARY OF THE STORY START")
                .doesNotContain("EXAMPLES OF THE AUTHOR'S INSTRUCTIONS").doesNotContain("PLAN OF THE STORY")
                .doesNotContain("Location & Time").doesNotContain("Characters present")
                .contains("Alice sailed.").contains("continue the story where it leads");
    }
}
