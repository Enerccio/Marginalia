package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.log.LogEntry;

/**
 * One change of the state that a run applied without asking (see {@code StateLog}): the value it made ({@link #getItemId()},
 * {@link #getText()}) and/or the value it ended ({@link #getEndedItemId()}, {@link #getEndedText()}), recorded on the part it was
 * read in ({@link #getMessageUuid()}; for the tidy the last processed part).
 */
public class StateLogEntry extends LogEntry {

    /** What the change did, as the review named it. */
    public enum Action {
        SET, CLEAR, ADD, REMOVE, REPLACE
    }

    private Action action;
    private String categoryId;
    private String categoryName;
    private String endedItemId;
    private String endedText;
    private boolean tidy;

    public StateLogEntry() {
    }

    /**
     * @param created the value the change made, {@code null} for none
     * @param ended   the value the change ended, {@code null} for none
     */
    public StateLogEntry(long time, Action action, TrackedCharacter character, Category category, StateItem created, StateItem ended,
                         String part, String evidence, boolean tidy) {
        super(time, character.getId(), character.getName(), created == null ? null : created.getId(),
                created == null ? null : created.getText(), part, evidence);
        this.action = action;
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.endedItemId = ended == null ? null : ended.getId();
        this.endedText = ended == null ? null : ended.getText();
        this.tidy = tidy;
    }

    public Action getAction() {
        return action;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getEndedItemId() {
        return endedItemId;
    }

    public String getEndedText() {
        return endedText;
    }

    /** Whether the tidy made the change (not the reading of a part). */
    public boolean isTidy() {
        return tidy;
    }
}
