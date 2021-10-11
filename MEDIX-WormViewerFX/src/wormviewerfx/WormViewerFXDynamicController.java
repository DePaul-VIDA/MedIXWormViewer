package wormviewerfx;

import graphics.DynamicLinePlotPanel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import object.DVDataset;
import object.NoMoreFrameException;
import object.StatusTimer;
import org.jfree.chart.fx.ChartViewer;
import singleton.ConfigurationManager;
import singleton.PostgresSQLDBManager;
import utils.GraphicUtils;

/**
 * FXML Controller class
 *
 * @author MSHAO1
 */
public class WormViewerFXDynamicController implements Initializable {

    static int FPS = 30;
    static int OFFSET = 7;
    private DynamicLinePlotPanel timelinePlotPanel;
    private StatusTimer timer;
    private DVDataset dataset;
    private LinkedList<String> imagePathList;

    @FXML
    ComboBox datasetComboBox;

    @FXML
    ComboBox tableComboBox;

    @FXML
    ComboBox featureComboBox;

    @FXML
    Button playButton;

    @FXML
    Button pauseButton;

    @FXML
    Button resetButton;

    @FXML
    ImageView imageView;

    @FXML
    TextArea dataDisplayTextArea;

    @FXML
    Pane pane;
    
    @FXML
    AnchorPane dynamicDataPane;

    @FXML
    VBox timelineVBox;

    @FXML
    private void handleDatasetComboBox() {
        Object item = datasetComboBox.getSelectionModel().getSelectedItem();
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvStrainTypeId(item.toString());
    }

    @FXML
    private void handleTableComboBox() {
        Object item = tableComboBox.getSelectionModel().getSelectedItem();
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvTableName(item.toString());
        ArrayList<String> tableKeys = PostgresSQLDBManager.getAllKeysOfTable(item.toString());
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvTableKeys(tableKeys);
        GraphicUtils.populateFeatureComboBox(featureComboBox);
    }

    @FXML
    private void handleFeatureComboBox() {
        if (featureComboBox.getSelectionModel().getSelectedItem() != null) {
            ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvSelectedColumn(featureComboBox.getSelectionModel().getSelectedItem().toString());
        }
    }

    @FXML
    private void onPlayButtonClicked() {
        dataset = PostgresSQLDBManager.getDVEntriesFromTable();
        imagePathList = utils.Utils.loadImagePathByStrainTypeId(ConfigurationManager.getConfigurationManager().getDVConfiguration().getDvStrainTypeId(), dataset.getFrameOffset(), dataset.getSize());
        String fpsStr = PostgresSQLDBManager.getFPSBySTID(ConfigurationManager.getConfigurationManager().getDVConfiguration().getDvStrainTypeId());

        try {
            FPS = Integer.parseInt(fpsStr);
        } catch (NumberFormatException nfe) {
            FPS = 30;
        }

        timelinePlotPanel = new graphics.DynamicLinePlotPanel(dataset, dataDisplayTextArea, FPS);
        ChartViewer cv = new ChartViewer();
        cv.setPrefSize(510.0, 260.0);
        pane.getChildren().add(cv);
        cv.setChart(timelinePlotPanel.getChart());

        timer = new StatusTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000 / FPS) {
                    if (imagePathList != null && !imagePathList.isEmpty()) {
                        try {
                            System.out.println(imagePathList.peek());
                            Image img = new Image(imagePathList.pop(), 400.0, 300.0, true, true);
                            imageView.setImage(img);
                            timelinePlotPanel.start();                         
                        } catch (NoMoreFrameException ex) {
                            timer.stop();
                            Logger.getLogger(WormViewerFXDynamicController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    lastUpdate = now;
                }
            }
        };

        timer.start();
        pauseButton.setDisable(false);
        resetButton.setDisable(false);
        playButton.setDisable(true);
        datasetComboBox.setDisable(true);
        featureComboBox.setDisable(true);
        tableComboBox.setDisable(true);
    }

    @FXML
    private void onPauseButtonClicked() {
        if (timer.isRunning()) {
            timer.stop();
            pauseButton.setText("Continue");
        } else {
            timer.start();
            pauseButton.setText("Pause");
        }
    }

    @FXML
    private void onResetButtonClicked() {
        resetControls();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initData();
    }

    private void initData() {
        datasetComboBox.getItems().clear();
        ArrayList<String> resultList = PostgresSQLDBManager.getAllStrainTypeIDs();
        for (String s : resultList) {
            datasetComboBox.getItems().add(s);
        }
        datasetComboBox.getSelectionModel().select(0);
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvStrainTypeId(datasetComboBox.getSelectionModel().getSelectedItem().toString());

        tableComboBox.getItems().clear();
        resultList = PostgresSQLDBManager.getAllTableNames();
        for (String s : resultList) {
            tableComboBox.getItems().add(s);
        }
        tableComboBox.getSelectionModel().select(0);
//        System.out.print(ConfigurationManager.getConfigurationManager().getConfiguration().getTableName());
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvTableName(tableComboBox.getSelectionModel().getSelectedItem().toString());
        ArrayList<String> tableKeys = PostgresSQLDBManager.getAllKeysOfTable(tableComboBox.getSelectionModel().getSelectedItem().toString());
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvTableKeys(tableKeys);
        GraphicUtils.populateFeatureComboBox(featureComboBox);
        featureComboBox.getSelectionModel().select(0);
        ConfigurationManager.getConfigurationManager().getDVConfiguration().setDvSelectedColumn(featureComboBox.getSelectionModel().getSelectedItem().toString());
    }

    private void resetControls() {
        timer.stop();
        timer = null;
        timelinePlotPanel = null;
        dataset = null;
        imageView.setImage(null);

        pauseButton.setText("Pause");
        pauseButton.setDisable(true);
        resetButton.setDisable(true);
        playButton.setDisable(false);
        datasetComboBox.setDisable(false);
        featureComboBox.setDisable(false);
        tableComboBox.setDisable(false);
        dataDisplayTextArea.setText("");
    }
}
