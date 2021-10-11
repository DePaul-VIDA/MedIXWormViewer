package annotationtoolfx.view;

import javafx.stage.Stage;

public interface WizardNavigationItem {

	public boolean readyForward();
	public boolean readyFinish();
	public boolean canGoBack();
	
	public void setWizardControl(OpenVideoWizard ovw);
	
	public boolean next();
	public boolean skip();
	
	public void finish();
	public void loadFromDB();
	
	public void setStage(Stage stage);
	public void setControlMgr(LoadingControlManager controlMgr);
}
