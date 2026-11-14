package com.github.enerccio.marginalia.instruct.fixture.agent;

import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.instruct.Woven;

/**
 * Carries the mark of a class woven at build time (here written by hand, the methods have no hooks): neither the
 * agent nor the weaver may touch it.
 */
@Extendable
@Woven
public class AgentWovenSubject {

    public String greet(String name) {
        return "woven " + name;
    }
}
