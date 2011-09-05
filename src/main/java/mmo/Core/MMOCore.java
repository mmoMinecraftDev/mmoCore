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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.config.Configuration;

public class MMOCore extends MMOPlugin {

	/**
	 * Config options - all mmoCore options are used for other plugins...
	 */
	static String config_database_driver = "org.sqlite.JDBC";
	static String config_database_url = "jdbc:sqlite:{DIR}{NAME}.db";
	static String config_database_username = "root";
	static String config_database_password = "";
	static String config_database_isolation = "SERIALIZABLE";
	static boolean config_database_logging = false;
	static boolean config_database_rebuild = true;
	static boolean config_show_display_name = false;
	static boolean config_show_player_faces = true;

	@Override
	public void onEnable() {
		super.onEnable();
		MMO.plugin = this;

		mmoCorePlayerListener cpl = new mmoCorePlayerListener();
		pm.registerEvent(Type.PLAYER_JOIN, cpl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_QUIT, cpl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_KICK, cpl, Priority.Monitor, this);
	}

	@Override
	public void onDisable() {
//		mmo.autoUpdate();
		super.onDisable();
	}

	@Override
	public void loadConfiguration(Configuration cfg) {
		config_show_display_name = cfg.getBoolean("show_display_name", config_show_display_name);
		config_show_player_faces = cfg.getBoolean("show_player_faces", config_show_player_faces);

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
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
/*
		if ((player.isOp() || player.hasPermission("mmocore.set")) && command.getName().equalsIgnoreCase("mmoset")) {
			MMO plugin;
			if (args.length == 0 || (plugin = MMO.findPlugin(args[0])) == null) {
				sendMessage(player, "Plugins: %s", MMO.listPlugins());
			} else {
				Object old;
				if (args.length == 1 || (old = plugin.cfg.getProperty(args[1])) == null) {
					String keys = "";
					for (String key : plugin.cfg.getKeys(args.length == 1 ? null : args[1])) {
						if (!keys.equals("")) {
							keys += ", ";
						}
						keys += key;
					}
					sendMessage(player, "Settings: %s", keys);
				} else {
					if (args.length > 2) {
						if (old instanceof Boolean) {
							plugin.cfg.setProperty(args[1], Boolean.valueOf(args[2]));
						} else if (old instanceof Double) {
							plugin.cfg.setProperty(args[1], new Double(args[2]));
						} else if (old instanceof Integer) {
							plugin.cfg.setProperty(args[1], new Integer(args[2]));
						} else if (old instanceof List) {
							plugin.cfg.setProperty(args[1], args[2]);
						} else {
							plugin.cfg.setProperty(args[1], args[2]);
						}
						plugin.cfg.save();
					}
					sendMessage(player, "%s: %s", args[1], plugin.cfg.getProperty(args[1]).toString());
				}
			}
			return true;
		} else if ((player.isOp() || player.hasPermission("mmocore.update")) && command.getName().equalsIgnoreCase("mmoupdate")) {
			sendMessage(player, "Updating %s...", MMO.listPlugins());
			log("Checking for updates...");
			mmo.autoUpdate(true);
			sendMessage(player, "...Finished");
			return true;
		}
*/
		return false;
	}

	public static class mmoCorePlayerListener extends PlayerListener {

		@Override
		public void onPlayerQuit(PlayerQuitEvent event) {
		}

		@Override
		public void onPlayerKick(PlayerKickEvent event) {
		}
	}
}
