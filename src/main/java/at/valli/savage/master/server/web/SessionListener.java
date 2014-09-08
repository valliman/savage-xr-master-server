package at.valli.savage.master.server.web;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by valli on 06.09.2014.
 */
public interface SessionListener {

    void sessionAdded(final Session session);

    void sessionRemoved(final Session session);
}
