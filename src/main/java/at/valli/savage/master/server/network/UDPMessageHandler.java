package at.valli.savage.master.server.network;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStateRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Created by valli on 13.07.2014.
 */
final class UDPMessageHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger(UDPMessageHandler.class);
    private static final byte[] HEADER = {(byte) 0x9E, 0x4c, 0x23, 0x00, 0x00};
    private static final int SERVER_HEARTBEAT = 0xCA;
    private static final int SERVER_SHUTDOWN = 0xCB;

    private final ServerStateRegistry stateRegistry;
    private final DatagramPacket packet;

    UDPMessageHandler(final ServerStateRegistry stateRegistry, final DatagramPacket packet) {
        this.stateRegistry = stateRegistry;
        this.packet = packet;
    }

    @Override
    public void run() {
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.getData()))) {
            if (isHeaderValid(stream)) {
                int cmd = readCommand(stream);
                if (SERVER_HEARTBEAT == cmd) {
                    ServerState serverState = readServerState(stream);
                    LOG.info("Server heartbeat received: {}", serverState);
                    stateRegistry.add(serverState);
                } else if (SERVER_SHUTDOWN == cmd) {
                    ServerState serverState = readServerState(stream);
                    stateRegistry.remove(serverState);
                    LOG.info("Server shutdown received: {}", serverState);
                } else {
                    LOG.warn("Unknown command received: 0x{}", Integer.toHexString(cmd));
                }
            } else {
                LOG.info("Header invalid, discarding message.");
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private int readCommand(DataInputStream stream) throws IOException {
        return stream.readUnsignedByte();
    }

    private boolean isHeaderValid(DataInputStream stream) throws IOException {
        byte[] head = new byte[5];
        stream.readFully(head, 0, 5);
        return Arrays.equals(HEADER, head);
    }

    private ServerState readServerState(DataInputStream stream) throws IOException {
        int version = stream.readByte();
        byte[] ip = new byte[4];
        stream.readFully(ip, 0, 4);
        short port = Short.reverseBytes((short) stream.readUnsignedShort());
        return new ServerState(version, ip, port);
    }
}
