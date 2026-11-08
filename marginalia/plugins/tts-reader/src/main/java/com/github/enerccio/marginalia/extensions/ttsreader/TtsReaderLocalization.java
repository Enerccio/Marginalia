package com.github.enerccio.marginalia.extensions.ttsreader;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Read Aloud plugin in the languages besides English; the English text is the key.
 */
public class TtsReaderLocalization extends PluginLocalizationBundle {

    public static final TtsReaderLocalization T = new TtsReaderLocalization();

    public static final String READ_ALOUD = "Read aloud";
    public static final String PAUSE_READING = "Pause reading";
    public static final String STOP_READING = "Stop reading";
    public static final String READING_SETTINGS = "Reading settings";
    public static final String VOICE = "Voice";
    public static final String DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE = "Default voice of the book language";
    public static final String SPEED = "Speed";
    public static final String PITCH = "Pitch";

    @Override
    protected void loadKeys() {
        ja(READ_ALOUD, "読み上げ");
        ja(PAUSE_READING, "読み上げを一時停止");
        ja(STOP_READING, "読み上げを停止");
        ja(READING_SETTINGS, "読み上げの設定");
        ja(VOICE, "音声");
        ja(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "本の言語の既定の音声");
        ja(SPEED, "速度");
        ja(PITCH, "ピッチ");

        zh(READ_ALOUD, "朗读");
        zh(PAUSE_READING, "暂停朗读");
        zh(STOP_READING, "停止朗读");
        zh(READING_SETTINGS, "朗读设置");
        zh(VOICE, "语音");
        zh(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "书籍语言的默认语音");
        zh(SPEED, "语速");
        zh(PITCH, "音调");
    }
}
