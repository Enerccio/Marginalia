package com.github.enerccio.marginalia.extensions.storynotes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One page of the notes of a book: a title (unique in the book, what {@code [[links]]} name), Markdown text, tags and
 * whether the assistants (commands, the plot wizard, agents) may see it.
 */
public class NotePage {

    private String uuid = UUID.randomUUID().toString();
    private String title = "";
    private String content = "";
    private List<String> tags = new ArrayList<>();
    /**
     * Whether commands, agents and the plot wizard can see the page. A page that is not visible is for the author only.
     */
    private boolean aiVisible = true;
    /**
     * Whether the last change came from a command (an agent, the plot wizard, the command bar) and not from the author
     * in the editor; cleared when the author saves the page.
     */
    private boolean changedByCommand;
    private long created = System.currentTimeMillis();
    private long modified = created;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public boolean isAiVisible() {
        return aiVisible;
    }

    public void setAiVisible(boolean aiVisible) {
        this.aiVisible = aiVisible;
    }

    public boolean isChangedByCommand() {
        return changedByCommand;
    }

    public void setChangedByCommand(boolean changedByCommand) {
        this.changedByCommand = changedByCommand;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    /** Marks the page as changed now. */
    public void touch() {
        long now = System.currentTimeMillis();
        // two changes in the same millisecond are still two versions (the editor compares the times to find a conflict)
        modified = Math.max(now, modified + 1);
    }

    public NotePage copy() {
        NotePage copy = new NotePage();
        copy.uuid = uuid;
        copy.title = title;
        copy.content = content;
        copy.tags = new ArrayList<>(getTags());
        copy.aiVisible = aiVisible;
        copy.changedByCommand = changedByCommand;
        copy.created = created;
        copy.modified = modified;
        return copy;
    }
}
