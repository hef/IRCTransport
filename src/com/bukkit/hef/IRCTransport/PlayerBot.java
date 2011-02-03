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
	Random r = new Random();

	/**
	 * 
	 */
	public PlayerBot(Player player) {
		this.player = player;
		setVerbose(true);
		connect("acm.cs.uic.edu", player.getName());
		joinChannel("#minecraft");
		
		// TODO Auto-generated constructor stub
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		//TODO: replace channel names with numbers
		player.sendMessage(sender + ": " + message);
	}
	private void connect(String server, String nick)
	{
		try{
			setName(nick);
			super.connect(server);
		}  catch (NickAlreadyInUseException e1) {
			  String randomString = Long.toString(Math.abs(r.nextLong()), 36);
			  connect(server, nick + randomString);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IrcException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
