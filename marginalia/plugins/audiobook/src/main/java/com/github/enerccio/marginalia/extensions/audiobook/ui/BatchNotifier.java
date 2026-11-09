package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionQueue;
import com.github.enerccio.marginalia.ui.widgets.DesktopNotifier;
import com.vaadin.flow.component.UI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Tells the user in the browser (the desktop notification of the application, when they chose it in the notification settings) that a batch of
 * conversions is over, so they can leave the tab while the speech server works. A batch is the jobs one click or one command put into the queue;
 * a batch the user stopped does not notify.
 */
public final class BatchNotifier {

    /** The id of the source in the notification settings; the automatic conversion of a new part is its automatic entry. */
    public static final String SOURCE = "audiobook.convert";

    private BatchNotifier() {
    }

    /**
     * Calls {@code done} once, from the thread that made the last job, when all the jobs are over (also at once when they are).
     */
    public static void whenOver(ConversionQueue queue, List<ConversionJob> jobs, Consumer<List<ConversionJob.Snapshot>> done) {
        if (jobs.isEmpty()) {
            return;
        }
        Set<String> ids = new HashSet<>();
        jobs.forEach(job -> ids.add(job.id()));
        AtomicBoolean called = new AtomicBoolean();
        Consumer<ConversionJob.Snapshot> listener = new Consumer<>() {
            @Override
            public void accept(ConversionJob.Snapshot ignored) {
                List<ConversionJob.Snapshot> mine = queue.jobs().stream().filter(job -> ids.contains(job.id())).toList();
                if (mine.size() == ids.size() && mine.stream().allMatch(ConversionJob.Snapshot::finished) && called.compareAndSet(false, true)) {
                    queue.removeListener(this);
                    done.accept(mine);
                }
            }
        };
        queue.addListener(listener);
        listener.accept(null);
    }

    /** Notifies the page when the batch ended well or failed; a stopped one says nothing. Can be called from any thread. */
    public static void tell(UI ui, boolean automatic, String book, List<ConversionJob.Snapshot> jobs) {
        if (ui == null || jobs.isEmpty()) {
            return;
        }
        boolean failed = jobs.stream().anyMatch(job -> job.state() == ConversionJob.State.FAILED);
        boolean stopped = !failed && jobs.stream().anyMatch(job -> job.state() == ConversionJob.State.CANCELLED);
        if (stopped) {
            return;
        }
        try {
            ui.access(() -> {
                DesktopNotifier notifier = new DesktopNotifier(ui);
                if (failed) {
                    notifier.failed(SOURCE, automatic, book);
                } else {
                    notifier.finished(SOURCE, automatic, book);
                }
            });
        } catch (RuntimeException e) {
            // the page is gone
        }
    }
}
