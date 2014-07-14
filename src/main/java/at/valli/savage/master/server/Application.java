package at.valli.savage.master.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by valli on 13.07.2014.
 */
public class Application {

    private static final Logger LOG = LogManager.getLogger(Application.class);
    private static final int UDP_LISTENING_PORT = Integer.getInteger("listening.port", 11236);

    public static void main(final String[] args) {
        MasterServer masterServer = new MasterServer(UDP_LISTENING_PORT);
        masterServer.start();
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        masterServer.stop();
    }
}
