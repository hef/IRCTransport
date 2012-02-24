package hef.IRCTransport;

import java.util.logging.Logger;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events.
 */
 public class IRCTransportPlayerListener extends PlayerListener {
    /** Maps to retrieve associated IrcAggent from player. */
    private TIntObjectHashMap<IrcAgent> bots;
    /** Reference to the parent plugin. */
    private final IRCTransport plugin;
    /** Logger object. */
    private Logger log;

    /**
     * @param instance A reference to the plugin.
     */
    public IRCTransportPlayerListener(final IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
        log = instance.getServer().getLogger();
    }

    @Override
    public void onPlayerChat(final PlayerChatEvent event) {
        IrcAgent bot = this.bots.get(event.getPlayer().getEntityId());
        if (bot.isConnected()) {
            bot.sendMessage(event.getMessage());
            // prevent messages from being displayed twice.
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
    	for (final int x : bots.keys())
    	{
    		IrcAgent current = bots.get(x);
    		if (current.getPlayer().getName().equals(player.getName()))
    			current.shutdown();
    		current.getPlayer().getName();
    	}
    	
        int playerID = player.getEntityId();
        IrcAgent agent = new IrcAgent(plugin, player);
        agent.getListenerManager().addListener(plugin.getListener());
        this.bots.put(playerID, agent);
        log.info(String.format("Created agent for Player ID: %d name: %s", playerID, player.getName()));
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        this.bots.get(playerID).shutdown();
        this.bots.remove(playerID);
        log.info(String.format("Removed agent for Player ID: %d name: %s", playerID, player.getName()));
    }
}
