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
package mmo.Core.util;

import java.util.HashMap;

/**
 * Case insensitive HashMap<String, V>.
 * Overrides the .containsKey(), .get() and .remove() methods.
 */
public class HashMapString<V> extends HashMap<String, V> {
	/**
	 * For long term storage.
	 */
	private static final long serialVersionUID = 4315151536261362924L;

	public boolean containsKey(String key) {
		for (String k : keySet()) {
			if (key.equalsIgnoreCase(k)) {
				return true;
			}
		}
		return false;
	}

	public V get(String key) {
		for (String k : keySet()) {
			if (key.equalsIgnoreCase(k)) {
				return super.get(k);
			}
		}
		return null;
	}

	public V remove(String key) {
		for (String k : keySet()) {
			if (key.equalsIgnoreCase(k)) {
				return super.remove(k);
			}
		}
		return null;
	}
}
