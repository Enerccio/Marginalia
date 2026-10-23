package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;

import java.util.List;
import java.util.Locale;

/**
 * What a resume did to the working copy of a run: the changes the user made to the book while the run was paused, which of them
 * the copy took over and which it did not, because the agent had changed the same thing. The agent is told by {@link #text()};
 * the items are named by their handles.
 *
 * @param taken     the changes of the user that are in the copy now
 * @param conflicts the changes that were not taken over: the copy keeps the version of the agent
 */
public record SyncReport(List<Line> taken, List<Line> conflicts) {

    public enum Reason {
        /** The user and the agent changed the same text. */
        BOTH_CHANGED,
        /** The agent deleted what the user changed. */
        YOU_DELETED,
        /** The user deleted what the agent changed. */
        USER_DELETED,
        /** The user added it, but what it belongs to is gone from the copy. */
        NOT_ADDED
    }

    /**
     * @param handle the handle of the item in the copy; null when it has none (a setting of the book, an item that is gone)
     * @param label  what the item is
     * @param fields the fields concerned, by name; empty for a whole item
     * @param reason why it was not taken over; null for a change that was
     */
    public record Line(Kind kind, Change change, String handle, String label, List<String> fields, Reason reason) {
    }

    public static final SyncReport EMPTY = new SyncReport(List.of(), List.of());

    public boolean isEmpty() {
        return taken.isEmpty() && conflicts.isEmpty();
    }

    /**
     * The report as the model reads it.
     */
    public String text() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder("While the run was paused the user changed the book. What they changed is in your working copy now, "
                + "except where you changed the same thing too. What you read earlier about the items below may be out of date: read them again "
                + "if you need them.");
        if (!taken.isEmpty()) {
            text.append("\n\nTaken over into your working copy:");
            taken.forEach(line -> text.append("\n- ").append(describe(line)).append(' ').append(switch (line.change()) {
                case ADDED -> "added by the user";
                case DELETED -> "deleted by the user";
                case CHANGED -> "changed by the user" + fields(line);
            }));
        }
        if (!conflicts.isEmpty()) {
            text.append("\n\nNot taken over, your version is kept (the user decides about these when they review your result):");
            conflicts.forEach(line -> text.append("\n- ").append(describe(line)).append(": ").append(switch (line.reason()) {
                case BOTH_CHANGED -> "the user changed it too" + fields(line);
                case YOU_DELETED -> "you deleted it and the user changed it";
                case USER_DELETED -> "the user deleted it and you changed it";
                case NOT_ADDED -> "the user added it, but what it belongs to is gone from your copy";
            }));
        }
        return text.toString();
    }

    private static String describe(Line line) {
        String kind = switch (line.kind()) {
            case BOOK -> "book setting";
            case MESSAGE -> "part";
            case LOREBOOK -> "lorebook";
            case ENTRY -> "lorebook entry";
            case ANNOTATION -> "annotation";
        };
        return kind + (line.handle() == null ? "" : " " + line.handle()) + " \"" + line.label() + "\"";
    }

    private static String fields(Line line) {
        return line.fields().isEmpty() ? "" : " (" + String.join(", ", line.fields().stream().map(SyncReport::humanize).toList()) + ")";
    }

    private static String humanize(String field) {
        return switch (field) {
            case "response" -> "text";
            case "payload" -> "content";
            case "comment" -> "note";
            default -> field.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT);
        };
    }
}
