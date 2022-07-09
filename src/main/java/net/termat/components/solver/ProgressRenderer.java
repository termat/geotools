/*
 * build: 2005/07/10
 *
 * Copyright (c) 2004 t.matsuoka. All Rights Reserved.
 *
 */
package net.termat.components.solver;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * ProgressRenderer<br>
 *
 * このクラスはテーブルセルの中のJProgressBarを表示する。<br>
 * ■使用例<br>
 *   JTable table = new JTable(tableModel);<br>
 *   table.getSelectionModel().addListSelectionListener(new<br>
 *     ListSelectionListener() {<br>
 *     public void valueChanged(ListSelectionEvent e) {<br>
 *       tableSelectionChanged();<br>
 *     }<br>
 *   });<br>
 * <br>
 *   // 進捗列を表示するプログレスバーをセットアップする<br>
 *   ProgressRenderer renderer = new ProgressRenderer(0, 100);<br>
 *   renderer.setStringPainted(true); // 進捗テキストを表示する<br>
 *   table.setDefaultRenderer(JProgressBar.class, renderer);<br>
 *<br>
 *   // テーブル行の高さをJProgressBarに合わせる<br>
 *   table.setRowHeight(<br>
 *     (int) renderer.getPreferredSize().getHeight());<br>
 *
 * @author Teruki.Matsuoka
 * @version 0.5
 *
 */
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
