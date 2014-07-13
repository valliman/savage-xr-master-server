package at.valli.savage.master.server.network;

import at.valli.savage.master.server.state.ServerStateRegistry;
import at.valli.savage.master.server.util.FutureEvaluator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by valli on 13.07.2014.
 */
public final class UDPHandlerThread extends Thread {

    private static final Logger LOG = LogManager.getLogger(UDPHandlerThread.class);
    private static final int UDP_RECEIVE_BUFFER_SIZE = 1024;
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("UDPHandlerThread-%d").build();

    private final AtomicBoolean started = new AtomicBoolean();
    private final DatagramSocket socket;
    private final ServerStateRegistry stateRegistry;

    private ExecutorService executorService;

    public UDPHandlerThread(final ServerStateRegistry stateRegistry, final DatagramSocket socket) {
        Validate.notNull(stateRegistry, "stateRegistry must not be null");
        Validate.notNull(socket, "socket must not be null");
        this.stateRegistry = stateRegistry;
        this.socket = socket;
    }

    public void startHandling() {
        if (started.get()) {
            throw new IllegalStateException("UDPHandlerThread already started ...");
        } else {
            LOG.debug("Starting UDPHandlerThread ...");
            started.set(true);
            executorService = Executors.newCachedThreadPool(NAMED_THREAD_FACTORY);
            this.start();
        }
    }

    public void stopHandling() {
        LOG.debug("Stopping UDPHandlerThread ...");
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
                // exception when shutting down is catched
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
