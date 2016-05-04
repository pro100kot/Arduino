package processing.app;

import gdbremoteserver.BreakpointListener;
import gdbremoteserver.GdbBreakpointHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;


import uk.co.cwspencer.gdb.Gdb;
import uk.co.cwspencer.gdb.Gdb.GdbEventCallback;
import uk.co.cwspencer.gdb.GdbListener;
import uk.co.cwspencer.gdb.gdbmi.GdbMiList;
import uk.co.cwspencer.gdb.gdbmi.GdbMiResult;
import uk.co.cwspencer.gdb.gdbmi.GdbMiResultRecord;
import uk.co.cwspencer.gdb.gdbmi.GdbMiStreamRecord;
import uk.co.cwspencer.gdb.gdbmi.GdbMiValue;
import uk.co.cwspencer.gdb.messages.GdbConnectedEvent;
import uk.co.cwspencer.gdb.messages.GdbDoneEvent;
import uk.co.cwspencer.gdb.messages.GdbEvent;
import uk.co.cwspencer.gdb.messages.GdbRunningEvent;
import uk.co.cwspencer.gdb.messages.GdbStoppedEvent;
import uk.co.cwspencer.gdb.messages.GdbStoppedEvent.Reason;
import uk.co.cwspencer.gdb.messages.GdbVariableObject;

public class GdbDebugProcess implements GdbListener, BreakpointListener{

	private Gdb m_gdb;
	private GdbBreakpointHandler m_breakpointHandler;
	private String host;
	private int port;
	private String elfFilePath;
	private Editor editor;
	private TracingHandler tracingHandler; 
	private ArrayList<VarTableElement> registers;
	private ArrayList<VarTableElement> debugVariables;
	
	public GdbBreakpointHandler getBreakpointHandler() {
		return m_breakpointHandler;
	}

	public GdbDebugProcess(Editor editor, String _host, int _port, String _elfFilePath) {
		host = _host;
		port = _port;
		elfFilePath = _elfFilePath;
		tracingHandler = editor.getTracingHandler();
		this.editor = editor;
		registers = new ArrayList<VarTableElement>();
		debugVariables = new ArrayList<VarTableElement>();
		// Prepare GDB
		m_gdb = new Gdb("avr-gdb", editor.getSketch().getFolder().getAbsolutePath(), this);
		
		// Create the breakpoint handler
		m_breakpointHandler = new GdbBreakpointHandler(m_gdb, this);
		
		// Launch the process
		m_gdb.start();
		//m_gdb.sendCommand("target remote localhost:4242");

		m_gdb.sendCommand("target remote " + host+ ":" + port, new GdbEventCallback() {
			@Override
			public void onGdbCommandCompleted(GdbEvent event) {
				System.out.println("onGdbCommandCompleted (target remote " + host + ":" + port + ")");
				
			}
		});
		
		m_gdb.sendCommand("file " + elfFilePath, new GdbEventCallback() {
			@Override
			public void onGdbCommandCompleted(GdbEvent event) {
				System.out.println("file command complete"); 
				
			}
		});
		
		/*m_gdb.sendCommand("-break-insert Test.cpp:20", new GdbEventCallback() {
			
			@Override
			public void onGdbCommandCompleted(GdbEvent event) {
				System.out.println("-break-insert command complete"); 
				
			}
		});*/
		
		//resume();
	}
	
	public void goToStartPosition(String mainFilename){
		m_gdb.sendCommand("-break-insert " + mainFilename + ":setup", new GdbEventCallback() {
			
			@Override
			public void onGdbCommandCompleted(GdbEvent event) {
				System.out.println("-break-insert command complete"); 
				
			}
		});
	}
	
	/**
	 * Resumes program execution.
	 */
	public void resume()
	{ 
		m_gdb.sendCommand("-exec-continue"); //change to correct gdb/mi command
	}
	
	/**
	 * Steps over the next line.
	 */
	public void startStepOver()
	{
		m_gdb.sendCommand("-exec-next");
	}

	/**
	 * Steps into the next line.
	 */
	public void startStepInto()
	{
		m_gdb.sendCommand("-exec-step");
	}

	/**
	 * Steps out of the current function.
	 */
	public void startStepOut()
	{
		m_gdb.sendCommand("-exec-finish");
	}
	
	public void getRegistersValue(){
		//m_gdb.sendCommand("-data-read-memory 0x38 x 1 5 1");
	}

	/**
	 * Stops program execution and exits GDB.
	 */
	public void stop()
	{
		m_gdb.sendCommand("-gdb-exit");
	}	
	
	@Override
	public void onGdbError(Throwable ex) {
		System.out.println(ex.toString());
		for(int i=0; i<ex.getStackTrace().length; i++)
			System.out.println("\t" + ex.getStackTrace()[i]);
	}

	@Override
	public void onGdbStarted() {
		System.out.println("GdbStarted");
		
	}

	@Override
	public void onGdbCommandSent(String command, long token) {
		System.out.println("CommandSent " + token + " command:" + command);
	}

	@Override
	public void onGdbEventReceived(GdbEvent event) {
		// TODO Auto-generated method stub
		if(event instanceof GdbStoppedEvent){
			GdbStoppedEvent stopEvent = (GdbStoppedEvent)event;
			if(stopEvent.reason != null)
				if(stopEvent.reason.equals(Reason.BreakpointHit) ||
					stopEvent.reason.equals(Reason.EndSteppingRange) ||
					stopEvent.reason.equals(Reason.FunctionFinished)){
					tracingHandler.deselectAllLines();
					tracingHandler.selectLine(stopEvent.frame.fileAbsolute, stopEvent.frame.line);
					editor.debugToolbar.targetIsStopped();
			}
			//System.out.println("event instanceof GdbStoppedEvent");
			m_gdb.getVariablesForFrame(1, 0, new GdbEventCallback() {
				@Override
				public void onGdbCommandCompleted(GdbEvent event) {
					if(m_gdb.m_variableObjectsByExpression.size()>0){
						debugVariables.clear();
						Iterator<Entry<String,GdbVariableObject>> it = m_gdb.m_variableObjectsByExpression.entrySet().iterator();
						System.out.println(m_gdb.m_variableObjectsByExpression.entrySet());
						while(it.hasNext()){
							Entry<String,GdbVariableObject> cur = it.next();
							System.out.println("<!>"+cur.getValue().type + " " + cur.getValue().toString());
							debugVariables.add(new VarTableElement(cur.getKey()+"", cur.getValue().value + ""));
						}
						//editor.varFrame.redraw(debugVariables);

						//System.out.println(m_gdb.m_variableObjectsByName);
					}
					m_gdb.sendCommand("-data-read-memory 0x30 x 1 12 1",new GdbEventCallback() {
						@Override
						public void onGdbCommandCompleted(GdbEvent event) {
							ArrayList<VarTableElement> list = new ArrayList<VarTableElement>(debugVariables);
							list.addAll(convertRegistersNames());
							editor.varFrame.redraw(list);
						}
					});
				}
			});

			//getRegistersValue();
		}
		else if(event instanceof GdbRunningEvent){
			editor.debugToolbar.targetIsRunning();
			System.out.println("Target has started");
		}
		else if(event instanceof GdbConnectedEvent){
			
			System.out.println("Connected to target");
		}
	}

	private ArrayList<VarTableElement> convertRegistersNames(){
		ArrayList<VarTableElement> list = new ArrayList<VarTableElement>();
		String names[] = {	"PIND", "DDRD", "PORTD",
							"PINC", "DDRC", "PORTC",
							"PINB", "DDRB", "PORTB",
							"PINA", "DDRA", "PORTA"};
		for(int i=0;i<registers.size();i++){
			list.add(new VarTableElement(names[i], registers.get(i).value.substring(1, registers.get(i).value.length()-1)));
		}
		return list;
	}
	
	/**
	 * Called when a stream record is received.
	 * @param record The record.
	 */
	@Override
	public void onStreamRecordReceived(GdbMiStreamRecord record) {
		// Log the record
		switch (record.type)
		{
		case Console:
			StringBuilder sb = new StringBuilder();
			if (record.userToken != null)
			{
				sb.append("<");
				sb.append(record.userToken);
				sb.append(" ");
			}
			sb.append(record.message);
			System.out.println("onStreamRecordReceived Console: " + sb.toString());
			break;

		case Target:
			System.out.println("onStreamRecordReceived Target: " + record.message);
			break;

		case Log:
			System.out.println("onStreamRecordReceived Log: " + record.message);
			break;
		}		
	}

	/**
	 * Called when a result record is received.
	 * @param record The record.
	 */
	@Override
	public void onResultRecordReceived(GdbMiResultRecord record) {
		
		
		// Log the record
		StringBuilder sb = new StringBuilder();
		sb.append("DameTime");
		sb.append(" ");
		if (record.userToken != null)
		{
			sb.append("<");
			sb.append(record.userToken);
			sb.append(" ");
		}
		else
		{
			sb.append("< ");
		}

		switch (record.type)
		{
		case Immediate:
			sb.append("[immediate] ");
			if(record.results!=null && record.results.size()>0)
				handleDataMemory(record.results);
			break;

		case Exec:
			sb.append("[exec] ");
			break;

		case Notify:
			sb.append("[notify] ");
			break;

		case Status:
			sb.append("[status] ");
			break;
		}

		sb.append(record);
		sb.append("\n");
		System.out.println("onResultRecordReceived" + sb.toString());
	}

	@Override
	public void onBreakpointError() {
		System.out.println("Breakpoint Error");
		
	}

	@Override
	public void onBreakpointSet() {
		System.out.println("Breakpoint Set succesfuly");
		
	}
	
	private void handleDataMemory(List<GdbMiResult> results){
		GdbMiList memory = null;
		Iterator<GdbMiResult> itRes = results.iterator();
		while(itRes.hasNext()){
			GdbMiResult cur = itRes.next();
			if(cur.variable.equals("memory"))
				memory = cur.value.list;
		}
		if(memory == null)
			return;
		Iterator<GdbMiValue> itVal = memory.values.iterator();
		registers.clear();
		while(itVal.hasNext()){
			GdbMiValue cur = itVal.next();
			if(cur.tuple != null){
				registers.add(new VarTableElement(cur.tuple.get(0).value.toString(), cur.tuple.get(1).value.list.values.get(0).toString()));
			}
			
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		m_gdb = null;
	}

}
