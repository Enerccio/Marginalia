package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.repository.AIRepository;
import com.github.enerccio.marginalia.domain.repository.SettingRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

public interface SettingService extends ExtendableService<Setting, SettingRepository> {

    <T extends Setting> T getOrCreate(Class<T> clazz) throws Exception;
    <T extends Setting> T getOrCreateApp(Class<T> clazz) throws Exception;
    <T extends Setting> T getOrCreate(Class<T> clazz, User user) throws Exception;

}
