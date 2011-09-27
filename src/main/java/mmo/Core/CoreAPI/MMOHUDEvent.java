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
package mmo.Core.CoreAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.WidgetAnchor;

/**
 * Used to alter the HUD item locations
 */
public class MMOHUDEvent extends Event {

	Player player;
	Plugin plugin;
	WidgetAnchor anchor;
	int offsetX, offsetY;

	public MMOHUDEvent(Player player, Plugin plugin, WidgetAnchor anchor, int offsetX, int offsetY) {
		super("mmoHUDEvent");
		this.player = player;
		this.plugin = plugin;
		this.anchor = anchor;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public Player getPlayer() {
		return player;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public WidgetAnchor getAnchor() {
		return anchor;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}
}
