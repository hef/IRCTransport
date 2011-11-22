package hef.IRCTransport;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * IRCTransport for Bukkit.
 */
public class IRCTransport extends JavaPlugin {

    /** The logging obect. Used internal to write to the console. */
    private static final Logger LOG = Logger.getLogger("Minecraft");
    /** MC Player to IRCAgent map */
    private final TIntObjectHashMap<IrcAgent> bots = new TIntObjectHashMap<IrcAgent>();
    /** The player action handler. */
    private IRCTransportPlayerListener playerListener;

    /**
     * Gets the maping of Bukkit Players to IRCAgents.
     * @return the map of player's to agents.
     */
    public TIntObjectHashMap<IrcAgent> getBots() {
        return this.bots;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(AgentSettings.class);
        return list;
    }

    /**
     * Create the database to store plugin settings. This method creates the
     * ebean.properties file. It's not strictly necessary, but it silences a
     * bukkit error message.
     */
    public void initDatabase() {
        // Always do this, since it will quiet unnecessary warnings
        File file = new File("ebean.properties");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                LOG.log(Level.WARNING, this.getDescription().getName()
                        + " Failed to create ebean.properties file.");
            }
        }

        // The rest we only try if the database is actually in use
        try {
            getDatabase().find(AgentSettings.class).findRowCount();
        } catch (PersistenceException e) {
            LOG.log(Level.INFO, this.getDescription().getName()
                    + " configuring database for the first time");
            installDDL();
        }
    }

    /**
     * Turnoff and cleanup the plugin. sets shutdown flag Signs all users out of
     * IRC
     */
    @Override
    public void onDisable() {
        // disconnect all agents
        TIntObjectProcedure<IrcAgent> shutdown = new ShutdownProcedure();
        bots.forEachEntry(shutdown);
        bots.clear();
        LOG.log(Level.INFO, this.getDescription().getFullName()
                + " is disabled");
    }

    /**
     * Turns on the plugin. Signs all users into IRC sets shutdown flag to
     * false. parses config file. registers events with bukkit.
     */
    @Override
    public void onEnable() {
        this.playerListener = new IRCTransportPlayerListener(this);
        getConfig().options().copyDefaults(true);
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdfFile = this.getDescription();
        if (getConfig().getString("server.address") == null) {
            LOG.severe(pdfFile.getName() + ": set \"server.address\" in "
                    + this.getDataFolder() + "/config.yml");
            return;
        }

        getConfig()
                .options()
                .header("Config File for IRCTransport\nSee the website for more information");
        saveConfig();
        initDatabase();

        // establish list of players
        Player[] players = getServer().getOnlinePlayers();
        for (Player player : players) {
            this.bots.put(player.getEntityId(), new IrcAgent(this, player));
        }

        // register for events we care about
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener,
                Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
                Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
                Priority.Normal, this);

        // set command executors
        IRCTransportCommandExecutor commandExecutor = new IRCTransportCommandExecutor(this);
        getCommand("join").setExecutor(commandExecutor);
        getCommand("leave").setExecutor(commandExecutor);
        getCommand("channel").setExecutor(commandExecutor);
        getCommand("msg").setExecutor(commandExecutor);
        getCommand("nick").setExecutor(commandExecutor);
        getCommand("names").setExecutor(commandExecutor);
        getCommand("me").setExecutor(commandExecutor);
        getCommand("topic").setExecutor(commandExecutor);
        getCommand("whois").setExecutor(commandExecutor);
        LOG.log(Level.INFO, pdfFile.getFullName() + " is enabled!");
    }

    /** ShutdownProcedure for shutting down agents. */
    private class ShutdownProcedure implements TIntObjectProcedure<IrcAgent> {
        /** Shutdown an agent.
         * @param a The key
         * @param b The Agent to shutdown
         */
        @Override
        public boolean execute(final int a, final IrcAgent b) {
            b.shutdown();
            return false;
        }
    }
}
