/**
 * 
 */
package com.bukkit.hef.IRCTransport;

import java.io.IOException;

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

	/**
	 * 
	 */
	public PlayerBot(Player player) {
		this.player = player;
		setName(player.getName());
		setVerbose(true);
		//TODO: make try/catch a recursive function
		try {
			connect("acm.cs.uic.edu");
		} catch (NickAlreadyInUseException e) {
			setName(player.getName() + Math.random());
			try {
				connect("acm.cs.uic.edu");
			} catch (NickAlreadyInUseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IrcException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IrcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		joinChannel("#minecraft");
		
		// TODO Auto-generated constructor stub
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		//TODO: replace channel names with numbers
		player.sendMessage(sender + ": " + message);
	}

}
