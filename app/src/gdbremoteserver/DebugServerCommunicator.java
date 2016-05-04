package gdbremoteserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class DebugServerCommunicator {
	
	private DebugServerCommand loadCommand = new DebugServerCommand("LOAD", (byte)0);
	private DebugServerCommand startCommand = new DebugServerCommand("STRT", (byte)0);
	
	String address;
	int port;
	public DebugServerCommunicator(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public int loadAndRun(File file){
		DebugServerCommand response = null;
		try {		
			Socket s = new Socket(address, port);
			OutputStream str = s.getOutputStream();
			DataOutputStream  dos = new DataOutputStream(str);
			dos.write(loadCommand.getData());
			dos.writeLong(file.length());
			RandomAccessFile rfile = new RandomAccessFile(file.getAbsolutePath(), "r");
			byte buff[] = new byte[128];
			int size;
			while((size = rfile.read(buff)) >0){
				dos.write(buff, 0, size);
			}
			rfile.close();
			dos.write(startCommand.getData());
			InputStream istr = s.getInputStream();
			DataInputStream dis = new DataInputStream(istr);
			byte resp[] = new byte[5];
			dis.read(resp);
			response = new DebugServerCommand(resp);
			s.close();
		} catch (IOException e) {
			return -1;
		}
		if(response.getCommand().equals("OKEY"))
			return 0;
		if(response.getCommand().equals("ERRR"))
			return 1;
		return 2;
	}
		
}

