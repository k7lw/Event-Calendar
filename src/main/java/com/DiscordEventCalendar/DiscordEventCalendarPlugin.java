
package com.DiscordEventCalendar;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.net.URL;

@PluginDescriptor(
	name = "Discord Event Calendar Panel",
	description = "Enable the Discord Event Calendar panel"
)
public class DiscordEventCalendarPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	private DiscordEventCalendarPanel panel;
	private NavigationButton navButton;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private DiscordEventCalendarConfig config;

	@Provides
	public DiscordEventCalendarConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(DiscordEventCalendarConfig.class);
	}

	public static BufferedImage loadBufferedImageFromURL(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			return ImageIO.read(url);  // Returns a BufferedImage
		} catch (Exception e) {
			e.printStackTrace();
			return null; // Handle errors gracefully
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(DiscordEventCalendarPanel.class);
		panel.init();

		BufferedImage icon = ImageUtil.loadImageResource(DiscordEventCalendarPlugin.class,  "/calendar_icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Discord Event Calendar")
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(DiscordEventCalendarConfig.GROUP))
		{
			// Using Switch incase adding config options
			switch (event.getKey())
			{
				case "GUILD_ID":
					panel.configChange();
					break;
			}
		}
	}

	@Override
	protected void shutDown()
	{
		panel.deinit();
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
	}
}
