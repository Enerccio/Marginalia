package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "lorebooks")
public class Lorebook extends ExtendableEntity {

    @Lob
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Lorebook> subbooks;

    private boolean enabled = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Lorebook> getSubbooks() {
        return subbooks;
    }

    public void setSubbooks(List<Lorebook> subbooks) {
        this.subbooks = subbooks;
    }
}
