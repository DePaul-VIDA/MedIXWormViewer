package annotationtoolfx.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;


public class ConnectionSingleton {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	/*Withheld*/
    static ConnectionSingleton instance = null;
    
    private Connection conn = null;    
    private String lastLoginId = "";
    private boolean skipAll = false;
    
    public boolean Login(String email, String password) {
		try {

		/*Withheld*/
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
    }

    protected ConnectionSingleton(Connection c) {
    	conn = c;
    }
    
    public boolean GetSkipAll()
    {
        return skipAll;
    }
    
    public void SkipAll(boolean value)
    {
        skipAll = value;
    }
    
    public static ConnectionSingleton getConnectionInstance() {
    	
    	if(instance != null) {
    		try {
		/*Withheld*/
    		}
    		catch (SQLException e){
    			instance = null;
    		}
    	}

    	if(instance == null) {
    		try {
    			Class.forName("org.postgresql.Driver");
    			Properties props = new Properties();
			/*Withheld*/
				instance = new ConnectionSingleton(DriverManager.getConnection(DB_URL, props));
				
    		}
    		catch(Exception e) {
    			//Should never happen in production
    			System.out.print(e.getMessage());
    		}
    	}
    	
    	return instance;
    }
    
    public Connection getConnection() {
    	return conn;
    }
    
    public void cleanup() {
    	try {
			conn.close();
		} catch (SQLException e) {
			// should not happen
			e.printStackTrace();
		}
    	instance = null;
    }

	public String getLastLoginId() {
		return lastLoginId;
	}
    
}
