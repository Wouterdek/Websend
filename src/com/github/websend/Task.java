package com.github.websend;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Task implements Runnable
{
	private ArrayList<Object> arguments = new ArrayList<Object>();

	public Task(Object... arg)
	{
		arguments.addAll(Arrays.asList(arg));
	}

	public ArrayList<Object> getArgs()
	{
		return arguments;
	}
}
