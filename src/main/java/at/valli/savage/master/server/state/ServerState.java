package at.valli.savage.master.server.state;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;

/**
 * Created by valli on 13.07.2014.
 */
public final class ServerState {

    private final InetSocketAddress senderAddress;
    private final InetSocketAddress receiverAddress;
    private final int version;
    private final long time;

    public ServerState(final InetSocketAddress senderAddress, final InetSocketAddress logicalAddress, final int version) {
        Validate.notNull(senderAddress, "senderAddress must not be null");
        Validate.notNull(logicalAddress, "receiverAddress must not be null");
        this.senderAddress = senderAddress;
        this.receiverAddress = logicalAddress;
        this.version = version;
        this.time = System.nanoTime();
    }

    public long getTime() {
        return time;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getRawIp() {
        return receiverAddress.getAddress().getAddress();
    }

    public int getPort() {
        return receiverAddress.getPort();
    }

    public InetSocketAddress getSenderAddress() {
        return senderAddress;
    }

    public InetSocketAddress getReceiverAddress() {
        return receiverAddress;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("senderAddress", senderAddress)
                .append("receiverAddress", receiverAddress)
                .append("version", version)
                .toString();
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
                .append(receiverAddress, rhs.receiverAddress)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(receiverAddress).toHashCode();
    }
}
