package com.github.enerccio.marginalia.domain.service.tool;

import org.osgi.framework.Bundle;

/**
 * Who asks a {@link ToolProvider} for its functions, so that it can decide what to give.
 *
 * @param caller           the bundle of the caller, null when the caller is the application itself
 * @param callerIdentifier a string that identifies the caller, unique to it, like the name of the class of its extension
 */
public record ToolRequest(Bundle caller, String callerIdentifier) {}
