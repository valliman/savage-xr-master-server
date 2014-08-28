package at.valli.savage.master.server.core;

/**
 * Created by valli on 15.07.2014.
 */
public class ServiceException extends Exception {

    public ServiceException() {
        super();
    }

    public ServiceException(final String message) {
        super(message);
    }

    public ServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
