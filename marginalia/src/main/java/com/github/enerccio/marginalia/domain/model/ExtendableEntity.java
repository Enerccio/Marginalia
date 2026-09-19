package com.github.enerccio.marginalia.domain.model;

import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.*;

@MappedSuperclass
@EntityListeners({ ExtendableEntityListener.class })
public class ExtendableEntity extends OwnedEntity {

    @Column(length = 268435456)
    private byte[] extendedContent;

    @Transient
    @ExtendedAttribute(inject = true, injectPrefix = "attributes")
    private JsonObject attributes = new JsonObject();

    public byte[] getExtendedContent() {
        return extendedContent;
    }

    public void setExtendedContent(byte[] extendedContent) {
        this.extendedContent = extendedContent;
    }

    public JsonObject getAttributes() {
        return attributes;
    }

    public void setAttributes(JsonObject attributes) {
        this.attributes = attributes;
    }
}
