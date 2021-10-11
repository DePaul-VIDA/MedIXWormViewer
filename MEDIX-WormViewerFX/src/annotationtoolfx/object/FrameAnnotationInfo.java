package annotationtoolfx.object;

import java.io.File;

import annotationtoolfx.view.UpdateObserver;

public class FrameAnnotationInfo implements Comparable<FrameAnnotationInfo> {

    static String STOP = "stop";
    static String DASHSTOP = "-stop";
    static String BACKWARD = "backward";
    static String FORWARD = "forward";
    static String REVERSE = "reverse";
    static String LONG = "long";
    static String SHORT = "short";
    static String SHALLOW = "shallow";
    static String SHARP = "sharp";
    static String NTD = "ntd";

    private int frameNo;
    private String frameNoStr;
    private String dbFrameId;
    private String elapsedTime;
    private double speed;
    private boolean reviewed;
    private File imageFile;
    private String humanAnnotation;
    private String predictedAnnotation;
    private String updatedAnnotation;
    private String priorAnnotation;
    private int revisedCount;
    

    private String predictedDiffUpdated;
    private AnnotationLongValue humanAnnotationLong;
    private AnnotationLongValue predictedAnnotationLong;
    private AnnotationLongValue updatedAnnotationLong;
    private AnnotationShortValue humanAnnotationShort;
    private AnnotationShortValue predictedAnnotationShort;
    private AnnotationShortValue updatedAnnotationShort;
    
    private UpdateObserver updateObserver;


    public FrameAnnotationInfo()
    {
        updatedAnnotationLong = AnnotationLongValue.Unknown;
        updatedAnnotationShort = AnnotationShortValue.Unknown;
        updateObserver = null;
        revisedCount = 0;
    }

    public int getFrameNo() {
        return frameNo;
    }
    public String getFrameNoAsString() {
        return frameNoStr;
    }

    public String getdbFrameId() {
        return dbFrameId;
    }

    public void setdbFrameId(String id) {
    	dbFrameId = id;
    }
    
    public void setSpeed(double spd)
    {
        speed = spd;
    }

    public double getSpeed()
    {
        return speed;
    }
    
    public void setFrameNo(int value) {
        frameNo = value;
        frameNoStr = Integer.toString(value);  
        if(updateObserver != null)
        	updateObserver.update();
    }

    public String getElapsedTime()
    {
        return elapsedTime;
    }

    public void setElapsedTime(String value) {
        elapsedTime = value;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File value) {
    	imageFile = value;
    }
    
    public class AnnotationValues {
        public AnnotationLongValue longVal;
        public AnnotationShortValue shortVal;
    }

    public class AnnotationShortValueReference {
        public AnnotationShortValue val;
    }

    private AnnotationValues getAnnValues(String value){

    	AnnotationValues av = new AnnotationValues();
        String lower = value.toLowerCase();
        if (lower.contains(STOP))
        {
            av.shortVal = AnnotationShortValue.Stop;
            av.longVal = AnnotationLongValue.Stop;
        }
        else if (lower.contains(REVERSE))
        {
            av.shortVal = AnnotationShortValue.Backward;
            if (lower.contains(LONG))
                av.longVal = AnnotationLongValue.ReverseLong;
            else if (lower.contains(SHORT))
                av.longVal = AnnotationLongValue.ReverseShort;
            else
                av.longVal = AnnotationLongValue.Unknown;
        }
        else if (lower.contains(SHARP))
        {
            av.shortVal = AnnotationShortValue.Turn;
                av.longVal = AnnotationLongValue.ForwardSharp;
        }
        else if (lower.contains(FORWARD))
        {
            av.shortVal = AnnotationShortValue.Forward;
            if (lower.contains(SHALLOW))
                av.longVal = AnnotationLongValue.ForwardShallow;
            else if (lower.contains(NTD))
                av.longVal = AnnotationLongValue.ForwardNTD;
            else
                av.longVal = AnnotationLongValue.Unknown;
        }
        else
        {
            av.shortVal = AnnotationShortValue.Unknown;
            av.longVal = AnnotationLongValue.Unknown;
        }
        return av;

    }

    public String getHumanAnnotation() {
        return humanAnnotation;
    }

    public String getPriorAnnotation() {
        return priorAnnotation;
    }
    public AnnotationShortValue getHumanShortAnnotation() {
        return humanAnnotationShort;
    }

    public AnnotationLongValue getHumanLongAnnotation() {
        return this.humanAnnotationLong;
    }
    
    public void setHumanAnnotation(String value, boolean rev)
    {
        setHumanAnnotation(value);
        reviewed = rev;
    }
    
    public boolean getReviewed(){
        return reviewed;
    }
    
    public void setHumanAnnotation(String value) {
        humanAnnotation = value;

        AnnotationValues av = getAnnValues(value);
        humanAnnotationLong = av.longVal;
        humanAnnotationShort = av.shortVal;
        
        predictedDiffUpdated = findPredDifferentUpdated();
        if(updateObserver != null)
        	updateObserver.update();
    }
        

    public String getPredictedAnnotation() {
        return predictedAnnotation;
    }

    public boolean setPredictedAnnotation(String value) {
        if(value == null)
            return false;
        
        
        
        AnnotationValues av = getAnnValues(value);
        predictedAnnotationLong = av.longVal;
        predictedAnnotationShort = av.shortVal;
        if(reviewed && av.longVal == AnnotationLongValue.NeedsReview)
            return false;
        predictedAnnotation = value;
        revisedCount++;

        predictedDiffUpdated = findPredDifferentUpdated();

        if(updateObserver != null)
        	updateObserver.update();
            return true;
    }

    public String getUpdatedAnnotation()
    {
        return updatedAnnotation;
    }

    public void setUpdatedAnnotation(String value, boolean rev){
        setUpdatedAnnotation(value);
        reviewed = rev;
    }
    
    public void setUpdatedAnnotation(String value){
        updatedAnnotation = value;
        
        AnnotationValues av = getAnnValues(value);
        updatedAnnotationLong = av.longVal;
        updatedAnnotationShort = av.shortVal;
        
        if(predictedAnnotationLong == AnnotationLongValue.Unknown)
        {
            if(humanAnnotationLong == av.longVal)
            {
                reviewed = true;
            }
        }

        predictedDiffUpdated = findPredDifferentUpdated();

        if(updateObserver != null)
        	updateObserver.update();
    }

    public String findPredDifferentUpdated() {
        if (updatedAnnotationLong != AnnotationLongValue.Unknown)
            return PredictionCode.Corrected.toString();
        else if (humanAnnotationLong == predictedAnnotationLong)
            return PredictionCode.SameNoUpdate.toString();
        else if (humanAnnotationShort == predictedAnnotationShort)
            return PredictionCode.SameNoUpdate.toString();
        else if (humanAnnotationShort == AnnotationShortValue.Unknown || predictedAnnotationShort == AnnotationShortValue.Unknown)
            return PredictionCode.SameNoUpdate.toString();
        else
            return PredictionCode.Different.toString();

    }

    public String getPredDifferentUpdated() {
        return predictedDiffUpdated;
    }

    public boolean IsNeedsReview()
    {
        return predictedAnnotation.contains("NeedsReview");
    }
    
    public boolean doAnnotationsMatch() {
        return (humanAnnotationLong == predictedAnnotationLong) ||
            (humanAnnotationShort == predictedAnnotationShort && predictedAnnotationLong == AnnotationLongValue.Unknown) ||
            (humanAnnotationShort == predictedAnnotationShort && humanAnnotationLong == AnnotationLongValue.Unknown);
    }

    public void Register(UpdateObserver obs) {
    	updateObserver = obs;
    }

	@Override
	public int compareTo(FrameAnnotationInfo o) {
    	FrameAnnotationInfo other = (FrameAnnotationInfo)o;

    	return frameNo > other.getFrameNo() ? +1 : frameNo < other.getFrameNo() ? -1 : 0;
	}

	public boolean hasBothAnnotations() {
		return humanAnnotation != null && humanAnnotation.trim().length() > 0 
				&& predictedAnnotation != null && predictedAnnotation.length() > 0;
	}
}