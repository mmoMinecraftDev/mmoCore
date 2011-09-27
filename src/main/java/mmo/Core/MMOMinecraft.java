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

import mmo.Core.ChatAPI.Chat;
import org.bukkit.plugin.Plugin;

public class MMOMinecraft {
	/**
	 * Quick checking of the various plugin APIs...
	 */
	public static boolean mmoChat = false;
	public static boolean mmoDamage = false;
	public static boolean mmoInfo = false;
	public static boolean mmoParty = false;
	public static boolean mmoSkill = false;
	/**
	 * The various plugin APIs themselves
	 */
	private static Chat mmoChatAPI = null;
	/**
	 * Static class only
	 */
	private MMOMinecraft() {
	}

	/**
	 * Keep note of plugins that provide an API for quick boolean check of their existence.
	 * @param plugin the plugin being enabled
	 */
	static public void enablePlugin(Plugin plugin) {
		String name = plugin.getDescription().getName();
		if ("mmoChat".equals(name)) {
			mmoChat = true;
		} else if ("mmoDamage".equals(name)) {
			mmoDamage = true;
		} else if ("mmoInfo".equals(name)) {
			mmoInfo = true;
		} else if ("mmoParty".equals(name)) {
			mmoParty = true;
		} else if ("mmoSkill".equals(name)) {
			mmoSkill = true;
		}
	}

	/**
	 * Keep note of plugins that provide an API for quick boolean check of their existence.
	 * @param plugin the plugin being enabled
	 * @param api an instance of the plugin API class
	 */
	static public void setAPI(Plugin plugin, Object api) {
		String name = plugin.getDescription().getName();
		if ("mmoChat".equals(name)) {
			mmoChatAPI = (Chat) api;
		} else if ("mmoDamage".equals(name)) {
			mmoDamage = true;
		} else if ("mmoInfo".equals(name)) {
			mmoInfo = true;
		} else if ("mmoParty".equals(name)) {
			mmoParty = true;
		} else if ("mmoSkill".equals(name)) {
			mmoSkill = true;
		}
	}

	/**
	 * Set the quick check boolean of API providing plugins to false.
	 * @param plugin The plugin being disabled
	 */
	static public void disablePlugin(Plugin plugin) {
		String name = plugin.getDescription().getName();
		if ("mmoChat".equals(name)) {
			mmoChat = false;
			mmoChatAPI = null;
		} else if ("mmoDamage".equals(name)) {
			mmoDamage = false;
		} else if ("mmoInfo".equals(name)) {
			mmoInfo = false;
		} else if ("mmoParty".equals(name)) {
			mmoParty = false;
		} else if ("mmoSkill".equals(name)) {
			mmoSkill = false;
		}
	}

	/**
	 * Get the mmoChat API.
	 * @return an instance of Chat
	 */
	static public Chat getChat() {
		return mmoChatAPI;
	}
}
