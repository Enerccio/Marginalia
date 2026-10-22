package com.github.enerccio.marginalia.extensions.placeprovider;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

import java.util.List;
import java.util.function.Supplier;

/**
 * The functions of {@link PlaceProviderFunctions} on the places of {@link PlaceIndex}. A question that is wrong (an unknown
 * type or country, a place that is not there) fails the call with a message the model can act on.
 */
public class PlaceProviderFunctionsImpl implements PlaceProviderFunctions {

    private final PlaceIndex index;

    public PlaceProviderFunctionsImpl(PlaceIndex index) {
        this.index = index;
    }

    @Override
    public void listTypes(FunctionContinuation<List<TypeCount>> done) {
        done.complete(index.types());
    }

    @Override
    public void searchPlaces(FunctionContinuation<PlaceSearch> done, String query, String type, String country, Integer limit) {
        answer(done, () -> index.search(query, type, country, limit));
    }

    @Override
    public void placesNear(FunctionContinuation<Nearby> done, String place, double radiusKm, String type, Integer limit) {
        answer(done, () -> index.near(place, radiusKm, type, limit));
    }

    @Override
    public void distanceBetween(FunctionContinuation<Distance> done, String from, String to) {
        answer(done, () -> index.distance(from, to));
    }

    private static <T> void answer(FunctionContinuation<T> done, Supplier<T> question) {
        T answer;
        try {
            answer = question.get();
        } catch (IllegalArgumentException e) {
            done.fail(e.getMessage());
            return;
        }
        done.complete(answer);
    }

}
