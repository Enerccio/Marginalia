package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.PassageKind;
import com.github.enerccio.marginalia.domain.service.PassageKinds;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PassageKindsImpl implements PassageKinds {

    private final Map<String, PassageKind> kinds = new ConcurrentHashMap<>();

    @Override
    public void register(PassageKind kind) {
        kinds.put(kind.id(), kind);
    }

    @Override
    public void unregister(PassageKind kind) {
        // only the one that is registered: a newer copy of the extension may have taken the id already
        kinds.remove(kind.id(), kind);
    }

    @Override
    public PassageKind get(String id) {
        return id == null ? null : kinds.get(id);
    }

    @Override
    public Collection<PassageKind> all() {
        return List.copyOf(kinds.values());
    }

}
