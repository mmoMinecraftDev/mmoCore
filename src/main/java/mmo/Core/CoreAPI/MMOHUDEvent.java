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
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.WidgetAnchor;

/**
 * Used to alter the HUD item locations.
 */
public class MMOHUDEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private Plugin plugin;
	private WidgetAnchor anchor;
	private int offsetX, offsetY;

	public MMOHUDEvent(final Player player, final Plugin plugin, final WidgetAnchor anchor, final int offsetX, final int offsetY) {
		super();
		this.player = player;
		this.plugin = plugin;
		this.anchor = anchor;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/**
	 * Get the player this HUD element is for.
	 *
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Get the plugin opening a HUD element.
	 *
	 * @return the plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Get the anchor point requested.
	 *
	 * @return the anchor
	 */
	public WidgetAnchor getAnchor() {
		return anchor;
	}

	/**
	 * Get the X offset of the element.
	 *
	 * @return X offset
	 */
	public int getOffsetX() {
		return offsetX;
	}

	/**
	 * Set the X offset for the element.
	 *
	 * @param offsetX in pixels
	 */
	public void setOffsetX(final int offsetX) {
		this.offsetX = offsetX;
	}

	/**
	 * Get the Y offset for the element.
	 *
	 * @return Y offset
	 */
	public int getOffsetY() {
		return offsetY;
	}

	/**
	 * Set the Y offset for the element.
	 *
	 * @param offsetY in pixels
	 */
	public void setOffsetY(final int offsetY) {
		this.offsetY = offsetY;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
