package com.github.enerccio.marginalia.domain.service.gpu;

/**
 * A lock as every user sees it in a picker: no steps, no secrets.
 *
 * @param uuid    the uuid of the lock, what an AI or a backend stores
 * @param name    the name of the lock
 * @param gpuName the name of its GPU
 */
public record GpuLockChoice(String uuid, String name, String gpuName) {

    /** The text of the lock in a list. */
    public String label() {
        return gpuName == null || gpuName.isBlank() ? name : name + " (" + gpuName + ")";
    }
}
