package annotationtoolfx.view;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

public class SelectVideoPane implements Initializable, WizardNavigationItem {
	@FXML
    private TreeTableView<WormVideoDisplay> wormTableView;
    @FXML 
    private TreeTableColumn<WormVideoDisplay, String> wormTypeColumn;
    @FXML 
    private TreeTableColumn<WormVideoDisplay, String> strainIdColumn;
    @FXML 
    private TreeTableColumn<WormVideoDisplay, String> foodCondColumn;
    @FXML 
    private TreeTableColumn<WormVideoDisplay, String> lengthColumn;
    @FXML 
    private TreeTableColumn<WormVideoDisplay, String> numAnnColumn;
    
    private OpenVideoWizard wizard;
    private LoadingControlManager controlMgr;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
		

    }
    
    WormVideoDisplay getSelectedRow(){
    	if(wormTableView.getSelectionModel().getSelectedItem() == null)
    		return null;
    	return wormTableView.getSelectionModel().getSelectedItem().getValue();
    }

	@Override
	public boolean readyForward() {
            return getSelectedRow() != null;
	}

	@Override
	public boolean readyFinish() {
            return false;
	}

	@Override
	public boolean canGoBack() {
            return false;
	}
    
	public void setWizardControl(OpenVideoWizard ovw) {
		wizard = ovw;

	}

	@Override
	public boolean next() {
            WormVideoDisplay wvd =  getSelectedRow();
            if(wvd != null) {
                    controlMgr.setStrainTypeId(wvd.getStrainTypeId());

                    return true;
            }
            return false;
	}

	@Override
	public void finish() {
		//Can't finish from here
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
            controlMgr.getStrainLoader().loadWormTypes();
            HashMap<String, TreeItem<WormVideoDisplay>> map = controlMgr.getStrainLoader().getWormTypes();

            TreeItem<WormVideoDisplay> root = new TreeItem<WormVideoDisplay>(new WormVideoDisplay("", "", "", "", ""));
            for(String wormType : controlMgr.getStrainLoader().getWormTypeNames()) {
                    map.get(wormType).setExpanded(true);
                    root.getChildren().add(map.get(wormType));
    	}
    	wormTableView.setRoot(root);
    	root.setExpanded(true);
    	
    	wormTypeColumn.setCellValueFactory(
    	            (TreeTableColumn.CellDataFeatures<WormVideoDisplay, String> param) -> 
    	            new ReadOnlyStringWrapper(param.getValue().getValue().getWormType())
    	        );

    	strainIdColumn.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<WormVideoDisplay, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getStrainTypeId())
	        );

    	foodCondColumn.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<WormVideoDisplay, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getFoodCond())
	        );

    	lengthColumn.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<WormVideoDisplay, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getLength())
	        );

    	numAnnColumn.setCellValueFactory(
	            (TreeTableColumn.CellDataFeatures<WormVideoDisplay, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getNumAnn())
	        );
    	
    	wormTableView.setRowFactory( tv -> {
    	    TreeTableRow<WormVideoDisplay> row = new TreeTableRow<>();
    	    row.setOnMouseClicked(event -> {
	        	if(wormTableView.getSelectionModel().getSelectedItem().getValue().getStrainTypeId() != null)
	        		wizard.setButtonEnabled();
    	    	if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
    	    		wizard.doNext();
    	        }
    	    });
    	    return row ;
    	 });
		
	}

	@Override
	public boolean skip() {
            return false;
	}
}
