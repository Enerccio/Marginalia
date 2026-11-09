package com.github.enerccio.marginalia.extensions.bookdescription;

import com.github.enerccio.marginalia.extensions.bookdescription.service.AnswerParser;
import com.github.enerccio.marginalia.extensions.bookdescription.service.AnswerParser.Parsed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The name and the description are read from JSON and from Markdown, whichever the model wrote.
 */
class AnswerParserTest {

    @Test
    void readsAJsonObject() {
        Parsed parsed = AnswerParser.parse("{\"name\": \"The Glass Tide\", \"description\": \"A harbor town.\\n\\nA storm.\"}");
        assertEquals("The Glass Tide", parsed.name());
        assertEquals("A harbor town.\n\nA storm.", parsed.description());
    }

    @Test
    void readsJsonInAFenceAndAfterAnIntroduction() {
        assertEquals("A", AnswerParser.parse("```json\n{\"name\":\"A\",\"description\":\"B\"}\n```").name());
        assertEquals("B", AnswerParser.parse("Here is the result:\n{\"name\":\"A\",\"description\":\"B\"}").description());
    }

    @Test
    void skipsTheThinkingOfTheModel() {
        Parsed parsed = AnswerParser.parse("<think>{\"name\": \"wrong\", \"description\": \"wrong\"}</think>\n{\"name\":\"Right\",\"description\":\"Yes\"}");
        assertEquals("Right", parsed.name());
        assertNull(AnswerParser.parse("<think>still thinking about the na"));
    }

    @Test
    void readsMarkdownWithAHeadingForTheName() {
        Parsed parsed = AnswerParser.parse("# The Glass Tide\n\nA harbor town sinks.\n\nA storm comes.");
        assertEquals("The Glass Tide", parsed.name());
        assertEquals("A harbor town sinks.\n\nA storm comes.", parsed.description());
    }

    @Test
    void readsBoldAndQuotedNames() {
        assertEquals("Glass", AnswerParser.parse("**Glass**\n\nText").name());
        assertEquals("Glass", AnswerParser.parse("## \"Glass\"\nText").name());
    }

    @Test
    void readsLabelledMarkdown() {
        Parsed parsed = AnswerParser.parse("**Name:** The Glass Tide\n\n**Description:** A harbor town sinks.\nA storm comes.");
        assertEquals("The Glass Tide", parsed.name());
        assertEquals("A harbor town sinks.\nA storm comes.", parsed.description());

        parsed = AnswerParser.parse("Title: The Glass Tide\n\n## Description\n\nA harbor town sinks.");
        assertEquals("The Glass Tide", parsed.name());
        assertEquals("A harbor town sinks.", parsed.description());
    }

    @Test
    void aLineThatOnlyStartsWithTheWordDescriptionIsPartOfTheDescription() {
        Parsed parsed = AnswerParser.parse("# Glass\n\nDescription of the tide matters here.");
        assertEquals("Description of the tide matters here.", parsed.description());
    }

    @Test
    void anAnswerWithoutADescriptionIsNotReadable() {
        assertNull(AnswerParser.parse("# Only a name"));
        assertNull(AnswerParser.parse("{\"name\": \"Only a name\"}"));
        assertNull(AnswerParser.parse("   "));
        assertNull(AnswerParser.parse(null));
    }

    @Test
    void jsonThatIsBrokenFallsBackToTheText() {
        // not JSON at all: the first line is the name
        Parsed parsed = AnswerParser.parse("{not json\nsome text");
        assertNotNull(parsed);
        assertEquals("{not json", parsed.name());
    }
}
