package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.repository.BaseRepository;
import com.github.enerccio.marginalia.domain.service.BaseService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class BaseServiceImpl<T extends BaseEntity, R extends BaseRepository<T>> implements BaseService<T, R> {

    @Autowired
    protected Configuration configuration;

    private R repository;

    @Override
    @CommonTxReadOnly
    public T find(Long id) throws Exception {
        return getRepository().find(id);
    }

    @Override
    @CommonTxReadOnly
    public Long find(String uuid) throws Exception {
        return getRepository().find(uuid);
    }

    @Override
    @CommonTxReadOnly
    public List<T> findAll() throws Exception {
        return getRepository().findAll();
    }

    @Override
    @CommonTxReadOnly
    public List<Long> findAllIds() throws Exception {
        return getRepository().findAllIds();
    }

    @Override
    @CommonTx
    public T save(T t) throws Exception {
        return getRepository().save(t);
    }

    @Override
    @CommonTx
    public T delete(T entity, boolean hard) throws Exception {
        return getRepository().delete(entity, hard);
    }

    public R getRepository() {
        return repository;
    }

    public void setRepository(R repository) {
        this.repository = repository;
    }
}
