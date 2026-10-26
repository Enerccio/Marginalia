package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code GET /health} is up while the context and the database work and down (503) otherwise.
 */
class HealthServletTest extends MarginaliaTestBase {

    @Autowired
    private WebApplicationContext applicationContext;

    private MockHttpServletResponse call(String method) throws Exception {
        HealthServlet servlet = new HealthServlet();
        servlet.init(new MockServletConfig(applicationContext.getServletContext()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(new MockHttpServletRequest(method, HealthServlet.PATH), response);
        return response;
    }

    @Test
    void runningApplicationIsUp() throws Exception {
        MockHttpServletResponse response = call("GET");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getContentAsString()).isEqualTo("{\"status\":\"UP\"}");
    }

    @Test
    void headHasNoBody() throws Exception {
        MockHttpServletResponse response = call("HEAD");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void startingApplicationIsDown() {
        HealthServlet.Report report = HealthServlet.check(null);

        assertThat(report.up()).isFalse();
        assertThat(report.toJson()).isEqualTo("{\"status\":\"DOWN\",\"context\":\"DOWN\",\"database\":\"DOWN\"}");
    }

    @Test
    void closedContextIsDown() {
        GenericApplicationContext closed = new GenericApplicationContext();
        closed.refresh();
        closed.close();

        assertThat(HealthServlet.check(closed).up()).isFalse();
    }

    @Test
    void databaseThatDoesNotAnswerIsDown() {
        DataSource broken = (DataSource) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{DataSource.class}, (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        throw new SQLException("database is gone");
                    }
                    return null;
                });
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.registerBean("dataSource", DataSource.class, () -> broken);
            context.refresh();

            HealthServlet.Report report = HealthServlet.check(context);

            assertThat(report.context()).isTrue();
            assertThat(report.database()).isFalse();
            assertThat(report.toJson()).isEqualTo("{\"status\":\"DOWN\",\"context\":\"UP\",\"database\":\"DOWN\"}");
        }
    }
}
