package annotationtoolfx.view;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SelectCompAnnFrame  extends AnchorPane implements Initializable, WizardNavigationItem {

	@FXML
	private RadioButton setSelectRadio;
	@FXML
	private RadioButton dontLoadRadio;
	@FXML
	private CheckBox autoReviseCheck;
	@FXML
	private ListView<String> finishedAnnotations;

	private OpenVideoWizard wizard;
	private LoadingControlManager controlMgr;
	private ToggleGroup group;
	private boolean skip = false;

	@Override
	public boolean readyForward() {
		Toggle new_toggle = group.getSelectedToggle() ;
        if (group.getSelectedToggle() != null) {
        	RadioOptions ro = (RadioOptions)new_toggle.getUserData();
        	if(ro != null) {
        		if(ro == RadioOptions.load) {
        			return finishedAnnotations.getSelectionModel().getSelectedIndex()  >= 0;
        		}
        		else {
        			return true;
        		}
        	}
        }
		return false;
	}

	@Override
	public boolean readyFinish() {
		return readyForward();
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
                                    controlMgr.setCompareaAnnSet(finishedAnnotations.getSelectionModel().getSelectedItem());
                            }
                            else {
                                controlMgr.setCompareaAnnSet(null);
                            }
                    }
            }
            controlMgr.setRevise(autoReviseCheck.isSelected());

            return true;
	}

	@Override
	public void finish() {
		next();
		controlMgr.loadFrameMgr();
	}

	@Override
	public void setStage(Stage stage) {
	}
	
	@Override
	public void setControlMgr(LoadingControlManager controlMgr) {
		this.controlMgr = controlMgr;
	}

	@Override
	public void loadFromDB() {
		skip = false;
		ObservableList<String> list = controlMgr.getStrainLoader().getAnnotationsByStrains(controlMgr.getStrainTypeId(), true);
		finishedAnnotations.setItems(list);
		if(list.size() == 0) {
			dontLoadRadio.setSelected(true);
			skip = true;
		}
		else {
			setSelectRadio.setSelected(true);
			finishedAnnotations.getSelectionModel().select(0);
		}
			
	}

	public enum RadioOptions{
		load,
		dontLoad
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		group = new ToggleGroup();
		setSelectRadio.setToggleGroup(group);
		dontLoadRadio.setToggleGroup(group);
		setSelectRadio.setUserData(RadioOptions.load);
		dontLoadRadio.setUserData(RadioOptions.dontLoad);

                autoReviseCheck.setSelected(false);
		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov,
		        Toggle old_toggle, Toggle new_toggle) {
		            if (group.getSelectedToggle() != null) {
		            	RadioOptions ro = (RadioOptions)new_toggle.getUserData();
		            	if(ro != null) {
		            		if(ro == RadioOptions.load) {
		            			finishedAnnotations.setDisable(false);
		            		}
		            		else if(ro == RadioOptions.dontLoad) {
		            			finishedAnnotations.setDisable(true);
		            		}
		            	}		            	
		            }
		    		wizard.setButtonEnabled();
		    	}
		    });

		
	}
	
	@Override
	public boolean skip() {
		return skip;
	}


}
