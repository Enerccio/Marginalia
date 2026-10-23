package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.CheckpointService.Difference;
import com.github.enerccio.marginalia.domain.service.CheckpointService.FileChange;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.utils.TextDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * How checkpoints are written down for the model: sizes, one line for each, the list.
 */
public final class CheckpointText {

    private CheckpointText() {
    }

    /**
     * A size: bytes, KB or MB with one decimal.
     */
    public static String bytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * {@code 0003 manual, round 12, 4.1 MB, 5 workspace files: "before the bulk replace"}
     */
    public static String line(Info info) {
        StringBuilder text = new StringBuilder(info.id()).append(' ').append(info.kind().name().toLowerCase(Locale.ROOT))
                .append(", round ").append(info.round()).append(", ").append(bytes(info.bytes())).append(", ")
                .append(info.workspaceFiles()).append(" workspace file").append(info.workspaceFiles() == 1 ? "" : "s");
        if (!info.label().isEmpty()) {
            text.append(": \"").append(info.label()).append('"');
        }
        return text.toString();
    }

    /**
     * The checkpoints, one in a line, the oldest first; a text for none.
     */
    public static String list(List<Info> infos) {
        if (infos.isEmpty()) {
            return "There are no checkpoints.";
        }
        StringBuilder text = new StringBuilder();
        for (Info info : infos) {
            text.append(line(info)).append('\n');
        }
        return text.toString().stripTrailing();
    }

    // the lines of each part of a comparison
    static final int MAX_DIFF_LISTED = 40;
    static final int MAX_DIFF_SENTENCE = 200;

    /**
     * What differs between a checkpoint and now, for the model: a count and a line for each change of the book and of the workspace,
     * and the sentences of the one text file that was asked for.
     */
    public static String difference(Difference diff) {
        StringBuilder text = new StringBuilder("Checkpoint ").append(line(diff.checkpoint())).append(", compared with now.\n");
        boolean nothing = true;
        if (diff.book() != null) {
            List<Item> items = diff.book().items();
            text.append("\nBook: ");
            if (items.isEmpty()) {
                text.append("no change.\n");
            } else {
                nothing = false;
                text.append(bookCounts(items)).append('\n');
                int shown = Math.min(items.size(), MAX_DIFF_LISTED);
                for (int i = 0; i < shown; i++) {
                    Item item = items.get(i);
                    text.append("- ").append(item.change().name().toLowerCase(Locale.ROOT)).append(' ').append(kindName(item.kind()))
                            .append(' ').append(item.label().isBlank() ? "(no name)" : "\"" + item.label() + "\"");
                    if (item.change() == Change.CHANGED) {
                        text.append(" (").append(String.join(", ", item.fields().stream().map(field -> field.name()).toList())).append(')');
                    }
                    text.append('\n');
                }
                more(text, items.size(), shown);
            }
        }
        text.append("\nWorkspace: ");
        if (diff.files().isEmpty()) {
            text.append("no change.\n");
        } else {
            nothing = false;
            text.append(fileCounts(diff.files())).append('\n');
            int shown = Math.min(diff.files().size(), MAX_DIFF_LISTED);
            for (int i = 0; i < shown; i++) {
                FileChange file = diff.files().get(i);
                text.append("- ").append(file.change().name().toLowerCase(Locale.ROOT)).append(' ').append(file.path()).append(" (")
                        .append(switch (file.change()) {
                            case ADDED -> bytes(file.after());
                            case DELETED -> bytes(file.before());
                            case CHANGED -> bytes(file.before()) + " -> " + bytes(file.after());
                        }).append(")\n");
            }
            more(text, diff.files().size(), shown);
        }
        if (diff.lines() != null && !diff.lines().identical()) {
            text.append("\nWhat differs in the file ('-' only in the checkpoint, '+' only now):\n");
            int shown = Math.min(diff.lines().changes().size(), MAX_DIFF_LISTED);
            for (int i = 0; i < shown; i++) {
                TextDiff.Change change = diff.lines().changes().get(i);
                String sentence = change.text().strip().replaceAll("\\s+", " ");
                text.append(change.added() ? "+ " : "- ").append(change.index()).append(": ")
                        .append(sentence.length() > MAX_DIFF_SENTENCE ? sentence.substring(0, MAX_DIFF_SENTENCE) + "..." : sentence)
                        .append('\n');
            }
            more(text, diff.lines().changes().size(), shown);
        }
        if (nothing) {
            text.append("\nNothing differs.");
        }
        return text.toString().stripTrailing();
    }

    private static void more(StringBuilder text, int total, int shown) {
        if (total > shown) {
            text.append("[and ").append(total - shown).append(" more]\n");
        }
    }

    private static String kindName(Kind kind) {
        return switch (kind) {
            case BOOK -> "setting";
            case MESSAGE -> "part";
            case LOREBOOK -> "lorebook";
            case ENTRY -> "entry";
            case ANNOTATION -> "annotation";
        };
    }

    private static String bookCounts(List<Item> items) {
        List<String> parts = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            for (Change change : Change.values()) {
                long count = items.stream().filter(item -> item.kind() == kind && item.change() == change).count();
                if (count > 0) {
                    parts.add(count + " " + kindName(kind) + (count == 1 ? " " : "s ") + change.name().toLowerCase(Locale.ROOT));
                }
            }
        }
        return String.join(", ", parts) + ".";
    }

    private static String fileCounts(List<FileChange> files) {
        List<String> parts = new ArrayList<>();
        for (Change change : Change.values()) {
            long count = files.stream().filter(file -> file.change() == change).count();
            if (count > 0) {
                parts.add(count + (count == 1 ? " file " : " files ") + change.name().toLowerCase(Locale.ROOT));
            }
        }
        return String.join(", ", parts) + ".";
    }
}
