package com.DiscordEventCalendar;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.SessionClose;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class DiscordEventCalendarPanel extends PluginPanel {
	private static final CalendarUtils calendar_util = new CalendarUtils();
	private static final String begin = calendar_util.get_startDateTime();
	private static final String end = calendar_util.get_endDateTime();
	private static final ImageIcon REFRESH_ICON;

	private JPanel actionsContainer;

	@Inject
	private Client client;

	@Inject
	private DiscordEventCalendarConfig config;

	@Inject
	private EventBus eventBus;

	@Inject
	private ScheduledExecutorService executor;

	@Provides
	public DiscordEventCalendarConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(DiscordEventCalendarConfig.class);
	}

	private JPanel eventListPanel;

    static
	{
		REFRESH_ICON = new ImageIcon(ImageUtil.loadImageResource(DiscordEventCalendarPanel.class, "/refresh_icon.png"));
	}

	void init() {
		setLayout(new GridBagLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		// Title Label
		JLabel titleLabel = new JLabel("Event Calendar");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);

		// Refresh Button
        JButton refreshButton = new JButton(REFRESH_ICON);
		refreshButton.addActionListener(e -> fetchAndDisplayEvents());

		// Top Panel (Title + Refresh Button)
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		GridBagConstraints topGbc = new GridBagConstraints();
		topGbc.insets = new Insets(5, 5, 5, 5);
		topGbc.fill = GridBagConstraints.HORIZONTAL;
		topGbc.weightx = 1;
		topGbc.gridx = 0;

		topPanel.add(titleLabel, topGbc);

		topGbc.gridx = 1;
		topGbc.weightx = 0;
		topPanel.add(refreshButton, topGbc);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.2; // 40% of the height for the top section
		gbc.fill = GridBagConstraints.BOTH;
		add(topPanel, gbc); //

		// Event List Panel (Scrollable Content)
		eventListPanel = new JPanel();
		eventListPanel.setLayout(new BoxLayout(eventListPanel, BoxLayout.Y_AXIS));
		eventListPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		eventListPanel.setBorder(new EmptyBorder(2, 2, 2, 2)); // Added margin

		// Scroll Pane for Events
        JScrollPane scrollPane = new JScrollPane(eventListPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		gbc.gridy = 1;
		gbc.weighty = 0.6; // 60% of the height for eventListPanel
		add(eventListPanel, gbc);

		// Title Label
		JLabel bottom_buffer = new JLabel(" ");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);

		gbc.gridy = 2;
		gbc.weighty = 0.1; // 60% of the height for eventListPanel
		add(bottom_buffer, gbc);

		fetchAndDisplayEvents(); // Fetch events on load
		eventBus.register(this);
	}

	public void configChange(){
		fetchAndDisplayEvents();
	}

	private void fetchAndDisplayEvents() {
		eventListPanel.removeAll(); // Clear previous events

		new Thread(() -> {
			SeshCalendarAPI seshApi = new SeshCalendarAPI(config.GUILD_ID(), begin, end);
			List<DiscordEvent> events = seshApi.fetchEvents();
			SwingUtilities.invokeLater(() -> {
				if (!events.isEmpty()) {
					for (DiscordEvent event : events) {
						eventListPanel.add(createEventPanel(
								event.getName(),
								event.getStartTime(),
								event.getEndTime(),
								event.getLocation(),
								event.getDescription()
						));
					}
				}else {
					displayNoEventsMessage();
				}

				eventListPanel.revalidate();
				eventListPanel.repaint();
			});
		}).start();
	}
	private JPanel createEventPanel(String name, String starttime, String endtime, String location, String description) {
		final int PANEL_WIDTH = 200;
		final int COLLAPSED_HEIGHT = 100;
		final int MAX_EXPANDED_HEIGHT = 125;

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new EmptyBorder(2, 2, 2, 2));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setPreferredSize(new Dimension(PANEL_WIDTH, COLLAPSED_HEIGHT));
		panel.setMaximumSize(new Dimension(PANEL_WIDTH, COLLAPSED_HEIGHT));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridx = 0;

		// Event Name with Offset
		gbc.gridy = 0;
		gbc.insets = new Insets(4, 1, 4, 1);
		JTextArea nameLabel = createDynamicTextArea(name, FontManager.getRunescapeBoldFont(), Color.WHITE, PANEL_WIDTH);
		panel.add(nameLabel, gbc);

		// Start Time with Offset
		gbc.gridy = 1;
		gbc.insets = new Insets(4, 1, 4, 1);
		JTextArea starttimeLabel = createDynamicTextArea("Start 📅 | " + starttime, FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY, PANEL_WIDTH);
		panel.add(starttimeLabel, gbc);

		// End Time with Offset
		gbc.gridy = 2;
		gbc.insets = new Insets(4, 1, 4, 1);
		JTextArea endtimeLabel = createDynamicTextArea("  End  📅 | " + endtime, FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY, PANEL_WIDTH);
		panel.add(endtimeLabel, gbc);

		// Event Location with Offset
		gbc.gridy = 3;
		gbc.insets = new Insets(4, 1, 4, 1);
		JTextArea locationLabel = createDynamicTextArea("🏠 " + location, FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY, PANEL_WIDTH);
		panel.add(locationLabel, gbc);


		// Event Description Panel (Initially Hidden)
		gbc.gridy = 4;
		gbc.insets = new Insets(4, 1, 4, 1);
		JTextArea descriptionArea = createDynamicTextArea(description, FontManager.getRunescapeSmallFont(), Color.WHITE, PANEL_WIDTH);
		descriptionArea.setVisible(false);

		JPanel eventDescriptionPanel = new JPanel(new BorderLayout());
		eventDescriptionPanel.setBackground(Color.darkGray);
		eventDescriptionPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		eventDescriptionPanel.add(descriptionArea, BorderLayout.CENTER);
		eventDescriptionPanel.setVisible(false);
		panel.add(eventDescriptionPanel, gbc);

		// Force proper height calculation
		panel.revalidate();
		panel.repaint();

		// Expand/Collapse Action on Click for All Elements
		MouseAdapter expandCollapseListener = new MouseAdapter() {
			private boolean isExpanded = false;



			@Override
			public void mouseClicked(MouseEvent e) {

				isExpanded = !isExpanded;
				int newHeight = 0;
				if (!description.isEmpty()) {
					descriptionArea.setVisible(isExpanded);
					eventDescriptionPanel.setVisible(isExpanded);
					newHeight = isExpanded ? COLLAPSED_HEIGHT + MAX_EXPANDED_HEIGHT : COLLAPSED_HEIGHT;
				}
				else {
					newHeight = isExpanded ? COLLAPSED_HEIGHT + 25 : COLLAPSED_HEIGHT;
				}
				panel.setPreferredSize(new Dimension(PANEL_WIDTH, newHeight));
				panel.setMaximumSize(new Dimension(PANEL_WIDTH, newHeight));
				panel.revalidate();
				panel.repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				panel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		};

		// Add the click listener to every element
		panel.addMouseListener(expandCollapseListener);
		nameLabel.addMouseListener(expandCollapseListener);
		starttimeLabel.addMouseListener(expandCollapseListener);
		endtimeLabel.addMouseListener(expandCollapseListener);
		locationLabel.addMouseListener(expandCollapseListener);
		if (!description.isEmpty()) {
			descriptionArea.addMouseListener(expandCollapseListener);
		}
		return panel;
	}


	/**
	 * Helper function to create a dynamically resizing JTextArea.
	 */
	private JTextArea createDynamicTextArea(String text, Font font, Color color, int width) {
		JTextArea textArea = new JTextArea(text);
		textArea.setFont(font);
		textArea.setForeground(color);
		textArea.setOpaque(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setBorder(null);

		// Dynamically adjust height based on content
		textArea.setSize(width - 5, Short.MAX_VALUE);
		int textHeight = textArea.getPreferredSize().height + 5;
		textArea.setPreferredSize(new Dimension(width - 5, textHeight));

		return textArea;
	}

	/**
	 * Displays a "No Events Found" message.
	 */
	private void displayNoEventsMessage() {
		JLabel noEvents = new JLabel("No events found.");
		noEvents.setForeground(Color.WHITE);
		eventListPanel.add(noEvents);
	}

	void deinit() {
		eventBus.unregister(this);
	}


	@Subscribe
	public void onSessionOpen(SessionOpen sessionOpen) {
		fetchAndDisplayEvents();
	}

	@Subscribe
	public void onSessionClose(SessionClose e) {
		eventListPanel.removeAll();
		eventListPanel.repaint();
	}
}
