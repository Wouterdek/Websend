/*
 * Packets: 1: DoCommandAsPlayer 2: DoCommandAsConsole 3: DoScript 4:
 * StartPluginOutputRecording 5: EndPluginOutputRecording 10:
 * WriteOutputToConsole 11: WriteOutputToPlayer 12: Broadcast 20: Disconnect 21:
 * Password
 */

package waterflames.websend.server;

import waterflames.websend.Main;
import waterflames.websend.PacketHandler;
import waterflames.websend.TrustedHosts;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;

public class CommunicationServer extends Thread
{
	private boolean running = false;
	private boolean connected = false;
	private boolean authenticated = false;
	private ServerSocket serverSkt;
	private HashMap<Byte, PacketHandler> customPacketHandlers = new HashMap<Byte, PacketHandler>();

	public CommunicationServer()
	{
	}

	@Override
	public void run()
	{
		try
		{
			if (Main.getSettings().isDebugMode())
			{
				Main.getMainLogger().log(Level.INFO, "Starting server");
			}
			startServer();
		}
		catch (Exception ex)
		{
			Main.getMainLogger().log(Level.SEVERE, "Server encountered an error. Attempting restart.", ex);
			running = true;
			connected = false;
			authenticated = false;

			try
			{
				serverSkt.close();
			}
			catch (IOException ex1)
			{
			}

			try
			{
				startServer();
			}
			catch (IOException ex1)
			{
				Main.getMainLogger().log(Level.SEVERE, "Server encountered an error. Server down.", ex);
			}
		}
	}

	public void addPacketHandler(PacketHandler wph)
	{
		customPacketHandlers.put(wph.getHeader(), wph);
	}

	private void startServer() throws IOException
	{
		running = true;
		serverSkt = new ServerSocket(Main.getSettings().getPort());
		while (running)
		{
			if (Main.getSettings().isDebugMode())
			{
				Main.getMainLogger().log(Level.INFO, "Waiting for client.");
			}
			Socket skt = serverSkt.accept();
			if (Main.getSettings().isDebugMode())
			{
				Main.getMainLogger().log(Level.INFO, "Client connected.");
			}
			if (TrustedHosts.isTrusted(skt.getInetAddress()))
			{
				if (Main.getSettings().isDebugMode())
				{
					Main.getMainLogger().log(Level.INFO, "Client is trusted.");
				}
				skt.setKeepAlive(true);
				DataInputStream in = new DataInputStream(skt.getInputStream());
				DataOutputStream out = new DataOutputStream(skt.getOutputStream());

				connected = true;

				if (Main.getSettings().isDebugMode())
				{
					Main.getMainLogger().log(Level.INFO, "Trying to read first byte.");
				}

				try
				{
					if (in.readByte() == 21)
					{
						if (Main.getSettings().isDebugMode())
						{
							Main.getMainLogger().log(Level.INFO, "First packet is password packet.");
						}
						authenticated = PacketParser.parsePasswordPacket(in, out);
						if (!authenticated)
						{
							Main.getMainLogger().log(Level.INFO, "Password is incorrect! Client disconnected!");
							connected = false;
						}
						else
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Password is correct! Client connected.");
							}
						}
					}
					else
					{
						Main.getMainLogger().log(Level.WARNING, "First packet wasn't a password packet! Disconnecting. (Are you using the correct protocol?)");
					}

					while (connected)
					{
						byte packetHeader = in.readByte();
						if (packetHeader == 1)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: DoCommandAsPlayer");
							}
							PacketParser.parseDoCommandAsPlayer(in, out);
						}
						else if (packetHeader == 2)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: DoCommandAsConsole");
							}
							PacketParser.parseDoCommandAsConsole(in, out);
						}
						else if (packetHeader == 3)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: DoScript");
							}
							PacketParser.parseDoScript(in, out);
						}
						else if (packetHeader == 4)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: StartPluginOutputRecording");
							}
							PacketParser.parseStartPluginOutputRecording(in, out);
						}
						else if (packetHeader == 5)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: EndPluginOutputRecording");
							}
							PacketParser.parseEndPluginOutputRecording(in, out);
						}
						else if (packetHeader == 10)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: WriteOutputToConsole");
							}
							PacketParser.parseWriteOutputToConsole(in, out);
						}
						else if (packetHeader == 11)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: WriteOutputToPlayer");
							}
							PacketParser.parseWriteOutputToPlayer(in, out);
						}
						else if (packetHeader == 12)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: Broadcast");
							}
							PacketParser.parseBroadcast(in, out);
						}
						else if (packetHeader == 20)
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got packet header: Disconnect");
							}
							connected = false;
						}
						else if (customPacketHandlers.containsKey(packetHeader))
						{
							if (Main.getSettings().isDebugMode())
							{
								Main.getMainLogger().log(Level.INFO, "Got custom packet header: " + packetHeader);
							}
							customPacketHandlers.get(packetHeader).onHeaderReceived(in, out);
						}
						else
						{
							Main.getMainLogger().log(Level.WARNING, "Unsupported packet header!");
						}
					}
					if (Main.getSettings().isDebugMode())
					{
						Main.getMainLogger().log(Level.INFO, "Closing connection with client.");
					}
					out.flush();
					out.close();
					in.close();
				}
				catch (IOException ex)
				{
					Main.getMainLogger().log(Level.WARNING, "IOException while communicating to client! Disconnecting.");
					connected = false;
				}
			}
			else
			{
				Main.getMainLogger().log(Level.WARNING, "Connection request from unauthorized address!");
				Main.getMainLogger().log(Level.WARNING, "Address: " + skt.getInetAddress());
				Main.getMainLogger().log(Level.WARNING, "Add this address to trusted.txt to allow access.");
			}
			skt.close();
		}
		serverSkt.close();
	}

	public void stopServer()
	{
		running = false;
	}

	public boolean isConnected()
	{
		return connected;
	}
}
