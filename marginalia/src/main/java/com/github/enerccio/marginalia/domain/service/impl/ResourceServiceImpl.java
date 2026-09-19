package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ResourceServiceImpl extends OwnedServiceImpl<Resource, ResourceRepository> implements ResourceService {

    @Override
    @CommonTx
    public Resource upload(String filename, byte[] content, String contentType) throws Exception {
        Resource resource = new Resource();
        resource.setMimeType(contentType);
        resource.setOriginalName(filename);
        resource.setSize(content.length);

        resource.setHash(DigestUtils.sha256Hex(content));

        Resource existing = findByHash(resource.getHash(), currentUser);
        if (existing != null) {
            byte[] existingContent = getResourceData(existing);
            if (existingContent.length == content.length && Arrays.equals(existingContent, content)) {
                resource.setPath(existing.getPath());
            }
        }

        if (resource.getPath() == null) {
            String extension = FilenameUtils.getExtension(filename);
            String name = UUID.randomUUID() + "." + extension;
            File storeFile;
            if (resource.getMimeType().startsWith("image/")) {
                storeFile = new File(configuration.getImagesFolder(currentUser), name);
            } else {
                storeFile = new File(configuration.getResourcesFolder(currentUser), name);
            }
            FileUtils.writeByteArrayToFile(storeFile, content);
            resource.setPath(name);
        }

        return save(resource);
    }

    @Override
    @CommonTxReadOnly
    public Resource findByHash(String hash, User owner) throws Exception {
        List<Resource> resources = getEntityManager()
                .createQuery("SELECT r FROM Resource r WHERE r.hash = :hash AND r.owner.id = :owner", Resource.class)
                .setParameter("owner", owner.getId())
                .setParameter("hash", hash)
                .getResultList();
        return resources.isEmpty() ? null : resources.getFirst();
    }

    @Override
    @NoTx
    public byte[] getResourceData(Resource resource) throws Exception {
        File file;
        if (resource.getMimeType().startsWith("image/")) {
            file = new File(configuration.getImagesFolder(currentUser), resource.getPath());
        } else {
            file = new File(configuration.getResourcesFolder(currentUser), resource.getPath());
        }
        return FileUtils.readFileToByteArray(file);
    }

}
