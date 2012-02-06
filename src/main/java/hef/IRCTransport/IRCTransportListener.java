package hef.IRCTransport;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events.
 */
 public class IRCTransportListener implements Listener {
    /** Maps to retrieve associated IrcAggent from player. */
    private HashMap<Integer,IrcAgent> bots;
    /** Reference to the parent plugin. */
    private final IRCTransport plugin;
    /** Logger object. */
    private Logger log;

    /**
     * @param instance A reference to the plugin.
     */
    public IRCTransportListener(final IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
        log = instance.getServer().getLogger();
    }

    @EventHandler
    public void onPlayerChat(final PlayerChatEvent event) {
        IrcAgent bot = this.bots.get(event.getPlayer().getEntityId());
        if (bot.isConnected()) {
            bot.sendMessage(event.getMessage());
            // prevent messages from being displayed twice.
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        IrcAgent agent;
        
        try {
        	agent = new IrcAgent(plugin, player);
        } catch (Exception e) {
        	return;
        }
        
        this.bots.put(playerID, agent);
        
        log.info(String.format("Created agent for Player ID: %d name: %s", playerID, player.getName()));
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        this.bots.get(playerID).shutdown();
        this.bots.remove(playerID);
        log.info(String.format("Removed agent for Player ID: %d name: %s", playerID, player.getName()));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
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
            
            // This winds up being sent to the player via IRC,
            // so suppress the default message
            deathEvent.setDeathMessage(null);
          }
    	}
    }
}
