package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * The built-in system message of the extraction of the State Tracker. The wording was tested against a real model (see TODO.md).
 * The JSON the model answers with and the headings of the user message ({@code [Tracked characters]}, {@code [Messages]}) belong to
 * it.
 */
public final class StateDefaults {

    private StateDefaults() {
    }

    /** The system message of the extraction, without the clock. */
    public static String extractionSystem() {
        return extractionSystem(false);
    }

    /**
     * The system message of the extraction; with the clock the model also reports how the time of the story moves in every message
     * (the wording that was tested merged, see TODO.md, Milestone 5).
     */
    public static String extractionSystem(boolean clock) {
        String text = stateSystem();
        if (!clock) {
            return text;
        }
        int answer = text.lastIndexOf("\n\nAnswer with JSON only");
        return text.substring(0, answer).replace("You get the TRACKED characters,",
                "You also keep the time of the story. You get the CLOCK (the story day counted from an anchor event, the time of day, and the calendar date when the story has one), the TRACKED characters,")
                + CLOCK_RULES;
    }

    private static final String CLOCK_RULES = """


            The clock. For each message, report how the time of the story moves in it. Do not count: report only what the message says, the program adds it up.
            - none: the message does not move the time, or says nothing about it. This is the default.
            - advance: time passes. "days" is how many days pass (0 when it stays the same day: later that day, an hour later), "timeOfDay" where the message ends (dawn, morning, noon, afternoon, evening, night) when it says so.
            - day: the message states the story day counted from the anchor event of the clock ("the fortieth day since you woke"): "day" is that number. An ordinal of something else ("on the third morning" of a journey or a stay) is an advance, not a day.
            - date: the message states a calendar date: "date" as YYYY-MM-DD, with the year of the clock when the message gives none.
            - flashback: the message is set in the past (a memory, a dream of the past, a story told about earlier events); the clock does not move, and nothing in it changes the current state of the characters.
            Clock rules: report only what the message states or makes plain ("the next morning" is one day, morning; "that night" is the same day, night; "three days later" is three days), never time the text does not show. Vague spans ("weeks passed", "a few days later") are an advance with your best number of days and "approximate": true. The time of day alone is an advance of 0 days. Every report other than none needs a quote copied word for word from the message.

            Answer with JSON only, no other text: {"clock":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}],"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}. "clock" has one entry per message, in order, leaving out the fields a kind does not use. In "characters", "value" is for set and add, "item" for remove. A character with no changes has an empty list; with no tracked characters, "characters" is empty.""";

    private static String stateSystem() {
        return """
            You keep track of the current state of the characters of a story: what they wear, carry, hold and so on. You get the TRACKED characters, each with its categories (the kind of the category, what belongs in it, and its current values, numbered), and the new story MESSAGES (numbered). Report, for each tracked character, only what changed in these messages, as it stands at the END of the messages.

            Kinds of category:
            - single: one value. "set" gives the new value (it replaces the current one), "clear" says the character has none any more.
            - list: entries. "add" a new entry, "remove" an entry by its number.
            - description: one text. "set" gives the whole new text; keep from the current text what is still true.

            Rules:
            1. Report a change only when the messages show it. A category marked strict changes only when the text states the change. A category that is not strict also changes by the direct physical consequence of what the text states (wet after falling into a river), never by a guess about what happened off the page.
            2. An action changes the state when it is done, not when it is planned, promised, offered or asked for. A thing handed over is removed from the giver and added to the receiver (when they are tracked and have that category). Putting a thing down for a moment, using it or showing it is not losing it: remove an entry only when it is given away, lost, used up, destroyed, left behind or taken.
            3. Follow the description of each category: what does not belong in it is not reported in it.
            4. Do not report what the current values already say, and do not rephrase a value that did not change.
            5. Values are short, in the words of the story, and describe a state, not an action (cheerful, not laughing).
            6. Every change needs an evidence quote copied word for word from the messages and the number of the message.
            7. When in doubt, report nothing. Empty lists are normal.

            Answer with JSON only, no other text: {"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}. "value" is for set and add, "item" for remove. A character with no changes has an empty list.""";
    }

    /** The system message of the tidy. Not yet tried against a real model (see TODO.md). */
    public static String tidySystem() {
        return """
            You keep the current state of the characters of a story tidy: what they wear, carry, hold, feel and so on. You get the TRACKED characters, each with its categories and their current values (numbered; a value marked "(stays)" cannot be changed), and the last story MESSAGES (numbered). Some states fade without the story saying so. Decide, for each numbered value, whether it is still true at the END of the messages.

            For each value, one of:
            - keep it: say nothing about it. This is the default.
            - drop: it is no longer true or no longer worth tracking (an anger of several scenes ago that the messages do not show any more, out of breath long after the run, "holding a cup" long after the meal, wet clothes that had time to dry).
            - update: the messages show that the state moved on (the mood improved, the bruise is fading): give the new short value.
            - merge: two or more entries of the same list category are the same thing ("rope" and "coil of rope"): give the one entry that replaces them.

            Rules:
            1. Lasting things stay: clothes, carried items, weapons, scars and injuries that need time to heal are kept unless the messages show otherwise. Drop or update mainly short-lived states (moods, feelings, being wet, cold, tired, out of breath, holding something for a moment).
            2. Judge only from the messages and the passing of time in them, never from a guess about what happened off the page.
            3. Values are short, in the words of the story, and describe a state, not an action.
            4. Every drop, update and merge needs a short reason.
            5. When in doubt, keep. Empty lists are normal.
            6. A value may say on which story day it was set and how long ago that was: use it, a short-lived state set many days ago has faded.

            Answer with JSON only, no other text: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"update":[{"item":N,"value":"...","reason":"..."}],"merge":[{"items":[N,M],"value":"...","reason":"..."}]}]}. A character with nothing to change has empty lists.""";
    }
}
