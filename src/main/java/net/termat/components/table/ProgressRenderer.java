/*
 * build: 2005/07/10
 *
 * Copyright (c) 2004 t.matsuoka. All Rights Reserved.
 *
 */
package net.termat.components.table;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressRenderer extends JProgressBar implements TableCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ProgressRenderer(int min, int max){
		super(min, max);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected,boolean hasFocus, int row, int column){
//		setValue((int) ((Float) value).floatValue());
		if(value instanceof Number){
			setValue((int) ((Float) value).floatValue());
		}
		return this;
	}

}
