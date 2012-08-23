package Waterflames.websend.script;

import Waterflames.websend.Main;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Script
{
	public String name;
	public boolean invokeOnLoad = false;
	private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	private Class<?> main;

	public Script(String name)
	{
		this.name = name;
	}

	public void invoke()
	{
		try
		{
			if (main == null)
			{
				Logger.getLogger(Script.class.getName()).log(Level.SEVERE, "No main class found for " + name + "!");
			}
			if (main != null)
			{
				main.getMethod("run", new Class[] {}).invoke(main.newInstance(), new Object[] {});
			}
		}
		catch (InvocationTargetException ex)
		{
			if (ex.getCause() != null)
			{
				Main.logger.log(Level.SEVERE, "The '" + name + "' script failed to run", ex.getCause());
			}
		}
		catch (NoSuchMethodException ex)
		{
			Logger.getLogger(Script.class.getName()).log(Level.SEVERE, "The '" + name + "' script doesn't contain a run method!");
		}
		catch (Exception ex)
		{
			Logger.getLogger(Script.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void addClass(Class<?> newClass)
	{
		classes.add(newClass);
	}

	public void setMainClass(Class<?> newMain)
	{
		main = newMain;
	}
}
