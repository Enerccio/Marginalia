package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The answer is read leniently and the schema says what the reader reads.
 */
class TranslationAnswerTest {

    private static final String ANSWER = """
            {"translations":[{"quote":"I see you.","speaker":"Mara","language":"Vrakht","text":"Da ja-se vek.","choice":"statement","understood":false,"register":"formal"},
            {"quote":"Hello.","speaker":"Brog","language":"none","text":"","choice":"common"}],
            "newWords":[{"word":"eiz-blod","gloss":"veteran","category":"War","rationale":"iron + lineage","register":"","kind":"compound","variantOf":""}]}""";

    @Test
    void anAnswerIsRead() {
        TranslationAnswer answer = TranslationAnswer.parse(ANSWER);

        assertEquals(2, answer.translations().size());
        Speech first = answer.translations().getFirst();
        assertEquals("Da ja-se vek.", first.text());
        assertFalse(first.isUnderstood());
        assertFalse(first.isNone());
        assertEquals("formal", first.register());
        Speech second = answer.translations().get(1);
        assertTrue(second.isNone());
        assertTrue(second.isUnderstood(), "an answer without the flag means it is understood");
        assertEquals("eiz-blod", answer.newWords().getFirst().word());
        assertEquals("compound", answer.newWords().getFirst().kind());
    }

    @Test
    void aCodeFenceAndTextAroundTheObjectAreIgnored() {
        assertNotNull(TranslationAnswer.parse("```json\n" + ANSWER + "\n```"));
        assertEquals(2, TranslationAnswer.parse("Here is the translation:\n" + ANSWER + "\nHope it helps!").translations().size());
    }

    @Test
    void missingListsAreEmptyAndAnswerWithNothingUsableIsNull() {
        TranslationAnswer onlyWords = TranslationAnswer.parse("{\"newWords\":[]}");
        assertTrue(onlyWords.translations().isEmpty());
        assertTrue(onlyWords.newWords().isEmpty());

        assertNull(TranslationAnswer.parse(null));
        assertNull(TranslationAnswer.parse(""));
        assertNull(TranslationAnswer.parse("no json here"));
        assertNull(TranslationAnswer.parse("{\"translations\": [ {"));
        assertNull(TranslationAnswer.parse("{}"));
        assertNull(TranslationAnswer.parse("{\"translations\": \"not a list\"}"));
    }

    @Test
    void aNoneLanguageIsWrittenInAnyWay() {
        assertTrue(new Speech("q", "s", "None", "", "", null, "").isNone());
        assertTrue(new Speech("q", "s", " none ", "", "", null, "").isNone());
        assertTrue(new Speech("q", "s", null, "", "", null, "").isNone());
        assertTrue(new Speech("q", "s", "", "", "", null, "").isNone());
    }

    @Test
    void theSchemaIsStrictAndHasExactlyTheFieldsOfTheRecords() {
        JsonObject schema = JsonParser.parseString(TranslationSchema.JSON).getAsJsonObject();

        assertFalse(schema.get("additionalProperties").getAsBoolean());
        assertEquals(Set.of("translations", "newWords"), required(schema));
        assertEquals(components(Speech.class), required(item(schema, "translations")));
        assertEquals(components(TranslationAnswer.NewWord.class), required(item(schema, "newWords")));
        assertEquals(components(Speech.class), item(schema, "translations").getAsJsonObject("properties").keySet());
        assertEquals(components(TranslationAnswer.NewWord.class), item(schema, "newWords").getAsJsonObject("properties").keySet());
        assertFalse(item(schema, "translations").get("additionalProperties").getAsBoolean());
    }

    private static JsonObject item(JsonObject schema, String list) {
        return schema.getAsJsonObject("properties").getAsJsonObject(list).getAsJsonObject("items");
    }

    private static Set<String> required(JsonObject object) {
        Set<String> names = new HashSet<>();
        JsonArray required = object.getAsJsonArray("required");
        required.forEach(name -> names.add(name.getAsString()));
        return names;
    }

    private static Set<String> components(Class<? extends Record> type) {
        return new HashSet<>(Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList());
    }
}
