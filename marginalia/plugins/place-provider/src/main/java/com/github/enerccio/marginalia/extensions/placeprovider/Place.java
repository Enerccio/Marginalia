package com.github.enerccio.marginalia.extensions.placeprovider;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * A place as the model gets it. Everything but {@code name}, {@code type}, {@code lat} and {@code lon} may be missing (null,
 * and left out of the JSON, which an empty list is too).
 *
 * @param name       the name in English (or romanized)
 * @param type       what it is: {@code bay}, {@code city}, {@code river}, {@code mountain range}... (see {@link PlaceIndex#types()})
 * @param lat        latitude in degrees, north positive; for an area or a line a point on or in it that a label would go to
 * @param lon        longitude in degrees, east positive
 * @param bbox       {@code [west, south, east, north]} in degrees of a line or an area (a river, a bay, a country), else null
 * @param countries  the countries it is in or touches, the ones with the most of it first
 * @param alt        other names
 * @param within     the province of a city or the continent of a physical feature
 * @param pop        inhabitants of a place or a country
 * @param elevation  meters above the sea of a mountain
 * @param code       IATA code of an airport, ISO 3166-1 alpha-3 code of a country
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Place(String name, String type, double lat, double lon, double[] bbox, List<String> countries,
                    List<String> alt, String within, Long pop, Integer elevation, String code) {

    public Place {
        countries = countries == null || countries.isEmpty() ? null : countries;
        alt = alt == null || alt.isEmpty() ? null : alt;
    }

    /** Whether the place is in or touches the country (by its name in the data). */
    public boolean isIn(String country) {
        return countries != null && countries.contains(country);
    }

}
