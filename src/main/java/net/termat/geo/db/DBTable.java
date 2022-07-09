package net.termat.geo.db;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class DBTable extends JPanel implements ChangeListener{
	private static final long serialVersionUID = 1L;
	private List<Index> list;
	private List<Index> target;
	private JTable table;
	private DefaultTableModel model;
	private PointCloudDB db;
	private JComboBox<String> name;
	private JComboBox<String> type;
	private boolean flg=false;
	private JButton down;

	public DBTable(){
		super(new BorderLayout());
		table=new JTable();
		table.setRowHeight(24);
		model=new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int arg0, int arg1) {
				return false;
			}
		};
		model.setColumnCount(2);
		model.setColumnIdentifiers(new String[]{"名称","タイプ"});
		model.setRowCount(0);
		table.setModel(model);
		table.getColumnModel().getColumn(1).setMinWidth(60);
		table.getColumnModel().getColumn(1).setMaxWidth(200);
		table.getColumnModel().getColumn(1).setWidth(100);
		super.add(new JScrollPane(table),BorderLayout.CENTER);
		JToolBar tool=new JToolBar();
		tool.setFloatable(false);
		tool.setBorder(BorderFactory.createEtchedBorder());
		super.add(tool,BorderLayout.NORTH);
		down=new JButton(getIcon("images/arrow4.png"));
		tool.addSeparator();
		tool.add(down);
		name=new JComboBox<>();
		type=new JComboBox<>();
		tool.addSeparator();
		tool.add(name);
		tool.addSeparator();
		tool.add(type);
		tool.addSeparator();
		list=new ArrayList<>();
		target=new ArrayList<>();
		name.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(flg)return;
				update();
			}
		});
		type.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(flg)return;
				update();
			}
		});
	}

	public List<Index> getSelectedRows(){
		int[] ii=table.getSelectedRows();
		List<Index> ret=new ArrayList<>();
		for(int id : ii){
			ret.add(target.get(id));
		}
		return ret;
	}

	public void setDownAction(ActionListener al){
		down.addActionListener(al);
	}

	public Index getSelectedRow(){
		int i=table.getSelectedRow();
		if(i>=0){
			return target.get(i);
		}else{
			return null;
		}
	}

	public List<Index> getAllData() {
		return list;
	}

	public List<Index> getTargetData() {
		return target;
	}
	
	public JTable getTable() {
		return table;
	}

	public void setFont(Font f){
		super.setFont(f);
		if(table!=null)table.setFont(f);
	}

	public void setDB(PointCloudDB db) throws SQLException{
		this.db=db;
		db.addChangeListsner(this);
		init();
	}

	public ImageIcon getIcon(String arg) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+arg);
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;
	}

	private void update(){
		target.clear();
		String na,tp;
		if(name.getSelectedIndex()==0){
			na=null;
		}else{
			na=(String)name.getSelectedItem();
		}
		if(type.getSelectedIndex()==0){
			tp=null;
		}else{
			tp=(String)type.getSelectedItem();
		}
		for(Index it : list){
			if(na==null&&tp==null){
				target.add(it);
			}else if(na!=null&&tp==null){
				if(na.equals(it.name))target.add(it);
			}else if(na==null&&tp!=null){
				if(tp.equals(it.type))target.add(it);
			}else{
				if(na.equals(it.name)&&tp.equals(it.type))target.add(it);
			}
		}
		model.setRowCount(target.size());
		for(int i=0;i<target.size();i++){
			Index it=target.get(i);
			model.setValueAt(it.name, i, 0);
			model.setValueAt(it.type, i, 1);
		}
		table.setModel(model);
		flg=false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void init(){
		list.clear();
		try {
			list.addAll(this.db.getIndexes());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		flg=true;
		String ns=(String)name.getSelectedItem();
		String ts=(String)type.getSelectedItem();
		Set<String> h1=new HashSet<>();
		Set<String> h2=new HashSet<>();
		for(Index i : list){
			h1.add(i.name);
			h2.add(i.type);
		}
		String[] names=h1.toArray(new String[h1.size()]);
		String[] types=h2.toArray(new String[h2.size()]);
		Arrays.sort(names);
		Arrays.sort(types);
		DefaultComboBoxModel m1=(DefaultComboBoxModel)name.getModel();
		m1.removeAllElements();
		m1.addElement("－");
		for(String s : names)m1.addElement(s);
		DefaultComboBoxModel m2=(DefaultComboBoxModel)type.getModel();
		m2.removeAllElements();
		m2.addElement("－");
		for(String s : types)m2.addElement(s);
		if(ns!=null)name.setSelectedItem(ns);
		if(ts!=null)type.setSelectedItem(ts);
		update();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		init();
	}

	public PointCloudDB getDB() {
		return db;
	}
	
	
}
