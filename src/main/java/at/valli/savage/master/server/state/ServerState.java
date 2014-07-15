package at.valli.savage.master.server.state;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;

/**
 * Created by valli on 13.07.2014.
 */
public final class ServerState {

    private final int version;
    private final byte[] rawIp;
    private final String ip;
    private final short port;
    private final long time;

    public ServerState(final int version, final byte[] rawIp, final short port) {
        Validate.notNull(rawIp, "rawIp must not be null");
        this.version = version;
        this.rawIp = Arrays.copyOf(rawIp, rawIp.length);
        this.ip = toIp(rawIp);
        this.port = port;
        this.time = System.nanoTime();
    }

    private static String toIp(byte[] rawIp) {
        return Byte.toUnsignedInt(rawIp[0]) + "." + Byte.toUnsignedInt(rawIp[1]) + "." + Byte.toUnsignedInt(rawIp[2]) + "." + Byte.toUnsignedInt(rawIp[3]);
    }

    public long getTime() {
        return time;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getRawIp() {
        return rawIp;
    }

    public short getPort() {
        return port;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("version", version).append("host", ip).append("port", port).toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ServerState rhs = (ServerState) obj;
        return new EqualsBuilder()
                .append(ip, rhs.ip)
                .append(port, rhs.port)
                .append(version, rhs.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ip).append(port).append(version).toHashCode();
    }
}
