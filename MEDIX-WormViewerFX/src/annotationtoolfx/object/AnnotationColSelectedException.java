package annotationtoolfx.object;


@SuppressWarnings("serial")
public class AnnotationColSelectedException extends Exception {
    private String annotationsFile;
    private boolean expert;

    public AnnotationColSelectedException()
    {
    }

    public AnnotationColSelectedException(String message) 
    {
    	super(message);
    }

    public AnnotationColSelectedException(String message, Exception innerException) 
    {
    	super(message, innerException);
    }

    public AnnotationColSelectedException(boolean expert, String annotationsFile)  
    {
    	this("Annotation column was not selected for file: " + annotationsFile);
    	this.expert = expert;
        this.annotationsFile = annotationsFile;
    }

    public String getAnnotationsFile()
    {
        return annotationsFile;
    }

    public boolean getExpert()
    {
        return expert;
    }
}