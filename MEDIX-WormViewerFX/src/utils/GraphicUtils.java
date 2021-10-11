package utils;

import singleton.ConfigurationManager;
import singleton.PostgresSQLDBManager;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

/**
 *
 * @author MSHAO1
 */
public class GraphicUtils {

    public static void populateFeatureList(ListView listView) {
        if (!ConfigurationManager.getConfigurationManager().getConfiguration().getStrainTypeId().isEmpty() && !ConfigurationManager.getConfigurationManager().getConfiguration().getTableName().isEmpty()) {
            ObservableList<String> columnNames = PostgresSQLDBManager.getAllTableColumnLabels(ConfigurationManager.getConfigurationManager().getConfiguration().getTableName());
            listView.setItems(columnNames);
            listView.getSelectionModel().select(0);
        }
    }

    public static void populateFeatureComboBox(ComboBox comboBox) {
        if (!ConfigurationManager.getConfigurationManager().getDVConfiguration().getDvStrainTypeId().isEmpty() && !ConfigurationManager.getConfigurationManager().getDVConfiguration().getDvTableName().isEmpty()) {
            ObservableList<String> columnNames = PostgresSQLDBManager.getAllTableColumnLabels(ConfigurationManager.getConfigurationManager().getDVConfiguration().getDvTableName());
            columnNames.remove("*");
            comboBox.setItems(columnNames);
            comboBox.getSelectionModel().select(0);
        }
    }
}
