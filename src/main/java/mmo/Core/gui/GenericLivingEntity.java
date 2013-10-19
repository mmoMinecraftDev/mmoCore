/*
 * This file is part of mmoCore <http://github.com/mmoMinecraftDev/mmoCore>.
 *
 * mmoCore is free software: you can redistribute it and/or modify
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
import mmo.Core.MMOCore;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.Container;
import org.getspout.spoutapi.gui.ContainerType;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericGradient;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Gradient;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;

public class GenericLivingEntity extends GenericContainer {
	private LivingEntity target = null;
	private Container _bar;
	private Label _label;
	private Gradient _top;
	private Gradient _bottom;
	private GenericFace _face;
	private int def_width = 80;
	private int def_height = 14;
	private int def_space = 1;
	String face = "~";

	public GenericLivingEntity() {
		this(80);
	}
	
	public GenericLivingEntity(int width) {
		def_width = width;
		this.addChildren(
				_bar = (Container) new GenericContainer(	// Used for the bar, this.children with an index 1+ are targets
						new GenericGradient(new Color(0, 0, 0, 0.75f)) //
								.setWidth(def_width) //
								.setPriority(RenderPriority.Highest), //
						new GenericContainer( //
								_top = (Gradient) new GenericGradient(new Color(1f, 0, 0, 0.75f)).setWidth(def_width), //
								_bottom = (Gradient) new GenericGradient(new Color(0.75f, 0.75f, 0.75f, 0.75f)).setWidth(def_width) //
						).setMargin(1) //
								.setPriority(RenderPriority.High), //
						new GenericContainer( //
								_face = (GenericFace) new GenericFace() //
										.setVisible(MMOCore.config_show_player_faces) //
										.setMargin(3, 0, 3, 3), //
								_label = (Label) new GenericLabel("") //
										.setResize(true) //
										.setFixed(true) //
										.setMargin(3, 3, 1, 3) //
						).setLayout(ContainerType.HORIZONTAL) //
				).setLayout(ContainerType.OVERLAY) //
						.setFixed(true) //
						.setWidth(def_width) //
						.setHeight(def_height) //
						.setMargin(0, 0, 1, 0) //
		).setAlign(WidgetAnchor.TOP_LEFT) //
				.setLayout(ContainerType.VERTICAL) //
				.setFixed(true) //
				.setWidth(def_width) //
				.setHeight(def_height + def_space);
	}

	/**
	 * Clear the current entity.
	 * @return this
	 */
	public GenericLivingEntity setEntity() {
		target = null;
		setLabel("");
		setFace("");
		Widget[] widgets = this.getChildren();
		for (int i = 1; i < widgets.length; i++) {
			this.removeChild(widgets[i]);
		}
		return this;
	}

	/**
	 * Set the display from a possibly offline player
	 * @param name the target
	 * @return this
	 */
	public GenericLivingEntity setEntity(String name) {
		return setEntity(name, "");
	}

	/**
	 * Set the display from a possibly offline player.
	 * @param name   the target
	 * @param prefix a string to show before the name
	 * @return this
	 */
	public GenericLivingEntity setEntity(String name, String prefix) {
		Player player = this.getPlugin().getServer().getPlayer(name);
		if (player != null && player.isOnline()) {
			return setEntity(player, prefix);
		}
		target = null;
		setLabel((!"".equals(prefix) ? prefix : "") + MMO.getColor(screen != null ? screen.getPlayer() : null, null) + name);
		setFace("~" + name);
		return this;
	}

	/**
	 * Set the display from a player or living entity.
	 * @param entity the target
	 * @return this
	 */
	public GenericLivingEntity setEntity(LivingEntity entity) {
		return setEntity(entity, "");
	}

	/**
	 * Set the display from a player or living entity.
	 * @param entity the target
	 * @param prefix a string to show before the name
	 * @return this
	 */
	public GenericLivingEntity setEntity(LivingEntity entity, String prefix) {
		if (entity != null && entity instanceof LivingEntity) {
			target = entity;
			setLabel((!"".equals(prefix) ? prefix : "") + MMO.getColor(screen != null ? screen.getPlayer() : null, entity) + MMO.getSimpleName(entity, !(getContainer() instanceof GenericLivingEntity)));
			setFace(entity instanceof Player ? ((Player) entity).getName() : "+" + MMO.getSimpleName(entity, false).replaceAll(" ", ""));
		} else {
			setEntity();
		}
		return this;
	}

	/**
	 * Get the current entity.
	 * @return current entity
	 */
	public LivingEntity getEntity() {
		return target;
	}

	/**
	 * Set the targets of this entity - either actual targets, or pets etc.
	 * @param targets a list of targets
	 * @return this
	 */
	public GenericLivingEntity setTargets(LivingEntity... targets) {
		Widget[] widgets = this.getChildren();
		if (targets == null) {
			targets = new LivingEntity[0]; // zero-length array is easier to handle
		}
		for (int i = targets.length + 1; i < widgets.length; i++) {
			this.removeChild(widgets[i]);
		}
		for (int i = 0; i < targets.length; i++) {
			GenericLivingEntity child;
			if (widgets.length > i + 1) {
				child = (GenericLivingEntity) widgets[i + 1];
			} else {
				this.addChild(child = new GenericLivingEntity());
				child._bar.setMarginLeft(def_width / 4).setWidth(def_width - (def_width / 4));
			}
			child.setEntity(targets[i]);
		}
		setHeight((targets.length + 1) * (def_height + def_space));
		setMarginBottom(((targets.length + 1) * (def_height + def_space)) - getHeight() +  def_space);
		return this;
	}

	/**
	 * Get the current top bar (health) value as a percentage.
	 * @return current health
	 */
	public double getTopValue() {
		if (target != null) {
			return MMO.getHealth(target); // Needs a maxHealth() check
		}
		return 0;
	}

	/**
	 * Get the current top bar (armor) value as a percentage.
	 * @return current armor
	 */
	public int getBottomValue() {
		if (target != null) {
			return MMO.getArmor(target);
		}
		return 0;
	}

	/**
	 * Set the top bar (health) colour to use.
	 * @param color the solid colour for the health bar
	 * @return this
	 */
	public GenericLivingEntity setTopColor(Color color) {
		_top.setColor(color);
		return this;
	}

	/**
	 * Set the bottom bar (armor) colour to use.
	 * @param color the solid colour for the health bar
	 * @return this
	 */
	public GenericLivingEntity setBottomColor(Color color) {
		_bottom.setColor(color);
		return this;
	}

	/**
	 * Set the label to use.
	 * @param label the string to display
	 * @return this
	 */
	public GenericLivingEntity setLabel(String label) {
		if (label != null && !label.equals(_label.getText())) {
			_label.setText(label).setScale(Math.min(1f, (def_width - _face.getWidth() - 10f) / (GenericLabel.getStringWidth(label) + 1f)));
		}
		return this;
	}

	/**
	 * Set the name of the face to use beside the label.
	 * This uses GenericFace to display.
	 * @param name the name of the player or mob
	 * @return this
	 */
	public GenericLivingEntity setFace(String name) {
		if (!this.face.equals(name)) {
			this.face = name;
			_face.setName(name).setVisible(!name.isEmpty());
			deferLayout();
		}
		return this;
	}

	@Override
	public void onTick() {
		super.onTick();
		Container box = _bottom.getContainer();
		_top.setWidth((box.getWidth() * (int)getTopValue()) / 100);
		_bottom.setWidth((box.getWidth() * getBottomValue()) / 100);
	}
}
