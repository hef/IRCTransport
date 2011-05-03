/**
 * 
 */
package hef.IRCTransport;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.bukkit.ChatColor;

/**
 * @author hef
 *
 */
public class IrcAgent extends PircBot {
	private Player player;
	private String activeChannel;
	final private IRCTransport plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	//flag to indicate we should not reconnect
	private boolean shuttingDown; 
	
	/**
	 * 
	 */
	public IrcAgent(IRCTransport instance, Player player) {
		this.plugin = instance;
		this.player = player;
		this.shuttingDown = false;
		setLogin(String.format("%s%s%s",plugin.getNickPrefix(),player.getName(),plugin.getNickSuffix()));
		super.setAutoNickChange(true);
		new Connect(this).run();
		
		/*if(!getPlugin().getAutoJoin().equals(""))
		{
			activeChannel = getPlugin().getAutoJoin();
			joinChannel(activeChannel);
		}*/
	}
	public void log(String line)
	{
		if(getPlugin().isVerbose())
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
	//sets the nick to attempt to use.
	//
	/** Set name to attempt to use at login
	 * This function is not the same as changeNick(String name)
	 * you probably don't want this function
	 * @param name the name to attempt to use.
	 */
	public void setNick(String name)
	{
		super.setName(name);
	}
	
	public void onDisconnect()
	{
		getPlayer().sendMessage("ChatService Disconnected.");
		if(!shuttingDown)
		{
			//Reconnect reconnectTask = new Reconnect(this);
			//plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, reconnectTask);
			new Connect(this).run();
		}
	}
	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		//TODO: replace channel names with numbers
		getPlayer().sendMessage(String.format("[%s] %s: %s", channel, sender, ColorMap.fromIrc(message)));
	}
	public void onPrivateMessage(String sender, String login, String hostname, String message)
	{
		//TODO: check validity of recipient or check for error response
		getPlayer().sendMessage(String.format("%s: %s",sender, message));
	}
	public void onAction(String sender, String login, String hostname, String target, String action)
	{
		getPlayer().sendMessage(String.format("[%s] * %s %s", target, sender, action));
	}
	public void sendMessage(String message)
	{
		// TODO: check ativeChannel for NULL, then just pick a random channel.
		sendMessage(activeChannel, message);
		if(isConnected())
		{
			String msg = String.format("[%s] %s: %s", activeChannel, getPlayer().getDisplayName(), message);
			getPlayer().sendMessage(msg);
		}
	}
	public void sendAction(String action)
	{
		sendAction(activeChannel, action);
		getPlayer().sendMessage(String.format("[%s] * %s %s", activeChannel, getPlayer().getDisplayName(), action));
	}
	public void onJoin(String channel, String sender, String login, String hostname) 
	{
		//if I joined, change active channel.
		if(sender.equals(getNick()))
			activeChannel = channel;
		getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] %s has joined.", channel, sender)); 
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) 
	{
		if(oldNick.equals(getPlayer().getDisplayName()))
		{
			getPlayer().setDisplayName(newNick);
		}
		getPlayer().sendMessage(String.format("%s is now known as %s", oldNick , newNick));
	}
	protected void onUserList(String channel, User[] users)
	{
		String usersString = "";
		for(User user: users)
		{
			usersString += user.toString() + " ";
		}
		getPlayer().sendMessage(String.format("%s members: %s", channel, usersString));
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
		Pattern responsePattern = Pattern.compile("(\\S*) (\\S*) :(.*)");
		Matcher responseMatcher = responsePattern.matcher(response);
		responseMatcher.find();
		switch(code)
		{
			case ERR_NOSUCHNICK: //TODO this needs a clearer error message
			case ERR_NOSUCHCHANNEL:
			case ERR_NICKNAMEINUSE:
			case ERR_INVITEONLYCHAN:
			case ERR_BADCHANNELKEY:
				onErrorMessage(responseMatcher.group(2),responseMatcher.group(3));
				break;
			 
		}
	}
	protected void onErrorMessage(String channel, String message)
	{
		getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] %s",channel, message));	
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
		//if(getPlugin().isVerbose())	{
		//	log.log(Level.INFO, String.format("On %tc, %s changed the topic of %s to: %s", date, setBy, channel, topic));
		//}			
		if(changed){
			getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] Topic changed: %s", channel, topic));
		} else {
			getPlayer().sendMessage(ChatColor.YELLOW + String.format("[%s] Topic: %s", channel, topic));
			//getPlayer().sendMessage(String.format("Topic set by %s [%tc]", setBy, date));
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
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
	{
		player.sendMessage(ChatColor.YELLOW + String.format("[%s] %s kicked by %s: %s", channel, recipientNick, kickerNick, reason));
	}
	public Player getPlayer()
	{
		return player;
	}
	public IRCTransport getPlugin()
	{
		return plugin;
	}
	public void shutdown()
	{
		shuttingDown=true;
		disconnect();
	}
	public boolean isShuttingDown()
	{
		return shuttingDown;
	}
}
