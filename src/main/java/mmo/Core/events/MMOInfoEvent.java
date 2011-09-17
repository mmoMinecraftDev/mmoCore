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
package mmo.Core.events;

import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;

public interface MMOInfoEvent extends Cancellable {
	/**
	 * The player who owns this Info bar
	 * @return 
	 */
	public SpoutPlayer getPlayer();

	/**
	 * Check if this token matches us (not including curly braces)
	 * @param token
	 * @return 
	 */
	public boolean isToken(String token);

	/**
	 * Get the optional args for our token
	 * @return 
	 */
	public String[] getArgs();

	/**
	 * Set the widget we want displayed
	 * @param plugin
	 * @param widget 
	 */
	public void setWidget(Plugin plugin, Widget widget);

	/**
	 * Get the widget we have set
	 * @return 
	 */
	public Widget getWidget();

	/**
	 * Set the path of the icon we want to display, FileManager cache permitted
	 */
	public void setIcon(String icon);
}
