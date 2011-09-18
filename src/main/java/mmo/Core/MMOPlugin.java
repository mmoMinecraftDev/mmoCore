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
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.Core.events.MMOHUDEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
	/**
	 * Variables
	 */
	private MMOPluginDatabase database;
	protected PluginDescriptionFile description;
	protected Configuration cfg;
	protected PluginManager pm;
	protected Server server;
	protected Logger logger;
	protected String title;
	protected String prefix;
	protected static MMOPlugin mmoCore;
	protected MMOPlugin plugin;
	public static boolean hasSpout = false;
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

		/**
		 * Move mmoPlugin/* to mmoCore/*
		 */
		if (getDataFolder().exists()) {
			try {
				for (File from : getDataFolder().listFiles()) {
					String name = from.getName();
					boolean isConfig = false;
					if (name.equalsIgnoreCase("config.yml")) {
						isConfig = true;
						name = description.getName() + ".yml";
					}
					if ((isConfig || this != mmoCore) && !from.renameTo(new File(mmoCore.getDataFolder(), name))) {
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
			cfg = new Configuration(new File(mmoCore.getDataFolder() + File.separator + description.getName() + ".yml"));
			cfg.load();
			loadConfiguration(cfg);
			if (!cfg.getKeys().isEmpty()) {
				cfg.setHeader("#" + title + " Configuration");
				cfg.save();
			}
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
								new File(mmoCore.getDataFolder() + File.separator + description.getName() + File.separator).mkdir();
								found = true;
							}
							File f = new File(mmoCore.getDataFolder() + File.separator + description.getName() + File.separator + name);
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
		log("Disabled " + description.getFullName());
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
		return this.getDataFolder() + File.separator + name;
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
		MMOHUDEventEvent event = new MMOHUDEventEvent(player, plugin, anchor, offsetX, offsetY);
		pm.callEvent(event);
		Container container = (Container) new GenericContainer().setAlign(event.anchor).setAnchor(event.anchor).setFixed(true).setX(event.offsetX).setY(event.offsetY).setWidth(427).setHeight(240);
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
			database = new MMOPluginDatabase();
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

	/**
	 * Used to alter the HUD item locations
	 */
	private class MMOHUDEventEvent extends Event implements MMOHUDEvent {

		Player player;
		MMOPlugin plugin;
		WidgetAnchor anchor;
		int offsetX, offsetY;

		public MMOHUDEventEvent(Player player, MMOPlugin plugin, WidgetAnchor anchor, int offsetX, int offsetY) {
			super("mmoHUDEvent");
			this.player = player;
			this.plugin = plugin;
			this.anchor = anchor;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		@Override
		public Player getPlayer() {
			return player;
		}

		@Override
		public MMOPlugin getPlugin() {
			return plugin;
		}

		@Override
		public WidgetAnchor getAnchor() {
			return anchor;
		}

		@Override
		public int getOffsetX() {
			return offsetX;
		}

		@Override
		public void setOffsetX(int offsetX) {
			this.offsetX = offsetX;
		}

		@Override
		public int getOffsetY() {
			return offsetY;
		}

		@Override
		public void setOffsetY(int offsetY) {
			this.offsetY = offsetY;
		}
	}

	/*
	 * MMOPluginDatabase by Lennard Fonteijn - http://www.lennardf1989.com/
	 * http://forums.bukkit.org/threads/24987/
	 * There may be alterations to more easily fit mmoMinecraft ;-)
	 */
	private class MMOPluginDatabase {

		private ClassLoader classLoader;
		private Level loggerLevel;
		private boolean usingSQLite;
		private ServerConfig serverConfig;
		private EbeanServer ebeanServer;

		/**
		 * Create an instance of MMOPluginDatabase
		 * @param javaPlugin Plugin instancing this database
		 */
		public MMOPluginDatabase() {
			//Try to get the ClassLoader of the plugin using Reflection
			try {
				//Find the "getClassLoader" method and make it "public" instead of "protected"
				Method method = JavaPlugin.class.getDeclaredMethod("getClassLoader");
				method.setAccessible(true);

				//Store the ClassLoader
				this.classLoader = (ClassLoader) method.invoke(plugin);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to retrieve the ClassLoader of the plugin using Reflection", ex);
			}
		}

		/**
		 * Initialize the database using the passed arguments
		 * 
		 * @param driver        Database-driver to use. For example: org.sqlite.JDBC
		 * @param url           Location of the database. For example: jdbc:sqlite:{DIR}{NAME}.db
		 * @param username      Username required to access the database
		 * @param password      Password belonging to the username, may be empty
		 * @param isolation     Isolation type. For example: SERIALIZABLE, also see TransactionIsolation
		 * @param logging       If set to false, all logging will be disabled
		 * @param rebuild       If set to true, all tables will be dropped and recreated. Be sure to create a backup before doing so!
		 */
		public void initializeDatabase(String driver, String url, String username, String password, String isolation, boolean logging, boolean rebuild) {
			//Logging needs to be set back to the original level, no matter what happens
			try {
				//Disable all logging
				disableDatabaseLogging(logging);
				//Prepare the database
				prepareDatabase(driver, url, username, password, isolation);
				//Load the database
				loadDatabase();
				//Create all tables
				installDatabase(rebuild);
			} catch (Exception ex) {
				throw new RuntimeException("An exception has occured while initializing the database", ex);
			} finally {
				//Enable all logging
				enableDatabaseLogging(logging);
			}
		}

		private void prepareDatabase(String driver, String url, String username, String password, String isolation) {
			//Setup the data source
			DataSourceConfig ds = new DataSourceConfig();
			ds.setDriver(driver);
			ds.setUrl(replaceDatabaseString(url));
			ds.setUsername(username);
			ds.setPassword(password);
			ds.setIsolationLevel(TransactionIsolation.getLevel(isolation));
			//Setup the server configuration
			ServerConfig sc = new ServerConfig();
			sc.setDefaultServer(false);
			sc.setRegister(false);
			sc.setName(ds.getUrl().replaceAll("[^a-zA-Z0-9]", ""));
			//Get all persistent classes
			List<Class<?>> classes = plugin.getDatabaseClasses();
			//Do a sanity check first
			if (classes.isEmpty()) {
				//Exception: There is no use in continuing to load this database
				throw new RuntimeException("Database has been enabled, but no classes are registered to it");
			}
			//Register them with the EbeanServer
			sc.setClasses(classes);
			//Check if the SQLite JDBC supplied with Bukkit is being used
			if (ds.getDriver().equalsIgnoreCase("org.sqlite.JDBC")) {
				//Remember the database is a SQLite-database
				usingSQLite = true;
				//Modify the platform, as SQLite has no AUTO_INCREMENT field
				sc.setDatabasePlatform(new SQLitePlatform());
				sc.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
			}
			//Finally the data source
			sc.setDataSourceConfig(ds);
			//Store the ServerConfig
			serverConfig = sc;
		}

		private void loadDatabase() {
			//Declare a few local variables for later use
			ClassLoader currentClassLoader = null;
			Field cacheField = null;
			boolean cacheValue = true;
			try {
				//Store the current ClassLoader, so it can be reverted later
				currentClassLoader = Thread.currentThread().getContextClassLoader();
				//Set the ClassLoader to Plugin ClassLoader
				Thread.currentThread().setContextClassLoader(classLoader);
				//Get a reference to the private static "defaultUseCaches"-field in URLConnection
				cacheField = URLConnection.class.getDeclaredField("defaultUseCaches");
				//Make it accessible, store the default value and set it to false
				cacheField.setAccessible(true);
				cacheValue = cacheField.getBoolean(null);
				cacheField.setBoolean(null, false);
				//Setup Ebean based on the configuration
				ebeanServer = EbeanServerFactory.create(serverConfig);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to create a new instance of the EbeanServer", ex);
			} finally {
				//Revert the ClassLoader back to its original value
				if (currentClassLoader != null) {
					Thread.currentThread().setContextClassLoader(currentClassLoader);
				}
				//Revert the "defaultUseCaches"-field in URLConnection back to its original value
				try {
					if (cacheField != null) {
						cacheField.setBoolean(null, cacheValue);
					}
				} catch (Exception e) {
					System.out.println("Failed to revert the \"defaultUseCaches\"-field back to its original value, URLConnection-caching remains disabled.");
				}
			}
		}

		private void installDatabase(boolean rebuild) {
			//Check if the database has to be rebuild
			if (!rebuild) {
				return;
			}
			//Create a DDL generator
			SpiEbeanServer serv = (SpiEbeanServer) ebeanServer;
			DdlGenerator gen = serv.getDdlGenerator();
			//Check if the database already (partially) exists
			boolean databaseExists = false;
			List<Class<?>> classes = plugin.getDatabaseClasses();
			for (int i = 0; i < classes.size(); i++) {
				try {
					//Do a simple query which only throws an exception if the table does not exist
					ebeanServer.find(classes.get(i)).findRowCount();
					//Query passed without throwing an exception, a database therefore already exists
					databaseExists = true;
					break;
				} catch (Exception ex) {
					//Do nothing
				}
			}
			//Fire "before drop" event
			try {
				beforeDropDatabase();
			} catch (Exception ex) {
				//If the database exists, dropping has to be canceled to prevent data-loss
				if (databaseExists) {
					throw new RuntimeException("An unexpected exception occured", ex);
				}
			}
			//Generate a DropDDL-script
			gen.runScript(true, gen.generateDropDdl());
			//If SQLite is being used, the database has to reloaded to release all resources
			if (usingSQLite) {
				loadDatabase();
			}
			//Generate a CreateDDL-script
			if (usingSQLite) {
				//If SQLite is being used, the CreateDLL-script has to be validated and potentially fixed to be valid
				gen.runScript(false, validateCreateDDLSqlite(gen.generateCreateDdl()));
			} else {
				gen.runScript(false, gen.generateCreateDdl());
			}
			//Fire "after create" event
			try {
				afterCreateDatabase();
			} catch (Exception ex) {
				throw new RuntimeException("An unexpected exception occured", ex);
			}
		}

		private String replaceDatabaseString(String input) {
			input = input.replaceAll("\\{DIR\\}", mmoCore.getDataFolder().getPath().replaceAll("\\\\", "/") + "/");
			input = input.replaceAll("\\{NAME\\}", plugin.getDescription().getName().replaceAll("[^\\w_-]", ""));
			return input;
		}

		private String validateCreateDDLSqlite(String oldScript) {
			try {
				//Create a BufferedReader out of the potentially invalid script
				BufferedReader scriptReader = new BufferedReader(new StringReader(oldScript));
				//Create an array to store all the lines
				List<String> scriptLines = new ArrayList<String>();
				//Create some additional variables for keeping track of tables
				HashMap<String, Integer> foundTables = new HashMap<String, Integer>();
				String currentTable = null;
				int tableOffset = 0;
				//Loop through all lines
				String currentLine;
				while ((currentLine = scriptReader.readLine()) != null) {
					//Trim the current line to remove trailing spaces
					currentLine = currentLine.trim();
					//Add the current line to the rest of the lines
					scriptLines.add(currentLine.trim());
					//Check if the current line is of any use
					if (currentLine.startsWith("create table")) {
						//Found a table, so get its name and remember the line it has been encountered on
						currentTable = currentLine.split(" ", 4)[2];
						foundTables.put(currentLine.split(" ", 3)[2], scriptLines.size() - 1);
					} else if (currentLine.startsWith(";") && currentTable != null && !currentTable.equals("")) {
						//Found the end of a table definition, so update the entry
						int index = scriptLines.size() - 1;
						foundTables.put(currentTable, index);
						//Remove the last ")" from the previous line
						String previousLine = scriptLines.get(index - 1);
						previousLine = previousLine.substring(0, previousLine.length() - 1);
						scriptLines.set(index - 1, previousLine);
						//Change ";" to ");" on the current line
						scriptLines.set(index, ");");
						//Reset the table-tracker
						currentTable = null;
					} else if (currentLine.startsWith("alter table")) {
						//Found a potentially unsupported action
						String[] alterTableLine = currentLine.split(" ", 4);
						if (alterTableLine[3].startsWith("add constraint")) {
							//Found an unsupported action: ALTER TABLE using ADD CONSTRAINT
							String[] addConstraintLine = alterTableLine[3].split(" ", 4);
							//Check if this line can be fixed somehow
							if (addConstraintLine[3].startsWith("foreign key")) {
								//Calculate the index of last line of the current table
								int tableLastLine = foundTables.get(alterTableLine[2]) + tableOffset;
								//Add a "," to the previous line
								scriptLines.set(tableLastLine - 1, scriptLines.get(tableLastLine - 1) + ",");
								//Add the constraint as a new line - Remove the ";" on the end
								String constraintLine = String.format("%s %s %s", addConstraintLine[1], addConstraintLine[2], addConstraintLine[3]);
								scriptLines.add(tableLastLine, constraintLine.substring(0, constraintLine.length() - 1));
								//Remove this line and raise the table offset because a line has been inserted
								scriptLines.remove(scriptLines.size() - 1);
								tableOffset++;
							} else {
								//Exception: This line cannot be fixed but is known the be unsupported by SQLite
								throw new RuntimeException("Unsupported action encountered: ALTER TABLE using ADD CONSTRAINT with " + addConstraintLine[3]);
							}
						}
					}
				}
				//Turn all the lines back into a single string
				String newScript = "";
				for (String newLine : scriptLines) {
					newScript += newLine + "\n";
				}
				//Print the new script
				System.out.println(newScript);
				//Return the fixed script
				return newScript;
			} catch (Exception ex) {
				//Exception: Failed to fix the DDL or something just went plain wrong
				throw new RuntimeException("Failed to validate the CreateDDL-script for SQLite", ex);
			}
		}

		private void disableDatabaseLogging(boolean logging) {
			//If logging is allowed, nothing has to be changed
			if (logging) {
				return;
			}
			//Retrieve the level of the root logger
			loggerLevel = Logger.getLogger("").getLevel();
			//Set the level of the root logger to OFF
			Logger.getLogger("").setLevel(Level.OFF);
		}

		private void enableDatabaseLogging(boolean logging) {
			//If logging is allowed, nothing has to be changed
			if (logging) {
				return;
			}
			//Set the level of the root logger back to the original value
			Logger.getLogger("").setLevel(loggerLevel);
		}

		/**
		 * Method called before the loaded database is being dropped
		 */
		protected void beforeDropDatabase() {
			plugin.beforeDropDatabase();
		}

		/**
		 * Method called after the loaded database has been created
		 */
		protected void afterCreateDatabase() {
			plugin.afterCreateDatabase();
		}

		/**
		 * Get the instance of the EbeanServer
		 * 
		 * @return EbeanServer Instance of the EbeanServer
		 */
		public EbeanServer getDatabase() {
			return ebeanServer;
		}
	}
}
