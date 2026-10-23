package com.github.enerccio.marginalia.extensions.stconverter.service;

/**
 * The default prompt of the wand. The variables are {@code backgroundLore}, {@code chat}, {@code firstMessage} and
 * {@code lastMessage}.
 */
public final class Defaults {

    private Defaults() {
    }

    public static final String PROMPT = """
[ You are an experienced story editor. The author is rewriting a roleplay chat as a novel, one turn at a time: for every turn a writer is given the scene setting, the point of view character, the characters who are present and the instructions of what happens, and writes the text of the turn. Your task is to read the chat below and divide it into such turns. ]

### Background lore
{{backgroundLore}}

### The chat
The messages below are the chat in the order they were written, messages {{firstMessage}} to {{lastMessage}}. Each is under its number and the name of the one who wrote it. A message of the user is what the user's character does or says, a message of a character is the answer of the story to it.

{{chat}}

### Guidelines
1. Divide the chat into turns at its natural turning points (a change of place, time, point of view or situation, or when a part of the action is finished), in the order the messages were written. A turn is made of one or more messages that follow each other; the turns together cover every message from {{firstMessage}} to {{lastMessage}} exactly once, with nothing left out and nothing in two turns. Prefer turns that are long enough to be a scene of the story, not a single line of dialogue.
2. Say which messages a turn is made from with "firstMessage" and "lastMessage" (the numbers of the headings).
3. "sceneSetting": where and when the turn happens, who and what is there and what matters about the scene. Take it from the chat and the background lore; do not invent anything the chat does not support.
4. "povCharacter": the one character the turn is written from, by the name used in the chat. Keep the same character from turn to turn unless the story really changes the point of view.
5. "presentCharacters": the names of the characters who take part in or are in the scene, separated by commas.
6. "instructions": what the writer should write for the turn, as instructions: the events in the order they happen, what the characters do, say and feel, the details of the chat that must stay, how the turn ends. Everything the messages tell must be in it, so that the writer can write the turn without seeing the chat. Do not continue the story, do not change what happened, respect the background lore.

### Output
Answer only with the JSON object requested by the response format: the list of "turns" with the fields above. No introduction, no commentary.
""";
}
