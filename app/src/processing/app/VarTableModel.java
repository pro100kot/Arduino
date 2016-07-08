package processing.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import static processing.app.I18n.tr;

public class VarTableModel extends AbstractTableModel{

	private Set<TableModelListener> listeners = new HashSet<>();
	private ArrayList<VarTableElement> vars;
	
	public VarTableModel(ArrayList<VarTableElement> vars) {
		this.vars = vars;
	}
	
	@Override
	public int getRowCount() {
		return vars.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return tr("Name");
		case 1:
			return tr("Value");
		default:
			return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		VarTableElement var = vars.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return var.getName();
		case 1:
			return var.getValue();
		default:
			return "";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

}
