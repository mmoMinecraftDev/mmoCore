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

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * This is a wrapper for the original {@link EntityDamageEvent}.
 * 
 * @author Sebastian Mayr
 */
public interface MMODamageEvent extends Cancellable {
	
	/**
	 * The type of damage
	 */
	public enum DamageType {
		PVP,
		PVE,
		EVE
	}
	
	/**
	 * Returns the method of damage done.
	 * 
	 * @return the type of the damage
	 */
	public DamageType getDamageType();

	/**
	 * Returns the original damage event.
	 * 
	 * @return the original damage event
	 */
	public EntityDamageEvent getEvent();
	
	/**
	 * Gets the attacker of this damage event.
	 * 
	 * @return the attacker
	 */
	public Entity getAttacker();
	
	/**
	 * Gets the defender of this damage event.
	 * 
	 * @return the defender
	 */
	public Entity getDefender();
}
