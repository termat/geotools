package net.termat.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class TableColorEditor extends AbstractCellEditor implements
		TableCellEditor, ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Color currentColor;
	private JButton button;
	private JColorChooser colorChooser;
	private JDialog dialog;
	private JTable currentTable;
	protected static final String EDIT = "edit";

	public TableColorEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button,
					"Pick a Color",
					true,  //modal
					colorChooser,
					this,  //OK button handler
					null); //no CANCEL button handler
		dialog.setAlwaysOnTop(true);
	}

	public void setAlwaysOnTop(boolean b){
		dialog.setAlwaysOnTop(b);
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);
			fireEditingStopped(); //Make the renderer reappear.
		} else { //User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
			currentTable.setValueAt(currentColor, currentTable.getSelectedRow(),
					currentTable.getSelectedColumn());
			currentTable.removeEditor();
		}
	}

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return currentColor;
	}

    //Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table,
					Object value,
					boolean isSelected,
					int row,
					int column) {
		currentColor = (Color)value;
		currentTable=table;
		return button;
    }

	public void setCurrentColor(Color currentColor) {
		this.currentColor = currentColor;
	}
}
