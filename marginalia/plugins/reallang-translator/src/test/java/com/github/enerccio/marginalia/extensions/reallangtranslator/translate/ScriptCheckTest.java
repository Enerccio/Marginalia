package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Script;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.ScriptCheck.Problem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The scripts a translation is asked to be in: what the check lets through and what it flags.
 */
class ScriptCheckTest {

    @Test
    void kanaAndKanjiAcceptsJapaneseAndFlagsLatinOnlyText() {
        assertNull(ScriptCheck.problem(Script.KANA_KANJI, "敵が見える。"));
        assertNull(ScriptCheck.problem(Script.KANA_KANJI, "ありがとう"));
        assertNull(ScriptCheck.problem(Script.KANA_KANJI, "マラさん、行こう"));
        assertEquals(Problem.NOT_JAPANESE, ScriptCheck.problem(Script.KANA_KANJI, "Teki ga mieru."));
    }

    @Test
    void aLatinNameInsideAJapaneseSentenceIsFine() {
        assertNull(ScriptCheck.problem(Script.KANA_KANJI, "Mara、行こう"));
    }

    @Test
    void kanaOnlyRejectsKanji() {
        assertNull(ScriptCheck.problem(Script.KANA, "てきがみえる。"));
        assertEquals(Problem.KANJI_IN_KANA, ScriptCheck.problem(Script.KANA, "敵がみえる。"));
        assertEquals(Problem.NOT_JAPANESE, ScriptCheck.problem(Script.KANA, "teki"));
    }

    @Test
    void romajiRejectsKanaAndKanji() {
        assertNull(ScriptCheck.problem(Script.ROMAJI, "Teki ga mieru."));
        assertNull(ScriptCheck.problem(Script.ROMAJI, "Ōkii desu ne."));
        assertEquals(Problem.JAPANESE_IN_ROMAJI, ScriptCheck.problem(Script.ROMAJI, "Teki ga 見える."));
        assertEquals(Problem.JAPANESE_IN_ROMAJI, ScriptCheck.problem(Script.ROMAJI, "ありがとう"));
    }

    @Test
    void anotherScriptIsNotChecked() {
        assertNull(ScriptCheck.problem(Script.NATIVE, "anything"));
        assertNull(ScriptCheck.problem(Script.NATIVE, "何でも"));
    }

    @Test
    void anEmptyTextHasNoProblemHere() {
        assertNull(ScriptCheck.problem(Script.KANA_KANJI, ""));
        assertNull(ScriptCheck.problem(Script.KANA_KANJI, null));
    }
}
