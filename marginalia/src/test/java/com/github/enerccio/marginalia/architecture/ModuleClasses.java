package com.github.enerccio.marginalia.architecture;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.Defaults;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Where the main classes of each module were compiled to: the platform's ({@code marginalia/platform}) and the
 * application's. A module is a folder of classes in a reactor build ({@code mvn test} from the root, also with
 * {@code -Dbuild.dir}) and the installed JAR when the application is built on its own; a JAR is opened as a file
 * system, so both are read the same way. {@link #SOURCES} are the folders of their sources, for the tests that read
 * them.
 */
public final class ModuleClasses {

    public static final String PLATFORM = "platform";
    public static final String APP = "app";

    /**
     * The source folders of the main classes, relative to the application's folder (the tests run there): the
     * platform's first.
     */
    public static final List<Path> SOURCES = List.of(Path.of("platform/src/main/java"), Path.of("src/main/java"));

    private static Map<String, Path> roots;

    private ModuleClasses() {
    }

    /** The layer name ({@link #PLATFORM}, {@link #APP}) to the root of that module's classes. */
    public static synchronized Map<String, Path> roots() {
        if (roots == null) {
            Map<String, Path> found = new LinkedHashMap<>();
            found.put(PLATFORM, root(Configuration.class));
            found.put(APP, root(Defaults.class));
            if (found.get(PLATFORM).equals(found.get(APP))) {
                throw new IllegalStateException("The platform and the application were compiled to the same place: "
                        + found.get(APP));
            }
            roots = found;
        }
        return roots;
    }

    private static Path root(Class<?> type) {
        try {
            Path location = Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(location)) {
                return location;
            }
            FileSystem jar = FileSystems.newFileSystem(location);
            return jar.getRootDirectories().iterator().next();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
