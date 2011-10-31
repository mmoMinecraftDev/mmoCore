/**
 * Database Handler
 * Abstract superclass for all subclass database files.
 * 
 * Date Created: 2011-08-26 19:08
 * @author PatPeter
 *
 * Updated: 2011-10-05
 * Rewritten a load of bad stuff - and reformatted the javadocs!
 * @author Rycochet
 */
package mmo.Core.SQLibrary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseHandler {

	protected static final Logger log = Logger.getLogger("Minecraft");
	protected final String prefixDatabase;
	protected final String prefix;
	protected boolean connected = false;
	protected Connection connection = null;

	protected enum Statements {

		SELECT, INSERT, UPDATE, DELETE, DO, REPLACE, LOAD, HANDLER, CALL, // Data manipulation statements
		CREATE, ALTER, DROP, TRUNCATE, RENAME  // Data definition statements
	}

	/**
	 * Create a new DatabaseHandler.
	 * @param database the name of the database type, ie "MySQL" or "SQLite"
	 * @param prefix a prefix for any error messages
	 */
	public DatabaseHandler(String database, String prefix) {
		this.prefixDatabase = database;
		if (prefix != null && !prefix.trim().isEmpty()) {
			this.prefix = " " + prefix.trim();
		} else {
			this.prefix = "";
		}
	}

	/**
	 * Escape any SQL characters for security.
	 * This isn't as secure as using a prepared statement!
	 * @param string the text to escape
	 * @return an escaped string
	 */
	public String escape(String string) {
		return string.replaceAll("([\\\\\"'_%])", "\\$1");
	}

	/**
	 * Writes information to the console.
	 * @param toWrite - the text to write to the console
	 */
	protected void writeInfo(String toWrite) {
		if (toWrite != null && !toWrite.isEmpty()) {
			log.info(String.format("[%s]%s: %s", prefixDatabase, prefix, toWrite));
		}
	}

	/**
	 * Writes either errors or warnings to the console.
	 * @param toWrite the text to write to the console
	 * @param severe whether console output should appear as an error or warning
	 */
	protected void writeError(String toWrite, boolean severe) {
		if (toWrite != null && !toWrite.isEmpty()) {
			if (severe) {
				log.severe(String.format("[%s]%s: %s", prefixDatabase, prefix, toWrite));
			} else {
				log.warning(String.format("[%s]%s: %s", prefixDatabase, prefix, toWrite));
			}
		}
	}

	/**
	 * Determines the name of the statement and converts it into an enum.
	 */
	protected Statements getStatement(String query) {
		try {
			query = query.trim();
			int endIndex = query.indexOf(" ");
			if (endIndex == -1) {
				endIndex = query.length();
			}
			return Statements.valueOf(query.substring(0, endIndex).toUpperCase());
		} catch (IllegalArgumentException e) {
			writeError("Error: " + e.getMessage(), false);
			return Statements.SELECT;
		}
	}

	/**
	 * This converts the next (first) row from a ResultSet into a simple HashMap.
	 * You can call this repeatedly on the same ResultSet to parse every row.
	 * @param result the ResultSet to convert
	 * @return a simple Map of the first row
	 */
	protected Map<String,Object> ResultSetToMap(ResultSet result) {
		Map<String,Object> output = new HashMap<String,Object>();
		try {
			if (result != null && result.next()) {
				ResultSetMetaData mtd = result.getMetaData();
				for (int i = mtd.getColumnCount(); i>0 ;i--) {
					output.put(mtd.getColumnLabel(i), result.getObject(i));
					writeInfo(mtd.getColumnLabel(i) + " -> " + result.getObject(i));
				}
			}
		} catch (SQLException e) {
			writeError("ResultSetToMap Error: " + e.getMessage(), false);
		}
		return output;
	}

	/**
	 * Used to check whether the class for the SQL engine is installed.
	 */
	abstract protected boolean initialize();

	/**
	 * Opens a connection with the database.
	 * @return the success of the method
	 */
	abstract public Connection open();

	/**
	 * Closes a connection with the database.
	 */
	abstract public void close();

	/**
	 * Gets the connection variable.
	 * @return the <a href="http://download.oracle.com/javase/6/docs/api/java/sql/Connection.html">Connection</a> variable.
	 */
	abstract public Connection getConnection();

	/**
	 * Checks the connection between Java and the database engine.
	 * @return the status of the connection
	 */
	abstract public boolean checkConnection();

	/**
	 * Sends a query to the SQL database.
	 * This uses a prepared statement to ensure the safety of the arguments.
	 * @param query the SQL query to send to the database
	 * @param vars a list of variables that replace "?" in the query in a secure manner
	 * @return a single row result from the query
	 */
	abstract public Map<String,Object> query(String query, Object... vars);

	/**
	 * Creates a prepared query for the database.
	 * @param query the SQL query to prepare to send to the database
	 * @param vars a list of variables that replace "?" in the query in a secure manner
	 * @return the prepared statement
	 */
	abstract public PreparedStatement prepare(String query, Object... vars);

	/**
	 * Creates a table in the database based on a specified query.
	 * @param name the name of the table to create
	 * @param columns a Map of name to definitions
	 * @return if it was created successfully
	 */
	abstract public boolean createTable(String name, Map<String, String> columns, String... options);

	/**
	 * Checks if a table in a database exists.
	 * @param table name of the table to check
	 * @return if it exists
	 */
	abstract public boolean checkTable(String table);

	/**
	 * Wipes a table given its name.
	 * @param table name of the table to wipe
	 * @return success of the method
	 */
	abstract public boolean wipeTable(String table);
}
