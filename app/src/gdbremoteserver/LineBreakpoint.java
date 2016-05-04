package gdbremoteserver;

public class LineBreakpoint {

	//Name WITH path
	private String fileName;
	public String getFileName() {
		return fileName;
	}

	public int getLine() {
		return line;
	}

	private int line;
	
	public LineBreakpoint(String fileName, int line) {
		this.fileName = fileName;
		this.line = line;
	}
	
	@Override
	public String toString() {
		return "[" + fileName + ", " + line + "]";
		
	}
	
	@Override
	public boolean equals(Object obj) {
		return (fileName.equals(((LineBreakpoint)obj).getFileName()) &&
				line == ((LineBreakpoint)obj).getLine());
	}
	
	@Override
	public int hashCode() {
		return fileName.hashCode() + 3*line;
	}
	
	
}
