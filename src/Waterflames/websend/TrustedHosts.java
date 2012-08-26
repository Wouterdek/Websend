package Waterflames.websend;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class TrustedHosts
{
	private static ArrayList<InetAddress> trusted = new ArrayList<InetAddress>();
	private static boolean allIsTrusted = false;

	public static void load(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (line.startsWith("#") || line.trim().equals(""))
			{
			}
			else if ("trust_all".equals(line.trim().toLowerCase()))
			{
				allIsTrusted = true;
			}
			else
			{
				trusted.add(InetAddress.getByName(line));
			}
		}
		reader.close();
	}

	public static boolean isTrusted(InetAddress address)
	{
		return trusted.contains(address) || allIsTrusted;
	}

	static void writeDefaultFile(File trustedFile) throws IOException
	{
		trustedFile.createNewFile();
		PrintWriter writer = new PrintWriter(new FileWriter(trustedFile));
		writer.println("#Put your trusted domains in this file.");
		writer.println("#Trusted domains can connect to websend via php->websend.");
		writer.println("#Domains are allowed in either IP or hostname form.");
		writer.println("#To allow everybody put trust_all");
		writer.println("trust_all");
		writer.println("#http://example.com/");
		writer.println("#123.456.798.132");
		writer.flush();
		writer.close();
	}

	public static ArrayList<InetAddress> getList()
	{
		return trusted;
	}

	public static void add(InetAddress address)
	{
		trusted.add(address);
	}

	public static void remove(InetAddress address)
	{
		trusted.remove(address);
	}
}