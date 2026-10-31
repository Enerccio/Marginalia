package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class BetaReadersActivator implements BundleActivator {

    private ServiceRegistration<MarginaliaExtension> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        BetaReadersExtension extension = new BetaReadersExtension();
        registration = context.registerService(MarginaliaExtension.class, extension, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
