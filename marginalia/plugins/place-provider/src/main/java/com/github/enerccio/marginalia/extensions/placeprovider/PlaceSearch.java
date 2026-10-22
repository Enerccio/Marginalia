package com.github.enerccio.marginalia.extensions.placeprovider;

import java.util.List;

/**
 * The answer of a search: the places that fit, the best first, and how many there were in all.
 *
 * @param total  how many places fit; more than {@code places} holds when the answer was cut at the limit
 * @param places the best matches
 */
public record PlaceSearch(int total, List<Place> places) {}
