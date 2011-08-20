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

import org.bukkit.entity.LivingEntity;
import org.getspout.spoutapi.gui.*;

public class GenericLivingEntity extends GenericContainer {

	private Container _bars;
	private Container _space;
	private Label _label;
	private Gradient _health;
	private Gradient _armor;
	private GenericFace _face;
	private LivingEntity entity;
	private int health = 100;
	private int armor = 100;
	private int def_width = 80;
	private int def_height = 14;
	String label = "";

	public GenericLivingEntity() {
		super();
		Color black = new Color(0, 0, 0, 0.75f);

		this.addChildren(
			_space = (Container) new GenericContainer() //				  .setFixed(true)
			.setMinWidth(def_width / 4).setMaxWidth(def_width / 4).setVisible(false),
			new GenericContainer(
			new GenericGradient().setTopColor(black).setBottomColor(black).setPriority(RenderPriority.Highest),
			_bars = (Container) new GenericContainer(
			_health = (Gradient) new GenericGradient(),
			_armor = (Gradient) new GenericGradient()).setMargin(1).setPriority(RenderPriority.High),
			new GenericContainer(
			_face = (GenericFace) new GenericFace().setMargin(3, 0, 3, 3),
			_label = (Label) new GenericLabel().setMargin(3)).setLayout(ContainerType.HORIZONTAL)).setLayout(ContainerType.OVERLAY)).setLayout(ContainerType.HORIZONTAL).setFixed(true).setAnchor(WidgetAnchor.TOP_LEFT).setMargin(1, 0, 0, 0).setWidth(def_width).setHeight(def_height);

		this.setHealthColor(new Color(1f, 0, 0, 0.75f));
		this.setArmorColor(new Color(0.75f, 0.75f, 0.75f, 0.75f));

		// Now we have the correct heights, these don't want to be re-sized horizontally
		_health.setFixed(true);
		_armor.setFixed(true);
	}

	public GenericLivingEntity setHealth(int health) {
		if (this.health != health) {
			this.health = health;
			updateLayout();
		}
		return this;
	}

	public GenericLivingEntity setHealthColor(Color color) {
		_health.setTopColor(color).setBottomColor(color);
		return this;
	}

	public GenericLivingEntity setArmor(int armor) {
		if (this.armor != armor) {
			this.armor = armor;
			updateLayout();
		}
		return this;
	}

	public GenericLivingEntity setArmorColor(Color color) {
		_armor.setTopColor(color).setBottomColor(color);
		return this;
	}

	public GenericLivingEntity setLabel(String prefix, String name) {
		if (!this.label.equals(prefix + name)) {
			this.label = prefix + name;
			_label.setText(prefix + name).setDirty(true);
			_face.setVisible(!name.equals(""));
			_face.setName(name).setDirty(true);
			updateLayout();
		}
		return this;
	}

	@Override
	public Container updateLayout() {
		_space.setVisible(armor == -1);
		super.updateLayout();
		_armor.setWidth((_bars.getWidth() * armor) / 100).setDirty(true);
		_health.setWidth((_bars.getWidth() * health) / 100).setDirty(true);
		return this;
	}
}
