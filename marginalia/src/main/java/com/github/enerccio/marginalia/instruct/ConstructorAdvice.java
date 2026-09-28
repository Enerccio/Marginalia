package com.github.enerccio.marginalia.instruct;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public  class ConstructorAdvice {

    @Autowired
    private ExtensionService extensionService;

    @Advice.OnMethodExit
    public static void onExit(@Advice.This Object target) {
        try {
            ExtensionService service = new ConstructorAdvice().extensionService;
            if (service != null) {
                target.getClass().getField("$extensionService").set(target, service);
            }
        } catch (Exception ignored) {
        }
    }
}