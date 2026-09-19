package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.repository.OwnedRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OwnedServiceImpl<T extends OwnedEntity, R extends OwnedRepository<T>> extends BaseServiceImpl<T, R> implements OwnedService<T, R> {

    @Autowired
    protected UserService userService;

    @Autowired
    protected User currentUser;

    @Override
    @CommonTxReadOnly
    public List<T> findAll(User user) throws Exception {
        return getRepository().findAll(user);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> findAllIds(User user) throws Exception {
        return getRepository().findAllIds(user);
    }

    @Override
    @CommonTxReadOnly
    public List<T> findAllForUser() throws Exception {
        return findAll(currentUser);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> findAllIdsForUser() throws Exception {
        return findAllIds(currentUser);
    }

    @Override
    public T save(T t) throws Exception {
        if (t.getOwner() == null && t.getId() == null) {
            t.setOwner(userService.find(currentUser.getId()));
        }
        return super.save(t);
    }

    protected T saveWithoutOwner(T t) throws Exception {
        return super.save(t);
    }
}
