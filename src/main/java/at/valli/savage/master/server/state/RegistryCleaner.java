package at.valli.savage.master.server.state;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by biggeruniverse on 15.07.2014.
 */
public class RegistryCleaner implements Runnable {
	private static final Logger LOG = LogManager.getLogger(RegistryCleaner.class);
	private static final long SERVER_STATE_TIMEOUT = TimeUnit.SECONDS.toNanos(Long.getLong("server.state.max.storage.seconds", 90L));
	
	private final ServerStateRegistry stateRegistry;

    public RegistryCleaner(final ServerStateRegistry stateRegistry) {
        Validate.notNull(stateRegistry, "stateRegistry must not be null");
        this.stateRegistry = stateRegistry;
    }

	@Override
	public void run() {
		try {
			LOG.debug("Cleaning registry ...");
			stateRegistry.removeIfOlder(System.nanoTime()-SERVER_STATE_TIMEOUT);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
	}
}
