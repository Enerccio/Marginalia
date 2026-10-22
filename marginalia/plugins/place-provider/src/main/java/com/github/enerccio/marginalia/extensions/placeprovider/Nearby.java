package com.github.enerccio.marginalia.extensions.placeprovider;

import java.util.List;

/**
 * The answer of a search around a point: the closest places first.
 *
 * @param origin the point the distances are from (the place that was named, or the coordinates)
 * @param total  how many places are in the radius; more than {@code places} holds when the answer was cut at the limit
 * @param places the closest ones
 */
public record Nearby(Place origin, int total, List<NearbyPlace> places) {}
