package at.valli.savage.master.server.web;

import at.valli.savage.master.server.core.Service;
import at.valli.savage.master.server.core.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

/**
 * Created by valli on 06.09.2014.
 */
public final class WebSocketService implements Service {

    private static final Logger LOG = LogManager.getLogger(WebSocketService.class);

    private final Server server;
    private final int port;

    public WebSocketService(final int port) {
        configureLogging();
        this.port = port;
        server = new Server(port);
        server.setHandler(new org.eclipse.jetty.websocket.server.WebSocketHandler.Simple(WebSocketHandler.class));
    }

    @Override
    public void startup() throws ServiceException {
        try {
            LOG.info("Starting WS Service ...");
            server.start();
            LOG.info("Opening WS at port {}.", port);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() throws ServiceException {
        try {
            LOG.info("Stopping WS Service ...");
            server.stop();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private void configureLogging() {
        try {
            org.eclipse.jetty.util.log.Log.setLog(new org.eclipse.jetty.util.log.Slf4jLog());
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }
}
