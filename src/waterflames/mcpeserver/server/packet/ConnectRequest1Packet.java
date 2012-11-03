package waterflames.mcpeserver.server.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import waterflames.mcpeserver.server.PacketHandler;

public class ConnectRequest1Packet extends Packet{
    private static byte[] nullPayload = new byte[1447];
    byte protocol = 0x5;
    
    @Override
    public void sendPacket(DataOutputStream stream) throws IOException {
        stream.write(0x5);
        this.writeMAGIC(stream);
        stream.write(protocol);
        
        if(nullPayload[0] != 0x0){
            for(int i = 0;i<nullPayload.length;i++){
                nullPayload[i] = 0x0;
            }
        }
        stream.write(nullPayload);
    }

    @Override
    public void readPacket(DataInputStream stream) throws IOException {
        stream.read(); //header
        boolean matchingMAGIC = this.readMAGIC(stream);
        protocol = stream.readByte();
    }

    public byte getProtocolVersion() {
        return protocol;
    }

    @Override
    public void run(PacketHandler handler) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
