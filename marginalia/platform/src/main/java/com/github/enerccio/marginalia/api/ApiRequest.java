package com.github.enerccio.marginalia.api;

import java.util.Map;

/**
 * A call of the HTTP API as {@link ApiDispatcher} sees it, without the servlet.
 *
 * @param method        {@code GET}, {@code POST}, {@code PUT}, {@code DELETE}
 * @param path          the path after the context, {@code /api/v1/version}
 * @param query         the query parameters (the first value of each)
 * @param authorization the {@code Authorization} header, null when there is none
 * @param body          the body, empty when there is none
 */
public record ApiRequest(String method, String path, Map<String, String> query, String authorization, byte[] body) {

    public ApiRequest {
        query = query == null ? Map.of() : Map.copyOf(query);
        body = body == null ? new byte[0] : body;
    }

    public String query(String name) {
        return query.get(name);
    }
}
