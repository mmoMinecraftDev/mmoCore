/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Core;

import com.avaje.ebean.EbeanServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.Core.util.MyDatabase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public abstract class MMOPlugin extends JavaPlugin {

	/**
	 * mmoSupport() BitSet values
	 */
	public final int MMO_PLAYER = 1; // Calls onSpoutCraftPlayer() when someone joins or after onEnable
	public final int MMO_NO_CONFIG = 2; // No config file used for this plugin
	public final int MMO_AUTO_EXTRACT = 3; // Has *.png files inside the plugin.jar
	public final int MMO_DATABASE = 4; // We have a database
	/**
	 * Static global variables
	 */
	static public boolean hasSpout = false;
	static public boolean singleFolder = false;
	/**
	 * Private and protected variables
	 */
	private MyDatabase database;
	protected PluginDescriptionFile description;
	protected Configuration cfg;
	protected PluginManager pm;
	protected Server server;
	protected Logger logger;
	protected String title;
	protected String prefix;
	protected static MMOPlugin mmoCore;
	protected MMOPlugin plugin;
	/**
	 * Public variables
	 */
	public int version = 0, revision = 0;
	public boolean update = false; // If there's an update available

	@Override
	public void onEnable() {
		if (this instanceof MMOCore) {
			mmoCore = (MMOCore) this;
		}
		plugin = this;
		logger = Logger.getLogger("Minecraft");
		description = getDescription();
		server = getServer();
		pm = server.getPluginManager();
		title = description.getName().replace("^mmo", "");
		prefix = ChatColor.GREEN + "[" + ChatColor.AQUA + title + ChatColor.GREEN + "] " + ChatColor.WHITE;
		hasSpout = server.getPluginManager().isPluginEnabled("Spout");
		String oldVersion[] = description.getVersion().split("\\.");
		if (oldVersion.length == 2) {
			version = Integer.parseInt(oldVersion[0]);
			revision = Integer.parseInt(oldVersion[1]);
		} else {
			log("Unable to determine version!");
		}

		// Cache the various event APIs
		if (title.equals("Chat")) {
			MMO.mmoChatAPI = true;
		} else if (title.equals("Damage")) {
			MMO.mmoDamageAPI = true;
		} else if (title.equals("Info")) {
			MMO.mmoInfoAPI = true;
		} else if (title.equals("Party")) {
			MMO.mmoPartyAPI = true;
		} else if (title.equals("Skill")) {
			MMO.mmoSkillAPI = true;
		}

		if (!singleFolder && new File("plugins/mmoMinecraft").exists()) {
			singleFolder = true;
		}
		/**
		 * Move plugins/mmoPlugin/* to plugins/mmoMinecraft/*
		 */
		if (singleFolder && super.getDataFolder().exists()) {
			try {
				for (File from : super.getDataFolder().listFiles()) {
					String name = from.getName();
					if (name.equalsIgnoreCase("config.yml")) {
						name = description.getName() + ".yml";
					}
					if (!from.renameTo(new File(getDataFolder(), name))) {
						log("Unable to move file: " + from.getName());
					}
				}
				getDataFolder().delete();
			} catch (Exception e) {
			}
		}

		log("Enabled " + description.getFullName());

		BitSet support = mmoSupport(new BitSet());

		if (!support.get(MMO_NO_CONFIG)) {
			cfg = new Configuration(new File(getDataFolder(), description.getName() + ".yml"));
			cfg.load();
			loadConfiguration(cfg);
			if (!cfg.getKeys().isEmpty()) {
				cfg.setHeader("#" + title + " Configuration");
				cfg.save();
			}
		}
		if (support.get(MMO_DATABASE)) {
			getDatabase();
		}
		if (hasSpout) {
			if (support.get(MMO_PLAYER)) {
				MMOCore.support_mmo_player.add(this);
			}
			if (support.get(MMO_AUTO_EXTRACT)) {
				try {
					boolean found = false;
					JarFile jar = new JarFile(getFile());
					Enumeration entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry file = (JarEntry) entries.nextElement();
						String name = file.getName();
						if (name.matches(".*\\.png$|^config.yml$")) {
							if (!found) {
								new File(getDataFolder(), description.getName()).mkdir();
								found = true;
							}
							File f = new File(getDataFolder(), description.getName() + File.separator + name);
							if (!f.exists()) {
								InputStream is = jar.getInputStream(file);
								FileOutputStream fos = new FileOutputStream(f);
								while (is.available() > 0) {
									fos.write(is.read());
								}
								fos.close();
								is.close();
							}
							SpoutManager.getFileManager().addToCache(plugin, f);
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void onDisable() {
		// Cache the various event APIs
		if (title.equals("Chat")) {
			MMO.mmoChatAPI = false;
		} else if (title.equals("Damage")) {
			MMO.mmoDamageAPI = false;
		} else if (title.equals("Info")) {
			MMO.mmoInfoAPI = false;
		} else if (title.equals("Party")) {
			MMO.mmoPartyAPI = false;
		} else if (title.equals("Skill")) {
			MMO.mmoSkillAPI = false;
		}

		log("Disabled " + description.getFullName());
	}

	@Override
	public File getDataFolder() {
		if (singleFolder) {
			return new File("plugins/mmoMinecraft");
		}
		return super.getDataFolder();
	}

	/**
	 * Load the configuration - don't save or anything...
	 * @param cfg 
	 */
	public void loadConfiguration(Configuration cfg) {
	}

	/**
	 * Supply a bitfield of shortcuts for MMOPlugin to handle
	 * @return 
	 */
	public BitSet mmoSupport(BitSet support) {
		return support;
	}

	/**
	 * Called when any player joins - need to return MMO_PLAYER from mmoSupport()
	 * @param player 
	 */
	public void onPlayerJoin(Player player) {
	}

	/**
	 * Called for every Spoutcraft player on /reload and PlayerJoin - need to return MMO_PLAYER from mmoSupport()
	 * @param player
	 */
	public void onSpoutCraftPlayer(SpoutPlayer player) {
	}

	/**
	 * Called for every *NON* Spoutcraft player on /reload and PlayerJoin - need to return MMO_PLAYER from mmoSupport()
	 * @param player
	 */
	public void onNormalPlayer(Player player) {
	}

	/**
	 * Called when any player quits or is kicked - need to return MMO_PLAYER from mmoSupport()
	 * @param player 
	 */
	public void onPlayerQuit(Player player) {
	}

	/**
	 * Send a message to the log
	 * @param text A format style string
	 * @param args
	 */
	public void log(String text, Object... args) {
		logger.log(Level.INFO, "[" + description.getName() + "] " + String.format(text, args));
	}

	/**
	 * Return the fill pathname for an auto-extracted file
	 * @param name
	 * @return 
	 */
	public String getResource(String name) {
		String path = getDataFolder() + File.separator;
		if (singleFolder) {
			path += description.getName() + File.separator;
		}
		return path + name;
	}

	/**
	 * Pop up a Party "achievement" message
	 * @param name The player to tell
	 * @param msg The message to send (max 23 chars)
	 */
	public void notify(String name, String msg, Object... args) {
		this.notify(server.getPlayer(name), msg, Material.SIGN, args);
	}

	/**
	 * Pop up a Party "achievement" message
	 * @param name The player to tell
	 * @param msg The message to send (max 23 chars)
	 * @param icon The material to use
	 */
	public void notify(String name, String msg, Material icon, Object... args) {
		this.notify(server.getPlayer(name), msg, icon, args);
	}

	/**
	 * Pop up a Party "achievement" message for multiple players
	 * @param players The player to tell
	 * @param msg The message to send (max 23 chars)
	 */
	public void notify(List<Player> players, String msg, Object... args) {
		for (Player player : players) {
			this.notify(player, msg, Material.SIGN, args);
		}
	}

	/**
	 * Pop up a Party "achievement" message for multiple players
	 * @param players The player to tell
	 * @param msg The message to send (max 23 chars)
	 */
	public void notify(List<Player> players, String msg, Material icon, Object... args) {
		for (Player player : players) {
			this.notify(player, msg, icon, args);
		}
	}

	/**
	 * Pop up a Party "achievement" message
	 * @param player The player to tell
	 * @param msg The message to send (max 23 chars)
	 */
	public void notify(Player player, String msg, Object... args) {
		this.notify(player, msg, Material.SIGN, args);
	}

	/**
	 * Pop up a Party "achievement" message
	 * @param player The player to tell
	 * @param msg The message to send (max 23 chars)
	 * @param icon The material to use
	 */
	public void notify(Player player, String msg, Material icon, Object... args) {
		if (hasSpout && player != null) {
			try {
				SpoutManager.getPlayer(player).sendNotification(title, String.format(msg, args), icon);
			} catch (Exception e) {
				// Bad format->Object type
			}
		}
	}

	/**
	 * Send a message to one person by name.
	 * @param prefix Whether to show the plugin name
	 * @param name The player to message
	 * @param msg The message to send
	 */
	public void sendMessage(String name, String msg, Object... args) {
		sendMessage(true, server.getPlayer(name), msg, args);
	}

	/**
	 * Send a message to multiple people.
	 * @param prefix Whether to show the plugin name
	 * @param players The Players to message
	 * @param msg The message to send
	 */
	public void sendMessage(List<Player> players, String msg, Object... args) {
		for (Player player : players) {
			sendMessage(true, player, msg, args);
		}
	}

	/**
	 * Send a message to one person.
	 * @param prefix Whether to show the plugin name
	 * @param player The Player to message
	 * @param msg The message to send
	 */
	public void sendMessage(CommandSender player, String msg, Object... args) {
		sendMessage(true, player, msg, args);
	}

	/**
	 * Send a message to one person by name.
	 * @param name The player to message
	 * @param msg The message to send
	 */
	public void sendMessage(boolean prefix, String name, String msg, Object... args) {
		sendMessage(prefix, server.getPlayer(name), msg, args);
	}

	/**
	 * Send a message to multiple people.
	 * @param players The Players to message
	 * @param msg The message to send
	 */
	public void sendMessage(boolean prefix, List<CommandSender> players, String msg, Object... args) {
		for (CommandSender player : players) {
			sendMessage(prefix, player, msg, args);
		}
	}

	/**
	 * Send a message to one person.
	 * @param player The Player to message
	 * @param msg The message to send
	 */
	public void sendMessage(boolean prefix, CommandSender player, String msg, Object... args) {
		if (player != null) {
			try {
				for (String line : String.format(msg, args).split("\n")) {
					player.sendMessage((prefix ? this.prefix : "") + line);
				}
			} catch (Exception e) {
				// Bad format->Object type
			}
		}
	}

	/**
	 * Get the container for use by this plugin, anchor and position can be overridden by options.
	 * @return 
	 */
	public Container getContainer(SpoutPlayer player, String anchorName, int offsetX, int offsetY) {
		WidgetAnchor anchor = WidgetAnchor.SCALE;
		if ("TOP_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_LEFT;
		} else if ("TOP_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_CENTER;
			offsetX -= 213;
		} else if ("TOP_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_RIGHT;
			offsetX = -427 - offsetX;
		} else if ("CENTER_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.CENTER_LEFT;
			offsetY -= 120;
		} else if ("CENTER_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.CENTER_CENTER;
			offsetX -= 213;
			offsetY -= 120;
		} else if ("CENTER_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.CENTER_RIGHT;
			offsetX = -427 - offsetX;
			offsetY -= 120;
		} else if ("BOTTOM_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.BOTTOM_LEFT;
			offsetY = -240 - offsetY;
		} else if ("BOTTOM_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.BOTTOM_CENTER;
			offsetX -= 213;
			offsetY = -240 - offsetY;
		} else if ("BOTTOM_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.BOTTOM_RIGHT;
			offsetX = -427 - offsetX;
			offsetY = -240 - offsetY;
		}
		MMOHUDEvent event = new MMOHUDEvent(player, plugin, anchor, offsetX, offsetY);
		pm.callEvent(event);
		Container container = (Container) new GenericContainer().setAlign(event.getAnchor()).setAnchor(event.getAnchor()).setFixed(true).setX(event.getOffsetX()).setY(event.getOffsetY()).setWidth(427).setHeight(240);
		player.getMainScreen().attachWidget(this, container);
		return container;
	}

	/**
	 * Spout-safe version of setGlobalTitle
	 * @param target
	 * @param title 
	 */
	public void setTitle(LivingEntity target, String title) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalTitle(target, title);
		}
	}

	/**
	 * Spout-safe version of setPlayerTitle
	 * @param player 
	 * @param target
	 * @param title 
	 */
	public void setTitle(Player player, LivingEntity target, String title) {
		if (hasSpout && player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerTitle(SpoutManager.getPlayer(player), target, title);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak
	 * @param target
	 * @param url 
	 */
	public void setCloak(HumanEntity target, String url) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalCloak(target, url);
		}
	}

	/**
	 * Spout-safe version of setPlayerCloak
	 * @param player 
	 * @param target
	 * @param url 
	 */
	public void setCloak(Player player, HumanEntity target, String url) {
		if (hasSpout && player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerCloak(SpoutManager.getPlayer(player), target, url);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak
	 * @param target
	 * @param url 
	 */
	public void setSkin(HumanEntity target, String url) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalSkin(target, url);
		}
	}

	/**
	 * Spout-safe version of setPlayerCloak
	 * @param player 
	 * @param target
	 * @param url 
	 */
	public void setSkin(Player player, HumanEntity target, String url) {
		if (hasSpout && player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerSkin(SpoutManager.getPlayer(player), target, url);
		}
	}

	@Override
	public EbeanServer getDatabase() {
		if (database == null) {
			database = new MyDatabase(this) {

				@Override
				protected List<Class<?>> getDatabaseClasses() {
					return plugin.getDatabaseClasses();
				}
			};
			database.initializeDatabase(
					  MMOCore.config_database_driver,
					  MMOCore.config_database_url,
					  MMOCore.config_database_username,
					  MMOCore.config_database_password,
					  MMOCore.config_database_isolation,
					  MMOCore.config_database_logging,
					  MMOCore.config_database_rebuild);
		}
		return database.getDatabase();
	}

	protected void beforeDropDatabase() {
	}

	protected void afterCreateDatabase() {
	}
}
