/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hef.IRCTransport;

import org.bukkit.entity.Player;

/**
 * Initializes an IRC Agent.  This was moved into a Runnable as a result of
 * profiling.  IRC Agent construction was causing bogs in the main thread.
 */
public class InitializeAgent implements Runnable {
    /** Handle on plugin. */
    private IRCTransport plugin;
    /** Handle on player. */
    private Player player;

    /**
     * Setup the agent initialization job.
     * @param myPlugin the IRCTRansport plugin
     * @param myPlayer the bukkit player handle.
     */
    InitializeAgent(final IRCTransport myPlugin, final Player myPlayer) {
        this.plugin = myPlugin;
        this.player = myPlayer;
    }

    /**
     * Constructs the irc agent,
     * Adds the agent to the list of bots,
     * and connects the agent to IRC.
     */
    @Override
    public void run() {
        int playerID = player.getEntityId();
        IrcAgent agent = new IrcAgent(plugin, player);
        agent.getListenerManager().addListener(plugin.getListener());
        plugin.getBots().put(playerID, agent);
        plugin.getLogger().info(String.format("Created agent for Player ID: %d name: %s", playerID, player.getName()));
        new Connect(agent).run();
    }
}
