/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
