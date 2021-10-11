package annotationtoolfx.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import annotationtoolfx.object.AnnotationSet;
import annotationtoolfx.object.FileNameInfo;
import annotationtoolfx.view.WormVideoDisplay;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;


public class StrainTypeLoader {

	
	HashMap<String, AnnotationSet> loadedAnnotations = new HashMap<String, AnnotationSet> ();
	HashMap<String, TreeItem<WormVideoDisplay>> wormTypeTreeMap = new HashMap<String, TreeItem<WormVideoDisplay>>();
	ArrayList<String> wormTypeNames = new ArrayList<String>();
	 
	public class AnnotationSetLists {
		HashMap<String, AnnotationSet> dbList;
		ObservableList<String> displayList;
		
		public AnnotationSetLists(ArrayList<String> stringList, HashMap<String, AnnotationSet> dbList) {
			this.dbList = dbList;
			this.displayList = FXCollections.observableList(stringList);
		}
		
	}

	ObservableList<String> strainList = null;
	HashMap<String, AnnotationSetLists> annotationsByStrainFinal = new HashMap<String, AnnotationSetLists>();
	HashMap<String, AnnotationSetLists> annotationsByStrainNotFinal = new HashMap<String, AnnotationSetLists>();
	
    public ObservableList<String> getStrains() {
		Statement stmt = null;
		
		if(strainList != null)
			return strainList;
		
		ArrayList<String> list = new ArrayList<String>();
				
		try{
		    stmt = ConnectionSingleton.getConnectionInstance().getConnection().createStatement();
		    String sql;
		    sql = "SELECT straintypeid FROM straintype ORDER BY straintypeid";
		    ResultSet rs = stmt.executeQuery(sql);
		    while(rs.next()){
		         //Retrieve by column name
		    	list.add(rs.getString("straintypeid"));
		      }

		      rs.close();
		      stmt.close();

		}catch(SQLException se){
			//TODO
		}catch(Exception e){
			//TODO
		}finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	    	  
	      }// nothing we can do
		
		}//end try
		
		strainList = FXCollections.observableList(list);
		
		return strainList;
	}
    
    public ObservableList<String> getAnnotationsByStrains(String strainId, boolean finalized){
    	
    	HashMap<String, AnnotationSetLists> annotationsByStrain;
    	if(finalized)
    		annotationsByStrain = annotationsByStrainFinal;
    	else
    		annotationsByStrain = annotationsByStrainNotFinal;

    	if(annotationsByStrain.containsKey(strainId))
    		return annotationsByStrain.get(strainId).displayList;
    	
		PreparedStatement stmt = null;
    	HashMap<String, AnnotationSet> dbList = new HashMap<String, AnnotationSet>();
    	ArrayList<String> stringList = new ArrayList<String>();
    	
    	try{
		    String sql;
		    sql = "SELECT userid, setid, name, datetimeannotation FROM annotationset WHERE straintypeid = ? AND finalversion = ? ORDER BY name";
		    stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql);
		    stmt.setString(1, strainId);
		    if(finalized)
		    	stmt.setString(2, "yes");
		    else
		    	stmt.setString(2, "no");
		    ResultSet rs = stmt.executeQuery();
		    while(rs.next()){
		         //Retrieve by column name
		    	String n = rs.getString("name");
		    	AnnotationSet a = new AnnotationSet(rs.getString("userid"), rs.getString("setid"), n, strainId, rs.getDate("datetimeannotation"));
		    	if(!loadedAnnotations.containsKey(n))
		    		loadedAnnotations.put(n, a);
		    	if(n.length() > 1) {
		    		stringList.add(n);
		    		dbList.put(n, a);
		    	}
		    }

		      rs.close();
		      stmt.close();

		}catch(SQLException se){
			//TODO
		}catch(Exception e){
			//TODO
			System.out.print(e.getMessage());
		}finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	    	  
	      }// nothing we can do
		
		}//end try
    	
    	annotationsByStrain.put(strainId, new AnnotationSetLists(stringList, dbList));
    	return annotationsByStrain.get(strainId).displayList;
    }

    public HashMap<String, TreeItem<WormVideoDisplay>> getWormTypes(){
    	return wormTypeTreeMap;
    	
    }
    
    public ArrayList<String> getWormTypeNames(){
    	return wormTypeNames;
    }
    
    
    public void loadWormTypes() {
    	String sql = "SELECT Straintype.wormtype, imageinfo.straintypeid, Straintype.foodcondition, imageinfo.frameid, imageinfo.timeelapsed, ss2.numAnn FROM imageinfo " + 
    			"INNER JOIN (SELECT max(imagenumber) AS imNum, straintypeid as stid FROM imageinfo GROUP BY straintypeid) ss ON imageinfo.imagenumber = ss.imNum and imageinfo.straintypeid= ss.stid " + 
    			"LEFT JOIN (SELECT count(setid) AS numAnn, straintypeid as stid FROM annotationset WHERE finalversion = 'yes' GROUP BY straintypeid) ss2 ON imageinfo.straintypeid = ss2.stid " + 
    			"LEFT JOIN Straintype ON imageinfo.straintypeid = Straintype.straintypeid ORDER BY straintypeid;";
		
    	PreparedStatement stmt = null;
    	wormTypeTreeMap.clear();
    	wormTypeNames.clear();
    	
    	try{

		    stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql);
		    ResultSet rs = stmt.executeQuery();
		    String wormtype, stid, fc, te;
		    int ac;
		    
		    while(rs.next()){
		    	wormtype = rs.getString(1);
		    	stid = rs.getString(2);
		    	fc = rs.getString(3);
		    	te = rs.getString(5);
		    	ac = rs.getInt(6);
		    	if(rs.wasNull())
		    		ac = 0;

		    	 if(!wormTypeTreeMap.containsKey(wormtype)) {
		    		 wormTypeTreeMap.put(wormtype, new TreeItem<WormVideoDisplay>(new WormVideoDisplay(wormtype, "", "", "", "") ));
		    		 wormTypeNames.add(wormtype);
		    	 }
		    	 wormTypeTreeMap.get(wormtype).getChildren().add(new TreeItem<WormVideoDisplay>(new WormVideoDisplay(wormtype, stid, fc, te, Integer.toString(ac))));
		    	
		    }

		    
    	}catch(SQLException se){
			//TODO
			System.out.print(se.getMessage());
		}finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	    	  
	      }// nothing we can do
		
		}//end try
    }
   

	public ArrayList<String> getFileNames(String wormVideo) {
            ArrayList<String> list = new ArrayList<String>();

            String query = "SELECT imagenumber FROM imageinfo WHERE straintypeid = ? ORDER BY imagenumber";

            FileNameInfo fni = Utilities.getFileComponents(wormVideo);
            
	    PreparedStatement stmt;
            try {
                stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(query);
                stmt.setString(1,  wormVideo);
                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                 //Retrieve by column name
                    String s = String.format("%d", rs.getInt(1)).toString();
                    s = (fni.Prefix + s).substring(s.length());

                    list.add(s + fni.Extension);
                }

            } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }


            return list;
	}


}
