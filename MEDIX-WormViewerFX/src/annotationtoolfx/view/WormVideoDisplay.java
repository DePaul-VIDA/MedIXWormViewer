package annotationtoolfx.view;

import javafx.beans.property.SimpleStringProperty;

public class WormVideoDisplay {
		
		private final SimpleStringProperty wormType;
		private final SimpleStringProperty strainTypeId;
		private final SimpleStringProperty foodCond;
		private final SimpleStringProperty length;
		private final SimpleStringProperty numAnn;
		
		
		public WormVideoDisplay (String wormType, String strainTypeId, String foodCond, String length, String numAnn){
			this.wormType = new SimpleStringProperty(wormType);
			this.strainTypeId = new SimpleStringProperty(strainTypeId);
			this.foodCond = new SimpleStringProperty(foodCond);
			this.length = new SimpleStringProperty(length);
			this.numAnn = new SimpleStringProperty(numAnn);
		}
		
		public String getWormType() {
			return wormType.get();
		}
		
		public void setWormType(String value) {
			wormType.set(value);
		}

		public String getStrainTypeId() {
			return strainTypeId.get();
		}
		
		public void setStrainTypeId(String value) {
			strainTypeId.set(value);
		}
		
		public String getFoodCond() {
			return foodCond.get();
		}
		
		public void setFoodCond(String value) {
			foodCond.set(value);
		}			
					
		public String getLength() {
			return length.get();
		}
		
		public void setLength(String value) {
			length.set(value);
		}
		
		public String getNumAnn() {
			return numAnn.get();
		}
		
		public void setNumAnn(String value) {
			numAnn.set(value);
		}

	}
