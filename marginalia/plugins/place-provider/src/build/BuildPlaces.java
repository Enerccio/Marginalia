import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Build step of the place provider (run by exec-maven-plugin with the profile {@code import-places}, see the pom):
 * reduces the 1:10m <a href="https://github.com/martynafford/natural-earth-geojson">Natural Earth GeoJSON</a> layers that
 * {@code FetchNaturalEarth.java} downloaded to the places the extension ships as {@code places.json}. Run as
 * {@code java BuildPlaces.java <download folder> <output file>}.
 * <p>
 * Every named feature becomes one record without its geometry: the name, the {@code type} (a small vocabulary of
 * lower case words: {@code bay}, {@code inlet}, {@code city}, {@code river}, {@code mountain range}...), the label point
 * {@code lat}/{@code lon}, for a line or an area its {@code bbox} {@code [west, south, east, north]}, and the
 * {@code countries} it is in or touches. Optional: {@code alt} (other names), {@code within} (the province of a place, the
 * continent of a physical feature), {@code pop}, {@code elevation}, {@code rank} (the Natural Earth scale rank, 0 is the
 * most important), {@code code} (IATA of an airport, ISO of a country).
 * <p>
 * Natural Earth does not say where a feature is, so the countries are found here from the polygons of
 * {@code admin_0_countries}: a point is in the country that contains it (else in the nearest within
 * {@value #POINT_NEAR} degrees, the coastline is generalized), a line or an area takes the countries of a sample of its
 * vertices and its label point, which is how a bay, whose label point is in the water, is found to be Canadian. The
 * countries of a feature are sorted by the number of samples, at most {@value #MAX_COUNTRIES}. Features of the same name
 * and type whose boxes touch (the pieces of a river) are one record.
 * <p>
 * Without the downloaded files the output is left as it is, so a clone of the repository builds without them.
 */
public class BuildPlaces {

    /** Degrees: how far from a country polygon a vertex of a water or line feature still counts as in it. */
    static final double NEAR = 0.3;
    /** Degrees: the same for a point (a port is on the coast that the polygon generalized). */
    static final double POINT_NEAR = 0.5;
    static final int MAX_SAMPLES = 24;
    static final int MAX_COUNTRIES = 12;
    /** Degrees: pieces of the same name closer than this are one feature. */
    static final double MERGE_GAP = 1.0;

    static final String COUNTRIES = "ne_10m_admin_0_countries.json";
    static final String ADMIN1 = "ne_10m_admin_1_label_points.json";
    static final String PLACES = "ne_10m_populated_places_simple.json";
    static final String PORTS = "ne_10m_ports.json";
    static final String AIRPORTS = "ne_10m_airports.json";
    static final String PARKS = "ne_10m_parks_and_protected_lands_area.json";
    static final String MARINE = "ne_10m_geography_marine_polys.json";
    static final String REGIONS = "ne_10m_geography_regions_polys.json";
    static final String REGION_POINTS = "ne_10m_geography_regions_points.json";
    static final String ELEVATION = "ne_10m_geography_regions_elevation_points.json";
    static final String LAKES = "ne_10m_lakes.json";
    static final String RIVERS = "ne_10m_rivers_lake_centerlines.json";

    static final List<String> ALL = List.of(COUNTRIES, ADMIN1, PLACES, PORTS, AIRPORTS, PARKS, MARINE, REGIONS,
            REGION_POINTS, ELEVATION, LAKES, RIVERS);

    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    // --- geometry ---

    record Ring(double[] x, double[] y, double minX, double minY, double maxX, double maxY) {

        boolean near(double px, double py, double tol) {
            return px >= minX - tol && px <= maxX + tol && py >= minY - tol && py <= maxY + tol;
        }

        boolean contains(double px, double py) {
            boolean in = false;
            for (int i = 0, j = x.length - 1; i < x.length; j = i++) {
                if ((y[i] > py) != (y[j] > py) && px < (x[j] - x[i]) * (py - y[i]) / (y[j] - y[i]) + x[i]) {
                    in = !in;
                }
            }
            return in;
        }

        /** Distance to the border in degrees, longitudes scaled by the cosine of the latitude. */
        double distance(double px, double py, double limit) {
            double scale = Math.cos(Math.toRadians(py));
            double best = limit;
            for (int i = 0, j = x.length - 1; i < x.length; j = i++) {
                double ax = x[j], ay = y[j], bx = x[i], by = y[i];
                if (Math.max(ax, bx) < px - best || Math.min(ax, bx) > px + best
                        || Math.max(ay, by) < py - best || Math.min(ay, by) > py + best) {
                    continue;
                }
                double dx = (bx - ax) * scale, dy = by - ay;
                double ex = (px - ax) * scale, ey = py - ay;
                double len = dx * dx + dy * dy;
                double t = len == 0 ? 0 : Math.clamp((ex * dx + ey * dy) / len, 0, 1);
                double d = Math.hypot(ex - t * dx, ey - t * dy);
                if (d < best) {
                    best = d;
                }
            }
            return best;
        }

        double area() {
            double sum = 0;
            for (int i = 0, j = x.length - 1; i < x.length; j = i++) {
                sum += x[j] * y[i] - x[i] * y[j];
            }
            return sum / 2;
        }
    }

    record Poly(Ring outer, List<Ring> holes) {

        boolean contains(double px, double py) {
            if (!outer.near(px, py, 0) || !outer.contains(px, py)) {
                return false;
            }
            for (Ring hole : holes) {
                if (hole.near(px, py, 0) && hole.contains(px, py)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class Country {
        String name, iso2, iso3, continent;
        long pop;
        List<Poly> polys = new ArrayList<>();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        List<String> alt = new ArrayList<>();
    }

    static final List<Country> countries = new ArrayList<>();
    /** Every spelling of a country (lower case) to its name, for the countries that Natural Earth gives as text. */
    static final Map<String, String> aliases = new java.util.HashMap<>();

    static String canonical(String country) {
        return country == null ? null : aliases.getOrDefault(country.toLowerCase(Locale.ROOT), country);
    }

    static Ring ring(JsonArray array) {
        int n = array.size();
        double[] xs = new double[n], ys = new double[n];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            JsonArray p = array.get(i).getAsJsonArray();
            xs[i] = p.get(0).getAsDouble();
            ys[i] = p.get(1).getAsDouble();
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        return new Ring(xs, ys, minX, minY, maxX, maxY);
    }

    static Poly poly(JsonArray rings) {
        List<Ring> holes = new ArrayList<>();
        for (int i = 1; i < rings.size(); i++) {
            holes.add(ring(rings.get(i).getAsJsonArray()));
        }
        return new Poly(ring(rings.get(0).getAsJsonArray()), holes);
    }

    static List<Poly> polys(JsonObject geometry) {
        List<Poly> out = new ArrayList<>();
        JsonArray c = geometry.getAsJsonArray("coordinates");
        switch (geometry.get("type").getAsString()) {
            case "Polygon" -> out.add(poly(c));
            case "MultiPolygon" -> c.forEach(p -> out.add(poly(p.getAsJsonArray())));
            default -> { }
        }
        return out;
    }

    static List<double[]> line(JsonArray array) {
        List<double[]> out = new ArrayList<>(array.size());
        for (JsonElement e : array) {
            JsonArray p = e.getAsJsonArray();
            out.add(new double[]{p.get(0).getAsDouble(), p.get(1).getAsDouble()});
        }
        return out;
    }

    /** The vertices of a geometry in pieces: a point, a line, the outer ring of a polygon. */
    static List<List<double[]>> parts(JsonObject geometry) {
        List<List<double[]>> out = new ArrayList<>();
        JsonArray c = geometry.getAsJsonArray("coordinates");
        switch (geometry.get("type").getAsString()) {
            case "Point" -> out.add(List.of(new double[]{c.get(0).getAsDouble(), c.get(1).getAsDouble()}));
            case "LineString" -> out.add(line(c));
            case "MultiLineString" -> c.forEach(l -> out.add(line(l.getAsJsonArray())));
            case "Polygon" -> out.add(line(c.get(0).getAsJsonArray()));
            case "MultiPolygon" -> c.forEach(p -> out.add(line(p.getAsJsonArray().get(0).getAsJsonArray())));
            default -> { }
        }
        return out;
    }

    // --- the country of a place ---

    static String containing(double x, double y) {
        for (Country country : countries) {
            if (x < country.minX || x > country.maxX || y < country.minY || y > country.maxY) {
                continue;
            }
            for (Poly poly : country.polys) {
                if (poly.contains(x, y)) {
                    return country.name;
                }
            }
        }
        return null;
    }

    static String nearest(double x, double y, double tol) {
        String best = null;
        double bestDistance = tol;
        for (Country country : countries) {
            if (x < country.minX - tol || x > country.maxX + tol || y < country.minY - tol || y > country.maxY + tol) {
                continue;
            }
            for (Poly poly : country.polys) {
                if (poly.outer.near(x, y, tol)) {
                    double d = poly.outer.distance(x, y, bestDistance);
                    if (d < bestDistance) {
                        bestDistance = d;
                        best = country.name;
                    }
                }
            }
        }
        return best;
    }

    static String countryOf(double x, double y, double tol) {
        String in = containing(x, y);
        return in != null ? in : nearest(x, y, tol);
    }

    // --- records ---

    static class Rec {
        String name, type, within, code;
        List<String> alt = new ArrayList<>();
        double lat, lon;
        double[] bbox;
        Map<String, Integer> countries = new LinkedHashMap<>();
        Long pop;
        Integer elevation, rank;
        long weight;

        Map<String, Object> json() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("type", type);
            m.put("lat", round(lat, 3));
            m.put("lon", round(lon, 3));
            if (bbox != null) {
                m.put("bbox", new double[]{round(bbox[0], 2), round(bbox[1], 2), round(bbox[2], 2), round(bbox[3], 2)});
            }
            if (!countries.isEmpty()) {
                m.put("countries", countries.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                        .limit(MAX_COUNTRIES).map(Map.Entry::getKey).toList());
            }
            if (!alt.isEmpty()) {
                m.put("alt", alt);
            }
            if (within != null) {
                m.put("within", within);
            }
            if (pop != null && pop > 0) {
                m.put("pop", pop);
            }
            if (elevation != null) {
                m.put("elevation", elevation);
            }
            if (rank != null) {
                m.put("rank", rank);
            }
            if (code != null) {
                m.put("code", code);
            }
            return m;
        }
    }

    static double round(double value, int digits) {
        double f = Math.pow(10, digits);
        return Math.round(value * f) / f;
    }

    static final List<Rec> records = new ArrayList<>();
    static int unnamed;

    static String str(JsonObject props, String key) {
        JsonElement e = props.get(key);
        if (e == null || e.isJsonNull() || !e.isJsonPrimitive()) {
            return null;
        }
        String s = e.getAsString().replace(' ', ' ').strip();
        return s.isEmpty() || s.equals("-99") ? null : s;
    }

    static Double number(JsonObject props, String key) {
        String s = str(props, key);
        if (s == null) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The other names of a feature: the {@code ;} separated ones of the properties, without the main name. */
    static List<String> alt(String name, JsonObject props, String... keys) {
        Set<String> out = new LinkedHashSet<>();
        for (String key : keys) {
            String s = str(props, key);
            if (s != null) {
                for (String part : s.split(";")) {
                    String p = part.strip();
                    if (!p.isEmpty() && !p.equalsIgnoreCase(name)) {
                        out.add(p);
                    }
                }
            }
        }
        return new ArrayList<>(out);
    }

    static final Set<String> SMALL_WORDS = Set.of("of", "the", "and", "de", "del", "da", "do", "du", "la", "le", "des", "di", "y");

    /** Natural Earth writes some names of regions in capitals ({@code GREAT RIFT VALLEY}); a short one is an acronym. */
    static String fixCase(String name) {
        if (name == null || !name.equals(name.toUpperCase(Locale.ROOT)) || name.chars().filter(Character::isLetter).count() <= 3) {
            return name;
        }
        StringBuilder out = new StringBuilder();
        for (String word : name.split(" ", -1)) {
            String lower = word.toLowerCase(Locale.ROOT);
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(SMALL_WORDS.contains(lower) || lower.isEmpty() ? lower : lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1));
        }
        return out.toString();
    }

    /** A record of a feature with its geometry reduced: label point, box and countries. Null without a name or a geometry. */
    static Rec make(String name, String type, JsonObject feature, boolean findCountries) {
        if (name == null) {
            unnamed++;
            return null;
        }
        if (!feature.has("geometry") || feature.get("geometry").isJsonNull()) {
            return null;
        }
        JsonObject geometry = feature.getAsJsonObject("geometry");
        String geometryType = geometry.get("type").getAsString();
        List<List<double[]>> parts = parts(geometry);
        if (parts.isEmpty()) {
            return null;
        }
        Rec r = new Rec();
        r.name = fixCase(name);
        r.type = type;
        boolean point = geometryType.equals("Point");
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        long vertices = 0;
        for (List<double[]> part : parts) {
            for (double[] p : part) {
                minX = Math.min(minX, p[0]);
                maxX = Math.max(maxX, p[0]);
                minY = Math.min(minY, p[1]);
                maxY = Math.max(maxY, p[1]);
                vertices++;
            }
        }
        r.weight = vertices;
        if (!point) {
            r.bbox = new double[]{minX, minY, maxX, maxY};
        }
        double[] label = labelPoint(parts, geometryType.contains("Polygon"));
        r.lon = label[0];
        r.lat = label[1];
        if (findCountries) {
            if (point) {
                add(r, countryOf(label[0], label[1], POINT_NEAR));
            } else {
                add(r, countryOf(label[0], label[1], NEAR));
                long stride = Math.max(1, vertices / MAX_SAMPLES);
                long index = 0;
                for (List<double[]> part : parts) {
                    for (double[] p : part) {
                        if (index++ % stride == 0) {
                            add(r, countryOf(p[0], p[1], NEAR));
                        }
                    }
                }
            }
        }
        return r;
    }

    static void add(Rec r, String country) {
        if (country != null) {
            r.countries.merge(country, 1, Integer::sum);
        }
    }

    /** The centroid of the largest ring of an area, the middle vertex of the longest line (so it is on it), a point. */
    static double[] labelPoint(List<List<double[]>> parts, boolean area) {
        if (area) {
            double best = -1;
            double[] result = null;
            for (List<double[]> part : parts) {
                double sum = 0, cx = 0, cy = 0;
                for (int i = 0, j = part.size() - 1; i < part.size(); j = i++) {
                    double[] a = part.get(j), b = part.get(i);
                    double cross = a[0] * b[1] - b[0] * a[1];
                    sum += cross;
                    cx += (a[0] + b[0]) * cross;
                    cy += (a[1] + b[1]) * cross;
                }
                double size = Math.abs(sum / 2);
                if (size > best) {
                    best = size;
                    result = size < 1e-12 ? part.get(part.size() / 2) : new double[]{cx / (3 * sum), cy / (3 * sum)};
                }
            }
            return result;
        }
        List<double[]> longest = parts.stream().max(Comparator.comparingInt(List::size)).orElseThrow();
        return longest.get(longest.size() / 2);
    }

    static JsonObject read(Path folder, String file) throws IOException {
        try (Reader reader = Files.newBufferedReader(folder.resolve(file), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, JsonObject.class);
        }
    }

    static void layer(Path folder, String file, Function<JsonObject, Rec> each) throws IOException {
        int before = records.size();
        for (JsonElement e : read(folder, file).getAsJsonArray("features")) {
            Rec r = each.apply(e.getAsJsonObject());
            if (r != null) {
                records.add(r);
            }
        }
        System.out.println("place-provider: " + file + ": " + (records.size() - before) + " records");
    }

    static JsonObject props(JsonObject feature) {
        return feature.getAsJsonObject("properties");
    }

    // --- layers ---

    static void countries(Path folder) throws IOException {
        for (JsonElement e : read(folder, COUNTRIES).getAsJsonArray("features")) {
            JsonObject feature = e.getAsJsonObject();
            JsonObject p = props(feature);
            Country c = new Country();
            // NAME is abbreviated for some ("Dem. Rep. Congo", "S. Sudan"), NAME_LONG is how a book writes them
            c.name = str(p, "NAME_LONG") != null ? str(p, "NAME_LONG") : str(p, "NAME");
            c.iso2 = valid2(str(p, "ISO_A2")) != null ? str(p, "ISO_A2") : valid2(str(p, "WB_A2"));
            c.iso3 = str(p, "ISO_A3") != null ? str(p, "ISO_A3") : str(p, "ADM0_A3");
            c.continent = str(p, "CONTINENT");
            c.pop = number(p, "POP_EST") == null ? 0 : number(p, "POP_EST").longValue();
            c.alt = alt(c.name, p, "NAME", "ADMIN", "FORMAL_EN", "NAME_ALT");
            c.polys = polys(feature.getAsJsonObject("geometry"));
            for (Poly poly : c.polys) {
                c.minX = Math.min(c.minX, poly.outer.minX);
                c.minY = Math.min(c.minY, poly.outer.minY);
                c.maxX = Math.max(c.maxX, poly.outer.maxX);
                c.maxY = Math.max(c.maxY, poly.outer.maxY);
            }
            countries.add(c);
            aliases.put(c.name.toLowerCase(Locale.ROOT), c.name);
        }
        // the alternatives never take the name of another country
        for (Country c : countries) {
            c.alt.forEach(a -> aliases.putIfAbsent(a.toLowerCase(Locale.ROOT), c.name));
        }
        System.out.println("place-provider: " + COUNTRIES + ": " + countries.size() + " countries");
    }

    static String valid2(String s) {
        return s != null && s.length() == 2 ? s : null;
    }

    static void countryRecords() {
        for (Country c : countries) {
            Poly largest = c.polys.stream().max(Comparator.comparingDouble(p -> Math.abs(p.outer.area()))).orElse(null);
            if (largest == null || c.name == null) {
                continue;
            }
            Rec r = new Rec();
            r.name = c.name;
            r.type = "country";
            double[] label = labelPoint(List.of(line(largest.outer)), true);
            r.lon = label[0];
            r.lat = label[1];
            r.bbox = new double[]{c.minX, c.minY, c.maxX, c.maxY};
            r.countries.put(c.name, 1);
            r.alt = c.alt;
            r.within = c.continent;
            r.pop = c.pop;
            r.code = c.iso3;
            r.weight = Long.MAX_VALUE;
            records.add(r);
        }
    }

    static List<double[]> line(Ring ring) {
        List<double[]> out = new ArrayList<>(ring.x.length);
        for (int i = 0; i < ring.x.length; i++) {
            out.add(new double[]{ring.x[i], ring.y[i]});
        }
        return out;
    }

    static String placeType(String featureClass) {
        return switch (featureClass == null ? "" : featureClass) {
            case "Admin-0 capital", "Admin-0 capital alt", "Admin-0 region capital" -> "capital";
            case "Admin-1 capital", "Admin-1 region capital" -> "provincial capital";
            case "Scientific station", "Meteorological Station" -> "station";
            case "Historic place" -> "historic place";
            default -> "city";
        };
    }

    static String marineType(String featureClass) {
        return switch (featureClass == null ? "" : featureClass) {
            case "sea", "gulf", "bay", "strait", "sound", "channel", "lagoon", "fjord", "ocean", "inlet", "reef" -> featureClass;
            default -> null;
        };
    }

    static String regionType(String featureClass) {
        return switch (featureClass == null ? "" : featureClass) {
            case "Island" -> "island";
            case "Range/mtn" -> "mountain range";
            case "Island group" -> "island group";
            case "Pen/cape", "Peninsula" -> "peninsula";
            case "Geoarea" -> "region";
            case "Plateau", "Desert", "Coast", "Plain", "Delta", "Basin", "Continent", "Valley", "Lowland", "Tundra",
                 "Isthmus", "Wetlands", "Gorge", "Foothills", "Lake", "Depression" -> featureClass.toLowerCase(Locale.ROOT);
            default -> null;
        };
    }

    static void layers(Path folder) throws IOException {
        layer(folder, PLACES, f -> {
            JsonObject p = props(f);
            String name = str(p, "name");
            Rec r = make(name, placeType(str(p, "featurecla")), f, true);
            if (r == null) {
                return null;
            }
            if (r.countries.isEmpty() && str(p, "adm0name") != null) {
                r.countries.put(canonical(str(p, "adm0name")), 1);
            }
            r.alt = alt(name, p, "namealt", "nameascii");
            r.within = str(p, "adm1name");
            Double pop = number(p, "pop_max");
            r.pop = pop == null ? null : pop.longValue();
            r.weight = r.pop == null ? 0 : r.pop;
            return r;
        });
        Set<String> provinces = new HashSet<>();
        layer(folder, ADMIN1, f -> {
            JsonObject p = props(f);
            // the layer has a label point for every piece of a province
            if (str(p, "name") == null || str(p, "name").contains("-99")
                    || !provinces.add(str(p, "name") + "|" + str(p, "admin"))) {
                return null;
            }
            Rec r = make(str(p, "name"), "province", f, false);
            if (r != null) {
                String country = countryOf(r.lon, r.lat, POINT_NEAR);
                String admin = canonical(str(p, "admin"));
                r.countries.put(country != null ? country : admin == null ? "?" : admin, 1);
                r.within = admin;
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
        layer(folder, PORTS, f -> make(str(props(f), "name"), "port", f, true));
        layer(folder, AIRPORTS, f -> {
            Rec r = make(str(props(f), "name"), "airport", f, true);
            if (r != null) {
                r.code = str(props(f), "iata_code");
            }
            return r;
        });
        layer(folder, PARKS, f -> make(str(props(f), "name"), "park", f, true));
        layer(folder, MARINE, f -> {
            JsonObject p = props(f);
            String type = marineType(str(p, "featurecla"));
            Rec r = type == null ? null : make(str(p, "name"), type, f, true);
            if (r != null) {
                r.alt = alt(r.name, p, "namealt");
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
        layer(folder, REGIONS, f -> {
            JsonObject p = props(f);
            String type = regionType(str(p, "featurecla"));
            Rec r = type == null ? null : make(str(p, "name"), type, f, true);
            if (r != null) {
                r.alt = alt(r.name, p, "namealt");
                r.within = str(p, "region");
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
        layer(folder, REGION_POINTS, f -> {
            JsonObject p = props(f);
            String type = str(p, "featurecla");
            Rec r = type == null ? null : make(str(p, "name"), type.toLowerCase(Locale.ROOT), f, true);
            if (r != null) {
                r.alt = alt(r.name, p, "name_alt");
                r.within = str(p, "region");
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
        layer(folder, ELEVATION, f -> {
            JsonObject p = props(f);
            String type = str(p, "featurecla");
            Rec r = type == null || type.equals("cape") ? null : make(str(p, "name"), type.equals("spot elevation") ? "peak" : type, f, true);
            if (r != null) {
                r.alt = alt(r.name, p, "name_alt");
                r.within = str(p, "region");
                r.elevation = number(p, "elevation") == null ? null : number(p, "elevation").intValue();
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
        layer(folder, LAKES, f -> {
            JsonObject p = props(f);
            String featureClass = str(p, "featurecla");
            Rec r = make(str(p, "name"), "Reservoir".equals(featureClass) ? "reservoir" : "lake", f, true);
            if (r != null) {
                r.alt = alt(r.name, p, "name_alt");
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
        layer(folder, RIVERS, f -> {
            JsonObject p = props(f);
            Rec r = "River".equals(str(p, "featurecla")) ? make(str(p, "name"), "river", f, true) : null;
            if (r != null) {
                r.alt = alt(r.name, p, "name_alt", "name_en");
                r.rank = number(p, "scalerank") == null ? null : number(p, "scalerank").intValue();
            }
            return r;
        });
    }

    /** The pieces of a feature (a river is many lines) become one record; places and countries are never merged. */
    static List<Rec> merge(List<Rec> all) {
        Set<String> single = Set.of("city", "capital", "provincial capital", "station", "historic place", "province", "country");
        Map<String, List<Rec>> clusters = new TreeMap<>();
        List<Rec> out = new ArrayList<>();
        for (Rec r : all) {
            if (single.contains(r.type) || r.bbox == null) {
                out.add(r);
                continue;
            }
            List<Rec> same = clusters.computeIfAbsent(r.type + "|" + r.name.toLowerCase(Locale.ROOT), k -> new ArrayList<>());
            Rec target = null;
            for (Rec other : same) {
                if (touches(other.bbox, r.bbox)) {
                    target = other;
                    break;
                }
            }
            if (target == null) {
                same.add(r);
                out.add(r);
                continue;
            }
            target.bbox = new double[]{Math.min(target.bbox[0], r.bbox[0]), Math.min(target.bbox[1], r.bbox[1]),
                    Math.max(target.bbox[2], r.bbox[2]), Math.max(target.bbox[3], r.bbox[3])};
            for (Map.Entry<String, Integer> c : r.countries.entrySet()) {
                target.countries.merge(c.getKey(), c.getValue(), Integer::sum);
            }
            for (String a : r.alt) {
                if (!target.alt.contains(a)) {
                    target.alt.add(a);
                }
            }
            if (r.weight > target.weight) {
                target.lat = r.lat;
                target.lon = r.lon;
                target.weight = r.weight;
            }
            if (target.rank == null || (r.rank != null && r.rank < target.rank)) {
                target.rank = r.rank;
            }
        }
        return out;
    }

    static boolean touches(double[] a, double[] b) {
        return a[0] - MERGE_GAP <= b[2] && b[0] - MERGE_GAP <= a[2] && a[1] - MERGE_GAP <= b[3] && b[1] - MERGE_GAP <= a[3];
    }

    public static void main(String[] args) throws IOException {
        Path folder = Path.of(args[0]);
        Path output = Path.of(args[1]);
        for (String file : ALL) {
            if (!Files.exists(folder.resolve(file))) {
                System.out.println("place-provider: " + file + " is missing, " + output + " left as it is");
                return;
            }
        }
        long start = System.currentTimeMillis();
        countries(folder);
        countryRecords();
        layers(folder);
        List<Rec> merged = merge(records);
        merged.sort(Comparator.comparing((Rec r) -> r.name.toLowerCase(Locale.ROOT)).thenComparing(r -> r.type));

        List<Map<String, Object>> table = new ArrayList<>();
        Set<String> known = new HashSet<>();
        for (Country c : countries) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", c.name);
            if (c.iso2 != null) {
                m.put("iso2", c.iso2);
            }
            if (c.iso3 != null) {
                m.put("iso3", c.iso3);
            }
            if (!c.alt.isEmpty()) {
                m.put("alt", c.alt);
            }
            table.add(m);
            known.add(c.name);
        }

        StringBuilder out = new StringBuilder("{\n\"source\": \"Natural Earth (public domain), 1:10m\",\n\"countries\": ");
        out.append(GSON.toJson(table)).append(",\n\"places\": [\n");
        Map<String, Integer> perType = new TreeMap<>();
        Set<String> unknown = new TreeSet<>();
        int withoutCountry = 0;
        for (int i = 0; i < merged.size(); i++) {
            Rec r = merged.get(i);
            out.append(GSON.toJson(r.json())).append(i + 1 < merged.size() ? ",\n" : "\n");
            perType.merge(r.type, 1, Integer::sum);
            if (r.countries.isEmpty()) {
                withoutCountry++;
            }
            r.countries.keySet().stream().filter(k -> !known.contains(k)).forEach(unknown::add);
        }
        out.append("]\n}\n");
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, out.toString(), StandardCharsets.UTF_8);
        System.out.println("place-provider: " + merged.size() + " records in " + (System.currentTimeMillis() - start) / 1000 + " s, "
                + Files.size(output) / 1024 + " KB written to " + output);
        System.out.println("place-provider: by type " + perType);
        System.out.println("place-provider: " + withoutCountry + " without a country, " + unnamed + " features without a name skipped");
        if (!unknown.isEmpty()) {
            System.out.println("place-provider: countries that are not in the table: " + unknown);
        }
    }

}
