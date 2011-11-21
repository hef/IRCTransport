/**
 * 
 */
package hef.IRCTransport;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author hef
 *
 */
public class IRCTransportCommandExecutor implements CommandExecutor {
	IRCTransport plugin;
	Logger log;
	IRCTransportCommandExecutor(IRCTransport plugin)
	{
		this.plugin = plugin;
		log = plugin.getServer().getLogger();
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
        if (plugin.getConfig().getBoolean("verbose")) {
            log.log(Level.INFO, String.format(
                    "Command '%s' received from %s with %d arguments",
                    commandLabel, sender, args.length));
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Irc commands are only runnable as a Player");
            return false;
        }
        Player player = (Player) sender;
        IrcAgent bot = plugin.getBots().get(player.getEntityId());
        String commandName = command.getName();
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
    
    /**
     * Get information about a nick
     * @param bot The Target IRC Agent
     * @param args a single element array of a nick
     * @return parse success
     */
    public boolean whois(final IrcAgent bot, final String[] args) {
        if (args.length == 1) {
            bot.whois(args[0]);
            return true;
        }
        return false;
    }
    
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
}
