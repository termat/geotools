package net.termat.geo.geojson;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import net.termat.components.table.MyTableCellRenderer;

public class GeojsonDataTable extends JPanel{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private FeatureCollection collection;
	private GeojsonDataTableModel model;
	private boolean isEditable;

	public GeojsonDataTable(boolean isEditable){
		super(new BorderLayout());
		table=new JTable();
		table.setRowHeight(24);
		table.setColumnSelectionAllowed(true);
		table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		this.add(new JScrollPane(table),BorderLayout.CENTER);
		this.isEditable=isEditable;
		add(createTool(),BorderLayout.NORTH);
	}

	public GeojsonDataTable(FeatureCollection c,boolean isEditable){
		super(new BorderLayout());
		this.collection=c;;
		table=new JTable();
		table.setRowHeight(24);
		table.setColumnSelectionAllowed(true);
		table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		this.add(new JScrollPane(table),BorderLayout.CENTER);
		add(createTool(),BorderLayout.NORTH);
		this.isEditable=isEditable;
		initModel();
	}

	public Feature getSelctedFeature() {
		int row=table.getSelectedRow();
		if(row<0)return null;
		return collection.features().get(row);
	}
	
	private JToolBar createTool(){
		JToolBar ret=new JToolBar();
		ret.setFloatable(false);
		ret.setBorder(BorderFactory.createEtchedBorder());
		ret.addSeparator();
		JCheckBox ch=new JCheckBox("Resize",true);
		ch.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox c=(JCheckBox)arg0.getSource();
				if(c.isSelected()){
					table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				}else{
					table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				}
			}

		});
		ret.add(ch);
		ret.addSeparator();
		JButton bt = new JButton("Copy",getIcon("images/copy.png"));		ret.add(bt);
		bt.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Clipboard clip =Toolkit.getDefaultToolkit().getSystemClipboard();
                StringBuffer str=new StringBuffer();
                TableModel dm=table.getModel();
                for(int i=0;i<dm.getColumnCount();i++){
                	if(i==dm.getColumnCount()-1){
                		str.append(dm.getColumnName(i)+"\n");
                	}else{
                       	str.append(dm.getColumnName(i)+"\t");
                	}
                }
                for(int i=0;i<dm.getRowCount();i++) {
                	for(int j=0;j<dm.getColumnCount();j++){
                		if(j==0){
                			str.append(dm.getValueAt(i, j));
                		}else{
                			str.append("\t"+dm.getValueAt(i, j));
                		}
                	}
            		str.append("\n");
                }
                StringSelection sel  = new StringSelection(str.toString());
                clip.setContents(sel, sel);
			}
		});
		ret.addSeparator();
		return ret;
	}

	public JTable getTable(){
		return table;
	}

	public GeojsonDataTableModel getModel(){
		return model;
	}

	public void setVector(FeatureCollection fc){
		this.collection=fc;
		initModel();
	}

	public void updateJson(){
		initModel();
		table.repaint();
	}

	private void initModel(){
		if(isEditable){
			model=new GeojsonDataTableModel(collection){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int arg0, int arg1) {
					return true;
				}
			};
		}else{
			model=new GeojsonDataTableModel(collection){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int arg0, int arg1) {
					return false;
				}
			};
		}
		table.setModel(model);
	}

	public ImageIcon getIcon(String arg) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+arg);
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;
	}
}
