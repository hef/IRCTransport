package hef.IRCTransport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * IRCTransport for Bukkit.
 * @author hef
 */
public final class IRCTransport extends JavaPlugin {
    /** The logging obect.  Used internal to write to the console. */
    private static final Logger LOG = Logger.getLogger("Minecraft");
    protected static FileConfiguration CONFIG;
    private final HashMap<Player, IrcAgent> bots = new HashMap<Player, IrcAgent>();
    private IRCTransportPlayerListener playerListener;

    /** Turns arguments into a string.
     * @bug bug: multiple spaces are not detected in args strings, so they get
     *      turned into a single space.
     * @param args
     *            the list of commands
     * @param position
     *            First word of non-command text
     * @return a string representing the non-command text.
     */
    private static String makeMessage(final String[] args, final int position) {
        String message = new String();
        for (int i = position; i < args.length; ++i) {
            message += args[i] + " ";
        }
        return message;
    }

    /** The IRC /me handler.
     * @param bot The IRC Agent that needs to handle the action
     * @param args The list of words to use as the "action"
     * @return parse success
     */
    public boolean action(final IrcAgent bot, final String[] args) {
        if (args.length > 0) {
            String message = makeMessage(args, 0);
            bot.sendAction(message);
            return true;
        }
        return false;
    }

    /** Change the active channel.
     * The agent must already be in the channel.
     * @param bot The IRC agent that needs to handle the action
     * @param args a 1 element array of the channel to switch to.
     * @return parse success
     */
    public boolean channel(final IrcAgent bot, final String[] args) {
        if (args.length == 1) {
            bot.setActiveChannel(args[0]);
            return true;
        }
        return false;
    }

    /** Gets the maping of Bukkit Players to IRCAgents.
     * @return the map of player's to agents.
     */
    public HashMap<Player, IrcAgent> getBots() {
        return this.bots;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(AgentSettings.class);
        return list;
    }

    /** Create the database to store plugin settings.
     * This method creates the ebean.properties file.
     * It's not strictly necessary, but it silences a bukkit error message.
     */
    public void initDatabase() {
        // Always do this, since it will quiet unnecessary warnings
        File file = new File("ebean.properties");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                LOG.log(Level.WARNING, this.getDescription().getName() + " Failed to create ebean.properties file.");
            }
        }

        // The rest we only try if the database is actually in use
        try {
            getDatabase().find(AgentSettings.class).findRowCount();
        } catch (PersistenceException e) {
            LOG.log(Level.INFO, this.getDescription().getName() + " configuring database for the first time");
            installDDL();
        }
    }

    /** Join a Channel
     * args can be 1 or 2 elements.
     * 1st element: channel name
     * 2nd element: channel key
     * @param bot irc agent
     * @param args array of channel and optionally key
     * @return parse success
     */
    public boolean join(final IrcAgent bot, final String[] args) {
        if (args.length == 1) {
            bot.joinChannel(args[0]);
            return true;
        } else if (args.length == 2) {
            bot.joinChannel(args[0], args[1]);
            return true;
        }
        return false;
    }

    /** part from a channel.
     * @param bot active IRC agent
     * @param args a channel to leave
     * @return parse success
     */
    public boolean leave(final IrcAgent bot, final String[] args) {
        if (args.length == 1) {
            bot.partChannel(args[0]);
            return true;
        } else if (args.length > 1) {
            String message = makeMessage(args, 1);
            bot.partChannel(args[0], message);
            return true;
        }
        return false;
    }

    /** Get a list of names from active channel.
     * @param bot active IRC agent
     * @param args 0 or 1 elements.  the 1st element can be a channel name to get names from.
     * @return parse success
     */
    public boolean names(final IrcAgent bot, final String[] args) {
        if (args.length < 1) {
            bot.names();
            return true;
        } else {
            bot.names(args[0]);
            return true;
        }
    }

    /** Change Nickname.
     * @param bot active IRC agent.
     * @param args 1 element array of the nick to change to
     * @return parse success.
     */
    public boolean nick(final IrcAgent bot, final String[] args) {
        if (args.length == 1) {
            bot.changeNick(args[0]);
            return true;
        }
        return false;
    }

    /** The main command handler.
     * Bukkit calls this to pass commands into the plugin.
     * @param sender The sender, Usually a player.  It could be the console, but that is not supported by this plugin.
     * @param command The command to execute
     * @param commandLabel The command Alias used to execute this command
     * @param args All the args past to the command
     * @return Parse Success
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        if (CONFIG.getBoolean("verbose")) {
            LOG.log(Level.INFO, String.format(
                    "Command '%s' received from %s with %d arguments",
                    commandLabel, sender, args.length));
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Irc commands are only runnable as a Player");
            return false;
        }
        Player player = (Player) sender;
        IrcAgent bot = bots.get(player);
        String commandName = command.getName().toLowerCase();

        if (commandName.equals("join")) {
            return join(bot, args);
        } else if (commandName.equals("leave")) {
            return leave(bot, args);
        } else if (commandName.equals("channel")) {
            return channel(bot, args);
        } else if (commandName.equals("msg")) {
            return privateMessage(bot, args);
        } else if (commandName.equals("nick")) {
            return nick(bot, args);
        } else if (commandName.equals("names")) {
            return names(bot, args);
        } else if (commandName.equals("me")) {
            return action(bot, args);
        } else if (commandName.equals("topic")) {
            return topic(bot, args);
        } else if (commandName.equals("whois")) {
        	return whois(bot, args);
        }
        return false;
    }

    /** Turnoff and cleanup the plugin.
     * sets shutdown flag
     * Signs all users out of IRC
     */
    @Override
    public void onDisable() {
        // disconnect all agents
        for (Entry<Player, IrcAgent> entry : bots.entrySet()) {
            entry.getValue().shutdown();
        }
        bots.clear();

        LOG.log(Level.INFO, this.getDescription().getFullName() + " is disabled");
    }

    /**
     * Turns on the plugin.
     * Signs all users into IRC
     * sets shutdown flag to false.
     * parses config file.
     * registers events with bukkit.
     */
    @Override
    public void onEnable() {
        this.playerListener = new IRCTransportPlayerListener(this);
        CONFIG = getConfig();
        CONFIG.options().copyDefaults(true);
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdfFile = this.getDescription();
        if (CONFIG.getString("server") == null) {
            LOG.severe(pdfFile.getName() + ": set \"irc.server\" in server.properties");
            return;
        }
        initDatabase();
        // establish list of players
        Player[] players = getServer().getOnlinePlayers();
        for (Player player : players) {
            this.bots.put(player, new IrcAgent(this, player));
        }
        // register for events we care about
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        LOG.log(Level.INFO, pdfFile.getFullName() + " is enabled!");
    }

    /** Send a private message in IRC.
     * @param bot Target IRC agent.
     * @param args element 1 is the IRC reciever.  The rest are the words to send.
     * @return parse success
     */
    public boolean privateMessage(final IrcAgent bot, final String[] args) {
        if (args.length > 1) {
            String message = makeMessage(args, 1);
            bot.sendMessage(args[0], message);
        }
        return false;
    }

    /** Set or get an IRC topic.
     * @param bot The target IRC Agent
     * @param args empty to get topic, non-empty to set topic.
     * @return parse success
     */
    public boolean topic(final IrcAgent bot, final String[] args) {
        if (args.length < 1) {
            bot.topic();
            return true;
        } else {
            bot.setTopic(makeMessage(args, 0));
            return true;
        }
    }
    public boolean whois(final IrcAgent bot, final String[] args)
    {
    	if (args.length==1)
    	{
    		bot.whois(args[0]);
    		return true;
    	}
    	return false;
    }
}
