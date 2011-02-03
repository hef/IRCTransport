package com.bukkit.hef.IRCTransport;

import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * IRCTransport for Bukkit
 * 
 * @author hef
 */
public class IRCTransport extends JavaPlugin {
	private final IRCTransportPlayerListener playerListener = new IRCTransportPlayerListener(
			this);
	private final IRCTransportBlockListener blockListener = new IRCTransportBlockListener(
			this);
	final HashMap<Player, PlayerBot> bots = new HashMap<Player, PlayerBot>();

	public IRCTransport(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	@Override
	public void onEnable() {
		// TODO: Place any custom enable code here including the registration of
		// any events

		// Register our events
		PluginManager pm = getServer().getPluginManager();

		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
				Priority.Normal, this);
	}

	@Override
	public void onDisable() {

		System.out.println("Goodbye world!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		Player player = (Player) sender;
		PlayerBot bot = bots.get(player);
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();

		if (commandName.equals("join")) {
			bot.joinChannel(args[0]);
			return true;
		}
		return false;
	}
}
