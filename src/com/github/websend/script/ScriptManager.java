package com.github.websend.script;

import com.github.websend.Main;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ScriptManager
{
	File scriptsDir;
	File compiledDir;
	private HashMap<String, Script> scripts;

	public ScriptManager()
	{
		scriptsDir = com.github.websend.Main.getScriptsDir();
		compiledDir = new File(com.github.websend.Main.getScriptsDir(), "compiled");
		scripts = new HashMap<String, Script>();
	}

	// ----------- API -----------

	public void invokeScript(String name)
	{
		if (Main.getSettings().isDebugMode())
		{
			Main.getMainLogger().log(Level.WARNING, "Searching script: " + name);
		}
		Script script = scripts.get(name);
		if (script != null)
		{
			if (Main.getSettings().isDebugMode())
			{
				Main.getMainLogger().log(Level.WARNING, "Found script, invoking main method.");
			}
			script.invoke();
		}
		else
		{
			Main.getMainLogger().info("No script with name: " + name);
		}
	}

	public void clearScripts()
	{
		if (Main.getSettings().isDebugMode())
		{
			Main.getMainLogger().log(Level.WARNING, "Cleared scripts map.");
		}
		scripts.clear();
	}

	public void reload()
	{
		clearScripts();
		loadScripts();
	}

	public void reload(String scriptName)
	{
		if (!scripts.containsKey(scriptName))
		{
			Main.getMainLogger().log(Level.WARNING, "'" + scriptName + "' was not found and therefore can't be reloaded.");
			return;
		}
		scripts.remove(scriptName);
		scripts.put(scriptName, loadScriptFromDir(new File(Main.getScriptsDir(), scriptName)));
	}

	public void loadScripts()
	{
		if (Main.getSettings().isDebugMode())
		{
			Main.getMainLogger().log(Level.WARNING, "Loading scripts");
		}
		File[] directories = scriptsDir.listFiles(new DirectoryFilter());
		for (File cur : directories)
		{
			if (!cur.getName().equals("compiled"))
			{
				if (Main.getSettings().isDebugMode())
				{
					Main.getMainLogger().log(Level.WARNING, "Loading script: " + cur.getName());
				}
				Script newScript = loadScriptFromDir(cur);
				scripts.put(newScript.name, newScript);
			}
		}
	}

	public String[] getScriptNames()
	{
		return scripts.keySet().toArray(new String[scripts.size()]);
	}

	public boolean hasScript(String name)
	{
		return scripts.containsKey(name);
	}

	// ----------- private methods -----------

	private Script loadScriptFromDir(File directory)
	{
		String scriptName = directory.getName();
		Script script = new Script(scriptName);

		loadScriptInfo(script, new File(directory, "info.txt"));

		File[] javas = directory.listFiles(new JavaFileFilter());
		if (!compileClasses(scriptName, javas))
		{
			Main.getMainLogger().log(Level.SEVERE, "Failed to compile script " + scriptName + "!");
			return null;
		}

		// File compiledFilesDir = new File(compiledDir, scriptName);

		loadClasses(script);

		if (script.invokeOnLoad)
		{
			script.invoke();
		}

		return script;
	}

	private void loadScriptInfo(Script script, File scriptInfoFile)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(scriptInfoFile));
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("#"))
				{
					continue;
				}
				else if (line.startsWith("INVOKEONLOAD"))
				{
					String strValue = line.split("=")[1];
					script.invokeOnLoad = Boolean.parseBoolean(strValue);
				}
			}
			reader.close();
		}
		catch (Exception ex)
		{
			Main.getMainLogger().log(Level.WARNING, "Failed to load script info for: " + script.name, ex);
		}
	}

	private boolean compileClasses(String name, File[] javaFiles)
	{
		if (Main.getSettings().isDebugMode())
		{
			Main.getMainLogger().log(Level.WARNING, "Compiling classes");
		}
		try
		{
			JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null);
			Iterable<? extends JavaFileObject> javas = sjfm.getJavaFileObjectsFromFiles(Arrays.asList(javaFiles));

			File compiledFilesDir = new File(compiledDir, name);
			String dir = compiledFilesDir.getCanonicalPath();
			if (!compiledFilesDir.exists())
			{
				if (!compiledFilesDir.mkdirs())
				{
					Main.getMainLogger().log(Level.WARNING, "Failed to make compiled scripts directory.");
				}
			}

			boolean succes = jc.getTask(null, sjfm, null, Arrays.asList(new String[] { "-d", dir }), null, javas).call();
			sjfm.close();
			return succes;
		}
		catch (IOException ex)
		{
			Main.getMainLogger().log(Level.SEVERE, null, ex);
			return false;
		}
	}

	private boolean loadClasses(Script container)
	{
		if (Main.getSettings().isDebugMode())
		{
			Main.getMainLogger().log(Level.WARNING, "Loading classes into JVM");
		}
		try
		{
			File scriptDir = new File(compiledDir, container.name);
			if (!scriptDir.exists())
			{
				Main.getMainLogger().log(Level.WARNING, "Invalid script! No compiled files dir found!");
				return false;
			}

			URLClassLoader classLoader = new URLClassLoader(new URL[] { scriptDir.toURI().toURL() });
			File[] classFiles = scriptDir.listFiles(new ClassFileFilter());

			for (File cur : classFiles)
			{
				Class<?> curClass = classLoader.loadClass(cur.getName().replace(".class", ""));
				if (curClass.getName().toLowerCase().equals("main"))
				{
					container.setMainClass(curClass);
				}
				container.addClass(curClass);
			}

			// Java 7 part
			/*
			 * try { classLoader.close(); } catch (IOException e) {
			 * e.printStackTrace(); }
			 */

			return true;
		}
		catch (Exception e)
		{
			Main.getMainLogger().log(Level.SEVERE, "Error while loading classes into the JVM!", e);
		}
		return false;
	}
}

class DirectoryFilter implements FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return file.isDirectory();
	}
}

class JavaFileFilter implements FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return file.getName().endsWith("java");
	}
}

class ClassFileFilter implements FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return file.getName().endsWith("class");
	}
}
