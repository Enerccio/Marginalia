package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.model.impl.settings.BaseUserSetting;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * The user settings of the platform's own tests: an application has its own subclass of {@link BaseUserSetting}
 * (Marginalia's {@code UserSetting}), the platform none. Same discriminator as Marginalia's, so the table looks alike.
 */
@Entity
@DiscriminatorValue("UserSetting")
public class TestUserSetting extends BaseUserSetting {
}
