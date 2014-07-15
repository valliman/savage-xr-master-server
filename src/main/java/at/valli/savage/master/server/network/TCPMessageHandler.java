package at.valli.savage.master.server.network;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by valli on 13.07.2014.
 */
final class TCPMessageHandler implements Runnable {

    private static final Logger LOG = LogManager.getLogger(TCPMessageHandler.class);

    private static final String SERVER_FIREWALL_TEST = "FIREWALL_TEST";
    private static final String SV_HI = "SV_HI";
    private static final String KS_NOT_FIREWALLED = "KS_NOTFIREWALLED";
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final byte[] KS_HI = "KS_HI".getBytes(UTF_8);

    private final Socket socket;

    TCPMessageHandler(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String host = null;
        int port = 0;

        try (Socket closeable = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(closeable.getInputStream()))
        ) {
            while (!in.ready()) {
                Thread.sleep(100L);
            }

            String message = in.readLine();
            String[] tokens = message.split(" ");
            if (SERVER_FIREWALL_TEST.equals(tokens[0])) {
                host = closeable.getInetAddress().getHostAddress();
                if( tokens.length >= 2 && isPortValid(Integer.valueOf(tokens[1]))) {
                	LOG.info("Request to test port {} from {} received.", host, port);
                    runPortTest(host, Integer.valueOf(tokens[1]));  //ya, I parsed it twice. Wanna make sumfin of it bruv?
                }
            } else {
                LOG.warn("Unknown message received: {}", message);
            }
        } catch (IOException | NumberFormatException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.debug("Thread interrupted.", e);
        } catch (IllegalArgumentException e) {
        	LOG.error(e.getMessage(), e);
        }

        if (isPortValid(port) && host != null) {
            LOG.info("Request to test port {} from {} received.", host, port);
            runPortTest(host, port);
        } else {
            LOG.warn("Invalid data received.");
        }
    }

    private void runPortTest(final String host, final int port) {
        try {
            LOG.info("Starting port test for {}:{} ...", host, port);
            String cookie = requestCookie(host, port);
            if (cookie == null) {
                LOG.warn("Port test for {}:{} failed. Cookie was null.", host, port);
            } else {
                sendNotFirewalled(host, port, cookie);
                LOG.info("Port test for port {}:{} successful.", host, port);
            }
        } catch (IOException e) {
            LOG.warn("Port test for port {}:{}  failed.", host, port, e);
        } catch (InterruptedException e) {
            LOG.debug("Thread interrupted.", e);
        }
    }

    private String requestCookie(final String host, final int port) throws IOException, InterruptedException {
        String cookie = null;
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            LOG.debug("Requesting cookie from server ...");
            out.write(KS_HI);
            out.flush();

            while (!in.ready()) {
                Thread.sleep(100L);
            }

            String message = in.readLine();
            String[] tokens = message.split(" ");
            if (tokens.length >= 2 && SV_HI.equals(tokens[0])) {
                LOG.debug("Server cookie received.");
                cookie = tokens[1];
            } else {
                LOG.warn("Unknown message received: {}", message);
            }
        }
        return cookie;
    }

    private void sendNotFirewalled(final String host, final int port, final String cookie) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            LOG.debug("Sending KS_NOTFIREWALLED command ...");
            out.write(KS_NOT_FIREWALLED + ' ' + cookie + ' ');
            out.flush();
            LOG.debug("KS_NOTFIREWALLED command sent.");
        }
    }

    private boolean isPortValid(int port) throws IllegalArgumentException {
    	Validate.inclusiveBetween(1024, 65535, port, "Not a valid port");
    	//ports below 1024 are reserved usually for services run by the system.
    	//someone asking for a port below that might be up to something devious
        return true;
    }
}
