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

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.Core.CoreAPI.MMOHUDEvent;
import mmo.Core.SQLibrary.*;
import mmo.Core.util.EnumBitSet;
import mmo.Core.util.HashMapString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.plugin.SpoutPlugin;

public abstract class MMOPlugin extends SpoutPlugin {

	public enum Support {

		/**
		 * Use onPlayerJoin, onPlayerQuit, onSpoutCraftPlayer and onNormalPlayer plugin methods.
		 */
		MMO_PLAYER,
		/**
		 * No config file used for this plugin.
		 */
		MMO_NO_CONFIG,
		/**
		 * Auto-extract supported files inside the plugin.jar file.
		 */
		MMO_AUTO_EXTRACT,
		/**
		 * Uses a custom table in the database, include "table.sql" files in the jar with all create and default values.
		 */
		MMO_DATABASE,
		/**
		 * Attach a i18n lookup table to the plugin.
		 */
		MMO_I18N,
		/**
		 * Don't cache shared database access (this allows external changes to be noticed).
		 * NOTE: This only affects shared access (the setXyz() and getXyz() methods), direct access has no cache!
		 */
		MMO_NO_SHARED_CACHE
	}
	/**
	 * There is a plugins/mmoMinecraft folder that we need to store all files in
	 */
	static public boolean singleFolder = false;
	/**
	 * Private and protected variables
	 */
	static protected DatabaseHandler database = null;
	static protected MMOPlugin mmoCore;
	static protected final Logger logger = Logger.getLogger("Minecraft");
	protected PluginDescriptionFile description;
	protected Configuration cfg;
	protected PluginManager pm;
	protected Server server;
	protected String title;
	protected String prefix;
	protected MMOPlugin plugin;
	protected MMOi18n i18n;
	/**
	 * Map of player+key entries for quick db cache access
	 */
	protected Map<Player, Map<String, String>> dbCache = null;
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
			mmoCore = this;
		}
		plugin = this;
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
				super.getDataFolder().delete();
			} catch (Exception e) {
			}
		}

		EnumBitSet support = mmoSupport(new EnumBitSet());

		// Don't try to load and save the config if the plugin doesn't use it
		if (!support.get(Support.MMO_NO_CONFIG)) {
			cfg = new Configuration(new File("plugins/mmoMinecraft", description.getName() + ".yml"));
			cfg.load();
			loadConfiguration(cfg);
			if (!cfg.getKeys().isEmpty()) {
				cfg.setHeader("#" + title + " Configuration");
				cfg.save();
			}
		}
		if (!support.get(Support.MMO_NO_SHARED_CACHE)) {
			dbCache = new HashMap<Player, Map<String, String>>();
		}
		// Do we want a custom database table
		if (support.get(Support.MMO_DATABASE)) {
			// Must be after CONFIG as mmoCore sets up the database there
			// Need to extract sql files from the jar, and then auto-apply them if needed
			// Maybe go with table.sql as the filename...
			try {
				JarFile jar = new JarFile(getFile());
				for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
					JarEntry entry = (JarEntry) entries.nextElement();
					String name = entry.getName();
					if (name.matches(".+\\.sql$")) {
						String table = name.substring(0, name.length() - 4);
						if (!database.checkTable(table)) {
							try {
								StringBuilder sql = new StringBuilder();
								InputStream is = jar.getInputStream(entry);
								Scanner scanner = new Scanner(is);
								while (scanner.hasNextLine()) {
									String line = scanner.nextLine().trim();
									if (!line.isEmpty() && !line.startsWith("--")) {
										sql.append(line).append(" ");
									}
								}
								scanner.close();
								is.close();
								database.query(sql.toString());
								if (database.checkTable(table)) {
									log("Created table: %s", table);
								} else {
									log("ERROR: Unable to create table '%s'", table);
								}
							} catch (IOException e) {
								log("ERROR: Unable to create table '%s': %s", table, e.getMessage());
							}
						}
					}
				}
				jar.close();
			} catch (IOException e) {
			}
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
			extractFile(".*\\.(png|jpg|ogg|midi|wav|zip)$", true);
		}
		// Create i18n Table if needed
		if (support.get(Support.MMO_I18N)) {
			try {
				i18n = new MMOi18n();

				// Extract any built-in translations
				extractFile("^(i18n|lang-[a-z]{2}(-[A-Z]{2})?).yml$");

				// i18n: Extract main configuration
				File i18nMain = new File(this.getDataFolder(), "i18n.yml");
				if (i18nMain.exists()) {
					Configuration i18nCfg = new Configuration(i18nMain);

					//Add load from web code here
					//Add check for old version here
					//Add more stuff
					/* The keys used to translate (and hence show in the config files) are the real
					 * strings with "[. ]" replaced with "_" (for use as YAML nodes). */

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
			JarFile jar = new JarFile(getFile());
			for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
				JarEntry entry = (JarEntry) entries.nextElement();
				String name = entry.getName();
				if (name.matches(regex)) {
					if (!getDataFolder().exists()) {
						getDataFolder().mkdir();
					}
					try {
						File file;
						if (singleFolder && name.equals("config.yml")) {
							name = description.getName() + ".yml";
							file = new File("plugins/mmoMinecraft", name);
						} else {
							file = new File(getDataFolder(), name);
						}
						if (!file.exists()) {
							InputStream is = jar.getInputStream(entry);
							FileOutputStream fos = new FileOutputStream(file);
							while (is.available() > 0) {
								fos.write(is.read());
							}
							fos.close();
							is.close();
							found = true;
						}
						if (cache && name.matches(".*\\.(txt|yml|xml|png|jpg|ogg|midi|wav|zip)$")) {
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
			return new File("plugins/mmoMinecraft", description.getName());
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
		logger.log(level, String.format("[" + description.getName() + "] " + text, args));
	}

	/**
	 * Return the fill pathname for an auto-extracted file.
	 * @param name the filename
	 * @return the pathname
	 */
	public String getPath(String name) {
		String path = getDataFolder() + File.separator;
		if (singleFolder) {
			path += description.getName() + File.separator;
		}
		return path + name;
	}

	/**
	 * Pop up an "achievement" message.
	 * @param player the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param args any args for the format string
	 */
	public <T> void notify(T player, String msg, Object... args) {
		this.notify(player, msg, Material.SIGN, args);
	}

	/**
	 * Pop up an "achievement" message.
	 * @param player the Player, player name, or List of Players or player names to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param icon the material to use
	 * @param args any args for the format string
	 */
	public <T> void notify(T player, String msg, Material icon, Object... args) {
		if (player != null) {
			if (player instanceof List) {
				for (Object entry : (List) player) {
					this.notify(entry, msg, icon, args);
				}
			} else {
				try {
					SpoutManager.getPlayer(MMO.playerFromName(player)).sendNotification(title, String.format(msg, args), icon);
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Send a message to one person by name.
	 * @param player the Player, player name, or List of Players or player names to tell
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public <T> void sendMessage(T player, String msg, Object... args) {
		sendMessage(true, player, msg, args);
	}

	/**
	 * Send a message to one person.
	 * @param prefix whether to show the plugin name
	 * @param player the Player, player name, or List of Players or player names to tell
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public <T> void sendMessage(boolean prefix, T player, String msg, Object... args) {
		if (player != null) {
			if (player instanceof List) {
				for (Object entry : (List) player) {
					sendMessage(prefix, entry, msg, args);
				}
			} else {
				try {
					for (String line : String.format(msg, args).split("\n")) {
						MMO.senderFromName(player).sendMessage((prefix ? this.prefix : "") + line);
					}
				} catch (Exception e) {
					// Bad format->Object type
				}
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
		if (target != null) {
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
		if (player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerTitle(SpoutManager.getPlayer(player), target, title);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak.
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setCloak(HumanEntity target, String url) {
		if (target != null) {
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
		if (player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerCloak(SpoutManager.getPlayer(player), target, url);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak.
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setSkin(HumanEntity target, String url) {
		if (target != null) {
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
		if (player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerSkin(SpoutManager.getPlayer(player), target, url);
		}
	}

	/**
	 * Sends a query to the SQL database.
	 * This uses a prepared statement to ensure the safety of the arguments.
	 * @param query the SQL query to send to the database
	 * @param vars a list of variables that replace "?" in the query in a secure manner
	 * @return the table of results from the query
	 * @throws NullPointerException if database is uninitialised
	 * @see DatabaseHandler
	 */
	public Map<String, Object> query(String query, Object... vars) {
		return database.query(query, vars);
	}

	/**
	 * Creates a prepared query for the database.
	 * @param query the SQL query to prepare to send to the database
	 * @param vars a list of variables that replace "?" in the query in a secure manner
	 * @return the prepared statement
	 * @throws NullPointerException if database is uninitialised
	 * @see DatabaseHandler
	 */
	public PreparedStatement prepare(String query, Object... vars) {
		return database.prepare(query, vars);
	}

	/**
	 * Get a value for a specific key.
	 * @param player the Player (or player name) this data relates to
	 * @param key a unique id (per plugin per player)
	 * @return the value stored
	 */
	public <T> String getData(T player, String key) {
		Player p = MMO.playerFromName(player);
		Map<String, String> cache = null;
		if (dbCache != null) {
			if (dbCache.containsKey(p)) {
				cache = dbCache.get(p);
			} else {
				dbCache.put(p, cache = new HashMapString<String>());
			}
			if (cache.containsKey(key)) {
				return cache.get(key);
			}
		}
		Map<String, Object> result = query("SELECT value FROM mmoMinecraft WHERE 'plugin' = ? AND 'player' = ? AND 'key' = ?", description.getName(), p.getName(), key);
		String value = (String) result.get("value");
		if (dbCache != null) {
			cache.put(key, value);
		}
		return value;
	}

	/**
	 * Set the value for a specific key.
	 * If caching is turned on and there is no change then this won't try to write to the database.
	 * @param player the Player (or player name) this data relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set it to
	 */
	public <T> void setData(T player, String key, String value) {
		Player p = MMO.playerFromName(player);
		if (dbCache != null) {
			Map<String, String> cache;
			if (dbCache.containsKey(p)) {
				cache = dbCache.get(p);
			} else {
				dbCache.put(p, cache = new HashMapString<String>());
			}
			if (value instanceof String && cache.containsKey(key) && value.equals(cache.get(key))) {
				return; // No need to do anything else if it's not changing
			}
			cache.put(key, value);
		}
		query("REPLACE INTO mmoMinecraft ('plugin', 'player', 'key', 'value') VALUES (?, ?, ?, ?)", description.getName(), p.getName(), key, value);
	}

	/**
	 * Delete the value for a specific key.
	 * @param player the Player (or player name) this data relates to
	 * @param key a unique id (per plugin per player)
	 */
	public <T> void deleteData(T player, String key) {
		Player p = MMO.playerFromName(player);
		if (dbCache != null) {
			Map<String, String> cache;
			if (dbCache.containsKey(p)) {
				cache = dbCache.get(p);
			} else {
				dbCache.put(p, cache = new HashMapString<String>());
			}
			cache.remove(key);
		}
		database.query("DELETE FROM mmoMinecraft WHERE 'plugin' = ? AND 'player' = ? AND 'key' = ?", description.getName(), p.getName(), key);
	}

	/**
	 * Clear the shared cache for a single player.
	 * This is automatically called after players disconnect.
	 * @param player the Player (or player name) to clear
	 */
	public <T> void clearCache(T player) {
		if (dbCache != null) {
			Player p = MMO.playerFromName(player);
			dbCache.remove(p);
		}
	}

	/**
	 * Clear the shared cache for all players.
	 */
	public void clearCache() {
		if (dbCache != null) {
			dbCache.clear();
		}
	}

	/**
	 * Set a string in the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setString(T player, String key, String value) {
		setData(player, key, value);
	}

	/**
	 * Get a string from the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> String getString(T player, String key, String def) {
		String result = getData(player, key);
		if (result != null) {
			return result;
		}
		return def;
	}

	/**
	 * Set a list of strings in the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setStringList(T player, String key, List<String> value) {
		setData(player, key, MMO.join(value, ","));
	}

	/**
	 * Get a list of strings from the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> List<String> getStringList(T player, String key, List<String> def) {
		String result = getData(player, key);
		if (result != null) {
			return Arrays.asList(result.split(","));
		}
		return def;
	}

	/**
	 * Set an integer in the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setInt(T player, String key, int value) {
		setData(player, key, "" + value);
	}

	/**
	 * Get an integer from the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> int getInt(T player, String key, int def) {
		String result = getData(player, key);
		if (result != null) {
			return Integer.parseInt(result);
		}
		return def;
	}

	/**
	 * Set a double in the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setDouble(T player, String key, double value) {
		setData(player, key, "" + value);
	}

	/**
	 * Get a double from the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> double getDouble(T player, String key, double def) {
		String result = getData(player, key);
		if (result != null) {
			return Double.parseDouble(result);
		}
		return def;
	}

	/**
	 * Set a boolean in the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setBoolean(T player, String key, boolean value) {
		setData(player, key, value ? "true" : "false");
	}

	/**
	 * Get a boolean from the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> boolean getBoolean(T player, String key, boolean def) {
		String result = getData(player, key);
		if (result != null) {
			return result.equals("true") ? true : false;
		}
		return def;
	}

	/**
	 * Set a Location in the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setLocation(T player, String key, Location value) {
		setData(player, key, value.getWorld().getName() + "," + value.getX() + "," + value.getY() + "," + value.getZ() + "," + value.getPitch() + "," + value.getYaw());
	}

	/**
	 * Get a Location from the shared database.
	 * @param player the Player (or player name) this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> Location getLocation(T player, String key, Location def) {
		String result = getData(player, key);
		if (result != null) {
			List<String> values = Arrays.asList(result.split(","));
			if (values.size() == 6) {
				try {
					return new Location(
							Bukkit.getWorld(values.get(0)),
							Double.parseDouble(values.get(1)),
							Double.parseDouble(values.get(2)),
							Double.parseDouble(values.get(3)),
							Float.parseFloat(values.get(4)),
							Float.parseFloat(values.get(5)));
				} catch (Exception e) {
				}
			}
		}
		return def;
	}

	/**
	 * Pop up a requester for the player.
	 * @param player the Player to ask
	 * @param description the question to ask them
	 * @param buttons a list of buttons to display
	 * @return if the request could be shown
	 */
	public boolean request(Player player, String id, String description, String... buttons) {
		if (false) {
			return true;
		}
		return false;
	}
	/**
	 * Will add custom commands to plugins without needing the plugin.yml entry
	 * 
	@Deprecated
	private void addCommand(String command) {
	Class<? extends Server> c = this.getServer().getClass();
	try {
	Field f = c.getDeclaredField("commandMap");
	f.setAccessible(true);
	SimpleCommandMap scm = (SimpleCommandMap) f.get(c);
	} catch (SecurityException e) {
	e.printStackTrace();
	} catch (NoSuchFieldException e) {
	e.printStackTrace();
	} catch (IllegalArgumentException e) {
	e.printStackTrace();
	} catch (IllegalAccessException e) {
	e.printStackTrace();
	}
	}
	 */
}
