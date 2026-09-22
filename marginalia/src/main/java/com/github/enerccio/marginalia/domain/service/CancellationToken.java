package com.github.enerccio.marginalia.domain.service;

import java.util.concurrent.atomic.AtomicBoolean;

public class CancellationToken {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Runnable onCancelCallback;

    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            if (onCancelCallback != null) {
                onCancelCallback.run();
            }
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }
}