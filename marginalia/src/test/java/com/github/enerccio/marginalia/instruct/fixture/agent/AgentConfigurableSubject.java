package com.github.enerccio.marginalia.instruct.fixture.agent;

import com.github.enerccio.marginalia.domain.traits.Extendable;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Shaped like the UI classes of the application: {@code @Configurable} (AspectJ adds interfaces to the class) with a
 * generic supertype (so the class has a generic signature, which does not name the added interfaces).
 */
@Extendable
@Configurable
public class AgentConfigurableSubject implements Supplier<String>, Serializable {

    @Override
    public String get() {
        StringBuilder value = new StringBuilder("configured");
        return value.toString();
    }
}
