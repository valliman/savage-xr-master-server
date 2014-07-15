package at.valli.savage.master.server.file;

import at.valli.savage.master.server.state.ServerState;
import at.valli.savage.master.server.state.ServerStateRegistry;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

/**
 * Created by valli on 13.07.2014.
 */
public final class StateFileWriter implements Runnable {

    private static final Logger LOG = LogManager.getLogger(StateFileWriter.class);
    private static final String DAT_FILE_NAME = System.getProperty("dat.file", "gamelist_full.dat");
    private static final byte[] HEADER = {0x7E, 0x41, 0x03, 0x00, 0x00};
    
    private final ServerStateRegistry stateRegistry;

    public StateFileWriter(final ServerStateRegistry stateRegistry) {
        Validate.notNull(stateRegistry, "stateRegistry must not be null");
        this.stateRegistry = stateRegistry;
    }

    @Override
    public void run() {
    	if(stateRegistry.isChanged()) {
	        File datFile = new File(DAT_FILE_NAME);
	        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(1024); 
	        		DataOutputStream stream = new DataOutputStream(baos)) {
	        	
	            LOG.debug("Generating dat file ...");
	            writeHeader(stream);
	            writeServerStates(stream, stateRegistry.getServerStates());
	            stream.flush();
	            Files.copy(new ByteArrayInputStream(baos.toByteArray()), datFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	            LOG.info("New Dat file generated. ");
	        } catch (IOException e) {
	            LOG.error(e.getMessage(), e);
	        }
    	}
    }

    private void writeServerStates(final DataOutputStream stream, final Collection<ServerState> serverStates) throws IOException {
        LOG.debug("Writing dat file server states ...");
        for (ServerState serverState : serverStates) {
            LOG.debug("Writing dat file server state {}  ...", serverState);
            serverState.serialize(stream);
        }
    }

    private void writeHeader(final DataOutputStream stream) throws IOException {
        LOG.debug("Writing dat file header ...");
        stream.write(HEADER);
    }


}
