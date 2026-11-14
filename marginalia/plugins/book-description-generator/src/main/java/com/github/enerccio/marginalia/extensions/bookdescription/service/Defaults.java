package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

public class Defaults {

    public String descriptionPrompt() {
        return """
[ You are an experienced book editor who writes the titles and the blurbs of books. Your task is to read a story that has already been written and give the book it belongs to a name and a description. ]

### The book now
Name: {{bookName}}
{{#if bookDescription}}Description: {{bookDescription}}{{/if}}

### The story
{{#if fromNotes}}The story is too long to be given in full. Below are the notes made of it, part by part, in the order the story is read.{{else}}Below is the story as it was written, in the order it is read. A part of it may be given as the summary that stands in for the messages it covers.{{/if}}

{{story}}

### Guidelines
1. Describe the book that is really there. Do not invent events, characters or places and do not continue the story.
2. The name is short and memorable, in the language of the story. It does not give away the ending. Do not just repeat the name the book has now unless it is really the best one.
3. The description is what a reader browsing a library sees: it introduces the setting and the main characters, sets up the central conflict and the stakes, and makes the reader want to read on. Two or three short paragraphs. Do not spoil the ending, do not list the chapters, do not address the reader as "you".
4. Write in the language of the story, in the tone of the story.
""";
    }

    public String notesPrompt() {
        return """
[ You are a careful story analyst. Your task is to read one part of a long story and write notes that someone who has not read it can use to write the description of the whole book. ]

### The book now
Name: {{bookName}}
{{#if bookDescription}}Description: {{bookDescription}}{{/if}}

### The story, part {{part}} of {{parts}}
{{story}}

### Guidelines
1. Write dense notes about what really happens in this part, in the order it happens. Do not invent anything and do not continue the story.
2. Name the characters (by the names used in the story) with who they are and what they want, the places, the central conflict of this part and how this part ends.
3. Add the genre, the tone and the themes that show in this part.
4. Keep what a blurb needs and leave out the minor detail. Use short paragraphs or bullet points, no headings.
5. Answer only with the notes. No introduction, no commentary.
""";
    }

    /**
     * What is added after the prompt so the answer can be read: the prompts say what to write, the format is up to the dialog.
     *
     * @param format the format the answer is asked for; {@link OutputFormat#AUTO} is the one with the schema
     */
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### Output\nAnswer only with the JSON object requested by the response format: the \"name\" of the book and its \"description\". No introduction, no commentary.";
            case JSON_NO_SCHEMA -> "### Output\nAnswer only with a JSON object of exactly this shape, nothing before it and nothing after it:\n"
                    + "{\"name\": \"<the name of the book>\", \"description\": \"<the description of the book>\"}";
            case MARKDOWN -> "### Output\nAnswer in Markdown, nothing else: the first line is the name of the book as a heading (# Name of the book), "
                    + "everything below it is the description of the book. No introduction, no commentary.";
        };
    }
}
