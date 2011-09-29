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
package mmo.Core.ChatAPI;

import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public interface MMOChatEvent extends Cancellable {

	/**
	 * Check if a filter is supposed to be used.
	 * @param filter the name of the filter
	 * @return if it should be used
	 */
	public boolean hasFilter(String filter);

	/**
	 * Get the args supplied for the filter.
	 * This string array is smart-split, so spaces within quotes will be
	 * a single arg.
	 * @param filter the name of the filter
	 * @return if it should be used
	 */
	public String[] getArgs(String filter);

	/**
	 * Set the message for a single player
	 * @param player the Player getting a custom message
	 * @param message the custom message
	 */
	public void setMessage(Player player, String message);

	/**
	 * Set the default message for all players.
	 * @param message the default message
	 */
	public void setMessage(String message);

	/**
	 * Get the message for a single player, get the default message if no custom message exists.
	 * @param player the Player we're interested in
	 * @return the message for that player
	 */
	public String getMessage(Player player);

	/**
	 * Get the default message for all players.
	 * @return the default message
	 */
	public String getMessage();

	/**
	 * Set the default format string for all players.
	 * "%1$s" = Channel name.
	 * "%2$s" = From name.
	 * "%3$s" = To name.
	 * "%4$s" = Message.
	 * @param format the default format for all players
	 */
	public void setFormat(String format);

	/**
	 * Set the format string for a single player.
	 * "%1$s" = Channel name.
	 * "%2$s" = From name.
	 * "%3$s" = To name.
	 * "%4$s" = Message.
	 * @param player the Player getting a custom format string
	 * @param format the format string for that player
	 */
	public void setFormat(Player player, String format);

	/**
	 * Get the default format string for all players.
	 * "%1$s" = Channel name.
	 * "%2$s" = From name.
	 * "%3$s" = To name.
	 * "%4$s" = Message.
	 * @return the default format string
	 */
	public String getFormat();

	/**
	 * Get the format string for a single player.
	 * @param player the Player we're interested in
	 * @return the format string for that player
	 */
	public String getFormat(Player player);

	/**
	 * Get the player who sent the message.
	 */
	public Player getPlayer();

	/**
	 * Get the list of all players that can currently see the message.
	 * This list should be edited via .remove or .retainAll to remove people
	 * when they shouldn't see the output of this event.
	 * @return a list of players
	 */
	public Set<Player> getRecipients();
}
