package waterflames.mcpeserver.server.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import waterflames.mcpeserver.Main;
import waterflames.mcpeserver.server.PacketHandler;

public class ConnectRequest2Packet extends Packet{
    byte[] securityAndCookie = new byte[5]; //0x043f57fefd
    short serverUDPPort = 19132;
    short nullPayloadSize = 1464;
    long clientID;
    
    @Override
    public void sendPacket(DataOutputStream stream) throws IOException {
        stream.write(0x7);
        this.writeMAGIC(stream);
        stream.write(this.securityAndCookie);
        stream.writeShort(this.serverUDPPort);
        stream.writeShort(this.nullPayloadSize);
        stream.writeLong(this.clientID);
    }

    @Override
    public void readPacket(DataInputStream stream) throws IOException {
        stream.read(); //header
        boolean matchingMAGIC = this.readMAGIC(stream);
        stream.read(securityAndCookie);
        this.serverUDPPort = stream.readShort();
        this.nullPayloadSize = stream.readShort();
        this.clientID = stream.readLong();
    }
    
    public long getClientID() {
        return this.clientID;
    }

    @Override
    public void run(PacketHandler handler) {
        try {
            handler.handleConnectRequest2(this);
        } catch (IOException ex) {
            Main.handleException(ex);
        }
    }
}
