package com.github.enerccio.marginalia.extensions.grok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

import static com.github.enerccio.marginalia.extensions.grok.GrokLocalization.*;

/**
 * Maps what the xAI API answers (and the network throws) to an {@link InferenceException}, so the user sees what to fix.
 */
final class GrokErrors {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Pattern CONTEXT_OVERFLOW = Pattern.compile(
            "maximum prompt length|context (length|window)|too many tokens|exceeds? .{0,40}(context|tokens|limit)"
                    + "|prompt (is )?too long|request too large",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern MODEL_MISSING = Pattern.compile(
            "model.{0,60}(not found|does not exist|not exist|unknown|invalid)|(not found|unknown|invalid).{0,40}model",
            Pattern.CASE_INSENSITIVE);

    private GrokErrors() {
    }

    /** The exception as an {@link InferenceException}; anything else that is not a network error is returned as it is. */
    static Throwable translate(Throwable e) {
        if (e instanceof InferenceException) {
            return e;
        }
        if (isIo(e)) {
            return fromIo(e);
        }
        return e;
    }

    /** The error of an HTTP answer that was not a success. */
    static InferenceException fromResponse(int status, String body) {
        String message = message(body);
        String lower = message.toLowerCase();

        Type type;
        if (status == 401) {
            type = Type.AUTHENTICATION;
        } else if (status == 403) {
            type = Type.PERMISSION;
        } else if (status == 404 || (status == 400 && MODEL_MISSING.matcher(lower).find())) {
            type = MODEL_MISSING.matcher(lower).find() ? Type.MODEL_NOT_FOUND : Type.NOT_FOUND;
        } else if (status == 408 || status == 504) {
            type = Type.TIMEOUT;
        } else if (status == 413 || (status < 500 && status != 429 && CONTEXT_OVERFLOW.matcher(lower).find())) {
            type = Type.CONTEXT_OVERFLOW;
        } else if (status == 429) {
            type = Type.RATE_LIMIT;
        } else if (status >= 500) {
            type = Type.SERVER;
        } else {
            type = Type.BAD_REQUEST;
        }
        return new InferenceException(type, status, message, null);
    }

    /**
     * The message of an error body: xAI answers {@code {"code":"...","error":"text"}}, a proxy or a compatible server
     * {@code {"error":{"message":"text"}}}; a body that is not JSON is the message itself.
     */
    static String message(String body) {
        if (StringUtils.isBlank(body)) {
            return "";
        }
        try {
            JsonNode node = MAPPER.readTree(body);
            JsonNode error = node.path("error");
            if (error.isTextual() && !error.asText().isEmpty()) {
                return error.asText();
            }
            String message = error.path("message").asText("");
            if (!message.isEmpty()) {
                return message;
            }
            message = node.path("message").asText("");
            if (!message.isEmpty()) {
                return message;
            }
        } catch (IOException ignored) {
            // not JSON
        }
        return body.length() > 500 ? body.substring(0, 500) : body;
    }

    /**
     * A response that ended without an answer because the model was stopped.
     *
     * @param reason the finish reason ({@code length}, {@code content_filter}...)
     */
    static InferenceException noAnswer(String reason) {
        if ("length".equals(reason)) {
            return new InferenceException(Type.BAD_REQUEST, null, T.localized(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE).formatted(reason), null);
        }
        return new InferenceException(Type.BAD_REQUEST, null, T.localized(THE_MODEL_GAVE_NO_ANSWER).formatted(reason), null);
    }

    private static boolean isIo(Throwable e) {
        for (int depth = 0; e != null && depth < 10; depth++, e = e.getCause()) {
            if (e instanceof IOException) {
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
