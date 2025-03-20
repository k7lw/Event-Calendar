package com.DiscordEventCalendar;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup(DiscordEventCalendarConfig.GROUP)
public interface DiscordEventCalendarConfig extends Config
{
	String GROUP = "API_Info";

	@ConfigItem(
		keyName = "GUILD_ID",
		name = "GUILD ID",
		position = 3,
		description = "Change to match the Discord Server Guild ID that has Events linked with the Sesh.xyz Calendar"
	)
	default String GUILD_ID()
	{
		return "0000000000000000000";
	}
}
