package web.weka.model;

public interface IPredictResponse {

	 String getError();
	 void setError(String error);
	 IPrediction getData();
	 void setData(IPrediction data);

}
