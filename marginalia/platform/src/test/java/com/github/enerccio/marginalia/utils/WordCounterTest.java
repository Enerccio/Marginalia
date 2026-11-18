package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordCounterTest {

    @Test
    void spacedScriptsCountRunsOfLetters() {
        assertThat(WordCounter.count("Hello, world! It's 2 o'clock")).isEqualTo(7);
        assertThat(WordCounter.count("Příliš žluťoučký kůň")).isEqualTo(3);
        assertThat(WordCounter.count("Привет мир")).isEqualTo(2);
        assertThat(WordCounter.count("안녕하세요 세계")).isEqualTo(2);
    }

    @Test
    void blankAndPunctuationOnlyTextIsZero() {
        assertThat(WordCounter.count(null)).isZero();
        assertThat(WordCounter.count("")).isZero();
        assertThat(WordCounter.count("  \n ")).isZero();
        assertThat(WordCounter.count("。、！？…「」")).isZero();
    }

    @Test
    void everyChineseCharacterIsAWord() {
        assertThat(WordCounter.count("我喜欢你。")).isEqualTo(4);
        assertThat(WordCounter.count("今天天气很好，我们去公园吧！")).isEqualTo(12);
    }

    @Test
    void everyJapaneseCharacterIsAWord() {
        assertThat(WordCounter.count("吾輩は猫である。")).isEqualTo(7);
        assertThat(WordCounter.count("コンピューター")).isEqualTo(7);
        assertThat(WordCounter.count("々")).isEqualTo(1);
    }

    @Test
    void mixedTextIsCountedPieceByPiece() {
        assertThat(WordCounter.count("AIは便利です")).isEqualTo(6);
        assertThat(WordCounter.count("He said 「こんにちは」 to Anna")).isEqualTo(4 + 5);
        assertThat(WordCounter.count("2024年に東京へ")).isEqualTo(1 + 5);
        assertThat(WordCounter.count("ＡＢＣ　１２３")).isEqualTo(2);
    }
}
