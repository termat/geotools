package net.termat.components.table;

import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class DateCellRenderer extends JLabel implements TableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private boolean isBordered = true;
	private DateFormat fort0;
	private DateFormat fort1;

	public DateCellRenderer(boolean isBordered) {
		this.isBordered = isBordered;
		this.setBackground(Color.WHITE);
		setOpaque(true);
		fort0=DateFormat.getDateInstance();
		fort1=DateFormat.getDateTimeInstance();
	}

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		Date date=(Date)arg1;
		this.setText(fort0.format(date));
		setToolTipText(fort1.format(date));
		if (isBordered) {
			if (arg2) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
							arg0.getSelectionBackground());
				}
				setBorder(selectedBorder);
			} else {
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
							arg0.getBackground());
				}
				setBorder(unselectedBorder);
			}
		}
		return this;
	}

}
