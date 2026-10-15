package com.github.enerccio.marginalia.extensions.arcoutline.service;

public class Defaults {

    /**
     * Template of the prompt for generating an arc: {{backgroundLore}}, {{existingArcs}} and {{specification}} are
     * replaced by the lore of the book, its arcs and what the author asked for.
     */
    public static String GENERATION_PROMPT = """
[ You are an experienced story planner. Your task is to design ONE new story arc for the book described below. An arc is a major part of the story with its own premise, conflict and resolution; it will be split into chapters later. ]

### Background lore
{{backgroundLore}}

### Arcs the book already has
{{existingArcs}}

### What the author wants from the new arc
{{specification}}

### Guidelines
1. The new arc follows the arcs that already exist. Continue the story, do not repeat or contradict them, and respect the background lore.
2. Follow the author's wishes above. Where they leave something open, decide it yourself in the way that serves the story best.
3. Give the arc a short, memorable name.
4. The "description" is shown in the outline, so keep it terse: one to three short sentences that say what the arc is about and where it leads, about 60 words at most. Put everything else into the "note": the premise, the main conflict, the turning points, how the arc ends, so the author can divide it into chapters. Mention the characters and places involved by the names used in the lore. Plain text with paragraphs, no headings. Do not repeat the description in the note, extend it.
5. If you need a name for a person, place or thing that the lore does not give, pick a less common one that will not be confused with the existing lore.

### Output
Answer only with the JSON object requested by the response format: the "name", the terse "description" and the detailed "note" of the arc. No introduction, no commentary.
""";

    /**
     * Template of the prompt for generating the chapters of an arc: {{backgroundLore}}, {{outline}},
     * {{numberOfChapters}}, {{generalSpecification}} and {{chapterSpecification}} are replaced by the lore of the
     * book, the outline up to the arc, and what the author asked for.
     */
    public static String CHAPTER_GENERATION_PROMPT = """
[ You are an experienced story planner. Your task is to divide ONE story arc of the book described below into chapters. ]

### Background lore
{{backgroundLore}}

### The outline of the story so far
{{outline}}

The arc marked "<<< WRITE THE CHAPTERS FOR THIS ARC" is the one to divide into chapters. If it already has chapters they are listed under it, the new chapters continue right after the last of them.

### Number of chapters
{{numberOfChapters}}

### What the author wants from the chapters in general
{{generalSpecification}}

### What the author wants from each chapter
{{chapterSpecification}}

### Guidelines
1. The chapters together carry out the description of the marked arc: its premise, conflict, turning points and ending. Do not leave out what the arc promises and do not add a different story.
2. Follow the chapters that already exist and the earlier arcs, do not repeat or contradict them, and respect the background lore.
3. Follow the author's wishes above. A chapter the author describes must be that chapter, in the order given; where they leave something open, decide it yourself in the way that serves the story best.
4. Every chapter has a short, memorable name, a terse "description" and a detailed "note". The "description" is shown in the outline, so keep it to one or two short sentences saying what happens, about 40 words at most. Put everything else into the "note": the scene, the characters and places involved (by the names used in the lore), what happens step by step, what changes and how the chapter ends, so that it leads into the next one. Plain text with paragraphs, no headings. Do not repeat the description in the note, extend it.
5. If you need a name for a person, place or thing that the lore does not give, pick a less common one that will not be confused with the existing lore.

### Output
Answer only with the JSON object requested by the response format: the list of "chapters", each with a "name", a terse "description" and a detailed "note", in the order they are read. No introduction, no commentary.
""";

    /**
     * Template of the prompt for importing an arc from the written story: {{backgroundLore}}, {{existingArcs}},
     * {{story}} and {{numberOfChapters}} are replaced by the lore of the book, its arcs, the chosen messages of the
     * story and what the author asked for.
     */
    public static String IMPORT_PROMPT = """
[ You are an experienced story editor. Your task is to read a part of a story that has already been written and summarize it as ONE story arc divided into chapters, so the author can keep track of it in the outline of the book. ]

### Background lore
{{backgroundLore}}

### Arcs the book already has in its outline
{{existingArcs}}

### The story to summarize
The messages below are the story as it was written, in the order they are read.

{{story}}

### Number of chapters
{{numberOfChapters}}

### Guidelines
1. Describe what really happens in the story above. Do not invent events, do not continue the story and do not change anything that was written. Respect the background lore.
2. The arcs the outline already has are listed only so you do not repeat them and so the new arc fits after them; the arc you write covers the story above only.
3. Divide the story into chapters at its natural turning points (a change of place, time, point of view or situation), in the order the events happen. Together the chapters cover the whole story given, with nothing left out and nothing counted twice.
4. Give the arc and every chapter a short, memorable name.
5. The "description" of the arc and of every chapter is shown in the outline, so keep it terse: one to three short sentences for the arc (about 60 words at most), one or two for a chapter (about 40 words at most). Put everything else into the "note": for the arc the premise, the main conflict, the turning points and how it ends; for a chapter the scene, the characters and places involved (by the names used in the story), what happens step by step, what changes and how the chapter ends. Plain text with paragraphs, no headings. Do not repeat the description in the note, extend it.

### Output
Answer only with the JSON object requested by the response format: the "name", the terse "description" and the detailed "note" of the arc and its list of "chapters", each with a "name", a terse "description" and a detailed "note", in the order they are read. No introduction, no commentary.
""";

    /** Added to the generation prompt when name categories are selected; the user can override it in the settings. */
    public static String NAME_TOOLS_PROMPT = """
[ Name tools: you can call list_name_categories to see the categories of names, and generate_names with a category and a count (and optionally min_length and max_length) to get names of it. Call a reasonable amount, minimum is 10. When the arc needs a name for a person, place or thing, call generate_names instead of inventing one, and pick from the returned names. You may also modify or remix the returned names when none of them fits as it is: combine parts of two names, change an ending or a letter, adjust the spelling, or shorten or lengthen a name. Keep the result sounding like it belongs to the same category. ]
""";
}
