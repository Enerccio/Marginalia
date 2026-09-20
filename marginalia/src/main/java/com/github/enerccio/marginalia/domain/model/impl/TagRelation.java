package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "t2e", indexes = {
        @Index(name = "ix__tag__id_clazz", columnList = "id,clazz"),
        @Index(name = "ix__tag__id_clazz_neg", columnList = "id,clazz,negative")
})
public class TagRelation extends ExtendableEntity {

    @ManyToOne
    private Tag tag;

    private Long objectId;

    @Column(length = 255)
    private String clazz;

    @Column
    private boolean negative = false;

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }
}
