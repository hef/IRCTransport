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
	//todo protect player again
	Player player;
	private String activeChannel;
	final IRCTransport plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	//flag to indicate we should not reconnect
	private boolean shutdown; 
	
	/**
	 * 
	 */
	public IrcAgent(IRCTransport instance, Player player) {
		//TODO: see how long this takes to construct
		this.plugin = instance;
		this.player = player;
		this.shutdown = false;
		setLogin(player.getName());
		super.setAutoNickChange(true);
		connect(plugin.getIrcServer(), player.getName());
		
		if(!plugin.getAutoJoin().equals(""))
		{
			activeChannel = plugin.getAutoJoin();
			joinChannel(activeChannel);
		}
	}
	public void log(String line)
	{
		if(plugin.isVerbose())
		{
			log.log(Level.INFO,line);
		}
	}
	
	public String getActiveChannel()
	{
		return this.activeChannel;
	}
	
	public void setActiveChannel(String channel)
	{
		this.activeChannel = channel;
	}
	
	private void connect(String server, String nick)
	{
		//TODO get rid of this line
		player.sendMessage("attempting to connect to chat server");
		try{
			setName(nick);
			//this doesn't have much in game affect, but might help other plugins.
			player.setDisplayName(nick);
			super.connect(server);
		}  catch (NickAlreadyInUseException e) {
			  //This should not be called anymore.
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			//TODO schedule a reconnect attempte
			System.out.println("IOException: Failed to connect to irc server: " + server);
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (IrcException e) {
			System.out.println("IrcException: Failed to connect to irc server: " + server);
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}
	public void onDisconnect()
	{
		player.sendMessage("ChatService Disconnected.");
		Reconnect reconnectTask = new Reconnect(this);
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, reconnectTask);
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
	public void onAction(String sender, String login, String hostname, String target, String action)
	{
		player.sendMessage(String.format("[%s] * %s %s", target, sender, action));
	}
	public void sendMessage(String message)
	{
		// TODO: check ativeChannel for NULL, then just pick a random channel.
		sendMessage(activeChannel, message);
		if(isConnected())
		{
			String msg = String.format("[%s] %s: %s", activeChannel, player.getDisplayName(), message);
			// if verbose, log all chat
			//if(plugin.isVerbose()) log.log(Level.INFO, msg);
			player.sendMessage(msg);
		}
	}
	public void sendAction(String action)
	{
		sendAction(activeChannel, action);
		player.sendMessage(String.format("[%s] * %s %s", activeChannel, player.getDisplayName(), action));
	}
	/*public void sendAction(String target, String action)
	{
		sendAction(target, action);
		player.sendMessage(String.format("[%s] %s: %s", target, player.getDisplayName(), action));
	}*/
	public void onJoin(String channel, String sender, String login, String hostname) 
	{
		//if I joined, change active channel.
		if(sender.equals(getNick()))
			activeChannel = channel;
		player.sendMessage(String.format("[%s] %s has joined.", channel, sender)); //TODO: colorize
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) 
	{
		if(oldNick.equals(player.getDisplayName()))
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
			usersString += user.toString() + " ";
		}
		player.sendMessage(String.format("%s members: %s", channel, usersString));
	}
	/** Handles response codes not handled by pircbot
	 * This methods handles irc response codes, slices up the response, and then
	 * calls the appropriate method
	 * @see org.jibble.pircbot.PircBot#onServerResponse(int, java.lang.String)
	 * @see org.jibble.pircbot.PircBot.ReplyConstants
	 * @param code the irc response code.
	 * @param response The message that came with the response
	 */
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
		sendRawLine("NAMES " + channel);
	}
	
	@Override
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
		if(plugin.isVerbose())	{
			log.log(Level.INFO, String.format("On %tc, %s changed the topic of %s to: %s", date, setBy, channel, topic));
		}			
		if(changed){
			player.sendMessage(String.format("%s changed the topic of %s to: %s", setBy, channel, topic));
		} else {
			player.sendMessage(String.format("Topic for %s: %s", channel, topic));
			player.sendMessage(String.format("Topic set by %s [%tc]", setBy, date));
		}
	}
	protected void topic()
	{
		sendRawLine(String.format("TOPIC %s", activeChannel));
	}
	protected void setTopic(String topic)
	{
		setTopic(activeChannel, topic);
	}
}
