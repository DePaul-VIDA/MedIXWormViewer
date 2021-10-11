/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wormappsfx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jpiane
 */
public class ChooseAppController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    @FXML
    private RadioButton wormViewerRadio;
    @FXML
    private RadioButton annToolRadio;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
   
    private ToggleGroup group;
    private ChooseAppOptions result;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        group = new ToggleGroup();
        wormViewerRadio.setToggleGroup(group);
        annToolRadio.setToggleGroup(group);
        annToolRadio.setUserData(ChooseAppOptions.Annotations);
        wormViewerRadio.setUserData(ChooseAppOptions.Viewer);
        wormViewerRadio.setSelected(true);
        
        okButton.setOnAction((event)-> {
            Toggle new_toggle = group.getSelectedToggle() ;
            if (group.getSelectedToggle() != null) {
        	ChooseAppOptions ro = (ChooseAppOptions)new_toggle.getUserData();
        	if(ro != null) {
                    result = ro;
        	}
            }
            ((Stage) okButton.getScene().getWindow()).close();
        });
        
        cancelButton.setOnAction((event)-> {
            result = ChooseAppOptions.Cancel;
           ((Stage) cancelButton.getScene().getWindow()).close();
        });
        
    }    
    
    public ChooseAppOptions getResult(){
        return result;
    }
    
}
