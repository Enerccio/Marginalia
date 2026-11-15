package com.github.enerccio.marginalia.extensions.audiobook.model;

/**
 * The precision Higgs Audio v3 computes in (the {@code dtype} of the engine of the TTS Audio Suite). bf16 gave gibberish, wrong voices and sound
 * effects instead of speech on an AMD R9700 with ROCm, fp32 the right words (see TODO.md), so fp32 is the default.
 */
public enum HiggsPrecision {
    FP32("fp32"), BF16("bf16"), AUTO("auto");

    private final String wire;

    HiggsPrecision(String wire) {
        this.wire = wire;
    }

    /** The value of {@code dtype} in the workflow. */
    public String wire() {
        return wire;
    }
}
