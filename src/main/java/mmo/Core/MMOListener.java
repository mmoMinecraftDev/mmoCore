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
package mmo.Core;

import mmo.Core.ChatAPI.MMOChatEvent;
import mmo.Core.CoreAPI.MMOHUDEvent;
import mmo.Core.DamageAPI.MMODamageEvent;
import mmo.Core.InfoAPI.MMOInfoEvent;
import mmo.Core.SkillAPI.MMOSkillEvent;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class MMOListener extends CustomEventListener {

	public MMOListener() {
	}

	/**
	 * Called on a chat message (requires mmoChat).
	 * @param event the event
	 */
	public void onMMOChat(MMOChatEvent event) {
	}

	/**
	 * Called on Player vs Player damage (requires mmoDamage).
	 * @param event 
	 */
	public void onMMOPVPDamage(MMODamageEvent event) {
	}

	/**
	 * Called on Player vs Environment damage (requires mmoDamage).
	 * @param event the event
	 */
	public void onMMOPVEDamage(MMODamageEvent event) {
	}

	/**
	 * Called on Environment vs Environment damage (requires mmoDamage).
	 * @param event 
	 */
	public void onMMOEVEDamage(MMODamageEvent event) {
	}

	/**
	 * Called on an info bar being created (requires mmoInfo).
	 * @param event the event
	 */
	public void onMMOInfo(MMOInfoEvent event) {
	}

	/**
	 * Called on a HUD container being created.
	 * @param event the event
	 */
	public void onMMOHUD(MMOHUDEvent event) {
	}

	/**
	 * Called on a skill being used (requires mmoSkills).
	 * @param event the event
	 */
	public void onMMOSkill(MMOSkillEvent event) {
	}

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof MMOHUDEvent) {
			onMMOHUD((MMOHUDEvent) event);
		} else if (MMOMinecraft.mmoChat && event instanceof MMOChatEvent) {
			onMMOChat((MMOChatEvent) event);
		} else if (MMOMinecraft.mmoInfo && event instanceof MMOInfoEvent) {
			onMMOInfo((MMOInfoEvent) event);
		} else if (MMOMinecraft.mmoSkill && event instanceof MMOSkillEvent) {
			onMMOSkill((MMOSkillEvent) event);
		} else if (MMOMinecraft.mmoDamage && event instanceof MMODamageEvent) {
			switch (((MMODamageEvent) event).getDamageType()) {
				case PVE:
					onMMOPVEDamage(((MMODamageEvent) event));
					break;
				case PVP:
					onMMOPVPDamage(((MMODamageEvent) event));
					break;
				case EVE:
					onMMOEVEDamage(((MMODamageEvent) event));
					break;
			}
		}
	}
}
