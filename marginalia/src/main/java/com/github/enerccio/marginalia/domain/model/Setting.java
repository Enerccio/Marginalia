package com.github.enerccio.marginalia.domain.model;

import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import jakarta.persistence.*;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.DiscriminatorOptions;

@Entity
@Table(name = "settings")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("DTYPE")
@DiscriminatorOptions(insert = true)
@CleanupReference(field = "owner", value = Policy.OWNED_BY)
public class Setting extends ExtendableEntity {

    public Setting() {
        this.dtype = this.getClass().getSimpleName();
    }

    @Column(name = "DTYPE", length = 255, updatable = false)
    protected String dtype;

    @Column(length = 64)
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
