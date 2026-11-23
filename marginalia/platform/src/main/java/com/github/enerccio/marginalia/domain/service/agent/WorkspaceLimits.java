package com.github.enerccio.marginalia.domain.service.agent;

/**
 * What an agent may keep in its workspace.
 *
 * @param maxFileBytes    largest file
 * @param maxTotalBytes   sum of all the files
 * @param maxFiles        number of files (folders are free)
 * @param maxImageBytes   largest image file (an image counts against the total and the number of files like any file)
 * @param maxArchiveBytes largest zip file (counts like any file; what it unpacks to is checked against the other limits)
 */
public record WorkspaceLimits(long maxFileBytes, long maxTotalBytes, int maxFiles, long maxImageBytes, long maxArchiveBytes) {

    /** What an image file may be at most when the limits don't say, in bytes. */
    public static final long DEFAULT_IMAGE_BYTES = 5L * 1024 * 1024;

    /** What a zip file may be at most when the limits don't say, in bytes. */
    public static final long DEFAULT_ARCHIVE_BYTES = 10L * 1024 * 1024;

    public static final WorkspaceLimits DEFAULT = new WorkspaceLimits(1024 * 1024, 20L * 1024 * 1024, 500);

    public WorkspaceLimits {
        if (maxFileBytes < 1 || maxTotalBytes < 1 || maxFiles < 1 || maxImageBytes < 1 || maxArchiveBytes < 1) {
            throw new IllegalArgumentException("The limits must be positive");
        }
    }

    /** Zip files may be {@link #DEFAULT_ARCHIVE_BYTES}, or as large as any file when that is more. */
    public WorkspaceLimits(long maxFileBytes, long maxTotalBytes, int maxFiles, long maxImageBytes) {
        this(maxFileBytes, maxTotalBytes, maxFiles, maxImageBytes, Math.max(maxFileBytes, DEFAULT_ARCHIVE_BYTES));
    }

    /** Images may be {@link #DEFAULT_IMAGE_BYTES}, or as large as any file when that is more. */
    public WorkspaceLimits(long maxFileBytes, long maxTotalBytes, int maxFiles) {
        this(maxFileBytes, maxTotalBytes, maxFiles, Math.max(maxFileBytes, DEFAULT_IMAGE_BYTES));
    }

    /** The largest file of any kind the workspace takes. */
    public long maxAnyFileBytes() {
        return Math.max(maxFileBytes, Math.max(maxImageBytes, maxArchiveBytes));
    }

}
