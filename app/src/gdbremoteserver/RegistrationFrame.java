package gdbremoteserver;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import processing.app.Editor;
import processing.app.PreferencesData;

import avrdebug.communication.Message;
import avrdebug.communication.Messenger;
import avrdebug.communication.SimpleDeviceInfo;
import avrdebug.communication.SimpleReserveItem;

import static processing.app.I18n.tr;

public class RegistrationFrame extends JFrame {
	private static final long serialVersionUID = 3697472927362661583L;
	private JTextField startDate;
	private JTextField startTime;
	private GregorianCalendar startDateTime;
	private GregorianCalendar endDateTime;
	private JTextField endDate;
	private JTextField endTime;
	private JComboBox<Integer> timeComboBox;
	private JTextField mcuNumber;
	private JPanel schedule;
	private JScrollPane scheduleScroll;
	private JLabel statusLabel;
	private SortedSet<SimpleReserveItem> reserve = new TreeSet<>();
	private ArrayList<SimpleDeviceInfo> devices = new ArrayList<>();
	private JButton registration;
	private Editor editor;
	
	public RegistrationFrame(Editor editor) {
		super(tr("Registration"));
		this.editor = editor;
		schedule = new SchedulePanel(reserve, devices,this);
		statusLabel = new JLabel();
		createGui();
	}
	
	@Override
	public void setVisible(boolean b) {
		if(b){
			reloadData();
			startDateTime = null;
			startDate.setText("");
			startTime.setText("");
			endDate.setText("");
			endTime.setText("");
			mcuNumber.setText("");
			timeComboBox.setSelectedIndex(0);
		}
		super.setVisible(b);
	}
	
	private void reloadData(){
		Socket s;
		scheduleScroll.setVisible(false);
		getContentPane().remove(statusLabel);
		statusLabel.setText("<html><b>" + tr("Downloading data") +"</b></html>");
		getContentPane().add(statusLabel, BorderLayout.SOUTH);
		revalidate();
		try {
			s = new Socket(PreferencesData.get("debug.server.address", "localhost"), PreferencesData.getInteger("debug.server.port",3129));
			Messenger.writeMessage(s, new Message("GET"));
			reserve.clear();
			SortedSet<SimpleReserveItem> set = Messenger.readSimpleReserveItemSet(s);
			if(set == null)
				throw new IOException();
			reserve.addAll(set);
			devices.clear();
			ArrayList<SimpleDeviceInfo> list = Messenger.readSimpleDeviceInfoList(s);
			if(list == null)
				throw new IOException();
			devices.addAll(list);
			getContentPane().remove(statusLabel);
			scheduleScroll.setVisible(true);
			revalidate();
			s.close();
		} catch (IOException e) {
			statusLabel.setText("<html><b>" + tr("Connection error") + "</b></html>");
			revalidate();
		}
	}
	
	private void createGui(){
		//configuration frame
		setSize(700, 700);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		//Calendar view
		scheduleScroll = new JScrollPane(schedule);
		scheduleScroll.setPreferredSize(new Dimension(500, 500));
		scheduleScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		getContentPane().add(scheduleScroll, BorderLayout.CENTER);
		//left elements
		mcuNumber = new JTextField(4);
		mcuNumber.setEditable(false);
		startDate = new JTextField();
		startDate.setPreferredSize(new Dimension(60, 25));
		startDate.setEditable(false);
		startTime = new JTextField();
		startTime.setPreferredSize(new Dimension(40, 25));
		startTime.setEditable(false);
		timeComboBox = new JComboBox<>();
		for(int i=1; i<=8; i++)
			timeComboBox.addItem(i*15);
		timeComboBox.setSelectedIndex(0);
		timeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateEndDateTime();
			}
		});
		endDate = new JTextField();
		endDate.setPreferredSize(new Dimension(60, 25));
		endDate.setEditable(false);
		endTime = new JTextField();
		endTime.setPreferredSize(new Dimension(40, 25));
		endTime.setEditable(false);
		registration = new JButton(tr("Registration")+"!");
		registration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Socket s;
				try {
					if(startDateTime == null)
						throw new NullPointerException();
					s = new Socket(PreferencesData.get("debug.server.address", "localhost"), PreferencesData.getInteger("debug.server.port",3129));
					Messenger.writeMessage(s, new Message("ADD"));
					int mcu = Integer.parseInt(mcuNumber.getText());
					Messenger.writeSimpleReserveItem(s, new SimpleReserveItem(mcu, startDateTime, endDateTime));
					Message message = Messenger.readMessage(s);
					if(message == null)
						throw new IOException();
					if(message.getParameter()!=0)
						throw new NullPointerException();
					statusLabel.setText("<html><b>"+tr("Ok")+"</b></html>");
					JOptionPane.showMessageDialog(null, "<html>" + tr("Your access key:")+"<br><font size=+2>" + message.getText() + "</font><br>"+tr("Save it or remember.")+"</html>", tr("Registered successfully"), JOptionPane.INFORMATION_MESSAGE);
					editor.setDebugKey(message.getText());
					reloadData();
					s.close();
				} catch (IOException e) {
					statusLabel.setText("<html><b>"+tr("Connection error")+"</b></html>");
					getContentPane().add(statusLabel, BorderLayout.SOUTH);
				}catch (NullPointerException e) {
					statusLabel.setText("<html><b>"+tr("Date error")+"</b></html>");
					getContentPane().add(statusLabel, BorderLayout.SOUTH);
				}catch (Exception e) {
					statusLabel.setText("<html><b>"+tr("Error")+"</b></html>");	
					getContentPane().add(statusLabel, BorderLayout.SOUTH);
				}
				
				revalidate();
			}
		});
		Box box = Box.createVerticalBox();
		//MCU number
		Box box0 = Box.createHorizontalBox();
		box0.add(new JLabel(tr("MCU number:")));
		box0.add(Box.createHorizontalStrut(5));
		box0.add(Box.createHorizontalGlue());
		box0.add(mcuNumber);
		//Start time
		Box box1 = Box.createHorizontalBox();
		box1.add(new JLabel(tr("Start time:")));
		box1.add(Box.createHorizontalStrut(5));
		box1.add(startDate);
		box1.add(Box.createHorizontalStrut(5));
		box1.add(startTime);
		//Session time 
		Box box2 = Box.createHorizontalBox();
		box2.add(new JLabel(tr("Session time:")));
		box2.add(Box.createHorizontalGlue());
		box2.add(timeComboBox);
		//End time
		Box box3 = Box.createHorizontalBox();
		box3.add(new JLabel(tr("End time:")));
		box3.add(Box.createHorizontalStrut(5));
		box3.add(endDate);
		box3.add(Box.createHorizontalStrut(5));
		box3.add(endTime);
		//Left side
		box.add(box0);
		box.add(Box.createVerticalStrut(10));
		box.add(box1);
		box.add(Box.createVerticalStrut(10));
		box.add(box2);
		box.add(Box.createVerticalStrut(10));
		box.add(box3);
		box.add(Box.createVerticalStrut(10));
		box.add(registration);
		box.add(Box.createVerticalStrut(460));
		
		box.setPreferredSize(new Dimension(330, 300));
		box.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(box,BorderLayout.WEST);
		//pack();
	}
	
	public void setTimeParams(int mcuNumber, GregorianCalendar startDateTime){
		this.mcuNumber.setText(mcuNumber+"");
		setStartDateTime(startDateTime);
	}
	
	private void setStartDateTime(GregorianCalendar c){
		startDateTime = c;
		startDate.setText(getDateString(startDateTime));
		startTime.setText(getTimeString(startDateTime));
		updateEndDateTime();
	}
	
	private void updateEndDateTime(){
		if(startDateTime == null)
			return;
		endDateTime = (GregorianCalendar) startDateTime.clone();
		endDateTime.add(Calendar.MINUTE, (Integer)timeComboBox.getSelectedItem());
		endDate.setText(getDateString(endDateTime));
		endTime.setText(getTimeString(endDateTime));
	}
	
	private String getDateString(GregorianCalendar c){
		String result = "";
		result += c.get(Calendar.DATE) + ".";
		result += (c.get(Calendar.MONTH)+1) + ".";
		result += (c.get(Calendar.YEAR));
		return result;
	}
	
	private String getTimeString(GregorianCalendar c){
		String result = "";
		result += c.get(Calendar.HOUR_OF_DAY) + ":";
		if(c.get(Calendar.MINUTE)<10)
			result+="0";
		result += c.get(Calendar.MINUTE);
		return result;
	}
	
}
