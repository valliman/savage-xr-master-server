package at.valli.savage.master.server.network;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStateRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by valli on 13.07.2014.
 */
final class UDPMessageHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger(UDPMessageHandler.class);

    private static final int HEADER_0 = 0x9E;
    private static final int HEADER_1 = 0x4C;
    private static final int HEADER_2 = 0x23;
    private static final int HEADER_3 = 0x00;
    private static final int HEADER_4 = 0x00;

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
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.getData()));
        try {
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
        int header0 = stream.readUnsignedByte();
        int header1 = stream.readUnsignedByte();
        int header2 = stream.readUnsignedByte();
        int header3 = stream.readUnsignedByte();
        int header4 = stream.readUnsignedByte();
        return HEADER_0 == header0 && HEADER_1 == header1 && HEADER_2 == header2 && HEADER_3 == header3 && HEADER_4 == header4;
    }

    private ServerState readServerState(DataInputStream stream) throws IOException {
        int version = stream.readByte();
        int ipSegment1 = stream.readUnsignedByte();
        int ipSegment2 = stream.readUnsignedByte();
        int ipSegment3 = stream.readUnsignedByte();
        int ipSegment4 = stream.readUnsignedByte();
        String ip = ipSegment1 + "." + ipSegment2 + "." + ipSegment3 + "." + ipSegment4;
        int port = Short.reverseBytes((short) stream.readUnsignedShort());
        return new ServerState(version, ip, port);
    }
}
