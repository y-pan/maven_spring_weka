package web.weka.model;

public interface IPrediction {

	public String getPredictionName();
	public double getPredictionValue();
	public double getPredictionConfidence();

	
	public void setPredictionName(String name);
	public void setPredictionValue(double value);
	public void setPredictionConfidence(double confidence);
}
