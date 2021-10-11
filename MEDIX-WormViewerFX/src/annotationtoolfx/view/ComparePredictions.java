package annotationtoolfx.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ComparePredictions  extends AnchorPane implements Initializable, WizardNavigationItem {
    @FXML
    private TextField predFileText;
    @FXML
    private Button chooseFileButton;
    @FXML
    private RadioButton predRadio;
    @FXML
    private RadioButton noneRadio;

    private LoadingControlManager controlMgr;
    private int predColumn;
    private Stage stage;
    private ToggleGroup group;
    private OpenVideoWizard wizard;

    @Override
    public boolean readyForward() {
            return false;
    }

	@Override
    public boolean readyFinish() {
	Toggle new_toggle = group.getSelectedToggle() ;
        if (group.getSelectedToggle() != null) {
            RadioOptions ro = (RadioOptions)new_toggle.getUserData();
            if(ro != null) {
                if(ro == RadioOptions.load) {
                    if(predFileText.getText() == null || predFileText.getText().length() < 1)
                        return false;

                        if(predColumn == -1) /*if still - 1*/ {
                                return false;
                        }
                        else {
                                return true;
                        }
                }
                else {
                    return true;
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
        return false;
    }

    @Override
    public void finish() {
		
	Toggle new_toggle = group.getSelectedToggle() ;
        if (group.getSelectedToggle() != null) {
            RadioOptions ro = (RadioOptions)new_toggle.getUserData();
            if(ro != null) {
                if(ro == RadioOptions.load) {
                    if(predColumn == -1) {
                        lookupColumn();
                    }
                }
                else {
                    controlMgr.setPredictionColumn(-1);
                    controlMgr.setPredictionFile(null);
                }
            }
        }
    }
	
    private void lookupColumn() {
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(predFileText.getText());
            bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            if(line != null) {
                ArrayList<String> collist = new ArrayList<String>();

                for(String s : line.split(",")) {
                        collist.add(s.trim());
                }

                String resColSelect = "/annotationtoolfx/view/SelectPredictionColumn.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(resColSelect));
                Parent root;

                root = (Parent)loader.load();

                SelectPredictionColumn spc = loader.getController();

                Stage newWindow = new Stage();
                spc.setStage(newWindow);
                spc.setColumnList(collist);

                newWindow.setTitle("Select Prediction Column");
                newWindow.setScene(new Scene(root, 250, 295));
                newWindow.toFront();
                newWindow.showAndWait();

                predColumn = spc.getSelectedIndex();
                controlMgr.setPredictionColumn(predColumn);
                controlMgr.setPredictionFile(new File(predFileText.getText()));
            }
            else {
                new Alert(Alert.AlertType.ERROR, "Error. Prediction File Invalid!").showAndWait();
                predFileText.setText("");
            }

        }
        catch(Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error. Can't read prediction file!").showAndWait();
            predFileText.setText("");
        }
        finally {
            try {
                if(bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }		
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
    }

    public enum RadioOptions{
        load,
        dontLoad,
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        group = new ToggleGroup();
        predRadio.setToggleGroup(group);
        noneRadio.setToggleGroup(group);
        predRadio.setUserData(RadioOptions.load);
        noneRadio.setUserData(RadioOptions.dontLoad);

        predColumn = -1;
        noneRadio.setSelected(true);
        chooseFileButton.setDisable(true);
        predFileText.setDisable(true);

		
        this.chooseFileButton.setOnAction((event) ->{
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Predicted File");
            controlMgr.setPredictionFile(fileChooser.showOpenDialog(stage));
            predFileText.setText(controlMgr.getPredictionFile().getPath());

            lookupColumn();
            wizard.setButtonEnabled();

        });
		
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                Toggle old_toggle, Toggle new_toggle) {
                    if (group.getSelectedToggle() != null) {
                        RadioOptions ro = (RadioOptions)new_toggle.getUserData();
                        if(ro != null) {
                            if(ro == RadioOptions.load) {
                                chooseFileButton.setDisable(false);
                                predFileText.setDisable(false);
                            }
                            else if(ro == RadioOptions.dontLoad) {
                                chooseFileButton.setDisable(true);
                                predFileText.setDisable(true);
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
