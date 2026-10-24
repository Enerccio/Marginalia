package com.github.enerccio.marginalia.domain.components;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenizerKeyTest {

    @Test
    void sameIdAndSecretAreEqual() {
        assertThat(TokenizerKey.of("http://a#m", "secret")).isEqualTo(TokenizerKey.of("http://a#m", "secret"))
                .hasSameHashCodeAs(TokenizerKey.of("http://a#m", "secret"));
    }

    @Test
    void anotherSecretOrIdIsAnotherKey() {
        assertThat(TokenizerKey.of("http://a#m", "secret")).isNotEqualTo(TokenizerKey.of("http://a#m", "other"));
        assertThat(TokenizerKey.of("http://a#m", "secret")).isNotEqualTo(TokenizerKey.of("http://b#m", "secret"));
        assertThat(TokenizerKey.of("http://a#m", "secret")).isNotEqualTo(TokenizerKey.of("http://a#m"));
    }

    @Test
    void blankSecretIsNoSecret() {
        assertThat(TokenizerKey.of("http://a#m", " ")).isEqualTo(TokenizerKey.of("http://a#m"));
        assertThat(TokenizerKey.of("http://a#m", null)).isEqualTo(TokenizerKey.of("http://a#m"));
    }

    @Test
    void secretIsNotShown() {
        TokenizerKey key = TokenizerKey.of("http://a#m", "sk-very-secret");

        assertThat(key.toString()).isEqualTo("http://a#m").doesNotContain("sk-very-secret");
        assertThat(key.id()).isEqualTo("http://a#m");
    }
}
