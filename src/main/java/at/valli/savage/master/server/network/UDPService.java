package at.valli.savage.master.server.network;

import at.valli.savage.master.server.core.Service;
import at.valli.savage.master.server.core.ServiceException;
import at.valli.savage.master.server.state.ServerStateRegistry;
import at.valli.savage.master.server.util.FutureEvaluator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by valli on 13.07.2014.
 */
public final class UDPService extends Thread implements Service {

    private static final Logger LOG = LogManager.getLogger(UDPService.class);
    private static final int UDP_RECEIVE_BUFFER_SIZE = 1024;
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("UDPMessageHandler-%d").build();

    private final AtomicBoolean started = new AtomicBoolean();
    private final ServerStateRegistry stateRegistry;
    private final int port;

    private ExecutorService executorService;
    private DatagramSocket socket;

    public UDPService(final ServerStateRegistry stateRegistry, final int port) {
        Validate.notNull(stateRegistry, "stateRegistry must not be null");
        this.stateRegistry = stateRegistry;
        this.port = port;
        setName("UDPService");
    }

    @Override
    public void startup() throws ServiceException {
        if (started.get()) {
            throw new IllegalStateException("UDPService already started ...");
        } else {
            LOG.debug("Starting UDPService ...");
            try {
                executorService = Executors.newCachedThreadPool(NAMED_THREAD_FACTORY);
                socket = new DatagramSocket(port);
                LOG.info("Listening at UDP port {}.", port);
                started.set(true);
                this.start();
            } catch (SocketException e) {
                throw new ServiceException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void shutdown() {
        LOG.debug("Stopping UDPService ...");
        started.set(false);
        executorService.shutdown();
        socket.close();
    }

    @Override
    public void run() {
        final byte[] buf = new byte[UDP_RECEIVE_BUFFER_SIZE];
        while (started.get()) {
            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                LOG.debug("Waiting for data");
                socket.receive(packet);
                LOG.debug("Received data from " + packet.getAddress().getHostAddress());
            } catch (IOException e) {
                if (started.get()) {
                    LOG.error(e.getMessage(), e);
                }
                // exception when shutting down is caught
            }
            processNewRequest(packet);
        }
    }

    private void processNewRequest(final DatagramPacket packet) {
        if (!executorService.isShutdown()) {
            Future<?> task = executorService.submit(new UDPMessageHandler(stateRegistry, packet));
            new FutureEvaluator(task).start();
        }
    }
}
