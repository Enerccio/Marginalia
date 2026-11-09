package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class PlannerServiceImpl implements PlannerService {

    private static final Logger log = LoggerFactory.getLogger(PlannerServiceImpl.class);

    @Autowired
    private ManuscriptService manuscriptService;

    /** Who wants to know that the scenes of a book changed, by the uuid of the book. */
    private final Map<String, Set<Runnable>> listeners = new ConcurrentHashMap<>();

    @Override
    @NoTx
    public List<PlannedScene> list(Manuscript book) throws Exception {
        Manuscript current = manuscriptService.find(book);
        List<PlannedScene> scenes = new ArrayList<>();
        for (PlannedScene scene : (current != null ? current : book).getPlan()) {
            scenes.add(scene.copy());
        }
        return scenes;
    }

    @Override
    @NoTx
    public PlannedScene add(Manuscript book, PlannedScene scene) throws Exception {
        PlannedScene added = clean(scene);
        added.setDone(false);
        change(book, scenes -> {
            if (scenes.size() >= MAX_SCENES) {
                throw new IllegalStateException("The planner has " + MAX_SCENES + " scenes already");
            }
            scenes.add(added);
        });
        return added.copy();
    }

    @Override
    @NoTx
    public void update(Manuscript book, PlannedScene scene) throws Exception {
        PlannedScene changed = clean(scene);
        changed.setUuid(scene.getUuid());
        changed.setDone(scene.isDone());
        change(book, scenes -> scenes.set(indexOf(scenes, scene.getUuid()), changed));
    }

    @Override
    @NoTx
    public void setDone(Manuscript book, String uuid, boolean done) throws Exception {
        change(book, scenes -> scenes.get(indexOf(scenes, uuid)).setDone(done));
    }

    @Override
    @NoTx
    public void delete(Manuscript book, String uuid) throws Exception {
        change(book, scenes -> scenes.removeIf(scene -> scene.getUuid().equals(uuid)));
    }

    @Override
    @NoTx
    public void move(Manuscript book, String uuid, int by) throws Exception {
        change(book, scenes -> {
            int from = indexOf(scenes, uuid);
            int to = Math.max(0, Math.min(scenes.size() - 1, from + by));
            scenes.add(to, scenes.remove(from));
        });
    }

    @Override
    public Runnable onChange(Manuscript book, Runnable listener) {
        String key = book.getUuid();
        listeners.computeIfAbsent(key, _ -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> listeners.computeIfPresent(key, (_, set) -> {
            set.remove(listener);
            return set.isEmpty() ? null : set;
        });
    }

    // --- internals ---

    /** Reads the book again, lets the change work on its scenes and saves it, then tells the listeners. */
    private void change(Manuscript book, Consumer<List<PlannedScene>> change) throws Exception {
        manuscriptService.update(book, fresh -> {
            List<PlannedScene> scenes = new ArrayList<>();
            fresh.getPlan().forEach(scene -> scenes.add(scene.copy()));
            change.accept(scenes);
            fresh.setPlan(scenes);
        });
        Set<Runnable> toTell = listeners.get(book.getUuid());
        if (toTell != null) {
            toTell.forEach(listener -> {
                try {
                    listener.run();
                } catch (RuntimeException e) {
                    log.warn("A view of the planner failed to refresh: {}", e.getMessage());
                }
            });
        }
    }

    private static int indexOf(List<PlannedScene> scenes, String uuid) {
        for (int i = 0; i < scenes.size(); i++) {
            if (scenes.get(i).getUuid().equals(uuid)) {
                return i;
            }
        }
        throw new IllegalArgumentException("The planner has no such scene");
    }

    /** A new scene with the texts of the argument stripped and checked, a new uuid and not done. */
    private static PlannedScene clean(PlannedScene scene) {
        String title = StringUtils.strip(scene.getTitle());
        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("The title of a scene is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("The title is longer than " + MAX_TITLE_LENGTH + " characters");
        }
        return new PlannedScene(title,
                text(scene.getSceneSetting()), text(scene.getPovCharacter()), text(scene.getPresentCharacters()), text(scene.getInstructions()));
    }

    private static String text(String text) {
        String stripped = StringUtils.strip(text);
        if (stripped != null && stripped.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("A text of a scene is longer than " + MAX_TEXT_LENGTH + " characters");
        }
        return stripped == null ? "" : stripped;
    }
}
