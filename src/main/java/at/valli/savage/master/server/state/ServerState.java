package at.valli.savage.master.server.state;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by valli on 13.07.2014.
 */
public final class ServerState {

    private final int version;
    private final String ip;
    private final int port;
    private final long time;

    public ServerState(final int version, final String ip, final int port) {
        Validate.notNull(ip, "ip must not be null");
        this.version = version;
        this.ip = ip;
        this.port = port;
        this.time = System.nanoTime();
    }

    public long getTime() {
        return time;
    }

    public int getVersion() {
        return version;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
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
