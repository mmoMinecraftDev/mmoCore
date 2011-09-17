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
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Widget;

public class MMOCore extends MMOPlugin {

	private int updateTask;
	/**
	 * Config options - all mmoCore options are used for other plugins...
	 */
	static public String config_database_driver = "org.sqlite.JDBC";
	static public String config_database_url = "jdbc:sqlite:{DIR}{NAME}.db";
	static public String config_database_username = "root";
	static public String config_database_password = "";
	static public String config_database_isolation = "SERIALIZABLE";
	static public boolean config_database_logging = false;
	static public boolean config_database_rebuild = true;
	static public boolean config_show_display_name = false;
	static public boolean config_show_player_faces = true;
	static public int config_update_hours = 24;
	static public boolean config_update_download = false;

	@Override
	public void onEnable() {
		super.onEnable();

		mmoCorePlayerListener cpl = new mmoCorePlayerListener();
		pm.registerEvent(Type.PLAYER_JOIN, cpl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, cpl, Priority.Monitor, this);

			updateTask = getServer().getScheduler().scheduleSyncRepeatingTask(this,
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
	public void loadConfiguration(Configuration cfg) {
		config_show_display_name = cfg.getBoolean("show_display_name", config_show_display_name);
		config_show_player_faces = cfg.getBoolean("show_player_faces", config_show_player_faces);
		config_update_hours = cfg.getInt("update_hours", config_update_hours);
		config_update_download = cfg.getBoolean("update_download", config_update_download);

		config_database_driver = cfg.getString("database.driver", config_database_driver);
		config_database_url = cfg.getString("database.url", config_database_url);
		config_database_username = cfg.getString("database.username", config_database_username);
		config_database_password = cfg.getString("database.password", config_database_password);
		config_database_isolation = cfg.getString("database.isolation", config_database_isolation);
		config_database_logging = cfg.getBoolean("database.logging", config_database_logging);
		config_database_rebuild = cfg.getBoolean("database.rebuild", config_database_rebuild);
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
				if (args.length == 1 || (old = mmo.cfg.getProperty(args[1])) == null) {
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
							mmo.cfg.setProperty(args[1], Boolean.valueOf(args[2]));
						} else if (old instanceof Double) {
							mmo.cfg.setProperty(args[1], new Double(args[2]));
						} else if (old instanceof Integer) {
							mmo.cfg.setProperty(args[1], new Integer(args[2]));
						} else if (old instanceof List) {
							mmo.cfg.setProperty(args[1], args[2]);
						} else {
							mmo.cfg.setProperty(args[1], args[2]);
						}
						mmo.cfg.save();
					}
					sendMessage(sender, "Config: %s%s.%s%s: %s", ChatColor.YELLOW, mmo.getDescription().getName(), args[1], ChatColor.WHITE, mmo.cfg.getProperty(args[1]));
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

	public class mmoCorePlayerListener extends PlayerListener {

		@Override
		public void onPlayerJoin(PlayerJoinEvent event) {
			if (event.getPlayer().hasPermission("mmocore.update")) {
				String list = "";
				for (Plugin p : Arrays.asList(pm.getPlugins())) {
					if (p instanceof MMOPlugin && ((MMOPlugin) p).update) {
						list += (list.isEmpty() ? "" : ", ") + p.getDescription().getName();
					}
				}
				if (!list.isEmpty()) {
					sendMessage(event.getPlayer(), "Updates: %s", list);
				}
			}
		}

		@Override
		public void onPlayerRespawn(PlayerRespawnEvent event) {
			if (hasSpout && SpoutManager.getPlayer(event.getPlayer()).isSpoutCraftEnabled()) {
				for (Widget widget : SpoutManager.getPlayer(event.getPlayer()).getMainScreen().getAttachedWidgets()) {
					if (widget.getPlugin() instanceof MMOPlugin) {
						widget.setDirty(true);
					}
				}
			}
		}
	}

	public void checkVersion() {
		try {
			URL url = new URL("http://mmo.rycochet.net/versions.txt");
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
