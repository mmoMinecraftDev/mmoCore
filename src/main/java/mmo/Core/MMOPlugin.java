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

import mmo.Core.util.EnumBitSet;
import com.avaje.ebean.EbeanServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import mmo.Core.CoreAPI.MMOHUDEvent;
import mmo.Core.util.MyDatabase;
import org.bukkit.Bukkit;
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

	public enum Support {

		/**
		 * Use onPlayerJoin, onPlayerQuit, onSpoutCraftPlayer and onNormalPlayer plugin methods.
		 */
		MMO_PLAYER,
		/**
		 * No config file used for this plugin
		 */
		MMO_NO_CONFIG,
		/**
		 * Auto-extract *.png files inside the plugin.jar
		 */
		MMO_AUTO_EXTRACT,
		/**
		 * Use a database (also needs getDatabaseClasses() to return a list of classes)
		 */
		MMO_DATABASE,
		/**
		 * Attach a i18n lookup table to the plugin
		 */
		MMO_I18N
	}
	/**
	 * Spout is loaded and enabled
	 */
	static public boolean hasSpout = false;
	/**
	 * There is a plugins/mmoMinecraft folder that we need to store all files in
	 */
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
	protected MMOi18n i18n;
	/**
	 * Version of this plugin
	 */
	public int version = 0;
	/**
	 * Revision of this plugin
	 */
	public int revision = 0;
	/**
	 * If there's an update available
	 */
	public boolean update = false;

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
		title = description.getName().replaceAll("^mmo", "");
		prefix = ChatColor.GREEN + "[" + ChatColor.AQUA + title + ChatColor.GREEN + "] " + ChatColor.WHITE;
		String oldVersion[] = description.getVersion().split("\\.");
		if (oldVersion.length == 2) {
			version = Integer.parseInt(oldVersion[0]);
			revision = Integer.parseInt(oldVersion[1]);
		} else {
			log("Unable to determine version!");
		}

		// Cache that we exist if we provide an event
		MMOMinecraft.enablePlugin(this);

		// Shortcut booleans to make life easier
		if (!hasSpout) {
			hasSpout = server.getPluginManager().isPluginEnabled("Spout");
		}
		if (!singleFolder && new File("plugins/mmoMinecraft").exists()) {
			singleFolder = true;
		}

		// Move plugins/mmoPlugin/* to plugins/mmoMinecraft/*
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

		EnumBitSet support = mmoSupport(new EnumBitSet());

		// Don't try to load and save the config if the plugin doesn't use it
		if (!support.get(Support.MMO_NO_CONFIG)) {
			cfg = new Configuration(new File(getDataFolder(), description.getName() + ".yml"));
			cfg.load();
			loadConfiguration(cfg);
			if (!cfg.getKeys().isEmpty()) {
				cfg.setHeader("#" + title + " Configuration");
				cfg.save();
			}
		}
		// Load the database handler if needed
		if (support.get(Support.MMO_DATABASE) && !getDatabaseClasses().isEmpty()) {
			getDatabase();
		}
		// Use our own global onPlayerXYZ plugin methods
		if (support.get(Support.MMO_PLAYER)) {
			MMOCore.support_mmo_player.add(this);
			server.getScheduler().scheduleSyncDelayedTask(plugin,
					new Runnable() {

						@Override
						public void run() {
							for (Player player : Bukkit.getOnlinePlayers()) {
								onPlayerJoin(player);
							}
						}
					});
		}
		// Auto-extract resource files from within our plugin.jar
		if (support.get(Support.MMO_AUTO_EXTRACT)) {
			extractFile("^config.yml$");
			extractFile("\\.(png|jpg|ogg|midi|wav|zip)$", true);
		}
		// Create i18n Table if needed
		if (support.get(Support.MMO_I18N)) {
			try {
				i18n = new MMOi18n();

				// Extract any built-in translations
				extractFile("^(i18n|lang-[a-z]{2}(-[A-Z]{2})?).yml$");
				
				// i18n: Extract main configuration
				File i18nMain = new File(this.getDataFolder() + "/i18n.yml");
				if (i18nMain.exists()) {
					Configuration i18nCfg = new Configuration(i18nMain);

					//Add load from web code here
					//Add check for old version here
					//Add more stuff

					i18nCfg = null;
				} else {
					log(Level.WARNING, "Warning: No i18n.yml");
				}

				i18nMain = null;
			} catch (Exception e) {
			}
		}

		// Done everything important, up to the individual plugin now...
		log("Enabled " + description.getFullName());
	}

	/**
	 * Extract files from the plugin jar.
	 * @param regex a pattern of files to extract
	 * @return if any files were extracted
	 */
	public boolean extractFile(String regex) {
		return extractFile(regex, false);
	}

	/**
	 * Extract files from the plugin jar and optionally cache them on the client.
	 * @param regex a pattern of files to extract
	 * @param cache if any files found should be added to the Spout cache
	 * @return if any files were extracted
	 */
	public boolean extractFile(String regex, boolean cache) {
		boolean found = false;
		try {
			boolean folder = false;
			JarFile jar = new JarFile(getFile());
			Enumeration entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarentry = (JarEntry) entries.nextElement();
				String name = jarentry.getName();
				if (name.matches(regex)) {
					if (!folder) {
						new File(getDataFolder(), description.getName()).mkdir();
						folder = true;
					}
					if (singleFolder && name.equals("config.yml")) {
						name = description.getName() + ".yml";
					}
					try {
						File file = new File(getDataFolder(), description.getName() + File.separator + name);
						if (!file.exists()) {
							InputStream is = jar.getInputStream(jarentry);
							FileOutputStream fos = new FileOutputStream(file);
							while (is.available() > 0) {
								fos.write(is.read());
							}
							fos.close();
							is.close();
							found = true;
						}
						if (hasSpout && cache && name.matches("\\.(txt|yml|xml|png|jpg|ogg|midi|wav|zip)$")) {
							SpoutManager.getFileManager().addToCache(plugin, file);
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		}
		return found;
	}

	@Override
	public void onDisable() {
		// Cache the various event APIs
		MMOMinecraft.disablePlugin(this);

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
	 * @param cfg load from here only
	 */
	public void loadConfiguration(Configuration cfg) {
	}

	/**
	 * Supply a bitset of shortcuts for MMOPlugin to handle
	 * @return the bitset provided with bits set
	 */
	public EnumBitSet mmoSupport(EnumBitSet support) {
		return support;
	}

	/**
	 * Called when any player joins - need to return MMO_PLAYER from mmoSupport.
	 * @param player 
	 */
	public void onPlayerJoin(Player player) {
	}

	/**
	 * Called for every Spoutcraft player on /reload and PlayerJoin - need to return MMO_PLAYER from mmoSupport.
	 * @param player
	 */
	public void onSpoutCraftPlayer(SpoutPlayer player) {
	}

	/**
	 * Called for every *NON* Spoutcraft player on /reload and PlayerJoin - need to return MMO_PLAYER from mmoSupport.
	 * @param player
	 */
	public void onNormalPlayer(Player player) {
	}

	/**
	 * Called when any player quits or is kicked - need to return MMO_PLAYER from mmoSupport.
	 * @param player 
	 */
	public void onPlayerQuit(Player player) {
	}

	/**
	 * Send a message to the log.
	 * @param text a format style string
	 * @param args any args for the format
	 */
	public void log(String text, Object... args) {
		log(Level.INFO, text, args);
	}

	/**
	 * Send a message to the log.
	 * @param text a format style string
	 * @param args any args for the format
	 */
	public void log(Level level, String text, Object... args) {
		logger.log(level, "[" + description.getName() + "] " + String.format(text, args));
	}

	/**
	 * Return the fill pathname for an auto-extracted file.
	 * @param name the filename
	 * @return the pathname
	 */
	public String getResource(String name) {
		String path = getDataFolder() + File.separator;
		if (singleFolder) {
			path += description.getName() + File.separator;
		}
		return path + name;
	}

	/**
	 * Pop up an "achievement" message.
	 * @param name the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param args any args for the format string
	 */
	public void notify(String name, String msg, Object... args) {
		this.notify(server.getPlayer(name), msg, Material.SIGN, args);
	}

	/**
	 * Pop up an "achievement" message.
	 * @param name the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param icon the material to use
	 * @param args any args for the format string
	 */
	public void notify(String name, String msg, Material icon, Object... args) {
		this.notify(server.getPlayer(name), msg, icon, args);
	}

	/**
	 * Pop up an "achievement" message for multiple players.
	 * @param players the players to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param args any args for the format string
	 */
	public void notify(List<Player> players, String msg, Object... args) {
		for (Player player : players) {
			this.notify(player, msg, Material.SIGN, args);
		}
	}

	/**
	 * Pop up an "achievement" message for multiple players.
	 * @param players the players to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param icon the material to use
	 * @param args any args for the format string
	 */
	public void notify(List<Player> players, String msg, Material icon, Object... args) {
		for (Player player : players) {
			this.notify(player, msg, icon, args);
		}
	}

	/**
	 * Pop up an "achievement" message.
	 * @param player the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param args any args for the format string
	 */
	public void notify(Player player, String msg, Object... args) {
		this.notify(player, msg, Material.SIGN, args);
	}

	/**
	 * Pop up an "achievement" message.
	 * @param player the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param icon the material to use
	 * @param args any args for the format string
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
	 * @param name the player to message
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public void sendMessage(String name, String msg, Object... args) {
		sendMessage(true, server.getPlayer(name), msg, args);
	}

	/**
	 * Send a message to multiple people.
	 * @param players the Players to message
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public void sendMessage(List<Player> players, String msg, Object... args) {
		for (Player player : players) {
			sendMessage(true, player, msg, args);
		}
	}

	/**
	 * Send a message to one person.
	 * @param player the Player to message
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public void sendMessage(CommandSender player, String msg, Object... args) {
		sendMessage(true, player, msg, args);
	}

	/**
	 * Send a message to one person by name.
	 * @param prefix whether to show the plugin name
	 * @param name the player to message
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public void sendMessage(boolean prefix, String name, String msg, Object... args) {
		sendMessage(prefix, server.getPlayer(name), msg, args);
	}

	/**
	 * Send a message to multiple people.
	 * @param prefix whether to show the plugin name
	 * @param players the Players to message
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public void sendMessage(boolean prefix, List<CommandSender> players, String msg, Object... args) {
		for (CommandSender player : players) {
			sendMessage(prefix, player, msg, args);
		}
	}

	/**
	 * Send a message to one person.
	 * @param prefix whether to show the plugin name
	 * @param player the Player to message
	 * @param msg the formatted message to send
	 * @param args any args for the format string
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
	 * Get the container for use by this plugin, anchor and position can be overridden by MMOHUDEvent.
	 * @param player the player this is for
	 * @param anchorName the name of the WidgetAnchor
	 * @param offsetX the horizontal offset to use
	 * @param offsetY the vertical offset to use
	 * @return the Container
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
	 * Spout-safe version of setGlobalTitle.
	 * @param target the entity to target
	 * @param title the title to show
	 */
	public void setTitle(LivingEntity target, String title) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalTitle(target, title);
		}
	}

	/**
	 * Spout-safe version of setPlayerTitle.
	 * @param player the player seeing the target
	 * @param target the entity to target
	 * @param title the title to show
	 */
	public void setTitle(Player player, LivingEntity target, String title) {
		if (hasSpout && player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerTitle(SpoutManager.getPlayer(player), target, title);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak.
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setCloak(HumanEntity target, String url) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalCloak(target, url);
		}
	}

	/**
	 * Spout-safe version of setPlayerCloak.
	 * @param player the player seeing the target
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setCloak(Player player, HumanEntity target, String url) {
		if (hasSpout && player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerCloak(SpoutManager.getPlayer(player), target, url);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak.
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setSkin(HumanEntity target, String url) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalSkin(target, url);
		}
	}

	/**
	 * Spout-safe version of setPlayerCloak.
	 * @param player the player seeing the target
	 * @param target the entity to target
	 * @param url the cloak to show
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
