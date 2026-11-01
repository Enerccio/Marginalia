package com.github.enerccio.marginalia.domain.components;

/**
 * Where a book is, straight from the database (not from the entities a session may hold): the name and the folder of a book of the
 * user that is not in the trash, for the name and path the writing statistics copy into their rows.
 *
 * @param folderId null for a book on the top level
 */
public record BookPlacement(String uuid, String name, Long folderId) {
}
