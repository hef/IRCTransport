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
		super.setAutoNickChange(true);
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
			  //This should not be called anymore.
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
		player.sendMessage(String.format("[%s] %s: %s", activeChannel, player.getDisplayName(), message));
	}
	public void onJoin(String channel, String sender, String login, String hostname) 
	{
		if(login.equals(player.getName()))
			activeChannel = channel;
		else
			player.sendMessage(String.format("[%s] %s has joined.", channel, sender)); //TODO: colorize
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) 
	{
		if(login.equals(player.getName()))
			player.setDisplayName("newNick");
		else
			player.sendMessage(String.format("$s is now known as %s", oldNick , newNick));
	}
}
