package com.github.enerccio.marginalia.domain.collections;

import org.apache.commons.lang3.StringUtils;

/**
 * The details of a version of a part that the application itself names ({@code MessageRevision#getSourceDetail()}), stored as
 * a code and shown in the language of whoever looks at the history. The other details are free text (the model and protocol of
 * a generated version, the name of an extension) and are shown as they are.
 */
public enum RevisionDetail {

    /**
     * A manuscript command (an agent, the help assistant).
     */
    COMMAND,
    /**
     * An agent's work merged into the book.
     */
    AGENT_MERGE,
    /**
     * A mass search and replace.
     */
    REPLACE,
    /**
     * The part is a branch of another part.
     */
    BRANCH,
    /**
     * Imported from an archive of text files.
     */
    ARCHIVE_IMPORT,
    /**
     * Imported from a SillyTavern chat.
     */
    SILLYTAVERN_IMPORT;

    // a model or an extension is not called like this
    private static final String PREFIX = "#";

    /**
     * What is stored as the detail.
     */
    public String code() {
        return PREFIX + name();
    }

    /**
     * The detail a stored text is, null when it is free text (or empty).
     */
    public static RevisionDetail parse(String stored) {
        if (StringUtils.isEmpty(stored) || !stored.startsWith(PREFIX)) {
            return null;
        }
        for (RevisionDetail detail : values()) {
            if (detail.code().equals(stored)) {
                return detail;
            }
        }
        return null;
    }
}
