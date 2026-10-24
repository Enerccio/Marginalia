package com.github.enerccio.marginalia.extensions.placeprovider;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

import java.util.List;

/**
 * The functions the model can call to look up real places: the types there are, a search by name, type and country, the
 * places around a point and the distance of two places.
 */
public interface PlaceProviderFunctions {

    String QUERY = "Part of a name, with or without accents or case ('hudson', 'st lawrence'); leave it out or give * for any name";
    String TYPE = "A type from list_place_types, or what it is called ('inlets', 'mountains', 'towns', 'states'); leave it out for any";
    String COUNTRY = "A country by name or code ('Canada', 'CA', 'CAN'); places that touch it count; leave it out for any";
    String PLACE = "A place by name ('Ottawa', 'Paris, France': a comma and the country or province picks one of several) or coordinates ('45.42, -75.69')";

    @LLMFunction(name = "list_place_types", linkedTool = {"search_places", "places_near"},
            description = "Lists the types of real-world places that can be searched (city, capital, bay, inlet, river, mountain range, island...) "
                    + "with how many places of each there are.")
    void listTypes(FunctionContinuation<List<TypeCount>> done);

    @LLMFunction(name = "search_places", linkedTool = {"list_place_types", "places_near"},
            description = "Searches real-world places of the Earth (Natural Earth data: cities, capitals, provinces, countries, seas, bays, gulfs, "
                    + "straits, fjords, lakes, rivers, islands, capes, mountains, mountain ranges, deserts, ports, airports, parks) by name, type and "
                    + "country. Give at least one of the three. Returns the best matches first with their coordinates, the countries they touch and "
                    + "how many places fit in all. It is a map of the notable places, not every one: a small creek or village may be missing.")
    void searchPlaces(FunctionContinuation<PlaceSearch> done,
                      @LLMParam(required = false, description = QUERY) String query,
                      @LLMParam(required = false, description = TYPE) String type,
                      @LLMParam(required = false, description = COUNTRY) String country,
                      @LLMParam(required = false, description = "How many places to return, 1 to " + PlaceIndex.MAX_LIMIT
                              + ". Default " + PlaceIndex.DEFAULT_LIMIT) Integer limit);

    @LLMFunction(name = "places_near", linkedTool = {"list_place_types", "search_places"},
            description = "Lists the real-world places around a place or a point, the closest first, with the distance in kilometers and the compass "
                    + "direction from it. Use it to find what lies around a place.")
    void placesNear(FunctionContinuation<Nearby> done,
                    @LLMParam(description = PLACE) String place,
                    @LLMParam(name = "radius_km", description = "How far around it to look, in kilometers") double radiusKm,
                    @LLMParam(required = false, description = TYPE) String type,
                    @LLMParam(required = false, description = "How many places to return, 1 to " + PlaceIndex.MAX_LIMIT
                            + ". Default " + PlaceIndex.DEFAULT_LIMIT) Integer limit);

    @LLMFunction(name = "distance_between",
            description = "The great-circle distance (a straight line over the surface of the Earth, not a road) in kilometers and miles between two "
                    + "real-world places or points, with the direction. The answer names the places that the names were taken for, check them "
                    + "when a name can mean several.")
    void distanceBetween(FunctionContinuation<Distance> done,
                         @LLMParam(description = PLACE) String from,
                         @LLMParam(description = PLACE) String to);

}
