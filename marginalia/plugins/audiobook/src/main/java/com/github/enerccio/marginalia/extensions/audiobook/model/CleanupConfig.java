package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The cleanup of reference clips on a ComfyUI with the TTS Audio Suite: the server, its own GPU lock (a cleanup must not load its models next to the
 * speech or the recognition models: give it a lock whose steps restart ComfyUI) and the presets the user picks from. Kept in the settings of the
 * user; the api key is stored protected ({@code InferenceServices.protectSecret}).
 */
public class CleanupConfig {

    public static final int DEFAULT_TIMEOUT_SECONDS = 600;

    private String baseUrl = "";
    private String protectedApiKey;
    private boolean local = true;
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    /** The uuid of the GPU lock of the application a cleanup takes; null: none. */
    private String gpuLock;
    /** The presets; empty (or null): {@link #defaultPresets()}. */
    private List<CleanupPreset> presets;

    /** Whether it is set up at all (an address). */
    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    public String getBaseUrl() {
        return baseUrl == null ? "" : baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getProtectedApiKey() {
        return protectedApiKey;
    }

    public void setProtectedApiKey(String protectedApiKey) {
        this.protectedApiKey = protectedApiKey;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getGpuLock() {
        return gpuLock == null || gpuLock.isBlank() ? null : gpuLock;
    }

    public void setGpuLock(String gpuLock) {
        this.gpuLock = gpuLock;
    }

    /** The presets of the user, sorted by name; the default ones while the user has none. */
    public List<CleanupPreset> getPresets() {
        List<CleanupPreset> list = presets == null || presets.isEmpty() ? defaultPresets() : new ArrayList<>(presets);
        java.text.Collator collator = AudiobookSettings.collator();
        list.sort((a, b) -> collator.compare(a.getName(), b.getName()));
        return list;
    }

    public void setPresets(List<CleanupPreset> presets) {
        this.presets = presets == null ? null : new ArrayList<>(presets);
    }

    /** The preset of the name, null when there is none. */
    public CleanupPreset preset(String name) {
        return getPresets().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    public static final String MODEL_VOCALS = "MELBAND/MelBandRoformer_fp32.safetensors";
    public static final String MODEL_DENOISE = "MELBAND/denoise_mel_band_roformer_sdr_27.99.ckpt";

    /** The separation models of the TTS Audio Suite (shown until a test reads the list of the server). */
    public static final List<String> KNOWN_MODELS = List.of("karafan/MDX23C-8KFFT-InstVoc_HQ.ckpt", "MDX23C/model_vocals_mdx23c_sdr_10.17.ckpt",
            "MDXNET/UVR-MDX-NET-vocal_FT.onnx", "MELBAND/MelBandRoformer_fp16.safetensors", "MELBAND/MelBandRoformer_fp32.safetensors",
            "MELBAND/denoise_mel_band_roformer_aggressive_sdr_27.97.ckpt", "MELBAND/denoise_mel_band_roformer_sdr_27.99.ckpt",
            "SCNET/model_scnet_masked_xl_ihf_sdr_9.82.ckpt", "SCNET/model_scnet_xl_ihf_sdr_10.08.ckpt", "UVR/5_HP-Karaoke-UVR.pth", "UVR/6_HP-Karaoke-UVR.pth",
            "UVR/HP5-vocals+instrumentals.pth", "UVR/UVR-BVE-4B_SN-44100-1.pth", "UVR/UVR-DeEcho-DeReverb.pth", "UVR/UVR-DeNoise.pth",
            "UVR/model_bs_roformer_ep_317_sdr_12.9755.ckpt");

    /**
     * The presets a user starts with (the models come with the TTS Audio Suite, which downloads them on first use). Tried on the author's server
     * (2026-11-15): the vocals model gives the voice as its first output, the denoise model as the remaining one; the UVR models failed there (a
     * download that is gone, a {@code samplerate} module without {@code resample}), so they are not among the defaults.
     */
    public static List<CleanupPreset> defaultPresets() {
        List<CleanupPreset> list = new ArrayList<>();
        list.add(new CleanupPreset("Voice from music", List.of(CleanupStep.separate(MODEL_VOCALS, CleanupStep.Keep.EXTRACTED))));
        list.add(new CleanupPreset("Remove noise", List.of(CleanupStep.separate(MODEL_DENOISE, CleanupStep.Keep.REMAINING))));
        list.add(new CleanupPreset("Restore the voice", List.of(CleanupStep.voiceFixer(0))));
        list.add(new CleanupPreset("Film or game clip", List.of(CleanupStep.separate(MODEL_VOCALS, CleanupStep.Keep.EXTRACTED),
                CleanupStep.separate(MODEL_DENOISE, CleanupStep.Keep.REMAINING))));
        return list;
    }
}
