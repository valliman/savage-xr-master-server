package at.valli.savage.master.server.file;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStateRegistry;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by valli on 13.07.2014.
 */
final class FileStateWriter implements Runnable {

    private static final Logger LOG = LogManager.getLogger(FileStateWriter.class);
    private static final String DAT_FILE_NAME = System.getProperty("dat.file", "gamelist_full.dat");
    private static final String TEMP_FILE_NAME = "serverstates.temp";
    private static final long SERVER_STATE_TIMEOUT = TimeUnit.SECONDS.toNanos(Long.getLong("server.state.max.storage.seconds", 90L));

    private final ServerStateRegistry stateRegistry;

    FileStateWriter(final ServerStateRegistry stateRegistry) {
        Validate.notNull(stateRegistry, "stateRegistry must not be null");
        this.stateRegistry = stateRegistry;
    }

    @Override
    public void run() {
        File tempFile = new File(TEMP_FILE_NAME);
        File datFile = new File(DAT_FILE_NAME);
        try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(tempFile, false))) {
            LOG.debug("Generating dat file ...");
            writeHeader(stream);
            writeServerStates(stream, stateRegistry.getServerStates());
            stream.flush();
            writeDatFile(tempFile, datFile);
            LOG.info("New Dat file generated. ");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Path writeDatFile(File tempFile, File datFile) throws IOException {
        LOG.debug("Copying working copy {} to dat file {} ...", tempFile.getAbsoluteFile(), datFile.getAbsoluteFile());
        return Files.copy(tempFile.toPath(), datFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeServerStates(final DataOutputStream stream, final Collection<ServerState> serverStates) throws IOException {
        LOG.debug("Writing dat file server states ...");
        for (ServerState serverState : serverStates) {
            if (serverTimeout(serverState)) {
                stateRegistry.remove(serverState);
            } else {
                writeServerState(stream, serverState);
            }
        }
    }

    private boolean serverTimeout(ServerState serverState) {
        return (System.nanoTime() - serverState.getTime()) > SERVER_STATE_TIMEOUT;
    }

    private void writeHeader(final DataOutputStream stream) throws IOException {
        LOG.debug("Writing dat file header ...");
        stream.writeByte(0x7E);
        stream.writeByte(0x41);
        stream.writeByte(0x03);
        stream.writeByte(0x00);
        stream.writeByte(0x00);
    }

    private void writeServerState(final DataOutputStream stream, final ServerState state) throws IOException {
        LOG.debug("Writing dat file server state {}  ...", state);
        String[] segments = state.getIp().split("\\.");
        stream.writeByte(Integer.valueOf(segments[0]).byteValue());
        stream.writeByte(Integer.valueOf(segments[1]).byteValue());
        stream.writeByte(Integer.valueOf(segments[2]).byteValue());
        stream.writeByte(Integer.valueOf(segments[3]).byteValue());

        String port = Integer.toHexString(state.getPort());
        stream.writeByte(Integer.valueOf(port.substring(2, 4), 16).byteValue());
        stream.writeByte(Integer.valueOf(port.substring(0, 2), 16).byteValue());
    }


}
