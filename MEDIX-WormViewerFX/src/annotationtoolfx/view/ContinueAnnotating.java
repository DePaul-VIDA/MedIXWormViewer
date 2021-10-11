package annotationtoolfx.view;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ContinueAnnotating  extends AnchorPane implements Initializable, WizardNavigationItem {

	@FXML
	private RadioButton loadSetRadio;
	@FXML
	private RadioButton dontLoadRadio;
	@FXML
	private RadioButton loadOfflineRadio;
	@FXML
	private ListView<String> unfinishedAnnotations;
	@FXML 
	private TextField offlineFile;
	@FXML
	private Button chooseOfflineFile;
	
	private OpenVideoWizard wizard;
	private LoadingControlManager controlMgr;
	private Stage stage;
	private ToggleGroup group;

	@Override
	public boolean readyForward() {
            Toggle new_toggle = group.getSelectedToggle() ;
            if (group.getSelectedToggle() != null) {
        	RadioOptions ro = (RadioOptions)new_toggle.getUserData();
        	if(ro != null) {
        		if(ro == RadioOptions.load) {
        			return unfinishedAnnotations.getSelectionModel().getSelectedIndex() >= 0;
        		}
        		if(ro == RadioOptions.dontLoad) {
        			return true;
        		}
        		if(ro == RadioOptions.offline) {
        			return false;
        		}
        	}
        }
		
		return false;
	}

	@Override
	public boolean readyFinish() {
            if(readyForward())
		return true;
		
            Toggle new_toggle = group.getSelectedToggle() ;
            if (group.getSelectedToggle() != null) {
        	RadioOptions ro = (RadioOptions)new_toggle.getUserData();
        	if(ro != null) {
        		if(ro == RadioOptions.offline) {
        			File f = new File(offlineFile.getText());
        			return f.exists();
        		}
        	}
        }
        return false;
		
	}

	@Override
	public boolean canGoBack() {
		return true;
	}

	@Override
	public void setWizardControl(OpenVideoWizard ovw) {
		wizard = ovw;
		wizard.setButtonEnabled();
	}

	@Override
	public boolean next() {
		
            Toggle new_toggle = group.getSelectedToggle() ;
            if (group.getSelectedToggle() != null) {
        	RadioOptions ro = (RadioOptions)new_toggle.getUserData();
        	if(ro != null) {
                    if(ro == RadioOptions.load) {
                            controlMgr.setOfflineFile(null);
                            controlMgr.setEditAnnSet(unfinishedAnnotations.getSelectionModel().getSelectedItem());
                    }
                    else if(ro == RadioOptions.dontLoad) {
                            controlMgr.setOfflineFile(null);
                            controlMgr.setEditAnnSet(null);
                    }
                    else if(ro == RadioOptions.offline) {
                            File f = new File(offlineFile.getText());
                            controlMgr.setOfflineFile(f);
                    }

        	}		            	
            }
		
		
            return true;
	}

	@Override
	public void finish() {
		next();
		controlMgr.loadFrameMgr();
	}

	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void setControlMgr(LoadingControlManager controlMgr) {
		this.controlMgr = controlMgr;
	}
	
	@Override
	public void loadFromDB() {
		ObservableList<String> list = controlMgr.getStrainLoader().getAnnotationsByStrains(controlMgr.getStrainTypeId(), false);
		unfinishedAnnotations.setItems(list);
		if(list.size() == 0) {
			loadSetRadio.setDisable(true);
			dontLoadRadio.setSelected(true);
		}
		else {
			loadSetRadio.setSelected(true);
			unfinishedAnnotations.getSelectionModel().select(0);
		}
	}
	
	public enum RadioOptions{
		load,
		dontLoad,
		offline
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
			
		group = new ToggleGroup();
		loadSetRadio.setToggleGroup(group);
		dontLoadRadio.setToggleGroup(group);
		loadOfflineRadio.setToggleGroup(group);
		loadSetRadio.setUserData(RadioOptions.load);
		dontLoadRadio.setUserData(RadioOptions.dontLoad);
		loadOfflineRadio.setUserData(RadioOptions.offline);
		
		
		chooseOfflineFile.setOnAction((event)-> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Offline annotations");
			File f = fileChooser.showOpenDialog(stage);
			if(f != null) {
	            controlMgr.setOfflineFile(f);
	            offlineFile.setText(controlMgr.getOfflineFile().getPath());
	            wizard.setButtonEnabled();
			}
		});

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov,
		        Toggle old_toggle, Toggle new_toggle) {
		            if (group.getSelectedToggle() != null) {
		            	RadioOptions ro = (RadioOptions)new_toggle.getUserData();
		            	if(ro != null) {
                                    if(ro == RadioOptions.load) {
                                            chooseOfflineFile.setDisable(true);
                                            offlineFile.setDisable(true);
                                            chooseOfflineFile.setDisable(true);
                                            unfinishedAnnotations.setDisable(false);
                                    }
                                    else if(ro == RadioOptions.dontLoad) {
                                            chooseOfflineFile.setDisable(true);
                                            offlineFile.setDisable(true);
                                            chooseOfflineFile.setDisable(true);
                                            unfinishedAnnotations.setDisable(true);
                                    }
                                    else if(ro == RadioOptions.offline) {
                                            chooseOfflineFile.setDisable(false);
                                            offlineFile.setDisable(false);
                                            chooseOfflineFile.setDisable(false);
                                            unfinishedAnnotations.setDisable(true);
                                    }
		            	}		            	
		            }
		    		wizard.setButtonEnabled();

		    	}
		    });
	}

	@Override
	public boolean skip() {
		return false;
	}

}
