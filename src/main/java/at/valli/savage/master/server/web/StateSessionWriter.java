package at.valli.savage.master.server.web;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStatesUpdateListener;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by valli on 06.09.2014.
 */
public final class StateSessionWriter implements ServerStatesUpdateListener, SessionListener {

    private static final Logger LOG = LogManager.getLogger(StateSessionWriter.class);
    private final AtomicReference<State> state = new AtomicReference<>(new State());
    private final Gson gson;

    public StateSessionWriter() {
        gson = new GsonBuilder().registerTypeAdapter(ServerState.class, new ServerStateSerializer()).create();
    }

    @Override
    public void stateUpdated(Set<ServerState> serverStates) {
        state.set(new State(serverStates));
        for (Session session : SessionManager.INSTANCE.getSessions()) {
            sendState(session);
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

    private void sendState(Session session) {
        if (session.isOpen()) {
            String msg = gson.toJson(state.get());
            LOG.debug("sendState {} to session {}", msg, session);
            session.getRemote().sendString(msg, null);
        }
    }

    private static final class State {

        @SuppressWarnings("UnusedDeclaration")
        private final Set<ServerState> servers;

        private State() {
            this(Collections.<ServerState>emptySet());
        }

        private State(final Set<ServerState> servers) {
            this.servers = new HashSet<>(servers);
        }
    }

    private static final class ServerStateSerializer implements JsonSerializer<ServerState> {
        @Override
        public JsonElement serialize(ServerState serverState, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("port", serverState.getReceiverAddress().getPort());
            json.addProperty("ip", serverState.getReceiverAddress().getHostString());
            json.addProperty("version", serverState.getVersion());
            return json;
        }
    }
}
