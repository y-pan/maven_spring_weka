package web.weka.model;

public class Feature {
	
	private int index;
	private String name;
	private String type;
	private String[] options = null;

	public Feature() {
		
	}

	public Feature(int index, String name, String type) {
		super();
		this.index = index;
		this.name = name;
		this.type = type;
	}
	
	public Feature(int index, String name, String type, String[] options) {
		super();
		this.index = index;
		this.name = name;
		this.type = type;
		this.options = options;
	}
	
	public int getIndex ()
    {
        return index;
    }
	public void setIndex(int index) {
		this.index = index;
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

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

    @Override
    public String toString()
    {
        return "ClassPojo [index = "+index+", name = "+name+", type = "+type+", options = "+options+"]";
    }
}