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

    /**
     * Like {@link #find}, but a setting that does not exist yet is a blank one that is not stored: nothing was chosen, which is what
     * a function that only reads needs. Use it instead of {@link #getOrCreate(Class, User)} inside a read-only transaction, where
     * creating the row is a write that fails with SQLITE_BUSY when another connection has written in between.
     */
    default <T extends Setting> T findOrBlank(Class<T> clazz, User user) throws Exception {
        T found = find(clazz, user);
        return found != null ? found : clazz.getConstructor().newInstance();
    }

}
