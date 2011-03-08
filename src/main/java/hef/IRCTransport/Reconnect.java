package hef.IRCTransport;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

public class Reconnect implements Runnable {
	private IrcAgent agent;
	private static final Logger log = Logger.getLogger("Minecraft");
	public Reconnect(IrcAgent agent)
	{
		this.agent=agent;
	}

	public void run()
	{
		agent.player.sendMessage("Attempting to reconnect to chat server");
		try {
			agent.reconnect();
		} catch (NickAlreadyInUseException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			agent.player.sendMessage("Failed to connect to Chat Server.");
			if(e.getMessage().equalsIgnoreCase("Connection refused"))
			{
				agent.player.sendMessage("Failed to connect to Chat Server.");
				//400 seems to be 20 seconds
				agent.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(agent.plugin, this, 400);
			}
			else
			{
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		} catch (IrcException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}
