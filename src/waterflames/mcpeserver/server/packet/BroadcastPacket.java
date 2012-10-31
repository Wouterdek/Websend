package waterflames.mcpeserver.server.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import waterflames.mcpeserver.server.PacketHandler;

public class BroadcastPacket extends Packet{
    long pingID;
    boolean equalMAGIC;

    public BroadcastPacket() {
    }
    
    public BroadcastPacket(long pingID) {
        this.pingID = pingID;
    }
    
    @Override
    public void sendPacket(DataOutputStream stream) throws IOException {
        stream.write(0x2);
        stream.writeLong(pingID);
        this.writeMAGIC(stream);
    }

    @Override
    public void readPacket(DataInputStream stream) throws IOException {
        stream.read(); //Header
        pingID = stream.readLong();
        equalMAGIC = this.readMAGIC(stream);
    }

    //GETTERS AND SETTERS
    
    public long getPingID() {
        return pingID;
    }

    public void setPingID(long pingID) {
        this.pingID = pingID;
    }

    public boolean isEqualMAGIC() {
        return equalMAGIC;
    }

    @Override
    public void run(PacketHandler handler) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
