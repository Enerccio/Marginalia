package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.test.TestImages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code view_image}: what the model is told, what it is refused, and what the runner is left to show.
 */
class ImageToolsTest {

    private static final class Answer implements FunctionContinuation<String> {
        String result;
        String error;

        @Override
        public void complete(String value) {
            result = value;
        }

        @Override
        public void fail(String message) {
            error = message;
        }

        @Override
        public void fail(Throwable throwable) {
            error = throwable.getMessage();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    @TempDir
    Path home;

    private VirtualFileSystem fs;
    private ShownImages shown;
    private ImageTools tools;

    @BeforeEach
    void open() {
        RunFolder folder = RunFolder.of(home, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        fs = VirtualFileSystem.ofRun(folder, WorkspaceLimits.DEFAULT, new ReentrantLock());
        shown = new ShownImages();
        tools = new ImageToolsImpl(fs, shown);
    }

    private Answer call(Consumer<Answer> call) {
        Answer answer = new Answer();
        call.accept(answer);
        return answer;
    }

    @Test
    void aViewedImageIsReportedAndLeftForTheRunnerToShow() throws IOException {
        fs.writeImage(fs.parse("/refs/map.png"), TestImages.image(300, 200, "png"));

        Answer answer = call(a -> tools.viewImage(a, "/refs/map.png"));

        assertThat(answer.error).isNull();
        assertThat(answer.result).isEqualTo("Showing /refs/map.png (png, 300 x 200 pixels) in the next message.");
        assertThat(shown.take()).extracting(VirtualPath::toString).containsExactly("/refs/map.png");
        assertThat(shown.take()).as("taken once").isEmpty();
    }

    @Test
    void aLargeImageIsSaidToBeScaledDown() throws IOException {
        fs.writeImage(fs.parse("/big.png"), TestImages.image(3200, 1600, "png"));

        assertThat(call(a -> tools.viewImage(a, "/big.png")).result)
                .isEqualTo("Showing /big.png (png, " + ImageFiles.MAX_SIDE + " x " + ImageFiles.MAX_SIDE / 2 + " pixels, scaled down) in the next message.");
    }

    @Test
    void theSameImageTwiceIsShownOnce() throws IOException {
        fs.writeImage(fs.parse("/a.png"), TestImages.image(20, 20, "png"));

        call(a -> tools.viewImage(a, "/a.png"));
        call(a -> tools.viewImage(a, "/a.png"));

        assertThat(shown.take()).hasSize(1);
    }

    @Test
    void whatIsNotAnImageOrNotThereIsRefusedAndNothingIsShown() {
        fs.writeText(fs.parse("/note.md"), "text", false);

        assertThat(call(a -> tools.viewImage(a, "/note.md")).error).contains("not an image");
        assertThat(call(a -> tools.viewImage(a, "/missing.png")).error).contains("no file /missing.png");
        assertThat(call(a -> tools.viewImage(a, "../../etc/passwd")).error).isNotNull().doesNotContain(home.toString());
        assertThat(call(a -> tools.viewImage(a, "/checkpoints")).error).isNotNull();
        assertThat(shown.take()).isEmpty();
    }
}
