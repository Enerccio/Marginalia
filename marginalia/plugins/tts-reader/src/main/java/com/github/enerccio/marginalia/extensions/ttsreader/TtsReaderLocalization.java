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
        fr(READ_ALOUD, "Lire à voix haute");
        es(READ_ALOUD, "Leer en voz alta");
        de(READ_ALOUD, "Vorlesen");
        ko(READ_ALOUD, "소리 내어 읽기");
        zh(PAUSE_READING, "暂停朗读");
        fr(PAUSE_READING, "Mettre la lecture en pause");
        es(PAUSE_READING, "Pausar la lectura");
        de(PAUSE_READING, "Vorlesen pausieren");
        ko(PAUSE_READING, "읽기 일시 정지");
        zh(STOP_READING, "停止朗读");
        fr(STOP_READING, "Arrêter la lecture");
        es(STOP_READING, "Detener la lectura");
        de(STOP_READING, "Vorlesen stoppen");
        ko(STOP_READING, "읽기 중지");
        zh(READING_SETTINGS, "朗读设置");
        fr(READING_SETTINGS, "Paramètres de lecture");
        es(READING_SETTINGS, "Ajustes de lectura");
        de(READING_SETTINGS, "Einstellungen zum Vorlesen");
        ko(READING_SETTINGS, "읽기 설정");
        zh(VOICE, "语音");
        fr(VOICE, "Voix");
        es(VOICE, "Voz");
        de(VOICE, "Stimme");
        ko(VOICE, "목소리");
        zh(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "书籍语言的默认语音");
        fr(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "Voix par défaut de la langue du livre");
        es(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "Voz predeterminada del idioma del libro");
        de(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "Standardstimme der Buchsprache");
        ko(DEFAULT_VOICE_OF_THE_BOOK_LANGUAGE, "책 언어의 기본 음성");
        zh(SPEED, "语速");
        fr(SPEED, "Vitesse");
        es(SPEED, "Velocidad");
        de(SPEED, "Geschwindigkeit");
        ko(SPEED, "속도");
        zh(PITCH, "音调");
        fr(PITCH, "Hauteur");
        es(PITCH, "Tono");
        de(PITCH, "Tonhöhe");
        ko(PITCH, "음높이");
    }
}
