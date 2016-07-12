package gdbremoteserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;

public class DebugServerCommunicator {

	
	String address;
	int port;
	public DebugServerCommunicator(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public int loadAndRun(File file, String key){
		Message message = null;
		try {		
			Socket s = new Socket(address, port);
			Messenger.writeMessage(s, new Message("LOAD"));
			Messenger.writeMessage(s, new Message(key));
			message = Messenger.readMessage(s);
			switch (message.getText()) {
			case "ACCESS_ERROR":
				System.out.println("Access error");
				return -4;
			}
			if(!message.getText().equals("OK")){
				return -3;
			}
			sendFile(s, file);
			message = Messenger.readMessage(s);
			s.close();
		} catch (IOException e) {
			return -1;
		}
		if(message.getText().equals("OK"))
			return message.getParameter();
		else return -2;

	}
	
	private void sendFile(Socket s, File file) throws IOException{
		OutputStream str = s.getOutputStream();
		DataOutputStream  dos = new DataOutputStream(str);
		dos.writeLong(file.length());
		RandomAccessFile rfile = new RandomAccessFile(file.getAbsolutePath(), "r");
		byte buff[] = new byte[128];
		int size;
		while((size = rfile.read(buff)) >0){
			dos.write(buff, 0, size);
		}
		rfile.close();
	}
		
}

