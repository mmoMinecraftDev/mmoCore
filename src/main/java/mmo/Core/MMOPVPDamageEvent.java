package mmo.Core;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * 
 * 
 * @author Sebastian Mayr
 */
public interface MMOPVPDamageEvent extends Cancellable {

	/**
	 * Returns the original damage event.
	 * 
	 * @return the original damage event
	 */
	public EntityDamageEvent getEvent();
	
	/**
	 * Gets the attacker of this PVP damage event.
	 * 
	 * @return the attacker
	 */
	public Player getAttacker();
	
	/**
	 * Gets the defender of this PVP damage event.
	 * 
	 * @return the defender
	 */
	public Player getDefender();
}
