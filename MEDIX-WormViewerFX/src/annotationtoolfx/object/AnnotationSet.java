package annotationtoolfx.object;

import java.util.Date;

public class AnnotationSet {

	private String name;
	private String setId;
	private String userName;
	private String strainTypeId;
	private Date annotationStartDate;

	public AnnotationSet(String name, String setId, String userName, String stainTypeId, Date startDate) {
		this.setName(name);
		this.setSetId(setId);
		this.setAnnotationStartDate(startDate);
		this.setUserName(userName);
		this.setStrainTypeId(stainTypeId);
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getSetId() {
		return setId;
	}

	private void setSetId(String setId) {
		this.setId = setId;
	}

	public String getUserName() {
		return userName;
	}

	private void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStrainTypeId() {
		return strainTypeId;
	}

	private void setStrainTypeId(String strainTypeId) {
		this.strainTypeId = strainTypeId;
	}

	public Date getAnnotationStartDate() {
		return annotationStartDate;
	}

	private void setAnnotationStartDate(Date annotationStartDate) {
		this.annotationStartDate = annotationStartDate;
	}

	
	
}
