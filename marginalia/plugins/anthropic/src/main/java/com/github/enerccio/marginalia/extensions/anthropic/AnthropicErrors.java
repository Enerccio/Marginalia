package com.github.enerccio.marginalia.extensions.anthropic;

import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import org.apache.commons.lang3.StringUtils;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

/**
 * Maps what the SDK throws to an {@link InferenceException}, so the user sees what to fix (the application knows its
 * types and shows a text for each).
 */
final class AnthropicErrors {

    private static final Pattern CONTEXT_OVERFLOW = Pattern.compile(
            "prompt is too long|exceed(s|ed)? the (model's )?(maximum )?context|context (length|window)"
                    + "|input length and .max_tokens. exceed|too many tokens|request_too_large",
            Pattern.CASE_INSENSITIVE);

    private AnthropicErrors() {
    }

    /**
     * The exception as an {@link InferenceException}; anything the SDK did not throw is returned as it is.
     */
    static Throwable translate(Throwable e) {
        if (e instanceof InferenceException) {
            return e;
        }
        if (e instanceof AnthropicServiceException api) {
            return fromApi(api);
        }
        if (e instanceof AnthropicIoException || isIo(e)) {
            return fromIo(e);
        }
        return e;
    }

    /**
     * A response that came back without an answer because the model was stopped: it refused, or all the tokens went
     * to the thinking, or the context was full.
     *
     * @param reason  the stop reason of the response ({@code refusal}, {@code max_tokens}...)
     * @param message the explanation Anthropic gave, may be null
     */
    static InferenceException noAnswer(String reason, String message) {
        if ("model_context_window_exceeded".equals(reason)) {
            return new InferenceException(Type.CONTEXT_OVERFLOW, null, "The context window of the model was used up", null);
        }
        String text = "The model gave no answer (" + reason + ")";
        if ("max_tokens".equals(reason)) {
            text += ": the response size was used up before the first word, raise it or lower the reasoning effort";
        } else if (StringUtils.isNotBlank(message)) {
            text += ": " + message;
        }
        return new InferenceException(Type.BAD_REQUEST, null, text, null);
    }

    private static InferenceException fromApi(AnthropicServiceException e) {
        int status = e.statusCode();
        String message = message(e);
        String lower = message.toLowerCase();

        Type type;
        if (status == 401) {
            type = Type.AUTHENTICATION;
        } else if (status == 403) {
            type = Type.PERMISSION;
        } else if (status == 404) {
            type = lower.contains("model") ? Type.MODEL_NOT_FOUND : Type.NOT_FOUND;
        } else if (status == 408 || status == 504) {
            type = Type.TIMEOUT;
        } else if (status == 413 || (status < 500 && status != 429 && CONTEXT_OVERFLOW.matcher(lower).find())) {
            type = Type.CONTEXT_OVERFLOW;
        } else if (status == 429) {
            type = Type.RATE_LIMIT;
        } else if (status >= 500) {
            // 529 is Anthropic's "overloaded"
            type = Type.SERVER;
        } else {
            type = Type.BAD_REQUEST;
        }
        return new InferenceException(type, status, message, e);
    }

    /** The message of the error body ({@code {"type":"error","error":{"message":"..."}}}), the exception's own text otherwise. */
    static String message(AnthropicServiceException e) {
        try {
            JsonNode body = e.body().convert(JsonNode.class);
            String message = body == null ? "" : body.path("error").path("message").asText("");
            if (!message.isEmpty()) {
                return message;
            }
        } catch (RuntimeException ignored) {
            // an error body that is not JSON
        }
        return StringUtils.defaultString(e.getMessage());
    }

    private static boolean isIo(Throwable e) {
        for (int depth = 0; e != null && depth < 10; depth++, e = e.getCause()) {
            if (e instanceof java.io.IOException) {
                return true;
            }
        }
        return false;
    }

    private static InferenceException fromIo(Throwable e) {
        boolean timeout = false;
        Throwable root = e;
        for (int depth = 0; root != null && depth < 10; depth++) {
            if (root instanceof SocketTimeoutException
                    || (root instanceof InterruptedIOException && StringUtils.containsIgnoreCase(root.getMessage(), "timeout"))) {
                timeout = true;
                break;
            }
            if (root.getCause() == null || root.getCause() == root) {
                break;
            }
            root = root.getCause();
        }
        Throwable cause = root != null ? root : e;
        return new InferenceException(timeout ? Type.TIMEOUT : Type.CONNECTION, null,
                cause.getClass().getSimpleName() + (cause.getMessage() != null ? ": " + cause.getMessage() : ""), e);
    }
}
