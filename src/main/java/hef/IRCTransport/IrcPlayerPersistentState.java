package hef.IRCTransport;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;	

import org.bukkit.entity.Player;

import com.avaje.ebean.annotation.Sql;
import com.avaje.ebean.validation.NotNull;
// import com.avaje.ebean.annotation.SqlSelect;

@Entity
@Table(name="IRCTransport_players")
public class IrcPlayerPersistentState
{
  public IrcPlayerPersistentState()
  {
  }
  
  public IrcPlayerPersistentState(Player player)
  {
    setPlayerName(player.getName());
  }
  // FIXME: Serial #?
  @Id
  @NotNull
  private String playerName;
  
  private String ircNick = null;

  void setPlayerName(String playerName)
  {
    this.playerName = playerName;
  }
  
  public void setIrcNick(String ircNick)
  {
	  this.ircNick = ircNick;
  }
  
  public String getIrcNick()
  {
    return ircNick;
  }
}
