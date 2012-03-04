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
package mmo.Core.util;

import mmo.Core.MMO;
import org.bukkit.entity.Player;

/**
 *
 * @author Xaymar
 */
public class util {

	public static String substitude(String On, String[] What, String[] With) {
		if (What.length != With.length) {
			throw new java.lang.ArrayIndexOutOfBoundsException();
		}

		for (int count = 0; count < What.length; count++) {
			if (What[count].contains(",")) {
				String[] WhatArgs = What[count].split(",");
				for (String arg : WhatArgs) {
					On = On.replace(arg, With[count]);
				}
			} else {
				On = On.replace(What[count], With[count]);
			}
		}

		return On;
	}

	/**
	 * Spout handles this internally
	 * @param On
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public static String colorize(String On) {
		return On.replaceAll("([^&])(&([a-fA-F0-9]))", "$1\u00A7$3").replace("&&", "&");
	}

	public static String parsePlayer(String On, Player plr) {
		return colorize(util.substitude(On,
			new String[]{
				"+n,+name",
				"+d,+displayname",
				"+w,+world",
				"+t,+time",
				"+l,+location",
				"+x",
				"+y",
				"+z",}, new String[]{
				plr.getName(),
				plr.getDisplayName(),
				plr.getWorld().getName(),
				String.valueOf(plr.getWorld().getTime()),
				String.valueOf(plr.getLocation().getX()) + "x, " + String.valueOf(plr.getLocation().getY()) + "y, " + String.valueOf(plr.getLocation().getZ()) + "z",
				String.valueOf(plr.getLocation().getX()),
				String.valueOf(plr.getLocation().getY()),
				String.valueOf(plr.getLocation().getZ())
			}));
	}

	public static String[] reparseArgs(String[] args) {
		return MMO.smartSplit(arrayCombine(args, " "));
	}

	public static String[] arraySplit(String split, String delimiter) {
		return split.split(delimiter);
	}

	public static String arrayCombine(String[] array, String delimiter) {
		String out = new String();
		if (array.length >= 1) {
			out += array[0];
			for (int i = 1; i < array.length; i++) {
				out += delimiter;
				out += array[i];
			}
		}
		return out;
	}

	public static Object resizeArray(Object oldArray, int start, int length) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		start = Math.min(Math.max(start, 0), oldSize - 1);
		length = Math.max(Math.min(length, oldSize - start), 0);

		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, length);

		System.arraycopy(oldArray, start, newArray, 0, length);
		return newArray;
	}
}
