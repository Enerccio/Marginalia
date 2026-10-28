package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class AnthropicActivator implements BundleActivator {

    private ServiceRegistration<MarginaliaExtension> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        registration = context.registerService(MarginaliaExtension.class, new AnthropicExtension(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }
}
