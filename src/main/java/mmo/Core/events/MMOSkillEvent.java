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

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface MMOSkillEvent {

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
	 * Set the entity using the skill
	 * @param self 
	 */
	public void setEntity(Entity self);

	/**
	 * Get the entity using the skill
	 * @return 
	 */
	public Entity getEntity();

	/**
	 * This casts getEntity to Player if possible, otherwise returns null
	 * @return 
	 */
	public Player getPlayer();

	/**
	 * Set the target type (default is under the mouse reticule)
	 * @param type 
	 */
	public void setTargetType(TargetType type);

	/**
	 * Get the target type
	 * @return 
	 */
	public TargetType getTargetType();

	/**
	 * Set the entity target of the skill
	 * @param target 
	 */
	public void setTargetEntity(Entity target);

	/**
	 * Get the entity target of the skill
	 * @return 
	 */
	public Entity getTargetEntity();

	/**
	 * Set the block target of the skill
	 * @param target 
	 */
	public void setTargetBlock(Block target);

	/**
	 * Get the block target of the skill
	 * @return 
	 */
	public Block getTargetBlock();

	/**
	 * Set per-id data for this skill - note, only survives until the skill is finished
	 * @param id
	 * @param data 
	 */
	public void setData(String id, Object data);

	/**
	 * Get per-id data for this skill, arguments passed to the skill are in "<Skill>_<arg>"
	 * @param id
	 * @return 
	 */
	public Object getData(String id);

	/**
	 * Set a delay for this skill - after the delay this skill is called again
	 * @param seconds 
	 */
	public void setDelay(int seconds);

	/**
	 * Get the current delay for this skill
	 * @return 
	 */
	public int getDelay();

	/**
	 * Get the number of times this skill has run
	 * @return 
	 */
	public int getCount();

	/**
	 * Check if a skill is to be used by name
	 * @param skill
	 * @return 
	 */
	public boolean hasSkill(String skill);

	/**
	 * Check if the skill should make any changes - NEVER do anything except check when this is false
	 * @return 
	 */
	public boolean isRunning();

	public boolean isCancelled();

	public void setCancelled(boolean cancel);
}
