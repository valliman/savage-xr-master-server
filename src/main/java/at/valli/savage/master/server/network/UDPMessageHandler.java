package at.valli.savage.master.server.network;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStateRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Created by valli on 13.07.2014.
 */
final class UDPMessageHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger(UDPMessageHandler.class);

    private static final int HEARTBEAT_CMD = 0xCA;
    private static final int HEARTBEAT_SERVER_SHUTDOWN = 0xCB;

    private final ServerStateRegistry stateRegistry;
    private final DatagramPacket packet;

    UDPMessageHandler(ServerStateRegistry stateRegistry, DatagramPacket packet) {
        this.stateRegistry = stateRegistry;
        this.packet = packet;
    }

    private static int toHexValue(byte b) {
        return (b & 0xff);
    }

    @Override
    public void run() {
        byte[] data = packet.getData();
        if (data.length >= 13) {
            if (toHexValue(data[5]) == HEARTBEAT_CMD) {
                ServerState serverState = determineServerState(data);
                LOG.info("Server Heartbeat received: {}", serverState);
                stateRegistry.add(serverState);
            } else if (toHexValue(data[5]) == HEARTBEAT_SERVER_SHUTDOWN) {
                ServerState serverState = determineServerState(data);
                stateRegistry.remove(serverState);
                LOG.info("Server Shutdown received: {}", serverState);
            } else {
                LOG.warn("Unknown message received: {}", Arrays.toString(data));
            }
        } else {
            LOG.warn("Malformed message received: {}", Arrays.toString(data));
        }
    }

    private ServerState determineServerState(byte[] data) {
        int version = data[6];
        String ip = toHexValue(data[7]) + "." + toHexValue(data[8]) + "." + toHexValue(data[9]) + "." + toHexValue(data[10]);
        int port = toHexValue(data[11]) + toHexValue(data[12]) * 256;

        return new ServerState(version, ip, port);
    }
}
