package web.weka.model;

public class PredictRequest {

	private String modelType = null;
	private String[] data = null;
	
	public PredictRequest() {}
	
	public PredictRequest(String modelType, String[] data) {
		super();
		this.modelType = modelType;
		this.data = data;
	}
	
	public String getModelType() {
		return modelType;
	}
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	public String[] getData() {
		return data;
	}
	public void setData(String[] data) {
		this.data = data;
	}
	
	
	
}
