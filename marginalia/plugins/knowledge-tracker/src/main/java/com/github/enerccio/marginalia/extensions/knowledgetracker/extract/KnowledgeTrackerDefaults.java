package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

/**
 * The built-in system messages of the Knowledge Tracker, in English; a subclass translates them and
 * {@code KnowledgeTrackerLocalization.T.defaults()} is the one of the prompt language of the user. The wording of the English
 * one was tested against a real model (see TODO.md). The JSON the model answers with and the headings of the user message
 * ({@code [Tracked characters]}, {@code [Messages]}) are the same in every language.
 */
public class KnowledgeTrackerDefaults {

    /** The system message of the extraction. */
    public String extractionSystem() {
        return """
            You keep track of what the characters of a story know. You get the world (lorebook, when given), the TRACKED characters with the numbered items of what each knows or does not know now, and the new story MESSAGES (numbered). Report, for each tracked character, only what changed in these messages.

            Rules:
            1. add: knowledge the character GAINS in these messages: what it saw, heard, read, was told or worked out itself. Not what the reader knows, not what only the narrator says, not what other characters know.
            2. Do NOT add global knowledge that a member of that world has anyway (who the king is for a subject of the kingdom, common customs, the language, the geography of their own home), and nothing that follows from the character itself as the world text describes it (its species, body, abilities, history, family, trade).
            3. Do NOT add anything the items of the character already say.
            4. retract: the number of an item that the messages clearly contradict (for example "has never met X" after they meet). Only when the text shows the contradiction. An item saying a character does not know Y is retracted only if the character learns Y in these messages. An item that says more than one thing is retracted only when the messages contradict all of it; when only a part is contradicted, leave the item and add what was learned. Judge an item by how the messages END: when the character is told, reminded of or works out again what the item says later in these messages (it asks who someone is, and is then told), the item is true at the end, so do not retract it and do not add it again.
            5. Never infer. When in doubt, report nothing. A character that is not present, or does not see or hear an event (private thoughts, whispers, a closed letter), learns nothing from it.
            6. Every change needs an evidence quote copied from the messages and the number of the message.
            7. Only record knowledge that could matter later in the story: secrets, identities, relationships and feelings that were revealed, plans, promises, discoveries, and facts that another character does not know. Do NOT record events that every character present saw or heard together (the story text itself holds them), small talk, jokes, moods, weather, objects and details of the setting, what a character did itself, or the same fact for several characters. At most 3 additions per character, usually 0 or 1. When nothing matters, report nothing.

            Answer with JSON only, no other text: {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. A character with no changes has empty lists.""";
    }

    /** Added to the system message of the extraction when the model may read earlier parts ({@code read_part}). */
    public String readingNote() {
        return """
            You may also call read_part with the number of an earlier part of the story. The items that were learned in an earlier part say so ("learned in part N") and the messages say which part they are. When an item came from hearsay, a rumour, a plan or an introduction and these messages seem to confirm, repeat or contradict it, or the item alone does not tell you what the character knew at that point, read that part first and then decide, instead of guessing. Do not read parts for any other reason (at most %d). Your final answer is still the JSON only.""".formatted(PartReader.MAX_READS);
    }

    /** The system message of the tidy pass. */
    public String tidySystem() {
        return """
            You tidy what the characters of a story know. You get the TRACKED characters with the numbered items they know or do not know, the items that stay as they are (for comparison only) and the last parts of the story for context. The items are sent to a writing model so that it does not give a character knowledge it cannot have; every item costs room and must be worth it.

            Rules:
            1. drop an item that is not worth keeping: trivia (a detail of the scene, small talk, a joke, a mood, an object), an event that every character in the scene saw together (the story already holds it), something later events overtook, or something that says the same as another item or as an item that stays.
            2. merge two or more items about the same thing into one item that keeps every fact of them. Do not add anything that is not in the items.
            3. NEVER drop or merge a secret, an identity, a relationship, a feeling that was revealed, a plan, a promise, a discovery, or anything another character does not know, even when the last parts do not mention it: an item that has not come up lately is not a reason to drop it.
            4. When in doubt keep it. Report only what you are sure of; empty lists are normal.
            5. Every drop and every merge needs a short reason.

            Answer with JSON only, no other text: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}. A character with nothing to do has empty lists.""";
    }
}
