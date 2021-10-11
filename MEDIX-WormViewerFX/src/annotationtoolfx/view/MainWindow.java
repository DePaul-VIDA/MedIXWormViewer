package annotationtoolfx.view;

import annotationtoolfx.db.ConnectionSingleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import annotationtoolfx.object.FrameAnnotationInfo;
import annotationtoolfx.object.FrameAnnotationManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import singleton.ConnectionManager;

public class MainWindow extends AnchorPane implements Initializable {
	
    @FXML
    private Button startButton;
    @FXML
    private ComboBox<String> selectedAnnotationCombo;
    @FXML
    private Button updateButton;
    @FXML
    private TextField beginAnnText;
    @FXML
    private TextField endAnnText;
    @FXML
    private Button setToCurrentButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ImageView wormImageView;
    @FXML
    private TextField frameNoText;
    @FXML
    private TextField delayText;
    @FXML
    private Button goButton;
    @FXML
    private Button forward10Button;
    @FXML
    private Button forwardButton;
    @FXML
    private Button playButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button backButton;
    @FXML
    private Button back10Button;
    @FXML
    private Button plusButton;
    @FXML
    private Button minusButton;
    @FXML
    private Label frameNoValueLabel;
    @FXML
    private Label elapsedTimeValueLabel;
    @FXML
    private Label humanAnnValueLabel;
    @FXML
    private Label predictedAnnValueLabel;
    @FXML
    private Label updatedAnnValueLabel;
    @FXML
    private Label frameNoLabel;
    @FXML
    private Label elapsedTimeLabel;
    @FXML
    private Label expertAnnotationLabel;
    @FXML
    private Label predictedAnnotationLabel;
    @FXML
    private Label updatedAnnotationLabel;
    @FXML
    private Label frameDoesntExistLabel;
    @FXML
    private Label fpsValLabel;
	@FXML
	private CheckBox pauseOnDiffCheck;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button exportButton;
    @FXML
    private TableView<FrameAnnotationInfoDisplay> annotationsTableView;
    @FXML 
    private TableColumn<FrameAnnotationInfoDisplay, String> frameNoCol;
    @FXML 
    private TableColumn<FrameAnnotationInfoDisplay, String> expertAnnCol;
    @FXML 
    private TableColumn<FrameAnnotationInfoDisplay, String> predictedAnnCol;
    @FXML 
    private TableColumn<FrameAnnotationInfoDisplay, String> updatedAnnCol;
    @FXML
    private AnchorPane annotatePane;
    @FXML
    private AnchorPane videoPane;
    
    private Stage stage;
    
    private FrameAnnotationManager frameMgr;
    int currentIndex;
    VideoPlayback playback;
    HashMap<FrameAnnotationInfo, FrameAnnotationInfoDisplay> faiDisplayByfai;
    ArrayList<FrameAnnotationInfoDisplay> faidList;
    ObservableList<FrameAnnotationInfoDisplay> faidDisplayList;
    //Annotation
    private int beginningFrame;
    private int endFrame;
    private boolean inprogress = false;
    private boolean wizardCanceled = false;

    private Background annModeValue;
    private Background watchModeValue;
   

    public static class FrameAnnotationInfoDisplay implements UpdateObserver {

            private final SimpleStringProperty frameNumber;
            private final SimpleStringProperty expertAnn;
            private final SimpleStringProperty predictedAnn;
            private final SimpleStringProperty updatedAnn;

            private FrameAnnotationInfo fai;

            public String getFrameNumber() {
                    return frameNumber.get();
            }

            public void setFrameNumber(String value) {
                    frameNumber.set(value);
            }

            public String getExpertAnn() {
                    return expertAnn.get();
            }

            public void setExpertAnn(String value) {
                    expertAnn.set(value);
            }

            public String getPredictedAnn() {
                    return predictedAnn.get();
            }

            public void setPredictedAnn(String value) {
                    predictedAnn.set(value);
            }

            public String getUpdatedAnn() {
                    return updatedAnn.get();
            }

            public void setUpdatedAnn(String value) {
                    updatedAnn.set(value);
            }

            public FrameAnnotationInfoDisplay(FrameAnnotationInfo fai) {
                    this.fai = fai;
                    frameNumber = new SimpleStringProperty(fai.getFrameNoAsString());
                    expertAnn = new SimpleStringProperty(fai.getHumanAnnotation());
                    predictedAnn = new SimpleStringProperty(fai.getPredictedAnnotation());
                    updatedAnn = new SimpleStringProperty(fai.getUpdatedAnnotation());

                    update();
                    this.fai.Register(this);
            }

            @Override
            public void update() {
                    frameNumber.set(fai.getFrameNoAsString());
                    expertAnn.set(fai.getHumanAnnotation());
                    predictedAnn.set(fai.getPredictedAnnotation());
                    String s = fai.getUpdatedAnnotation();
                    updatedAnn.set(s);

            }

            // this method will be used by the PropertyValueFactory
            // and returns a Property which notifies TableView of changes
            public StringProperty frameNoProperty() {
                return frameNumber;
            }

            public StringProperty updatedAnnProperty() {
                return updatedAnn;
            }	

    }

    class VideoPlayback extends Thread {
            MainWindow videoPanel;
    AtomicInteger run;
    private int delay;

    public VideoPlayback(MainWindow panel, int timeDelay) {
            videoPanel = panel;
            delay = timeDelay;
            run = new AtomicInteger();
            run.set(1);
    }

    public void stopPlayback() {
            run.set(0);
    }

    @Override public void run(){
      try {
              Platform.runLater(new GoToFrame(videoPanel));
          while (run.get() == 1)
          {
              Platform.runLater(new MoveForward(videoPanel));
              Thread.sleep((int)delay);
          }

      }
      catch (InterruptedException ex) {
                    ex.printStackTrace();
            }
    }
  }

    class MoveForward implements Runnable {
    	MainWindow videoPanel;
    	
        public MoveForward(MainWindow panel) {
        	videoPanel = panel;
		}

		@Override public void run(){
        	videoPanel.moveForward();
          }
    }
    
    class GoToFrame implements Runnable {

    	MainWindow videoPanel;
    	
        public GoToFrame(MainWindow panel) {
            videoPanel = panel;
        }

        @Override public void run(){
            videoPanel.goToFrame();
        }

    }
    
    public class SpeedChangeHandler implements EventHandler<Event>{
    	
		
    	MainWindow videoWindow;
		int dir;
		
		 SpeedChangeHandler(MainWindow controller, int direction) {
			 videoWindow = controller;
			 dir = direction;
		}
    		
    	
        @Override
        public void handle(Event event) {
			boolean wasPlaying = videoWindow.isPlaying();
			videoWindow.pause();
			int delay = videoWindow.getDelay() +  dir;
            if (delay > 300)
                delay = 300;
            if (delay < 25)
                delay = 25;
            videoWindow.setDelay(delay);

            if(wasPlaying)
				videoWindow.play();
        }

    }

	class NavigationHandler implements EventHandler<Event>{
		int difference;
		MainWindow videoWindow;
							
		NavigationHandler(MainWindow window, int frameDiff){
			difference = frameDiff;
			videoWindow = window;
		}
		
        @Override
        public void handle(Event event) {
			
			int index = videoWindow.currentIndex + difference;
			if(index > videoWindow.getFrameMgr().getCount() -1)
				index = videoWindow.getFrameMgr().getCount() -1;
			else if(index < 0)
				index = 0;
			videoWindow.currentIndex = index;
			videoWindow.loadCurrentImage();
			videoWindow.frameNoText.setText(Integer.toString(index));
		}
	}
	  
        
   
    @Override
	public void initialize(URL location, ResourceBundle resources) {

    	ArrayList<String> annList = new ArrayList<String>();
    	annList.add("Forward-NTD");
    	annList.add("Forward-Sharp");
    	annList.add("Forward-Shallow");
    	annList.add("Stop");
    	annList.add("ReverseLong");
    	annList.add("ReverseShort");
    	selectedAnnotationCombo.setItems( FXCollections.observableList(annList));
    	
    	annModeValue = new Background(new BackgroundFill(Color.valueOf("#f0e0d0"), CornerRadii.EMPTY, Insets.EMPTY));
    	watchModeValue = new Background(new BackgroundFill(Color.valueOf("#f0f0f0"), CornerRadii.EMPTY, Insets.EMPTY));
    	
    	startButton.setOnAction((event)-> {
    		setBegToCurrentAnn();
    		endAnnText.setText(Integer.toString(beginningFrame));
    		setAnnDisabled(false);
    		inprogress = true;
			this.annotatePane.setBackground(annModeValue);
    	});

    	updateButton.setOnAction((event)-> {
    		String annotation = this.selectedAnnotationCombo.getSelectionModel().getSelectedItem();
    		beginningFrame = Integer.parseInt(beginAnnText.getText());
    		endFrame = Integer.parseInt(endAnnText.getText());
    		
    		if(beginningFrame > endFrame) {
    			int temp = beginningFrame;
    			beginningFrame = endFrame;
    			endFrame = temp;
    		}
    		if(annotation != null && annotation.length() > 0) {
    			for(int i = beginningFrame; i<=endFrame; i++)
    				frameMgr.get(i).setUpdatedAnnotation(annotation);
    			frameMgr.setAnnotationsChanged(true);
    		}
    		inprogress = false;
    		setAnnDisabled(true);
			this.annotatePane.setBackground(watchModeValue);
		
    	});

    	cancelButton.setOnAction((event)-> {
    		setAnnDisabled(true);
    		setStartDisabled(false);
    		beginningFrame = 0;
    		endFrame = 0;
    		beginAnnText.setText("0");
    		endAnnText.setText("0");
    		
    	});
    	
    	saveButton.setOnAction((event)->{
    		save();
    	});

    	exportButton.setOnAction((event)->{
    		export();
    	});
    	
    	setToCurrentButton.setOnAction((event)->{
    		setBegToCurrentAnn();
    	});

    	prevButton.setOnAction((event)-> {
    		 frameNoText.setText(Integer.toString(frameMgr.findNonMatchBefore(currentIndex)));
			  goToFrame();
		});

    	nextButton.setOnAction((event)-> {
   		 frameNoText.setText(Integer.toString(frameMgr.findNonMatchAfter(currentIndex)));
   		 frameNoText.setText(Integer.toString(frameMgr.findNeedsReview(currentIndex)));
   		 frameNoText.setText(Integer.toString(frameMgr.findSharpTurn(currentIndex)));
			  goToFrame();
		});
    	
    	delayText.setOnAction((event) -> {
    		calcFPS();
    	});
    	
    	goButton.setOnAction((event)-> {
   			  goToFrame();
		});
    
    	minusButton.addEventHandler(ActionEvent.ACTION, new SpeedChangeHandler(this, 1));
    	plusButton.addEventHandler(ActionEvent.ACTION, new SpeedChangeHandler(this, -1));

    	back10Button.addEventHandler(ActionEvent.ACTION, new NavigationHandler(this, -10));
    	backButton.addEventHandler(ActionEvent.ACTION, new NavigationHandler(this, -1));
    	forward10Button.addEventHandler(ActionEvent.ACTION, new NavigationHandler(this, 10));
    	forwardButton.addEventHandler(ActionEvent.ACTION, new NavigationHandler(this, 1));
    	
    	playButton.setOnAction((event)-> {
   			  play();
   		});
       	
    	pauseButton.setOnAction((event)-> {
 			  pause();
 		});

		currentIndex = 0;
    	
		delayText.setText("40");
		setAnnDisabled(true);
    	try {
            
            if(ConnectionSingleton.getConnectionInstance().GetSkipAll())
            { 
                LoadingControlManager mgr = new LoadingControlManager();
                mgr.setRevise(true);

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_LR_nf_4"));
                mgr.setStrainTypeId("N2_LR_nf_4");
                mgr.setCompareaAnnSet("cleanup-jp2");
                
                mgr.setCompareaAnnSet("lr 4 revision1 xufan");
              //  mgr.setCompareaAnnSet("jp_cleanup2_nextrev");
             //   mgr.setCompareaAnnSet("jp_cleanup2_nextrev2");

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_LR_nf_4"));
                mgr.setStrainTypeId("N2_LR_nf_4");
                mgr.setCompareaAnnSet("N2_nf4_12-8-18");

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_nf25"));
                mgr.setStrainTypeId("N2_LR_nf_25");
                mgr.setCompareaAnnSet("jp_n2_nf_LR_25");

                mgr.setCompareaAnnSet("jp_n2_nf_LR_25_nextRev");
                mgr.setCompareaAnnSet("jp_n2_nf_LR_25_nextRev2");

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_nf25"));
                mgr.setStrainTypeId("N2_LR_nf_25");
                mgr.setCompareaAnnSet("N2_nf25_final");

                
                mgr.setCompareaAnnSet("25 revision 1 xufan");
                
                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_nf5"));
                mgr.setStrainTypeId("N2_LR_nf_5");
                mgr.setCompareaAnnSet("jp_ann_n2_nf5");
 
                mgr.setCompareaAnnSet("jp_ann_n2_nf5_ar1");
                mgr.setCompareaAnnSet("jp_ann_n2_nf5_ar2");
                
                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_nf5"));
                mgr.setStrainTypeId("N2_LR_nf_5");
                mgr.setCompareaAnnSet("N2_nf5_final");
                
 
                
                mgr.setCompareaAnnSet("nf5 revision 1 xufan");
 /*               mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_nf20"));
                mgr.setStrainTypeId("N2_LR_nf_20");
                mgr.setCompareaAnnSet("n2_n20_low");

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_HR_nf_10"));
                mgr.setStrainTypeId("N2_HR_nf_10");
                mgr.setCompareaAnnSet("N2_nfHr_10_ann");

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_HR_nf_16"));
                mgr.setStrainTypeId("N2_HR_nf_16");
                mgr.setCompareaAnnSet("jpn2nf16HR");
                mgr.setCompareaAnnSet("n2_hr_nf_16");

                
                
                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_HR_nf_16"));
                mgr.setStrainTypeId("N2_HR_nf_16");
                mgr.setCompareaAnnSet("TPCN2_nf16_annotated");
                
                
                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_HR_nf_20"));
                mgr.setStrainTypeId("N2_HR_nf_20");
                mgr.setCompareaAnnSet("N2_nf20_HR_ann");

                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_HR_nf_8"));
                mgr.setStrainTypeId("N2_HR_nf_8");
                mgr.setCompareaAnnSet("TPC_N2_NF8_annotated");
                
                mgr.setWormImageDirectory(new File("C:\\Users\\jenny\\Documents\\C. elegans\\Videos\\N2_HR_nf_8"));
                mgr.setStrainTypeId("N2_HR_nf_8");
              //  mgr.setCompareaAnnSet("N2_HR_NF_8_Ian_Annotation");
          //      mgr.setCompareaAnnSet("jpn2_nf_HR_8");

                */
                mgr.setPredictionColumn(-1);
                mgr.loadFrameMgr();
                frameMgr = mgr.getFrameMgr();
            }
            else{
    		String resOpen = "/annotationtoolfx/view/OpenVideoWizard.fxml";
    		FXMLLoader loader = new FXMLLoader(getClass().getResource(resOpen));
    		Parent root;
	
                    root = (Parent)loader.load();

                    OpenVideoWizard ov = loader.getController();

                    Stage newWindow = new Stage();
                    ov.setStage(newWindow);

                    newWindow.setTitle("Select Worm Video");
                    newWindow.setScene(new Scene(root, 600, 350));
                    newWindow.toFront();
                    newWindow.showAndWait();

                    if(!ov.getResult())
                    {
                            wizardCanceled = true;
                            Platform.exit();
                            if(stage != null)
                                    stage.close();
                            return;
                    }
                    frameMgr = ov.getFrameMgr();
            }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	faidList = new ArrayList<FrameAnnotationInfoDisplay>();
    	Iterator<FrameAnnotationInfo> it = frameMgr.iterator();
    	faiDisplayByfai = new HashMap<FrameAnnotationInfo, FrameAnnotationInfoDisplay>();
    	while(it.hasNext()) {
    		FrameAnnotationInfo fai = it.next();
    		FrameAnnotationInfoDisplay faid = new FrameAnnotationInfoDisplay(fai);
    		faidList.add(faid);
    		faiDisplayByfai.put(fai, faid);
    	}
    	
    	faidDisplayList = FXCollections.observableArrayList(faidList);
    	frameNoCol.setCellValueFactory(new PropertyValueFactory<FrameAnnotationInfoDisplay, String>("frameNo"));
    	expertAnnCol.setCellValueFactory(new PropertyValueFactory<FrameAnnotationInfoDisplay, String>("expertAnn"));
    	predictedAnnCol.setCellValueFactory(new PropertyValueFactory<FrameAnnotationInfoDisplay, String>("predictedAnn"));
    	updatedAnnCol.setCellValueFactory(new PropertyValueFactory<FrameAnnotationInfoDisplay, String>("updatedAnn"));
    	
    	annotationsTableView.setItems(faidDisplayList);
    	
    	annotationsTableView.setRowFactory( tv -> {
    	    TableRow<FrameAnnotationInfoDisplay> row = new TableRow<>();
    	    row.setOnMouseClicked(event -> {
    	        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
    	        	FrameAnnotationInfoDisplay rowData = row.getItem();
    	        	frameNoText.setText(rowData.frameNumber.get());
    	        	goToFrame();
    	        }
    	    });
    	    return row ;
    	 });
    	moveForward();
    	
		
	
	}
    
    private void save() {
        String res = "/annotationtoolfx/view/SaveAnnotations.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(res));
        Parent root;
        try {

            root = (Parent)loader.load();

            SaveAnnotations sa = loader.getController();

            Stage newWindow = new Stage();
            sa.setStage(newWindow);
            sa.setFrameMgr(frameMgr);
            sa.loadCombos();

            newWindow.setTitle("Save Annotations");
            newWindow.setScene(new Scene(root, 500, 280));
            newWindow.toFront();
            newWindow.showAndWait();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
				
    }

	private void setBegToCurrentAnn() {
		beginningFrame = currentIndex;
		this.beginAnnText.setText(Integer.toString(beginningFrame));
    }
    
    private void setStartDisabled(boolean value) {
    	startButton.setDisable(value);
    }
    
    private void setAnnDisabled(boolean value) {
    	updateButton.setDisable(value);
    	cancelButton.setDisable(value);
    	setToCurrentButton.setDisable(value);
    	this.selectedAnnotationCombo.setDisable(value);
    	if(value) {
    		
    		//annotatePane.setBackground(Background.);().
    		
    	}
    }
    
    private void loadCurrentImage()
    {
        
        frameDoesntExistLabel.setVisible(false);

        FrameAnnotationInfo fai = frameMgr.get(currentIndex);

        while(fai == null & currentIndex < frameMgr.getCount()) {

            currentIndex++;
            fai = frameMgr.get(currentIndex);
        }

        if(fai == null)
        {
            frameDoesntExistLabel.setVisible(true);
            pause();
            return;
        }

        
            File f = fai.getImageFile();
            if(!f.exists())
            {
                frameDoesntExistLabel.setVisible(true);
                pause();
                return;
            }

            if (pauseOnDiffCheck.isSelected())
            {
                if (frameMgr.get(currentIndex).hasBothAnnotations() && !frameMgr.get(currentIndex).doAnnotationsMatch())
                {
                    if(playback != null)
                            if (playback.run.get() == 1)
                                    pause();
                }
            }

            wormImageView.setImage(new Image(f.toURI().toString()));

            this.frameNoText.setText(fai.getFrameNoAsString());
            this.frameNoValueLabel.setText(fai.getFrameNoAsString());
            this.elapsedTimeValueLabel.setText(fai.getElapsedTime());
            this.humanAnnValueLabel.setText(fai.getHumanAnnotation());
            this.predictedAnnValueLabel.setText(fai.getPredictedAnnotation());
            this.updatedAnnValueLabel.setText(fai.getUpdatedAnnotation());

            annotationsTableView.scrollTo(faiDisplayByfai.get(frameMgr.get(currentIndex)));

            if(inprogress) {
                endFrame = currentIndex;
                endAnnText.setText(Integer.toString(endFrame));
            }

    }

    public FrameAnnotationManager getFrameMgr() {
            return frameMgr;
    }

    void moveForward() {
        currentIndex++;
        frameNoText.setText(Integer.toString(currentIndex));
        loadCurrentImage();
    }
    
    void play() {
    	playback = new VideoPlayback(this, getDelay());
    	playback.start();
    	pauseButton.setVisible(true);
    	playButton.setVisible(false);
    }
    
    void pause() {
    	if(playback != null) {
	    	playback.stopPlayback();
	    	playback = null;
	    	playButton.setVisible(true);
	    	pauseButton.setVisible(false);
    	}
    }

	public void setDelay(int delay) {
		String del = Integer.toString(delay);
        delayText.setText(del);
        calcFPS();
	}

	public int getDelay() {
		try {
		return Integer.parseInt(delayText.getText());
		}catch(Exception e) {
			return 40;
		}
	}

	public boolean isPlaying() {
		return playback != null;
	}

	private void calcFPS() {
		try {
			String del = delayText.getText();
			int delay = Integer.parseInt(del);
			fpsValLabel.setText(Integer.toString(Math.round(1000/delay)));
		}
		catch(NumberFormatException e) {
			//Don't change
		}
	}

	private void goToFrame() {
    	try {
            int frameNo = Integer.parseInt(frameNoText.getText());

            if (frameMgr.isFrameInRange(frameNo))
            {
                currentIndex = frameNo;
                loadCurrentImage();
            }
    	}
    	catch(NumberFormatException e) {
    		
    	}		
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
		
		if(wizardCanceled)
			stage.close();
		
		stage.widthProperty().addListener((obs, oldVal, newVal) -> {
			setImageSize();
		});
		stage.widthProperty().addListener((obs, oldVal, newVal) -> {
			setImageSize();
		});
	}
    
	private void setImageSize() {
		wormImageView.fitWidthProperty().bind(videoPane.widthProperty().subtract(240));
		wormImageView.fitHeightProperty().bind(videoPane.heightProperty().subtract(185));
	}

	public void checkSave() {
		if(frameMgr != null)
			if(frameMgr.getAnnotationsChanged())
				save();
	}
	
	private void export() {
		FileChooser fileChooser = new FileChooser();
	     
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File file = fileChooser.showSaveDialog(stage);
        
        if(file != null){
     		
        	FileWriter fileWriter;
        	try {
        		fileWriter = new FileWriter(file);

 	            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
 	            
 	            bufferedWriter.write("FrameNo, Expert Annotation, Predicted Annotation, Updated Annotation\n");
 		        for (FrameAnnotationInfo fai : frameMgr.getFrameList())
 		        {
 		        	bufferedWriter.write(Integer.toString(fai.getFrameNo()));
	        		bufferedWriter.write(", ");
 		        	if(fai.getHumanAnnotation() != null) {
 		        		bufferedWriter.write(fai.getHumanAnnotation());
 		        	}
	        		bufferedWriter.write(", ");
 		        	if(fai.getPredictedAnnotation() != null) {
 		        		bufferedWriter.write(fai.getPredictedAnnotation());
 		        	} 	
	        		bufferedWriter.write(", ");
 		        	if(fai.getUpdatedAnnotation() != null) {
 		        		bufferedWriter.write(fai.getUpdatedAnnotation());
 		        	} 	
 		        	bufferedWriter.write("\n");
 		        }
 		        bufferedWriter.flush();
 		        bufferedWriter.close();
	    		new Alert(Alert.AlertType.INFORMATION, "Annotations exported").showAndWait();

 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}		
     			

         }
	}
	
}
