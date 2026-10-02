package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.vaadin.flow.component.Component;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.util.List;

public interface OsgiService {

    BundleContext getContext();

    Bundle findBundleByJar(String jarName);

    void start() throws Exception;

    void stop() throws Exception;

    void restart() throws Exception;

    void installPackage(File f) throws Exception;

    void uninstallPackage(Bundle b) throws Exception;

    List<Bundle> getBundles();

    List<ServiceReference<?>> getServices(Bundle b, Class<?> service) throws InvalidSyntaxException;

    List<ServiceReference<?>> getServices(Class<?> service) throws InvalidSyntaxException;

    void addSubscriber(ExtensionObserver observer);

    void removeSubscriber(ExtensionObserver observer);

    String getExtensionsPath();

    <T extends Component> T bindAttachableComponent(T component, Runnable callback, MarginaliaExtension extension) throws Exception;

    interface ExtensionObserver {

        void beforeServiceStarted(OsgiService service);
        void afterServiceStarted(OsgiService service);
        void beforeServiceStopped(OsgiService service);
        void afterServiceStopped(OsgiService service);

    }
}
