package annotationtoolfx.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import annotationtoolfx.object.FrameAnnotationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class OpenVideoWizard extends AnchorPane implements Initializable {

    @FXML
    private Button backButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button finishButton;
    @FXML 
    private AnchorPane controlPane;

    private LoadingControlManager controlMgr;
    private Stage stage;
    private boolean result = false;
       
    
  //  WizardNavigationItem currentControl;

    public class NodeController{
    	public Node wizardNode;
    	public WizardNavigationItem wizardController;
    	
    	public NodeController previous;
    	public NodeController next;
    	
    }
    
    private NodeController currentControl;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/annotationtoolfx/view/SelectVideoPane.fxml"));
                currentControl = new NodeController();
                currentControl.wizardNode = (Parent)loader.load();
                currentControl.wizardController = loader.getController();
                currentControl.previous = null;

                loader = new FXMLLoader(getClass().getResource("/annotationtoolfx/view/SelectDirPane.fxml"));
                NodeController dircontrol = new NodeController();
                currentControl.next = dircontrol;
                dircontrol.wizardNode = (Parent)loader.load();
                dircontrol.wizardController = loader.getController();
                dircontrol.previous = currentControl;

                loader = new FXMLLoader(getClass().getResource("/annotationtoolfx/view/ContinueAnnotating.fxml"));
                NodeController cacontrol = new NodeController();
                dircontrol.next = cacontrol;
                cacontrol.wizardNode = (Parent)loader.load();
                cacontrol.wizardController = loader.getController();
                cacontrol.previous = dircontrol;

                loader = new FXMLLoader(getClass().getResource("/annotationtoolfx/view/SelectCompAnnFrame.fxml"));
                NodeController sacontrol = new NodeController();
                cacontrol.next = sacontrol;
                sacontrol.wizardNode = (Parent)loader.load();
                sacontrol.wizardController = loader.getController();
                sacontrol.previous = cacontrol;

                loader = new FXMLLoader(getClass().getResource("/annotationtoolfx/view/ComparePredictions.fxml"));
                NodeController predcontrol = new NodeController();
                sacontrol.next = predcontrol;
                predcontrol.wizardNode = (Parent)loader.load();
                predcontrol.wizardController = loader.getController();
                predcontrol.previous = sacontrol;
                predcontrol.next = null;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		
            controlMgr = new LoadingControlManager();
            NodeController nc = currentControl;
            do {
                nc.wizardController.setWizardControl(this);
                nc.wizardController.setControlMgr(controlMgr);
                nc.wizardController.setStage(stage);
            }
            while((nc = nc.next) != null);

            controlPane.getChildren().add(currentControl.wizardNode);
            setButtonEnabled();

            currentControl.wizardController.loadFromDB();

            backButton.setOnAction((event)-> {

                    controlPane.getChildren().clear();
                    NodeController p = currentControl.previous;
                    currentControl = p;
                    controlPane.getChildren().add(currentControl.wizardNode);

                    setButtonEnabled();
            });


            nextButton.setOnAction((event)-> {
                    doNext();
            });

            cancelButton.setOnAction((event)-> {
                    result = false;
                    stage.hide();
                    Platform.exit();
            });


            finishButton.setOnAction((event)-> {
                    currentControl.wizardController.finish();
                    controlMgr.loadFrameMgr();
                    this.result = true;
                    stage.hide();

            });
		
	}
	
	public void setButtonEnabled() {
            backButton.setDisable(!currentControl.wizardController.canGoBack());
            nextButton.setDisable(!currentControl.wizardController.readyForward());
            finishButton.setDisable(!currentControl.wizardController.readyFinish());
	}
	
	public void setStage(Stage stage) {
            this.stage = stage;
            result = false;

	}
	
	public boolean getResult() {
            return result;
	}
    
	public FrameAnnotationManager getFrameMgr() {
            return controlMgr.getFrameMgr();
	}

	public void doNext() {
		
            if(currentControl.wizardController.next()) {

                controlPane.getChildren().clear();
                NodeController p = currentControl.next;
                currentControl = p;
                p.wizardController.loadFromDB();
                if(p.wizardController.skip()) {
                        doNext();
                }
                else {
                        controlPane.getChildren().add(currentControl.wizardNode);
                        setButtonEnabled();
                }
            }

	}


}
