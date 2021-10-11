
package wormviewerfx;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import object.FilePathConfiguration;
import singleton.ConfigurationManager;
import utils.DatabaseTableInserter;
import utils.MasterFileCreater;
import utils.TableCreater;
import utils.Utils;

/**
 * FXML Controller class
 *
 * @author MSHAO1
 */


public class WormViewerFXDataController implements Initializable {

    @FXML
    TextArea consoleDisplayTextArea;
    
    @FXML
    TextField filePathTextField;
    
    @FXML
    CheckBox outputHeadersCheckBox;
    
    @FXML
    Button mfPathButton;
    
    @FXML
    Button mfGenerationButton;
    
    @FXML
    Button tableGenerationButton;
    
    @FXML
    Button uploadIntoDBButton;
    
    @FXML
    private void onOutputHeadersCheckBoxClicked() {                                                      
        ConfigurationManager.getConfigurationManager().getGTConfiguration().setWithHeader(outputHeadersCheckBox.isSelected());
    }  
    
    @FXML
    private void onTableGenerationButtonClicked() {                                                      
        if (validateGTFilePath()) {
            setupFileConfiguration(ConfigurationManager.getConfigurationManager().getGTConfiguration(), filePathTextField.getText());
            TableCreater tc = new TableCreater(consoleDisplayTextArea);
            tc.createAllDBTables();
        }
    }                                                     
    
    @FXML
    private void onMfPathButtonClicked() {
        DirectoryChooser  directoryChooser  = new DirectoryChooser ();
        directoryChooser.setTitle("Choose dataset directory");
        directoryChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = directoryChooser.showDialog(new Stage());
        if (file != null) {
            System.out.println("getCurrentDirectory(): " + directoryChooser.getInitialDirectory());
            System.out.println("getSelectedFile() : " + file.getPath());
            filePathTextField.setText(file.getPath());
        }
    }                                            
    
    @FXML
    private void onMfGenerationButtonClicked() {                                                   
        if (validateMFFilePath()) {
            setupFileConfiguration(ConfigurationManager.getConfigurationManager().getMFConfiguration(), filePathTextField.getText());
            MasterFileCreater mfc = new MasterFileCreater(consoleDisplayTextArea);
            mfc.createMasterFile();
        }
    }                                                  
    
    @FXML
    private void onUploadIntoDBButtonClicked() {                                                   
        if (validateDPFilePath()) {
            setupFileConfiguration(ConfigurationManager.getConfigurationManager().getDPConfiguration(), filePathTextField.getText());
            DatabaseTableInserter dti = new DatabaseTableInserter(consoleDisplayTextArea);
            dti.insertIntoDatabase();
        }
    }  
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        outputHeadersCheckBox.setSelected(true);
    }    
    
    private boolean validateGTFilePath() {
        if (filePathTextField.getText().equals("")) {
            Utils.displaySimpleDialog(AlertType.WARNING, "Please select directory first!");
            return false;
        }
        if (!Files.exists(Paths.get(filePathTextField.getText() + "\\data"))) {
//            consoleDisplayTextArea.append("Error: no 'data' path under root!\n");
            Utils.displaySimpleDialog(AlertType.ERROR, "Error: no 'data' path under root!\n");
            return false;
        }
        return true;
    }

    private boolean validateMFFilePath() {
        if (filePathTextField.getText().equals("")) {
            Utils.displaySimpleDialog(AlertType.WARNING, "Please select directory first!");
            return false;
        }
        if (!Files.exists(Paths.get(filePathTextField.getText() + "\\data\\movementFeatures.csv"))) {
//            consoleDisplayTextArea.append("Error: no movementFeatures.csv under data folder!\n");
            Utils.displaySimpleDialog(AlertType.ERROR, "Error: no movementFeatures.csv under data folder!\n");
            return false;
        } else if (!Files.exists(Paths.get(filePathTextField.getText() + "\\matlab\\AllFeatures.csv"))) {
//            consoleDisplayTextArea.append("Error: no AllFeatures.csv under matlab folder!\n");
            Utils.displaySimpleDialog(AlertType.ERROR, "Error: no AllFeatures.csv under matlab folder!\n");
            return false;
        }
        consoleDisplayTextArea.setText("");
        return true;
    }

    private boolean validateDPFilePath() {
        if (filePathTextField.getText().equals("")) {
            Utils.displaySimpleDialog(AlertType.WARNING, "Please select directory first!");
            return false;
        }
        if (!Files.exists(Paths.get(filePathTextField.getText() + "\\dbtables"))) {
//            consoleDisplayTextArea.append("Error: no movementFeatures.csv under data folder!\n");
            Utils.displaySimpleDialog(AlertType.ERROR, "Error: no 'dbtables' folder under current folder!\n");
            return false;
        }
        
        String folderPath = filePathTextField.getText() + "\\dbtables";
        
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        HashSet<String> hSet = new HashSet<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                hSet.add(listOfFiles[i].getName().toLowerCase());
            }
        }

        if (hSet.size() < 17) {
//            consoleDisplayTextArea.append("Error: no 17 tables under current folder!\n");
            Utils.displaySimpleDialog(AlertType.ERROR, "Error: no 17 tables under current folder!\n");
            return false;
        }

        for (String s : ConfigurationManager.getConfigurationManager().getDPConfiguration().getTABLE_NAMES()) {
            if (!hSet.contains(s.toLowerCase() + ".csv")) {
//                consoleDisplayTextArea.append("Error: no " + s + " file under data folder!\n");
                Utils.displaySimpleDialog(AlertType.ERROR, "Error: no " + s + " file under current folder!\n");
                return false;
            }
        }
        consoleDisplayTextArea.setText("");
        return true;
    }

    private void setupFileConfiguration(FilePathConfiguration fc, String filePath) {
        String[] filePathComponents = filePath.split("\\\\");
        if (filePathComponents.length > 0) {
            String cName = filePathComponents[filePathComponents.length - 1];
            fc.setAllPaths(filePath, cName);
        }
    }
}
