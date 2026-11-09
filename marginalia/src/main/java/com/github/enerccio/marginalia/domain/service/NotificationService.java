package com.github.enerccio.marginalia.domain.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The desktop notifications of the browser that tell the user a long run of a model is over (a generation, an annotation...), so they
 * can leave the tab while the model works. The service knows <i>what</i> can notify (the {@link Source sources}: built in ones
 * and the ones extensions register), what the current user chose ({@code NotificationSettings}) and builds the text; showing the
 * notification is the browser's job, see {@code ui.widgets.DesktopNotifier}.
 * <p>
 * A source that has an automatic run (the annotation that the book starts by itself after a generation, as opposed to the dialog the
 * author opens) lists two {@link Entry entries}, the user chooses them apart: a run reports itself as automatic or not
 * ({@link #prepare}), and only the entry of that kind decides. So the user who chose <i>Annotate with AI</i> is not told about the
 * automatic annotation, only the one who chose <i>Annotate with AI (automatic)</i> is.
 * <p>
 * The sources of an extension are removed when the extension is unloaded ({@link #unregisterAll}); what the user chose for them stays in
 * their settings and applies again when the extension is back.
 */
public interface NotificationService {

    /**
     * The id of the source of the generation of the next part of the story.
     */
    String GENERATION = "generation";
    String ANNOTATE = "annotate";
    String REDRAFT = "redraft";
    String IMPERSONATE = "impersonate";
    String SUMMARY = "summary";
    String RESUMMARIZE = "resummarize";
    String HELP_ASSISTANT = "help-assistant";

    /**
     * What an id gets to say it is the automatic run of a source, see {@link Entry#key()}.
     */
    String AUTOMATIC_SUFFIX = ".auto";

    /**
     * How a run ended; a run the user stopped does not notify.
     */
    enum Outcome {
        FINISHED,
        FAILED
    }

    /**
     * Something that can notify.
     *
     * @param id           stable and unique, an extension prefixes it with its own name ({@code "reviewer.review"}); it is stored in the
     *                     settings of the users
     * @param name         the name the user sees in the list, asked every time it is shown so it follows the language of the session
     * @param hasAutomatic whether the source also runs by itself (without the user opening a dialog), which the user chooses apart
     */
    record Source(String id, Supplier<String> name, boolean hasAutomatic) {

        public Source(String id, Supplier<String> name) {
            this(id, name, false);
        }
    }

    /**
     * One line of the list of the settings.
     *
     * @param key       the id of the source, with {@link #AUTOMATIC_SUFFIX} for its automatic run; what is stored
     * @param name      the name in the language of the session; the automatic run says so
     * @param source    the id of the source
     * @param automatic whether this is the automatic run of the source
     */
    record Entry(String key, String name, String source, boolean automatic) {
    }

    /**
     * What to show.
     *
     * @param title the title of the notification
     * @param body  its text
     * @param tag   notifications with the same tag replace each other in the browser, so a user who starts ten runs is not buried
     */
    record Notice(String title, String body, String tag) {
    }

    /**
     * Adds the source (one with the same id is replaced).
     *
     * @param owner who registers it, the symbolic name of the bundle of an extension; see {@link #unregisterAll}
     */
    void register(String owner, Source source);

    void unregister(String id);

    /**
     * Removes the sources an owner registered; called when the extension is unloaded.
     */
    void unregisterAll(String owner);

    /**
     * The sources that are registered now, the built in ones first and then the ones of the extensions in the order they were registered.
     */
    List<Source> sources();

    /**
     * The lines of the list of the settings, named in the language of the session: for every source one, and one more for its automatic
     * run when it has one, right after it.
     */
    List<Entry> entries();

    /**
     * Whether the current user turned notifications on.
     */
    boolean isEnabled() throws Exception;

    /**
     * The keys the current user chose, including those of sources that are not registered now.
     */
    Set<String> getChosen() throws Exception;

    /**
     * Stores the choice of the current user.
     *
     * @param enabled the master switch
     * @param chosen  the keys of the {@link #entries()} that are chosen; the keys of sources that are not registered now are kept as they were
     */
    void save(boolean enabled, Collection<String> chosen) throws Exception;

    /**
     * The notification the current user wants for the end of a run, null when they want none: notifications are off, or the entry of this
     * kind of run is not chosen, or the source is not registered.
     *
     * @param source    the id of the {@link Source}
     * @param automatic whether the run started by itself; ignored for a source that has no automatic run
     * @param detail    what the run was about (the title of the book), null for nothing; shown in the text
     */
    Notice prepare(String source, boolean automatic, Outcome outcome, String detail) throws Exception;

    /**
     * The notification for the test button of the settings, whatever the user chose.
     */
    Notice test();
}
