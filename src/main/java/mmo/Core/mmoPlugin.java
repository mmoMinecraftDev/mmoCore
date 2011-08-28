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

import com.avaje.ebean.EbeanServer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public abstract class mmoPlugin extends JavaPlugin {

	static protected MyDatabase database;
	static protected List<Class<?>> classes = new ArrayList<Class<?>>();

	@Override
	public EbeanServer getDatabase() {
		if (database == null) {
			database = new MyDatabase(this) {

				@Override
				protected java.util.List<Class<?>> getDatabaseClasses() {
					return classes;
				}
			};

			Configuration cfg = mmoCore.mmo.cfg;
			database.initializeDatabase(
					  cfg.getString("database.driver", "org.sqlite.JDBC"),
					  cfg.getString("database.url", "jdbc:sqlite:{DIR}{NAME}.db"),
					  cfg.getString("database.username", "root"),
					  cfg.getString("database.password", ""),
					  cfg.getString("database.isolation", "SERIALIZABLE"),
					  cfg.getBoolean("database.logging", false),
					  cfg.getBoolean("database.rebuild", true));
			cfg.setProperty("database.rebuild", false);
			cfg.save();
		}
		return database.getDatabase();
	}
}
