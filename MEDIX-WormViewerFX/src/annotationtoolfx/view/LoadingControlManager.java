package annotationtoolfx.view;

import java.io.File;

import annotationtoolfx.db.FrameManagerLoader;
import annotationtoolfx.db.StrainTypeLoader;
import annotationtoolfx.db.AutoRevise;
import annotationtoolfx.object.FrameAnnotationManager;

public class LoadingControlManager {

    private FrameManagerLoader loader;
    File wormImageDirectory = null;
    File predictedAnnFile = null;
    File offlineAnnFile = null;
    private int selectedPredCol = -1;
    private String strainTypeId;
    private String compareAnnSet;
    private String editAnnSet;
    private StrainTypeLoader strainLoader; 
    private boolean revise = false;
    
    public LoadingControlManager() {
    	strainLoader = new StrainTypeLoader();
    	loader = new FrameManagerLoader();
    }
    
	public void loadFrameMgr() {
		loader.setStrainId(strainTypeId);
		loader.setDirectory(wormImageDirectory);
		loader.setOfflineAnnFile(offlineAnnFile);
		loader.setCompareAnnSet(compareAnnSet);
		loader.setEditAnnSet(editAnnSet);
		loader.setPredictionFile(predictedAnnFile);
		loader.setPredictionColumn(selectedPredCol);
		loader.LoadAnnFrameMgr();	
                if(revise){
//                    new AutoRevise().Revise(loader.getFrameMgr().getFrameList());                 
                      new AutoRevise().Revise(loader.getFrameMgr().getFrameList(), compareAnnSet);
                }
	}
	
	public FrameAnnotationManager getFrameMgr() {
		return loader.getFrameMgr();
	}

	public String getStrainTypeId() {
		return strainTypeId;
	}
	
	public void setStrainTypeId(String strainTypeId) {
		this.strainTypeId = strainTypeId;
	}

	public File getWormImageDirectory() {
		return wormImageDirectory;
	}

	public void setWormImageDirectory(File wormDir) {
		wormImageDirectory = wormDir;
	}

	public StrainTypeLoader getStrainLoader() {
		return strainLoader;
	}

	public File getOfflineFile() {
		return offlineAnnFile;
	}

	public void setOfflineFile(File file) {
		offlineAnnFile = file;
	}

	public String getEditAnnSet() {
		return editAnnSet;
	}

	public void setEditAnnSet(String editAnnSet) {
		this.editAnnSet = editAnnSet;
	}

	public void setCompareaAnnSet(String compareSet) {
		compareAnnSet = compareSet;
	}

	public void setPredictionFile(File predFile) {
		predictedAnnFile = predFile;
	}
	
	public File getPredictionFile() {
		return predictedAnnFile;
	}
	
	public void setPredictionColumn(int predColumn) {
		selectedPredCol = predColumn;
	}
	
	public int getPredictionColumn() {
		return selectedPredCol;
	}
        
        public void setRevise(boolean val)
        {
            revise = val;
        }
	
}
