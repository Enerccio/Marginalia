package com.github.enerccio.marginalia.domain.service.agent;

/**
 * Turns the uuids of the items of the working copy into the <b>handles</b> an agent works with and back. An agent never
 * sees a uuid: the uuids of the copy change when it is restored from a checkpoint or made again, the handles of the run
 * stay, and a handle is a few tokens where a uuid is about twenty. A handle looks like {@code h:m17}: the prefix
 * {@code h:}, the kind of the item (m part, s summary, l lorebook, e lorebook entry, a annotation) and the number it got in the run.
 * <p>
 * The translation is done at the border between the model and the commands, on the text of the results and on the
 * arguments, so a command needs no knowledge of it and a command of a plugin gets it too.
 */
public interface HandleTranslator {

    /**
     * What a handle looks like.
     */
    String HANDLE_PATTERN = "\\bh:[mlsea]\\d+\\b";

    /**
     * The text with every uuid of an item of the working copy replaced by its handle (one is given when the item has none
     * yet); the uuids of anything else are left as they are.
     */
    String toHandles(String text);

    /**
     * The value with every handle of the run replaced by the uuid of its item. A text is searched for handles, a map and a
     * list are translated in all their values (the keys are left), anything else is returned as it is. A handle the run
     * does not have is left as it is, so the command that gets it says it knows no such item.
     */
    Object fromHandles(Object value);

    /**
     * Translates nothing.
     */
    HandleTranslator NONE = new HandleTranslator() {
        @Override
        public String toHandles(String text) {
            return text;
        }

        @Override
        public Object fromHandles(Object value) {
            return value;
        }
    };

}
