/*
 * This file is part of mmoMinecraft (http://code.google.com/p/mmo-minecraft/).
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

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Core;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public class MMOListener extends CustomEventListener implements Listener {

	public MMOListener() {
	}

	public void onMMOChat(MMOChatEvent event) {
	}

	public void onMMOPVPDamage(MMODamageEvent evt) {
	}

	public void onMMOPVEDamage(MMODamageEvent evt) {
	}

	public void onMMOEVEDamage(MMODamageEvent evt) {
	}

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof MMOChatEvent) {
			onMMOChat((MMOChatEvent) event);
		} else if (event instanceof MMODamageEvent) {
			MMODamageEvent evt = (MMODamageEvent) event;
			switch (evt.getDamageType()) {
				case PVE:
					onMMOPVEDamage(evt);
					break;
				case PVP:
					onMMOPVPDamage(evt);
					break;
				case EVE:
					onMMOEVEDamage(evt);
					break;
			}
		}
	}
}
