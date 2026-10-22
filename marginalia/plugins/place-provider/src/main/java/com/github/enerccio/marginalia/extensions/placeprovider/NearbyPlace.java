package com.github.enerccio.marginalia.extensions.placeprovider;

/**
 * A place and how far it is from the point that was asked about.
 *
 * @param place     the place
 * @param km        great-circle distance in kilometers, to the label point of an area or a line
 * @param direction compass direction from the point to the place: {@code N}, {@code NE}, {@code E}...
 */
public record NearbyPlace(Place place, double km, String direction) {}
