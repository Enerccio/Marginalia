package com.github.enerccio.marginalia.extensions.placeprovider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The places read from {@code places.json} (written by the build step {@code src/build/BuildPlaces.java} from Natural
 * Earth) and the questions asked of them: search by name, type and country, the places around a point, the distance of two
 * places. Names are compared without case, accents and punctuation.
 */
public class PlaceIndex {

    static final String RESOURCE = "places.json";

    /** The most places one answer holds, whatever the model asks for. */
    public static final int MAX_LIMIT = 100;
    public static final int DEFAULT_LIMIT = 25;

    static final double EARTH_KM = 6371.0088;
    static final double MILES_PER_KM = 0.621371;
    private static final String[] COMPASS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final Pattern COORDINATES = Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*[,;\\s]\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    /** The water types: a bay, an inlet and a fjord are drawn as one or the other, so the name decides too. */
    private static final Set<String> WATER = Set.of("sea", "gulf", "bay", "strait", "sound", "channel", "lagoon", "fjord",
            "ocean", "inlet", "reef", "lake", "reservoir", "river");
    private static final Set<String> MOUNTAINS = Set.of("mountain", "peak", "mountain range");

    /** What people call a type, to the types of the data. */
    private static final Map<String, Set<String>> ALIASES = Map.ofEntries(
            Map.entry("mountain", MOUNTAINS), Map.entry("mount", MOUNTAINS), Map.entry("peak", MOUNTAINS),
            Map.entry("summit", MOUNTAINS), Map.entry("range", Set.of("mountain range")),
            Map.entry("town", Set.of("city", "capital", "provincial capital")), Map.entry("village", Set.of("city")),
            Map.entry("settlement", Set.of("city", "capital", "provincial capital")),
            Map.entry("place", Set.of("city", "capital", "provincial capital")),
            Map.entry("capital", Set.of("capital", "provincial capital")),
            Map.entry("state", Set.of("province")), Map.entry("county", Set.of("province")),
            Map.entry("isle", Set.of("island", "island group")), Map.entry("island", Set.of("island", "island group")),
            Map.entry("harbor", Set.of("port")), Map.entry("harbour", Set.of("port")),
            Map.entry("airfield", Set.of("airport")), Map.entry("lake", Set.of("lake", "reservoir")),
            Map.entry("water", WATER), Map.entry("waterbody", WATER));

    /** A place with what the search needs: the compared forms of the names and the order among equals. */
    record Entry(Place place, String name, List<String> alt, Set<String> words, double importance) {}

    /** A type of the question: the types it is, and the types whose name may carry the type as a word instead. */
    record TypeFilter(Set<String> types, Set<String> nameTypes, String word) {

        boolean matches(Entry e) {
            return types.contains(e.place.type()) || word != null && nameTypes.contains(e.place.type()) && e.words.contains(word);
        }
    }

    private final List<Entry> entries;
    private final Map<String, Integer> typeCounts;
    /** Every spelling of a country (compared form), its code included, to its name. */
    private final Map<String, String> countryNames;

    /** A place as read, with the scale rank of Natural Earth that is not part of the answer. */
    record Loaded(Place place, Integer rank) {}

    record CountryRow(String name, String iso2, String iso3, List<String> alt) {}

    PlaceIndex(List<Loaded> places, List<CountryRow> countries) {
        List<Entry> list = new ArrayList<>(places.size());
        Map<String, Integer> counts = new TreeMap<>();
        for (Loaded loaded : places) {
            Place p = loaded.place;
            Set<String> words = new HashSet<>(Arrays.asList(norm(p.name()).split(" ")));
            List<String> alt = new ArrayList<>();
            for (String a : p.alt() == null ? List.<String>of() : p.alt()) {
                alt.add(norm(a));
                words.addAll(Arrays.asList(norm(a).split(" ")));
            }
            list.add(new Entry(p, norm(p.name()), alt, words, importance(p, loaded.rank)));
            counts.merge(p.type(), 1, Integer::sum);
        }
        this.entries = List.copyOf(list);
        this.typeCounts = counts;
        Map<String, String> names = new LinkedHashMap<>();
        // the names first, so that no code or alternative name takes the place of another country's name
        for (CountryRow c : countries) {
            names.put(norm(c.name), c.name);
        }
        for (CountryRow c : countries) {
            for (String key : new String[]{c.iso2, c.iso3}) {
                if (key != null) {
                    names.putIfAbsent(norm(key), c.name);
                }
            }
            for (String a : c.alt) {
                names.putIfAbsent(norm(a), c.name);
            }
        }
        this.countryNames = names;
    }

    /** Reads the places from the extension's own resource; none when there are none. */
    public static PlaceIndex load() {
        try (InputStream in = PlaceIndex.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return new PlaceIndex(List.of(), List.of());
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return parse(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    static PlaceIndex parse(Reader reader) {
        JsonObject root = new Gson().fromJson(reader, JsonObject.class);
        List<CountryRow> countries = new ArrayList<>();
        root.getAsJsonArray("countries").forEach(element -> {
            JsonObject c = element.getAsJsonObject();
            countries.add(new CountryRow(c.get("name").getAsString(), c.has("iso2") ? c.get("iso2").getAsString() : null,
                    c.has("iso3") ? c.get("iso3").getAsString() : null, c.has("alt") ? strings(c.getAsJsonArray("alt")) : List.of()));
        });
        List<Loaded> places = new ArrayList<>();
        root.getAsJsonArray("places").forEach(element -> {
            JsonObject p = element.getAsJsonObject();
            places.add(new Loaded(new Place(p.get("name").getAsString(), p.get("type").getAsString(), p.get("lat").getAsDouble(),
                    p.get("lon").getAsDouble(), p.has("bbox") ? doubles(p.getAsJsonArray("bbox")) : null,
                    p.has("countries") ? strings(p.getAsJsonArray("countries")) : List.of(),
                    p.has("alt") ? strings(p.getAsJsonArray("alt")) : List.of(),
                    p.has("within") ? p.get("within").getAsString() : null,
                    p.has("pop") ? p.get("pop").getAsLong() : null,
                    p.has("elevation") ? p.get("elevation").getAsInt() : null,
                    p.has("code") ? p.get("code").getAsString() : null),
                    p.has("rank") ? p.get("rank").getAsInt() : null));
        });
        return new PlaceIndex(places, countries);
    }

    private static List<String> strings(JsonArray array) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : array) {
            out.add(e.getAsString());
        }
        return List.copyOf(out);
    }

    private static double[] doubles(JsonArray array) {
        double[] out = new double[array.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = array.get(i).getAsDouble();
        }
        return out;
    }

    /** How important a place is, to order what fits equally: big places and the ones Natural Earth draws at a small scale first. */
    private static double importance(Place p, Integer rank) {
        if (p.pop() != null && p.pop() > 0) {
            return Math.log10(p.pop());
        }
        if (p.elevation() != null) {
            return p.elevation() / 1000.0;
        }
        return rank == null ? 0 : (10 - rank) / 2.0;
    }

    /** The compared form: lower case, no accents, letters and digits separated by single spaces. */
    static String norm(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").strip();
    }

    public int size() {
        return entries.size();
    }

    /** The types of the places with how many there are of each, sorted by the name of the type. */
    public List<TypeCount> types() {
        return typeCounts.entrySet().stream().map(e -> new TypeCount(e.getKey(), e.getValue())).toList();
    }

    // --- type and country ---

    /**
     * What a type of the question stands for. A type of the data, or its plural, or what people call it ({@code mountains},
     * {@code town}, {@code state}). For the water types (an inlet, a bay) the places of the other water types that have the
     * word in their name count too, since Natural Earth draws Hamilton Inlet as a bay.
     *
     * @throws IllegalArgumentException when it is no type
     */
    TypeFilter resolveType(String type) {
        String key = norm(type);
        for (String candidate : new String[]{key, singular(key)}) {
            Set<String> types = new TreeSet<>();
            if (typeCounts.containsKey(candidate)) {
                types.add(candidate);
            }
            types.addAll(ALIASES.getOrDefault(candidate, Set.of()));
            types.retainAll(typeCounts.keySet());
            if (!types.isEmpty() || typeCounts.containsKey(candidate)) {
                boolean water = WATER.contains(candidate);
                return new TypeFilter(types, water ? WATER : Set.of(), water ? candidate : null);
            }
        }
        throw new IllegalArgumentException("Unknown type '" + type + "'. Types: " + String.join(", ", typeCounts.keySet()));
    }

    private static String singular(String word) {
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
        }
        return word.endsWith("s") && !word.endsWith("ss") ? word.substring(0, word.length() - 1) : word;
    }

    /**
     * The name of the country a name, an alternative name or a code stands for ({@code Canada}, {@code CA}, {@code CAN},
     * {@code Czech Republic}), or a country whose name contains the text when only one does.
     *
     * @throws IllegalArgumentException when there is none or there are several
     */
    String resolveCountry(String country) {
        String key = norm(country);
        String exact = countryNames.get(key);
        if (exact != null) {
            return exact;
        }
        List<String> like = key.isEmpty() ? List.of() : countryNames.entrySet().stream()
                .filter(e -> e.getKey().contains(key)).map(Map.Entry::getValue).distinct().sorted().toList();
        if (like.size() == 1) {
            return like.get(0);
        }
        throw new IllegalArgumentException(like.isEmpty()
                ? "Unknown country '" + country + "'. Search type country for the names."
                : "Which country is '" + country + "'? " + String.join(", ", like.subList(0, Math.min(10, like.size()))));
    }

    // --- search ---

    /**
     * The places that fit the name, the type and the country (all that are given; at least one has to be), the best match
     * first: the name equal to the query, then starting with it, then having it as a word, then containing it; among equals the
     * bigger and more important ones. A blank query or {@code *} is no condition on the name.
     *
     * @throws IllegalArgumentException when nothing is given, or the type or the country is unknown
     */
    public PlaceSearch search(String query, String type, String country, Integer limit) {
        String q = "*".equals(query == null ? null : query.strip()) ? "" : norm(query);
        boolean hasType = type != null && !type.isBlank();
        boolean hasCountry = country != null && !country.isBlank();
        if (q.isEmpty() && !hasType && !hasCountry) {
            throw new IllegalArgumentException("Give a query (part of a name), a type or a country");
        }
        TypeFilter typeFilter = hasType ? resolveType(type) : null;
        String countryName = hasCountry ? resolveCountry(country) : null;
        record Hit(Entry entry, int score) {}
        List<Hit> hits = new ArrayList<>();
        for (Entry e : entries) {
            if (typeFilter != null && !typeFilter.matches(e) || countryName != null && !e.place.isIn(countryName)) {
                continue;
            }
            int score = q.isEmpty() ? 1 : score(e, q);
            if (score > 0) {
                hits.add(new Hit(e, score));
            }
        }
        hits.sort(Comparator.comparingInt(Hit::score).reversed()
                .thenComparing(Comparator.comparingDouble((Hit h) -> h.entry.importance).reversed())
                .thenComparing(h -> h.entry.name));
        int max = clampLimit(limit);
        return new PlaceSearch(hits.size(), hits.stream().limit(max).map(h -> h.entry.place).toList());
    }

    /** How well the names of a place fit the query, 0 for not at all. */
    private static int score(Entry e, String q) {
        int best = fieldScore(e.name, q);
        for (String alt : e.alt) {
            best = Math.max(best, fieldScore(alt, q) - 5);
        }
        return Math.max(best, 0);
    }

    private static int fieldScore(String field, String q) {
        if (field.equals(q)) {
            return 100;
        }
        if (field.startsWith(q)) {
            return 80;
        }
        if ((" " + field).contains(" " + q)) {
            return 60;
        }
        if (field.contains(q)) {
            return 40;
        }
        for (String token : q.split(" ")) {
            if (!field.contains(token)) {
                return 0;
            }
        }
        return 30;
    }

    static int clampLimit(Integer limit) {
        return limit == null ? DEFAULT_LIMIT : Math.clamp(limit, 1, MAX_LIMIT);
    }

    // --- around a point, distance ---

    /**
     * The places within the radius of a place or of coordinates, the closest first. The origin itself is left out.
     *
     * @param place    a name ({@code Ottawa}, {@code Paris, France}) or coordinates ({@code 45.42, -75.69})
     * @param radiusKm the radius in kilometers, more than 0
     * @param type     only places of this type, or null
     * @throws IllegalArgumentException when the place or the type is unknown or the radius is not positive
     */
    public Nearby near(String place, double radiusKm, String type, Integer limit) {
        if (!(radiusKm > 0)) {
            throw new IllegalArgumentException("The radius has to be more than 0 km");
        }
        Place origin = resolve(place);
        TypeFilter typeFilter = type == null || type.isBlank() ? null : resolveType(type);
        List<NearbyPlace> found = new ArrayList<>();
        for (Entry e : entries) {
            if (e.place == origin || typeFilter != null && !typeFilter.matches(e)) {
                continue;
            }
            double km = km(origin.lat(), origin.lon(), e.place.lat(), e.place.lon());
            if (km <= radiusKm) {
                found.add(new NearbyPlace(e.place, Math.round(km * 10) / 10.0, direction(origin.lat(), origin.lon(), e.place.lat(), e.place.lon())));
            }
        }
        found.sort(Comparator.comparingDouble(NearbyPlace::km));
        return new Nearby(origin, found.size(), found.stream().limit(clampLimit(limit)).toList());
    }

    /** The great-circle distance of two places or points. */
    public Distance distance(String from, String to) {
        Place a = resolve(from);
        Place b = resolve(to);
        double km = km(a.lat(), a.lon(), b.lat(), b.lon());
        return new Distance(a, b, Math.round(km * 10) / 10.0, Math.round(km * MILES_PER_KM * 10) / 10.0,
                direction(a.lat(), a.lon(), b.lat(), b.lon()));
    }

    /**
     * The place a text names: coordinates {@code lat, lon}, or a name, which may be followed by the country or the province
     * after a comma ({@code Paris, France}). Of several places of the name the best match, then the biggest, is taken; the
     * answer says which it was.
     *
     * @throws IllegalArgumentException when there is no such place or the coordinates are out of range
     */
    Place resolve(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("A place is missing");
        }
        Matcher m = COORDINATES.matcher(text);
        if (m.matches()) {
            double lat = Double.parseDouble(m.group(1));
            double lon = Double.parseDouble(m.group(2));
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new IllegalArgumentException("Coordinates out of range (latitude -90..90, longitude -180..180): " + text);
            }
            return new Place(lat + ", " + lon, "point", lat, lon, null, List.of(), List.of(), null, null, null, null);
        }
        String name = text;
        String qualifier = null;
        int comma = text.indexOf(',');
        if (comma > 0) {
            name = text.substring(0, comma);
            qualifier = norm(text.substring(comma + 1));
        }
        String q = norm(name);
        String country = qualifier == null ? null : countryNames.get(qualifier);
        Entry best = null;
        int bestScore = 0;
        for (Entry e : entries) {
            if (country != null && !e.place.isIn(country)
                    || country == null && qualifier != null && !qualifier.equals(norm(e.place.within()))) {
                continue;
            }
            int score = score(e, q);
            if (score > bestScore || score == bestScore && best != null && score > 0 && e.importance > best.importance) {
                best = e;
                bestScore = score;
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("Cannot find a place '" + text + "'. Use search_places to find the name, or give coordinates as 'lat, lon'.");
        }
        return best.place;
    }

    static double km(double lat1, double lon1, double lat2, double lon2) {
        double p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2);
        double dp = p2 - p1, dl = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * EARTH_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    /** The compass point (of eight) of the initial bearing from the first point to the second. */
    static String direction(double lat1, double lon1, double lat2, double lon2) {
        double p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2), dl = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dl) * Math.cos(p2);
        double x = Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl);
        double bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        return COMPASS[(int) Math.round(bearing / 45) % 8];
    }

}
