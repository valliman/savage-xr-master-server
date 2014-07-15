package at.valli.savage.master.server.state;

import java.util.Set;

/**
 * Created by valli on 15.07.2014.
 */
public interface ServerStatesUpdateListener {

    void stateUpdated(final Set<ServerState> serverStates);

}
