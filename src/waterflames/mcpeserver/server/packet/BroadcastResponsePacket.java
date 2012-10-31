package waterflames.mcpeserver.server.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import waterflames.mcpeserver.server.PacketHandler;

public class BroadcastResponsePacket extends Packet{
    byte[] serverID;
    long ping = 0x0;
    boolean equalMagic;
    String string = "";
    byte header = 0x1c;

    public BroadcastResponsePacket() {
    }

    public BroadcastResponsePacket(long ping, byte[] serverID, String string) {
        this.ping = ping;
        this.serverID = serverID;
        this.string = string;
    }
    
    @Override
    public void sendPacket(DataOutputStream stream) throws IOException {
        stream.write(header);
        stream.writeLong(ping);
        stream.write(serverID);
        this.writeMAGIC(stream);
        Packet.writeString(stream, string);
    }

    @Override
    public void readPacket(DataInputStream stream) throws IOException {
        stream.read();
        ping = stream.readLong();
        serverID = new byte[8];
        stream.read(serverID);
        equalMagic = this.readMAGIC(stream);
        short stringLenght = stream.readShort();
        for(int i = 0;i<stringLenght;i++){
            string += stream.readChar();
        }
    }
    
    public void setAlternativeHeader(boolean bool){
        if(bool){
            header = 0x1D;
        }else{
            header = 0x1C;
        }
    }

    @Override
    public void run(PacketHandler handler) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
