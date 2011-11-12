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
import mmo.Core.GroupAPI.Group;
import mmo.Core.PartyAPI.Party;
import org.bukkit.plugin.Plugin;

/**
 * MMOMinecraft provides access to the various APIs within mmoMinecraft.
 */
public class MMOMinecraft {

	/**
	 * Does mmoChat exist - quicker than isPluginEnabled.
	 */
	public static boolean mmoChat = false;
	/**
	 * Does mmoDamage exist - quicker than isPluginEnabled.
	 */
	public static boolean mmoDamage = false;
	/**
	 * Does mmoInfo exist - quicker than isPluginEnabled.
	 */
	public static boolean mmoInfo = false;
	/**
	 * Does mmoParty exist - quicker than isPluginEnabled.
	 */
	public static boolean mmoParty = false;
	/**
	 * Does mmoSkill exist - quicker than isPluginEnabled.
	 */
	public static boolean mmoSkill = false;
	/**
	 * Does mmoGroup exist - quicker than isPluginEnabled.
	 */
	public static boolean mmoGroup = false;
	/**
	 * mmoChat API
	 */
	private static Chat mmoChatAPI = null;
	/**
	 * mmoParty API
	 */
	private static Party mmoPartyAPI = null;
	/**
	 * mmoGroup API
	 */
	private static Group mmoGroupAPI = null;

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
		} else if ("mmoGroup".equals(name)) {
			mmoGroup = true;
		}
	}

	/**
	 * Keep note of plugins that provide an API for quick boolean check of their existence.
	 * @param api an instance of the plugin API class
	 */
	static public void addAPI(Object api) {
		if (api instanceof Chat) {
			mmoChatAPI = (Chat) api;
		} else if (api instanceof Party) {
			mmoPartyAPI = (Party) api;
		} else if (api instanceof Group) {
			mmoGroupAPI = (Group) api;
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
			mmoPartyAPI = null;
		} else if ("mmoSkill".equals(name)) {
			mmoSkill = false;
		} else if ("mmoGroup".equals(name)) {
			mmoGroup = false;
			mmoGroupAPI = null;
		}
	}

	/**
	 * Get the mmoChat API.
	 * @return an instance of Chat
	 */
	static public Chat getChat() {
		return mmoChatAPI;
	}

	/**
	 * Get the mmoParty API.
	 * @return an instance of Party
	 */
	static public Party getParty() {
		return mmoPartyAPI;
	}

	/**
	 * Get the mmoGroup API.
	 * @return an instance of Group
	 */
	static public Group getGroup() {
		return mmoGroupAPI;
	}

}
