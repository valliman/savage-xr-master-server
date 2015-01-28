package at.valli.savage.master.server.util;

/**
 * Created by valli on 28.01.2015.
 */
public final class Bytes {

    private Bytes() {
        throw new AssertionError("Must not be initialised");
    }

    public static int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }
}
