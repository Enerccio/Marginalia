package com.github.enerccio.marginalia.domain.security.repository.impl;

import com.github.enerccio.marginalia.domain.repository.impl.JpaExtendableRepository;
import com.github.enerccio.marginalia.domain.security.model.ApiKey;
import com.github.enerccio.marginalia.domain.security.repository.ApiKeyRepository;

import java.util.List;

public class JpaApiKeyRepository extends JpaExtendableRepository<ApiKey> implements ApiKeyRepository {

    @Override
    protected Class<ApiKey> getEntityClass() {
        return ApiKey.class;
    }

    @Override
    public ApiKey findByPrefix(String prefix) throws Exception {
        List<ApiKey> results = getEntityManager()
                .createQuery("SELECT k FROM ApiKey k WHERE k.keyPrefix = :prefix AND k.deleted = false", ApiKey.class)
                .setParameter("prefix", prefix)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : hydrate(results.getFirst());
    }
}
