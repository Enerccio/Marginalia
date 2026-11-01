package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;

import java.util.List;

/**
 * The text styles ({@link PartStyle}) the current user can give their books: the built in ones of the {@link PartStyleRegistry} and the
 * ones they made, which are kept in their {@code StylesSetting} together with the style the books use unless they choose another.
 * Built in styles can be chosen and duplicated but not changed or deleted.
 */
public interface PartStyleService {

    /**
     * All styles the user has, the built in ones first and then their own, each group sorted by name.
     */
    List<PartStyle> list() throws Exception;

    /**
     * The style of the id (built in, or of the user), null when there is none.
     */
    PartStyle find(String id) throws Exception;

    /**
     * The style the author chose for the book, null when they chose none (the book follows the default). A book with the old "book
     * style" switch on and no choice yet has chosen the book style; a chosen style that does not exist any more counts as not chosen.
     */
    PartStyle findChosen(Manuscript manuscript) throws Exception;

    /**
     * The style the story of the book shows: the one the author chose for the book, else the default of the user, else the plain look.
     * A chosen style that does not exist any more (deleted, or of another user after an import) counts as not chosen. A book that
     * had the old "book style" switch on and no choice yet gets the book style.
     */
    PartStyle resolve(Manuscript manuscript) throws Exception;

    /**
     * The style the books use unless they choose another; the plain look when the user set none (or deleted it).
     */
    PartStyle getDefault() throws Exception;

    /**
     * The id of the default style, null when none was set.
     */
    String getDefaultId() throws Exception;

    /**
     * Makes the style (built in or own) the default one of the user's books, null removes the default.
     *
     * @throws IllegalArgumentException when no style has the id
     */
    void setDefault(String id) throws Exception;

    /**
     * Stores the style of the user: a new one when it has no id (it gets one), else the style with the id is replaced. The name must
     * not be blank.
     *
     * @throws IllegalArgumentException for a built in style, a blank name or custom CSS with unbalanced braces
     */
    PartStyle save(PartStyle style) throws Exception;

    /**
     * A new style of the user with the options and CSS of the style of the id (built in or own).
     *
     * @throws IllegalArgumentException when no style has the id
     */
    PartStyle duplicate(String id, String name) throws Exception;

    /**
     * Deletes the style of the user; if it was the default, the default is removed. Books that chose it show the default style.
     *
     * @throws IllegalArgumentException for a built in style or an unknown id
     */
    void delete(String id) throws Exception;
}
