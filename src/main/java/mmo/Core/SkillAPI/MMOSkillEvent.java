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
package mmo.Core.SkillAPI;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public interface MMOSkillEvent extends Cancellable {

	public enum TargetType {

		/**
		 * Target is an entity
		 */
		ENTITY,
		/**
		 * Target is a block
		 */
		BLOCK
	}

	/**
	 * Set the entity using the skill.
	 * @param self 
	 */
	public void setEntity(Entity self);

	/**
	 * Get the entity using the skill.
	 * @return the entity
	 */
	public Entity getEntity();

	/**
	 * This casts getEntity to Player if possible, otherwise returns null.
	 * @return the player
	 */
	public Player getPlayer();

	/**
	 * Set the target type (default is under the mouse reticule).
	 * @param type the type of target
	 */
	public void setTargetType(TargetType type);

	/**
	 * Get the target type.
	 * @return the type of target
	 */
	public TargetType getTargetType();

	/**
	 * Set the entity target of the skill.
	 * @param target the target
	 */
	public void setTargetEntity(Entity target);

	/**
	 * Get the entity target of the skill.
	 * @return the target
	 */
	public Entity getTargetEntity();

	/**
	 * Set the block target of the skill.
	 * @param target the target block
	 */
	public void setTargetBlock(Block target);

	/**
	 * Get the block target of the skill.
	 * @return the target block
	 */
	public Block getTargetBlock();

	/**
	 * Set per-id data for this skill - note, only survives until the skill is finished.
	 * @param id the id to set
	 * @param data the data to be saved
	 */
	public void setData(String id, Object data);

	/**
	 * Get per-id data for this skill, arguments passed to the skill are in "<Skill>_<arg>".
	 * @param id the id to get
	 * @return a previously set object
	 */
	public Object getData(String id);

	/**
	 * Set a delay for this skill - after the delay this skill is called again.
	 * @param seconds between calls
	 */
	public void setDelay(int seconds);

	/**
	 * Get the current delay for this skill.
	 * @return the delay in seconds.
	 */
	public int getDelay();

	/**
	 * Get the number of times this skill has run. First run is 0.
	 * @return the number of times
	 */
	public int getCount();

	/**
	 * Check if a skill is to be used by name.
	 * @param skill the name of the skill
	 * @return if this skill is used
	 */
	public boolean hasSkill(String skill);

	/**
	 * Check if the skill should make any changes - NEVER do anything except check when this is false.
	 * @return if this is not just testing
	 */
	public boolean isRunning();
}
