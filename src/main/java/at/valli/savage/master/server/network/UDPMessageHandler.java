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

    private static final byte[] HEADER = { (byte) 0x9E, 0x4c, 0x23, 0x00, 0x00 };

    private static final int CPROTO_HEARTBEAT = 0xCA;
    private static final int CPROTO_SERVER_SHUTDOWN = 0xCB;
    //this one is defined with the others in net.h under "heartbeat packet" but I don't think it was ever used
    //private static final int CPROTO_PLAYER_DISCONNECT = 0xCC;
    
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
                switch(cmd) {
                	case CPROTO_HEARTBEAT:
                	{
                		ServerState serverState = readServerState(stream);
                        LOG.info("Server heartbeat received: {}", serverState);
                        stateRegistry.add(serverState);
                		} break;
                	case CPROTO_SERVER_SHUTDOWN:
                	{
                		ServerState serverState = readServerState(stream);
                        stateRegistry.remove(serverState);
                        LOG.info("Server shutdown received: {}", serverState);
                		}break;
                	default:
                		LOG.warn("Unknown command received: 0x{}", Integer.toHexString(cmd));
                		break;
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
    	byte[] ip = new byte[4];
        int version = stream.readByte();
        stream.readFully(ip, 0, 4);
        int port = Short.reverseBytes((short) stream.readUnsignedShort());
        return new ServerState(version, ip, port);
    }
}
