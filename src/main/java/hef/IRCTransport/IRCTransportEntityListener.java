package hef.IRCTransport;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import org.bukkit.entity.Player;


/**
 * Handle events for all Player related events.
 */
 public class IRCTransportEntityListener extends EntityListener {
    /** Maps to retrieve associated IrcAggent from player. */
    private TIntObjectHashMap<IrcAgent> bots;
    /** Reference to the parent plugin. */
    private final IRCTransport plugin;

    /**
     * @param instance A reference to the plugin.
     */
    public IRCTransportEntityListener(final IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
    }

    @Override
    public void onEntityDeath (EntityDeathEvent event)
    {
    	if (event instanceof PlayerDeathEvent)
    	{
          PlayerDeathEvent deathEvent = (PlayerDeathEvent)event;
          String deathMessage = deathEvent.getDeathMessage();
          
          IrcAgent bot = this.bots.get(deathEvent.getEntity().getEntityId());
          if (bot.isConnected() && deathMessage != null && deathMessage != "")
          {
        	String playerName = ((Player)event.getEntity()).getName();
        	System.out.println("PN=>" + playerName + "<");

        	// deathMessage seems to be 'playerName died for some reason'
        	if (deathMessage.startsWith(playerName))
        	{
        	  // So, we just remove the leading playerName, so deaths render as:
        	  // * ircNick died for some reason
        	  deathMessage = deathMessage.substring(playerName.length());
        	}
        	else
        	{
        	  // Otherwise, just try to make it look reasonable
        	  deathMessage = "died: " + deathMessage;
        	}
        	        	
            bot.sendAction(deathMessage.trim());
          }
    	}
    }
}
