/*
 * This file is part of mmoMinecraft (http://code.google.com/p/mmo-minecraft/).
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

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Core;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.SpoutManager;

public class mmoCore extends JavaPlugin {

	protected static Server server;
	protected static PluginManager pm;
	protected static PluginDescriptionFile description;
	protected static mmo mmo;
	protected String prefix = ChatColor.GREEN + "[" + ChatColor.AQUA + "mmoParty" + ChatColor.GREEN + "] " + ChatColor.WHITE;

	@Override
	public void onEnable() {
		server = getServer();
		pm = server.getPluginManager();
		description = getDescription();

		mmo = mmo.create(this);

		mmo.log("loading " + description.getFullName());

		if (pm.isPluginEnabled("Spout")) {
			mmo.hasSpout = true;
		}

		// Default values
		mmo.cfg.getBoolean("auto_update", true);
		mmo.cfg.getBoolean("show_display_name", false);
		mmo.cfg.save();

		mmoCorePlayerListener cpl = new mmoCorePlayerListener();
		pm.registerEvent(Type.PLAYER_JOIN, cpl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_QUIT, cpl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_KICK, cpl, Priority.Monitor, this);
	}

	@Override
	public void onDisable() {
		mmo.log("Disabled " + description.getFullName());
		mmo.autoUpdate();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		if ((player.isOp() || player.hasPermission("mmocore.set")) && command.getName().equalsIgnoreCase("mmoset")) {
			mmo plugin;
			if (args.length == 0 || (plugin = mmo.findPlugin(args[0])) == null) {
				mmo.sendMessage(player, "Plugins: %s", mmo.listPlugins());
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
					mmo.sendMessage(player, "Settings: %s", keys);
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
					mmo.sendMessage(player, "%s: %s", args[1], plugin.cfg.getProperty(args[1]).toString());
				}
			}
			return true;
		} else if ((player.isOp() || player.hasPermission("mmocore.update")) && command.getName().equalsIgnoreCase("mmoupdate")) {
			mmo.sendMessage(player, "Updating %s...", mmo.listPlugins());
			mmo.log("Checking for updates...");
			mmo.autoUpdate(true);
			mmo.sendMessage(player, "...Finished");
			return true;
		}
		return false;
	}

	public class mmoCorePlayerListener extends PlayerListener {

		@Override
		public void onPlayerQuit(PlayerQuitEvent event) {
		}

		@Override
		public void onPlayerKick(PlayerKickEvent event) {
		}
	}
}
