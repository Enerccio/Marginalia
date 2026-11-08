package com.github.enerccio.marginalia.extensions.httphooks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/** Collects the body of an answer and stops reading at the limit: the rest is never taken into memory. */
final class CappedBody implements HttpResponse.BodySubscriber<byte[]> {

    /** The answer is bigger than the limit. */
    static final class TooBig extends IOException {
        TooBig(int max) {
            super("more than " + max + " bytes");
        }
    }

    private final int max;
    private final boolean refuse;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final CompletableFuture<byte[]> result = new CompletableFuture<>();
    private Flow.Subscription subscription;
    private boolean done;

    /** @param refuse the headers already say the body is over the limit: read nothing */
    CappedBody(int max, boolean refuse) {
        this.max = max;
        this.refuse = refuse;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        if (refuse) {
            fail(new TooBig(max));
        } else {
            subscription.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onNext(List<ByteBuffer> items) {
        if (done) {
            return;
        }
        for (ByteBuffer buffer : items) {
            if ((long) out.size() + buffer.remaining() > max) {
                fail(new TooBig(max));
                return;
            }
            byte[] chunk = new byte[buffer.remaining()];
            buffer.get(chunk);
            out.write(chunk, 0, chunk.length);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (!done) {
            done = true;
            result.completeExceptionally(throwable);
        }
    }

    @Override
    public void onComplete() {
        if (!done) {
            done = true;
            result.complete(out.toByteArray());
        }
    }

    @Override
    public CompletionStage<byte[]> getBody() {
        return result;
    }

    private void fail(IOException e) {
        done = true;
        subscription.cancel();
        result.completeExceptionally(e);
    }
}
