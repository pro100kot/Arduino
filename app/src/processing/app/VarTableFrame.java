package processing.app;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import static processing.app.I18n.tr;

public class VarTableFrame extends JFrame {
	private static final long serialVersionUID = 6231806369380869041L;
	private JTable table;
	private JScrollPane pane;
	private ArrayList<VarTableElement> variables;
	private VarTableModel model;
	
	public VarTableFrame(){
		super(tr("Variables"));
		variables = new ArrayList<VarTableElement>();
		setSize(150, 300);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		model = new VarTableModel(variables);
		table = new JTable(model);
		pane = new JScrollPane(table);
		getContentPane().add(pane);
	}
	
	public void redraw(ArrayList<VarTableElement> elements){
		variables.clear();
		for(VarTableElement cur : elements)
			variables.add(cur);
		model.fireTableDataChanged();
		getContentPane().remove(pane);
		table = new JTable(model);
		pane = new JScrollPane(table);
		getContentPane().add(pane);
		revalidate();
		repaint();
	}
	
	public void clear(){
		variables.clear();
	}

}
