package com.github.enerccio.marginalia.instruct.fixture.agent;

/**
 * Not {@code @Extendable} - must stay untouched by the agent.
 */
public class AgentPlainSubject {

    public String greet(String name) {
        return "hi " + name;
    }
}
