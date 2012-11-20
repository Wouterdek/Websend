package waterflames.mcpeserver.server.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import waterflames.mcpeserver.server.PacketHandler;

public abstract class Packet
{
	public boolean isWritten = false;
	public boolean isRead = false;

	public abstract void sendPacket(DataOutputStream stream) throws IOException;

	public abstract void readPacket(DataInputStream stream) throws IOException;

	public static Packet getPacketInstanceFromID(int id)
	{
		switch (id)
		{
			case 0x2:
				return new BroadcastPacket();
			case 0x1C:
				return new BroadcastResponsePacket();
			case 0x1D:
				return new BroadcastResponsePacket();
			case 0x5:
				return new ConnectRequest1Packet();
			case 0x6:
				return new ConnectResponse1Packet();
			case 0x7:
				return new ConnectRequest2Packet();
			case 0x8:
				return new ConnectResponse2Packet();
		}
		return null;
	}

	public void sendPacket(DatagramSocket socket, InetAddress address, int port) throws IOException
	{
		ByteArrayOutputStream in = new ByteArrayOutputStream();
		this.sendPacket(new DataOutputStream(in));
		byte[] data = in.toByteArray();
		DatagramPacket pkt = new DatagramPacket(data, data.length, address, port);
		socket.send(pkt);
	}

	public void receivePacket(DatagramSocket socket) throws IOException
	{
		byte[] data = new byte[1024];
		DatagramPacket pkt = new DatagramPacket(data, data.length);
		socket.receive(pkt);

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		this.readPacket(new DataInputStream(in));
	}

	public void readPacket(DatagramPacket pkt) throws IOException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(pkt.getData());
		this.readPacket(new DataInputStream(in));
	}

	public static void writeString(DataOutputStream stream, String string) throws IOException
	{
		stream.writeShort(string.length());
		stream.write(string.getBytes("US-ASCII"));
	}

	public static String readString(DataInputStream stream) throws IOException
	{
		short lenght = stream.readShort();
		byte[] data = new byte[lenght];
		stream.read(data);
		String result = new String(data, "US-ASCII");

		return result;
	}

	public void writeMAGIC(DataOutputStream stream) throws IOException
	{
		int[] magic = { 0x0, 0xff, 0xff, 0x0, 0xfe, 0xfe, 0xfe, 0xfe, 0xfd, 0xfd, 0xfd, 0xfd, 0x12, 0x34, 0x56, 0x78 };
		for (int i = 0; i < magic.length; i++)
		{
			stream.write(magic[i]);
		}
	}

	public boolean readMAGIC(DataInputStream stream) throws IOException
	{
		int[] magic = { 0x0, 0xff, 0xff, 0x0, 0xfe, 0xfe, 0xfe, 0xfe, 0xfd, 0xfd, 0xfd, 0xfd, 0x12, 0x34, 0x56, 0x78 };
		boolean isEqual = true;
		for (int i = 0; i < magic.length; i++)
		{
			if (stream.read() != magic[i])
			{
				isEqual = false;
			}
		}
		return isEqual;
	}

	public abstract void run(PacketHandler handler);
}
