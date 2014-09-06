package at.valli.savage.master.server.network;

import at.valli.savage.master.server.core.Service;
import at.valli.savage.master.server.core.ServiceException;
import at.valli.savage.master.server.util.FutureEvaluator;
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
public final class TCPService extends Thread implements Service {

    private static final Logger LOG = LogManager.getLogger(TCPService.class);
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("TCPMessageHandler-%d").build();

    private final AtomicBoolean started = new AtomicBoolean();
    private final int port;
    private ServerSocket socket;

    private ExecutorService executorService;

    public TCPService(final int port) {
        this.port = port;
        setName("TCPService");
    }

    @Override
    public void startup() throws ServiceException {
        if (started.get()) {
            throw new IllegalStateException("TCPService already started ...");
        } else {
            try {
                LOG.info("Starting TCPService ...");
                executorService = Executors.newCachedThreadPool(NAMED_THREAD_FACTORY);
                socket = new ServerSocket(port);
                LOG.info("Listening at TCP port {}.", port);
                started.set(true);
                this.start();
            } catch (IOException e) {
                throw new ServiceException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void shutdown() {
        LOG.info("Stopping TCPService ...");
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
                // exception when shutting down is caught
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
