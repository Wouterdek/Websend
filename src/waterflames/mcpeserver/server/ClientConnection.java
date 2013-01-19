package waterflames.mcpeserver.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import waterflames.mcpeserver.Main;
import waterflames.mcpeserver.server.packet.Packet;

public class ClientConnection
{
	InetAddress address;
	long clientID;
	int port;

	ArrayBlockingQueue<Packet> sendQueue = new ArrayBlockingQueue(32768);
	WritingThread writer;
	DatagramSocket socket;
	PacketHandler handler;

	public ClientConnection(DatagramPacket connectionPacket)
	{
		address = connectionPacket.getAddress();
		port = connectionPacket.getPort();

		try
		{
			DatagramSocket skt = new DatagramSocket(connectionPacket.getPort());
			writer = new WritingThread(this, skt, connectionPacket.getAddress(), connectionPacket.getPort());
			writer.start();
		}
		catch (IOException ex)
		{
			Main.handleException(ex);
		}
		handler = new PacketHandler(Main.netManager);
		handler.setClientConnection(this);
	}

	public void disconnect()
	{
		// Send disconnect packet

	}

	public void onPacketReceived(DatagramPacket packet, PacketHandler handler) throws IOException
	{
		int header = packet.getData()[0] & 0xff; // Unsigned byte to signed.
		Packet pkt = Packet.getPacketInstanceFromID(header);
		if (pkt != null)
		{
			pkt.readPacket(packet);
			pkt.run(handler);
		}
		else
		{
			Main.log("Packetheader " + header + " was not recognized.");
		}
	}

	public void sendPacket(Packet packet)
	{
		while (!sendQueue.offer(packet))
		{
			Main.log("Packet queue full! Retrying...");
		}
	}

	public InetAddress getAddress()
	{
		return this.address;
	}

	public int getPort()
	{
		return this.port;
	}

	public DatagramSocket getSocket()
	{
		return this.socket;
	}

	public PacketHandler getPacketHandler()
	{
		return handler;
	}
}

class WritingThread extends Thread
{
	boolean running = true;
	ClientConnection connection;
	DatagramSocket serverSocket;
	InetAddress address;
	int port;

	public WritingThread(ClientConnection connection, DatagramSocket serverSocket, InetAddress address, int port) throws IOException
	{
		this.serverSocket = serverSocket;
		this.connection = connection;
		this.address = address;
		this.port = port;
	}

	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				Packet pkt = connection.sendQueue.poll(1, TimeUnit.SECONDS);
				if (pkt != null)
				{
					try
					{
						pkt.sendPacket(serverSocket, address, port);
					}
					catch (IOException ex)
					{
						Main.handleException(ex);
					}
					pkt.isWritten = true;
				}
			}
			catch (InterruptedException ex)
			{
				Main.handleException(ex);
			}
		}
	}

	public void close()
	{
		this.running = false;
	}
}