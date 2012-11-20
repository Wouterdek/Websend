package waterflames.mcpeserver.server.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import waterflames.mcpeserver.server.PacketHandler;

public class ConnectResponse2Packet extends Packet
{
	byte[] serverID;
	short clientUDPPort = 1447;
	short nullPayloadSize = 1447;

	public ConnectResponse2Packet()
	{
	}

	public ConnectResponse2Packet(byte[] serverID)
	{
		this.serverID = serverID;
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException
	{
		stream.write(0x8);
		this.writeMAGIC(stream);
		stream.write(serverID);
		stream.writeShort(clientUDPPort);
		stream.writeShort(1447);
		stream.write(0); // Security
	}

	@Override
	public void readPacket(DataInputStream stream) throws IOException
	{
		stream.read(); // header
		boolean matchingMAGIC = this.readMAGIC(stream);
		stream.read(serverID);
		clientUDPPort = stream.readShort();
		nullPayloadSize = stream.readShort();
		stream.read(); // Security
	}

	@Override
	public void run(PacketHandler handler)
	{
		throw new UnsupportedOperationException("Not supported.");
	}
}
