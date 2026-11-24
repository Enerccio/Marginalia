package com.github.enerccio.marginalia.domain.security.repository;

import com.github.enerccio.marginalia.domain.repository.ExtendableRepository;
import com.github.enerccio.marginalia.domain.security.model.ApiKey;

public interface ApiKeyRepository extends ExtendableRepository<ApiKey> {

    /**
     * @return the key with that prefix, of any user; null when there is none (or it is deleted)
     */
    ApiKey findByPrefix(String prefix) throws Exception;

}
