/**
 * SQLite
 * Inherited subclass for reading and writing to and from an SQLite file.
 * 
 * Date Created: 2011-08-26 19:08
 * @author PatPeter
 *
 * Updated: 2011-10-05
 * Rewritten a load of bad stuff
 * @author Rycochet
 */
package mmo.Core.SQLibrary;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SQLite extends DatabaseHandler {

	public static final String type = "SQLite";
	public static final String driver = "org.sqlite.JDBC";
	private String file;

	public SQLite(String prefix, File file) {
		super(type, prefix);
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}
		this.file = file.getPath();
	}

	@Override
	protected boolean initialize() {
		try {
			Class.forName(driver);
			return true;
		} catch (ClassNotFoundException e) {
			this.writeError("You need the SQLite library " + e, true);
		}
		return false;
	}

	@Override
	public Connection open() {
		if (!connected && initialize()) {
			try {
				connection = DriverManager.getConnection("jdbc:sqlite:" + file);
			} catch (SQLException e) {
				this.writeError("SQLite exception on initialize " + e, true);
			}
			connected = connection == null;
		}
		return connection;
	}

	@Override
	public void close() {
		if (connected) {
			try {
				connection.close();
				connection = null;
				connected = false;
			} catch (SQLException ex) {
				this.writeError("Error on Connection close: " + ex, true);
			}
		}
	}

	@Override
	public Connection getConnection() {
		return open();
	}

	@Override
	public boolean checkConnection() {
		return open() != null;
	}

	@Override
	public Map<String,Object> query(String query, Object... vars) {
		Map<String,Object> output = new HashMap<String,Object>();
		try {
			int retry = 10; // Retry at most this number of times in case of a file lock
			int index = 1;
			PreparedStatement statement = open().prepareStatement(query);
			for (Object var : vars) {
				statement.setObject(index++, var);
			}
			while (retry-- > 0) {
				try {
					switch (this.getStatement(query)) {
						case SELECT:
//							this.writeInfo("executeQuery: " + query);
							output = ResultSetToMap(statement.executeQuery());
							break;
						default:
//							this.writeInfo("executeUpdate: " + query);
							statement.executeUpdate();
							break;
					}
					retry = 0;
					statement.close();
				} catch (SQLException e) {
					if (!e.getMessage().toLowerCase().contains("locking") && !e.getMessage().toLowerCase().contains("locked")) {
						this.writeError("Error at SQL Query: " + e.getMessage(), false);
						retry = 0;
					} else {
						this.writeInfo("Locking error at SQL Query: " + e.getMessage());
					}
				}
			}
		} catch (SQLException e) {
		}
		return output;
	}

	@Override
	public PreparedStatement prepare(String query, Object... vars) {
		PreparedStatement statement = null;
		try {
			statement = open().prepareStatement(query);
			int index = 1;
			for (Object var : vars) {
				statement.setObject(index++, var);
			}
		} catch (SQLException e) {
			if (!e.toString().contains("not return ResultSet")) {
				this.writeError("Error in SQL prepare() query: " + e.getMessage(), false);
			}
		}
		return statement;
	}

	@Override
	public boolean createTable(String name, Map<String, String> columns, String... options) {
		String sql = "CREATE TABLE `" + name + "` (";
		boolean first = true;
		for (String column : columns.keySet()) {
			sql += (first ? "" : ", ") + column + " " + columns.get(column);
			first = false;
		}
		sql += ") ";
		for (String option : options) {
			sql += option;
		}
		if (name == null || name.isEmpty() || columns.isEmpty()) {
			this.writeError("SQL Create Table query empty.", true);
		} else {
			try {
				open().createStatement().execute(sql);
				return true;
			} catch (SQLException ex) {
				this.writeError(sql, true);
				this.writeError(ex.getMessage(), true);
			}
		}
		return false;
	}

	@Override
	public boolean checkTable(String table) {
		return !query("SELECT name FROM sqlite_master WHERE type='table' AND name='"+table+"'").isEmpty();
	}

	@Override
	public boolean wipeTable(String table) {
		if (!this.checkTable(table)) {
			this.writeError("Error at Wipe Table: table, " + table + ", does not exist", true);
			return false;
		}
		query("DELETE FROM ?", table);
		return true;
	}
}
