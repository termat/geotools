package net.termat.geo.geojson;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import net.termat.components.table.MyTableCellEditor;
import net.termat.components.table.MyTableCellRenderer;

public class ValueChaneDialog extends JDialog{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private DefaultTableModel model;
	private boolean isSelect=false;

	public ValueChaneDialog(Frame f,List<Object> list){
		super(f,true);
		super.setSize(400,400);
		super.getContentPane().setLayout(new BorderLayout());
		initModel(list);
		table=new JTable(model);
		table.setRowHeight(24);
		table.setDefaultEditor(Object.class,new MyTableCellEditor());
		table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		super.getContentPane().add(new JScrollPane(table),BorderLayout.CENTER);
		JToolBar tool=new JToolBar();
		tool.setFloatable(false);
		tool.add(Box.createGlue());
		tool.addSeparator();
		JButton ok =new JButton(" O.K. ");
		ok.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				isSelect=true;
				setVisible(false);
			}
		});
		JButton can=new JButton("cancel");
		can.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				isSelect=false;
				setVisible(false);
			}
		});
		tool.add(ok);
		tool.addSeparator();
		tool.add(can);
		tool.addSeparator();
		tool.setFloatable(false);
		super.getContentPane().add(tool,BorderLayout.SOUTH);
		super.getContentPane().add(new JLabel("　変更前の値と変更後の値を設定してください"),BorderLayout.NORTH);
		super.pack();
	}

	private void initModel(List<Object> list){
		Set<Object> str=new HashSet<Object>();
		for(Object s :list){
			str.add(s);
		}
		Object[] sl=str.toArray(new Object[str.size()]);
		if(sl[0] instanceof Comparable){
			Arrays.sort(sl);
		}
		model=new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int arg0, int arg1) {
				return arg1==1;
			}
		};
		model.setRowCount(sl.length);
		model.setColumnCount(2);
		model.setColumnIdentifiers(new String[]{"変更前","変更後"});
		for(int i=0;i<sl.length;i++){
			model.setValueAt(sl[i], i, 0);
			model.setValueAt(sl[i], i, 1);
		}
	}

	public Map<Object,Object> getTransTable(){
		if(!this.isSelect)return null;
		Map<Object,Object> ret=new HashMap<Object,Object>();
		for(int i=0;i<model.getRowCount();i++){
			ret.put(model.getValueAt(i, 0), model.getValueAt(i, 1));
		}
		return ret;
	}
}
