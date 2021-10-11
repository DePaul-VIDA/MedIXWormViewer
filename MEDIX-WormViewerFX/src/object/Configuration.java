package object;

import java.util.ArrayList;

/**
 *
 * @author mshao1
 */
public class Configuration {

    private String strainTypeId = "";
    private String tableName = "";
    private ArrayList<String> selectedColumns = new ArrayList();
    private ArrayList<String> tableKeys = new ArrayList();

    public final String generateSQLQuery() {
        ArrayList<String> featuresList = new ArrayList();
        featuresList.addAll(tableKeys);
        for (String s : selectedColumns) {
            if (!featuresList.contains(s)) {
                featuresList.add(s);
            }
        }

        StringBuilder sb = new StringBuilder();
        if (selectedColumns.contains("*")) {
            sb.append("SELECT * FROM ");
        } else {
            sb.append("SELECT ");
            for (String s : featuresList) {
                sb.append(s);
                if (featuresList.indexOf(s) != (featuresList.size() - 1)) {
                    sb.append(",");
                } else {
                    sb.append(" ");
                }
            }
            sb.append("FROM ");
        }
        sb.append(tableName);
        
        if(tableKeys.contains("frameid")){
            sb.append(" WHERE frameid LIKE ");
            sb.append("'");
            sb.append(strainTypeId);
            sb.append("\\_%'");
        } else if (tableKeys.contains("straintypeid")) {
            sb.append(" WHERE straintypeid LIKE ");
            sb.append("'");
            sb.append(strainTypeId);
            sb.append("%'");
        }
        
        System.out.println(sb.toString());
        return sb.toString();
    }

    public String getStrainTypeId() {
        return strainTypeId;
    }

    public void setStrainTypeId(String strainTypeId) {
        this.strainTypeId = strainTypeId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<String> getSelectedColumns() {
        return selectedColumns;
    }

    public void setSelectedColumns(ArrayList<String> selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    public ArrayList<String> getTableKeys() {
        return tableKeys;
    }

    public void setTableKeys(ArrayList<String> tableKeys) {
        this.tableKeys = tableKeys;
    }

}
