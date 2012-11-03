package waterflames.mcpeserver.server.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import waterflames.mcpeserver.server.PacketHandler;

public class ConnectResponse1Packet extends Packet{
    byte[] serverID;
    short nullPayloadSize = 1447;

    public ConnectResponse1Packet() {
    }
    
    public ConnectResponse1Packet(byte[] serverID) {
        this.serverID = serverID;
    }
    
    @Override
    public void sendPacket(DataOutputStream stream) throws IOException {
        stream.write(0x6);
        this.writeMAGIC(stream);
        stream.write(serverID);
        stream.write(0x0);
        stream.writeShort(1447);
    }

    @Override
    public void readPacket(DataInputStream stream) throws IOException {
        stream.read(); //header
        boolean matchingMAGIC = this.readMAGIC(stream);
        stream.read(serverID);
        stream.read();
        nullPayloadSize = stream.readShort();
    }

    @Override
    public void run(PacketHandler handler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
