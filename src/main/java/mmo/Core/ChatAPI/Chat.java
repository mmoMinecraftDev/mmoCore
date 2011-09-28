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

import org.bukkit.entity.Player;

public interface Chat {

	/**
	 * Perform a chat message, if there is an empty message then set the default channel.
	 * @param channel the channel to send the message on
	 * @param from the Player who is sending the message
	 * @param message the message to send
	 * @return if the message was sent
	 */
	public boolean doChat(String channel, Player from, String message);

	/**
	 * Hide output of a channel from a Player.
	 * @param player the Player wanting to hide a channel
	 * @param channel the channel being hidden
	 * @return if the channel exists
	 */
	public boolean hideChannel(Player player, String channel);

	/**
	 * Show output of a channel to a Player.
	 * @param player the Player wanting to see a channel
	 * @param channel the channel being shown
	 * @return if the channel exists
	 */
	public boolean showChannel(Player player, String channel);

	/**
	 * Set the default chat channel for a Player.
	 * @param player the Player whose channel we are changing
	 * @param channel the channel being chosen
	 * @return if the channel exists
	 */
	public boolean setChannel(Player player, String channel);

	/**
	 * Checks the permissions nodes for read access to a channel.
	 * Checks permission nodes in order:
	 * "mmo.chat.channel.see"
	 * "mmo.chat.*.see"
	 * "mmo.chat.channel"
	 * "mmo.chat.*"
	 * If no node is found then access is true.
	 * @param player the Player we are checking
	 * @param channel the channel to check
	 * @return if we can see it
	 */
	public boolean seeChannel(Player player, String channel);

	/**
	 * Checks the permissions nodes for write access to a channel.
	 * Checks permission nodes in order:
	 * "mmo.chat.channel.use"
	 * "mmo.chat.*.use"
	 * "mmo.chat.channel"
	 * "mmo.chat.*"
	 * If no node is found then access is true.
	 * @param player the Player we are checking
	 * @param channel the channel to check
	 * @return if we can use it
	 */
	public boolean useChannel(Player player, String channel);

	/**
	 * Find the proper capitalisation of a channel name.
	 * @param channel the name of the channel
	 * @return the real name of the channel or null
	 */
	public String findChannel(String channel);

	/**
	 * Check if a channel exists.
	 * @param channel the name of the channel
	 * @return if it exists
	 */
	public boolean isChannel(String channel);

	/**
	 * Get the default channel name for a Player.
	 * @param player the Player we want the channel for
	 * @return the name of their default channel
	 */
	public String getChannel(Player player);
}
