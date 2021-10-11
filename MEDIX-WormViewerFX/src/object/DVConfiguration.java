package object;

import java.util.ArrayList;

/**
 *
 * @author mshao1
 */
public class DVConfiguration {

    private String dvStrainTypeId = "";
    private String dvTableName = "";
    private String dvSelectedColumn = "";
    private ArrayList<String> dvTableKeys = new ArrayList();

    public final String generateDVSQLQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(dvSelectedColumn);
        sb.append(" FROM ");
        sb.append(dvTableName);
        
        if(dvTableKeys.contains("frameid")){
            sb.append(" WHERE frameid LIKE ");
            sb.append("'");
            sb.append(dvStrainTypeId);
            sb.append("\\_%'");
        } else if (dvTableKeys.contains("straintypeid")) {
            sb.append(" WHERE straintypeid LIKE ");
            sb.append("'");
            sb.append(dvStrainTypeId);
            sb.append("%'");
        }
        
        System.out.println(sb.toString());
        return sb.toString();
    }

    public String getDvStrainTypeId() {
        return dvStrainTypeId;
    }

    public void setDvStrainTypeId(String dvStrainTypeId) {
        this.dvStrainTypeId = dvStrainTypeId;
    }

    public String getDvTableName() {
        return dvTableName;
    }

    public void setDvTableName(String dvTableName) {
        this.dvTableName = dvTableName;
    }

    public String getDvSelectedColumn() {
        return dvSelectedColumn;
    }

    public void setDvSelectedColumn(String dvSelectedColumn) {
        this.dvSelectedColumn = dvSelectedColumn;
    }

    public ArrayList<String> getDvTableKeys() {
        return dvTableKeys;
    }

    public void setDvTableKeys(ArrayList<String> dvTableKeys) {
        this.dvTableKeys = dvTableKeys;
    }
    
    
}
