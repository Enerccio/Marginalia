package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import org.apache.commons.lang3.StringUtils;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

/**
 * Maps what the SDK throws to an {@link InferenceException}, so the user sees what to fix (the application knows its
 * types and shows a text for each).
 */
final class GoogleGenAiErrors {

    private static final Pattern CONTEXT_OVERFLOW = Pattern.compile(
            "input token count|token count .{0,40}exceeds|exceeds the maximum number of tokens|context (length|window)"
                    + "|too many tokens|request payload size exceeds",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BAD_KEY = Pattern.compile("api key not valid|api_key_invalid|invalid api key|api key expired",
            Pattern.CASE_INSENSITIVE);

    private GoogleGenAiErrors() {
    }

    /**
     * The exception as an {@link InferenceException}; anything the SDK did not throw is returned as it is.
     */
    static Throwable translate(Throwable e) {
        if (e instanceof InferenceException) {
            return e;
        }
        if (e instanceof ApiException api) {
            return fromApi(api);
        }
        if (e instanceof GenAiIOException || isIo(e)) {
            return fromIo(e);
        }
        return e;
    }

    /**
     * A response that came back without an answer because the model was stopped: the safety filters, mostly, or all
     * the tokens went to the thinking.
     */
    static InferenceException noAnswer(String reason, String message) {
        String text = "The model gave no answer (" + reason + ")";
        if ("MAX_TOKENS".equals(reason)) {
            text += ": the response size was used up before the first word, raise it or lower the reasoning effort";
        } else if (StringUtils.isNotBlank(message)) {
            text += ": " + message;
        }
        return new InferenceException(Type.BAD_REQUEST, null, text, null);
    }

    private static InferenceException fromApi(ApiException e) {
        int status = e.code();
        String message = e.message();
        String lower = StringUtils.defaultString(message).toLowerCase();

        Type type;
        if (status == 401 || BAD_KEY.matcher(lower).find()) {
            type = Type.AUTHENTICATION;
        } else if (status == 403) {
            type = Type.PERMISSION;
        } else if (status == 404) {
            type = lower.contains("model") ? Type.MODEL_NOT_FOUND : Type.NOT_FOUND;
        } else if (status == 408 || status == 504) {
            type = Type.TIMEOUT;
        } else if (status == 429) {
            type = Type.RATE_LIMIT;
        } else if (status >= 500) {
            type = Type.SERVER;
        } else if (CONTEXT_OVERFLOW.matcher(lower).find()) {
            type = Type.CONTEXT_OVERFLOW;
        } else {
            type = Type.BAD_REQUEST;
        }
        return new InferenceException(type, status, message, e);
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
