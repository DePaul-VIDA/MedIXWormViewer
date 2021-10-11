package annotationtoolfx.object;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FilenameFilter;

public class FrameAnnotationManager implements Iterable<FrameAnnotationInfo> {

	public FrameAnnotationManager(ArrayList<FrameAnnotationInfo> faiList, String strainTypeId, String arfromset, int arcount) { 
		frameList = faiList;
		frameByNo = new HashMap<Integer, FrameAnnotationInfo>();
		for(FrameAnnotationInfo fai : faiList)
			frameByNo.put(new Integer(fai.getFrameNo()), fai);
		this.strainTypeId = strainTypeId;
		this.revisionCount = arcount;
		this.arFrom = arfromset;
	}

	public FrameAnnotationManager(ArrayList<FrameAnnotationInfo> faiList, String strainTypeId, String setId, String arfromset, int arcount) { 
		this(faiList, strainTypeId, arfromset, arcount);
		this.setId = setId;
	}
	
    private ArrayList<FrameAnnotationInfo> frameList;
    private HashMap<Integer, FrameAnnotationInfo> frameByNo;
    private boolean annotationsChanged = false;
    private boolean annotationsLoaded = false;
    private boolean elapsedTimeLoaded = false;
    private boolean imagesLoaded = false;
    private String strainTypeId = "";
    private String setId = "";
    private String arFrom = "";
    private int revisionCount = 0;
    
    public int getRevisionCount() {
    	return revisionCount;
    }
    
    public String getPriorARSet() {
    	return arFrom;
    }
    
    
    public String getStrainTypeId() {
    	return strainTypeId;
    }

    public String getSetId() {
    	return setId;
    }

    public boolean getAnnotationsChanged() {
        return annotationsChanged;
    }

    public void setAnnotationsChanged(boolean value) {
        annotationsChanged = value;
    }

    public boolean getAnnotationsLoaded() {
        return annotationsLoaded;
    }

    public boolean getElapsedTimeLoaded() {
        return elapsedTimeLoaded;
    }

    public boolean getImagesLoaded() {
        return imagesLoaded;
    }


    public int getCount() {
        return frameByNo.size();
    }
   
    public FrameAnnotationInfo get(int index) {
      
        if (frameByNo.size() > index)
            return frameByNo.get(index);
        else
            return null;
    }
    
    public void set(Integer index, FrameAnnotationInfo value) {

    	frameByNo.put(index, value);
    }

    public boolean isFrameInRange(int frame) {
        return frameByNo.size() > frame;
    }

    public ArrayList<FrameAnnotationInfo> getFrameList() {
        return frameList;
    }
    

    private FrameAnnotationInfo getEmptyFAI( int i)
    {
        NullFrameAnnInfo fai = new NullFrameAnnInfo();
        fai.setFrameNo(i);
        fai.setImageFile(null);
        return fai;
    }

    public int findNeedsReview(int currentIndex)
    {
        int j = currentIndex + 1;
        try
        {
        while (j >= 0 && j < frameByNo.size() && j >= 0 && frameByNo.containsKey(j) && !frameByNo.get(j).IsNeedsReview())
            j += 1;
        }
        catch(Exception e)
        {
            boolean b = frameByNo.get(j).IsNeedsReview();
        }
        return j;
    }
    
    public int findSharpTurn(int currentIndex)
    {
        int j = currentIndex + 1;
        try
        {
        while (j >= 0 && j < frameByNo.size() && j >= 0 && frameByNo.containsKey(j) && !(frameByNo.get(j).getHumanShortAnnotation() == AnnotationShortValue.Turn))
            j += 1;
        }
        catch(Exception e)
        {
        }
        return j;
    }
    
    public int findNonMatchAfter(int currentIndex)
    {
        return findNonMatch(currentIndex, 1);
    }
        
    
    private int findNonMatch(int currentIndex, int direction)
    {
        int j = currentIndex + direction;
        if (!frameByNo.get(currentIndex).doAnnotationsMatch())
        {
            int i = currentIndex;
            while (i < frameByNo.size() && i >= 0 && frameByNo.containsKey(i) && !frameByNo.get(i).doAnnotationsMatch())
            {
                i += direction;
            }
            j = i + direction;
        }

        while (j >= 0 && j < frameByNo.size() && j >= 0 && frameByNo.containsKey(j) && frameByNo.get(j).doAnnotationsMatch())
            j += direction;

        if (j >= frameByNo.size() ||  j < 0)
            return currentIndex;
        else
            return j;

    }

    public int findNonMatchBefore(int currentIndex)
    {
        return findNonMatch(currentIndex, -1);
    }

	@Override
	public Iterator<FrameAnnotationInfo> iterator() {
		return frameList.iterator();
	}

}

// FileNameFilter implementation
class JPEGFilter implements FilenameFilter {

	private static JPEGFilter instance = null;
	private String ext;

	public JPEGFilter(String extension) {
		this.ext = extension.toLowerCase();
	}
	
	public static JPEGFilter getInstance(){
		if(instance == null)
			instance = new JPEGFilter("jpeg");
		return instance;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(".jpeg");
	}

}

class FileCompare implements Comparator<File> {

	private static FileCompare instance = null;

	public FileCompare() {
	}
	
	public static FileCompare getInstance(){
		if(instance == null)
			instance = new FileCompare();
		return instance;
	}

	@Override
	public int compare(File arg0, File arg1) {
		return arg0.getName().compareTo(arg1.getName());

	}

}
