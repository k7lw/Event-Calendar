package com.DiscordEventCalendar;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DiscordEventCalendarTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DiscordEventCalendarPlugin.class);
		RuneLite.main(args);
	}
}