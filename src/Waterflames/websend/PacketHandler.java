package waterflames.websend;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class PacketHandler
{
	private byte header;

	public PacketHandler(byte header)
	{
		this.header = header;
	}

	public abstract void onHeaderReceived(DataInputStream in, DataOutputStream out);

	public byte getHeader()
	{
		return header;
	}
}
