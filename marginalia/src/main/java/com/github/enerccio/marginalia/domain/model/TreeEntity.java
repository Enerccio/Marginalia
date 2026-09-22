package com.github.enerccio.marginalia.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class TreeEntity extends ExtendableEntity {

    @Column(length = 255)
    private String tree;

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }
}
