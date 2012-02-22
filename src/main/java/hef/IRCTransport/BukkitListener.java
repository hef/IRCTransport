package hef.IRCTransport;

import java.util.logging.Logger;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events.
 */
public class BukkitListener implements Listener {

    /** Maps to retrieve associated IrcAggent from player. */
    private TIntObjectHashMap<IrcAgent> bots;
    /** Reference to the parent plugin. */
    private final IRCTransport plugin;
    /** Logger object. */
    private Logger log;

    /**
     * @param instance A reference to the plugin.
     */
    public BukkitListener(final IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
        log = instance.getServer().getLogger();
    }

    /**
     * Player chat handler.  It cancels that chat event.
     * @param event the player chat event.
     */
    @EventHandler
    public void onPlayerChat(final PlayerChatEvent event) {
        IrcAgent bot = this.bots.get(event.getPlayer().getEntityId());
        if (bot.isConnected()) {
            bot.sendMessage(event.getMessage());
            // prevent messages from being displayed twice.
            event.setCancelled(true);
        }
    }

    /**
     * Creates Player related objects.
     * @param event player join event.
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        IrcAgent agent = new IrcAgent(plugin, player);
        agent.getListenerManager().addListener(plugin.getListener());
        this.bots.put(playerID, agent);
        log.info(String.format("Created agent for Player ID: %d name: %s", playerID, player.getName()));
    }

    /**
     * Removes player from list of players.
     * @param event the player quit event.
     */
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        int playerID = player.getEntityId();
        this.bots.get(playerID).shutdown();
        this.bots.remove(playerID);
        log.info(String.format("Removed agent for Player ID: %d name: %s", playerID, player.getName()));
    }

    /**
     * Sends death message as action on player death.
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();

        IrcAgent bot = this.bots.get(event.getEntity().getEntityId());
        if (bot.isConnected() && deathMessage != null && deathMessage.equals("")) {
            String playerName = ((Player) event.getEntity()).getName();

            // deathMessage seems to be 'playerName died for some reason'
            if (deathMessage.startsWith(playerName)) {
                // So, we just remove the leading playerName, so deaths render as:
                // * ircNick died for some reason
                deathMessage = deathMessage.substring(playerName.length());
            } else {
                // Otherwise, just try to make it look reasonable
                deathMessage = "died: " + deathMessage;
            }
            bot.sendAction(deathMessage.trim());
            // This winds up being sent to the player via IRC,
            // so suppress the default message
            event.setDeathMessage(null);
        }
    }
}
