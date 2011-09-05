package hef.IRCTransport;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "players")
public class AgentSettings {
	@NotEmpty
	private String ircNick = null;

	@Id
	@NotNull
	private String playerName;

	public AgentSettings() {
	}

	public AgentSettings(Player player) {
		setPlayerName(player.getName());
	}

	public String getIrcNick() {
		return ircNick;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setIrcNick(String ircNick) {
		this.ircNick = ircNick;
	}

	// If you don't have this, the persistence stuff gets cranky
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
