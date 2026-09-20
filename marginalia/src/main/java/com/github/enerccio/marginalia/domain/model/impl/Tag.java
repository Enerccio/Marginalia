package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "tags")
public class Tag extends ExtendableEntity {

    @Column
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        if (getId() != null && tag.getId() != null) {
            return Objects.equals(getId(), tag.getId());
        }
        return Objects.equals(getUuid(), tag.getUuid());
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : Objects.hash(getUuid());
    }
}
