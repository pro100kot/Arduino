package gdbremoteserver;

public interface BreakpointListener {
	void onBreakpointError();
	
	void onBreakpointSet();
}
