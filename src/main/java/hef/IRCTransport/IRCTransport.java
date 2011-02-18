package hef.IRCTransport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
/**
 * IRCTransport for Bukkit
 * 
 * @author hef
 */
public class IRCTransport extends JavaPlugin {
	private final IRCTransportPlayerListener playerListener = new IRCTransportPlayerListener(
			this);
	final HashMap<Player, IrcAgent> bots = new HashMap<Player, IrcAgent>();
	String ircserver = "";
	String autojoin ="";
	boolean verbose;
	private static final Logger log = Logger.getLogger("Minecraft");
	

	public IRCTransport(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		PluginDescriptionFile pdfFile = this.getDescription();
		
		FileInputStream spf;
		Properties sp = new Properties();
		try {
			spf = new FileInputStream("server.properties");
			sp.load(spf);
			this.ircserver = sp.getProperty("irc.server","");
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		if(this.ircserver.equals(""))
		{
			log.log(Level.SEVERE, pdfFile.getName() + ": set \"irc.server\" in server.properties" );		
			return;
		}
		this.autojoin = sp.getProperty("irc.autojoin", "");
		this.verbose = Boolean.parseBoolean(sp.getProperty("irc.verbose", "false"));
	
		System.out.println(pdfFile.getFullName() + " is enabled!");
		//Event Registration
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
	}
	private static String makeMessage(String[] args, int position)
	{
		String message = new String();
		for(int i = position; i < args.length; ++i)
			message += args[i] + " ";
		return message;
	}

	public void onDisable() {

		System.out.println("Goodbye world!");
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getFullName() + " is disabled" );
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		Player player = (Player) sender;
		IrcAgent bot = bots.get(player);
		String commandName = command.getName().toLowerCase();

		if (commandName.equals("join"))
			return join(bot, args);
		else if(commandName.equals("leave"))
			return leave(bot, args);
		else if(commandName.equals("channel"))
			return channel(bot, args);
		else if(commandName.equals("msg"))
			return privateMessage(bot,args);
		else if(commandName.equals("nick"))
			return nick(bot,args);
		else if(commandName.equals("names"))
			return names(bot,args);
		else if(command.equals("me"))
			return action(bot,args);
		return false;
	}
	public boolean join(IrcAgent bot, String[] args)
	{
		bot.joinChannel(args[0]);
		return true;
	}
	public boolean leave(IrcAgent bot, String[] args)
	{
		if( args.length == 1  )
		{
			bot.partChannel(args[0]);
			return true;
		}
		else if(args.length > 1)
		{
			String message = makeMessage(args,1);
			bot.partChannel(args[0], message);
			return true;
		}
		return false;
	}
	public boolean channel(IrcAgent bot, String[] args)
	{
		if(args.length == 1)
		{
			bot.activeChannel = args[0];
			return true;
		}
		return false;
	}
	public boolean privateMessage(IrcAgent bot, String[] args)
	{
		if(args.length > 1)
		{
			String message = makeMessage(args, 1);
			bot.sendMessage(args[0], message);
		}
		return false;
	}
	public boolean nick(IrcAgent bot, String[] args)
	{
		if(args.length==1)
		{
			bot.changeNick(args[0]);
			if(!bot.getNick().equalsIgnoreCase(args[0]))
				//TODO: print error message to player.
			return true;
		}
		return false;
	}
	public boolean names(IrcAgent bot, String[] args)
	{
		if(args.length < 1)
		{
			bot.names();
			return true;
		}
		else
		{
			bot.names(args[0]);
			return true;
		}
			
	}
	public boolean action(IrcAgent bot, String[] args)
	{
		if(args.length > 1)
		{
			String message = makeMessage(args, 0);
			bot.sendAction(message);
			return true;
		}
		return false;
	}
	
	
}
