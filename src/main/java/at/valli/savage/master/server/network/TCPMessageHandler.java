package at.valli.savage.master.server.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by valli on 13.07.2014.
 */
final class TCPMessageHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger(TCPMessageHandler.class);

    private static final String SERVER_FIREWALL_TEST = "FIREWALL_TEST";

    private final Socket socket;

    TCPMessageHandler(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader stream = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (!stream.ready()) {
                Thread.sleep(100L);
            }

            String message = stream.readLine();
            String[] tokens = message.split(" ");
            if (SERVER_FIREWALL_TEST.equals(tokens[0])) {
                int port = Integer.valueOf(tokens[1]);
                if (isPortValid(port)) {
                    LOG.info("Request to test port {} from {} received.", port, socket.getInetAddress().getHostAddress());
                } else {
                    LOG.warn("Invalid message received: {}", message);
                }
            } else {
                LOG.warn("Unknown message received: {}", message);
            }
        } catch (NumberFormatException e) {
            LOG.warn(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            // do nothing when interrupted
        }
    }

    private boolean isPortValid(int port) {
        return port > 0 && port < 65536;
    }
}
