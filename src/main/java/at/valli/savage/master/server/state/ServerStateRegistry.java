package at.valli.savage.master.server.state;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by valli on 13.07.2014.
 */
public final class ServerStateRegistry {

    private static final Logger LOG = LogManager.getLogger(ServerStateRegistry.class);

    private final Object LOCK = new Object();

    private Set<ServerState> states = new HashSet<>();

    public void add(ServerState serverState) {
        Validate.notNull(serverState, "serverState must not be null");
        synchronized (LOCK) {
            states.remove(serverState);
            states.add(serverState);
            LOG.info("Current server registry state {}", states);
        }
    }

    public void remove(ServerState serverState) {
        Validate.notNull(serverState, "serverState must not be null");
        synchronized (LOCK) {
            states.remove(serverState);
            LOG.info("Current server registry state {}", states);
        }
    }

    public Set<ServerState> getServerStates() {
        synchronized (LOCK) {
            return new HashSet<>(states);
        }
    }
}
