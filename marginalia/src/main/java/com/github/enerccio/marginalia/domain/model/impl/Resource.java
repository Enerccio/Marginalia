package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "resources", indexes = {
        @Index(name = "resource_is_deleted_ix", columnList = "is_deleted"),
        @Index(name = "resource_user_id_ix", columnList = "userId"),
        @Index(name = "resource_hash_ix", columnList = "hash")
})
public class Resource extends OwnedEntity {

    private String mimeType;

    private String path;

    @Lob
    private String originalName;

    @Lob
    private String hash;

    private long size;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
