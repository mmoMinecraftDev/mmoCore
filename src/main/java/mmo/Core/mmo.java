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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;

public class mmo {

	/**
	 * All opened mmo Objects
	 */
	protected static final HashMap<String, mmo> modules = new HashMap<String, mmo>();
	/**
	 * Quick checking of the various plugins...
	 */
	public static boolean hasSpout = false;
	public static boolean mmoParty = false;
	public static boolean mmoPet = false;
	public static boolean mmoTarget = false;
	/**
	 * Spout
	 */
	public Plugin plugin;
	public static Server server;
	public static Logger log;
	private String prefix;
	private String title;
	private PluginDescriptionFile description;
	public Configuration cfg;

	/**
	 * Never want to manually create a new instance outside of mmo.create()
	 */
	private mmo() {
	}

	/**
	 * Create a new mmo Object, or return one already created.
	 * @param plugin The plugin to associate it with
	 * @return The new mmo Object
	 */
	public final static mmo create(Plugin plugin) {
		mmo me = null;
		String name = plugin.getDescription().getName();
		synchronized (modules) {
			me = modules.get(name);
			if (me == null) {
				me = new mmo();
				modules.put(name, me);
			}
			me.plugin = plugin;
			me.server = plugin.getServer();
			me.description = plugin.getDescription();
			me.cfg = plugin.getConfiguration();
			me.cfg.setHeader("#" + name + " Configuration");
			me.log = Logger.getLogger("Minecraft");
			me.setPluginName(name);
		}
		return me;
	}

	/**
	 * Return an already create mmo Object associated with a plugin.
	 * @param name The name of the plugin
	 * @return The mmo Object if it exists
	 */
	public static final mmo findPlugin(String name) {
		if (name != null) {
			return modules.get(name);
		}
		return null;
	}

	/**
	 * Get a list of installed plugin names
	 * @return Installed plugins
	 */
	public static final String listPlugins() {
		String list = "";
		for (String name : modules.keySet()) {
			if (!list.equals("")) {
				list += ", ";
			}
			list += name;
		}
		return list;
	}

	/**
	 * Set the notification title.
	 * @param title Title to use
	 */
	public final mmo setPluginName(String title) {
		this.title = title;
		this.prefix = ChatColor.GREEN + "[" + ChatColor.AQUA + title + ChatColor.GREEN + "] " + ChatColor.WHITE;
		return this;
	}

	/**
	 * Automatically update a plugin
	 */
	public void autoUpdate() {
		autoUpdate(false);
	}

	/**
	 * Automatically or manually update a plugin
	 */
	public void autoUpdate(boolean always) {
		if (always || cfg.getBoolean("auto_update", true)) {
			boolean canUpdate = false;
			try {
				URL url = new URL("http://mmo.rycochet.net/" + description.getName() + ".yml");

				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String str;
				while ((str = in.readLine()) != null) {
					if (str.startsWith("version: ")) {
						Float oldVersion = new Float(description.getVersion().replaceAll("[^0-9.]", ""));
						Float newVersion = new Float(str.replaceAll("[^0-9.]", ""));
						if (newVersion > oldVersion) {
							canUpdate = true;
							break;
						}
					}
				}
				in.close();
				if (canUpdate) {
					FileOutputStream fos = null;
					try {
						File directory = new File(Bukkit.getServer().getUpdateFolder());
						if (!directory.exists()) {
							try {
								directory.mkdir();
							} catch (SecurityException e1) {
							}
						}
						File newFile = new File(directory.getPath(), description.getName() + ".jar");
						if (newFile.canWrite()) {
							url = new URL("http://mmo.rycochet.net/" + description.getName() + ".jar");
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
				}
			} catch (Exception e) {
				canUpdate = false;
			}
			if (canUpdate) {
				log("Updated");
			}
		}
	}

	/**
	 * Send a message to one person by name.
	 * @param name The player to message
	 * @param msg The message to send
	 */
	public void sendMessage(String name, String msg, Object... args) {
		sendMessage(server.getPlayer(name), msg, args);
	}

	/**
	 * Send a message to multiple people.
	 * @param players The Players to message
	 * @param msg The message to send
	 */
	public void sendMessage(List<Player> players, String msg, Object... args) {
		for (Player player : players) {
			sendMessage(player, msg, args);
		}
	}

	/**
	 * Send a message to one person.
	 * @param player The Player to message
	 * @param msg The message to send
	 */
	public void sendMessage(Player player, String msg, Object... args) {
		if (player != null) {
			try {
				for (String line : String.format(msg, args).split("\n")) {
					player.sendMessage(prefix + line);
				}
			} catch (Exception e) {
				// Bad format->Object type
			}
		}
	}

	/**
	 * Get a chat colour by percent.
	 * @param percent The current value
	 * @return A ChatColor containing string
	 */
	public static String color(int percent, String text) {
		return color(percent, 100, text);
	}

	/**
	 * Get a chat colour by percent.
	 * @param num Current value
	 * @param max The maximum value
	 * @return A ChatColor containing string
	 */
	public static String color(int num, int max, String text) {
		String output = "";
		if (num >= (max * 0.75)) {
			output += ChatColor.GREEN;
		} else if (num >= (max * 0.5)) {
			output += ChatColor.AQUA;
		} else if (num >= (max * 0.25)) {
			output += ChatColor.YELLOW;
		} else {
			output += ChatColor.RED;
		}
		return output + text;
	}

	/**
	 * Get a chat colour by percent.
	 * @param percent The current value
	 * @return A ChatColor containing string
	 */
	public static String color(int percent) {
		return color(percent, 100);
	}

	/**
	 * Get a chat colour by percent.
	 * @param num Current value
	 * @param max The maximum value
	 * @return A ChatColor containing string
	 */
	public static String color(int num, int max) {
		String output = "";
		if (num >= (max * 0.75)) {
			output += ChatColor.GREEN;
		} else if (num >= (max * 0.5)) {
			output += ChatColor.AQUA;
		} else if (num >= (max * 0.25)) {
			output += ChatColor.YELLOW;
		} else {
			output += ChatColor.RED;
		}
		return output + num + ChatColor.WHITE;
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

	public void updateUI(String data) {
	}

	public void setGlobalTitle(LivingEntity target, String title) {
		if (hasSpout && target != null) {
			SpoutManager.getAppearanceManager().setGlobalTitle(target, title);
		}
	}

	/**
	 * Get the percentage health of a Player.
	 * @param player The Player we're interested in
	 * @return The percentage of max health
	 */
	public static int getHealth(Entity player) {
		if (player != null && player instanceof LivingEntity) {
			try {
				return Math.min(((LivingEntity) player).getHealth() * 5, 100);
			} catch (Exception e) {
			}
		}
		return 0;
	}

	/**
	 * Get the percentage armour of a Player.
	 * @param player The Player we're interested in
	 * @return The percentage of max armour
	 */
	public static int getArmor(Entity player) {
		if (player != null && player instanceof Player) {
			int armor = 0, max, multi[] = {15, 30, 40, 15};
			ItemStack inv[] = ((Player) player).getInventory().getArmorContents();
			for (int i = 0; i < inv.length; i++) {
				max = inv[i].getType().getMaxDurability();
				if (max >= 0) {
					armor += multi[i] * (max - inv[i].getDurability()) / max;
				}
			}
			return armor;
		}
		return 0;
	}

	public static LivingEntity[] getPets(HumanEntity player) {
		ArrayList<LivingEntity> pets = new ArrayList<LivingEntity>();
		if (player != null && (!(player instanceof Player) || ((Player)player).isOnline())) {
			String name = player.getName();
			for (World world : server.getWorlds()) {
				for (LivingEntity entity : world.getLivingEntities()) {
					if (entity instanceof Tameable && ((Tameable) entity).isTamed() && ((Tameable) entity).getOwner() instanceof Player) {
						if (name.equals(((Player) ((Tameable) entity).getOwner()).getName())) {
							pets.add(entity);
						}
					}
				}
			}
		}
		LivingEntity[] list = new LivingEntity[pets.size()];
		pets.toArray(list);
		return list;
	}

	/**
	 * Get the name of a LivingEntity target in colour.
	 * @param target The target to name
	 * @return The name to use
	 */
	public static String getName(LivingEntity target) {
		return getName(null, target);
	}

	/**
	 * Get the name of a LivingEntity target from a player's point of view.
	 * @param player The player viewing the target
	 * @param target The target to name
	 * @return The name to use
	 */
	public static String getName(Player player, LivingEntity target) {
		String name;
		if (target instanceof Player) {
			name = ChatColor.YELLOW + ((Player) target).getName();
		} else {
			if (target instanceof Monster) {
				if (player != null && player.equals(((Creature) target).getTarget())) {
					name = "" + ChatColor.RED;
				} else {
					name = "" + ChatColor.YELLOW;
				}
			} else if (target instanceof WaterMob) {
				name = "" + ChatColor.GREEN;
			} else if (target instanceof Flying) {
				name = "" + ChatColor.YELLOW;
			} else if (target instanceof Animals) {
				if (player != null && player.equals(((Creature) target).getTarget())) {
					name = "" + ChatColor.RED;
				} else if (target instanceof Tameable) {
					Tameable pet = (Tameable) target;
					if (pet.isTamed()) {
						name = "" + ChatColor.YELLOW;
					} else {
						name = "" + ChatColor.GREEN;
					}
				} else {
					name = "" + ChatColor.GRAY;
				}
			} else {
				name = "" + ChatColor.GRAY;
			}
			name += getSimpleName(target, true);
			if (target instanceof Tameable) {
				Tameable pet = (Tameable) target;
				if (pet.isTamed() && pet.getOwner() instanceof HumanEntity) {
					name = ChatColor.YELLOW + ((HumanEntity) pet.getOwner()).getName() + "'s " + name;
				}
			}
		}
		return name;
	}

	/**
	 * Get the colour of a LivingEntity target from a player's point of view.
	 * @param player The player viewing the target
	 * @param target The target to name
	 * @return The name to use
	 */
	public static String getColor(Player player, LivingEntity target) {
		if (target instanceof Player) {
			return ChatColor.YELLOW.toString();
		} else {
			if (target instanceof Monster) {
				if (player != null && player.equals(((Creature) target).getTarget())) {
					return ChatColor.RED.toString();
				} else {
					return ChatColor.YELLOW.toString();
				}
			} else if (target instanceof WaterMob) {
				return ChatColor.GREEN.toString();
			} else if (target instanceof Flying) {
				return ChatColor.YELLOW.toString();
			} else if (target instanceof Animals) {
				if (player != null && player.equals(((Creature) target).getTarget())) {
					return ChatColor.RED.toString();
				} else if (target instanceof Tameable) {
					Tameable pet = (Tameable) target;
					if (pet.isTamed()) {
						return ChatColor.GREEN.toString();
					} else {
						return ChatColor.YELLOW.toString();
					}
				} else {
					return ChatColor.GRAY.toString();
				}
			} else {
				return ChatColor.GRAY.toString();
			}
		}
	}

	public static String getSimpleName(LivingEntity target, boolean showOwner) {
		String name = "";
		if (target instanceof Player) {
			if (mmoCore.mmo.cfg.getBoolean("show_display_name", false)) {
				name += ((Player)target).getName();
			} else {
				name += ((Player)target).getDisplayName();
			}
		} else {
			if (target instanceof Tameable) {
				if (((Tameable)target).isTamed()) {
					if (showOwner && ((Tameable)target).getOwner() instanceof Player) {
						if (mmoCore.mmo.cfg.getBoolean("show_display_name", false)) {
							name += ((Player)((Tameable)target).getOwner()).getName() + "'s ";
						} else {
							name += ((Player)((Tameable)target).getOwner()).getDisplayName() + "'s ";
						}
					} else {
						name += "Pet ";
					}
				}
			}
			if (target instanceof Chicken) {
				name += "Chicken";
			} else if (target instanceof Cow) {
				name += "Cow";
			} else if (target instanceof Creeper) {
				name += "Creeper";
			} else if (target instanceof Ghast) {
				name += "Ghast";
			} else if (target instanceof Giant) {
				name += "Giant";
			} else if (target instanceof Pig) {
				name += "Pig";
			} else if (target instanceof PigZombie) {
				name += "PigZombie";
			} else if (target instanceof Sheep) {
				name += "Sheep";
			} else if (target instanceof Slime) {
				name += "Slime";
			} else if (target instanceof Skeleton) {
				name += "Skeleton";
			} else if (target instanceof Spider) {
				name += "Spider";
			} else if (target instanceof Squid) {
				name += "Squid";
			} else if (target instanceof Wolf) {
				name += "Wolf";
			} else if (target instanceof Zombie) {
				name += "Zombie";
			} else if (target instanceof Monster) {
				name += "Monster";
			} else if (target instanceof Creature) {
				name += "Creature";
			} else {
				name += "Unknown";
			}
		}
		return name;
	}

	public static String name(String name) {
		return name(name, true);
	}

	public static String name(String name, boolean online) {
		return (online ? ChatColor.YELLOW : ChatColor.GRAY) + name + ChatColor.WHITE;
	}

	public static String makeBar(ChatColor prefix, int current) {
		String bar = "||||||||||";
		int left = Math.min(Math.max(0, (current + 9) / 10), 10); // 0 < current < 10
		int right = 10 - left;
		return prefix + bar.substring(0, left) + (current > 0 ? ChatColor.DARK_GRAY : ChatColor.BLACK) + bar.substring(0, right) + " ";
	}

	public void log(String text) {
		log.log(Level.INFO, "[" + description.getName() + "] " + text);
	}
}
