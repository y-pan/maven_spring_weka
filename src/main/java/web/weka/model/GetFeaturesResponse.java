package web.weka.model;

import java.util.ArrayList;

/**
 * Usage: {"error": String, "data": ArrayList<Feature>}
 * */
public class GetFeaturesResponse {

	private String error = null;
	private String modelRelation = null; // @RELATION in .arff, defines the model
	private String modelType = null;
	private ArrayList<Feature> data = null;

	public String getModelRelation() {
		return modelRelation;
	}

	public void setModelRelation(String modelRelation) {
		this.modelRelation = modelRelation;
	}

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
	public GetFeaturesResponse(String error){
		this.error = error;
	}
	
	public GetFeaturesResponse(ArrayList<Feature> data){
		this.data = data;
	}
	public GetFeaturesResponse(ArrayList<Feature> data, String relation, String modelType){
		this.data = data;
		this.modelRelation = relation;
		this.modelType = modelType;
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
