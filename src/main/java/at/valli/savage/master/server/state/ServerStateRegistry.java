package at.valli.savage.master.server.state;

import at.valli.savage.master.server.core.Service;
import at.valli.savage.master.server.util.FutureEvaluator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by valli on 13.07.2014.
 */
public final class ServerStateRegistry implements Service {

    private static final Logger LOG = LogManager.getLogger(ServerStateRegistry.class);
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("RegistryCleanerTask-%d").build();
    private static final long SERVER_STATE_TIMEOUT = TimeUnit.SECONDS.toNanos(Long.getLong("server.state.max.storage.seconds", 90L));
    private final Object LOCK = new Object();
    private final AtomicBoolean started = new AtomicBoolean();
    private final Set<ServerState> states = new HashSet<>();
    private final ServerStatesUpdateListener updateListener;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> cleanTask;

    public ServerStateRegistry(final ServerStatesUpdateListener updateListener) {
        Validate.notNull(updateListener);
        this.updateListener = updateListener;
    }

    private static boolean serverTimeout(final ServerState serverState) {
        return Math.abs(System.nanoTime() - serverState.getTime()) > SERVER_STATE_TIMEOUT;
    }

    public void add(final ServerState serverState) {
        Validate.notNull(serverState, "serverState must not be null");
        synchronized (LOCK) {
            states.remove(serverState);
            states.add(serverState);
            notifyListener();
            LOG.info("Current server registry state {}", states);
        }
    }

    public void remove(final ServerState serverState) {
        Validate.notNull(serverState, "serverState must not be null");
        synchronized (LOCK) {
            states.remove(serverState);
            notifyListener();
            LOG.info("Current server registry state {}", states);
        }
    }

    private void notifyListener() {
        updateListener.stateUpdated(new HashSet<>(states));
    }

    @Override
    public void startup() {
        if (started.get()) {
            throw new IllegalStateException("ServerStateRegistry already started ...");
        } else {
            LOG.info("Starting registry service ...");
            executorService = Executors.newScheduledThreadPool(1, NAMED_THREAD_FACTORY);
            cleanTask = executorService.scheduleAtFixedRate(new RegistryCleanerTask(), SERVER_STATE_TIMEOUT, SERVER_STATE_TIMEOUT, TimeUnit.SECONDS);
            new FutureEvaluator(cleanTask).start();
            started.set(true);
        }
    }

    @Override
    public void shutdown() {
        LOG.info("Stopping registry service ...");
        cleanTask.cancel(true);
        executorService.shutdown();
        started.set(false);
    }

    private class RegistryCleanerTask implements Runnable {

        @Override
        public void run() {
            LOG.debug("Cleaning registry ...");

            synchronized (LOCK) {
                Set<ServerState> statesToCheck = new HashSet<>(states);
                boolean notifyListener = false;
                for (ServerState state : statesToCheck) {
                    if (serverTimeout(state)) {
                        states.remove(state);
                        notifyListener = true;
                    }
                }
                if (notifyListener) {
                    notifyListener();
                }
            }

            LOG.debug("Registry cleaned ...");
        }
    }
}
