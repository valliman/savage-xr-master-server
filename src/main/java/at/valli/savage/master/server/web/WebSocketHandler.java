package at.valli.savage.master.server.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Created by valli on 06.09.2014.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public final class WebSocketHandler {

    private static final Logger LOG = LogManager.getLogger(WebSocketHandler.class);

    @OnWebSocketConnect
    public void onOpen(Session session) {
        LOG.debug("onOpen {}", session);
        SessionManager.INSTANCE.addSession(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        LOG.debug("onClose {}", session);
        SessionManager.INSTANCE.removeSession(session);
    }
}

