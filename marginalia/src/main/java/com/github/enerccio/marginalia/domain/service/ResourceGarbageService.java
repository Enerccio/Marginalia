package com.github.enerccio.marginalia.domain.service;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * Garbage collection of the files of resources, for administrators only.
 * <p>
 * Files of deleted resources and of replaced content stay in the data folders of the users ({@code <user>/images} and
 * {@code <user>/resources}, see {@link ResourceService}). A file is in use as long as any {@code Resource} row points to it,
 * a soft deleted one too (it can be restored from the trash); the files nothing points to can be downloaded and
 * deleted. Files are matched by their name (a random UUID plus extension) in all user folders, also the ones of deleted users
 * ({@code <login>-deleted}), whose resources are still in the database until the cleanup purges them.
 */
public interface ResourceGarbageService {

    /**
     * Folders with the files of resources inside.
     */
    String IMAGES = "images";
    String RESOURCES = "resources";

    /**
     * A file in a resource folder of a user.
     *
     * @param userFolder   name of the folder of the user in the data folder
     * @param kind         {@link #IMAGES} or {@link #RESOURCES}
     * @param name         name of the file
     * @param size         bytes
     * @param lastModified milliseconds since the epoch
     */
    record StoredFile(String userFolder, String kind, String name, long size, long lastModified) {

        /**
         * @return path of the file in the data folder, also the name of its entry in the ZIP
         */
        public String path() {
            return userFolder + "/" + kind + "/" + name;
        }
    }

    /**
     * @param deleted  files removed from the disk
     * @param skipped  files that were not removed: they are in use now, gone already, or could not be deleted
     * @param bytes    size of the removed files
     */
    record DeleteResult(int deleted, int skipped, long bytes) {
    }

    /**
     * @return names of the folders in the data folder that can hold files of resources (all of them: the folders of the
     * users and the retired folders of deleted users)
     */
    List<String> userFolders() throws Exception;

    /**
     * @return files in both resource folders of the user folder, subfolders and links are ignored
     */
    List<StoredFile> listFiles(String userFolder) throws Exception;

    /**
     * @return those of the files that no resource points to
     */
    List<StoredFile> findUnused(Collection<StoredFile> files) throws Exception;

    /**
     * @return the file on the disk
     * @throws IllegalArgumentException if it is not a regular file in a resource folder of the data folder
     */
    File getFile(StoredFile file) throws Exception;

    /**
     * Writes the files into a ZIP archive, the entry names are {@link StoredFile#path()}. Files that are gone are left out.
     */
    void writeZip(Collection<StoredFile> files, OutputStream out) throws Exception;

    /**
     * Deletes the files, the ones in use by a resource now (it may have been uploaded since the scan) are kept.
     */
    DeleteResult delete(Collection<StoredFile> files) throws Exception;

}
