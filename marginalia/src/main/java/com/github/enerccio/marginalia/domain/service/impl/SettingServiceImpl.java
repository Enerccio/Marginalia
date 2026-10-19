package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.repository.SettingRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;

public class SettingServiceImpl extends ExtendableServiceImpl<Setting, SettingRepository> implements SettingService {

    @Override
    @CommonTx
    public <T extends Setting> T getOrCreate(Class<T> clazz) throws Exception {
        return getOrCreate(clazz, currentUser);
    }

    @Override
    @CommonTx
    public <T extends Setting> T getOrCreateApp(Class<T> clazz) throws Exception {
        return getOrCreate(clazz, null);
    }

    @Override
    @CommonTxReadOnly
    public <T extends Setting> T find(Class<T> clazz, User user) throws Exception {
        return getRepository().findSetting(clazz, user);
    }

    @SuppressWarnings("unchecked")
    @Override
    @CommonTx
    public synchronized <T extends Setting> T getOrCreate(Class<T> clazz, User user) throws Exception {
        T setting = getRepository().findSetting(clazz, user);
        if (setting == null) {
            setting = clazz.getConstructor().newInstance();
            setting.setKey(clazz.getSimpleName());
            if (user != null)
                setting.setOwner(userService.find(user.getId()));
            setting = (T) getRepository().save(setting);
        }
        return setting;
    }
}
