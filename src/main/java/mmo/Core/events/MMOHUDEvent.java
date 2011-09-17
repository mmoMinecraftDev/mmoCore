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

import mmo.Core.MMOPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.getspout.spoutapi.gui.WidgetAnchor;

public interface MMOHUDEvent {
	public Player getPlayer();

	public MMOPlugin getPlugin();

	public WidgetAnchor getAnchor();

	public int getOffsetX();

	public void setOffsetX(int offsetX);

	public int getOffsetY();

	public void setOffsetY(int offsetY);
}
