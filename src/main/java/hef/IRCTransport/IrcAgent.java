/**
 * 
 */
package hef.IRCTransport;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 * @author hef
 *
 */
public class IrcAgent extends PircBot {
	private Player player;
	String activeChannel;
	private final IRCTransport plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	/**
	 * 
	 */
	public IrcAgent(IRCTransport instance, Player player) {
		this.plugin = instance;
		this.player = player;
		setLogin(player.getName());
		super.setAutoNickChange(true);
		connect(plugin.ircserver, player.getName());
		if(!plugin.autojoin.equals(""))
		{
			joinChannel(plugin.autojoin);
			activeChannel = plugin.autojoin;
		}
	}
	public void log(String line)
	{
		if(plugin.verbose)
		{
			log.log(Level.INFO,line);
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
			log.log(Level.SEVERE, e1.getMessage(), e1);
		} catch (IOException e1) {
			System.out.println("IOException: Failed to connect to irc server: " + server);
			log.log(Level.SEVERE, e1.getMessage(), e1);
		} catch (IrcException e1) {
			System.out.println("IrcException: Failed to connect to irc server: " + server);
			log.log(Level.SEVERE, e1.getMessage(), e1);
		}
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		//TODO: replace channel names with numbers
		player.sendMessage(String.format("[%s] %s: %s", channel, sender, ColorMap.fromIrc(message)));
	}
	public void onPrivateMessage(String sender, String login, String hostname, String message)
	{
		//TODO: check validity of recipient or check for error response
		player.sendMessage(String.format("%s: %s",sender, message));
	}
	public void sendMessage(String message)
	{
		// TODO: check ativeChannel for NULL, then just pick a random channel.
		sendMessage(activeChannel, message);
		player.sendMessage(String.format("[%s] %s: %s", activeChannel, player.getDisplayName(), message));
	}
	public void onJoin(String channel, String sender, String login, String hostname) 
	{
		//if I joined, change active channel.
		if(sender.equals(getNick()))
			activeChannel = channel;
		player.sendMessage(String.format("[%s] %s has joined.", channel, sender)); //TODO: colorize
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) 
	{
		if(oldNick.equals(player.getName()))
		{
			player.setDisplayName(newNick);
		}
		player.sendMessage(String.format("%s is now known as %s", oldNick , newNick));
	}
	protected void onUserList(String channel, User[] users)
	{
		String usersString = "";
		for(User user: users)
		{
			usersString += user + " ";
			
		}
		player.sendMessage(String.format("[%s names] ", channel, users));
	}
	protected void onServerResponse(int code, String response)
	{
		switch(code)
		{
			case ERR_INVITEONLYCHAN:
				int firstSpace = response.indexOf(' ');
	            int secondSpace = response.indexOf(' ', firstSpace + 1);
	            int colon = response.indexOf(':');
	            String channel = response.substring(firstSpace + 1, secondSpace);
	            String message = response.substring(colon + 1);		
				onErrInviteOnlyChan(channel, message);
				break;
		}
	}
	protected void onErrInviteOnlyChan(String channel, String message)
	{
		player.sendMessage(channel + " is invite only");	
	}
	protected void names()
	{
		names(activeChannel);
	}
	protected void names(String channel)
	{
		sendRawLine("NAMES " + "channel");
	}
	
}
