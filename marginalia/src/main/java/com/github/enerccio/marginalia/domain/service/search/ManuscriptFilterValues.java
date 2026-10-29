package com.github.enerccio.marginalia.domain.service.search;

import com.github.enerccio.marginalia.domain.model.impl.Folder;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The filter of the list of books (and of the folders in it): name, tags and where they are. Without {@link #inFolder} a
 * search covers everything.
 */
public class ManuscriptFilterValues {

    private String name;
    private List<String> tags;

    private boolean scoped;
    private Folder folder;
    private boolean includeSubfolders;

    // what the scope comes to, set by FolderService.resolveScope; null folderIds = no restriction
    private Set<Long> folderIds;
    private boolean topLevel;

    // only these books (the favorites), null = no restriction
    private Collection<String> uuids;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Restricts the search to the folder (null is the top level): its books and folders directly, or with
     * {@code includeSubfolders} everything below it (below the top level: everything).
     */
    public ManuscriptFilterValues inFolder(Folder folder, boolean includeSubfolders) {
        this.scoped = true;
        this.folder = folder;
        this.includeSubfolders = includeSubfolders;
        return this;
    }

    public boolean isScoped() {
        return scoped;
    }

    public Folder getFolder() {
        return folder;
    }

    public boolean isIncludeSubfolders() {
        return includeSubfolders;
    }

    public Set<Long> getFolderIds() {
        return folderIds;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    /**
     * Sets what the scope comes to, for the repository: the ids of the folders that match (null: any) and whether the top
     * level matches too.
     */
    public void setResolvedScope(Set<Long> folderIds, boolean topLevel) {
        this.folderIds = folderIds;
        this.topLevel = topLevel;
    }

    public Collection<String> getUuids() {
        return uuids;
    }

    public void setUuids(Collection<String> uuids) {
        this.uuids = uuids;
    }

    public ManuscriptFilterValues copy() {
        ManuscriptFilterValues copy = new ManuscriptFilterValues();
        copy.name = name;
        copy.tags = tags;
        copy.scoped = scoped;
        copy.folder = folder;
        copy.includeSubfolders = includeSubfolders;
        copy.folderIds = folderIds;
        copy.topLevel = topLevel;
        copy.uuids = uuids;
        return copy;
    }
}
