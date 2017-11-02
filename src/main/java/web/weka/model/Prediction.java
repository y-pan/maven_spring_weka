package web.weka.model;

public class Prediction implements IPrediction{
	
	private String predictionName = "";
	private double predictionValue = -99999; // -1 -> not set
	private double predictionConfidence = -99999; // -1 -> not set

	public Prediction() {}
	
	public Prediction(String predictionName) {
		this.predictionName = predictionName;
	}
	
	public Prediction(double predictionValue) {
		this.predictionValue = predictionValue;
	}

	public Prediction(String predictionName, double predictionValue) {
		this.predictionName = predictionName;
		this.predictionValue = predictionValue;
	}

	
	public Prediction(String predictionName, double predictionValue, double predictionConfidence) {
		this.predictionName = predictionName;
		this.predictionValue = predictionValue;
		this.predictionConfidence = predictionConfidence;
	}

	@Override
	public String getPredictionName() {
		
		return this.predictionName;
	}

	@Override
	public double getPredictionValue() {
		return this.predictionValue;
	}

	@Override
	public double getPredictionConfidence() {
		return this.predictionConfidence;
	}

	@Override
	public void setPredictionName(String name) {
		this.predictionName = name;
		
	}

	@Override
	public void setPredictionValue(double value) {
		this.predictionValue = value;
	}

	@Override
	public void setPredictionConfidence(double confidence) {
		this.predictionConfidence = confidence;
	}

}