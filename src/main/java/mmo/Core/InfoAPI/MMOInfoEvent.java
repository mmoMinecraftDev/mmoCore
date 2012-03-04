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
package mmo.Core.InfoAPI;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;

public abstract class MMOInfoEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * The player who owns this Info bar
	 * 
	 * @return the player we're drawing for
	 */
	public abstract SpoutPlayer getPlayer();

	/**
	 * Check if this token matches us (not including curly braces)
	 * 
	 * @param token
	 *            the name of the token we're looking for
	 * @return if the token exists
	 */
	public abstract boolean isToken(String token);

	/**
	 * Get the optional args for our token
	 * 
	 * @return an array of args, split by spaces unless within quotes
	 */
	public abstract String[] getArgs();

	/**
	 * Set the widget we want displayed
	 * 
	 * @param plugin
	 *            an instance of the plugin this widget is created for
	 * @param widget
	 *            any widget including a filled Container
	 */
	public abstract void setWidget(Plugin plugin, Widget widget);

	/**
	 * Get the widget we have set
	 * 
	 * @return the widget for this token
	 */
	public abstract Widget getWidget();

	/**
	 * Set the path of the icon we want to display, FileManager cache permitted
	 * 
	 * @param icon
	 *            the pathname of the icon we're using, either a url, or an
	 *            internal file using mmoSupport
	 */
	public abstract void setIcon(String icon);

	/**
	 * Set the path of the icon we want to display, FileManager cache permitted
	 */
	public abstract String getIcon();
}
