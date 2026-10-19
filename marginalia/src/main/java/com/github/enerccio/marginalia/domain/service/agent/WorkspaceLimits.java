package com.github.enerccio.marginalia.domain.service.agent;

/**
 * What an agent may keep in its workspace.
 *
 * @param maxFileBytes  largest file
 * @param maxTotalBytes sum of all the files
 * @param maxFiles      number of files (folders are free)
 * @param maxImageBytes largest image file (an image counts against the total and the number of files like any file)
 */
public record WorkspaceLimits(long maxFileBytes, long maxTotalBytes, int maxFiles, long maxImageBytes) {

    /** What an image file may be at most when the limits don't say, in bytes. */
    public static final long DEFAULT_IMAGE_BYTES = 5L * 1024 * 1024;

    public static final WorkspaceLimits DEFAULT = new WorkspaceLimits(1024 * 1024, 20L * 1024 * 1024, 500);

    public WorkspaceLimits {
        if (maxFileBytes < 1 || maxTotalBytes < 1 || maxFiles < 1 || maxImageBytes < 1) {
            throw new IllegalArgumentException("The limits must be positive");
        }
    }

    /** Images may be {@link #DEFAULT_IMAGE_BYTES}, or as large as any file when that is more. */
    public WorkspaceLimits(long maxFileBytes, long maxTotalBytes, int maxFiles) {
        this(maxFileBytes, maxTotalBytes, maxFiles, Math.max(maxFileBytes, DEFAULT_IMAGE_BYTES));
    }

}
