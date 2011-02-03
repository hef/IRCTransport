package com.bukkit.hef.IRCTransport;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * IRCTransport block listener
 * @author hef
 */
public class IRCTransportBlockListener extends BlockListener {
    private final IRCTransport plugin;

    public IRCTransportBlockListener(final IRCTransport plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}
