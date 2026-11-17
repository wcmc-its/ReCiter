package reciter.xml.retriever.pubmed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class RetrievalErrorTrackerTest {

    @Test
    void resetClearsFlagAndMarkErrorSetsIt() {
        RetrievalErrorTracker.reset();
        assertFalse(RetrievalErrorTracker.hadError());

        RetrievalErrorTracker.markError();
        assertTrue(RetrievalErrorTracker.hadError());

        RetrievalErrorTracker.reset();
        assertFalse(RetrievalErrorTracker.hadError());
    }

    /** Each uid retrieval runs on its own thread — one run's failure must not leak to another. */
    @Test
    void errorFlagIsThreadScoped() throws InterruptedException {
        RetrievalErrorTracker.reset();
        RetrievalErrorTracker.markError();
        assertTrue(RetrievalErrorTracker.hadError());

        AtomicBoolean otherThreadSawError = new AtomicBoolean(true);
        Thread other = new Thread(() -> otherThreadSawError.set(RetrievalErrorTracker.hadError()));
        other.start();
        other.join();

        assertFalse(otherThreadSawError.get(), "a different thread must not see another run's error flag");
    }

    /**
     * Mirrors retrieveArticlesByDateRange: many identities on virtual-thread-per-task threads at once.
     * Each task resets, optionally marks an error, and must read back only its own state.
     */
    @Test
    void errorFlagIsIsolatedAcrossConcurrentVirtualThreads() throws Exception {
        final int tasks = 500;
        List<Future<Boolean>> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < tasks; i++) {
                final boolean failing = i % 2 == 0;
                Callable<Boolean> task = () -> {
                    RetrievalErrorTracker.reset();
                    if (failing) {
                        RetrievalErrorTracker.markError();
                    }
                    Thread.yield();
                    return RetrievalErrorTracker.hadError() == failing;
                };
                results.add(executor.submit(task));
            }
        }
        int isolated = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                isolated++;
            }
        }
        assertEquals(tasks, isolated, "every task must see only its own error flag");
    }
}
