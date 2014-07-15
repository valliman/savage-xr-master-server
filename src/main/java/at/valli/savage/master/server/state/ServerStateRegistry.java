package at.valli.savage.master.server.state;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.valli.savage.master.server.file.StateFileWriter;
import at.valli.savage.master.server.util.FutureEvaluator;

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
public final class ServerStateRegistry {

    private static final Logger LOG = LogManager.getLogger(ServerStateRegistry.class);
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("RegistryService-%d").build();
    private static final long FILE_WRITING_INTERVAL_SECONDS = Long.getLong("dat.file.writing.interval.seconds", 60L);
    private static final long SERVER_STATE_TIMEOUT = Long.getLong("server.state.max.storage.seconds", 90L);
    
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> writeTask, cleanTask;
    
    private final Object LOCK = new Object();
    private final Set<ServerState> states = new HashSet<>();
    private final AtomicBoolean changeFlag = new AtomicBoolean();
    
    public void add(final ServerState serverState) {
        Validate.notNull(serverState, "serverState must not be null");
        synchronized (LOCK) {
            states.remove(serverState);
            states.add(serverState);
            changeFlag.set(true);
            LOG.info("Current server registry state {}", states);
        }
    }

    public void remove(final ServerState serverState) {
        Validate.notNull(serverState, "serverState must not be null");
        synchronized (LOCK) {
            states.remove(serverState);
            changeFlag.set(true);
            LOG.info("Current server registry state {}", states);
        }
    }

    public void removeIfOlder(long cullTime) {
    	synchronized (LOCK) {
    		for (ServerState serverState : states) {
    			if(cullTime >= serverState.getTime()) {
    				states.remove(serverState);
    	            changeFlag.set(true);
    			}
    		}
    	}
    }
    
    public Set<ServerState> getServerStates() {
        synchronized (LOCK) {
        	changeFlag.set(false);
            return new HashSet<>(states);
        }
    }
    
    public boolean isChanged() {
    	return changeFlag.get();
    }
    
    public void startServices() {
    	LOG.info("Starting registry services ...");
    	changeFlag.set(true);
    	executorService = Executors.newScheduledThreadPool(2, NAMED_THREAD_FACTORY);
        
    	writeTask = executorService.scheduleAtFixedRate(new StateFileWriter(this), FILE_WRITING_INTERVAL_SECONDS, FILE_WRITING_INTERVAL_SECONDS, TimeUnit.SECONDS);
        new FutureEvaluator(writeTask).start();
        cleanTask = executorService.scheduleAtFixedRate(new RegistryCleaner(this), SERVER_STATE_TIMEOUT, SERVER_STATE_TIMEOUT, TimeUnit.SECONDS);
        new FutureEvaluator(cleanTask).start();
    }

    public void stopServices() {
        LOG.info("Stopping registry services ...");
        writeTask.cancel(true);
        cleanTask.cancel(true);
        executorService.shutdown();
    }
}
