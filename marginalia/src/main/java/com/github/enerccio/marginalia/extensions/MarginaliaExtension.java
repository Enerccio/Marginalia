package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import org.osgi.framework.Bundle;

public interface MarginaliaExtension {

    void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService);

    void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService);

}
