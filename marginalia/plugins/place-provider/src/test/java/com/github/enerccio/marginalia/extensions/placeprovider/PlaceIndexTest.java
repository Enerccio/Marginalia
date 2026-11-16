package com.github.enerccio.marginalia.extensions.placeprovider;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The questions the model asks of the places: search by name, type and country, the places around a point and the distance of
 * two places, on a small hand-made index (and a few checks of the bundled one).
 */
class PlaceIndexTest {

    private static final String DATA = """
            {
              "countries": [
                {"name": "France", "iso2": "FR", "iso3": "FRA", "alt": ["French Republic"]},
                {"name": "Canada", "iso2": "CA", "iso3": "CAN"},
                {"name": "United States of America", "iso2": "US", "iso3": "USA", "alt": ["United States"]},
                {"name": "Czechia", "iso2": "CZ", "iso3": "CZE", "alt": ["Czech Republic"]},
                {"name": "Guinea", "iso2": "GN", "iso3": "GIN"},
                {"name": "Guinea-Bissau", "iso2": "GW", "iso3": "GNB"},
                {"name": "Equatorial Guinea", "iso2": "GQ", "iso3": "GNQ"}
              ],
              "places": [
                {"name": "Springfield", "type": "city", "lat": 39.80, "lon": -89.64, "countries": ["United States of America"],
                 "within": "Illinois", "pop": 100000},
                {"name": "Springfield", "type": "city", "lat": 37.21, "lon": -93.29, "countries": ["United States of America"],
                 "within": "Missouri", "pop": 150000},
                {"name": "North Springfield", "type": "city", "lat": 38.80, "lon": -77.20, "countries": ["United States of America"],
                 "within": "Virginia", "pop": 5000},
                {"name": "Paris", "type": "capital", "lat": 48.869, "lon": 2.331, "countries": ["France"], "within": "Île-de-France",
                 "pop": 9904000},
                {"name": "Lyon", "type": "city", "lat": 45.764, "lon": 4.835, "countries": ["France"], "within": "Rhône-Alpes",
                 "pop": 1500000},
                {"name": "Mont Blanc", "type": "mountain", "lat": 45.833, "lon": 6.865, "countries": ["France"], "elevation": 4808},
                {"name": "Ottawa", "type": "capital", "lat": 45.419, "lon": -75.702, "countries": ["Canada"], "within": "Ontario",
                 "pop": 1145000},
                {"name": "Montréal", "type": "city", "lat": 45.50, "lon": -73.57, "countries": ["Canada"], "within": "Québec",
                 "alt": ["Montreal-Laval"], "pop": 3500000},
                {"name": "Hamilton Inlet", "type": "bay", "lat": 54.2, "lon": -58.0, "countries": ["Canada"], "rank": 5},
                {"name": "Hudson Bay", "type": "bay", "lat": 60.0, "lon": -85.0, "bbox": [-95.0, 51.0, -77.0, 64.0],
                 "countries": ["Canada"], "rank": 1},
                {"name": "Cook Inlet", "type": "inlet", "lat": 60.0, "lon": -152.0, "countries": ["United States of America"], "rank": 4}
              ]
            }
            """;

    private static PlaceIndex index() {
        return PlaceIndex.parse(new StringReader(DATA));
    }

    private static List<String> names(List<Place> places) {
        return places.stream().map(p -> p.name() + (p.within() == null ? "" : " / " + p.within())).toList();
    }

    @Test
    void namesAreComparedWithoutCaseAccentsAndPunctuation() {
        assertEquals("montreal nord", PlaceIndex.norm("  Montréal-Nord! "));
        assertEquals("sao tome", PlaceIndex.norm("São Tomé"));
        assertEquals("", PlaceIndex.norm(null));
    }

    @Test
    void parsedPlacesKeepTheirFieldsAndEmptyListsBecomeNull() {
        PlaceIndex index = index();
        assertEquals(11, index.size());
        Place bay = index.resolve("Hudson Bay");
        assertArrayEquals(new double[]{-95.0, 51.0, -77.0, 64.0}, bay.bbox());
        assertNull(bay.alt());
        assertNull(bay.pop());
        assertTrue(bay.isIn("Canada"));
        assertFalse(bay.isIn("France"));
        assertEquals(4808, index.resolve("Mont Blanc").elevation());
    }

    @Test
    void typesAreListedByNameWithTheirCounts() {
        assertEquals(List.of(new TypeCount("bay", 2), new TypeCount("capital", 2), new TypeCount("city", 5),
                new TypeCount("inlet", 1), new TypeCount("mountain", 1)), index().types());
    }

    @Test
    void exactNamesComeFirstThenTheBiggerPlaces() {
        PlaceSearch result = index().search("springfield", null, null, null);
        assertEquals(3, result.total());
        assertEquals(List.of("Springfield / Missouri", "Springfield / Illinois", "North Springfield / Virginia"), names(result.places()));
    }

    @Test
    void theLimitCutsTheAnswerButNotTheTotal() {
        PlaceSearch result = index().search("springfield", null, null, 1);
        assertEquals(3, result.total());
        assertEquals(1, result.places().size());
        assertEquals(PlaceIndex.DEFAULT_LIMIT, PlaceIndex.clampLimit(null));
        assertEquals(1, PlaceIndex.clampLimit(0));
        assertEquals(PlaceIndex.MAX_LIMIT, PlaceIndex.clampLimit(10_000));
    }

    @Test
    void accentsAndOtherNamesAreFound() {
        assertEquals(List.of("Montréal / Québec"), names(index().search("MONTREAL", null, null, null).places()));
        assertEquals(List.of("Montréal / Québec"), names(index().search("montreal laval", null, null, null).places()));
        // every word of the query somewhere in the name
        assertEquals(List.of("Hudson Bay"), names(index().search("bay hud", null, null, null).places()));
        assertEquals(0, index().search("atlantis", null, null, null).total());
    }

    @Test
    void typesAcceptPluralsAndWhatPeopleCallThem() {
        PlaceIndex index = index();
        assertEquals(List.of("Paris / Île-de-France", "Lyon / Rhône-Alpes"), names(index.search("*", "towns", "France", null).places()));
        assertEquals(List.of("Paris / Île-de-France"), names(index.search(null, "capital", "France", null).places()));
        assertEquals(List.of("Mont Blanc"), names(index.search("", "peaks", null, null).places()));
        assertEquals(List.of("Mont Blanc"), names(index.search("", "summit", null, null).places()));
    }

    @Test
    void aWaterTypeAlsoFindsOtherWatersWithTheWordInTheName() {
        // Natural Earth draws Hamilton Inlet as a bay
        assertEquals(List.of("Cook Inlet", "Hamilton Inlet"), names(index().search(null, "inlet", null, null).places()));
        assertEquals(List.of("Hudson Bay", "Hamilton Inlet"), names(index().search(null, "bays", null, null).places()));
    }

    @Test
    void unknownTypeNamesTheTypes() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> index().search(null, "volcano", null, null));
        assertTrue(e.getMessage().contains("volcano"), e.getMessage());
        assertTrue(e.getMessage().contains("bay, capital, city, inlet, mountain"), e.getMessage());
    }

    @Test
    void searchNeedsAtLeastOneCondition() {
        assertThrows(IllegalArgumentException.class, () -> index().search(null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> index().search(" * ", " ", "", null));
    }

    @Test
    void countriesAreFoundByNameCodeOtherNameOrAUniquePart() {
        PlaceIndex index = index();
        assertEquals("France", index.resolveCountry("france"));
        assertEquals("France", index.resolveCountry("FR"));
        assertEquals("France", index.resolveCountry("fra"));
        assertEquals("France", index.resolveCountry("French Republic"));
        assertEquals("Czechia", index.resolveCountry("Czech Republic"));
        assertEquals("Czechia", index.resolveCountry("czech"));
        assertEquals("United States of America", index.resolveCountry("United States"));
        // a name wins over a part of other names
        assertEquals("Guinea", index.resolveCountry("Guinea"));
    }

    @Test
    void anAmbiguousOrUnknownCountryIsRefused() {
        PlaceIndex index = index();
        IllegalArgumentException ambiguous = assertThrows(IllegalArgumentException.class, () -> index.resolveCountry("guin"));
        assertTrue(ambiguous.getMessage().contains("Equatorial Guinea, Guinea, Guinea-Bissau"), ambiguous.getMessage());
        assertThrows(IllegalArgumentException.class, () -> index.resolveCountry("Narnia"));
        assertThrows(IllegalArgumentException.class, () -> index.search("paris", null, "Narnia", null));
    }

    @Test
    void theCountryFiltersTheSearch() {
        assertEquals(List.of("Montréal / Québec"), names(index().search("", "city", "CAN", null).places()));
        assertEquals(List.of("Montréal / Québec", "Ottawa / Ontario"), names(index().search("", "town", "Canada", null).places()));
        assertEquals(0, index().search("paris", null, "Canada", null).total());
    }

    @Test
    void aNameIsResolvedToItsBestMatchAndAQualifierPicksAmongEqualNames() {
        PlaceIndex index = index();
        assertEquals("Missouri", index.resolve("Springfield").within());
        assertEquals("Illinois", index.resolve("Springfield, Illinois").within());
        assertEquals("Paris", index.resolve("Paris, FR").name());
        assertThrows(IllegalArgumentException.class, () -> index.resolve("Paris, Canada"));
        assertThrows(IllegalArgumentException.class, () -> index.resolve("Atlantis"));
        assertThrows(IllegalArgumentException.class, () -> index.resolve(" "));
    }

    @Test
    void coordinatesAreAPoint() {
        Place point = index().resolve(" 45.42 ; -75.69 ");
        assertEquals("point", point.type());
        assertEquals(45.42, point.lat());
        assertEquals(-75.69, point.lon());
        assertThrows(IllegalArgumentException.class, () -> index().resolve("91, 0"));
        assertThrows(IllegalArgumentException.class, () -> index().resolve("0, 181"));
    }

    @Test
    void greatCircleDistanceAndCompassDirection() {
        assertEquals(0, PlaceIndex.km(10, 20, 10, 20), 1e-9);
        // a quarter of the equator
        assertEquals(Math.PI / 2 * PlaceIndex.EARTH_KM, PlaceIndex.km(0, 0, 0, 90), 1e-6);
        assertEquals(PlaceIndex.km(48.0, 2.0, 45.0, 4.0), PlaceIndex.km(45.0, 4.0, 48.0, 2.0), 1e-9);
        assertEquals("N", PlaceIndex.direction(0, 0, 1, 0));
        assertEquals("E", PlaceIndex.direction(0, 0, 0, 1));
        assertEquals("SW", PlaceIndex.direction(0, 0, -1, -1));
        assertEquals("W", PlaceIndex.direction(0, 10, 0, -10));
    }

    @Test
    void distanceOfTwoNamedPlaces() {
        Distance d = index().distance("Paris", "Lyon");
        assertEquals("Paris", d.from().name());
        assertEquals("Lyon", d.to().name());
        assertTrue(d.km() > 385 && d.km() < 400, "Paris - Lyon " + d.km());
        assertEquals(Math.round(d.km() * PlaceIndex.MILES_PER_KM * 10) / 10.0, d.miles(), 0.11);
        assertEquals("SE", d.direction());
    }

    @Test
    void placesAroundAPointAreTheClosestFirstWithoutTheOrigin() {
        PlaceIndex index = index();
        Nearby around = index.near("Ottawa", 200, null, null);
        assertEquals("Ottawa", around.origin().name());
        assertEquals(1, around.total());
        NearbyPlace montreal = around.places().getFirst();
        assertEquals("Montréal", montreal.place().name());
        assertTrue(montreal.km() > 150 && montreal.km() < 180, "Ottawa - Montréal " + montreal.km());
        assertEquals("E", montreal.direction());

        Nearby lyon = index.near("45.764, 4.835", 500, null, null);
        assertEquals(List.of("Lyon", "Mont Blanc", "Paris"), lyon.places().stream().map(n -> n.place().name()).toList(),
                "coordinates are no place of the index, so the place there is not left out");
        assertEquals(List.of("Mont Blanc"), index.near("Lyon", 500, "mountains", null).places().stream().map(n -> n.place().name()).toList());
        assertEquals(1, index.near("Lyon", 500, null, 1).places().size());
        assertThrows(IllegalArgumentException.class, () -> index.near("Lyon", 0, null, null));
        assertThrows(IllegalArgumentException.class, () -> index.near("Lyon", Double.NaN, null, null));
    }

    @Test
    void theBundledPlacesAreLoadedAndAnswer() {
        PlaceIndex index = PlaceIndex.load();
        assertTrue(index.size() > 10_000, "places: " + index.size());
        // the capital, not the river or the port of the same name
        assertEquals("capital", index.resolve("Ottawa").type());
        assertEquals("Canada", index.resolveCountry("CAN"));
        Distance d = index.distance("Paris, France", "Ottawa");
        assertTrue(d.km() > 5500 && d.km() < 5800, "Paris - Ottawa " + d.km());
    }
}
