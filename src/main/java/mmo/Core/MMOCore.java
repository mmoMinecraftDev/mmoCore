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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import mmo.Core.SQLibrary.HSQLDB;
import mmo.Core.SQLibrary.MySQL;
import mmo.Core.SQLibrary.SQLite;
import mmo.Core.util.EnumBitSet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.event.spout.SpoutcraftFailedEvent;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;

public class MMOCore extends MMOPlugin {

	/**
	 * List of plugins that want to react on various Player events
	 */
	static protected List<MMOPlugin> support_mmo_player = new ArrayList<MMOPlugin>();
	/**
	 * Task to check for mmoMinecraft updates
	 */
	private int updateTask;
	/**
	 * Config options - all mmoCore options are used for other plugins...
	 */
	static public String config_database_type = "sqlite";
	static public String config_database_mysql_hostname = "localhost";
	static public String config_database_mysql_port = "3306";
	static public String config_database_mysql_database = "minecraft";
	static public String config_database_mysql_username = "root";
	static public String config_database_mysql_password = "";
	static public String config_database_hsqldb_hostname = "localhost";
	static public String config_database_hsqldb_port = "9001";
	static public String config_database_hsqldb_database = "minecraft";
	static public String config_database_hsqldb_username = "sa";
	static public String config_database_hsqldb_password = "";
	static public boolean config_show_display_name = false;
	static public boolean config_show_player_faces = true;
	static public int config_update_hours = 24;
	static public boolean config_update_download = false;
	static public List<String> config_colours = new ArrayList<String>();
	static public LinkedHashMap<String, String> default_colours = new LinkedHashMap<String, String>();

	@Override
	public EnumBitSet mmoSupport(EnumBitSet support) {
		support.set(Support.MMO_DATABASE);
		return support;
	}

	@Override
	public void onEnable() {
		super.onEnable();

		mmoCorePlayerListener cpl = new mmoCorePlayerListener();
		pm.registerEvents(cpl, this);
		updateTask = server.getScheduler().scheduleSyncRepeatingTask(this,
				new Runnable() {

					@Override
					public void run() {
						checkVersion();
					}
				}, 20 * 60 * 60 * config_update_hours, 20 * 60 * 60 * config_update_hours);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(updateTask);
		super.onDisable();
	}

	@Override
	public void loadConfiguration(FileConfiguration cfg) {
		config_show_display_name = cfg.getBoolean("show_display_name", config_show_display_name);
		config_show_player_faces = cfg.getBoolean("show_player_faces", config_show_player_faces);
		config_update_hours = cfg.getInt("update_hours", config_update_hours);
		config_update_download = cfg.getBoolean("update_download", config_update_download);
		config_database_type = cfg.getString("database.type", config_database_type);
		config_database_mysql_hostname = cfg.getString("database.mysql.hostname", config_database_mysql_hostname);
		config_database_mysql_port = cfg.getString("database.mysql.port", config_database_mysql_port);
		config_database_mysql_database = cfg.getString("database.mysql.database", config_database_mysql_database);
		config_database_mysql_username = cfg.getString("database.mysql.username", config_database_mysql_username);
		config_database_mysql_password = cfg.getString("database.mysql.password", config_database_mysql_password);
		config_database_hsqldb_hostname = cfg.getString("database.hsqldb.hostname", config_database_hsqldb_hostname);
		config_database_hsqldb_port = cfg.getString("database.hsqldb.port", config_database_hsqldb_port);
		config_database_hsqldb_database = cfg.getString("database.hsqldb.database", config_database_hsqldb_database);
		config_database_hsqldb_username = cfg.getString("database.hsqldb.username", config_database_hsqldb_username);
		config_database_hsqldb_password = cfg.getString("database.hsqldb.password", config_database_hsqldb_password);
		config_colours.add("op=GOLD");
		config_colours.add("default=YELLOW");
		config_colours = cfg.getStringList("player_colors");//, config_colours); // american spelling for config, proper spelling for us!
		default_colours.clear();
		for (String arg : config_colours) {
			String[] perm = arg.split("=");
			if (perm.length == 2) {
				default_colours.put(perm[0].trim(), perm[1].trim());
			}
		}


		if (database != null) {
			database.close();
			database = null;
		}
		if (SQLite.type.equalsIgnoreCase(MMOCore.config_database_type)) {
			database = new SQLite(null, new File(singleFolder ? "plugins/mmoMinecraft" : getDataFolder().getPath(), "mmoMinecraft.db"));
		} else if (MySQL.type.equalsIgnoreCase(MMOCore.config_database_type)) {
			database = new MySQL(null,
					MMOCore.config_database_mysql_hostname,
					MMOCore.config_database_mysql_port,
					MMOCore.config_database_mysql_database,
					MMOCore.config_database_mysql_username,
					MMOCore.config_database_mysql_password);
		} else if (HSQLDB.type.equalsIgnoreCase(MMOCore.config_database_type)) {
			database = new HSQLDB(null,
					MMOCore.config_database_hsqldb_hostname,
					MMOCore.config_database_hsqldb_port,
					MMOCore.config_database_hsqldb_database,
					MMOCore.config_database_hsqldb_username,
					MMOCore.config_database_hsqldb_password);
		} else {
			log("Unknown database type: " + MMOCore.config_database_type);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("mmocore.set") && command.getName().equalsIgnoreCase("mmoset")) {
			args = MMO.smartSplit(args);
			MMOPlugin mmo = null;
			String list = "";
			for (Plugin p : Arrays.asList(pm.getPlugins())) {
				if (p instanceof MMOPlugin) {
					String name = p.getDescription().getName();
					list += (list.isEmpty() ? "" : ", ") + name;
					if (args.length > 0 && name.equalsIgnoreCase(args[0])) {
						mmo = (MMOPlugin) p;
					}
				}
			}
			if (args.length == 0 || mmo == null) {
				sendMessage(sender, "Plugins: %s", list);
			} else {
				Object old;
				if (args.length == 1 || (old = mmo.cfg.get(args[1])) == null) {
					String keys = "";
					for (String key : mmo.cfg.getKeys(args.length == 1 ? null : args[1])) {
						if (!keys.equals("")) {
							keys += ", ";
						}
						keys += key;
					}
					sendMessage(sender, "Config: %s%s%s: %s", ChatColor.YELLOW, mmo.getDescription().getName(), ChatColor.WHITE, keys);
				} else {
					if (args.length > 2) {
						if (old instanceof Boolean) {
							mmo.cfg.set(args[1], Boolean.valueOf(args[2]));
						} else if (old instanceof Double) {
							mmo.cfg.set(args[1], new Double(args[2]));
						} else if (old instanceof Integer) {
							mmo.cfg.set(args[1], new Integer(args[2]));
						} else if (old instanceof List) {
							mmo.cfg.set(args[1], args[2]);
						} else {
							mmo.cfg.set(args[1], args[2]);
						}
						mmo.cfg.save();
					}
					sendMessage(sender, "Config: %s%s.%s%s: %s", ChatColor.YELLOW, mmo.getDescription().getName(), args[1], ChatColor.WHITE, mmo.cfg.getString(args[1]));
				}
			}
			return true;
		} else if (sender.hasPermission("mmocore.update") && command.getName().equalsIgnoreCase("mmoupdate")) {
			sendMessage(sender, "Checking for updates...");
			checkVersion();
			String list = "";
			for (Plugin p : Arrays.asList(pm.getPlugins())) {
				if (p instanceof MMOPlugin && ((MMOPlugin) p).update) {
					getUpdate((MMOPlugin) p);
					list += (list.isEmpty() ? "" : ", ") + p.getDescription().getName();
				}
			}
			sendMessage(sender, "...Updated: %s", list.isEmpty() ? "none" : list);
			return true;
		}
		return false;
	}

	public void redrawAll(Player player) {
		SpoutPlayer splayer = SpoutManager.getPlayer(player);
		if (splayer.isSpoutCraftEnabled()) {
			for (Widget widget : splayer.getMainScreen().getAttachedWidgets()) {
				if (widget.getPlugin() instanceof MMOPlugin) {
					widget.setDirty(true);
				}
			}
		}
	}

	public class mmoCorePlayerListener implements Listener {

		@EventHandler (priority = EventPriority.MONITOR)
		public void onPlayerJoin(PlayerJoinEvent event) {
			Player player = event.getPlayer();
			if (player.hasPermission("mmocore.update")) {
				String list = "";
				for (Plugin p : Arrays.asList(pm.getPlugins())) {
					if (p instanceof MMOPlugin && ((MMOPlugin) p).update) {
						list += (list.isEmpty() ? "" : ", ") + p.getDescription().getName();
					}
				}
				if (!list.isEmpty()) {
					sendMessage(player, "Updates: %s", list);
				}
			}
			for (MMOPlugin plugin : support_mmo_player) {
				plugin.onPlayerJoin(player);
			}
		}

		@EventHandler (priority = EventPriority.MONITOR)
		public void onPlayerQuit(PlayerQuitEvent event) {
			Player player = event.getPlayer();
			for (MMOPlugin plugin : support_mmo_player) {
				plugin.onPlayerQuit(player);
			}
			for (Plugin p : Arrays.asList(pm.getPlugins())) {
				if (p instanceof MMOPlugin) {
					((MMOPlugin) p).clearCache(player);
				}
			}
		}

		@EventHandler (priority = EventPriority.MONITOR)
		public void onPlayerKick(PlayerKickEvent event) {
			Player player = event.getPlayer();
			for (MMOPlugin plugin : support_mmo_player) {
				plugin.onPlayerQuit(player);
			}
			for (Plugin p : Arrays.asList(pm.getPlugins())) {
				if (p instanceof MMOPlugin) {
					((MMOPlugin) p).clearCache(player);
				}
			}
		}

		@EventHandler (priority = EventPriority.MONITOR)
		public void onPlayerRespawn(PlayerRespawnEvent event) {
			redrawAll(event.getPlayer());
		}

		@EventHandler (priority = EventPriority.MONITOR)
		public void onPlayerPortal(PlayerPortalEvent event) {
			redrawAll(event.getPlayer());
		}

		@EventHandler (priority = EventPriority.MONITOR)
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
				redrawAll(event.getPlayer());
			}
		}
	}

	public class mmoCoreSpoutListener implements Listener {

		@EventHandler (priority = EventPriority.MONITOR)
		public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
			SpoutPlayer player = event.getPlayer();
			for (MMOPlugin plugin : support_mmo_player) {
				plugin.onSpoutCraftPlayer(player);
			}
		}

		@EventHandler (priority = EventPriority.MONITOR)
		public void onSpoutcraftFailed(SpoutcraftFailedEvent event) {
			Player player = event.getPlayer();
			for (MMOPlugin plugin : support_mmo_player) {
				plugin.onNormalPlayer(player);
			}
		}
	}

	public void checkVersion() {
		try {
			URL url = new URL("http://files.mmo.me.uk/versions.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String str;
			while ((str = in.readLine()) != null) { // mmoPlugin:0.1:descrption
				String[] tokens = str.split(":");
				if (tokens.length == 3) {
					Plugin mmo = pm.getPlugin(tokens[0]);
					if (mmo != null && mmo instanceof MMOPlugin && !((MMOPlugin) mmo).update) {
						String newVersion[] = tokens[1].split("\\.");
						int newVer = Integer.parseInt(newVersion[0]), newRev = Integer.parseInt(newVersion[1]);
						if (newVer > ((MMOPlugin) mmo).version || (newVer == ((MMOPlugin) mmo).version && newRev > ((MMOPlugin) mmo).revision)) {
							((MMOPlugin) mmo).update = true;
							log("Update found: %s", ((MMOPlugin) mmo).description.getName());
						}
					}
				}
			}
			in.close();
		} catch (Exception e) {
		}
	}

	public void getUpdate(MMOPlugin mmo) {
		try {
			FileOutputStream fos = null;
			try {
				File directory = new File(getServer().getUpdateFolder());
				if (!directory.exists()) {
					try {
						directory.mkdir();
					} catch (SecurityException e1) {
					}
				}
				File newFile = new File(directory.getPath(), mmo.description.getName() + ".jar");
				if (newFile.canWrite()) {
					URL url = new URL("http://mmo.rycochet.net/" + mmo.description.getName() + ".jar");
					HttpURLConnection con = (HttpURLConnection) (url.openConnection());
					ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
					fos = new FileOutputStream(newFile);
					fos.getChannel().transferFrom(rbc, 0, 1 << 24);
				}
			} catch (Exception e) {
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (Exception e) {
		}
	}
}
