package com.github.enerccio.marginalia.domain.model;

import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.*;

import java.util.Date;

@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String uuid;

    @Column(nullable = false, name = "is_deleted")
    private boolean deleted = false;

    @SuppressWarnings("deprecation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation;

    @SuppressWarnings("deprecation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modification;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getModification() {
        return modification;
    }

    public void setModification(Date modification) {
        this.modification = modification;
    }
}
