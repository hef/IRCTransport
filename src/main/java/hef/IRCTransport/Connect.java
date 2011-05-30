package hef.IRCTransport;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

/** Connection Task
 * This class us a Runnable class for connecting to the irc server.  It will reschedule a connection
 * @author hef
 *
 */
public class Connect implements Runnable {
	private IrcAgent agent;
	private static final Logger log = Logger.getLogger("Minecraft");
	/** Create a Connection instance
	 * Pass this from IrcAgent so we have access
	 * @param agent pass in this from the calling IrcAgent
	 */
	public Connect(IrcAgent agent)
	{
		this.agent=agent;
	}
	/** Run the connection task
	 * This task will schedule another connection task if it fails to connect.
	 */
	public void run()
	{
		//If a task where scheduled, and the user disconnected, this would
		//continually keep the agent alive.
		if(!agent.isShuttingDown())
		{
			try {
				//If we never set the server i.e. havn't connected yet
				if(agent.getServer()==null)
				{
					agent.connect(agent.getPlugin().getIrcServer(), agent.getPlugin().getIrcPort(), agent.getPlugin().getIrcPassword());
				}
				else
				{
					//reconnect should recycle settings the user already has
					agent.reconnect();
				}
			} catch (NickAlreadyInUseException e) {
				//This should not happen, the agent is set to auto retry new names
				log.log(Level.SEVERE, e.getMessage(), e);
			} catch (IOException e) {
				if(e.getMessage().equalsIgnoreCase("Connection refused"))
				{
					agent.getPlayer().sendMessage(ChatColor.YELLOW + "Failed to connect to Chat Server.");
					//400 seems to be 20 seconds
					agent.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(agent.getPlugin(), this, 400);
				}
				else if(e.getMessage().equalsIgnoreCase("Connection reset"))
				{
					agent.getPlayer().sendMessage(ChatColor.YELLOW + "Connection reset while connecting to Chat Server");
					agent.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(agent.getPlugin(), this, 100);
				}
				else
				{
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			} catch (IrcException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}

			//The player may have not gotten then name they wanted.
			agent.getPlayer().setDisplayName(agent.getNick());
			agent.getSettings().setIrcNick(agent.getNick());
			if(!agent.getPlugin().getAutoJoin().equals(""))
			{
				//if no channel key is set
				if(agent.getPlugin().getAutoJoinKey().equals(""))
				{
					agent.joinChannel(agent.getPlugin().getAutoJoin());
				}
				else // channel key is set
				{
					agent.joinChannel(agent.getPlugin().getAutoJoin(),agent.getPlugin().getAutoJoinKey());
				}
				agent.setActiveChannel(agent.getPlugin().getAutoJoin());
			}
		}
	}

}
