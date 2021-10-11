package annotationtoolfx.db;

import annotationtoolfx.object.AnnotationLongValue;
import annotationtoolfx.object.AnnotationShortValue;
import annotationtoolfx.object.FileNameInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import annotationtoolfx.object.FrameAnnotationInfo;
import annotationtoolfx.object.FrameAnnotationManager;

public class FrameManagerLoader {
	
	private String strainId;
	private File wormImageDirectory;
	private String compareAnnSet;
	private String editAnnSet;
	private File predictedAnnFile;
	private int predColIndex;
	private File offlineAnnFile;
        
	
	private String arfromset;
	private int arcount;
	
	private FrameAnnotationManager frameMgr;

	public FrameManagerLoader() {
		predColIndex = -1;
	}
	
	public FrameAnnotationManager getFrameMgr() {
		return frameMgr; 
	}
	
	public void setStrainId(String strainId) {
		this.strainId = strainId;
	}

	public void setDirectory(String imagesPath) {
		try {
			File f = new File(imagesPath);
			wormImageDirectory = f;
		}
		catch(Exception e) {
			wormImageDirectory = null;
		}
	}

	public void setDirectory(File wormImageDirectory) {
		this.wormImageDirectory = wormImageDirectory;
	}	
	
	public void setPredictionFile(File predictedAnnFile) {
		this.predictedAnnFile = predictedAnnFile;
	}
	
	public void setPredictionColumn(int predColIndex) {
		this.predColIndex = predColIndex;
	}

	public void setPredictionFile(String predictionFile) {
		try {
			File f = new File(predictionFile);
			predictedAnnFile = f;
		}
		catch(Exception e) {
			predictedAnnFile = null;
		}
	}

	public boolean hasValidImagesPath() {
		if(wormImageDirectory != null)
			return wormImageDirectory.exists();
		return false;
	}
	
	public void setCompareAnnSet(String compareAnnSet) {
		this.compareAnnSet = compareAnnSet;
	}

	public void setEditAnnSet(String editAnnSet) {
		this.editAnnSet = editAnnSet;
	}

        public boolean AddAnnotations(boolean compare, HashMap<String, FrameAnnotationInfo> map){
            String queryAnn = "SELECT frameid, annotation, reviewed, priorannotation FROM annotations WHERE annotations.setid = ?"; 

            ResultSet rs = null;
            PreparedStatement stmt = null;
            String frameNoStr;
            try
            {
                stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(queryAnn);
                if(compare)
                {
                	
                    stmt.setString(1, getSetIdFromName(compareAnnSet));
                }                   
                else{
                    stmt.setString(1, getSetIdFromName(editAnnSet));
                }                   
                rs = stmt.executeQuery();
                while(rs.next()){
                    frameNoStr = rs.getString(1);
                    String[] list = frameNoStr.split("_");
                    frameNoStr = list[list.length-1];
                    if(map.containsKey(frameNoStr)){
                        if(compare)
                            map.get(frameNoStr).setHumanAnnotation(rs.getString(2), rs.getBoolean(3));
                        else
                            map.get(frameNoStr).setUpdatedAnnotation(rs.getString(2), rs.getBoolean(3));
                    }
                }
            }
            catch(Exception e){
                    return false;  
            }
            finally {
                try
                {
                    rs.close();
                    stmt.close();;
                }
                catch(Exception e){

                }
            }
            return true;
        }
        
	public boolean LoadAnnFrameMgr() {

		String queryNoAnn = "SELECT imageinfo.frameid, imageinfo.imagenumber, imageinfo.timeelapsed, rawmovementfeatures.speed FROM imageinfo " + 
				"JOIN rawmovementfeatures ON imageInfo.frameid = rawmovementfeatures.frameid WHERE imageinfo.straintypeid = ? ORDER BY imageinfo.imagenumber;";
		PreparedStatement stmt = null;
		ArrayList<FrameAnnotationInfo> list = new ArrayList<FrameAnnotationInfo>();
		HashMap<String, FrameAnnotationInfo> map = new HashMap<String, FrameAnnotationInfo>();

		ResultSet rs = null;
                FileNameInfo info = Utilities.getFileComponents(strainId);
                
                boolean compare = compareAnnSet != null  && compareAnnSet.length() > 0;
                boolean edit = editAnnSet != null  && editAnnSet.length() > 0;
                if(offlineAnnFile != null &&  offlineAnnFile.exists()) {
                    compare = false;
                    edit = false;
                }

                String frameNoStr;
                try
                {
                    stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(queryNoAnn);
                    stmt.setString(1, strainId);
                    rs = stmt.executeQuery();
	
                    String set;
                    int frameNo = 0;
                    FrameAnnotationInfo fai;
                    while(rs.next()){
                        frameNo = rs.getInt(2);
                        frameNoStr = Integer.toString(frameNo);
                        if(!map.containsKey(frameNoStr)) {
		           
                            fai = new FrameAnnotationInfo();
		            fai.setElapsedTime(String.format("%f", rs.getDouble(3)));
		            fai.setFrameNo(rs.getInt(2));
		            fai.setdbFrameId(rs.getString(1));
		            fai.setSpeed(rs.getDouble(4));
                            list.add(fai);			    	
                            map.put(frameNoStr, fai);
                        }
                        else {
                            fai = map.get(frameNoStr);
                        }

		    	String s = String.format("%d", rs.getInt(2)).toString();
		    	s = (info.Prefix + s).substring(s.length()) + info.Extension;
		    	
		    	fai.setImageFile(new File(String.format("%s/%s", wormImageDirectory.getAbsolutePath(), s)));
                    }			

                    String line;
                    String[] values;
                    String frameId;
                    String val;
                    FileReader fileReader;
                    BufferedReader bufferedReader = null;

                    if(offlineAnnFile != null && offlineAnnFile.exists()) {
                        fileReader = new FileReader(offlineAnnFile);
                        bufferedReader = new BufferedReader(fileReader);
                        while((line = bufferedReader.readLine()) != null) {
                            values = line.split(",");
                            frameId = values[0].trim();
                            if(map.containsKey(frameId)) {
                                fai = map.get(frameId);
                                val = values[1].trim();
                                if(val.length() > 0)
                                        fai.setHumanAnnotation(val);
                                val = values[2].trim();
                                if(val.length() > 0)
                                        fai.setPredictedAnnotation(val);
                                val = values[3].trim();
                                if(val.length() > 0)
                                        fai.setUpdatedAnnotation(val);
                            }
                        }
                        bufferedReader.close();
                        fileReader.close();
                    }
                    else if(predictedAnnFile != null && predColIndex > -1) {
                            fileReader = new FileReader(predictedAnnFile);
                            bufferedReader = new BufferedReader(fileReader);

                    if(predictedAnnFile != null) {
                        while((line = bufferedReader.readLine()) != null) {
                                values = line.split(",");
                                frameId = values[0].trim();
                                if(map.containsKey(frameId)) {
                                        fai = map.get(frameId);
                                        fai.setPredictedAnnotation(values[predColIndex]);
                                }
                        }
                    }
                    bufferedReader.close();
                    fileReader.close();
                    }
                }
                catch(Exception e){
			return false;  
		}
		finally {
                    try
                    {
                        rs.close();
                        stmt.close();;
                    }
                    catch(Exception e){

                    }
		}

                if(compare)
                    AddAnnotations(true, map);
                if(edit)
                    AddAnnotations(false, map);
		
		if(editAnnSet != null && editAnnSet.trim().length() > 0)
			frameMgr = new FrameAnnotationManager(list, strainId, editAnnSet, arfromset, arcount);
		else
			frameMgr = new FrameAnnotationManager(list, strainId, arfromset, arcount);
	    return true;
	}

	public void setOfflineAnnFile(File offlineAnnFile) {
		this.offlineAnnFile = offlineAnnFile;
	}

        private String getSetIdFromName(String annSetName) {
            ResultSet rs = null;
            PreparedStatement stmt = null;
            String id = null;
            try
            {
                stmt = ConnectionSingleton.getConnectionInstance().getConnection().prepareStatement(
                		"SELECT setid, autorevisecount, autorevisefromset FROM Annotationset WHERE name = ?");
                stmt.setString(1, annSetName);
                rs = stmt.executeQuery();
                rs.next();
                id = rs.getString(1);
                arcount = rs.getInt(2);
                arfromset = rs.getString(3);
            }
            catch(Exception e){

            }
            return id;
    }



	
}
