package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
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

    @Autowired
    private ChatMessageService chatMessageService;

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
    public PlannedScene insert(Manuscript book, String afterUuid, PlannedScene scene) throws Exception {
        PlannedScene added = clean(scene);
        added.setDone(false);
        change(book, scenes -> {
            int after = indexOf(scenes, afterUuid);
            if (scenes.size() >= MAX_SCENES) {
                throw new IllegalStateException("The planner has " + MAX_SCENES + " scenes already");
            }
            scenes.add(after + 1, added);
        });
        return added.copy();
    }

    @Override
    @NoTx
    public PlannedScene merge(Manuscript book, String firstUuid, String secondUuid, PlannedScene merged) throws Exception {
        if (StringUtils.equals(firstUuid, secondUuid)) {
            throw new IllegalArgumentException("A scene can't be merged with itself");
        }
        PlannedScene changed = clean(merged);
        changed.setUuid(firstUuid);
        change(book, scenes -> {
            int first = indexOf(scenes, firstUuid);
            int second = indexOf(scenes, secondUuid);
            changed.setDone(scenes.get(first).isDone() && scenes.get(second).isDone());
            scenes.set(first, changed);
            scenes.remove(second);
        });
        return changed.copy();
    }

    @Override
    @NoTx
    public int importBranch(Manuscript book) throws Exception {
        Manuscript current = manuscriptService.find(book);
        List<PlannedScene> imported = new ArrayList<>();
        if (current != null && current.getActiveLeaf() != null) {
            for (ChatMessage part : chatMessageService.getBranchFromLeaf(current.getActiveLeaf())) {
                String setting = cut(part.getSceneSetting());
                String pov = cut(part.getPovCharacter());
                String present = cut(part.getPresentCharacters());
                String instructions = cut(part.getInstructions());
                String named = StringUtils.firstNonBlank(instructions, setting, pov, present);
                if (named == null) {
                    // a part the author wrote or imported without turn details: nothing to plan
                    continue;
                }
                String firstLine = named.lines().filter(StringUtils::isNotBlank).findFirst().orElse(named).strip();
                imported.add(new PlannedScene(StringUtils.abbreviate(firstLine, "…", IMPORTED_TITLE_LENGTH), setting, pov, present, instructions));
            }
        }
        if (imported.isEmpty()) {
            return 0;
        }
        change(book, scenes -> {
            if (scenes.size() + imported.size() > MAX_SCENES) {
                throw new IllegalStateException("The planner can't have more than " + MAX_SCENES + " scenes");
            }
            scenes.addAll(imported);
        });
        return imported.size();
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

    /** The text stripped and cut to the longest one a scene takes. */
    private static String cut(String text) {
        return StringUtils.truncate(StringUtils.trimToEmpty(text), MAX_TEXT_LENGTH);
    }

    private static String text(String text) {
        String stripped = StringUtils.strip(text);
        if (stripped != null && stripped.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("A text of a scene is longer than " + MAX_TEXT_LENGTH + " characters");
        }
        return stripped == null ? "" : stripped;
    }
}
