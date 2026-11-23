package com.github.enerccio.marginalia.ui.main;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Health endpoint {@code GET /health} for Docker, Kubernetes and load balancers, no login needed. {@code 200} and
 * {@code {"status":"UP"}} when the application context is running and the database answers, else {@code 503} and
 * {@code {"status":"DOWN","database":"DOWN"}} (the context still starting up counts as down). It tells nothing else
 * (no version, no details), the reason of a failure is in the log.
 * <p>
 * Registered in {@link WebappApplicationInitializer}, the exact mapping goes before the {@code /*} of the Vaadin servlet.
 */
public class HealthServlet extends HttpServlet {

    public static final String PATH = "/health";

    private static final Logger log = LoggerFactory.getLogger(HealthServlet.class);
    private static final int DATABASE_TIMEOUT_SECONDS = 3;

    /**
     * What the checks found.
     */
    public record Report(boolean context, boolean database) {
        public boolean up() {
            return context && database;
        }

        public String toJson() {
            if (up()) {
                return "{\"status\":\"UP\"}";
            }
            return "{\"status\":\"DOWN\",\"context\":\"" + (context ? "UP" : "DOWN") + "\",\"database\":\""
                    + (database ? "UP" : "DOWN") + "\"}";
        }
    }

    /**
     * Checks the application context (may be {@code null} while the application starts) and the database.
     */
    public static Report check(ApplicationContext context) {
        if (context == null) {
            return new Report(false, false);
        }
        boolean active = isActive(context);
        return new Report(active, active && databaseAnswers(context));
    }

    private static boolean isActive(ApplicationContext context) {
        try {
            return !(context instanceof ConfigurableApplicationContext configurable) || configurable.isActive();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean databaseAnswers(ApplicationContext context) {
        try {
            DataSource dataSource = context.getBean("dataSource", DataSource.class);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(DATABASE_TIMEOUT_SECONDS);
                statement.execute("SELECT 1");
            }
            return true;
        } catch (Exception e) {
            log.warn("Health check: the database does not answer", e);
            return false;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Report report = check(WebApplicationContextUtils.getWebApplicationContext(getServletContext()));
        byte[] body = report.toJson().getBytes(StandardCharsets.UTF_8);
        resp.setStatus(report.up() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.setContentLength(body.length);
        // HEAD gets the headers only
        if (!"HEAD".equals(req.getMethod())) {
            resp.getOutputStream().write(body);
        }
    }
}
