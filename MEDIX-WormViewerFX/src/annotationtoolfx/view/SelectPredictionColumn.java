package annotationtoolfx.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SelectPredictionColumn  extends AnchorPane implements Initializable {

	@FXML
	private ListView<String> columnList;
    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;
    
    private Stage stage;
	private String selectedColumn = null;
	private ArrayList<String> list;
	private int selectedIndex = -1;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		okButton.setOnAction((event)-> {
			selectedColumn = columnList.getSelectionModel().getSelectedItem();
			this.selectedIndex = list.indexOf(selectedColumn);
			stage.hide();

		});
		
		cancelButton.setOnAction((event)-> {
			//Close Window
			this.stage.hide();
		});
	}
	

	public void setStage(Stage stage) {
		// TODO Auto-generated method stub
		this.stage = stage;
	}
    

	public void setColumnList(ArrayList<String> list) {
		columnList.setItems(FXCollections.observableList(list));
		this.list = list;
	}


	public int getSelectedIndex() {
		return selectedIndex;
	}
	
}
