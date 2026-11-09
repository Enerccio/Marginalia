package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link StructuredOutputs#parse}: strict JSON first, then the lenient reading for answers that models write badly.
 */
class StructuredOutputsParseTest {

    record Change(String name, List<String> retract, List<String> add) {
    }

    record Answer(List<Change> characters) {
    }

    @Test
    void validJsonIsRead() throws Exception {
        Answer answer = StructuredOutputs.parse("{\"characters\":[{\"name\":\"A\",\"retract\":[],\"add\":[\"x\"]}]}", Answer.class);
        assertThat(answer.characters()).containsExactly(new Change("A", List.of(), List.of("x")));
    }

    @Test
    void markdownBlockIsRemoved() throws Exception {
        Answer answer = StructuredOutputs.parse("```json\n{\"characters\":[{\"name\":\"A\"}]}\n```", Answer.class);
        assertThat(answer.characters().getFirst().name()).isEqualTo("A");
    }

    @Test
    void repeatedKeyTakesTheLastValue() throws Exception {
        Answer answer = StructuredOutputs.parse(
                "{\"characters\":[{\"name\":\"A\",\"retract\":[],\"add\":[],\"name\":\"B\"}]}", Answer.class);
        assertThat(answer.characters().getFirst().name()).isEqualTo("B");
    }

    @Test
    void trailingCommaIsForgiven() throws Exception {
        Answer answer = StructuredOutputs.parse("{\"characters\":[{\"name\":\"A\",\"add\":[\"x\"],}]}", Answer.class);
        assertThat(answer.characters().getFirst().add()).containsExactly("x");
    }

    @Test
    void sentenceBeforeTheJsonIsDropped() throws Exception {
        Answer answer = StructuredOutputs.parse("Here is the result:\n{\"characters\":[{\"name\":\"A\"}]}", Answer.class);
        assertThat(answer.characters().getFirst().name()).isEqualTo("A");
    }

    @Test
    void unknownKeysAreIgnored() throws Exception {
        Answer answer = StructuredOutputs.parse("{\"characters\":[{\"name\":\"A\",\"mood\":\"calm\"}],\"note\":1}", Answer.class);
        assertThat(answer.characters()).hasSize(1);
    }

    @Test
    void leniencyDoesNotMakeValidationLenient() {
        var schema = StructuredOutputs.schemaOf(Answer.class);
        assertThat(StructuredOutputs.validate(schema, "{\"characters\":[{\"name\":\"A\"").valid()).isFalse();
    }

    @Test
    void textWithoutJsonFails() {
        assertThatThrownBy(() -> StructuredOutputs.parse("I cannot do that.", Answer.class))
                .isInstanceOf(StructuredOutputException.class);
        assertThatThrownBy(() -> StructuredOutputs.parse("", Answer.class))
                .isInstanceOf(StructuredOutputException.class);
    }

    @Test
    void wrongTypeFailsEvenLeniently() {
        assertThatThrownBy(() -> StructuredOutputs.parse("{\"characters\":[{\"name\":{\"a\":1}}]}", Answer.class))
                .isInstanceOf(StructuredOutputException.class);
    }
}
