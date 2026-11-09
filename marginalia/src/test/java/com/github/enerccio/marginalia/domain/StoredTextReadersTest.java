package com.github.enerccio.marginalia.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps the list of the code that reads the text of a part as it is stored ({@code getResponse()}) known.
 * <p>
 * An extension may keep the story in another form than a model should read it, so whatever gives the text of a part to a model takes it
 * from {@code PartTextRegistry}; the rest of the readers are listed in {@code stored-text-readers.txt} with the reason. A new reader fails
 * this test until somebody has decided which of the two it is.
 */
class StoredTextReadersTest {

    private static final Pattern READS_STORED_TEXT = Pattern.compile("\\.getResponse\\(\\)|::getResponse\\b");

    private static Set<String> listed() throws IOException {
        try (InputStream in = StoredTextReadersTest.class.getResourceAsStream("/stored-text-readers.txt")) {
            assertThat(in).as("stored-text-readers.txt").isNotNull();
            return Arrays.stream(new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n"))
                    .map(String::strip).filter(line -> !line.isEmpty() && !line.startsWith("#")).collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private static Set<String> found() throws IOException {
        Path root = Path.of("src/main/java");
        assertThat(root).as("the sources of the application, the tests run in its folder").isDirectory();
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java")).filter(path -> {
                try {
                    return READS_STORED_TEXT.matcher(Files.readString(path, StandardCharsets.UTF_8)).find();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }).map(path -> root.relativize(path).toString().replace('\\', '/')).collect(Collectors.toCollection(TreeSet::new));
        }
    }

    @Test
    void everyReaderOfTheStoredTextIsListed() throws IOException {
        Set<String> unknown = new TreeSet<>(found());
        unknown.removeAll(listed());
        assertThat(unknown).as("Files that read the stored text of a part and are not in stored-text-readers.txt. If a file gives the text "
                + "to a model, it takes it from PartTextRegistry; if it works on the text as it is stored, add it to the list under the right heading")
                .isEmpty();
    }

    @Test
    void theListHasNoFileThatNoLongerReads() throws IOException {
        Set<String> stale = new TreeSet<>(listed());
        stale.removeAll(found());
        assertThat(stale).as("Files in stored-text-readers.txt that do not read the stored text any more: remove them").isEmpty();
    }
}
