package singleton;

import com.sun.rowset.CachedRowSetImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import object.QueryFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.sql.rowset.CachedRowSet;
import object.DVDataset;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 *
 * @author mshao1
 */
public class PostgresSQLDBManager {

    private static PostgresSQLDBManager postgresSQLDBManager = null;

    private final static String GET_ALL_STRAINTYPEIDS = "SELECT straintypeid FROM straintype";
    private final static String GET_ALL_TABLE_NAMES = "SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema='public'";
    private final static String GET_KEYS_FROM_TABLE = "SELECT constraint_name, column_name FROM information_schema.key_column_usage WHERE table_name = ?";
    private final static String GET_FRAME_RATE_BY_STRAIN_TYPE_ID = "SELECT framerate FROM videoinfo WHERE straintypeid = ?";

    static {
        postgresSQLDBManager = new PostgresSQLDBManager();
    }

    private PostgresSQLDBManager() {
    }

    public final static PostgresSQLDBManager getPostgresSQLDBManager() {
        return postgresSQLDBManager;
    }

    public static ObservableList<String> getAllTableColumnLabels(String tableName) {
        Statement stmt = null;
        ResultSet rs = null;
        ObservableList<String> resultList = FXCollections.observableArrayList();
        final String query = new QueryFactory("SELECT * FROM ? WHERE false").set(tableName, false).toString();

        try {
            stmt = ConnectionManager.getConnectionManager().getConnection().createStatement();
            rs = stmt.executeQuery(query);
            int columnCount = rs.getMetaData().getColumnCount();
            resultList.add("*");
            for (int column = 1; column <= columnCount; column++) {
                if (!ConfigurationManager.getConfigurationManager().getConfiguration().getTableKeys().contains(rs.getMetaData().getColumnName(column))) {
                    resultList.add(rs.getMetaData().getColumnName(column));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultList;
    }

    public static ArrayList<String> getAllKeysOfTable(String tableName) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> resultList = new ArrayList();

        try {
            ps = ConnectionManager.getConnectionManager().getConnection().prepareStatement(GET_KEYS_FROM_TABLE);
            ps.setString(1, tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!resultList.contains(rs.getString("column_name"))) {
                    resultList.add(rs.getString("column_name"));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultList;
    }

    public static ArrayList<String> getAllStrainTypeIDs() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> resultList = new ArrayList();

        try {
            ps = ConnectionManager.getConnectionManager().getConnection().prepareStatement(GET_ALL_STRAINTYPEIDS);
            rs = ps.executeQuery();
            while (rs.next()) {
                resultList.add(rs.getString("straintypeid"));
            }

        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultList;
    }

    public static ArrayList<String> getAllTableNames() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> resultList = new ArrayList();

        try {
            ps = ConnectionManager.getConnectionManager().getConnection().prepareStatement(GET_ALL_TABLE_NAMES);
            rs = ps.executeQuery();
            while (rs.next()) {
                resultList.add(rs.getString("table_name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultList;
    }

    public static CachedRowSet getEntriesFromTable() {
        Statement stmt = null;
        ResultSet rs = null;
        CachedRowSet rowset = null;

        try {
            final String query = ConfigurationManager.getConfigurationManager().getConfiguration().generateSQLQuery();
            stmt = ConnectionManager.getConnectionManager().getConnection().createStatement();
            rs = stmt.executeQuery(query);
            rowset = new CachedRowSetImpl();
            rowset.populate(rs);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rowset;
    }

    public static String getFPSBySTID(String strainTypeId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = null;

        try {
            ps = ConnectionManager.getConnectionManager().getConnection().prepareStatement(GET_FRAME_RATE_BY_STRAIN_TYPE_ID);
            ps.setString(1, strainTypeId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result = rs.getString("framerate");
            }
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public static DVDataset getDVEntriesFromTable() {
        Statement stmt = null;
        ResultSet rs = null;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        String title = "";
        LinkedList<Float> resultList = new LinkedList<>();

        try {
            final String query = ConfigurationManager.getConfigurationManager().getDVConfiguration().generateDVSQLQuery();
            title = ConfigurationManager.getConfigurationManager().getDVConfiguration().getDvSelectedColumn();
            stmt = ConnectionManager.getConnectionManager().getConnection().createStatement();
            rs = stmt.executeQuery(query);
            int frameCount = 0;
            while (rs.next()) {
                frameCount++;
                String strValue = rs.getString(1);
                if (strValue != null) {
                    try {
                        Float floatValue = Float.parseFloat(strValue);
                        if (floatValue > max) {
                            max = floatValue;
                        }
                        if (floatValue < min) {
                            min = floatValue;
                        }
                        resultList.add(floatValue);
                    } catch (NumberFormatException nfe) {
                        resultList.add(Float.NaN);
                    }
                } else {
                    resultList.add(Float.NaN);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new DVDataset(min, max, resultList, title);
    }

    public static void saveOutputData(String outputPath) {
        FileOutputStream fos = null;
        try {
            Connection connection = ConnectionManager.getConnectionManager().getConnection();
            CopyManager copyManager = new CopyManager((BaseConnection) connection);
            File file = new File(outputPath);
            fos = new FileOutputStream(file);

            copyManager.copyOut("COPY (" + ConfigurationManager.getConfigurationManager().getConfiguration().generateSQLQuery() + ") TO STDOUT WITH (FORMAT CSV)", fos);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
