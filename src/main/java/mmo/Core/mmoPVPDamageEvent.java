package mmo.Core;

import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * 
 * 
 * @author Sebastian Mayr
 */
public interface mmoPVPDamageEvent extends Cancellable {
	
	/**
	 * Returns the original damage event.
	 * 
	 * @return the original damage event
	 */
	public EntityDamageEvent getEvent();
}
