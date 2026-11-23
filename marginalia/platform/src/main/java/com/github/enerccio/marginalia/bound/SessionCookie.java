package com.github.enerccio.marginalia.bound;

import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Name of the servlet session cookie. Cookies are scoped to the host, not to the port, so two applications on the
 * platform on one host (two ports, or one domain behind a proxy) that both keep the container's {@code JSESSIONID}
 * with {@code Path=/} replace each other's session on every switch between them. An application's
 * {@code WebApplicationInitializer} calls {@link #configure} in its {@code onStartup}, before the context is
 * initialized (the servlet API refuses the change later).
 */
public final class SessionCookie {
    private static final Logger log = LoggerFactory.getLogger(SessionCookie.class);

    /**
     * The setting that overrides the name: a {@code -D} system property or the environment variable
     * {@code MARGINALIA_SESSION_COOKIE}.
     */
    public static final String SETTING = "marginalia.session-cookie";

    private SessionCookie() {
    }

    /**
     * Sets the name of the session cookie: the setting {@value #SETTING} when it is set, else {@code defaultName};
     * when both are empty the container's default ({@code JSESSIONID}) stays.
     *
     * @return the name set, {@code null} when the default stays
     */
    public static String configure(ServletContext servletContext, String defaultName) {
        String name = setting(SETTING);
        if (name == null) {
            name = blankToNull(defaultName);
        }
        if (name == null) {
            return null;
        }
        if (!isValidName(name)) {
            log.warn("The session cookie name '{}' is not a valid cookie name, the default stays", name);
            return null;
        }
        servletContext.getSessionCookieConfig().setName(name);
        return name;
    }

    /**
     * A cookie name is an RFC 6265 token: ASCII letters, digits and {@code !#$%&'*+-.^_`|~}.
     */
    static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || "!#$%&'*+-.^_`|~".indexOf(c) >= 0;
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /**
     * The value of a setting the way Spring's placeholders find it: the system property of the name, then the
     * environment variable of the name and of its upper case form with {@code .} and {@code -} as {@code _}.
     */
    static String setting(String name) {
        String value = blankToNull(System.getProperty(name));
        if (value == null) {
            value = blankToNull(System.getenv(name));
        }
        if (value == null) {
            value = blankToNull(System.getenv(name.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT)));
        }
        return value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
