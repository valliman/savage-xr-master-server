package at.valli.savage.master.server.web;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by valli on 06.09.2014.
 */
public enum SessionManager {

    INSTANCE;

    private final List<Session> sessions = new CopyOnWriteArrayList<>();
    private final ConcurrentHashSet<SessionListener> sessionListeners = new ConcurrentHashSet<>();

    public List<Session> getSessions() {
        return new ArrayList<>(sessions);
    }

    public void addSession(final Session session) {
        sessions.add(session);
        notifySessionAdded(session);
    }

    public void removeSession(final Session session) {
        sessions.remove(session);
        notifySessionRemoved(session);
    }

    public void addListener(final SessionListener listener) {
        sessionListeners.add(listener);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void removeListener(final SessionListener listener) {
        sessionListeners.remove(listener);
    }

    private void notifySessionAdded(final Session session) {
        for (SessionListener listener : new HashSet<>(sessionListeners)) {
            listener.sessionAdded(session);
        }
    }

    private void notifySessionRemoved(final Session session) {
        for (SessionListener listener : new HashSet<>(sessionListeners)) {
            listener.sessionRemoved(session);
        }
    }
}
