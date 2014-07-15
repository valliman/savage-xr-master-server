package at.valli.savage.master.server.core;

import at.valli.savage.master.server.file.StateFileWriter;
import at.valli.savage.master.server.network.TCPService;
import at.valli.savage.master.server.network.UDPService;
import at.valli.savage.master.server.state.ServerStateRegistry;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by valli on 13.07.2014.
 */
public final class MasterServer implements Service {

    private static final Logger LOG = LogManager.getLogger(MasterServer.class);

    private final List<Service> services = new LinkedList<>();

    public MasterServer(final int port) {
        Validate.inclusiveBetween(0, 65535, port, "invalid port provided");

        StateFileWriter stateFileWriter = new StateFileWriter();
        ServerStateRegistry stateRegistry = new ServerStateRegistry(stateFileWriter);
        Service udpHandler = new UDPService(stateRegistry, port);
        Service tcpHandler = new TCPService(port);

        services.add(stateFileWriter);
        services.add(stateRegistry);
        services.add(udpHandler);
        services.add(tcpHandler);
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
