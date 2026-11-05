package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ModelJsonTest {

    @Test
    void aBareObjectIsReturnedAsItIs() {
        assertEquals("{\"a\":1}", ModelJson.object("{\"a\":1}"));
    }

    @Test
    void aCodeFenceAndTextAroundTheObjectAreIgnored() {
        assertEquals("{\"a\":{\"b\":2}}", ModelJson.object("```json\n{\"a\":{\"b\":2}}\n```"));
        assertEquals("{\"a\":1}", ModelJson.object("Here you are: {\"a\":1} done"));
    }

    @Test
    void noObjectIsNull() {
        assertNull(ModelJson.object(null));
        assertNull(ModelJson.object("no braces"));
        assertNull(ModelJson.object("} {"));
    }
}
