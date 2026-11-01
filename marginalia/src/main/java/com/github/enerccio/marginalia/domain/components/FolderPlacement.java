package com.github.enerccio.marginalia.domain.components;

/**
 * A folder of the user that is not in the trash as the database has it, to put the path of every folder together without the entities.
 *
 * @param parentId null for a folder on the top level
 */
public record FolderPlacement(Long id, String name, Long parentId) {
}
