package main.java.AdaptiveWRFTracingTool.bean;

public class Keyword {

	private String name;
	private String parentname;
	private String type;
	
	public Keyword(String n, String p, String t) {
		name = n;
		parentname = p;
		type = t;
	}
	
	public String getName() {
		return name;
	}
	
	public String getParentname() {
		return parentname;
	}
	
	public String getType() {
		return type;
	}

}
