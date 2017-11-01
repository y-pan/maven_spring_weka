package web.weka.model;


public class ModelMetaMap {

	private String key;
	private ModelMeta data;
	
	public ModelMetaMap(String key, ModelMeta data) {
		this.key = key;
		this.data = data;
	}
	public String getKey() {
		return this.key;
	}
	public ModelMeta getValue() {
		return data;
	}
	
}
