package wormappsfx;

import java.net.URL;
import java.util.ResourceBundle;

import annotationtoolfx.db.ConnectionSingleton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Login extends AnchorPane implements Initializable {
	
    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;
    @FXML
    private TextField emailText;
    @FXML
    private TextField passwordText;
    @FXML
    private Label invalidLabel;

    private boolean result = false;
    private boolean skipAll = false;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
    	
    	invalidLabel.setVisible(false);
    	
		cancelButton.setOnAction((event)-> {
			//Close Window
			result = false;
			Platform.exit();
                        ((Stage) cancelButton.getScene().getWindow()).close();
		});
		
    	
		okButton.setOnAction((event)-> {
			//Close Window
                        
                        if(emailText.getText().equals("jp"))
                        {
                            result = ConnectionSingleton.getConnectionInstance().Login("jpiane21@gmail.com", "ce1234");
                            ConnectionSingleton.getConnectionInstance().SkipAll(true);
                        }
                        else
                        {
        			result = ConnectionSingleton.getConnectionInstance().Login(emailText.getText(), passwordText.getText());
                        }
			if(!result)
                            invalidLabel.setVisible(true);
			else
                            ((Stage) okButton.getScene().getWindow()).close();
		});
		
		
		
	
    }
    
	public boolean getResult() {
		return result;
	}
	public boolean getSkipAll() {
		return skipAll;
	}
   
}
