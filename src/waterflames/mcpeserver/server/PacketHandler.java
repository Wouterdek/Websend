package waterflames.mcpeserver.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import waterflames.mcpeserver.Main;
import waterflames.mcpeserver.server.packet.BroadcastPacket;
import waterflames.mcpeserver.server.packet.BroadcastResponsePacket;
import waterflames.mcpeserver.server.packet.ConnectRequest1Packet;
import waterflames.mcpeserver.server.packet.ConnectRequest2Packet;
import waterflames.mcpeserver.server.packet.ConnectResponse1Packet;
import waterflames.mcpeserver.server.packet.ConnectResponse2Packet;

public class PacketHandler
{
	NetworkManager manager;
	ClientConnection clientConnection;

	public PacketHandler(NetworkManager manager)
	{
		this.manager = manager;
	}

	public void handleBroadcast(BroadcastPacket broadcast, DatagramSocket serverSocket, DatagramPacket packet, byte[] serverID) throws IOException
	{
		BroadcastResponsePacket response = new BroadcastResponsePacket(broadcast.getPingID(), serverID, "MCCPP;Demo;Awesome server");
		response.sendPacket(serverSocket, packet.getAddress(), packet.getPort());
	}

	public void handleConnectRequest1(ConnectRequest1Packet connectPacket, DatagramSocket serverSocket, DatagramPacket packet, HashMap<String, ClientConnection> connections) throws IOException
	{
		Main.log("Received connect request. (" + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ")");

		ClientConnection connection = null;
		String address = packet.getAddress().getHostAddress() + ":" + packet.getPort();
		if (!connections.containsKey(address))
		{
			connection = new ClientConnection(packet);
			connections.put(address, connection);
		}
		else
		{
			Main.log("Connection list already contains new connection?");
			connection = connections.get(address);
		}
		connection.socket = serverSocket;

		ConnectResponse1Packet response = new ConnectResponse1Packet(Main.netManager.getServerID());
		response.sendPacket(serverSocket, connection.getAddress(), connection.getPort());
	}

	public void handleConnectRequest2(ConnectRequest2Packet connectPacket) throws IOException
	{
		clientConnection.clientID = connectPacket.getClientID();
		ConnectResponse2Packet response = new ConnectResponse2Packet(Main.netManager.getServerID());
		response.sendPacket(clientConnection.getSocket(), clientConnection.getAddress(), clientConnection.getPort());
	}

	public ClientConnection getClientConnection()
	{
		return clientConnection;
	}

	public void setClientConnection(ClientConnection clientConnection)
	{
		this.clientConnection = clientConnection;
	}
}
