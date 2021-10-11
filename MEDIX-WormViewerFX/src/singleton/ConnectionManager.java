package singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mshao1
 */
public class ConnectionManager {

	/*Withheld*/
    private static ConnectionManager connectionManager = null;
    private Connection connection = null;

    static {
        connectionManager = new ConnectionManager();
    }

    private ConnectionManager() {
        if (connection == null) {
            try {
	/*Withheld*/
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public final static ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public final Connection getConnection() {
        return connection;
    }
}
