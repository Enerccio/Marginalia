package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the SDK throws, as the failures the application shows to the user.
 */
class GoogleGenAiErrorsTest {

    private static InferenceException translate(Throwable e) {
        return (InferenceException) GoogleGenAiErrors.translate(e);
    }

    private static Type typeOf(int code, String message) {
        return translate(new ApiException(code, "STATUS", message)).getType();
    }

    @Test
    void invalidKeyIsAnAuthenticationFailureWhateverTheStatus() {
        assertEquals(Type.AUTHENTICATION, typeOf(400, "API key not valid. Please pass a valid API key."));
        assertEquals(Type.AUTHENTICATION, typeOf(401, "Unauthorized"));
    }

    @Test
    void statusDecidesTheOthers() {
        assertEquals(Type.PERMISSION, typeOf(403, "The caller does not have permission"));
        assertEquals(Type.MODEL_NOT_FOUND, typeOf(404, "models/nothing is not found for API version v1beta"));
        assertEquals(Type.NOT_FOUND, typeOf(404, "Not found"));
        assertEquals(Type.TIMEOUT, typeOf(504, "Deadline exceeded"));
        assertEquals(Type.RATE_LIMIT, typeOf(429, "Quota exceeded"));
        assertEquals(Type.SERVER, typeOf(503, "The model is overloaded"));
        assertEquals(Type.BAD_REQUEST, typeOf(400, "Invalid JSON payload"));
    }

    @Test
    void promptThatDoesNotFitIsAContextOverflow() {
        assertEquals(Type.CONTEXT_OVERFLOW, typeOf(400,
                "The input token count (2000000) exceeds the maximum number of tokens allowed (1048576)."));
    }

    @Test
    void providerMessageAndStatusAreKept() {
        InferenceException e = translate(new ApiException(429, "RESOURCE_EXHAUSTED", "Quota exceeded"));

        assertEquals(429, e.getStatusCode());
        assertEquals("Quota exceeded", e.getProviderMessage());
    }

    @Test
    void networkFailuresAreConnectionOrTimeout() {
        assertEquals(Type.CONNECTION, translate(new GenAiIOException("failed", new IOException("refused"))).getType());
        assertEquals(Type.TIMEOUT, translate(new GenAiIOException("failed", new SocketTimeoutException("Read timed out"))).getType());
    }

    @Test
    void otherExceptionsAndInferenceExceptionsAreLeftAlone() {
        IllegalStateException other = new IllegalStateException("bug");
        InferenceException inference = new InferenceException(Type.AUTHENTICATION, null, "no key", null);

        assertSame(other, GoogleGenAiErrors.translate(other));
        assertSame(inference, GoogleGenAiErrors.translate(inference));
    }

    @Test
    void stoppedModelIsExplained() {
        assertTrue(GoogleGenAiErrors.noAnswer("SAFETY", "filtered").getProviderMessage().contains("SAFETY"));
        assertTrue(GoogleGenAiErrors.noAnswer("MAX_TOKENS", null).getProviderMessage().contains("response size"));
    }
}
