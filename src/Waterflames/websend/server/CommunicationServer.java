/*
 * Packets: 1: DoCommandAsPlayer 2: DoCommandAsConsole 3: DoScript 4:
 * StartPluginOutputRecording 5: EndPluginOutputRecording 10:
 * WriteOutputToConsole 11: WriteOutputToPlayer 12: Broadcast 20: Disconnect 21:
 * Password
 */

package Waterflames.websend.server;

import Waterflames.websend.Main;
import Waterflames.websend.PacketHandler;
import Waterflames.websend.TrustedHosts;
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
	private HashMap<Byte, PacketHandler> customPacketHandlers = new HashMap<Byte, PacketHandler>();

	public CommunicationServer()
	{
	}

	@Override
	public void run()
	{
		try
		{
			if (Main.settings.isDebugMode())
			{
				Main.logger.log(Level.INFO, "Starting server");
			}
			startServer();
		}
		catch (IOException ex)
		{
			Main.logger.log(Level.SEVERE, null, ex);
		}
	}

	public void addPacketHandler(PacketHandler wph)
	{
		customPacketHandlers.put(wph.getHeader(), wph);
	}

	private void startServer() throws IOException
	{
		running = true;
		ServerSocket serverSkt = new ServerSocket(Main.settings.getPort());
		while (running)
		{
			if (Main.settings.isDebugMode())
			{
				Main.logger.log(Level.INFO, "Waiting for client.");
			}
			Socket skt = serverSkt.accept();
			if (Main.settings.isDebugMode())
			{
				Main.logger.log(Level.INFO, "Client connected.");
			}
			if (TrustedHosts.isTrusted(skt.getInetAddress()))
			{
				if (Main.settings.isDebugMode())
				{
					Main.logger.log(Level.INFO, "Client is trusted.");
				}
				skt.setKeepAlive(true);
				DataInputStream in = new DataInputStream(skt.getInputStream());
				DataOutputStream out = new DataOutputStream(skt.getOutputStream());

				connected = true;

				if (Main.settings.isDebugMode())
				{
					Main.logger.log(Level.INFO, "Trying to read first byte.");
				}
				if (in.readByte() == 21)
				{
					if (Main.settings.isDebugMode())
					{
						Main.logger.log(Level.INFO, "First packet is password packet.");
					}
					authenticated = PacketParser.parsePasswordPacket(in, out);
					if (!authenticated)
					{
						Main.logger.log(Level.INFO, "Password is incorrect! Client disconnected!");
						connected = false;
					}
					else
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Password is correct! Client connected.");
						}
					}
				}
				else
				{
					Main.logger.log(Level.WARNING, "First packet wasn't a password packet! Disconnecting. (Are you using the correct protocol?)");
				}

				while (connected)
				{
					byte packetHeader = in.readByte();
					if (packetHeader == 1)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: DoCommandAsPlayer");
						}
						PacketParser.parseDoCommandAsPlayer(in, out);
					}
					else if (packetHeader == 2)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: DoCommandAsConsole");
						}
						PacketParser.parseDoCommandAsConsole(in, out);
					}
					else if (packetHeader == 3)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: DoScript");
						}
						PacketParser.parseDoScript(in, out);
					}
					else if (packetHeader == 4)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: StartPluginOutputRecording");
						}
						PacketParser.parseStartPluginOutputRecording(in, out);
					}
					else if (packetHeader == 5)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: EndPluginOutputRecording");
						}
						PacketParser.parseEndPluginOutputRecording(in, out);
					}
					else if (packetHeader == 10)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: WriteOutputToConsole");
						}
						PacketParser.parseWriteOutputToConsole(in, out);
					}
					else if (packetHeader == 11)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: WriteOutputToPlayer");
						}
						PacketParser.parseWriteOutputToPlayer(in, out);
					}
					else if (packetHeader == 12)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: Broadcast");
						}
						PacketParser.parseBroadcast(in, out);
					}
					else if (packetHeader == 20)
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got packet header: Disconnect");
						}
						connected = false;
					}
					else if (customPacketHandlers.containsKey(packetHeader))
					{
						if (Main.settings.isDebugMode())
						{
							Main.logger.log(Level.INFO, "Got custom packet header: " + packetHeader);
						}
						customPacketHandlers.get(packetHeader).onHeaderReceived(in, out);
					}
					else
					{
						Main.logger.log(Level.WARNING, "Unsupported packet header!");
					}
				}
				if (Main.settings.isDebugMode())
				{
					Main.logger.log(Level.INFO, "Closing connection with client.");
				}
				out.flush();
				out.close();
				in.close();
			}
			else
			{
				Main.logger.log(Level.WARNING, "Connection request from unauthorized address!");
				Main.logger.log(Level.WARNING, "Address: " + skt.getInetAddress());
				Main.logger.log(Level.WARNING, "Add this address to trusted.txt to allow access.");
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
