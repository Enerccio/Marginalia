package com.github.enerccio.marginalia.extensions.audiobook.model;

/** One step of a {@link CleanupPreset}: a model of the TTS Audio Suite on ComfyUI, or the filter of ffmpeg here. */
public class CleanupStep {

    public enum Type {
        /** {@code VocalRemovalNode}: splits the audio with a separation model (voice from music, noise, echo). */
        SEPARATE,
        /** {@code VoiceFixerNode}: restores a degraded voice. */
        VOICE_FIXER,
        /** The noise filter of ffmpeg ({@code Ffmpeg.cleanVoice}), run by the application. */
        FFMPEG
    }

    /** Which of the two outputs of a separation is kept. */
    public enum Keep {
        /** The first output ("extracted voice/noise/echo"): the voice for a vocals model. */
        EXTRACTED,
        /** The second output ("remaining"). */
        REMAINING
    }

    public static final int DEFAULT_AGGRESSIVENESS = 10;
    public static final int MAX_AGGRESSIVENESS = 20;
    public static final int DEFAULT_FIXER_MODE = 0;

    private Type type = Type.SEPARATE;
    /** The model of a separation (one of the options of the node). */
    private String model;
    private Integer aggressiveness;
    private Keep keep;
    /** The restoration mode of the voice fixer: 0 original, 1 with the high frequencies removed, 2 for a seriously degraded voice. */
    private Integer mode;

    public CleanupStep() {
    }

    public static CleanupStep separate(String model, Keep keep) {
        CleanupStep step = new CleanupStep();
        step.setType(Type.SEPARATE);
        step.setModel(model);
        step.setKeep(keep);
        return step;
    }

    public static CleanupStep voiceFixer(int mode) {
        CleanupStep step = new CleanupStep();
        step.setType(Type.VOICE_FIXER);
        step.setMode(mode);
        return step;
    }

    public static CleanupStep ffmpeg() {
        CleanupStep step = new CleanupStep();
        step.setType(Type.FFMPEG);
        return step;
    }

    public CleanupStep copy() {
        CleanupStep copy = new CleanupStep();
        copy.type = type;
        copy.model = model;
        copy.aggressiveness = aggressiveness;
        copy.keep = keep;
        copy.mode = mode;
        return copy;
    }

    public Type getType() {
        return type == null ? Type.SEPARATE : type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getModel() {
        return model == null ? "" : model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getAggressiveness() {
        return aggressiveness == null ? DEFAULT_AGGRESSIVENESS : Math.max(0, Math.min(MAX_AGGRESSIVENESS, aggressiveness));
    }

    public void setAggressiveness(Integer aggressiveness) {
        this.aggressiveness = aggressiveness;
    }

    public Keep getKeep() {
        return keep == null ? Keep.EXTRACTED : keep;
    }

    public void setKeep(Keep keep) {
        this.keep = keep;
    }

    public int getMode() {
        return mode == null ? DEFAULT_FIXER_MODE : Math.max(0, Math.min(2, mode));
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    /** A short description for the name of a version ("MelBandRoformer_fp32", "VoiceFixer", "ffmpeg"). */
    public String describe() {
        return switch (getType()) {
            case SEPARATE -> {
                String m = getModel();
                m = m.substring(m.lastIndexOf('/') + 1).replaceFirst("\\.(safetensors|ckpt|pth|onnx)$", "");
                yield getKeep() == Keep.REMAINING ? m + " (remaining)" : m;
            }
            case VOICE_FIXER -> "VoiceFixer " + getMode();
            case FFMPEG -> "ffmpeg";
        };
    }
}
