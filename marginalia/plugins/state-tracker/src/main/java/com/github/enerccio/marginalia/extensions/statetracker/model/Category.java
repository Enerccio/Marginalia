package com.github.enerccio.marginalia.extensions.statetracker.model;

import java.util.Objects;
import java.util.UUID;

/**
 * What can be tracked about a character (clothes, inventory, the equipped weapon...). The same class holds the categories of the
 * user (the catalog in the user settings) and the copies a book keeps of the categories it uses: a book copy names the user
 * category it came from in {@link #getSourceId()}, a category made for one book only has none.
 */
public class Category {

    private String id;
    private String name;
    private CategoryKind kind = CategoryKind.SINGLE;
    /** For the model: what belongs in the category and what does not. */
    private String instruction = "";
    /** Change it only when the text says so (on), or also on obvious consequences (off). */
    private boolean strict = true;
    /** The block says "none" when the character has no value of it (the lost weapon), instead of leaving it out. */
    private boolean showWhenEmpty;
    /** In a book: the id of the user category it was copied from; {@code null} for a category of this book only, and in the catalog. */
    private String sourceId;

    public Category() {
    }

    public Category(String id, String name, CategoryKind kind, String instruction) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.instruction = instruction;
    }

    /** A new category with a fresh id. */
    public static Category create(String name, CategoryKind kind, String instruction) {
        return new Category(UUID.randomUUID().toString(), name, kind, instruction);
    }

    /** A copy for a book: a new id, linked to this category. */
    public Category copyForBook() {
        Category copy = new Category(UUID.randomUUID().toString(), name, kind, instruction);
        copy.strict = strict;
        copy.showWhenEmpty = showWhenEmpty;
        copy.sourceId = id;
        return copy;
    }

    /** A copy for the user's catalog: a new id, no link. */
    public Category copyForCatalog() {
        Category copy = new Category(UUID.randomUUID().toString(), name, kind, instruction);
        copy.strict = strict;
        copy.showWhenEmpty = showWhenEmpty;
        return copy;
    }

    /** Whether name, kind, instruction and the switches are the same (the ids and the link are not compared). */
    public boolean sameDefinition(Category other) {
        return other != null && Objects.equals(name, other.name) && kind == other.kind
                && Objects.equals(getInstruction(), other.getInstruction()) && strict == other.strict
                && showWhenEmpty == other.showWhenEmpty;
    }

    /** Takes the definition of {@code other} (name, kind, instruction, switches); the id and the link stay. */
    public void updateFrom(Category other) {
        this.name = other.name;
        this.kind = other.kind;
        this.instruction = other.getInstruction();
        this.strict = other.strict;
        this.showWhenEmpty = other.showWhenEmpty;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryKind getKind() {
        return kind == null ? CategoryKind.SINGLE : kind;
    }

    public void setKind(CategoryKind kind) {
        this.kind = kind;
    }

    public String getInstruction() {
        return instruction == null ? "" : instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isShowWhenEmpty() {
        return showWhenEmpty;
    }

    public void setShowWhenEmpty(boolean showWhenEmpty) {
        this.showWhenEmpty = showWhenEmpty;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
