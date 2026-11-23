package com.github.enerccio.marginalia.extensions.statetracker.model;

import java.util.UUID;

/**
 * One value of a category of a character: the whole value of a single or description category, one entry of a list. It is
 * <b>active</b> (the current state, sent to the model) or <b>history</b> (replaced or removed; kept and shown, never sent).
 * Nothing the user wrote is lost: a replaced value goes to the history, only a delete removes it for good.
 */
public class StateItem {

    /** Why an item left the current state. */
    public enum EndReason {
        /** A new value of a single or description category took its place. */
        REPLACED,
        /** It was removed (a list entry given away, a value cleared). */
        REMOVED,
        /** The tidy dropped it (no longer true or relevant), replaced it by a newer value or merged it with other entries. */
        TIDIED
    }

    private String id;
    private String categoryId;
    private String text;
    /** The tidy leaves a locked item alone (extraction can still change it). */
    private boolean locked;
    private boolean active = true;
    private EndReason endReason;
    private long created;
    private long ended;
    /** The part (uuid) the model read this value in; {@code null} for a value the user wrote. */
    private String sourceMessage;
    /** The part (uuid) the model read the end of this value in; {@code null} when it is current or the user ended it. */
    private String endedBy;
    /** The tidy wrote this value (an update or a merge) after {@link #sourceMessage}, the story did not state it there. */
    private boolean fromTidy;
    /** The story day the value was set on (the clock after its part), {@code null} when the book had no clock. */
    private Integer storyDay;

    public StateItem() {
    }

    public StateItem(String categoryId, String text, long now) {
        this.id = UUID.randomUUID().toString();
        this.categoryId = categoryId;
        this.text = text;
        this.created = now;
    }

    /** Leaves the current state for the history. */
    void end(EndReason reason, long now) {
        end(reason, now, null);
    }

    /**
     * @param part the part the end was read in, {@code null} when the user ended it
     */
    void end(EndReason reason, long now, String part) {
        this.active = false;
        this.endReason = reason;
        this.ended = now;
        this.endedBy = part;
    }

    /** Back into the current state. */
    void reactivate() {
        this.active = true;
        this.endReason = null;
        this.ended = 0;
        this.endedBy = null;
    }

    /** Whether the model read this value in a part (it is taken back with that part). */
    public boolean isFromStory() {
        return sourceMessage != null;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    /** {@code null} makes the value the user's (an edit): it is no longer the tidy's either. */
    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage;
        if (sourceMessage == null) {
            this.fromTidy = false;
        }
    }

    public Integer getStoryDay() {
        return storyDay;
    }

    public void setStoryDay(Integer storyDay) {
        this.storyDay = storyDay;
    }

    public boolean isFromTidy() {
        return fromTidy;
    }

    public void setFromTidy(boolean fromTidy) {
        this.fromTidy = fromTidy;
    }

    public String getEndedBy() {
        return endedBy;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isActive() {
        return active;
    }

    public EndReason getEndReason() {
        return endReason;
    }

    public long getCreated() {
        return created;
    }

    public long getEnded() {
        return ended;
    }
}
