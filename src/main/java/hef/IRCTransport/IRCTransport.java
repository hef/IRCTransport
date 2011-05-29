package hef.IRCTransport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.PersistenceException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * IRCTransport for Bukkit
 * 
 * @author hef
 */
public class IRCTransport extends JavaPlugin {
	private IRCTransportPlayerListener playerListener;
	private final HashMap<Player, IrcAgent> bots = new HashMap<Player, IrcAgent>();
	private String ircServer = "";
	private int ircPort;
	private String ircPassword ;
	private String autoJoin ="";
	private String autoJoinKey = "";
	private String nickPrefix = "";
	private String nickSuffix = "";
	private boolean verbose;
	private boolean useDB;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	public String getIrcServer()
	{
		return this.ircServer;
	}
	
	public String getAutoJoin()
	{
		return this.autoJoin;
	}
	
	public boolean isVerbose()
	{
		return this.verbose;
	}
	
	public HashMap<Player, IrcAgent> getBots()
	{
		return this.bots;
	}

	public void onEnable() {
		this.playerListener = new IRCTransportPlayerListener(this);
		
		PluginManager pm = getServer().getPluginManager();
		PluginDescriptionFile pdfFile = this.getDescription();
		
		//handle plugin settings.
		FileInputStream spf;
		Properties sp = new Properties();
		try {
			spf = new FileInputStream("server.properties");
			sp.load(spf);
			this.ircServer = sp.getProperty("irc.server","");
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
				
		//grab data from server.properties
		this.ircServer = sp.getProperty("irc.server","");
		this.ircPort = Integer.parseInt(sp.getProperty("irc.port", "6667"));
		this.ircPassword = sp.getProperty("irc.password","");
		this.autoJoin = sp.getProperty("irc.autojoin", "");
		this.autoJoinKey = sp.getProperty("irc.autojoinkey", "");
		this.nickPrefix = sp.getProperty("irc.nickprefix", "");
		this.nickSuffix = sp.getProperty("irc.nicksuffix","");
		this.verbose = Boolean.parseBoolean(sp.getProperty("irc.verbose", "false"));
		this.useDB = Boolean.parseBoolean(sp.getProperty("irc.useDB", "false"));
		
		//validate data
		if(this.ircServer.equals(""))
		{
			log.log(Level.SEVERE, pdfFile.getName() + ": set \"irc.server\" in server.properties" );		
			return;
		}
	
		log.log(Level.INFO, pdfFile.getFullName() + " is enabled!");
		
		initDatabase();
		
		//Event Registration
		
        //establish list of players
        Player[] players = getServer().getOnlinePlayers();
        for(Player player: players)
        {
        	this.bots.put(player, new IrcAgent(this,player));
        }
        //register for events we care about
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
	}
	/** Turns arguments into a string
	 * 
	 * @bug bug: multiple spaces are not detected in args strings, so they get turned into a single space.
	 * @param args the list of commands
	 * @param position First word of non-command text
	 * @return a string representing the non-command text.
	 */
	private static String makeMessage(String[] args, int position)
	{
		String message = new String();
		for(int i = position; i < args.length; ++i)
			message += args[i] + " ";
		return message;
	}
	
	public void initDatabase()
	{
      // Always do this, since it will quiet unnecessary warnings
      File file = new File("ebean.properties");
      if(!file.exists())
      {
        try
        {
          file.createNewFile();
        }
        catch (Exception e)
        {
          log.log(Level.WARNING, this.getDescription().getName() + " Failed to create ebean.properties file.");
        }
      }

      // The rest we only try if the database is actually in use
      if (useDB)
      {
        try
        {
          getDatabase().find(IrcPlayerPersistentState.class).findRowCount();
        }
        catch (PersistenceException e)
        {
    	  log.log(Level.INFO, this.getDescription().getName() + " configuring database for the first time" );
          installDDL();
        }
      }
	}

	public void onDisable() {
		//disconnect all agents
		for(Entry<Player, IrcAgent> entry: bots.entrySet())
		{
			entry.getValue().shutdown();
		}
		bots.clear();
		
		log.log(Level.INFO, this.getDescription().getFullName() + " is disabled" );
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if(this.isVerbose()){
			log.log(Level.INFO, String.format("Command '%s' received from %s with %d arguments", commandLabel, sender, args.length));
		}
		
		if(! (sender instanceof Player) )
		{
			sender.sendMessage("Irc commands are only runnable as a Player");
			return false;
		}
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
		else if(commandName.equals("me"))
			return action(bot,args);
		else if(commandName.equals("topic"))
			return topic(bot, args);
		return false;
	}
	public boolean join(IrcAgent bot, String[] args)
	{
		if(args.length == 1)
		{
			bot.joinChannel(args[0]);
			return true;
		}
		else if(args.length == 2)
		{
			bot.joinChannel(args[0], args[1]);
			return true;
		}
		return false;
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
			bot.setActiveChannel(args[0]);
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
			IrcPlayerPersistentState state = getPersistentState(bot.getPlayer());
			state.setIrcNick(args[0]);
			savePersistentState(state);
			
			bot.changeNick(args[0]);
			if(!bot.getNick().equalsIgnoreCase(args[0]))
				//TODO: print error message to player.
			return true;
		}
		return false;
	}
	
    @Override
    public List<Class<?>> getDatabaseClasses()
    {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(IrcPlayerPersistentState.class);
        return list;
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
	public boolean topic(IrcAgent bot, String[] args)
	{
		if(args.length < 1){
			bot.topic();
			return true;
		} else {
			bot.setTopic(makeMessage(args, 0));
			return true;
		}
	}
	
	public boolean action(IrcAgent bot, String[] args)
	{
		if(args.length > 0)
		{
			String message = makeMessage(args, 0);
			bot.sendAction(message);
			return true;
		}
		return false;
	}
	
	protected IrcPlayerPersistentState getPersistentState(Player player)
	{
		IrcPlayerPersistentState state = null;
		if (useDB)
		{
		  state = getDatabase().find(IrcPlayerPersistentState.class, player.getName());
		}
		
		if (null == state)
		{
		  state = new IrcPlayerPersistentState(player);
		}
		return state;
	}

	protected void savePersistentState(IrcPlayerPersistentState state)
	{
		if (useDB)
		{
		  getDatabase().save(state);
		}
	}
	
	/**
	 * @return the ircPort
	 */
	public int getIrcPort()
	{
		return ircPort;
	}

	/**
	 * @return the ircPassword
	 */
	public String getIrcPassword()
	{
		return ircPassword;
	}

	/**
	 * @return the autoJoinKey
	 */
	public String getAutoJoinKey()
	{
		return autoJoinKey;
	}

	/**
	 * Nick prefix is only used as a default
	 * @return the nickPrefix
	 */
	public String getNickPrefix() {
		return nickPrefix;
	}

	/**
	 * nick suffix is only used as a default
	 * If the plugin has a suffix of "_mc" and a player with nick of "player" will become "player_mc"
	 * @return the nickSuffix
	 */
	public String getNickSuffix() {
		return nickSuffix;
	}
}
