package web.weka.model;

/**Usage: server will look at this to determine if need to remove from memory, when max size of memory reached */
public class ModelMeta {
	private String name = null;
	private String type = null;
	private int useCount = 0;
	
	public void increment() {
		this.useCount = this.useCount + 1;
	}
	
	public ModelMeta(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getUseCount() {
		return useCount;
	}
	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}
	
	
}
