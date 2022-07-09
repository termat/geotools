package net.termat.geo.db;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DBEditor extends JPanel{

	private static final long serialVersionUID = 1L;
	private PointCloudDB db;
	private DBTable table;
	private List<Index> list;
	private Set<Index> upList=new HashSet<>();
	private DefaultTableModel model;
	private List<Index> target;
	private JComboBox<String> name,type;
	private boolean editable=false;

	public DBEditor(DBTable d) throws SQLException {
		super(new BorderLayout());
		this.db=d.getDB();
		this.table=d;
		list=db.getIndexes();
		target=new ArrayList<>();
		target.addAll(list);
		JTable tmp=new JTable();
		model=new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int arg0, int arg1) {
				return arg1==0||arg1==1;
			}
		};
		model.setRowCount(list.size());
		model.setColumnCount(6);
		model.setColumnIdentifiers(new String[]{"name","type","dataType","coordSys","width","height"});
		tmp.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
		tmp.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,int column) {
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
			
		});
		TableModelListener tml=new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if(editable) {
					int row=e.getLastRow();
					int col=e.getColumn();
					Index it=target.get(row);
					if(col==0) {
						it.name=(String)model.getValueAt(row, col);
					}else if(col==1) {
						it.type=(String)model.getValueAt(row, col);
					}
					upList.add(it);
				}
			}			
		};
		model.addTableModelListener(tml);
		tmp.setModel(model);
		tmp.setRowHeight(24);
		super.add(new JScrollPane(tmp),BorderLayout.CENTER);
		JToolBar tool=new JToolBar();
		tool.setFloatable(false);
		tool.setBorder(BorderFactory.createEtchedBorder());
		tool.addSeparator();
		JButton update=new JButton("DB更新",getIcon("images/updown1.png"));
		update.addActionListener(e->{
			for(Index ii: upList) {
				try {
					db.update(ii);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			upList.clear();
			try {
				table.setDB(db);
				initComb();
				updateList();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
		tool.add(update);
		tool.addSeparator();
		name=new JComboBox<>(new String[] {"－"});
		name.setMaximumSize(new Dimension(120,36));
		type=new JComboBox<>(new String[] {"－"});
		type.setMaximumSize(new Dimension(120,36));
		tool.add(name);
		tool.addSeparator();
		tool.add(type);
		tool.addSeparator();
		super.add(tool,BorderLayout.NORTH);
		name.addItemListener(e->{
			if(editable) {
				updateList();
			}
		});
		type.addItemListener(e->{
			if(editable) {
				updateList();
			}
		});
		initComb();
		updateList();
	}
	
	private void initComb() {
		while(name.getItemCount()>1) {
			name.removeItemAt(name.getItemCount()-1);
		}
		name.setSelectedIndex(0);
		while(type.getItemCount()>1) {
			type.removeItemAt(type.getItemCount()-1);
		}
		type.setSelectedIndex(0);
		Set<String> na=new HashSet<>();
		Set<String> ty=new HashSet<>();
		for(Index ii : list) {
			na.add(ii.name);
			ty.add(ii.type);
		}
		String[] nas=na.toArray(new String[na.size()]);
		String[] tys=ty.toArray(new String[ty.size()]);
		if(nas.length>0) {
			Arrays.sort(nas);
			for(String s: nas)name.addItem(s);
		}
		if(tys.length>0) {
			Arrays.sort(tys);
			for(String s: tys)type.addItem(s);
		}
	}
	
	private void updateList() {
		editable=false;
		target.clear();
		String nc=(String)name.getSelectedItem();
		String tc=(String)type.getSelectedItem();
		for(Index ii : list) {
			if(!nc.equals("－")) {
				if(!ii.name.equals(nc)) {
					continue;
				}
			}
			if(!tc.equals("－")) {
				if(!ii.type.equals(tc)) {
					continue;
				}
			}
			target.add(ii);
		}
		int row=0;
		model.setRowCount(target.size());
		for(Index i : target){
			model.setValueAt(i.name, row, 0);
			model.setValueAt(i.type, row, 1);
			model.setValueAt(i.dataType, row, 2);
			model.setValueAt(i.coordSys, row, 3);
			model.setValueAt(i.width, row, 4);
			model.setValueAt(i.height, row, 5);
			row++;
		}
		editable=true;
	}
	
	public ImageIcon getIcon(String arg) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+arg);
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;
	}
}
