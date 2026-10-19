package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.repository.SettingRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

public interface SettingService extends ExtendableService<Setting, SettingRepository> {

    <T extends Setting> T getOrCreate(Class<T> clazz) throws Exception;
    <T extends Setting> T getOrCreateApp(Class<T> clazz) throws Exception;
    <T extends Setting> T getOrCreate(Class<T> clazz, User user) throws Exception;

    /**
     * The setting of the user (null: the application's) when it exists, null otherwise. Unlike {@link #getOrCreate(Class, User)} it
     * never writes, which is what a background thread wants: SQLite has one writer, and a write on the side makes a transaction
     * of the user that has read before it fail.
     */
    <T extends Setting> T find(Class<T> clazz, User user) throws Exception;

}
