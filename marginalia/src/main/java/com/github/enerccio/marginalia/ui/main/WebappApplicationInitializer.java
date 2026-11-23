package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.bound.SessionCookie;
import com.github.enerccio.marginalia.bound.SessionTrackingListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.jspecify.annotations.NonNull;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class WebappApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(@NonNull ServletContext servletContext) throws ServletException {
        System.setProperty("vaadin.copilot.enable", "false");
        // JSESSIONID unless MARGINALIA_SESSION_COOKIE names another one (two applications on one host)
        SessionCookie.configure(servletContext, null);
        initRootApplicationContext(servletContext);
        servletContext.addServlet("HealthServlet", new HealthServlet()).addMapping(HealthServlet.PATH);
        servletContext.addServlet("ApiServlet", new ApiServlet()).addMapping(ApiServlet.PATH);
    }

    @SuppressWarnings("UnusedReturnValue")
    private XmlWebApplicationContext initRootApplicationContext(ServletContext servletContext) throws ServletException {
        XmlWebApplicationContext rootContext = new XmlWebApplicationContext();
        rootContext.setConfigLocations("/WEB-INF/classes/META-INF/spring/application-config.xml");

        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.addListener(new RequestContextListener());
        servletContext.addListener(new SessionTrackingListener(rootContext));
        return rootContext;
    }
}
