package com.github.enerccio.marginalia.gpu;

import com.github.enerccio.marginalia.domain.service.gpu.GpuStepRunner.Step;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The variables of a job in a GPU step: {@code {{model}}}, the filters, what is left alone.
 */
class GpuStepVariablesTest {

    private static Step step(String url, String headers, String body) {
        return new Step("unload {{model}}", url, "POST", headers, body, 10, 0);
    }

    @Test
    void theNameAddressHeadersAndBodyAreFilled() {
        Step filled = step("http://box/{{ model | url }}/unload", "X-Model: {{model}}", "{\"keep\": [\"{{model | strip:openai/}}\"]}")
                .with(Map.of("model", "openai/big 32b"));
        assertThat(filled.name()).isEqualTo("unload openai/big 32b");
        assertThat(filled.url()).isEqualTo("http://box/openai%2Fbig%2032b/unload");
        assertThat(filled.headers()).isEqualTo("X-Model: openai/big 32b");
        assertThat(filled.body()).isEqualTo("{\"keep\": [\"big 32b\"]}");
        assertThat(filled.method()).isEqualTo("POST");
    }

    @Test
    void theFilters() {
        Map<String, String> vars = Map.of("model", "hosted_vllm/org/Model \"q\"");
        assertThat(step("{{model | last}}", null, null).with(vars).url()).isEqualTo("Model \"q\"");
        assertThat(step("{{model | strip:openai/}}", null, null).with(vars).url()).isEqualTo("hosted_vllm/org/Model \"q\"");
        assertThat(step("{{model | last | json}}", null, null).with(vars).url()).isEqualTo("Model \\\"q\\\"");
        assertThat(step("{{model | nosuch}}", null, null).with(vars).url()).as("an unknown filter is ignored").isEqualTo("hosted_vllm/org/Model \"q\"");
    }

    @Test
    void aMissingVariableIsEmptyAndOtherBracesStay() {
        Step filled = step("http://box/x", null, "{\"a\": {\"b\": 1}} {{ lock }} {{model}}").with(Map.of());
        assertThat(filled.body()).isEqualTo("{\"a\": {\"b\": 1}}  ");
        assertThat(step("http://box/x", null, null).with(Map.of("model", "m")).headers()).isNull();
    }
}
