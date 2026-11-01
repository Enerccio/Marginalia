package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The templates the exporters render, which the user can change in the settings.
 */
class ExporterTemplateTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    private Exporter exporter(String extension) {
        return exporterService.getExporters().stream().filter(e -> e.getFileExtension().equals(extension)).findFirst().orElseThrow();
    }

    private String export(String extension, Map<String, String> templates) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse("The text.");
        ExportOptions options = new ExportOptions("The Book", "An Author", true, "en", null, templates);
        return new String(exporter(extension).export(options, List.of(message), new ProgressBarDialog(false)), StandardCharsets.UTF_8);
    }

    @Test
    void theFormatsWithATitlePageHaveItsTemplate() {
        for (Exporter exporter : exporterService.getExporters()) {
            if (List.of("txt", "md", "html", "docx", "pdf", "epub").contains(exporter.getId())) {
                assertThat(exporter.getTemplates()).as(exporter.getName()).extracting(ExporterService.ExportTemplate::key).containsExactly("titlePage");
            }
        }
    }

    @Test
    void theDefaultTemplateIsUsedUnlessTheUserChangedIt() throws Exception {
        assertThat(export("md", Map.of())).startsWith("# The Book\n\n*An Author*");
        assertThat(export("md", null)).startsWith("# The Book");
        assertThat(export("md", Map.of("titlePage", "   "))).as("a blank text is no change").startsWith("# The Book");
    }

    @Test
    void theChangedTemplateIsRenderedWithTheDetailsOfTheBook() throws Exception {
        Map<String, String> changed = Map.of("titlePage", "## {{title}}\n\nwritten by {{author}}");

        assertThat(export("md", changed)).startsWith("## The Book\n\nwritten by An Author").doesNotContain("*An Author*");
        assertThat(export("txt", changed)).startsWith("The Book\n\nwritten by An Author");
        assertThat(export("html", changed)).contains("written by An Author");
    }

    @Test
    void aTemplateForAnotherKeyChangesNothing() throws Exception {
        assertThat(export("md", Map.of("other", "x"))).startsWith("# The Book");
    }
}
