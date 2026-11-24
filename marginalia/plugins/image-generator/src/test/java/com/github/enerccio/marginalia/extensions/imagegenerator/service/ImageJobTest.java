package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * One image holds the GPU lock for itself only, with the requests before and after it inside.
 */
class ImageJobTest {

    private final List<String> events = new ArrayList<>();

    private final ImageJob.Gpu gpu = cancel -> {
        events.add("take");
        return () -> events.add("give back");
    };

    private ImageBackend backend(boolean fails) {
        return new ImageBackend() {
            @Override
            public byte[] generate(ImageRequest request, Cancel cancel) throws BackendException {
                events.add("image");
                if (fails) {
                    throw new BackendException("broken");
                }
                return new byte[]{1};
            }

            @Override
            public void test() {
            }
        };
    }

    private static ImageRequest request() {
        return new ImageRequest("a cat", "", 1, 64, 64, null, 1.0);
    }

    @Test
    void theHooksRunInsideTheLockAroundTheImage() throws Exception {
        byte[] image = ImageJob.run(gpu, backend(false), () -> events.add("before"), () -> events.add("after"), request(), () -> false);
        assertArrayEquals(new byte[]{1}, image);
        assertEquals(List.of("take", "before", "image", "after", "give back"), events);
    }

    @Test
    void theLockIsGivenBackWhenTheBackendFails() {
        assertThrows(BackendException.class,
                () -> ImageJob.run(gpu, backend(true), () -> events.add("before"), () -> events.add("after"), request(), () -> false));
        assertEquals(List.of("take", "before", "image", "after", "give back"), events);
    }

    @Test
    void aFailedRequestBeforeTheImageStopsItAndGivesTheLockBack() {
        BackendException e = assertThrows(BackendException.class, () -> ImageJob.run(gpu, backend(false), () -> {
            throw new BackendException("no server");
        }, () -> events.add("after"), request(), () -> false));
        assertEquals("no server", e.getMessage());
        assertEquals(List.of("take", "give back"), events);
    }

    @Test
    void aFailedRequestAfterTheImageKeepsTheImage() throws Exception {
        byte[] image = ImageJob.run(gpu, backend(false), () -> {
        }, () -> {
            throw new BackendException("stop failed");
        }, request(), () -> false);
        assertNotNull(image);
        assertEquals(List.of("take", "image", "give back"), events);
    }

    @Test
    void aGpuThatCannotBeTakenMakesNoImage() {
        ImageJob.Gpu busy = cancel -> {
            throw new BackendException("step failed");
        };
        assertThrows(BackendException.class, () -> ImageJob.run(busy, backend(false), () -> events.add("before"), () -> events.add("after"), request(),
                () -> false));
        assertTrue(events.isEmpty());
    }
}
