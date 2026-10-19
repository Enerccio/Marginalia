package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;

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
}
