package com.github.enerccio.marginalia.extensions.lorebookvcs.model;

import java.util.ArrayList;
import java.util.List;

public class LoreEntryVCSData {

    private String entryUuid;
    private List<LoreEntryRevision> revisions = new ArrayList<>();
    private int currentRevision = -1;

    public String getEntryUuid() { return entryUuid; }
    public void setEntryUuid(String entryUuid) { this.entryUuid = entryUuid; }

    public List<LoreEntryRevision> getRevisions() { return revisions; }
    public void setRevisions(List<LoreEntryRevision> revisions) { this.revisions = revisions; }

    public int getCurrentRevision() { return currentRevision; }
    public void setCurrentRevision(int currentRevision) { this.currentRevision = currentRevision; }

    public LoreEntryRevision getCurrent() {
        if (currentRevision >= 0 && currentRevision < revisions.size()) {
            return revisions.get(currentRevision);
        }
        return null;
    }
}