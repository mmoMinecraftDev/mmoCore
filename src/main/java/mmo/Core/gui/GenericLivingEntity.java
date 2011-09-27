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
import mmo.Core.MMOCore;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.*;

public class GenericLivingEntity extends GenericContainer {

	private Container _bar;
	private Label _label;
	private Gradient _health;
	private Gradient _armor;
	private GenericFace _face;
	private int health = 100;
	private int armor = 100;
	private int def_width = 80;
	private int def_height = 14;
	private int old_health_width = -1;
	private int old_armor_width = -1;
	String face = "~";
	String label = "";

	public GenericLivingEntity() {
		super();
		Color color = new Color(0, 0, 0, 0.75f);

		this.addChildren( 
			_bar = (Container) new GenericContainer(	// Used for the bar, this.children with an index 1+ are targets
				new GenericGradient()
						.setTopColor(color)
						.setBottomColor(color)
						.setPriority(RenderPriority.Highest),
				new GenericContainer(
					_health = (Gradient) new GenericGradient(),
					_armor = (Gradient) new GenericGradient()
				)		.setMargin(1)
						.setPriority(RenderPriority.High),
				new GenericContainer(
					_face = (GenericFace) new GenericFace()
							.setVisible(MMOCore.config_show_player_faces)
							.setMargin(3, 0, 3, 3),
					_label = (Label) new GenericLabel()
							.setResize(true)
							.setFixed(true)
							.setMargin(3, 3, 1, 3)
				)		.setLayout(ContainerType.HORIZONTAL)
			)		.setLayout(ContainerType.OVERLAY)
					.setMaxHeight(def_height)
					.setMargin(0, 0, 1, 0)
		)		.setAlign(WidgetAnchor.TOP_LEFT)
				.setMinWidth(def_width)
//				.setMaxWidth(def_width * 2)
				.setMaxHeight(def_height + 1);

		color = new Color(1f, 0, 0, 0.75f);
		_health.setTopColor(color).setBottomColor(color);
		color = new Color(0.75f, 0.75f, 0.75f, 0.75f);
		_armor.setTopColor(color).setBottomColor(color);
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
	 * @param name the target
	 * @param prefix a string to show before the name
	 * @return this
	 */
	public GenericLivingEntity setEntity(String name, String prefix) {
		Player player = this.getPlugin().getServer().getPlayer(name);
		if (player != null && player.isOnline()) {
			return setEntity(player, prefix);
		}
		setHealth(0);
		setArmor(0);
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
			setHealth(MMO.getHealth(entity)); // Needs a maxHealth() check
			setArmor(MMO.getArmor(entity));
			setLabel((!"".equals(prefix) ? prefix : "") + MMO.getColor(screen != null ? screen.getPlayer() : null, entity) + MMO.getSimpleName(entity, !(getContainer() instanceof GenericLivingEntity)));
			setFace(entity instanceof Player ? ((Player)entity).getName() : "+" + MMO.getSimpleName(entity,false).replaceAll(" ", ""));
		} else {
			setHealth(0);
			setArmor(0);
			setLabel("");
			setFace("");
		}
		return this;
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
		for (int i=targets.length + 1; i<widgets.length; i++) {
			this.removeChild(widgets[i]);
		}
		for (int i=0; i<targets.length; i++) {
			GenericLivingEntity child;
			if (widgets.length > i + 1) {
				child = (GenericLivingEntity) widgets[i+1];
			} else {
				this.addChild(child = new GenericLivingEntity());
				child._bar.setMarginLeft(def_width / 4);
			}
			child.setEntity(targets[i]);
		}
		setMaxHeight((targets.length + 1) * (def_height + 1));
		if (getContainer() instanceof Container) {
			getContainer().updateLayout();
		} else {
			updateLayout();
		}
		return this;
	}
	
	/**
	 * Set the health to display.
	 * @param health percentage
	 * @return this
	 */
	public GenericLivingEntity setHealth(int health) {
		if (this.health != health) {
			this.health = health;
			updateLayout();
		}
		return this;
	}

	/**
	 * Set the health colour to use.
	 * @param color the solid colour for the health bar
	 * @return this
	 */
	public GenericLivingEntity setHealthColor(Color color) {
		_health.setTopColor(color).setBottomColor(color);
		return this;
	}

	/**
	 * Set the armor to display.
	 * @param armor percentage
	 * @return this
	 */
	public GenericLivingEntity setArmor(int armor) {
		if (this.armor != armor) {
			this.armor = armor;
			updateLayout();
		}
		return this;
	}

	/**
	 * Set the armor colour to use.
	 * @param color the solid colour for the health bar
	 * @return this
	 */
	public GenericLivingEntity setArmorColor(Color color) {
		_armor.setTopColor(color).setBottomColor(color);
		return this;
	}

	/**
	 * Set the label to use.
	 * @param label the string to display
	 * @return this
	 */
	public GenericLivingEntity setLabel(String label) {
		if (!this.label.equals(label)) {
			this.label = label;
			_label.setText(label).setDirty(true);
			updateLayout();
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
			updateLayout();
		}
		return this;
	}

	@Override
	public Container updateLayout() {
		_armor.setWidth(old_armor_width); // cache for bandwidth
		_health.setWidth(old_health_width); // cache for bandwidth
		super.updateLayout();
		old_armor_width = _armor.getWidth(); // cache for bandwidth
		old_health_width = _health.getWidth(); // cache for bandwidth
		_armor.setWidth((_armor.getContainer().getWidth() * armor) / 100).setDirty(true);
		_health.setWidth((_health.getContainer().getWidth() * health) / 100).setDirty(true);
		return this;
	}
}
