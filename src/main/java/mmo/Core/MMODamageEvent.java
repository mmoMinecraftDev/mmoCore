package mmo.Core;

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
