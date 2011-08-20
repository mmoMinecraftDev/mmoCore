/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmo.Core;

import java.util.ArrayList;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.WidgetAnchor;

public class mmoPlayer {

	protected ArrayList<mmoPlayer> mmoPlayerlist = new ArrayList<mmoPlayer>();
	protected GenericContainer container;
	protected String name;

	protected void setup() {
	}

	protected void setupSpout() {
		container = new GenericContainer();
		container.setAlign(WidgetAnchor.TOP_LEFT).setAnchor(WidgetAnchor.TOP_LEFT).setWidth(427).setHeight(240).setFixed(true);
	}

	public String getName() {
		return name;
	}

	public GenericContainer getScreen() {
		return container;
	}
}
