package com.github.enerccio.marginalia.domain.service.agent;

/**
 * What an agent may keep in its workspace.
 *
 * @param maxFileBytes  largest file
 * @param maxTotalBytes sum of all the files
 * @param maxFiles      number of files (folders are free)
 */
public record WorkspaceLimits(long maxFileBytes, long maxTotalBytes, int maxFiles) {

    public static final WorkspaceLimits DEFAULT = new WorkspaceLimits(1024 * 1024, 20L * 1024 * 1024, 500);

    public WorkspaceLimits {
        if (maxFileBytes < 1 || maxTotalBytes < 1 || maxFiles < 1) {
            throw new IllegalArgumentException("The limits must be positive");
        }
    }

}
