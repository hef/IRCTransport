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
		//setVerbose(true);
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
		log.log(Level.FINE,line);
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
		player.sendMessage(String.format("[%s] %s: %s", channel, sender, message));
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
}
