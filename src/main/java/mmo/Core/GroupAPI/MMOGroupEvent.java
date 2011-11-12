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
package mmo.Core.GroupAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public interface MMOGroupEvent extends Cancellable {

	/**
	 * Get the player who is affected by this event.
	 * @return the player
	 */
	public Player getPlayer();

	/**
	 * Get the player who caused the event (ie, party leader).
	 * @return the player
	 */
	public Player getBlamer();

	/**
	 * Get the group that is being affected.
	 * @return the group
	 */
	public Group getGroup();

	/**
	 * Get the type of event that has happened.
	 * @return the group event type
	 */
	public MMOGroupAction getAction();
}
