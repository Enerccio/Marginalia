package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;

/**
 * The model and the protocol of a request.
 *
 * @param protocol may be null
 */
public record ModelSelection(AI ai, Protocol protocol) {
}
