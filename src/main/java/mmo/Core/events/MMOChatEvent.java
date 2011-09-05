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

import java.util.HashSet;
import org.bukkit.entity.Player;

public interface MMOChatEvent {

	/**
	 * Check if a filter is supposed to be used
	 * @param filter The filter name
	 * @return 
	 */
	public boolean hasFilter(String filter);

	/**
	 * Set the message for a single player
	 * @param player
	 * @param message 
	 */
	public void setMessage(Player player, String message);

	/**
	 * Get the message for a single player
	 * @param player
	 * @return 
	 */
	public String getMessage(Player player);

	/**
	 * Set the default format for all players
	 * @param format 
	 */
	public void setFormat(String format);

	/**
	 * Set the format for a single player
	 * @param player
	 * @param format 
	 */
	public void setFormat(Player player, String format);

	/**
	 * Get the default format for all players
	 * @param player
	 * @return 
	 */
	public String getFormat();

	/**
	 * Get the format for a single player
	 * @param player
	 * @return 
	 */
	public String getFormat(Player player);

	/**
	 * Inherit from PlayerEvent
	 */
	public Player getPlayer();

	public boolean isCancelled();
	public void setCancelled(boolean cancel);
	public String getMessage();
	public void setMessage(String message);
	public HashSet<Player> getRecipients();
}
