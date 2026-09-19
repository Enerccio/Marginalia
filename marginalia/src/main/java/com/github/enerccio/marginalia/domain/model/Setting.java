package com.github.enerccio.marginalia.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Setting extends ExtendableEntity {

    @Column(length = 64)
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
