package hef.IRCTransport;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events
 * 
 * @author hef
 */
public class IRCTransportPlayerListener extends PlayerListener {
    private HashMap<Player, IrcAgent> bots;
    private final IRCTransport plugin;

    public IRCTransportPlayerListener(IRCTransport instance) {
        this.bots = instance.getBots();
        plugin = instance;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        IrcAgent bot = this.bots.get(event.getPlayer());
        bot.sendMessage(event.getMessage());
        // prevent messages from being displayed twice.
        event.setCancelled(true);
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.bots.put(event.getPlayer(),
                new IrcAgent(plugin, event.getPlayer()));
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.bots.get(event.getPlayer()).shutdown();
        this.bots.remove(event.getPlayer());
    }
}
