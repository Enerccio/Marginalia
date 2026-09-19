package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

public interface ResourceService extends OwnedService<Resource, ResourceRepository> {

    Resource upload(String filename, byte[] content, String contentType) throws Exception;

    Resource findByHash(String hash, User owner) throws Exception;

    byte[] getResourceData(Resource resource) throws Exception;

}
