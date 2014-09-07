package at.valli.savage.master.server.web;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStatesUpdateListener;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by valli on 06.09.2014.
 */
public class StateSessionWriter implements ServerStatesUpdateListener, SessionListener {

    private static final Logger LOG = LogManager.getLogger(StateSessionWriter.class);
    private final AtomicReference<State> state = new AtomicReference<>(new State());
    private final Gson gson;

    public StateSessionWriter() {
        gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return (field.getName().equals("time") || field.getName().equals("rawIp"));
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }).create();
    }

    @Override
    public void stateUpdated(Set<ServerState> serverStates) {
        state.set(new State(serverStates));
        for (Session session : SessionManager.INSTANCE.getSessions()) {
            sendState(session);
        }
    }

    private void sendState(Session session) {
        if (session.isOpen()) {
            String msg = gson.toJson(state.get());
            LOG.debug("sendState {} to session {}", msg, session);
            session.getRemote().sendString(msg, null);
        }
    }

    @Override
    public void sessionAdded(Session session) {
        sendState(session);
    }

    @Override
    public void sessionRemoved(Session session) {
        // nothing to do
    }

    private static class State {

        private final Set<ServerState> servers;

        private State() {
            this(Collections.<ServerState>emptySet());
        }

        private State(Set<ServerState> servers) {
            this.servers = servers;
        }
    }

}
