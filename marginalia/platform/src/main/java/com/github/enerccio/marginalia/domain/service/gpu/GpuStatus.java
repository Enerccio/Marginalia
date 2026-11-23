package com.github.enerccio.marginalia.domain.service.gpu;

/**
 * What a GPU does now.
 *
 * @param gpuUuid    the GPU
 * @param loadedLock the name of the lock the GPU is loaded with, null when it is not known (nothing ran since the start, or it was unloaded)
 * @param active     how many jobs use it now
 * @param waiting    how many jobs wait for it
 * @param switching  true while steps run (the GPU goes from one lock to another)
 */
public record GpuStatus(String gpuUuid, String loadedLock, int active, int waiting, boolean switching) {
}
