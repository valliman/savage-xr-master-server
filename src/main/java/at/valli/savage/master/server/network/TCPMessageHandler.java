package at.valli.savage.master.server.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by valli on 13.07.2014.
 */
final class TCPMessageHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger(TCPMessageHandler.class);

    private final Socket socket;

    TCPMessageHandler(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream stream = new DataInputStream(socket.getInputStream());
            byte[] buf = new byte[1024];
            do {
                LOG.debug("Received the following message: {}", Arrays.toString(buf));
            } while (stream.read(buf) == 1024);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
