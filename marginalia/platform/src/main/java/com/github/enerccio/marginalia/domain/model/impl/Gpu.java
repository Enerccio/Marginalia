package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A GPU (or any machine that holds one model at a time) shared by the AIs and the extensions of the installation. Set up by the
 * administrator; what is loaded on it is known only while the application runs (see {@code GpuService}).
 */
@Entity
@Table(name = "gpus")
public class Gpu extends BaseEntity {

    @Column(nullable = false)
    private String name = "";

    /** Programs or people outside Marginalia use it too: what is loaded is never known, the acquire steps run before every job. */
    @Column(name = "used_outside", nullable = false)
    private boolean usedOutside;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsedOutside() {
        return usedOutside;
    }

    public void setUsedOutside(boolean usedOutside) {
        this.usedOutside = usedOutside;
    }
}
