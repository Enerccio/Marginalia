package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.api.ApiDispatcher;
import com.github.enerccio.marginalia.api.ApiRequest;
import com.github.enerccio.marginalia.api.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The HTTP API under {@value #PATH}: reads the request (the body up to a limit) and hands it to {@link ApiDispatcher}, which does
 * everything else. It never creates a session of the container. Registered in {@link WebappApplicationInitializer}; the prefix
 * mapping goes before the {@code /*} of the Vaadin servlet.
 */
public class ApiServlet extends HttpServlet {

    public static final String PATH = "/api/*";

    /** A JAR of an extension. */
    static final int MAX_UPLOAD_BYTES = 256 * 1024 * 1024;
    /** Anything else (the arguments of a command, a chapter of text in them). */
    static final int MAX_BODY_BYTES = 16 * 1024 * 1024;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        if (context == null) {
            write(response, ApiResponse.error(503, "The application is starting"));
            return;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        int limit = "PUT".equalsIgnoreCase(request.getMethod()) && path.startsWith(ApiDispatcher.PREFIX + "/admin/extensions/")
                ? MAX_UPLOAD_BYTES : MAX_BODY_BYTES;
        if (request.getContentLengthLong() > limit) {
            write(response, ApiResponse.error(413, "The body is larger than " + limit + " bytes"));
            return;
        }
        byte[] body = read(request.getInputStream(), limit);
        if (body == null) {
            write(response, ApiResponse.error(413, "The body is larger than " + limit + " bytes"));
            return;
        }

        ApiRequest apiRequest = new ApiRequest(request.getMethod(), path, query(request.getQueryString()), request.getHeader("Authorization"), body);
        write(response, context.getBean(ApiDispatcher.class).handle(apiRequest));
    }

    /**
     * The parameters of the query string, the first value of each. Not {@code getParameterMap()}: for a body sent as a form (what
     * {@code curl -d} does by default) it would read the body.
     */
    static Map<String, String> query(String queryString) {
        Map<String, String> query = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return query;
        }
        for (String pair : queryString.split("&")) {
            int eq = pair.indexOf('=');
            String name = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            if (!name.isEmpty()) {
                query.putIfAbsent(name, value);
            }
        }
        return query;
    }

    /**
     * The body, null when it is over the limit.
     */
    private static byte[] read(InputStream in, int limit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[64 * 1024];
        int total = 0;
        int n;
        while ((n = in.read(buffer)) > 0) {
            total += n;
            if (total > limit) {
                return null;
            }
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    private static void write(HttpServletResponse response, ApiResponse answer) throws IOException {
        response.setStatus(answer.status());
        response.setContentType(answer.contentType());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        answer.headers().forEach(response::setHeader);
        response.setContentLength(answer.body().length);
        response.getOutputStream().write(answer.body());
    }
}
