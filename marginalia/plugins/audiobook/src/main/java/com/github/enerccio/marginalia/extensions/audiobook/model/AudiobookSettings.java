package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.ArrayList;
import java.util.List;

/** What the user keeps for the plugin, in the attributes of the user settings. */
public class AudiobookSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.audiobook";

    private List<BackendConfig> backends = new ArrayList<>();
    private String selectedBackendId;
    /** The id of the {@link AutoMode} for the books that neither they nor a folder say (the user level of the inherited setting). */
    private String autoMode;
    /** The voices of the user; the cast of a book names them by id. */
    private List<Actor> actors = new ArrayList<>();

    public List<BackendConfig> getBackends() {
        if (backends == null) {
            backends = new ArrayList<>();
        }
        return backends;
    }

    public void setBackends(List<BackendConfig> backends) {
        this.backends = backends;
    }

    public String getSelectedBackendId() {
        return selectedBackendId;
    }

    public void setSelectedBackendId(String selectedBackendId) {
        this.selectedBackendId = selectedBackendId;
    }

    public AutoMode getAutoMode() {
        return AutoMode.of(autoMode);
    }

    public void setAutoMode(AutoMode mode) {
        this.autoMode = mode == null ? null : mode.id();
    }

    public List<Actor> getActors() {
        if (actors == null) {
            actors = new ArrayList<>();
        }
        // by name, as the user reads them (the case and the accents do not count)
        java.text.Collator collator = java.text.Collator.getInstance();
        collator.setStrength(java.text.Collator.PRIMARY);
        actors.sort((a, b) -> collator.compare(a.getName() == null ? "" : a.getName(), b.getName() == null ? "" : b.getName()));
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public Actor findActor(String id) {
        if (id == null) {
            return null;
        }
        return getActors().stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
    }

    /** Puts the actor in place of the one with its id, or at the end. */
    public void putActor(Actor actor) {
        List<Actor> list = getActors();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(actor.getId())) {
                list.set(i, actor);
                return;
            }
        }
        list.add(actor);
    }

    public BackendConfig find(String id) {
        if (id == null) {
            return null;
        }
        return getBackends().stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }

    /** The backend picked, else the first one, null when there is none. */
    public BackendConfig selected() {
        BackendConfig picked = find(selectedBackendId);
        return picked != null ? picked : getBackends().stream().findFirst().orElse(null);
    }
}
