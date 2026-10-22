import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

/**
 * Build step of the place provider (run by exec-maven-plugin with the profile {@code import-places}, see the pom):
 * downloads the layers of the 1:10m <a href="https://github.com/martynafford/natural-earth-geojson">Natural Earth
 * GeoJSON</a> (public domain, CC0) that {@code BuildPlaces.java} reads. Run as
 * {@code java FetchNaturalEarth.java <raw url of the 10m folder> <download folder>}.
 * <p>
 * Only the listed files are fetched (about 80 MB), not the whole repository, which also holds 7z copies and the road
 * and railroad layers. A file that is already in the folder is not downloaded again (delete it to refresh); a file that
 * cannot be downloaded is reported and skipped, {@code BuildPlaces} then leaves {@code places.json} as it is.
 */
public class FetchNaturalEarth {

    /** The layers, relative to the 10m folder; {@code BuildPlaces} reads them by the same names. */
    static final List<String> LAYERS = List.of(
            "cultural/ne_10m_admin_0_countries.json",
            "cultural/ne_10m_admin_1_label_points.json",
            "cultural/ne_10m_populated_places_simple.json",
            "cultural/ne_10m_ports.json",
            "cultural/ne_10m_airports.json",
            "cultural/ne_10m_parks_and_protected_lands_area.json",
            "physical/ne_10m_geography_marine_polys.json",
            "physical/ne_10m_geography_regions_polys.json",
            "physical/ne_10m_geography_regions_points.json",
            "physical/ne_10m_geography_regions_elevation_points.json",
            "physical/ne_10m_lakes.json",
            "physical/ne_10m_rivers_lake_centerlines.json");

    public static void main(String[] args) throws InterruptedException {
        String base = args[0].endsWith("/") ? args[0] : args[0] + "/";
        Path folder = Path.of(args[1]);
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20)).build();
        int failed = 0;
        for (String layer : LAYERS) {
            Path target = folder.resolve(Path.of(layer).getFileName().toString());
            if (Files.exists(target)) {
                continue;
            }
            Path part = folder.resolve(target.getFileName() + ".part");
            try {
                Files.createDirectories(folder);
                HttpResponse<Path> response = client.send(HttpRequest.newBuilder(URI.create(base + layer)).GET().build(),
                        HttpResponse.BodyHandlers.ofFile(part));
                if (response.statusCode() != 200) {
                    throw new IOException("HTTP " + response.statusCode());
                }
                Files.move(part, target, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("place-provider: downloaded " + target.getFileName() + " (" + Files.size(target) / 1024 + " KB)");
            } catch (IOException e) {
                failed++;
                System.out.println("place-provider: cannot download " + layer + ": " + e);
                try {
                    Files.deleteIfExists(part);
                } catch (IOException ignored) {
                    // a leftover part file is replaced by the next try
                }
            }
        }
        if (failed > 0) {
            System.out.println("place-provider: " + failed + " layers missing, places.json will be left as it is");
        }
    }

}
