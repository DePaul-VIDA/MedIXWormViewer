package utils;

import singleton.ConfigurationManager;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 *
 * @author MSHAO1
 */
import singleton.ConnectionManager;

public class DatabaseTableInserter {

    private final TextArea consoleDisplayTextArea;
    private final String[] tableNames = ConfigurationManager.getConfigurationManager().getDPConfiguration().getTABLE_NAMES();
    private String[] fileNames = new String[tableNames.length];
    private final String extension = ".csv";

    public DatabaseTableInserter(TextArea consoleDisplayTextArea) {
        this.consoleDisplayTextArea = consoleDisplayTextArea;
        for (int i = 0; i < tableNames.length; i++) {
            fileNames[i] = ConfigurationManager.getConfigurationManager().getDPConfiguration().getDirectoryPath() + "\\dbtables\\" + tableNames[i] + extension;
            tableNames[i] = tableNames[i].toLowerCase();
        }
    }

    public void insertIntoDatabase() {
        FileReader fr = null;
        try {
            for (int i = 0; i < fileNames.length; i++) {
                fr = new FileReader(fileNames[i]);
                CopyManager copyManager = new CopyManager(ConnectionManager.getConnectionManager().getConnection().unwrap(BaseConnection.class));
                copyManager.copyIn("COPY " + tableNames[i] + " FROM STDIN WITH CSV HEADER DELIMITER AS ','", fr);
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + fileNames[i] + "\n");
                fr.close();
            }
        } catch (FileNotFoundException fnfex) {
            Logger.getLogger(DatabaseTableInserter.class.getName()).log(Level.SEVERE, null, fnfex);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseTableInserter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DatabaseTableInserter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception ex) {
                    Logger.getLogger(DatabaseTableInserter.class.getName()).log(Level.SEVERE, null, ex);
                }
                fr = null;
            }
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "\n\nFinished!!\n");
            Utils.displaySimpleDialog(Alert.AlertType.INFORMATION, "Finished uploading into database!\n");
        }
    }
}
