package com.github.enerccio.marginalia.domain.service;

import com.google.gson.JsonObject;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ResourceService extends ExtendableService<Resource, ResourceRepository> {

    Resource upload(String filename, byte[] content, String contentType) throws Exception;

    /**
     * The file an upload handler writes into before the upload is given to {@link #saveFile}: a new {@code <uuid>.part} in the
     * resources folder of the current user (not created). Nothing else is done with it: a caller that does not store it deletes it.
     */
    File newUploadFile() throws Exception;

    /**
     * Stores a file that is already in the folder of the current user (such as the {@link #newUploadFile() upload file}), without
     * reading it into memory: what {@link #upload} does for a byte array. The SHA-256 and the size are computed from the file; when
     * another resource of the user has the same content (same hash, size and bytes, in the same folder), the file is deleted and the
     * new resource uses the file of the other one, else the file is moved to {@code <uuid>.<extension of originalName>} in the images
     * folder for an {@code image/*} type and the resources folder for the others. Images are taken as they are (not checked or
     * converted, unlike {@link #uploadImage}).
     *
     * @param file         a file directly in the resources or images folder of the current user
     * @param mimeType     the type; blank is {@code application/octet-stream}
     * @return the saved resource, not linked to anything
     * @throws IllegalArgumentException if the file is not a file of the user's resources or images folder
     */
    Resource saveFile(File file, String originalName, String mimeType) throws Exception;

    /**
     * Same as {@link #saveFile(File, String, String)}, the resource is linked to the object that uses it.
     *
     * @see #link
     */
    Resource saveFile(File file, String originalName, String mimeType, Class<?> clazz, Long objectId) throws Exception;

    /**
     * Soft deletes the resources of the current user with the original name that no object took (not {@link #link linked}) and
     * that were made before the date: uploads a caller stored ahead and that were never used. The files stay (cleanup purges them).
     *
     * @return number of the deleted resources
     */
    int deleteUnlinked(String originalName, Date before) throws Exception;

    Resource findByHash(String hash, User owner) throws Exception;

    byte[] getResourceData(Resource resource) throws Exception;

    /**
     * @return the file with the content of the resource of the current user, for streaming it (it may not exist)
     */
    File getResourceFile(Resource resource) throws Exception;

    /**
     * Makes another resource of the current user for the same file, for an object that must not share the resource
     * with the original (a cloned book). Files are never changed, so the copy does not copy the file. The data of the
     * extensions ({@link Resource#getAttributes()}) is copied.
     *
     * @return the copy, not linked to anything
     * @throws IllegalArgumentException if the resource is not the user's
     */
    Resource copy(Resource resource) throws Exception;

    /**
     * Gives the user's resource the data of the extensions from an export (a backup, a lorebook file) and saves it; the
     * resource as it is for null or empty attributes.
     *
     * @throws IllegalArgumentException if the resource is not the user's
     */
    Resource withAttributes(Resource resource, JsonObject attributes) throws Exception;

    /**
     * A new image of the current user with the content of an image of another user: the images of a lorebook that user shared, when
     * the current user takes a copy of it. The caller has checked that the user may see the image (it is shown by an entry of a
     * lorebook shared with them by its owner).
     *
     * @return the copy, null when the owner has no such image (or it is deleted)
     */
    Resource copyImageOf(String uuid, User owner) throws Exception;

    /**
     * Largest image {@link #uploadImage} accepts.
     */
    int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    /**
     * Stores an image for the current user. The format (PNG, JPEG, GIF or WebP) is detected from the content, the
     * type sent by the client is not trusted.
     *
     * @throws IllegalArgumentException if the content is not a supported image or is larger than {@link #MAX_IMAGE_BYTES}
     */
    Resource uploadImage(String filename, byte[] content) throws Exception;

    /**
     * Same as {@link #uploadImage(String, byte[])}, the resource is linked to the object that uses the image.
     *
     * @see #link
     */
    Resource uploadImage(String filename, byte[] content, Class<?> clazz, Long objectId) throws Exception;

    /**
     * Notes which object uses the resource. Bookkeeping only: it is not checked, nothing is cleaned up with it.
     */
    Resource link(Resource resource, Class<?> clazz, Long objectId) throws Exception;

    /**
     * Forgets the object linked by {@link #link}.
     */
    Resource unlink(Resource resource) throws Exception;

    /**
     * @param description the linked object as text (class and id)
     * @param present     false if the object is deleted or doesn't exist
     */
    record ResourceLink(String description, boolean present) {
    }

    /**
     * @return the object the resource is linked to, null if it is not linked
     */
    ResourceLink describeLink(Resource resource) throws Exception;

    /**
     * Resources of the current user for a lazy table.
     *
     * @param sortProperty one of creation, originalName, mimeType, size
     */
    List<Resource> findPageForUser(int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long countForUser() throws Exception;

    /**
     * The resources of the current user grouped by the object that uses them, for the table of resources.
     *
     * @param sortProperty one of creation, size, originalName (by the object)
     */
    List<ResourceRepository.ResourceGroup> findGroupPageForUser(int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long countGroupsForUser() throws Exception;

    /**
     * Same as {@link #findPageForUser} for the resources of one group (null class and id: linked to nothing).
     */
    List<Resource> findPageOfGroupForUser(ResourceRepository.ResourceGroup group, int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long countOfGroupForUser(ResourceRepository.ResourceGroup group) throws Exception;

    /**
     * @return the object the group of resources belongs to, null for the group of the resources linked to nothing
     */
    ResourceLink describeGroup(ResourceRepository.ResourceGroup group) throws Exception;

    /**
     * Replaces the content of the resource of the current user: the new content is written as a new file and the
     * resource points to it, so everything that references the resource keeps working. The old file stays in the
     * folder. An image can be replaced only by an image (see {@link #uploadImage}).
     *
     * @throws IllegalArgumentException if the resource is not the user's, or the content is not acceptable
     */
    Resource replace(Resource resource, String filename, byte[] content, String contentType) throws Exception;

    /**
     * Soft deletes the resources of the current user with given uuids (cleanup purges them later), the files stay.
     * Other uuids are ignored.
     *
     * @return number of the deleted resources
     */
    int softDelete(Collection<String> uuids) throws Exception;

    /**
     * @return image of the current user, null if there is none with such uuid (or it is not an image, or not theirs)
     */
    Resource findImage(String uuid) throws Exception;

    /**
     * An image of the current user, or an image linked to a part of a book they may view: a published one, when its
     * {@link com.github.enerccio.marginalia.domain.model.impl.Manuscript#isShowImagesToReaders() setting} lets readers see the images.
     * The content of the returned resource can be read with {@link #getResourceData} either way.
     *
     * @return the image, null when it is none of these
     */
    Resource findImageForReader(String uuid, String manuscriptUuid) throws Exception;

    /**
     * The content of a resource that is linked to a part of a book the current user may view: their own book, or a published
     * one, whoever owns it (the resource is read from its owner's folder). This is how the readers of a published book get the
     * files its parts carry (the audio of the Audiobook extension). Images are not read this way, see {@link #findImageForReader}.
     *
     * @return the bytes, null when the book can't be viewed by the user, or the resource is deleted, belongs to somebody else
     * than the owner of the book, is an image, or is not linked to a part of the book
     */
    byte[] readForReader(String resourceUuid, String manuscriptUuid) throws Exception;

}
