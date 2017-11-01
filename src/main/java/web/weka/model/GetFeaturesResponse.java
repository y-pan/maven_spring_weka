package web.weka.model;

import java.util.ArrayList;

/**
 * Usage: {"error": String, "data": ArrayList<Feature>}
 * */
public class GetFeaturesResponse {

	private String error = null;
	private ArrayList<Feature> data = null;
	
	public GetFeaturesResponse(String error){
		this.error = error;
	}
	
	public GetFeaturesResponse(ArrayList<Feature> data){
		this.data = data;
	}
	public GetFeaturesResponse(String error, ArrayList<Feature> data){
		this.error = error;
		this.data = data;
	}
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public ArrayList<Feature> getData() {
		return data;
	}
	public void setData(ArrayList<Feature> data) {
		this.data = data;
	}
	
}
