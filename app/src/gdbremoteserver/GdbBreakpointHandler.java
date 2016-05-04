package gdbremoteserver;

import processing.app.GdbDebugProcess;
import uk.co.cwspencer.gdb.Gdb;
import uk.co.cwspencer.gdb.messages.GdbBreakpoint;
import uk.co.cwspencer.gdb.messages.GdbErrorEvent;
import uk.co.cwspencer.gdb.messages.GdbEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class GdbBreakpointHandler
{
	private Gdb m_gdb;
	private GdbDebugProcess m_debugProcess;

	// The breakpoints that have been set and their GDB breakpoint numbers
	private final BiMap<Integer, LineBreakpoint>
		m_breakpoints = HashBiMap.create();

	public GdbBreakpointHandler(Gdb gdb, GdbDebugProcess debugProcess)
	{
		m_gdb = gdb;
		m_debugProcess = debugProcess;
	}

	/**
	 * Registers the given breakpoint with GDB.
	 * @param breakpoint The breakpoint.
	 */
	public void registerBreakpoint(
		final LineBreakpoint breakpoint)
	{
		// TODO: I think we can use tracepoints here if the suspend policy isn't to stop the process

		// Check if the breakpoint already exists
		Integer number = findBreakpointNumber(breakpoint);
		if (number != null)
		{
			// Re-enable the breakpoint
			m_gdb.sendCommand("-break-enable " + number);
		}
		else
		{
			// Set the breakpoint
			String fileName = breakpoint.getFileName();//путь к файлу
			String command = "-break-insert -f " + fileName + ":" +
				(breakpoint.getLine() + 1);
			m_gdb.sendCommand(command, new Gdb.GdbEventCallback()
				{
					@Override
					public void onGdbCommandCompleted(GdbEvent event)
					{
						onGdbBreakpointReady(event, breakpoint);
					}
				});
		}
	}

	/**
	 * Unregisters the given breakpoint with GDB.
	 * @param breakpoint The breakpoint.
	 * @param temporary Whether we are deleting the breakpoint or temporarily disabling it.
	 */
	public void unregisterBreakpoint(LineBreakpoint breakpoint,
		boolean temporary)
	{
		Integer number = findBreakpointNumber(breakpoint);
		if (number == null)
		{
			System.out.println("Cannot remove breakpoint; could not find it in breakpoint table");
			return;
		}

		if (!temporary)
		{
			// Delete the breakpoint
			m_gdb.sendCommand("-break-delete " + number);
			synchronized (m_breakpoints)
			{
				m_breakpoints.remove(number);
			}
		}
		else
		{
			// Disable the breakpoint
			m_gdb.sendCommand("-break-disable " + number);
		}
	}

	/**
	 * Finds a breakpoint by its GDB number.
	 * @param number The GDB breakpoint number.
	 * @return The breakpoint, or null if it could not be found.
	 */
	public LineBreakpoint findBreakpoint(int number)
	{
		synchronized (m_breakpoints)
		{
			return m_breakpoints.get(number);
		}
	}

	/**
	 * Finds a breakpoint's GDB number.
	 * @param breakpoint The breakpoint to search for.
	 * @return The breakpoint number, or null if it could not be found.
	 */
	public Integer findBreakpointNumber(LineBreakpoint breakpoint)
	{
		Integer number;
		synchronized (m_breakpoints)
		{
			
			number = m_breakpoints.inverse().get(breakpoint);
		}

		if (number == null)
		{
			return null;
		}
		return number;
	}

	/**
	 * Callback function for when GDB has responded to our breakpoint request.
	 * @param event The event.
	 * @param breakpoint The breakpoint we tried to set.
	 */
	private void onGdbBreakpointReady(GdbEvent event,
		LineBreakpoint breakpoint)
	{
		if (event instanceof GdbErrorEvent)
		{
			m_debugProcess.onBreakpointError();
			/*m_debugProcess.getSession().updateBreakpointPresentation(breakpoint,
				AllIcons.Debugger.Db_invalid_breakpoint, ((GdbErrorEvent) event).message);*/
			return;
		}
		if (!(event instanceof GdbBreakpoint))
		{
			m_debugProcess.onBreakpointError();
			/*m_debugProcess.getSession().updateBreakpointPresentation(breakpoint,
				AllIcons.Debugger.Db_invalid_breakpoint, "Unexpected data received from GDB");
			m_log.warn("Unexpected event " + event + " received from -break-insert request");*/
			return;
		}

		// Save the breakpoint
		GdbBreakpoint gdbBreakpoint = (GdbBreakpoint) event;
		if (gdbBreakpoint.number == null)
		{
			m_debugProcess.onBreakpointError();
			/*m_debugProcess.getSession().updateBreakpointPresentation(breakpoint,
				AllIcons.Debugger.Db_invalid_breakpoint, "No breakpoint number received from GDB");
			m_log.warn("No breakpoint number received from GDB after -break-insert request");*/
			return;
		}

		synchronized (m_breakpoints)
		{
			m_breakpoints.put(gdbBreakpoint.number, breakpoint);
		}
		m_debugProcess.onBreakpointSet();
		// Mark the breakpoint as set
		// TODO: Don't do this yet if the breakpoint is pending
		/*m_debugProcess.getSession().updateBreakpointPresentation(breakpoint,
			AllIcons.Debugger.Db_verified_breakpoint, null);*/
	}
}
