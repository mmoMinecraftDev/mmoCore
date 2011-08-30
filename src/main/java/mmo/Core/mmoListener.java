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

public class mmoListener extends CustomEventListener implements Listener {

	public mmoListener() {
	}

	public void onMMOChat(mmoChatEvent event) {
	}
	
	public void onMMOPVPDamage(mmoPVPDamageEvent evt) {
	}

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof mmoChatEvent) {
			onMMOChat((mmoChatEvent) event);
		} else if (event instanceof mmoPVPDamageEvent) {
			onMMOPVPDamage((mmoPVPDamageEvent) event);
		}
	}
}
