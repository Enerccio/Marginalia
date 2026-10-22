package com.github.enerccio.marginalia.domain.service.search;

import java.util.Date;
import java.util.List;

/**
 * What the advanced search of the book list asks for; every value that is null (or an empty list, a blank text) is not
 * checked. A book is found when it passes all the checks.
 * <p>
 * Dates are instants: {@code from} is included, {@code to} is not (the UI passes the start of the day after the one the
 * user picked).
 */
public class ManuscriptSearchCriteria {

    /**
     * Whether a book has to carry all the tags of the list or any of them.
     */
    public enum TagMatch {
        ALL, ANY
    }

    private String name;
    private List<String> includeTags = List.of();
    private TagMatch includeMatch = TagMatch.ALL;
    private List<String> excludeTags = List.of();
    private TagMatch excludeMatch = TagMatch.ANY;
    private Date createdFrom;
    private Date createdTo;
    private Date modifiedFrom;
    private Date modifiedTo;
    private Integer minParts;
    private Integer maxParts;
    private String fulltext;
    private boolean allBranches;

    /**
     * Text the name of the book contains (ignoring case, {@code *} is a wildcard).
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Values of the tags the book has to carry, see {@link #getIncludeMatch()}.
     */
    public List<String> getIncludeTags() {
        return includeTags;
    }

    public void setIncludeTags(List<String> includeTags) {
        this.includeTags = includeTags == null ? List.of() : includeTags;
    }

    /**
     * {@code ALL}: the book carries every included tag; {@code ANY}: at least one of them.
     */
    public TagMatch getIncludeMatch() {
        return includeMatch;
    }

    public void setIncludeMatch(TagMatch includeMatch) {
        this.includeMatch = includeMatch == null ? TagMatch.ALL : includeMatch;
    }

    /**
     * Values of the tags the book must not carry, see {@link #getExcludeMatch()}.
     */
    public List<String> getExcludeTags() {
        return excludeTags;
    }

    public void setExcludeTags(List<String> excludeTags) {
        this.excludeTags = excludeTags == null ? List.of() : excludeTags;
    }

    /**
     * {@code ANY}: a book that carries at least one excluded tag is left out; {@code ALL}: only a book that carries
     * every excluded tag is left out.
     */
    public TagMatch getExcludeMatch() {
        return excludeMatch;
    }

    public void setExcludeMatch(TagMatch excludeMatch) {
        this.excludeMatch = excludeMatch == null ? TagMatch.ANY : excludeMatch;
    }

    public Date getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(Date createdFrom) {
        this.createdFrom = createdFrom;
    }

    public Date getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(Date createdTo) {
        this.createdTo = createdTo;
    }

    public Date getModifiedFrom() {
        return modifiedFrom;
    }

    public void setModifiedFrom(Date modifiedFrom) {
        this.modifiedFrom = modifiedFrom;
    }

    public Date getModifiedTo() {
        return modifiedTo;
    }

    public void setModifiedTo(Date modifiedTo) {
        this.modifiedTo = modifiedTo;
    }

    /**
     * The least number of parts (the parts of all the branches that are not deleted) of the book.
     */
    public Integer getMinParts() {
        return minParts;
    }

    public void setMinParts(Integer minParts) {
        this.minParts = minParts;
    }

    /**
     * The most parts the book has.
     */
    public Integer getMaxParts() {
        return maxParts;
    }

    public void setMaxParts(Integer maxParts) {
        this.maxParts = maxParts;
    }

    /**
     * Words and {@code "phrases"} the text of the book has to contain, see {@link FulltextQuery}.
     */
    public String getFulltext() {
        return fulltext;
    }

    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }

    /**
     * Whether the {@link #getFulltext() text} is searched in the parts of all the branches; otherwise only in the
     * branch the book is at (the one of its active part).
     */
    public boolean isAllBranches() {
        return allBranches;
    }

    public void setAllBranches(boolean allBranches) {
        this.allBranches = allBranches;
    }
}
