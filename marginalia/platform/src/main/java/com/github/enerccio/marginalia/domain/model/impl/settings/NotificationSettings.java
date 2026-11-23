package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * What a user wants to be told about with a desktop notification of the browser when it is over (see {@code NotificationService}): whether
 * notifications are on at all and which of the sources (a generation, an annotation...) they are on for. A row of the settings table like
 * {@link StylesSetting}, one per user, created when the user first saves it. Read and written through {@code NotificationService}.
 */
@Entity
@DiscriminatorValue("NotificationSettings")
public class NotificationSettings extends Setting {

    /**
     * The master switch; the sources below are ignored while it is off. The browser has to allow the notifications too, which is
     * decided per browser and not stored here.
     */
    @ExtendedAttribute
    @Transient
    private Boolean enabled = false;

    /**
     * The keys of the notifications the user chose, see {@code NotificationService.Entry#key()}: the id of a source, and the id with the suffix
     * {@code .auto} for the automatic run of a source that has one. Keys of sources that are not registered (an extension that is not
     * loaded) stay, they apply again when it is loaded.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "chosen")
    @Transient
    private List<String> chosen = new ArrayList<>();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getChosen() {
        return chosen;
    }

    public void setChosen(List<String> chosen) {
        this.chosen = chosen == null ? new ArrayList<>() : chosen;
    }
}
