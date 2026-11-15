package com.github.enerccio.marginalia.domain.service.gpu;

import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Makes the HTTP request of a {@link GpuStep}. The administrator set the step up, so any address is allowed (the servers are usually on the
 * local network). The answer is read up to a small limit and thrown away; the status says whether it went well.
 */
public final class GpuStepRunner {

    private static final Logger log = LoggerFactory.getLogger(GpuStepRunner.class);

    /** A step answers with little; a longer answer is an error page, it is not read. */
    private static final int MAX_ANSWER_BYTES = 1024 * 1024;

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** A step as it is run: a copy that does not need the database. */
    public record Step(String name, String url, String method, String headers, String body, int timeoutSeconds, int waitSeconds) {

        public static Step of(GpuStep step) {
            return new Step(step.getName(), step.getUrl(), step.getMethod(), step.getHeaders(), step.getBody(), step.effectiveTimeoutSeconds(),
                    step.effectiveWaitSeconds());
        }
    }

    private GpuStepRunner() {
    }

    /**
     * @throws GpuException the address is not valid, the server is not reachable or answers with an error
     */
    public static void run(Step step) throws GpuException {
        URI uri;
        try {
            uri = URI.create(step.url() == null ? "" : step.url().strip());
        } catch (IllegalArgumentException e) {
            throw new GpuException("Not an address: " + step.url(), e);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https") || uri.getHost() == null) {
            throw new GpuException("Not an http(s) address: " + step.url());
        }
        String method = step.method() == null || step.method().isBlank() ? "POST" : step.method().strip().toUpperCase(Locale.ROOT);
        boolean hasBody = step.body() != null && !step.body().isBlank() && !method.equals("GET");
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(Math.max(1, step.timeoutSeconds())))
                .method(method, hasBody ? HttpRequest.BodyPublishers.ofString(step.body()) : HttpRequest.BodyPublishers.noBody());
        boolean contentType = false;
        for (String[] header : headers(step.headers())) {
            try {
                builder.header(header[0], header[1]);
            } catch (IllegalArgumentException e) {
                throw new GpuException("Not a header: " + header[0], e);
            }
            contentType |= header[0].equalsIgnoreCase("Content-Type");
        }
        if (hasBody && !contentType) {
            builder.header("Content-Type", "application/json");
        }
        long start = System.nanoTime();
        int status;
        try {
            HttpResponse<InputStream> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            status = response.statusCode();
            try (InputStream body = response.body()) {
                body.readNBytes(MAX_ANSWER_BYTES);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw GpuException.cancelled();
        } catch (IOException e) {
            throw new GpuException("The step " + label(step, uri) + " failed: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
        }
        log.info("GPU step {} {} {}: {} in {} ms", label(step, uri), method, uri.getHost(), status, (System.nanoTime() - start) / 1_000_000);
        if (status < 200 || status >= 300) {
            throw new GpuException("The step " + label(step, uri) + " answered " + status);
        }
        if (step.waitSeconds() > 0) {
            try {
                Thread.sleep(step.waitSeconds() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw GpuException.cancelled();
            }
        }
    }

    /**
     * Runs the steps in order and stops at the first failure (what loads a lock: the next step must not load a model on a full GPU).
     */
    public static void runAll(List<Step> steps) throws GpuException {
        for (int i = 0; i < steps.size(); i++) {
            try {
                run(steps.get(i));
            } catch (GpuException e) {
                if (e.isCancelled() || steps.size() == 1) {
                    throw e;
                }
                throw new GpuException("Step " + (i + 1) + " of " + steps.size() + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Runs every step whatever happens to one of them (what frees a GPU: every container is to be stopped).
     *
     * @return the message of the first failure, null when all went well
     */
    public static String runAllRegardless(List<Step> steps) {
        String first = null;
        for (int i = 0; i < steps.size(); i++) {
            try {
                run(steps.get(i));
            } catch (GpuException e) {
                log.warn("GPU release step {} of {} failed: {}", i + 1, steps.size(), e.getMessage());
                if (first == null) {
                    first = steps.size() == 1 ? e.getMessage() : "Step " + (i + 1) + " of " + steps.size() + ": " + e.getMessage();
                }
            }
        }
        return first;
    }

    private static List<String[]> headers(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return text.lines()
                .map(line -> {
                    int colon = line.indexOf(':');
                    return colon > 0 ? new String[]{line.substring(0, colon).strip(), line.substring(colon + 1).strip()} : null;
                })
                .filter(h -> h != null && !h[0].isEmpty())
                .toList();
    }

    private static String label(Step step, URI uri) {
        return step.name() == null || step.name().isBlank() ? uri.getHost() : "\"" + step.name() + "\"";
    }
}
