package at.valli.savage.master.server.core;

import at.valli.savage.master.server.file.StateFileWriter;
import at.valli.savage.master.server.network.TCPService;
import at.valli.savage.master.server.network.UDPService;
import at.valli.savage.master.server.state.ServerStateRegistry;
import at.valli.savage.master.server.state.ServerStatesUpdateListener;
import at.valli.savage.master.server.web.SessionManager;
import at.valli.savage.master.server.web.StateSessionWriter;
import at.valli.savage.master.server.web.WebSocketService;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by valli on 13.07.2014.
 */
public final class MasterServer implements Service {

    private static final Logger LOG = LogManager.getLogger(MasterServer.class);

    private final List<Service> services = new LinkedList<>();

    public MasterServer(final int listeningPort, final int websocketPort) {
        Validate.inclusiveBetween(0, 65535, listeningPort, "invalid port provided");
        Validate.inclusiveBetween(0, 65535, websocketPort, "invalid port provided");

        StateSessionWriter stateSessionWriter = new StateSessionWriter();
        StateFileWriter stateFileWriter = new StateFileWriter();

        Set<ServerStatesUpdateListener> updateListeners = new HashSet<>();
        updateListeners.add(stateSessionWriter);
        updateListeners.add(stateFileWriter);

        SessionManager.INSTANCE.addListener(stateSessionWriter);
        ServerStateRegistry stateRegistry = new ServerStateRegistry(updateListeners);
        Service udpHandler = new UDPService(stateRegistry, listeningPort);
        Service tcpHandler = new TCPService(listeningPort);
        Service wsHandler = new WebSocketService(websocketPort);

        services.add(stateFileWriter);
        services.add(stateRegistry);
        services.add(udpHandler);
        services.add(tcpHandler);
        services.add(wsHandler);
    }

    @Override
    public void startup() {
        LOG.info("MasterServer starting...");
        for (Service service : services) {
            try {
                service.startup();
            } catch (ServiceException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        LOG.info("MasterServer started.");
    }

    @Override
    public void shutdown() {
        LOG.info("MasterServer stopping ...");
        for (Service service : services) {
            try {
                service.shutdown();
            } catch (ServiceException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        LOG.info("MasterServer stopped.");
    }


}
