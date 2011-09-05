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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class MMO {

	/**
	 * Quick checking of the various plugins...
	 */
	public static boolean mmoParty = false;
	public static boolean mmoPet = false;
	public static boolean mmoTarget = false;
	public static boolean mmoFriends = false;
	public static boolean mmoChat = false;
	public static boolean mmoInfo = false;

	public static MMOCore plugin;

	/**
	 * Never want to manually create a new instance outside of mmo.create()
	 */
	private MMO() {
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
		if (always || plugin.config_auto_update) {
			boolean canUpdate = false;
			try {
				URL url = new URL("http://mmo.rycochet.net/" + plugin.getDescription().getName() + ".yml");

				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String str;
				while ((str = in.readLine()) != null) {
					if (str.startsWith("version: ")) {
						String oldVersion[] = plugin.getDescription().getVersion().replaceAll("[^0-9.]", "").split(".");
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
						File newFile = new File(directory.getPath(), plugin.getDescription().getName() + ".jar");
						if (newFile.canWrite()) {
							url = new URL("http://mmo.rycochet.net/" + plugin.getDescription().getName() + ".jar");
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
				plugin.log("Updated");
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
			for (World world : Bukkit.getServer().getWorlds()) {
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
			if (MMOCore.config_show_display_name) {
				name += ((Player) target).getName();
			} else {
				name += ((Player) target).getDisplayName();
			}
		} else if (target instanceof HumanEntity) {
			name += ((HumanEntity) target).getName();
		} else {
			if (target instanceof Tameable) {
				if (((Tameable) target).isTamed()) {
					if (showOwner && ((Tameable) target).getOwner() instanceof Player) {
						if (MMOCore.config_show_display_name) {
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
