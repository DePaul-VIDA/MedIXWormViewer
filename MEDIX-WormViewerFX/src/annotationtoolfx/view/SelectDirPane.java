package annotationtoolfx.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class SelectDirPane  extends AnchorPane implements Initializable, WizardNavigationItem {
    @FXML
    private TextField wormLocalVideoPathText;
    @FXML
    private Button chooseDirButton;
    @FXML
    private Button removeWormDirButton;
    @FXML
    private Button downloadButton;
    @FXML 
    private ProgressBar downloadProgress;
    @FXML 
    private Button cancelDownloadButton;
    @FXML
    private Label validDirLabel;
    @FXML
    private RadioButton loadExistingRadio;
    @FXML
    private RadioButton skipLoadExistingRadio;
    
    private Stage stage;
    private LoadingControlManager controlMgr;
    private OpenVideoWizard wizard;
    private WormVideoDL wormVideoDownload = null;
    private double progressMax;
    private ToggleGroup group;
    
    public enum LoadAnnNextOptions{
        loadAnn,
        skip
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validDirLabel.setVisible(false);

        chooseDirButton.setOnAction((event)-> {
            selectDir();
        });


        removeWormDirButton.setOnAction((event)-> {
            controlMgr.setWormImageDirectory(null);
            wormLocalVideoPathText.setText("");
        });

        downloadButton.setOnAction((event)-> {

            File newDirectory = controlMgr.getWormImageDirectory();
            if (newDirectory == null) {
                newDirectory = getFileFromString();
            }

            if(newDirectory == null || !newDirectory.exists()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Download directory does not exist!  Please create a new directory.");
                alert.showAndWait();
                selectDir();

            }
            else {
                setDownloadingControls(true);
                ArrayList<String> files = controlMgr.getStrainLoader().getFileNames(controlMgr.getStrainTypeId());
                progressMax = files.size();

                wormVideoDownload = new WormVideoDL(controlMgr.getStrainTypeId(), newDirectory, files, this);
                wormVideoDownload.start();
            }

        });

        this.cancelDownloadButton.setOnAction((event) -> {
            wormVideoDownload.cancelDownload();
            wormVideoDownload = null;
            setDownloadingControls(false);
        });

        wormLocalVideoPathText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                String oldValue, String newValue) {
                    wizard.setButtonEnabled();
            }
        });

        setDownloadingControls(false);
        downloadButton.setDisable(true);
        
        group = new ToggleGroup();
        loadExistingRadio.setToggleGroup(group);
        skipLoadExistingRadio.setToggleGroup(group);
        loadExistingRadio.setUserData(LoadAnnNextOptions.loadAnn);
        skipLoadExistingRadio.setUserData(LoadAnnNextOptions.skip);
        skipLoadExistingRadio.setSelected(true);
        
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
            Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    wizard.setButtonEnabled();
                }
            }
        });
    }

    private File getFileFromString() {
	String newDirectoryText = this.wormLocalVideoPathText.getText(); 
		
  	if(newDirectoryText.lastIndexOf(File.separator) < newDirectoryText.length() -1) {
            newDirectoryText = newDirectoryText + File.separator;
	} 
  		
  	File f =  new File(newDirectoryText);
  	if(f.exists())
            return f;
  	else
            return null;
    }

    public void setDownloadingControls(boolean value) {
	downloadProgress.setVisible(value);
	cancelDownloadButton.setVisible(value);
	downloadButton.setDisable(value);
	wormLocalVideoPathText.setDisable(value);
	chooseDirButton.setDisable(value);
	removeWormDirButton.setDisable(value);

    }
    
    private void selectDir(){
        downloadButton.setDisable(true);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Worm image directory");
        controlMgr.setWormImageDirectory(directoryChooser.showDialog(stage));
        if(controlMgr.getWormImageDirectory() != null){
            wormLocalVideoPathText.setText(controlMgr.getWormImageDirectory().getPath());
            downloadButton.setDisable(false);
        }        
    }
	
    public void updateProgress(double count) {
	downloadProgress.setProgress(count/progressMax);
    }
	
    public void downloadComplete() {
	setDownloadingControls(false);
	wormVideoDownload = null;
    }
	
    class DownloadComplete implements Runnable {
    	SelectDirPane dirpane;
    	
        public DownloadComplete(SelectDirPane pane) {
            dirpane = pane;
	}

	@Override public void run(){
            dirpane.downloadComplete();
        }
    }
    
    class UpdateProgress implements Runnable {
    	SelectDirPane dirpane;
    	int newCount;
    	
        public UpdateProgress(SelectDirPane pane, int count) {
            dirpane = pane;
            newCount = count;
	}

	@Override public void run(){
            dirpane.updateProgress(newCount);
        }
    }

    class WormVideoDL extends Thread {
    	AtomicInteger run;
    	String wormVideo;
    	ArrayList<String> files;
    	File newDirectory;
    	SelectDirPane pane;
    	
    	public WormVideoDL(String strainTypeId, File newDirectory, ArrayList<String> files, SelectDirPane pane) {
            run = new AtomicInteger();
            run.set(1);
            wormVideo = strainTypeId;
            this.files = files;
            this.newDirectory = newDirectory;
            this.pane = pane;
    	}
    	
    	public void cancelDownload() {
            run.set(0);
    	}
       	    	
        @Override public void run(){
            String partialUrl = annotationtoolfx.object.Constants.WormUrl + wormVideo + "/";
            int count = 0;

            String surl, toFile;
            for(String fileName : files) {

                try {
                    surl = partialUrl + fileName;
                    toFile = newDirectory + File.separator + fileName;

                    URL website;
                    website = new URL(surl);
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    FileOutputStream fos = new FileOutputStream(toFile);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                    rbc.close();
                    count++;
                    Platform.runLater(new UpdateProgress(pane, count));


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            Platform.runLater(new DownloadComplete(pane));

      		
        }
        
        private void doDowload(ArrayList downloadList){
            
        }

        private void tryWith7() {
        }
    }
	

    @Override
    public boolean readyForward() {
        if(isDirValid()){
            Toggle new_toggle = group.getSelectedToggle() ;
            if (group.getSelectedToggle() != null) {
        	LoadAnnNextOptions ro = (LoadAnnNextOptions)new_toggle.getUserData();
        	if(ro != null) {
                    if(ro == LoadAnnNextOptions.loadAnn)
                        return true;
                    else
                        return false;
        	}
            }

            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean isDirValid(){
        File dir = controlMgr.getWormImageDirectory();
        if(dir != null && dir.exists()) {
            validDirLabel.setVisible(false);
            return true;
        }

        String dirName = this.wormLocalVideoPathText.getText();
        if(dirName != null && dirName.length() > 0) {
            File f = getFileFromString();
            if(f != null)	{
                controlMgr.setWormImageDirectory(f);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean readyFinish() {
        if(isDirValid()){
            Toggle new_toggle = group.getSelectedToggle() ;
            if (group.getSelectedToggle() != null) {
        	LoadAnnNextOptions ro = (LoadAnnNextOptions)new_toggle.getUserData();
        	if(ro != null) {
                    if(ro == LoadAnnNextOptions.loadAnn)
                        return false;
                    else
                        return true;
        	}
            }

            return false;
        }
        else{
            return false;
        }
            
        
    }

    @Override
    public boolean canGoBack() {
        return true;
    }

    @Override
    public void setWizardControl(OpenVideoWizard ovw) {
        wizard = ovw;
    }


    @Override
    public boolean next() {
        if(readyForward()) {
            return true;
        }
        validDirLabel.setVisible(true);
        return false;
    }

    @Override
    public void finish() {
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
    }

    @Override
    public boolean skip() {
        return false;
    }

}
