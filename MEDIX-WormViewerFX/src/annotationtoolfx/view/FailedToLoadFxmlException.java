package annotationtoolfx.view;

@SuppressWarnings("serial")
public class FailedToLoadFxmlException extends Exception{

	String filePath;
	
	public FailedToLoadFxmlException(String filePath, Exception e) {
		super("Failed to load FXML file: " + filePath, e);
		this.filePath = filePath;
	}
	
	
	
}
