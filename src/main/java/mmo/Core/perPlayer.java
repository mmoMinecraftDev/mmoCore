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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class perPlayer {

	private static final Map<String, perPlayer> players = new HashMap<String, perPlayer>();
	String player;

	protected static perPlayer get(Player player) {
		return get(player.getName());
	}

	protected static perPlayer get(String name) {
		perPlayer data = players.get(name);
		if (data == null) {
			data = new perPlayer();
			data.player = name;
			data.setup();
			if (mmo.hasSpout) {
				data.setupSpout();
			}
			players.put(name, data);
		}
		return data;
	}

	protected static void remove(Player player) {
		remove(player.getName());
	}

	protected static void remove(String name) {
		players.remove(name);
	}

	protected static void clear() {
		players.clear();
	}

	/**
	 * Setup non-Spout data
	 */
	protected void setup() {
	}

	/**
	 * Setup Spout related data
	 */
	protected void setupSpout() {
	}
}
