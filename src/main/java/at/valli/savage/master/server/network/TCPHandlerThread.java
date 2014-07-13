package at.valli.savage.master.server.network;

import at.valli.savage.master.server.util.FutureEvaluator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by valli on 13.07.2014.
 */
public final class TCPHandlerThread extends Thread {

    private static final Logger LOG = LogManager.getLogger(TCPHandlerThread.class);
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("TCPHandlerThread-%d").build();

    private final AtomicBoolean started = new AtomicBoolean();
    private final ServerSocket socket;

    private ExecutorService executorService;

    public TCPHandlerThread(final ServerSocket socket) {
        Validate.notNull(socket, "socket must not be null");
        this.socket = socket;
    }

    public void startHandling() {
        if (started.get()) {
            throw new IllegalStateException("TCPHandlerThread already started ...");
        } else {
            LOG.debug("Starting TCPHandlerThread ...");
            started.set(true);
            executorService = Executors.newCachedThreadPool(NAMED_THREAD_FACTORY);
            this.start();
        }
    }

    public void stopHandling() {
        LOG.debug("Stopping TCPHandlerThread ...");
        started.set(false);
        executorService.shutdown();
        try {
            socket.close();
        } catch (IOException e) {
            // ignoring exception when closing
        }
    }

    @Override
    public void run() {
        while (started.get()) {
            try {
                LOG.debug("Waiting for data");
                Socket message = socket.accept();
                LOG.debug("Received data from " + socket.getInetAddress().getHostAddress());
                processNewRequest(message);
            } catch (IOException e) {
                // exception when shutting down is catched
                if (started.get()) {
                    LOG.error(e.getMessage(), e);
                }
            }

        }
    }

    private void processNewRequest(final Socket socket) {
        if (!executorService.isShutdown()) {
            Future<?> task = executorService.submit(new TCPMessageHandler(socket));
            new FutureEvaluator(task).start();
        }
    }
}
