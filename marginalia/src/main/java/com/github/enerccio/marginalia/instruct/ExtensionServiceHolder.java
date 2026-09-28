package com.github.enerccio.marginalia.instruct;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@SuppressWarnings("unused") // is used dynamically
@Configurable
public class ExtensionServiceHolder {

    private static ExtensionService instance;

    @Autowired
    private ExtensionService extensionService;

    private ExtensionServiceHolder() {

    }

    public static ExtensionService getInstance() {
        if (instance == null)
            instance = new ExtensionServiceHolder().extensionService;
        return instance;
    }
}