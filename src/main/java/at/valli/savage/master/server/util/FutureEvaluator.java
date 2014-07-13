package at.valli.savage.master.server.util;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by valli on 13.07.2014.
 */
public final class FutureEvaluator extends Thread {

    private static final Logger LOG = LogManager.getLogger(FutureEvaluator.class);

    private Future<?> future;

    public FutureEvaluator(final Future<?> future) {
        Validate.notNull(future, "future must not be null");
        this.future = future;
    }

    @Override
    public void run() {
        try {
            future.get();
        } catch (CancellationException e) {
            // do nothing
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
