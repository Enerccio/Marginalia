package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.ThemeMode;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The color scheme of the UI is a setting of the user: dark until they choose, and the choice is kept.
 */
class ThemeModeSettingTest extends MarginaliaTestBase {

    @Autowired
    private SettingService settingService;

    @Test
    void darkUntilChosenThenKept() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        assertThat(setting.getThemeMode()).isEqualTo(ThemeMode.DARK);

        setting.setThemeMode(setting.getThemeMode().toggled());
        settingService.save(setting);

        assertThat(settingService.getOrCreate(UserSetting.class).getThemeMode()).isEqualTo(ThemeMode.LIGHT);
    }
}
