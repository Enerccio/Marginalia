package com.github.enerccio.marginalia.extensions.placeprovider;

/**
 * The distance between two places, with the places that the names were taken for.
 *
 * @param from      the first place
 * @param to        the second place
 * @param km        great-circle distance in kilometers (a straight line over the surface, not a road)
 * @param miles     the same in miles
 * @param direction compass direction from the first to the second place at the start: {@code N}, {@code NE}, {@code E}...
 */
public record Distance(Place from, Place to, double km, double miles, String direction) {}
