package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.service.ResourceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The checkpoints of a book as resources of the current user linked to the book ({@link ResourceService#link}). Nothing in the book
 * names them, so the book's backups and exports leave them out; the user sees them among their files (Resources), grouped by the book.
 * Must be used with the request context of the user.
 */
public class ResourceCheckpointFiles implements CheckpointFiles {

    /** More files than a book of a tracker ever keeps; the list is read in one page. */
    private static final int PAGE = 500;

    private final ResourceService resources;
    private final long bookId;

    /** @param book the book, stored (it has an id) */
    public ResourceCheckpointFiles(ResourceService resources, Manuscript book) {
        if (book.getId() == null) {
            throw new IllegalArgumentException("The book is not stored");
        }
        this.resources = resources;
        this.bookId = book.getId();
    }

    @Override
    public String write(String name, byte[] content) throws Exception {
        Resource resource = resources.link(resources.upload(name, content, "application/gzip"), Manuscript.class, bookId);
        return resource.getUuid();
    }

    @Override
    public List<StoredFile> list() throws Exception {
        ResourceRepository.ResourceGroup group = new ResourceRepository.ResourceGroup(Manuscript.class.getName(), bookId, 0, 0, null);
        List<StoredFile> files = new ArrayList<>();
        for (Resource resource : resources.findPageOfGroupForUser(group, 0, PAGE, "creation", false)) {
            files.add(new StoredFile(resource.getUuid(), resource.getOriginalName(),
                    resource.getCreation() == null ? 0 : resource.getCreation().getTime()));
        }
        return files;
    }

    @Override
    public byte[] read(String id) throws Exception {
        Resource resource = resources.findForUser(id);
        return resource == null ? null : resources.getResourceData(resource);
    }

    @Override
    public void delete(Collection<String> ids) throws Exception {
        if (!ids.isEmpty()) {
            resources.softDelete(ids);
        }
    }
}
