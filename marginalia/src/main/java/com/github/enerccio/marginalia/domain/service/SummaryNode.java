package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Summary;

import java.util.List;

/**
 * A summary of the branch as the summary overview shows it. Top level nodes are the summaries generation uses; the
 * children of a meta summary are the summaries it stands in for. Nodes are compared by identity.
 */
public final class SummaryNode {

    private final ChatMessage message;
    private final int order;
    private final Summary summary;
    private final boolean replaced;
    private final List<SummaryNode> children;

    /**
     * @param message the part the summary belongs to
     * @param order   position of the part in the branch, the root is 1
     * @param summary the summary
     * @param replaced true for a summary that is not stored on the part anymore, a meta summary replaced it and keeps it
     *                 (it has no id)
     */
    public SummaryNode(ChatMessage message, int order, Summary summary, boolean replaced, List<SummaryNode> children) {
        this.message = message;
        this.order = order;
        this.summary = summary;
        this.replaced = replaced;
        this.children = children;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public int getOrder() {
        return order;
    }

    public Summary getSummary() {
        return summary;
    }

    public boolean isReplaced() {
        return replaced;
    }

    public List<SummaryNode> getChildren() {
        return children;
    }

    public long getTokens() {
        return summary.getSummaryTokens() == null ? 0 : summary.getSummaryTokens();
    }
}
