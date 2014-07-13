package at.valli.savage.master.server;

import at.valli.savage.master.server.file.StateWriterThread;
import at.valli.savage.master.server.network.TCPHandlerThread;
import at.valli.savage.master.server.network.UDPHandlerThread;
import at.valli.savage.master.server.state.ServerStateRegistry;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Created by valli on 13.07.2014.
 */
public final class MasterServer {

    private static final Logger LOG = LogManager.getLogger(MasterServer.class);

    private final ServerStateRegistry stateRegistry = new ServerStateRegistry();
    private final int port;

    private UDPHandlerThread udpHandler;
    private TCPHandlerThread tcpHandler;
    private StateWriterThread datFileWriter;

    public MasterServer(final int port) {
        Validate.inclusiveBetween(0, 65535, port, "invalid port provided");
        this.port = port;
    }

    public void start() {
        try {
            LOG.info("MasterServer starting... Listening at UDP {}", port);
            DatagramSocket udpSocket = new DatagramSocket(port);
            ServerSocket tcpSocket = new ServerSocket(port);
            udpHandler = new UDPHandlerThread(stateRegistry, udpSocket);
            udpHandler.startHandling();
            tcpHandler = new TCPHandlerThread(tcpSocket);
            tcpHandler.startHandling();
            datFileWriter = new StateWriterThread(stateRegistry);
            datFileWriter.startWriting();
            LOG.info("MasterServer started.");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void stop() {
        LOG.info("MasterServer stopping ...");
        udpHandler.stopHandling();
        tcpHandler.stopHandling();
        datFileWriter.stopWriting();
        LOG.info("MasterServer stopped.");
    }


}
