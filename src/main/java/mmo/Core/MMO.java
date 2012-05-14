/*
 * This file is part of mmoCore <http://github.com/mmoMinecraftDev/mmoCore>.
 *
 * mmoCore is free software: you can redistribute it and/or modify
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

import javax.xml.bind.TypeConstraintException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class MMO {
	/**
	 * Never want to manually create a new instance - we're static only
	 */
	private MMO() {
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
		return color(num, max) + text;
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
	 * @param player the Player we're interested in
	 * @return the percentage of max armour
	 */
	public static int getArmor(final Entity player) {
		int armor = 0;
		if (player instanceof Player) {
			final int[] multi = {15, 30, 40, 15};
			final ItemStack[] inv = ((Player) player).getInventory().getArmorContents();
			for (int i = 0; i < inv.length; i++) {
				final int max = inv[i].getType().getMaxDurability();
				if (max > 0) {
					armor += multi[i] * (max - inv[i].getDurability()) / max;
				}
			}
		}
		return armor;
	}

	/**
	 * Get a list of pets belonging to a target.
	 * @param player the player we're interested in
	 * @return a list of their pets
	 */
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
	 * @param target the target to name
	 * @return the name to use
	 */
	public static String getName(LivingEntity target) {
		return getName(null, target);
	}

	/**
	 * Get the name of a LivingEntity target from a player's point of view.
	 * @param player the player viewing the target
	 * @param target the target to name
	 * @return the name to use
	 */
	public static String getName(Player player, LivingEntity target) {
		String name = getColor(player, target) + getSimpleName(target, true);
		if (target instanceof Tameable) {
			Tameable pet = (Tameable) target;
			if (pet.isTamed() && pet.getOwner() instanceof LivingEntity) {
				name = getColor(player, (LivingEntity) pet.getOwner()) + name;
			}
		}
		return name;
	}

	/**
	 * Get the colour of a LivingEntity target from a player's point of view.
	 * @param player the player viewing the target
	 * @param target the target to name
	 * @return the colour to use
	 */
	public static String getColor(Player player, LivingEntity target) {
		if (target instanceof Player) {
			for (String arg : MMOCore.default_colours.keySet()) {
				if ((arg.equals("op") && ((Player) target).isOp()) || arg.equals("default") || ((Player) target).hasPermission(arg)) {
					String color = MMOCore.default_colours.get(arg);
					if (color.matches("[0-9a-f]")) {
						return "§" + color;
					}
					return ChatColor.valueOf(color.toUpperCase()).toString();
				}
			}
			return ((Player) target).isOp() ? ChatColor.GOLD.toString() : ChatColor.YELLOW.toString();
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

	/**
	 * Get a simple name for a living entity.
	 * @param target	the target we want named
	 * @param showOwner if we prefix a pet's name with the owner's name
	 * @return the full name
	 */
	public static String getSimpleName(LivingEntity target, boolean showOwner) {
		String name = "";
		if (target instanceof ComplexLivingEntity) {
			if (target instanceof EnderDragon) {
				name += "Ender Dragon";
			} else {
				name += "Complex";
			}
		} else if (target instanceof Creature) {
			if (target instanceof Tameable) {
				if (((Tameable) target).isTamed()) {
					if (showOwner && ((Tameable) target).getOwner() instanceof Player) {
						Player owner = (Player) ((Tameable) target).getOwner();
						if (MMOCore.config_show_display_name) {
							name += owner.getName() + "'s ";
						} else {
							name += owner.getDisplayName() + "'s ";
						}
					} else {
						name += "Pet ";
					}
				}
			}
			if(!((Ageable) target).isAdult()) name+="Baby ";
			if (target instanceof Animals) {
				if (target instanceof Chicken) {
					name += "Chicken";
				} else if (target instanceof MushroomCow) { //Mooshrooms need to be before Cow because they subclass Cow.
					name += "Mooshroom";
				} else if (target instanceof Cow) {
					name += "Cow";
				} else if (target instanceof Ocelot){
					name += ((Tameable) target).isTamed() ? "Cat" : "Ocelot";
				} else if (target instanceof Pig) {
					name += "Pig";
				} else if (target instanceof Sheep) {
					name += "Sheep";
				} else if (target instanceof Wolf) {
					name += "Wolf";
				} else {
					name += "Animal";
				}
			} else if(target instanceof Golem) {
				if(target instanceof IronGolem) {
					name += "Iron Golem";
				} else if(target instanceof Snowman) {
					name += "Snow Golem";
				} else {
					name += "Golem";
				}
			} else if (target instanceof Monster) {
				if (target instanceof Blaze) {
					name += "Blaze";
				} else if (target instanceof Creeper) {
					name += "Creeper";
				} else if (target instanceof Enderman) {
					name += "Enderman";
				} else if (target instanceof Giant) {
					name += "Giant";
				} else if (target instanceof Silverfish) {
					name += "Silverfish";
				} else if (target instanceof Skeleton) {
					name += "Skeleton";
				} else if (target instanceof CaveSpider) {
					name += "CaveSpider";
				} else if (target instanceof Spider) {
					name += "Spider";
				} else if (target instanceof PigZombie) {
					name += "PigZombie";
				} else if (target instanceof Zombie) {
					name += "Zombie";
				} else {
					name += "Monster";
				}
			} else if (target instanceof NPC) {
				if (target instanceof Villager) {
					name += "Villager";
				} else {
					name += "NPC";
				}
			} else if (target instanceof WaterMob) {
				if (target instanceof Squid) {
					name += "Squid";
				} else {
					name += "WaterMob";
				}
			} else {
				name += "Creature";
			}
		} else if (target instanceof Flying) {
			if (target instanceof Ghast) {
				name += "Ghast";
			} else {
				name += "Flying";
			}
		} else if(target instanceof HumanEntity) {
			if (target instanceof Player && !MMOCore.config_show_display_name) {
				name += ((Player) target).getDisplayName();
			} else {
				name += ((HumanEntity) target).getName();
			}
		} else if (target instanceof Slime) {
			if(target instanceof MagmaCube) {
				name += "Magma Cube";
			} else {
				name += "Slime";
			}
		} else {
			name += "Unknown";
		}
		return name;
	}

	/**
	 * Get a coloured name for a player.
	 * @param name the player name
	 * @return a string including colour
	 */
	public static String name(String name) {
		return name(name, true);
	}

	/**
	 * Get a coloured name for a player.
	 * @param name   the player name
	 * @param online is they are currently online
	 * @return a string including colour
	 */
	public static String name(String name, boolean online) {
		return (online ? ChatColor.YELLOW : ChatColor.GRAY) + name + ChatColor.WHITE;
	}

	/**
	 * Get a string health bar etc.
	 * @param prefix  a string to output before the bar
	 * @param current between 0 and 10
	 * @return a string with the bar in it
	 */
	public static String makeBar(ChatColor prefix, int current) {
		String bar = "||||||||||";
		int left = Math.min(Math.max(0, (current + 9) / 10), 10); // 0 < current < 10
		int right = 10 - left;
		return prefix + bar.substring(0, left) + (current > 0 ? ChatColor.DARK_GRAY : ChatColor.BLACK) + bar.substring(0, right) + " ";
	}

	/**
	 * Return the first word of a line.
	 * @param line a string of text
	 * @return the first word
	 */
	public static String firstWord(String line) {
		int endIndex = line.indexOf(" ");
		if (endIndex == -1) {
			endIndex = line.length();
		}
		return line.substring(0, endIndex);
	}

	/**
	 * Return a line with the first word removed.
	 * @param line a string of text
	 * @return everything except the first word
	 */
	public static String removeFirstWord(String line) {
		int endIndex = line.indexOf(" ");
		if (endIndex == -1) {
			endIndex = line.length();
		}
		return line.substring(endIndex).trim();
	}

	/**
	 * Split a String by spaces, but understand both single and double quotes.
	 * @param text a string to split
	 * @return a list of args
	 */
	public static String[] smartSplit(String text) {
		ArrayList<String> list = new ArrayList<String>();
		Matcher match = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(text);
		while (match.find()) {
			list.add(match.group(1) != null ? match.group(1) : match.group(2) != null ? match.group(2) : match.group());
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Re-split args by spaces, but understand both single and double quotes.
	 * @param args an array of args
	 * @return a list of args
	 */
	public static String[] smartSplit(String[] args) {
		return smartSplit(join(args, " "));
	}

	/**
	 * Join an array into a string with space delimiters.
	 * @param array an array of strings
	 * @return the final string
	 */
	public static String join(String[] array) {
		return join(array, " ");
	}

	/**
	 * Join an array into a string.
	 * @param array	 an array of strings
	 * @param delimiter the string to place between each word
	 * @return the final string
	 */
	public static String join(String[] array, String delimiter) {
		String output = "";
		for (String word : array) {
			output += (output.isEmpty() ? "" : delimiter) + word;
		}
		return output;
	}

	/**
	 * Join an array into a string with space delimiters.
	 * @param array an array of strings
	 * @return the final string
	 */
	public static String join(List<String> array) {
		return join(array, " ");
	}

	/**
	 * Join a list into a string.
	 * @param array	 a list of strings
	 * @param delimiter the string to place between each word
	 * @return the final string
	 */
	public static String join(List<String> array, String delimiter) {
		String output = "";
		for (String word : array) {
			output += (output.isEmpty() ? "" : delimiter) + word;
		}
		return output;
	}

	/**
	 * Return a player name from a Player or string.
	 * @param <T>    a Player or String
	 * @param player the player to find
	 * @return the player name
	 */
	public static <T> String nameFromPlayer(T player) {
		if (player instanceof Player) {
			return ((Player) player).getName();
		} else if (player instanceof String) {
			return (String) player;
		} else {
			throw new TypeConstraintException("'player' must be Player or String");
		}
	}

	/**
	 * Return a Player from a Player or string.
	 * @param <T>    a Player or String
	 * @param player the player to find
	 * @return the player name
	 */
	public static <T> Player playerFromName(T player) {
		if (player instanceof Player) {
			return (Player) player;
		} else if (player instanceof String) {
			return Bukkit.getPlayerExact((String) player);
		} else {
			throw new TypeConstraintException("'player' must be Player or String");
		}
	}

	/**
	 * Return a CommandSender from a Player or string.
	 * @param <T>    a Player or String
	 * @param player the player to find
	 * @return the player name
	 */
	public static <T> CommandSender senderFromName(T player) {
		if (player instanceof CommandSender) {
			return (CommandSender) player;
		} else if (player instanceof Player) {
			return (Player) player;
		} else if (player instanceof String) {
			return Bukkit.getPlayerExact((String) player);
		} else {
			throw new TypeConstraintException("'player' must be CommandSender, Player or String");
		}
	}
}
