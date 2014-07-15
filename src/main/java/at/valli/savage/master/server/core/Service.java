package at.valli.savage.master.server.core;

/**
 * Created by valli on 15.07.2014.
 */
public interface Service {

    void startup() throws ServiceException;

    void shutdown() throws ServiceException;

}
