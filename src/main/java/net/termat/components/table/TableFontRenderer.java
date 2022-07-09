package net.termat.components.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class TableFontRenderer extends JLabel implements TableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private boolean isBordered = true;

	public TableFontRenderer(boolean isBordered) {
		this.isBordered = isBordered;
		setOpaque(true);
		super.setText("Aa1");
	}

	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		Font font=(Font)arg1;
		String family=font.getFamily();
		String style=getStyle(font.getStyle());
		String size=Integer.toString(font.getSize());
		super.setFont(font);
		setToolTipText(
			family+","+style+","+size);
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

	private String getStyle(int arg){
		switch(arg){
			case Font.PLAIN:
				return "PLAIN";
			case Font.BOLD:
				return "BOLD";
			case Font.ITALIC:
				return "ITALIC";
			default:
				return "ITALIC-BOLD";
		}
	}

}
