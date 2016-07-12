package gdbremoteserver;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SortedSet;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import avrdebug.communication.SimpleDeviceInfo;
import avrdebug.communication.SimpleReserveItem;

public class SchedulePanel extends JPanel{
	private static final long serialVersionUID = -3103181360421043001L;
	private final int topMargin = 30;
	private final int mcuInfoBlockWidth = 120;
	private final int mcuInfoBlockHeight = 30;
	private final int timeBlockWidth = 40;
	private final int timeBlockHeight = 30;
	private final int maxHeight = 24*timeBlockHeight+mcuInfoBlockHeight+topMargin;
	private final int daysAhead = 3; 
	private JComboBox<String> comboBox;
	private ArrayList<GregorianCalendar> days;
	private SortedSet<SimpleReserveItem> reserve;
	private ArrayList<SimpleDeviceInfo> devices;
	private RegistrationFrame regFrame;
	public SchedulePanel(SortedSet<SimpleReserveItem> reserve, ArrayList<SimpleDeviceInfo> devices, RegistrationFrame regFrame) {
		this.regFrame = regFrame;
		this.reserve = reserve;
		this.devices = devices;
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				x-=timeBlockWidth;
				y-=(mcuInfoBlockHeight+topMargin);
				if(x<=0 || y<=0)
					return;
				x/=mcuInfoBlockWidth;
				if(x >= SchedulePanel.this.devices.size())
					return;
				int y1 = (y%timeBlockHeight)*2;
				//15min interval linking
				if(y1 < 15) 
					y1 = 0;
				else
					if(y1 < 30) 
					y1 = 15;
					else
						if(y1 < 45)
							y1 = 30;
						else
							y1 = 45;
				//
				y/=timeBlockHeight;
				GregorianCalendar c = (GregorianCalendar) days.get(comboBox.getSelectedIndex()).clone();
				c.set(Calendar.HOUR_OF_DAY, y);
				c.set(Calendar.MINUTE, y1);
				SchedulePanel.this.regFrame.setTimeParams(SchedulePanel.this.devices.get(x).getId(), c);
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		//setPreferredSize(new Dimension(300, maxHeight));
		//setPreferredSize(new Dimension(devices.getRowCount()*mcuInfoBlockWidth+timeBlockWidth, maxHeight));
		//setPreferredSize(new Dimension((devices.getRowCount()+1)*mcuInfoBlockWidth+timeBlockWidth, maxHeight));
		days = new ArrayList<>();
		comboBox = new JComboBox<>();
		this.add(comboBox);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				repaint();				
			}
		});
		updateComboBox();
		repaint();
	}
	
	private void updateComboBox(){
		comboBox.removeAllItems();
		days.clear();
		for(int i=0; i<daysAhead; i++){
			GregorianCalendar c = new GregorianCalendar();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.add(Calendar.DATE, i);
			days.add(c);
			comboBox.addItem(c.get(Calendar.DATE)+"."+(c.get(Calendar.MONTH)+1)+"."+ c.get(Calendar.YEAR));
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(new GregorianCalendar().getTimeInMillis() - days.get(0).getTimeInMillis() > 86400000L){
			updateComboBox();
		}
		Graphics2D g2d = (Graphics2D)g;
		paintMcuInfo(g2d, timeBlockWidth, topMargin);
		paintTimeScale(g2d, 0, mcuInfoBlockHeight+topMargin);
		paintTimeNet(g2d, timeBlockWidth, mcuInfoBlockHeight+topMargin);
		colorTime(g2d);
		setPreferredSize(new Dimension((devices.size()+1)*mcuInfoBlockWidth+timeBlockWidth, maxHeight));
		//repaint();
	}
	
	private void colorTime(Graphics2D g2d){
		g2d.setColor(new Color(135, 206, 235));
		int selected = comboBox.getSelectedIndex();
		GregorianCalendar startDayInterval = days.get(selected);
		GregorianCalendar endDayInterval =  (GregorianCalendar) startDayInterval.clone();
		endDayInterval.add(Calendar.DATE, 1);
		for(SimpleReserveItem cur : reserve){
			Calendar start = cur.getStartTime();
			Calendar end = cur.getEndTime();
			int mcuNumber = -1;
			for(int i=0;i<devices.size();i++){
				if(devices.get(i).getId() == cur.getMcuId()){
					mcuNumber = i;
					break;
				}
			}
			if(mcuNumber<0)
				continue;
			int x1 = timeBlockWidth + mcuNumber*mcuInfoBlockWidth+1;
			int y1;
			int x2 = mcuInfoBlockWidth-1;
			int y2;
			if( (start.compareTo(startDayInterval)>=0) && (start.compareTo(endDayInterval)<=0)){
				y1 = topMargin+mcuInfoBlockHeight + start.get(Calendar.HOUR_OF_DAY)*timeBlockHeight + start.get(Calendar.MINUTE)/2;
				y2 = (int)((end.getTimeInMillis() - start.getTimeInMillis())/(1000*60*2));				
			}
			else
				if((end.compareTo(startDayInterval)>=0) && (end.compareTo(endDayInterval)<=0)){
					y1 = mcuInfoBlockHeight+topMargin+1;
					y2 = (int)((end.getTimeInMillis() - startDayInterval.getTimeInMillis())/(1000*60*2));
				}
				else
					continue;

			

			g2d.fillRect(x1, y1, x2, y2);
		}
		//g2d.setColor(Color.LIGHT_GRAY);
		
		
	}
	
	private void paintMcuInfo(Graphics2D g2d, int x, int y){
		for(int i=0;i<devices.size();i++){
			g2d.setColor(Color.darkGray);
			g2d.drawRect(x+i*mcuInfoBlockWidth, y, mcuInfoBlockWidth, mcuInfoBlockHeight);
			g2d.setColor(Color.BLACK);
			g2d.drawString("#"+devices.get(i).getId(), x+i*mcuInfoBlockWidth+2, y+10+2);
			g2d.drawString(""+devices.get(i).getMcuName(), x+i*mcuInfoBlockWidth+2, y+20+5);	
		}
		
		
	}
	
	private void paintTimeScale(Graphics2D g2d, int x, int y){
		for(int i = 0; i<24; i++){
			paintTimeScaleBlock(g2d, x, y+i*timeBlockHeight, i+":00");
		}
	}
	
	private void paintTimeScaleBlock(Graphics2D g2d, int x, int y, String time){
		g2d.setColor(Color.darkGray);
		g2d.drawRect(x, y, timeBlockWidth, timeBlockHeight);
		g2d.setColor(Color.BLACK);
		g2d.drawString(time, x+2, y+10+2);
	}
	
	private void paintTimeNet(Graphics2D g2d, int x, int y){
		g2d.setColor(Color.GRAY);
		for(int j=1;j<=devices.size();j++){
			for(int i=0; i<24; i++){
				g2d.drawLine(x, y + i*timeBlockHeight, x+devices.size()*mcuInfoBlockWidth, y + i*timeBlockHeight);
			}
			g2d.drawLine(x+j*mcuInfoBlockWidth, y, x+j*mcuInfoBlockWidth, y+24*timeBlockHeight);
		}
		
		
	}

}
