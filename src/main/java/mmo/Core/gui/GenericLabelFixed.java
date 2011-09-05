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
package mmo.Core.gui;

import mmo.Core.MMO;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Label;

public class GenericLabelFixed extends GenericLabel {

	public GenericLabelFixed() {
		super.setFixed(true);
	}

	public GenericLabelFixed(String text) {
		super(text);
		super.setHeight(MMO.getStringHeight(text));
		super.setWidth(MMO.getStringWidth(text));
		super.setFixed(true);
	}

	@Override
	public Label setText(String text) {
		super.setHeight(MMO.getStringHeight(text));
		super.setWidth(MMO.getStringWidth(text));
		return super.setText(text);
	}
}
