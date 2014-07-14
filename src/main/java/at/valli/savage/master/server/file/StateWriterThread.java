package at.valli.savage.master.server.file;

import at.valli.savage.master.server.state.ServerStateRegistry;
import at.valli.savage.master.server.util.FutureEvaluator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by valli on 13.07.2014.
 */
public final class StateWriterThread extends Thread {

    private static final Logger LOG = LogManager.getLogger(StateWriterThread.class);
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("StateWriterThread-%d").build();
    private static final long FILE_WRITING_INTERVAL_SECONDS = Long.getLong("dat.file.writing.interval.seconds", 60L);

    private final AtomicBoolean started = new AtomicBoolean();

    private final ServerStateRegistry stateRegistry;

    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> task;

    public StateWriterThread(final ServerStateRegistry stateRegistry) {
        Validate.notNull(stateRegistry, "stateRegistry must not be null");
        this.stateRegistry = stateRegistry;
    }

    @Override
    public void run() {
        task = executorService.scheduleAtFixedRate(new StateFileWriter(stateRegistry), FILE_WRITING_INTERVAL_SECONDS, FILE_WRITING_INTERVAL_SECONDS, TimeUnit.SECONDS);
        new FutureEvaluator(task).start();
    }

    public void startWriting() {
        if (started.get()) {
            throw new IllegalStateException("StateWriterThread already started ...");
        } else {
            LOG.debug("Starting StateWriterThread ...");
            started.set(true);
            executorService = Executors.newScheduledThreadPool(1, NAMED_THREAD_FACTORY);
            this.start();
        }
    }

    public void stopWriting() {
        LOG.debug("Stopping StateWriterThread ...");
        started.set(false);
        task.cancel(true);
        executorService.shutdown();
    }
}
