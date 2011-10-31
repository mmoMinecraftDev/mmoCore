/**
 * HSQLDB
 * Inherited subclass for making a connection to a HSQLDB server.
 * 
 * Date Created: 2011-10-05
 * Rewritten a load of bad stuff
 * @author Rycochet
 */
package mmo.Core.SQLibrary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class HSQLDB extends DatabaseHandler {

	public static final String type = "HSQLDB";
	public static final String driver = "org.hsqldb.jdbcDriver";
	private String hostname = "localhost";
	private String port = "9001";
	private String username = "sa";
	private String password = "";
	private String database = "minecraft";

	public HSQLDB(String prefix,
			String hostname,
			String port,
			String database,
			String username,
			String password) {
		super(type, prefix);
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	@Override
	protected boolean initialize() {
		try {
			Class.forName(driver); // Check that server's Java has HSQLDB support.
			return true;
		} catch (ClassNotFoundException e) {
			this.writeError("Class Not Found Exception: " + e.getMessage() + ".", true);
		}
		return false;
	}

	@Override
	public Connection open() {
		if (!connected && initialize()) {
			String url = "";
			try {
				url = "jdbc:hsqldb:hsql://" + this.hostname + ":" + this.port + "/" + this.database;
				connection = DriverManager.getConnection(url, this.username, this.password);
			} catch (SQLException e) {
				this.writeError(url, true);
				this.writeError("Could not be resolved because of an SQL Exception: " + e.getMessage() + ".", true);
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
				connected = false;
			} catch (Exception e) {
				this.writeError("Failed to close database connection: " + e.getMessage(), true);
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
		Map<String,Object> output = null;
		PreparedStatement statement = null;
		try {
			statement = open().prepareStatement(query);
			int index = 1;
			for (Object var : vars) {
				statement.setObject(index++, var);
			}
			switch (this.getStatement(query)) {
				case SELECT:
					output = ResultSetToMap(statement.executeQuery());
					break;
				default:
					statement.executeUpdate();
					break;
			}
			statement.close();
		} catch (SQLException e) {
			this.writeError("Error in SQL query: " + e.getMessage(), false);
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
		String query = "CREATE TABLE `" + name + "` (";
		boolean first = true;
		for (String column : columns.keySet()) {
			query += (first ? "" : ", ") + column + " " + columns.get(column);
			first = false;
		}
		query += ") ";
		for (String option : options) {
			query += option;
		}
		if (name == null || name.isEmpty() || columns.isEmpty()) {
			this.writeError("SQL query empty: createTable(" + query + ")", true);
		} else {
			try {
				open().createStatement().execute(query);
				return true;
			} catch (SQLException e) {
				this.writeError(query, true);
				this.writeError(e.getMessage(), true);
			} catch (Exception e) {
				this.writeError(e.getMessage(), true);
			}
		}
		return false;
	}

	@Override
	public boolean checkTable(String table) {
		try {
			ResultSet result = open().createStatement().executeQuery("SELECT COUNT(*) FROM `" + table + "`");
			if (result != null) {
				return true;
			}
		} catch (SQLException e) {
			if (!e.getMessage().contains("exist")) {
				this.writeError("Error in SQL query: " + e.getMessage(), false);
			}
		}
		return false;
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
