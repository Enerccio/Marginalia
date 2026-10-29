package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;

import java.util.List;
import java.util.Map;

/**
 * The part of the list queries that restricts them to a folder, the same for the books ({@code m.folder}) and the folders
 * ({@code f.parent}).
 */
final class FolderScope {

    private FolderScope() {
    }

    /**
     * Adds the condition of the resolved scope of the filter, if it restricts anything, and the one of the book uuids.
     *
     * @param relation the path to the folder of the row: {@code m.folder} or {@code f.parent}
     * @param uuid     the path to the uuid of the row, null when the filter's uuids don't apply
     */
    static void add(List<String> ands, Map<String, Object> parameters, ManuscriptFilterValues filter, String relation, String uuid) {
        if (filter == null) {
            return;
        }
        if (filter.isScoped() && filter.getFolderIds() != null) {
            boolean ids = !filter.getFolderIds().isEmpty();
            if (ids && filter.isTopLevel()) {
                ands.add("(" + relation + ".id IN :scopeIds OR " + relation + " IS NULL)");
            } else if (ids) {
                ands.add(relation + ".id IN :scopeIds");
            } else if (filter.isTopLevel()) {
                ands.add(relation + " IS NULL");
            } else {
                ands.add("1 = 0");
            }
            if (ids) {
                parameters.put("scopeIds", filter.getFolderIds());
            }
        }
        if (uuid != null && filter.getUuids() != null) {
            if (filter.getUuids().isEmpty()) {
                ands.add("1 = 0");
            } else {
                ands.add(uuid + " IN :scopeUuids");
                parameters.put("scopeUuids", filter.getUuids());
            }
        }
    }
}
