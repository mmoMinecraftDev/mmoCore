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
package mmo.Core.DamageAPI;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * This is a wrapper for {@link EntityDamageEvent} providing projectile firing
 * and pet detection, and separate events depending on PVP status.
 * @author Sebastian Mayr
 */
public abstract class MMODamageEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Returns the damage this event does.
	 * @return the amount of damage
	 */
	public abstract int getDamage();

	/**
	 * Sets the damage this event does.
	 * @param damage the amount of damage
	 */
	public abstract void setDamage(int damage);

	/**
	 * Returns the method of damage done.
	 * @return the type of the damage
	 */
	public abstract MMODamageType getDamageType();

	/**
	 * Returns the original damage event.
	 * @return the original damage event
	 */
	public abstract EntityDamageEvent getEvent();

	/**
	 * Gets the attacker of this damage event.
	 * @return the attacker
	 */
	public abstract Entity getAttacker();

	/**
	 * Gets the real attacker of this damage event, pet rather than owner etc.
	 * @return the attacker
	 */
	public abstract Entity getRealAttacker();

	/**
	 * Gets the defender of this damage event.
	 * @return the defender
	 */
	public abstract Entity getDefender();

	/**
	 * Gets the real defender of this damage event, pet rather than owner etc.
	 * @return the defender
	 */
	public abstract Entity getRealDefender();
}
