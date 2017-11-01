package web.weka.model;

import java.util.Date;

/**Usage: server will look at this to determine if need to remove from memory, when max size of memory reached */
public class ModelMeta {
	private String modelRelation = null; // @RELATION
	private String modelType = null; // %TYPE
	private int useCount = 0;
	private Date lastUsed;
	
	public void mark() {
		this.useCount = this.useCount + 1;
		this.lastUsed = new Date();
		System.out.println(toString());
	}
	
	public ModelMeta(String relation, String modelType) {
		super();
		this.modelRelation = relation;
		this.modelType = modelType;
		mark();
	}

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
	public int getUseCount() {
		return useCount;
	}

	@Override
	public String toString() {
		return "ModelMeta [modelRelation=" + modelRelation + ", modelType=" + modelType + ", useCount=" + useCount
				+ ", lastUsed=" + lastUsed + "]";
	}


	
}
