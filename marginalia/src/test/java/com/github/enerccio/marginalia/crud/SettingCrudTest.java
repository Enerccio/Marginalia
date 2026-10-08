package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SettingCrudTest extends ExtendableCrudContract<Setting> {

    @Autowired
    private SettingService settingService;

    @Override
    protected OwnedService<Setting, ?> service() {
        return settingService;
    }

    @Override
    protected Setting newEntity() {
        UserSetting setting = new UserSetting();
        // a key getOrCreate never looks up, so the contract tests don't clash with it
        setting.setKey(uniqueName("crud"));
        setting.setDefaultModel(11L);
        setting.setDefaultProtocol(12L);
        setting.setMasterTemplate("template");
        setting.setDefaultPov("pov");
        setting.setDefaultTense("tense");
        setting.setDefaultStyle("style");
        setting.setDefaultUserPrompt("user prompt");
        setting.setDefaultSummaryPrompt("summary prompt");
        setting.setBackupStrategy(BackupStrategy.AFTER_N_MESSAGES);
        setting.setBackupStrategyValue("20");
        return setting;
    }

    @Override
    protected void assertCreated(Setting loaded) {
        assertThat(loaded).isInstanceOf(UserSetting.class);
        UserSetting setting = (UserSetting) loaded;
        assertThat(setting.getKey()).startsWith("crud-");
        assertThat(setting.getDefaultModel()).isEqualTo(11L);
        assertThat(setting.getDefaultProtocol()).isEqualTo(12L);
        assertThat(setting.getMasterTemplate()).isEqualTo("template");
        assertThat(setting.getDefaultPov()).isEqualTo("pov");
        assertThat(setting.getDefaultTense()).isEqualTo("tense");
        assertThat(setting.getDefaultStyle()).isEqualTo("style");
        assertThat(setting.getDefaultUserPrompt()).isEqualTo("user prompt");
        assertThat(setting.getDefaultSummaryPrompt()).isEqualTo("summary prompt");
        assertThat(setting.getBackupStrategy()).isEqualTo(BackupStrategy.AFTER_N_MESSAGES);
        assertThat(setting.getBackupStrategyValue()).isEqualTo("20");
    }

    @Override
    protected void modify(Setting entity) {
        UserSetting setting = (UserSetting) entity;
        setting.setDefaultModel(null);
        setting.setDefaultPov("changed pov");
        setting.setBackupStrategy(BackupStrategy.DISABLED);
    }

    @Override
    protected void assertModified(Setting loaded) {
        UserSetting setting = (UserSetting) loaded;
        assertThat(setting.getDefaultModel()).isNull();
        assertThat(setting.getDefaultPov()).isEqualTo("changed pov");
        assertThat(setting.getBackupStrategy()).isEqualTo(BackupStrategy.DISABLED);
        assertThat(setting.getDefaultTense()).isEqualTo("tense");
    }

    @Test
    void getOrCreateReturnsSameSettingPerUser() throws Exception {
        UserSetting first = settingService.getOrCreate(UserSetting.class);
        first.setDefaultStyle("mine");
        settingService.save(first);

        UserSetting again = settingService.getOrCreate(UserSetting.class);
        assertThat(again.getId()).isEqualTo(first.getId());
        assertThat(again.getDefaultStyle()).isEqualTo("mine");
        assertThat(again.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(again.getKey()).isEqualTo("UserSetting");

        User other = login();
        UserSetting others = settingService.getOrCreate(UserSetting.class);
        assertThat(others.getId()).isNotEqualTo(first.getId());
        assertThat(others.getOwner().getId()).isEqualTo(other.getId());
        assertThat(others.getDefaultStyle()).isNull();

        assertThat(settingService.getOrCreate(UserSetting.class, owner).getId()).isEqualTo(first.getId());
    }

    @Test
    void appSettingsAreGlobal() throws Exception {
        AppSettings settings = settingService.getOrCreateApp(AppSettings.class);

        assertThat(settings.getOwner()).isNull();
        assertThat(settings.getDbVersion()).isNotNull();

        login();
        assertThat(settingService.getOrCreateApp(AppSettings.class).getId()).isEqualTo(settings.getId());
    }

    @Test
    void appSettingsFieldsArePersisted() throws Exception {
        AppSettings settings = settingService.getOrCreateApp(AppSettings.class);
        Integer appVersion = settings.getAppVersion();
        try {
            settings.setAppVersion(appVersion + 41);
            settingService.save(settings);

            AppSettings loaded = settingService.getOrCreateApp(AppSettings.class);
            assertThat(loaded.getAppVersion()).isEqualTo(appVersion + 41);
            assertThat(loaded).isInstanceOf(AppSettings.class);
        } finally {
            AppSettings restore = settingService.getOrCreateApp(AppSettings.class);
            restore.setAppVersion(appVersion);
            settingService.save(restore);
        }
    }

    @Test
    void settingTypeIsStoredInDiscriminator() throws Exception {
        UserSetting saved = settingService.getOrCreate(UserSetting.class);

        Setting loaded = settingService.find(saved.getId());

        assertThat(loaded).isExactlyInstanceOf(UserSetting.class);
    }
}
