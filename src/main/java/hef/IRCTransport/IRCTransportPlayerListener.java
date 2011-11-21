package hef.IRCTransport;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events.
 */
 public final class IRCTransportPlayerListener extends PlayerListener {
    /** Maps to retrieve associated IrcAggent from player. */
    private TIntObjectHashMap<IrcAgent> bots;
    /** Reference to the parent plugin. */
    private final IRCTransport plugin;

    /**
     * @param instance A reference to the plugin.
     */
    public IRCTransportPlayerListener(final IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
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
        this.bots.put(event.getPlayer().getEntityId(),
                new IrcAgent(plugin, event.getPlayer()));
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.bots.get(event.getPlayer().getEntityId()).shutdown();
        this.bots.remove(event.getPlayer().getEntityId());
    }
}
