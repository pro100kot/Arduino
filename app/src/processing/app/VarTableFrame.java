package processing.app;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class VarTableFrame extends JFrame {
	private static final long serialVersionUID = 6231806369380869041L;
	private JTable table;
	private JScrollPane pane;
	private ArrayList<VarTableElement> variables;
	
	public VarTableFrame(){
		super("Variables");
		variables = new ArrayList<VarTableElement>();
		setSize(150, 300);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		draw();
		setVisible(true);
	}
	
	private void draw(){
		VarTableModel model = new VarTableModel(variables);
		table = new JTable(model);
		table.setSize(150, 300);
		pane = new JScrollPane(table);
		pane.setSize(150, 300);
		getContentPane().removeAll();
		getContentPane().add(pane);
		getContentPane().repaint();
	}
	
	public ArrayList<VarTableElement> getVarsStorage(){
		return variables;
	}
	
	public void redraw(ArrayList<VarTableElement> elements){
		variables = elements;
		draw();
	}
	
	public void close(){
		dispose();
	}

}
