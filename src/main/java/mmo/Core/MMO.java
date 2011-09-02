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
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.WidgetAnchor;

public class MMO {

	/**
	 * All opened mmo Objects
	 */
	protected static final HashMap<String, MMO> modules = new HashMap<String, MMO>();
	/**
	 * Quick checking of the various plugins...
	 */
	public static boolean hasSpout = false;
	public static boolean mmoParty = false;
	public static boolean mmoPet = false;
	public static boolean mmoTarget = false;
	public static boolean mmoFriends = false;
	public static boolean mmoChat = false;
	public static boolean mmoInfo = false;
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
	private MMO() {
	}

	/**
	 * Create a new mmo Object, or return one already created.
	 * @param plugin The plugin to associate it with
	 * @return The new mmo Object
	 */
	public static MMO create(Plugin plugin) {
		MMO me = null;
		String name = plugin.getDescription().getName();
		synchronized (modules) {
			me = modules.get(name);
			if (me == null) {
				me = new MMO();
				modules.put(name, me);
			}
			me.plugin = plugin;
			me.description = plugin.getDescription();
			me.cfg = plugin.getConfiguration();
			me.cfg.setHeader("#" + name + " Configuration");
			MMO.server = plugin.getServer();
			MMO.log = Logger.getLogger("Minecraft");
			me.setPluginName(name);
		}
		return me;
	}

	/**
	 * Return an already create mmo Object associated with a plugin.
	 * @param name The name of the plugin
	 * @return The mmo Object if it exists
	 */
	public static MMO findPlugin(String name) {
		if (name != null) {
			return modules.get(name);
		}
		return null;
	}

	/**
	 * Get a list of installed plugin names
	 * @return Installed plugins
	 */
	public static String listPlugins() {
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
	public final MMO setPluginName(String title) {
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
						String oldVersion[] = description.getVersion().replaceAll("[^0-9.]", "").split(".");
						String newVersion[] = str.replaceAll("[^0-9.]", "").split(".");
						int oldVer = new Integer(oldVersion[0]), oldRev = new Integer(oldVersion[1]);
						int newVer = new Integer(newVersion[0]), newRev = new Integer(newVersion[1]);
						if (newVer > oldVer || (newVer == oldVer && newRev > oldRev)) {
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
	public void sendMessage(Player player, String msg, Object... args) {
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
	public void sendMessage(boolean prefix, List<Player> players, String msg, Object... args) {
		for (Player player : players) {
			sendMessage(prefix, player, msg, args);
		}
	}

	/**
	 * Send a message to one person.
	 * @param player The Player to message
	 * @param msg The message to send
	 */
	public void sendMessage(boolean prefix, Player player, String msg, Object... args) {
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

	/**
	 * Get the container for use by this plugin, anchor and position can be overridden by options.
	 * @return 
	 */
	public GenericContainer getContainer() {
		WidgetAnchor anchor = WidgetAnchor.TOP_LEFT;
		String anchorName = cfg.getString("ui.default.align", "TOP_LEFT");
		int offsetX = cfg.getInt("ui.default.left", 0), offsetY = cfg.getInt("ui.default.top", 0);
		int extra = mmoInfo ? 11 : 0; // If mmoInfo exists

		if ("TOP_LEFT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_LEFT;
			offsetY += extra;
		} else if ("TOP_CENTER".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_CENTER;
			offsetX -= 213;
			offsetY += extra;
		} else if ("TOP_RIGHT".equalsIgnoreCase(anchorName)) {
			anchor = WidgetAnchor.TOP_RIGHT;
			offsetX = -427 - offsetX;
			offsetY += extra;
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
		GenericContainer container = new GenericContainer();
		container.setAlign(anchor).setAnchor(anchor).setX(offsetX).setY(offsetY).setWidth(427).setHeight(240).setFixed(true);
		return container;
	}

	public void updateUI(String data) {
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
		if (player != null && (!(player instanceof Player) || ((Player) player).isOnline())) {
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
			if (((Player) target).isOp()) {
				return ChatColor.GOLD.toString();
			}
			return ChatColor.YELLOW.toString();
		} else {
			if (target instanceof Monster) {
				if (player != null && player.equals(((Monster) target).getTarget())) {
					return ChatColor.RED.toString();
				} else {
					return ChatColor.YELLOW.toString();
				}
			} else if (target instanceof WaterMob) {
				return ChatColor.GREEN.toString();
			} else if (target instanceof Flying) {
				return ChatColor.YELLOW.toString();
			} else if (target instanceof Animals) {
				if (player != null && player.equals(((Animals) target).getTarget())) {
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
			if (MMOCore.mmo.cfg.getBoolean("show_display_name", false)) {
				name += ((Player) target).getName();
			} else {
				name += ((Player) target).getDisplayName();
			}
		} else {
			if (target instanceof Tameable) {
				if (((Tameable) target).isTamed()) {
					if (showOwner && ((Tameable) target).getOwner() instanceof Player) {
						if (MMOCore.mmo.cfg.getBoolean("show_display_name", false)) {
							name += ((Player) ((Tameable) target).getOwner()).getName() + "'s ";
						} else {
							name += ((Player) ((Tameable) target).getOwner()).getDisplayName() + "'s ";
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

	public void log(String text, Object... args) {
		log.log(Level.INFO, "[" + description.getName() + "] " + String.format(text, args));
	}

	/**
	 * Return the first word of a line
	 * @param line
	 * @return 
	 */
	public static String firstWord(String line) {
		int endIndex = line.indexOf(" ");
		if (endIndex == -1) {
			endIndex = line.length();
		}
		return line.substring(0, endIndex);
	}

	/**
	 * Return a line with the first word removed
	 * @param line
	 * @return 
	 */
	public static String removeFirstWord(String line) {
		int endIndex = line.indexOf(" ");
		if (endIndex == -1) {
			endIndex = line.length();
		}
		return line.substring(endIndex).trim();
	}

	public static int getStringHeight(String text) {
		return text.split("\n").length * 10;
	}

	public static int getStringWidth(String text) {
		final int[] characterWidths = new int[]{
			1, 9, 9, 8, 8, 8, 8, 7, 9, 8, 9, 9, 8, 9, 9, 9,
			8, 8, 8, 8, 9, 9, 8, 9, 8, 8, 8, 8, 8, 9, 9, 9,
			4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
			7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
			3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6,
			6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6,
			6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6,
			8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6,
			9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
			9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9,
			8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7,
			7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1
		};
		final String allowedCharacters = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~?Ã³ÚÔõÓÕþÛÙÞ´¯ý─┼╔µã¶÷‗¹¨ Í▄°úÏÎâßÝ¾·±Ð¬║┐«¼¢╝í½╗";
		int length = 0;
		for (String line : ChatColor.stripColor(text).split("\n")) {
			int lineLength = 0;
			for (int i = 0; i < line.length(); i++) {
				char ch = text.charAt(i);
				if (ch == '\u00A7') {
					i++;
					continue;
				}
				int index = allowedCharacters.indexOf(ch);
				if (index == -1) {
					continue;
				}
				lineLength += characterWidths[index + 32];
				length = Math.max(length, lineLength);
			}
		}
		return length;
	}
}
