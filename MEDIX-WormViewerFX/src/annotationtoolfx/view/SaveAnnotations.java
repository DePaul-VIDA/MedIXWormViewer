package annotationtoolfx.view;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import annotationtoolfx.db.AnnotationSaver;
import annotationtoolfx.db.ConnectionSingleton;
import annotationtoolfx.object.AnnotationSet;
import annotationtoolfx.object.FrameAnnotationInfo;
import annotationtoolfx.object.FrameAnnotationManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SaveAnnotations  extends AnchorPane implements Initializable {
	
	@FXML
	private ComboBox<String> nameCombo;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	@FXML
	private TextField emailText;
	@FXML
	private PasswordField passwordText;
	@FXML
	private CheckBox finalVersionCheck;
	@FXML
	private Label invalidLabel;
	@FXML
	private Label nameExistsLabel;
	@FXML 
	private Label nameBlankLabel;
        @FXML 
        private Label enterTimeLabel;
	@FXML
	private TextField hoursText;
	@FXML
	private TextField minutesText;
	
	private FrameAnnotationManager frameMgr;
	private AnnotationSaver saver;
	
	private HashMap<String, AnnotationSet> namesSets;
	
	private boolean result = false;
	private Stage stage;
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        emailText.setText(ConnectionSingleton.getConnectionInstance().getLastLoginId());

        cancelButton.setOnAction((event)-> {
                result = false;
                stage.hide();
        });

        okButton.setOnAction((event)-> {
            nameExistsLabel.setVisible(false);
            nameBlankLabel.setVisible(false);
            enterTimeLabel.setVisible(false);
            
            String minutes = minutesText.getText();
            String hours = minutesText.getText();
            if(minutes == null || minutes.length() == 0){
                minutes = "0";
            }
            if(hours == null || hours.length() == 0){
                hours = "0";
            }
            
            double totalMinutes = 0;
            try{
               totalMinutes =  Double.parseDouble(hours)*60; 
               totalMinutes += Double.parseDouble(minutes);
            }
            catch(NumberFormatException e){
                enterTimeLabel.setVisible(true);
                return;
            }
            
            if((finalVersionCheck.isSelected() && ((int)totalMinutes) == 0)||totalMinutes < 0){
                enterTimeLabel.setVisible(true);
                return;
                
            }
            

            if(!ConnectionSingleton.getConnectionInstance().Login(emailText.getText(), passwordText.getText())) {
                    invalidLabel.setVisible(true);
            }
            else {

                try {

                    if(namesSets.containsKey(nameCombo.getSelectionModel().getSelectedItem())) {
                        AnnotationSet set = namesSets.get(nameCombo.getSelectionModel().getSelectedItem());
                        if(set != null && set.getName().length() > 0) {
                            saver.saveAnnotations(frameMgr, set, finalVersionCheck.isSelected(), totalMinutes);
                            result = true;
                        }
                        else {
                            result = save(totalMinutes);
                        }
                    }
                    else {
                            result = save(totalMinutes);
                    }
                } catch (SQLException e) {

                    new Alert(AlertType.ERROR, "Save Failed!").showAndWait();
                    e.printStackTrace();
                    result = false;
                }
            }
            if(result){
                stage.hide();
                frameMgr.setAnnotationsChanged(false);
            new Alert(AlertType.CONFIRMATION, "Save Complete!").showAndWait();
            }
        });
        
        finalVersionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                hoursText.setDisable(!newValue);
                minutesText.setDisable(!newValue);
            }
        });
    }
	

	
    private boolean save(double totalMinutes) throws SQLException {
        if(nameCombo.getSelectionModel().getSelectedItem().length() > 0) {
            if(saver.nameNotUsed(nameCombo.getSelectionModel().getSelectedItem())) {
                saver.saveAnnotations(frameMgr, this.emailText.getText(), nameCombo.getSelectionModel().getSelectedItem(), finalVersionCheck.isSelected(), totalMinutes);
                return true;
            } else {
                nameExistsLabel.setVisible(true);
                return false;
            }
        } else {
            nameBlankLabel.setVisible(true);
            return false;
        }

    }

    public void setFrameMgr(FrameAnnotationManager frameMgr) {
        this.frameMgr = frameMgr;
        loadCombos();
    }
	
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
	
    public void loadCombos() {
        saver = new AnnotationSaver();
        try {
            namesSets =  saver.getNameSets(frameMgr.getStrainTypeId());
            ArrayList<String> names = new ArrayList<String>();
            for (Map.Entry<String, AnnotationSet> entry : namesSets.entrySet()) {
                    names.add(entry.getKey());
            }
            nameCombo.setItems(FXCollections.observableArrayList(names));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public boolean getResult() {
    	return result;
    }

	
	

}
