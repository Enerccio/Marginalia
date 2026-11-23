package com.github.enerccio.marginalia.domain.service.agent;

import java.time.Instant;

/**
 * A file or a folder of the virtual file system.
 *
 * @param size     bytes of a file, zero for a folder
 * @param modified null when not known
 */
public record VirtualEntry(VirtualPath path, boolean directory, long size, Instant modified) {
}
