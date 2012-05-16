/*
 * This file is part of mmoCore <http://github.com/mmoMinecraftDev/mmoCore>.
 *
 * mmoCore is free software: you can redistribute it and/or modify
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

public enum MMODamageType {

	/**
	 * Player vs Player damage, owned pets are classed as their owner.
	 */
	PVP,
	/**
	 * Player vs Environment damage, owned pets are classed as their owner.
	 */
	PVE,
	/**
	 * Environment vs Environment damage.
	 */
	EVE
}
