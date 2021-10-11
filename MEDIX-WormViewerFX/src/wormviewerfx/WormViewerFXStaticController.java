package wormviewerfx;

import object.FiveNumberSummary;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.sql.rowset.CachedRowSet;
import singleton.ConfigurationManager;
import singleton.PostgresSQLDBManager;
import utils.GraphicUtils;
import utils.StatisticsUtils;
import utils.Utils;
import static utils.Utils.convertStarinTypeIdToDatasetName;

/**
 * FXML Controller class
 *
 * @author MSHAO1
 */
public class WormViewerFXStaticController implements Initializable {

    @FXML
    AnchorPane mainAnchorPane;

    @FXML
    ComboBox datasetComboBox;

    @FXML
    ComboBox tableComboBox;

    @FXML
    Button downloadMasterFileButton;

    @FXML
    Button downloadDatasetButton;

    @FXML
    Button viewFeaturesButton;

    @FXML
    AnchorPane summaryAnchorPane;

    @FXML
    ListView featureSelectorList;

    @FXML
    private void handleDatasetComboBox() {
        Object item = datasetComboBox.getSelectionModel().getSelectedItem();
        ConfigurationManager.getConfigurationManager().getConfiguration().setStrainTypeId(item.toString());
    }

    @FXML
    private void handleTableComboBox() {
        Object item = tableComboBox.getSelectionModel().getSelectedItem();
        ConfigurationManager.getConfigurationManager().getConfiguration().setTableName(item.toString());
        ArrayList<String> tableKeys = PostgresSQLDBManager.getAllKeysOfTable(item.toString());
        ConfigurationManager.getConfigurationManager().getConfiguration().setTableKeys(tableKeys);
        GraphicUtils.populateFeatureList(featureSelectorList);
    }

    @FXML
    private void onViewFeaturesButtonClicked() {
        System.out.println(ConfigurationManager.getConfigurationManager().getConfiguration().getStrainTypeId());
        System.out.println(ConfigurationManager.getConfigurationManager().getConfiguration().getTableName());
        System.out.println(ConfigurationManager.getConfigurationManager().getConfiguration().getSelectedColumns());
        CachedRowSet crs = PostgresSQLDBManager.getEntriesFromTable();
        if (crs != null) {
            mainAnchorPane.getChildren().clear();
            TableView mainTableView = buildMainDisplayTable(crs);
            AnchorPane.setTopAnchor(mainTableView, 0.0);
            AnchorPane.setLeftAnchor(mainTableView, 0.0);
            AnchorPane.setRightAnchor(mainTableView, 0.0);
            AnchorPane.setBottomAnchor(mainTableView, 0.0);
            mainAnchorPane.getChildren().addAll(mainTableView);

            HashMap<String, ArrayList<Double>> resultMap = prepareDataForFiveNumberSummary(crs);
            System.out.println(resultMap.size());
            ArrayList<FiveNumberSummary> fnsList = StatisticsUtils.getAllFiveNumberSummaries(resultMap);

            summaryAnchorPane.getChildren().clear();
            TableView summaryTableView = buildSummaryDisplayTable(fnsList);
            AnchorPane.setTopAnchor(summaryTableView, 0.0);
            AnchorPane.setLeftAnchor(summaryTableView, 0.0);
            AnchorPane.setRightAnchor(summaryTableView, 0.0);
            AnchorPane.setBottomAnchor(summaryTableView, 0.0);
            summaryAnchorPane.getChildren().addAll(summaryTableView);
        }
    }

    @FXML
    private void onDownloadDatasetButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("CSV file", "*.csv"));
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            String filename = file.getPath();
            if (!filename.endsWith(".csv")) {
                filename += ".csv";
            }
            PostgresSQLDBManager.saveOutputData(filename);
            System.out.println("getCurrentDirectory(): " + fileChooser.getInitialDirectory());
            System.out.println("getSelectedFile(): " + filename);
        }
    }

    @FXML
    private void onDownloadMasterFileButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("CSV file", "*.csv"));
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            String filename = file.getPath();
            if (!filename.endsWith(".csv")) {
                filename += ".csv";
            }
            this.saveMasterFile(filename);
            System.out.println("getCurrentDirectory(): " + fileChooser.getInitialDirectory());
            System.out.println("getSelectedFile(): " + filename);
        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        featureSelectorList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        featureSelectorList.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                ObservableList<String> selectedItems = featureSelectorList.getSelectionModel().getSelectedItems();
                ArrayList<String> selectedFeatures = new ArrayList();

                for (String s : selectedItems) {
                    selectedFeatures.add(s);
                }
                ConfigurationManager.getConfigurationManager().getConfiguration().setSelectedColumns(selectedFeatures);
            }
        });

        initData();
    }

    private void initData() {
        datasetComboBox.getItems().clear();
        ArrayList<String> resultList = PostgresSQLDBManager.getAllStrainTypeIDs();
        for (String s : resultList) {
            datasetComboBox.getItems().add(s);
        }
        datasetComboBox.getSelectionModel().select(0);
        ConfigurationManager.getConfigurationManager().getConfiguration().setStrainTypeId(datasetComboBox.getSelectionModel().getSelectedItem().toString());

        tableComboBox.getItems().clear();
        resultList = PostgresSQLDBManager.getAllTableNames();
        for (String s : resultList) {
            tableComboBox.getItems().add(s);
        }
        tableComboBox.getSelectionModel().select(0);
        System.out.print(ConfigurationManager.getConfigurationManager().getConfiguration().getTableName());
        ConfigurationManager.getConfigurationManager().getConfiguration().setTableName(tableComboBox.getSelectionModel().getSelectedItem().toString());
        ArrayList<String> tableKeys = PostgresSQLDBManager.getAllKeysOfTable(tableComboBox.getSelectionModel().getSelectedItem().toString());
        ConfigurationManager.getConfigurationManager().getConfiguration().setTableKeys(tableKeys);
        GraphicUtils.populateFeatureList(featureSelectorList);
    }

    private TableView buildMainDisplayTable(CachedRowSet crs) {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        TableView tableView = new TableView();
        try {
            crs.beforeFirst();
            ResultSetMetaData metaData = crs.getMetaData();

            for (int i = 0; i < metaData.getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(metaData.getColumnName(i + 1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                tableView.getColumns().addAll(col);
            }

            while (crs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    row.add(crs.getString(i));
                }
                data.add(row);
            }

            tableView.setItems(data);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresSQLDBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tableView;
    }

    private TableView buildSummaryDisplayTable(ArrayList<FiveNumberSummary> fnsList) {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        TableView tableView = new TableView();
        TableColumn col;

        for (int i = -1; i < fnsList.size(); i++) {
            final int j = i + 1;
            if (i == -1) {
                col = new TableColumn("");
            } else {
                col = new TableColumn(fnsList.get(i).getName());
            }
            col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });
            tableView.getColumns().addAll(col);
        }

        data.add(Utils.generateDataRowFromFiveNumberSummaryList("Min", fnsList));
        data.add(Utils.generateDataRowFromFiveNumberSummaryList("1st Quartile", fnsList));
        data.add(Utils.generateDataRowFromFiveNumberSummaryList("Median", fnsList));
        data.add(Utils.generateDataRowFromFiveNumberSummaryList("3rd Quartile", fnsList));
        data.add(Utils.generateDataRowFromFiveNumberSummaryList("Max", fnsList));
        tableView.setItems(data);

        return tableView;
    }

    private static HashMap<String, ArrayList<Double>> prepareDataForFiveNumberSummary(CachedRowSet crs) {
        HashMap<String, ArrayList<Double>> resultMap = new HashMap<>();
        try {
            crs.beforeFirst();
            ResultSetMetaData metaData = crs.getMetaData();
            Vector<String> columnNames = new Vector<>();
            int columnCount = metaData.getColumnCount();

            for (int column = 1; column <= columnCount; column++) {
                columnNames.add(metaData.getColumnName(column));
                if (!ConfigurationManager.getConfigurationManager().getConfiguration().getTableKeys().contains(metaData.getColumnName(column)) && !metaData.getColumnName(column).equalsIgnoreCase("IsMoving") && !metaData.getColumnName(column).equalsIgnoreCase("Resolution")) {
                    resultMap.put(metaData.getColumnName(column), new ArrayList<>());
                }
            }

            while (crs.next()) {
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    if (resultMap.get(columnNames.get(columnIndex - 1)) != null) {
                        try {
                            if (crs.getObject(columnIndex).toString().equalsIgnoreCase("Inf")) {
                                resultMap.get(columnNames.get(columnIndex - 1)).add(Double.POSITIVE_INFINITY);
                            } else {
                                resultMap.get(columnNames.get(columnIndex - 1)).add(Double.parseDouble(crs.getObject(columnIndex).toString()));
                            }
                        } catch (NullPointerException | NumberFormatException npe) {
                            resultMap.get(columnNames.get(columnIndex - 1)).add(Double.NaN);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(WormViewerFXStaticController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultMap;
    }
    
    private void saveMasterFile(String filename){
        try{
//            String dataSetName = convertStarinTypeIdToDatasetName(ConfigurationManager.getConfigurationManager().getConfiguration().getStrainTypeId());
            String inputPath = "http://140.192.247.25:8585/data/*****/masterFile.csv";
//            inputPath = inputPath.replace("*****", dataSetName);
            inputPath = inputPath.replace("*****", ConfigurationManager.getConfigurationManager().getConfiguration().getStrainTypeId().toUpperCase());
            System.out.println(inputPath);
            URL inputURL = new URL(inputPath);
            File outputFile = new File(filename);
            try(InputStream in = inputURL.openStream()) {
                Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(WormViewerFXStaticController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(WormViewerFXStaticController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
