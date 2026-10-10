package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.vaadin.flow.component.Component;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.List;

public interface OsgiService {

    BundleContext getContext();

    Bundle findBundleByJar(String jarName);

    void start() throws Exception;

    void stop() throws Exception;

    void restart() throws Exception;

    /**
     * Stores an uploaded JAR in the extensions folder (under a sanitized file name) and loads it. An installed bundle
     * with the same symbolic name or file name is unloaded and replaced. When the new bundle can't be installed or
     * started, the uploaded file is deleted and the replaced bundle is restored.
     *
     * @throws ExtensionVerificationException when the extension asks for something this version of the application
     *                                        doesn't have, the exception carries the report
     */
    Bundle installPackage(String fileName, byte[] data) throws Exception;

    /**
     * Unloads the bundle, uninstalls it and deletes its JAR.
     */
    void uninstallPackage(Bundle b) throws Exception;

    /**
     * Every installed bundle except the framework itself, also the ones that failed to start.
     */
    List<Bundle> getBundles();

    List<ServiceReference<?>> getServices(Bundle b, Class<?> service) throws InvalidSyntaxException;

    List<ServiceReference<?>> getServices(Class<?> service) throws InvalidSyntaxException;

    void addSubscriber(ExtensionObserver observer);

    void removeSubscriber(ExtensionObserver observer);

    String getExtensionsPath();

    /**
     * The report of the verification of the extension ({@link ExtensionService#verifyExtension}), {@code null} when
     * there is none yet. An extension whose report says it is invalid is installed but not started.
     */
    ExtensionReport getVerificationReport(Bundle b);

    record ExtensionReport(boolean valid, String text) {
    }

    /**
     * Registers a cleanup callback for a component the extension added to the UI. When the extension is unloaded, the
     * callback runs for every bound component that is attached, inside {@code ui.access(...)} of the component's UI;
     * a component that is detached at that moment runs it when it is attached again.
     *
     * @throws IllegalStateException when the extension is not loaded
     */
    <T extends Component> T bindAttachableComponent(T component, Runnable callback, MarginaliaExtension extension) throws Exception;

    interface ExtensionObserver {

        void beforeServiceStarted(OsgiService service);
        void afterServiceStarted(OsgiService service);
        void beforeServiceStopped(OsgiService service);
        void afterServiceStopped(OsgiService service);

    }
}
