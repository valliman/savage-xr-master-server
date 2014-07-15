package at.valli.savage.master.server.file;

import at.valli.savage.master.server.core.Service;
import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStatesUpdateListener;
import at.valli.savage.master.server.util.FutureEvaluator;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by valli on 13.07.2014.
 */
public final class StateFileWriter implements Service, ServerStatesUpdateListener {

    private static final Logger LOG = LogManager.getLogger(StateFileWriter.class);
    private static final String DAT_FILE_NAME = System.getProperty("dat.file", "gamelist_full.dat");
    private static final long FILE_WRITING_INTERVAL_SECONDS = Long.getLong("dat.file.writing.interval.seconds", 60L);
    private static final byte[] HEADER = {0x7E, 0x41, 0x03, 0x00, 0x00};
    private static final BasicThreadFactory NAMED_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("FileWriterTask-%d").build();

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean stateChanged = new AtomicBoolean(true);
    private final AtomicReference<Set<ServerState>> state = new AtomicReference<Set<ServerState>>(new HashSet<ServerState>());

    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> writeTask;

    private void writeServerStates(final DataOutputStream stream, final Collection<ServerState> serverStates) throws IOException {
        LOG.debug("Writing dat file server states ...");
        for (ServerState serverState : serverStates) {
            LOG.debug("Writing dat file server state {}  ...", serverState);
            stream.write(serverState.getRawIp());
            stream.writeShort(Short.reverseBytes(serverState.getPort()));
        }
    }

    private void writeHeader(final DataOutputStream stream) throws IOException {
        LOG.debug("Writing dat file header ...");
        stream.write(HEADER);
    }

    @Override
    public void stateUpdated(final Set<ServerState> registryState) {
        stateChanged.set(true);
        state.set(new HashSet<>(registryState));
    }

    @Override
    public void startup() {
        if (started.get()) {
            throw new IllegalStateException("TCPService already started ...");
        } else {
            LOG.info("Starting registry services ...");
            executorService = Executors.newScheduledThreadPool(1, NAMED_THREAD_FACTORY);
            writeTask = executorService.scheduleAtFixedRate(new StateFileWriterTask(), 0L, FILE_WRITING_INTERVAL_SECONDS, TimeUnit.SECONDS);
            new FutureEvaluator(writeTask).start();
            started.set(true);
        }
    }

    @Override
    public void shutdown() {
        LOG.info("Stopping registry services ...");
        writeTask.cancel(true);
        executorService.shutdown();
        started.set(false);
    }

    private class StateFileWriterTask implements Runnable {

        @Override
        public void run() {
            if (stateChanged.compareAndSet(true, false)) {
                LOG.debug("Generating dat file ...");
                File datFile = new File(DAT_FILE_NAME);
                Set<ServerState> serverStates = state.get();
                int fileSize = 5 + serverStates.size() * 6;
                try (ByteArrayOutputStream out = new ByteArrayOutputStream(fileSize);
                     DataOutputStream stream = new DataOutputStream(out)) {
                    writeHeader(stream);
                    writeServerStates(stream, serverStates);
                    stream.flush();
                    Files.copy(new ByteArrayInputStream(out.toByteArray()), datFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOG.info("New Dat file generated. ");
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }


}
