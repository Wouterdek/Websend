package waterflames.mcpeserver.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import waterflames.mcpeserver.Main;
import waterflames.mcpeserver.server.packet.*;

public class NetworkManager extends Thread
{
	HashMap<String, ClientConnection> connections = new HashMap<String, ClientConnection>();
	PacketHandler generalPacketHandler;
	byte[] serverID = { -1, -1, -1, -1, -23, 100, -61, -115 };
	boolean running = true;
	DatagramSocket serverSocket;

	@Override
	public void run()
	{
		this.setName("Network Manager");
		generalPacketHandler = new PacketHandler(this);

		Main.log("Starting datagram socket.");
		try
		{
			serverSocket = new DatagramSocket(19132);
		}
		catch (IOException ex)
		{
			Main.handleException(ex);
			return;
		}

		while (running)
		{
			try
			{
				DatagramPacket packet = receivePacket(serverSocket);
				int header = packet.getData()[0] & 0xff;

				if (header == 0x2)
				{
					BroadcastPacket broadcast = new BroadcastPacket();
					broadcast.readPacket(packet);
					generalPacketHandler.handleBroadcast(broadcast, serverSocket, packet, serverID);
				}
				else if (header == 0x5)
				{
					ConnectRequest1Packet connectPacket = new ConnectRequest1Packet();
					connectPacket.readPacket(packet);
					generalPacketHandler.handleConnectRequest1(connectPacket, serverSocket, packet, connections);
				}
				else
				{
					String address = packet.getAddress().getHostAddress() + ":" + packet.getPort();
					if (connections.containsKey(address))
					{
						ClientConnection con = connections.get(address);
						con.onPacketReceived(packet, con.getPacketHandler());
					}
				}
			}
			catch (IOException ex)
			{
				Main.handleException(ex);
			}
		}
	}

	public DatagramPacket receivePacket(DatagramSocket serverSocket) throws IOException
	{
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);
		return receivePacket;
	}

	public HashMap<String, ClientConnection> getConnections()
	{
		return connections;
	}

	public PacketHandler getGeneralPacketHandler()
	{
		return generalPacketHandler;
	}

	public byte[] getServerID()
	{
		return serverID;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void stopService()
	{
		for (ClientConnection con : connections.values())
		{
			con.disconnect();
		}

		running = false;
		serverSocket.close();
		Main.log("Disabled NetworkManager");
	}
}