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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.Core.CoreAPI.MMOHUDEvent;
import mmo.Core.SQLibrary.DatabaseHandler;
import mmo.Core.util.EnumBitSet;
import mmo.Core.util.HashMapString;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.getspout.spoutapi.plugin.SpoutPlugin;

/**
 * All mmoMinecraft plugins should extend this instead of SpoutPlugin to provide
 * a consistent interface and access to all abilities within the mmoMinecraft
 * suite.
 */
public abstract class MMOPlugin extends SpoutPlugin {

	/**
	 * Abilities that this plugin requires.
	 */
	public enum Support {

		/**
		 * No config file used for this plugin.
		 */
		MMO_NO_CONFIG,
		/**
		 * Auto-extract supported files inside the plugin.jar file.
		 */
		MMO_AUTO_EXTRACT,
		/**
		 * Uses a custom table in the database, include "table.sql" files in the
		 * jar with all create and default values.
		 */
		MMO_DATABASE,
		/**
		 * Attach a i18n lookup table to the plugin.
		 */
		MMO_I18N,
		/**
		 * Don't cache shared database access (this allows external changes to
		 * be noticed). NOTE: This only affects shared access (the setXyz() and
		 * getXyz() methods), direct access has no cache!
		 */
		MMO_NO_SHARED_CACHE
	}
	/**
	 * There is a plugins/mmoMinecraft folder that we need to store all files
	 * in.
	 */
	public static boolean singleFolder = false;
	/**
	 * Private and protected variables.
	 */
	protected static DatabaseHandler database = null;
	protected static MMOPlugin mmoCore;
	protected static final Logger logger = Logger.getLogger("Minecraft");
	protected PluginDescriptionFile description;
	protected FileConfiguration cfg;
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
		final String[] oldVersion = description.getVersion().split("\\.");
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

		final EnumBitSet support = mmoSupport(new EnumBitSet());

		// Don't try to load and save the config if the plugin doesn't use it
		if (!support.get(Support.MMO_NO_CONFIG)) {
			final File cfgFile = new File(singleFolder ? "plugins/mmoMinecraft" : getDataFolder().getPath(), description.getName() + ".yml");
			cfg = YamlConfiguration.loadConfiguration(cfgFile);
			if (!cfg.getKeys(false).isEmpty()) {
				cfg.options().header("#" + title + " Configuration");
				try {
					cfg.save(cfgFile);
				} catch (IOException e) {
					log("Could not save to file " + cfgFile);
				}
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
				final JarFile jar = new JarFile(getFile());
				for (final Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
					final JarEntry entry = entries.nextElement();
					final String name = entry.getName();
					if (name.matches(".+\\.sql$")) {
						final String table = name.substring(0, name.length() - 4);
						if (!database.checkTable(table)) {
							try {
								final StringBuilder sql = new StringBuilder();
								final InputStream is = jar.getInputStream(entry);
								final Scanner scanner = new Scanner(is);
								while (scanner.hasNextLine()) {
									final String line = scanner.nextLine().trim();
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
					FileConfiguration i18nCfg = YamlConfiguration.loadConfiguration(i18nMain);

					// TODO: Add load from web code here
					// TODO: Add check for old version here
					// TODO: Add more stuff
					/*
					 * The keys used to translate (and hence show in the config
					 * files) are the real strings with "[. ]" replaced with "_"
					 * (for use as YAML nodes).
					 */

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
	 *
	 * @param regex a pattern of files to extract
	 * @return if any files were extracted
	 */
	public boolean extractFile(final String regex) {
		return extractFile(regex, false);
	}

	/**
	 * Extract files from the plugin jar and optionally cache them on the
	 * client.
	 *
	 * @param regex a pattern of files to extract
	 * @param cache if any files found should be added to the Spout cache
	 * @return if any files were extracted
	 */
	public boolean extractFile(final String regex, final boolean cache) {
		boolean found = false;
		try {
			final JarFile jar = new JarFile(getFile());
			for (final Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
				final JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.matches(regex)) {
					if (!getDataFolder().exists()) {
						getDataFolder().mkdir();
					}
					try {
						File file;
						if (singleFolder && "config.yml".equals(name)) {
							name = description.getName() + ".yml";
							file = new File("plugins/mmoMinecraft", name);
						} else {
							file = new File(getDataFolder(), name);
						}
						if (!file.exists()) {
							final InputStream is = jar.getInputStream(entry);
							final FileOutputStream fos = new FileOutputStream(file);
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
		File result;
		if (singleFolder) {
			result = new File("plugins/mmoMinecraft", description.getName());
		} else {
			result = super.getDataFolder();
		}
		return result;
	}

	/**
	 * Load the configuration - don't save or anything...
	 *
	 * @param cfg load from here only
	 */
	public void loadConfiguration(final FileConfiguration cfg) {
	}

	/**
	 * Supply a bitset of shortcuts for MMOPlugin to handle.
	 *
	 * @return the bitset provided with bits set
	 */
	public EnumBitSet mmoSupport(final EnumBitSet support) {
		return support;
	}

	/**
	 * Send a message to the log.
	 *
	 * @param text a format style string
	 * @param args any args for the format
	 */
	public void log(final String text, final Object... args) {
		log(Level.INFO, text, args);
	}

	/**
	 * Send a message to the log.
	 *
	 * @param text a format style string
	 * @param args any args for the format
	 */
	public void log(final Level level, final String text, final Object... args) {
		logger.log(level, String.format("[" + description.getName() + "] " + text, args));
	}

	/**
	 * Return the fill pathname for an auto-extracted file.
	 *
	 * @param name the filename
	 * @return the pathname
	 */
	public String getPath(final String name) {
		String path = getDataFolder() + File.separator;
		if (singleFolder) {
			path += description.getName() + File.separator;
		}
		return path + name;
	}

	/**
	 * Pop up an "achievement" message.
	 *
	 * @param <T> A Player, String or List<Player or String>
	 * @param player the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param args any args for the format string
	 */
	public <T> void notify(final T player, final String msg, final Object... args) {
		this.notify(player, msg, Material.SIGN, args);
	}

	/**
	 * Pop up an "achievement" message.
	 *
	 * @param <T> A Player, String or List<Player or String>
	 * @param player the player to tell
	 * @param msg a formatted message to send (max 23 chars)
	 * @param icon the material to use
	 * @param args any args for the format string
	 */
	public <T> void notify(final T player, final String msg, final Material icon, final Object... args) {
		if (player != null) {
			if (player instanceof List) {
				for (Object entry : (List<?>) player) {
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
	 * Send a message to one or more players.
	 *
	 * @param <T> A Player, String or List<Player or String>
	 * @param player the player to tell
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public <T> void sendMessage(final T player, final String msg, final Object... args) {
		sendMessage(true, player, msg, args);
	}

	/**
	 * Send a message to one or more players.
	 *
	 * @param <T> A Player, String or List<Player or String>
	 * @param prefix whether to show the plugin name
	 * @param player the player to tell
	 * @param msg the formatted message to send
	 * @param args any args for the format string
	 */
	public <T> void sendMessage(final boolean prefix, final T player, final String msg, final Object... args) {
		if (player != null) {
			if (player instanceof List) {
				for (Object entry : (List<?>) player) {
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
	 * Get the container for use by this plugin, anchor and position can be
	 * overridden by MMOHUDEvent.
	 *
	 * @param player the player this is for
	 * @param anchorName the name of the WidgetAnchor
	 * @param offsetX the horizontal offset to use
	 * @param offsetY the vertical offset to use
	 * @return the Container
	 */
	public Container getContainer(final SpoutPlayer player, final String anchorName, final int offsetX, final int offsetY) {
		int X = offsetX, Y = offsetY;
		WidgetAnchor anchor = WidgetAnchor.SCALE;
		if ("TOP_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_LEFT;
		} else if ("TOP_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_CENTER;
			X -= 213;
		} else if ("TOP_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_RIGHT;
			X = -427 - X;
		} else if ("CENTER_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.CENTER_LEFT;
			Y -= 120;
		} else if ("CENTER_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.CENTER_CENTER;
			X -= 213;
			Y -= 120;
		} else if ("CENTER_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.CENTER_RIGHT;
			X = -427 - X;
			Y -= 120;
		} else if ("BOTTOM_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.BOTTOM_LEFT;
			Y = -240 - Y;
		} else if ("BOTTOM_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.BOTTOM_CENTER;
			X -= 213;
			Y = -240 - Y;
		} else if ("BOTTOM_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.BOTTOM_RIGHT;
			X = -427 - X;
			Y = -240 - Y;
		}
		MMOHUDEvent event = new MMOHUDEvent(player, plugin, anchor, X, Y);
		pm.callEvent(event);
		Container container = (Container) new GenericContainer().setAlign(event.getAnchor()).setAnchor(event.getAnchor()).setFixed(true).setX(event.getOffsetX()).setY(event.getOffsetY()).setWidth(427).setHeight(240);
		player.getMainScreen().attachWidget(this, container);
		return container;
	}

	/**
	 * Spout-safe version of setGlobalTitle.
	 *
	 * @param target the entity to target
	 * @param title the title to show
	 */
	public void setTitle(final LivingEntity target, final String title) {
		if (target != null) {
			SpoutManager.getAppearanceManager().setGlobalTitle(target, title);
		}
	}

	/**
	 * Spout-safe version of setPlayerTitle.
	 *
	 * @param player the player seeing the target
	 * @param target the entity to target
	 * @param title the title to show
	 */
	public void setTitle(final Player player, final LivingEntity target, final String title) {
		if (player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerTitle(SpoutManager.getPlayer(player), target, title);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak.
	 *
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setCloak(final HumanEntity target, final String url) {
		if (target != null) {
			SpoutManager.getAppearanceManager().setGlobalCloak(target, url);
		}
	}

	/**
	 * Spout-safe version of setPlayerCloak.
	 *
	 * @param player the player seeing the target
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setCloak(final Player player, final HumanEntity target, final String url) {
		if (player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerCloak(SpoutManager.getPlayer(player), target, url);
		}
	}

	/**
	 * Spout-safe version of setGlobalCloak.
	 *
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setSkin(final HumanEntity target, final String url) {
		if (target != null) {
			SpoutManager.getAppearanceManager().setGlobalSkin(target, url);
		}
	}

	/**
	 * Spout-safe version of setPlayerCloak.
	 *
	 * @param player the player seeing the target
	 * @param target the entity to target
	 * @param url the cloak to show
	 */
	public void setSkin(final Player player, final HumanEntity target, final String url) {
		if (player != null && target != null) {
			SpoutManager.getAppearanceManager().setPlayerSkin(SpoutManager.getPlayer(player), target, url);
		}
	}

	/**
	 * Sends a query to the SQL database. This uses a prepared statement to
	 * ensure the safety of the arguments.
	 *
	 * @param query the SQL query to send to the database
	 * @param vars a list of variables that replace "?" in the query in a secure
	 * manner
	 * @return the table of results from the query
	 * @see DatabaseHandler
	 */
	public Map<String, Object> query(final String query, final Object... vars) {
		return database.query(query, vars);
	}

	/**
	 * Creates a prepared query for the database.
	 *
	 * @param query the SQL query to prepare to send to the database
	 * @param vars a list of variables that replace "?" in the query in a secure
	 * manner
	 * @return the prepared statement
	 * @see DatabaseHandler
	 */
	public PreparedStatement prepare(final String query, final Object... vars) {
		return database.prepare(query, vars);
	}

	/**
	 * Get a value for a specific key.
	 *
	 * @param <T> a Player or String player name
	 * @param player the player this data relates to
	 * @param key a unique id (per plugin per player)
	 * @return the value stored
	 */
	public <T> String getData(final T player, final String key) {
		final Player p = MMO.playerFromName(player);
		Map<String, String> cache = null;
		String value = null;
		if (dbCache != null) {
			if (dbCache.containsKey(p)) {
				cache = dbCache.get(p);
			} else {
				dbCache.put(p, cache = new HashMapString<String>());
			}
			if (cache.containsKey(key)) {
				value = cache.get(key);
			}
		}
		if (value != null) {
			final Map<String, Object> result = query("SELECT value FROM mmoMinecraft WHERE 'plugin' = ? AND 'player' = ? AND 'key' = ?", description.getName(), p.getName(), key);
			value = (String) result.get("value");
			if (dbCache != null) {
				cache.put(key, value);
			}
		}
		return value;
	}

	/**
	 * Set the value for a specific key. If caching is turned on and there is no
	 * change then this won't try to write to the database.
	 * @param <T> a Player or String player name
	 * @param player the player this data relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set it to
	 */
	public <T> void setData(final T player, final String key, final String value) {
		final Player p = MMO.playerFromName(player);
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
	 * @param <T> a Player or String player name
	 * @param player the player this data relates to
	 * @param key a unique id (per plugin per player)
	 */
	public <T> void deleteData(final T player, final String key) {
		final Player p = MMO.playerFromName(player);
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
	 * Clear the shared cache for a single player. This is automatically called
	 * after players disconnect.
	 * @param <T> a Player or String player name
	 * @param player to clear
	 */
	public <T> void clearCache(final T player) {
		if (dbCache != null) {
			dbCache.remove(MMO.playerFromName(player));
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
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setString(final T player, final String key, final String value) {
		setData(player, key, value);
	}

	/**
	 * Get a string from the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> String getString(final T player, final String key, final String def) {
		String result = getData(player, key);
		if (result == null) {
			result = def;
		}
		return result;
	}

	/**
	 * Set a list of strings in the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setStringList(final T player, final String key, final List<String> value) {
		setData(player, key, MMO.join(value, ","));
	}

	/**
	 * Get a list of strings from the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> List<String> getStringList(final T player, final String key, final List<String> def) {
		String result = getData(player, key);
		if (result != null) {
			return Arrays.asList(result.split(","));
		}
		return def;
	}

	/**
	 * Set an integer in the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setInt(final T player, final String key, final int value) {
		setData(player, key, "" + value);
	}

	/**
	 * Get an integer from the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> int getInt(final T player, final String key, final int def) {
		String result = getData(player, key);
		if (result != null) {
			return Integer.parseInt(result);
		}
		return def;
	}

	/**
	 * Set a double in the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setDouble(final T player, final String key, final double value) {
		setData(player, key, "" + value);
	}

	/**
	 * Get a double from the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> double getDouble(final T player, final String key, final double def) {
		String result = getData(player, key);
		if (result != null) {
			return Double.parseDouble(result);
		}
		return def;
	}

	/**
	 * Set a boolean in the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setBoolean(final T player, final String key, final boolean value) {
		setData(player, key, value ? "true" : "false");
	}

	/**
	 * Get a boolean from the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> boolean getBoolean(final T player, final String key, final boolean def) {
		String result = getData(player, key);
		if (result != null) {
			return result.equals("true") ? true : false;
		}
		return def;
	}

	/**
	 * Set a Location in the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param value the value to set
	 */
	public <T> void setLocation(final T player, final String key, final Location value) {
		setData(player, key, value.getWorld().getName() + "," + value.getX() + "," + value.getY() + "," + value.getZ() + "," + value.getPitch() + "," + value.getYaw());
	}

	/**
	 * Get a Location from the shared database.
	 * @param <T> a Player or String player name
	 * @param player this relates to
	 * @param key a unique id (per plugin per player)
	 * @param def the default value if not found
	 * @return the data
	 */
	public <T> Location getLocation(final T player, final String key, final Location def) {
		String result = getData(player, key);
		if (result != null) {
			List<String> values = Arrays.asList(result.split(","));
			if (values.size() == 6) {
				try {
					return new Location(Bukkit.getWorld(values.get(0)),
							Double.parseDouble(values.get(1)), Double.parseDouble(values.get(2)),
							Double.parseDouble(values.get(3)), Float.parseFloat(values.get(4)),
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
	public boolean request(final Player player, final String id, final String description, final String... buttons) {
		return false;
	}
	/**
	 * Will add custom commands to plugins without needing the plugin.yml entry
	 *
	 * @Deprecated private void addCommand(String command) { Class<? extends
	 * Server> c = this.getServer().getClass(); try { Field f =
	 * c.getDeclaredField("commandMap"); f.setAccessible(true); SimpleCommandMap
	 * scm = (SimpleCommandMap) f.get(c); } catch (SecurityException e) {
	 * e.printStackTrace(); } catch (NoSuchFieldException e) {
	 * e.printStackTrace(); } catch (IllegalArgumentException e) {
	 * e.printStackTrace(); } catch (IllegalAccessException e) {
	 * e.printStackTrace(); } }
	 */
}
