package com.github.enerccio.marginalia.domain.service.manual;

import java.util.List;

/**
 * Where the help assistant's model reads the data of the Admin page, see {@link AdminHelpTools}. Only used for an
 * administrator and when an administrator allowed it (the application settings).
 */
public interface AdminDataSource {

    /** The topics of {@link #read}. */
    List<String> TOPICS = List.of("extensions", "users", "backups", "cleanup", "application");

    /**
     * The data of the topic as Markdown, short and without secrets.
     *
     * @param topic one of {@link #TOPICS}
     * @throws IllegalArgumentException for another topic; the message lists them
     */
    String read(String topic) throws Exception;

    /**
     * The last lines of the log of an extension (what it writes through {@code createLogger}).
     *
     * @param extension its name, symbolic name or the name of its JAR
     * @param lines     how many lines at most
     * @throws IllegalArgumentException when there is no such extension; the message lists the ones there are
     */
    String readExtensionLog(String extension, int lines) throws Exception;
}
