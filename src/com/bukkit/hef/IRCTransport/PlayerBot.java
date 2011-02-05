/**
 * 
 */
package com.bukkit.hef.IRCTransport;

import java.io.IOException;
import java.util.Random;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.bukkit.entity.Player;
/**
 * @author hef
 *
 */
public class PlayerBot extends PircBot {
	private Player player;
	String activeChannel;
	Random r = new Random();
	private final IRCTransport plugin;

	/**
	 * 
	 */
	public PlayerBot(IRCTransport instance, Player player) {
		this.plugin = instance;
		this.player = player;
		setVerbose(true);
		setLogin(player.getName());
		connect(plugin.ircserver, player.getName());
		if(!plugin.autojoin.equals(""))
		{
			joinChannel(plugin.autojoin);
			activeChannel = plugin.autojoin;
		}
	}
	private void connect(String server, String nick)
	{
		try{
			setName(nick);
			//this doesn't have much in game affect, but might help other plugins.
			player.setDisplayName(nick);
			super.connect(server);
		}  catch (NickAlreadyInUseException e1) {
			  //TODO: make this about 4 charachters long, and strip previous 4 if already done.
			  String randomString = Long.toString(Math.abs(r.nextLong()), 36);
			  connect(server, nick + randomString);
		} catch (IOException e1) {
			System.out.println("IOException: Failed to connect to irc server: " + server);
			e1.printStackTrace();
		} catch (IrcException e1) {
			System.out.println("IrcException: Failed to connect to irc server: " + server);
			e1.printStackTrace();
		}
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		//TODO: replace channel names with numbers
		player.sendMessage(String.format("[%s] %s: %s", channel, sender, message));
	}
	public void onPrivateMessage(String sender, String login, String hostname, String message)
	{
		player.sendMessage(String.format("%s: %s",sender, message));
	}
	public void sendMessage(String message)
	{
		sendMessage(activeChannel, message);
		player.sendMessage(String.format("[%s] %s: %s", activeChannel, player.getName(), message));
	}
}
