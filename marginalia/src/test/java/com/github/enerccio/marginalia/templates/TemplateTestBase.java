package com.github.enerccio.marginalia.templates;

import com.github.enerccio.marginalia.domain.service.impl.TemplateServiceImpl;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Renders templates through the real {@link TemplateServiceImpl} (macro translation + Handlebars) without Spring.
 * <p>
 * The context is deterministic: the clock is fixed at {@link #NOW} (a Sunday, UTC) and random macros use a seeded
 * {@link Random}. {@link #data} is shared by all {@link #render} calls of a test, like the lorebook entries of one
 * generation share their data object.
 */
public abstract class TemplateTestBase {

    /**
     * Sunday, March 15, 2026, 14:30:45 UTC.
     */
    protected static final Instant NOW = Instant.parse("2026-03-15T14:30:45Z");

    protected TemplateServiceImpl templateService;
    protected TemplateContext context;
    protected LorebookTemplateData data;

    @BeforeEach
    void setUpTemplates() throws Exception {
        templateService = new TemplateServiceImpl();
        templateService.afterPropertiesSet();

        context = newContext();
        data = new LorebookTemplateData();
        data.setPovCharacter(context.getPovCharacter());
        data.setSceneSetting(context.getSceneSetting());
        data.setPresentCharacters(context.getPresentCharacters());
        data.setInstructions(context.getInstructions());
        data.setNarrativePov(context.getNarrativePov());
        data.setNarrativeTense(context.getNarrativeTense());
        data.setStyle(context.getStyle());
        data.setTemplateContext(context);
    }

    /**
     * Context of a generation in the middle of a story.
     */
    protected static TemplateContext newContext() {
        TemplateContext context = new TemplateContext();
        context.setPovCharacter("Alice");
        context.setPresentCharacters("Alice, Bob, Carol");
        context.setSceneSetting("A rainy harbour at dusk");
        context.setInstructions("Alice meets the smuggler");
        context.setNarrativePov("Third-Person Limited");
        context.setNarrativeTense("Past Tense");
        context.setStyle("Noir");
        context.setManuscriptName("Harbour Lights");
        context.setManuscriptDescription("A smuggling story");
        context.setModelName("mock-model");
        context.setMaxContextTokens(8192);
        context.setMaxResponseTokens(1024);
        context.setGenerationType("normal");
        context.setStoryMessages(List.of("The ship arrived.", "Alice waited on the pier."));
        context.setLastInstructions("Alice waits for the ship");
        context.setLastMessageTime(Date.from(NOW.minusSeconds(2 * 60 * 60)));
        context.setLatestSummary("Alice came to the harbour.");
        context.setPickSeed("manuscript-1");
        context.setClock(Clock.fixed(NOW, ZoneOffset.UTC));
        context.setRandom(new Random(42));
        return context;
    }

    protected String render(String template) throws Exception {
        return render(template, data);
    }

    protected String render(String template, TemplateData values) throws Exception {
        return templateService.processTemplate(template, "test", values);
    }

    protected boolean isValid(String template) throws Exception {
        return templateService.isValidTemplate(template, "test").isValid();
    }
}
