package web.weka.model;

import java.util.ArrayList;


/**
 * Usage: {"error": String, "data": ArrayList<Feature>}
 * */
public class PredictResponse implements IPredictResponse {

	private String error = null;
	private IPrediction data = null;
	
	/*private String predictionName = null;
	private double predictionValue = -1; // -1 -> not set
	private double predictionConfidence = -1; // -1 -> not set*/
	public PredictResponse() {}
	public PredictResponse(String error) {
		this.error = error;
	}
	public PredictResponse(IPrediction data) {
		this.data = data;
	}
	
	@Override
	public String getError() {
		return error;
	}
	@Override
	public void setError(String error) {
		this.error = error;
	}
	@Override
	public IPrediction getData() {
		return data;
	}
	@Override
	public void setData(IPrediction data) {
		this.data = data;
	}
	
}
