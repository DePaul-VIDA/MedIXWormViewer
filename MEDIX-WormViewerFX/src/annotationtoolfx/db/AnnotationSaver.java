package annotationtoolfx.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import annotationtoolfx.object.AnnotationSet;
import annotationtoolfx.object.FrameAnnotationInfo;
import annotationtoolfx.object.FrameAnnotationManager;


public class AnnotationSaver {
	
	
	public void saveAnnotations(FrameAnnotationManager mgr, AnnotationSet set, boolean finalversion, double minutes) throws SQLException{
		//Existing Set
		PreparedStatement stmt;

		ArrayList<FrameAnnotationInfo> unhandledFrames = new ArrayList<FrameAnnotationInfo>();
		unhandledFrames.addAll(mgr.getFrameList());
		ArrayList<FrameAnnotationInfo> updateFrames = new ArrayList<FrameAnnotationInfo>();

		String sql = "SELECT imageinfo.imagenumber FROM annotations JOIN imageinfo on annotations.frameid = imageinfo.frameid WHERE annotations.setid = ? ORDER BY imageinfo.imagenumber;";
		stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql); 
		stmt.setString(1, set.getSetId());
		ResultSet rs = stmt.executeQuery();
		
		int frameNo = 0;
		while(rs.next()){
			frameNo = rs.getInt(1);
			//Should frames in both sets should be sorted, reducing search time
			for(FrameAnnotationInfo fai : unhandledFrames) {
				if(fai.getFrameNo() == frameNo) {
					updateFrames.add(fai);
					unhandledFrames.remove(fai);
					break;
				}
			}
		}
		rs.close();
		stmt.close();
		//Do updates
	    String SQL = "UPDATE annotations SET annotation = ?, reviewed = ? WHERE setid = ? AND frameid = ?";
	    try {
	        stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(SQL);
	        ConnectionSingleton.getConnectionInstance().getConnection().setAutoCommit(false);
	        stmt.setString(3,  set.getSetId());
	        
	        for(FrameAnnotationInfo fai : updateFrames) {
		        stmt.setString(1,  fai.getUpdatedAnnotation());
		        stmt.setBoolean(2,  fai.getReviewed());
		        stmt.setString(4,  fai.getdbFrameId());
		        stmt.addBatch(); // Add to Batch
	        }

	        stmt.executeBatch(); // execute the Batch and commit
	        ConnectionSingleton.getConnectionInstance().getConnection().commit();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    	ConnectionSingleton.getConnectionInstance().getConnection().rollback();
	    } finally {
		    stmt.close();
	    }
	    
	    if(finalversion) {
		    SQL = "UPDATE annotationset SET finalversion = 'yes', timerequired = ? WHERE setid = ?";
	        stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(SQL);
			stmt.setDouble(1, minutes);
			stmt.setString(2, set.getSetId());
			stmt.execute();
	    }
	    
	    doSaveAnnotations(unhandledFrames, set.getSetId());
	}
	
	public void saveAnnotations(FrameAnnotationManager mgr, String userId, String name, boolean finalversion, double minutes) throws SQLException{
		//New Set
		ArrayList<FrameAnnotationInfo> unhandledFrames = new ArrayList<FrameAnnotationInfo>();
		unhandledFrames.addAll(mgr.getFrameList());		
		
		String setId = getNextSetId(mgr.getStrainTypeId());
		String fv = "no";
		if(finalversion)
			fv = "yes";
		
		PreparedStatement stmt;
		String insertSet = "INSERT INTO AnnotationSet(userid, setid, name, datetimeannotation, straintypeid, finalversion, timerequired) VALUES(?, ?, ?, ?, ?, ?, ?);";
		stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(insertSet);
		stmt.setString(1,  userId);
		stmt.setString(2,  setId);
		stmt.setString(3,  name);
		java.util.Date utildate = new java.util.Date();
		java.sql.Date date = new java.sql.Date(utildate.getTime());
		stmt.setDate(4, date);
		stmt.setString(5,  mgr.getStrainTypeId());
		stmt.setString(6,  fv);
		stmt.setDouble(7,  minutes);
		stmt.executeUpdate();
		
	    doSaveAnnotations(unhandledFrames, setId);
	}
	

	private void doSaveAnnotations(ArrayList<FrameAnnotationInfo> unhandledFrames, String setId) throws SQLException{
		PreparedStatement stmt;
		/*ArrayList<FrameAnnotationInfo> insertFrames = new ArrayList<FrameAnnotationInfo>();

			
		for(FrameAnnotationInfo fai : unhandledFrames) {
			if(!(fai.getUpdatedAnnotation() == null || fai.getUpdatedAnnotation().trim().length() == 0)) {
				insertFrames.add(fai);
			}
		}		

		for(FrameAnnotationInfo fai : insertFrames) {
			unhandledFrames.remove(fai);
		}
		*/
		//Do inserts
	    String SQL = "INSERT INTO annotations(frameid, setid, annotation, reviewed, priorannotation) VALUES(?, ?, ?, ?, ?);";
	    try {
	        stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(SQL);
	        ConnectionSingleton.getConnectionInstance().getConnection().setAutoCommit(false);
	        stmt.setString(2,  setId);
	        
	        for(FrameAnnotationInfo fai : unhandledFrames) {
                    stmt.setBoolean(4, fai.getReviewed());
		      if(!(fai.getUpdatedAnnotation() == null || fai.getUpdatedAnnotation().trim().length() == 0))
                              stmt.setString(3,  fai.getUpdatedAnnotation());
                      else if((!(fai.getPredictedAnnotation()== null || fai.getPredictedAnnotation().trim().length() == 0)) &&
                              (!fai.getPredictedAnnotation().contains("NeedsReview")))
                              stmt.setString(3,  fai.getPredictedAnnotation());
                      else if(!(fai.getHumanAnnotation()== null || fai.getHumanAnnotation().trim().length() == 0))
                              stmt.setString(3,  fai.getHumanAnnotation());
                      else
                          continue;
		      stmt.setString(5,  fai.getPriorAnnotation());
		        stmt.setString(1,  fai.getdbFrameId());
		        stmt.addBatch(); // Add to Batch
	        }

	        stmt.executeBatch(); // execute the Batch and commit
	        ConnectionSingleton.getConnectionInstance().getConnection().commit();
	    } catch (SQLException e) {
	    	ConnectionSingleton.getConnectionInstance().getConnection().rollback();
	        e.printStackTrace();
	    } finally {
	    }
	}

	private String getNextSetId(String strainTypeId) throws SQLException {
	    String sql = "SELECT setid FROM annotationset WHERE straintypeid = ? ORDER BY datetimeannotation DESC;";
		PreparedStatement stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql); 
		stmt.setString(1, strainTypeId);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()){
               
                    String s = rs.getString(1);
                    rs.close();
                    stmt.close();
                    s = s.replace(strainTypeId, "");
                    s = s.replace("_a", "");
                    int i = Integer.parseInt(s);
                    i++;
                    s = strainTypeId + "_a" + Integer.toString(i);
                    return s;
                }
                else{
                    return strainTypeId + "_a1";
                }
	}

	public HashMap<String, AnnotationSet> getNameSets(String strainTypeId) throws SQLException {
	    String sql = "SELECT setid, userid, name, datetimeannotation FROM annotationset WHERE straintypeid = ? AND finalversion <> 'yes';";
		PreparedStatement stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql); 
		stmt.setString(1, strainTypeId);
		ResultSet rs = stmt.executeQuery();
		HashMap<String, AnnotationSet> map = new HashMap<String, AnnotationSet>();
		java.util.Date d;
		String name;
		while(rs.next()){
			d = new java.util.Date(rs.getDate(4).getTime());
			name = rs.getString(3);
			if(!map.containsKey(name)) {
				map.put(name, new AnnotationSet(name, rs.getString(1), rs.getString(2), strainTypeId, d));
			}
		}
		
		return map;
	}

	public ArrayList<String> getUsernames() throws SQLException {
	    String sql = "SELECT emailid FROM userinfo;";
		PreparedStatement stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql); 
		ResultSet rs = stmt.executeQuery();
		ArrayList<String> list = new ArrayList<String>();
		while(rs.next()){
			list.add(rs.getString(1));
		}
		return list;
	}

	public boolean nameNotUsed(String name) {
	    String sql = "SELECT setid FROM annotationset WHERE name = ?;";
	    boolean result = false;
		PreparedStatement stmt;
		try {
			stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(sql);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
				result = false;
			result = true;
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return result;
	}
	
}
