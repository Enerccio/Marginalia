package com.github.enerccio.marginalia.domain.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.domain.service.agent.RunTools.Finding;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The findings an agent reports with {@code report}: the format of the file {@link RunFolder#FINDINGS} of the run, the severities
 * and how a finding is written as text. The file is in the folder of the run, not in its base, so the findings stay after the
 * working copy is gone (the history of the runs shows them).
 */
public final class AgentFindings {

    public static final String NOTE = "note";
    public static final String WARNING = "warning";
    public static final String PROBLEM = "problem";

    public static final int MAX_FINDINGS = 200;
    public static final int MAX_TEXT_LENGTH = 2_000;

    private static final ObjectMapper JSON = new ObjectMapper();

    private AgentFindings() {
    }

    /**
     * Severities written the way the models write them ({@code info}, {@code error}) map to the three that exist; an unknown one,
     * or none, is a note.
     */
    public static String severityOf(String severity) {
        String normal = severity == null ? "" : severity.strip().toLowerCase(Locale.ROOT);
        return switch (normal) {
            case "warning", "warn", "medium", "concern" -> WARNING;
            case "problem", "error", "critical", "high", "issue", "bug" -> PROBLEM;
            default -> NOTE;
        };
    }

    public static String toJson(List<Finding> findings) {
        try {
            return JSON.writeValueAsString(findings);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * What {@link #toJson} wrote; empty for null, and a finding that can't be read is left out.
     */
    public static List<Finding> fromJson(String json) {
        List<Finding> findings = new ArrayList<>();
        if (StringUtils.isBlank(json)) {
            return findings;
        }
        try {
            for (JsonNode item : JSON.readTree(json)) {
                String text = item.path("text").asText("");
                if (!text.isBlank()) {
                    findings.add(new Finding(findings.size() + 1, severityOf(item.path("severity").asText()), text,
                            item.path("time").asText("")));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("The findings can't be read: " + e.getMessage(), e);
        }
        return findings;
    }

    /**
     * The findings as the model reads them, one to a line, the text cut at {@code max} characters.
     */
    public static String render(List<Finding> findings, int max) {
        StringBuilder text = new StringBuilder();
        for (Finding finding : findings) {
            text.append(finding.number()).append(". [").append(finding.severity()).append("] ")
                    .append(StringUtils.abbreviate(finding.text().strip().replaceAll("\\s+", " "), max)).append('\n');
        }
        return text.toString().stripTrailing();
    }

    /**
     * The findings as a Markdown section for the export of a run; empty for none.
     */
    public static String toMarkdown(List<Finding> findings) {
        if (findings.isEmpty()) {
            return "";
        }
        StringBuilder md = new StringBuilder("## Findings\n\n");
        for (Finding finding : findings) {
            md.append(finding.number()).append(". **").append(finding.severity()).append("** ")
                    .append(finding.text().strip().replace("\n", "\n   ")).append("\n\n");
        }
        return md.toString();
    }
}
