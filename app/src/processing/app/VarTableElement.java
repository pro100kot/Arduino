package processing.app;

public class VarTableElement {
	String name;
	String value;
	public VarTableElement(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (((VarTableElement)obj).name.equals(name) &&
			((VarTableElement)obj).value.equals(value));
	}
	
}
