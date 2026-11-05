package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Where an extension says how the text of the parts <b>in an export</b> differs from the text as it is stored: a passage replaced by another
 * text, with a footnote if it likes. The export dialog asks the registry once for the whole export and gives the exporters the result, so the
 * exporters are never touched.
 * <p>
 * This is the export's counterpart of {@link PartTextRegistry} (which is what a <i>model</i> reads). The Conlang Translator, for one, stores the
 * speech of the characters in an invented language and lets the author export the English instead, or either with the other in footnotes.
 * <p>
 * <b>Why a registry and not a decorator that changes the parts:</b> an edit is a range of the <i>stored</i> text. When two extensions both change the
 * parts of an export one after the other, the second one reads its ranges against text the first one changed. Here every contributor answers for
 * the stored text, the registry puts all the answers together once (an edit that overlaps an earlier one is dropped, never applied to the wrong
 * text) and numbers the footnotes of the whole export in one sequence, so two extensions can work on the same book.
 * <p>
 * Extensions register a {@link Contributor} when they are loaded and call {@link #unregisterAll} in {@code onExtensionUnload}. The export dialog
 * is the caller ({@link #apply}); with none registered, or with no edit, the parts are returned as they are.
 *
 * @see PartTextRegistry
 */
public class ExportTextRegistry {

    private static final Logger log = LoggerFactory.getLogger(ExportTextRegistry.class);

    /**
     * What an export is about, handed to the contributors so they can find the choices the author made in the dialog.
     */
    public interface Session {

        /**
         * The object the export was started from: the export dialog. An extension that adds a field to the dialog (a decorator of
         * {@code ExportDialog.createExtensionFields}) keeps it in a map keyed by the dialog and finds its field here.
         */
        Object source();

        /**
         * The book that is exported.
         */
        Manuscript manuscript();
    }

    /**
     * One change of the text of a part in the export: the range {@code [start, end)} of the stored text becomes {@code replacement}, and, when
     * {@code note} is not null, a footnote mark follows it and the note is written after the part.
     *
     * @param start       first character of the stored text that is replaced
     * @param end         end of the replaced range (exclusive); {@code start == end} inserts
     * @param replacement what is written in its place; may be empty (the range is removed)
     * @param note        the text of the footnote, null for none; the registry puts it on one line
     * @param markAt      where the footnote mark goes in the stored text, {@code end} or later: the text between {@code end} and {@code markAt}
     *                    is kept and the mark follows it (a mark behind a closing quotation mark). Ignored without a note.
     */
    public record Edit(int start, int end, String replacement, String note, int markAt) {

        /** A replacement without a footnote. */
        public static Edit replace(int start, int end, String replacement) {
            return new Edit(start, end, replacement, null, end);
        }

        /** A replacement with a footnote mark right behind it. */
        public static Edit withNote(int start, int end, String replacement, String note) {
            return new Edit(start, end, replacement, note, end);
        }

        /** A replacement with a footnote mark further on in the stored text (behind the closing quotation mark of the passage, for one). */
        public static Edit withNote(int start, int end, String replacement, String note, int markAt) {
            return new Edit(start, end, replacement, note, markAt);
        }

        private int stop() {
            return note == null ? end : Math.max(end, markAt);
        }
    }

    /**
     * Says what an extension changes in the text of the parts of an export.
     */
    public interface Contributor {

        /**
         * Called once with every part of the export, before {@link #edits} is asked for each of them, so the data of the whole export can be loaded
         * in one go (a book of hundreds of parts must not cost a query per part).
         */
        default void prepare(Collection<ChatMessage> parts, Session session) throws Exception {
        }

        /**
         * The changes to the text of the part, as ranges of {@code part.getResponse()}, the stored text.
         *
         * @return the edits in any order, empty for none; an edit outside the text, or one that overlaps an earlier one, is ignored
         */
        List<Edit> edits(ChatMessage part, Session session) throws Exception;
    }

    // registration order is the order of priority: the first one wins an overlap
    private final Map<Contributor, String> contributors = new LinkedHashMap<>();

    /**
     * Adds a contributor.
     *
     * @param owner who registers it, the symbolic name of the bundle of an extension; see {@link #unregisterAll}
     */
    public synchronized void register(String owner, Contributor contributor) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(contributor, "contributor");
        contributors.put(contributor, owner);
    }

    public synchronized void unregister(Contributor contributor) {
        contributors.remove(contributor);
    }

    /**
     * Removes the contributors an owner registered.
     */
    public synchronized void unregisterAll(String owner) {
        contributors.values().removeIf(registered -> Objects.equals(registered, owner));
    }

    private synchronized List<Contributor> registered() {
        return new ArrayList<>(contributors.keySet());
    }

    /**
     * Whether any contributor is registered (an export with none changes nothing).
     */
    public synchronized boolean isActive() {
        return !contributors.isEmpty();
    }

    /**
     * The parts as the export gets them: a part that has edits is a copy with the new text (the stored part is never changed), the others are the
     * parts themselves, in the order given. The footnotes of the whole export are numbered {@code [^1]}, {@code [^2]}... in the order of the parts, and
     * each part ends with the definitions of its own notes.
     */
    public List<ChatMessage> apply(List<ChatMessage> parts, Session session) {
        List<Contributor> active = registered();
        if (active.isEmpty() || parts.isEmpty()) {
            return parts;
        }
        List<Contributor> prepared = new ArrayList<>();
        for (Contributor contributor : active) {
            try {
                contributor.prepare(parts, session);
                prepared.add(contributor);
            } catch (Exception | LinkageError e) {
                log.warn("An extension could not prepare the text of the parts for the export, it is left out: {}", e.getMessage(), e);
            }
        }
        List<ChatMessage> result = new ArrayList<>(parts.size());
        int number = 1;
        for (ChatMessage part : parts) {
            List<Edit> edits = new ArrayList<>();
            for (Contributor contributor : prepared) {
                try {
                    List<Edit> asked = contributor.edits(part, session);
                    if (asked != null) {
                        edits.addAll(asked);
                    }
                } catch (Exception | LinkageError e) {
                    log.warn("An extension could not change a part for the export, it is exported as stored: {}", e.getMessage(), e);
                }
            }
            Rendered rendered = render(part.getResponse(), edits, number);
            if (rendered == null) {
                result.add(part);
                continue;
            }
            ChatMessage copy = ChatMessage.copyOf(part);
            copy.setResponse(rendered.text());
            result.add(copy);
            number = rendered.next();
        }
        return result;
    }

    /**
     * @param text the new text of the part
     * @param next the number of the first footnote of the part after this one
     */
    record Rendered(String text, int next) {
    }

    /**
     * Puts the edits into the stored text. Public to the package for the tests; null when there is nothing to change.
     *
     * @param first the number of the first footnote of this part
     */
    static Rendered render(String stored, List<Edit> edits, int first) {
        String text = stored == null ? "" : stored;
        List<Edit> ordered = new ArrayList<>();
        for (Edit edit : edits) {
            // the order the contributors gave them in is the priority among edits that start at the same place (the sort is stable)
            if (edit != null && edit.start() >= 0 && edit.end() >= edit.start() && edit.stop() <= text.length() && edit.replacement() != null) {
                ordered.add(edit);
            }
        }
        ordered.sort(Comparator.comparingInt(Edit::start));
        StringBuilder out = new StringBuilder();
        List<String> notes = new ArrayList<>();
        int number = first;
        int taken = 0;
        boolean changed = false;
        for (Edit edit : ordered) {
            if (edit.start() < taken) {
                continue;
            }
            out.append(text, taken, edit.start()).append(edit.replacement());
            int end = edit.end();
            if (edit.note() != null) {
                out.append(text, end, edit.stop()).append("[^").append(number).append(']');
                notes.add("[^" + number + "]: " + oneLine(edit.note()));
                number++;
                end = edit.stop();
            }
            taken = end;
            changed = true;
        }
        if (!changed) {
            return null;
        }
        out.append(text, taken, text.length());
        for (String note : notes) {
            out.append("\n\n").append(note);
        }
        return new Rendered(out.toString(), number);
    }

    /** A line break would end the definition of a footnote. */
    private static String oneLine(String note) {
        return note.replaceAll("\\s*[\\r\\n]+\\s*", " ").trim();
    }
}
