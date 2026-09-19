package com.github.enerccio.marginalia.domain.security.service;

import com.github.enerccio.marginalia.domain.security.PersistedLoginInfo;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.repository.UserRepository;
import com.github.enerccio.marginalia.domain.service.BaseService;

import java.util.List;

public interface UserService extends BaseService<User, UserRepository> {

    boolean existsUsers() throws Exception;
    User findByName(String name) throws Exception;
    boolean authenticate(String username, String password) throws Exception;
    User changePassword(User user, String password) throws Exception;
    PersistedLoginInfo authenticateFromCookie(User user, String cookieIdentifier, String cookieSecret) throws Exception;

    void deletePersistedLoginInfo(User user, PersistedLoginInfo persistedLoginInfo) throws Exception;
    List<PersistedLoginInfo> getPersistedLoginInfo(User user) throws Exception;
    PersistedLoginInfo generateNewPersistentInfo(User user) throws Exception;
    void addPersistedLoginInfo(User user, PersistedLoginInfo persistedLoginInfo) throws Exception;
}
