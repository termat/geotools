package net.termat.geo.geojson;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

public class GeojsonEditor extends JPanel{
	private static final long serialVersionUID = 1L;
	private FeatureCollection collection;
	private GeojsonDataTable table;
	private JFileChooser ch;
	private File file=null;
	private FileFilter filter;
	
	public GeojsonEditor(boolean editable) {
		super(new BorderLayout());
		table=new GeojsonDataTable(editable);
		super.add(table,BorderLayout.CENTER);
		if(editable)super.add(createToolBar(),BorderLayout.NORTH);
		filter=new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory()||arg0.getName().toLowerCase().endsWith(".geojson");
			}
			@Override
			public String getDescription() {
				return "*.geojson";
			}
		};
		if(editable)initDnD();
	}

	public JTable getTable() {
		return table.getTable();
	}
	
	public File getFile() {
		return file;
	}
	
	public Feature getSelectedFeature() {
		return table.getSelctedFeature();
	}
	
	public File getGeojsonFile() {
		return file;
	}
	
	public String[] getPropName() {
		List<String> ret=new ArrayList<>();
		DefaultTableModel mo=(DefaultTableModel)table.getModel();
		for(int i=0;i<mo.getColumnCount();i++) {
			ret.add(mo.getColumnName(i));
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	public void setJFileChooser(JFileChooser ch) {
		this.ch = ch;
	}
	
	public FeatureCollection getFeatureCollection() {
		return this.collection;
	}
	
	private JToolBar createToolBar(){
		JToolBar ret=new JToolBar();
		ret.setFloatable(false);
		ret.setBorder(BorderFactory.createEtchedBorder());
		ret.addSeparator();
		JButton bt=new JButton("開く",getIcon("images/open.png"));
		bt.addActionListener(e -> {
			if(file!=null)ch.setSelectedFile(file);
			ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
			ch.setFileFilter(filter);
			int ck=ch.showOpenDialog(JOptionPane.getRootFrame());
			if(ck==JFileChooser.APPROVE_OPTION){
				file=ch.getSelectedFile();
				try{
					collection=GeojsonUtil.loadGeojson(file);
					table.setVector(collection);
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
		});
		ret.add(bt);
		ret.addSeparator();
		bt=new JButton("保存",getIcon("images/save.png"));
		bt.addActionListener(e -> {
			if(collection==null)return;
			if(file!=null)ch.setSelectedFile(file);
			ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
			ch.setFileFilter(filter);
			int ck=ch.showSaveDialog(JOptionPane.getRootFrame());
			if(ck==JFileChooser.APPROVE_OPTION){
				file=ch.getSelectedFile();
				try{
					if(!file.getName().toLowerCase().endsWith(".geojson")){
						file=new File(file.getAbsoluteFile()+".geojson");
					}
					table.getModel().update();
					GeojsonUtil.output(collection, file);
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
		});
		ret.add(bt);
		ret.addSeparator();
		bt=new JButton("列削除",getIcon("images/batsu.png"));
		bt.addActionListener(e -> {
			if(collection==null)return;
			int col=table.getTable().getSelectedColumn();
			if(col==-1)return;
			String name=table.getModel().getColumnName(col);
			int ck=JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "プロパティ「"+name+"」を\n削除しますか？", "Info", JOptionPane.YES_NO_OPTION);
			if(ck==JOptionPane.NO_OPTION)return;
			GeojsonUtil.removePropety(collection, name);
			table.updateJson();
		});
		ret.add(bt);
		ret.addSeparator();
		bt=new JButton("列名変更",getIcon("images/wrench2.png"));
		bt.addActionListener(e -> {
			if(collection==null)return;
			int col=table.getTable().getSelectedColumn();
			if(col==-1)return;
			String name=table.getModel().getColumnName(col);
			String str=JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "プロパティ「"+name+"」の\n項目名を変更しますか？", name);
			if(str==null||str.isEmpty())return;
			if(str.equals(name))return;
			GeojsonUtil.changePropetyName(collection, name, str);
			table.updateJson();
		});
		ret.add(bt);
		ret.addSeparator();
		bt=new JButton("値置換",getIcon("images/updown1.png"));
		bt.addActionListener(e -> {
			if(collection==null)return;
			int col=table.getTable().getSelectedColumn();
			if(col==-1)return;
			String name=table.getModel().getColumnName(col);
			List<Object> list=new ArrayList<Object>();
			DefaultTableModel model=table.getModel();
			for(int i=0;i<model.getRowCount();i++){
				list.add(model.getValueAt(i, col));
			}
			ValueChaneDialog dd=new ValueChaneDialog(JOptionPane.getRootFrame(),list);
			dd.setLocationRelativeTo(null);
			dd.setVisible(true);
			Map<Object,Object> om=dd.getTransTable();
			if(om==null)return;
			GeojsonUtil.transPropetyValue(collection, name, om);
			table.updateJson();
		});
		ret.add(bt);
		ret.addSeparator();
		bt=new JButton("列追加",getIcon("images/plus.png"));
		bt.addActionListener(e -> {
			if(collection==null)return;
			JPanel p=new JPanel(new GridLayout(3,1));
			p.add(new JLabel("新規に追加するプロパティ名と初期値を設定してください"));
			JPanel p1=new JPanel(new BorderLayout());
			p1.add(new JLabel("プロパティ名"),BorderLayout.WEST);
			JTextField t1=new JTextField("");
			p1.add(t1,BorderLayout.CENTER);
			p.add(p1);
			JPanel p2=new JPanel(new BorderLayout());
			p2.add(new JLabel("初期値　　　"),BorderLayout.WEST);
			JTextField t2=new JTextField("");
			p2.add(t2,BorderLayout.CENTER);
			p.add(p2);
			int ck=JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), p, "Info", JOptionPane.YES_NO_OPTION);
			if(ck==JOptionPane.NO_OPTION)return;
			String key=t1.getText();
			String sv=t2.getText();
			if(key==null||key.isEmpty()||sv==null||sv.isEmpty()){
				JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "設定に問題がありました", "Info", JOptionPane.WARNING_MESSAGE);
				return;
			}
			Object value=null;
			try{
				value=Integer.parseInt(sv);
			}catch(NumberFormatException e1){
				try{
					value=Double.parseDouble(sv);
				}catch(NumberFormatException e2){
					value=sv;
				}
			}
			GeojsonUtil.createNewProperties(collection, key, value);
			table.updateJson();
		});
		ret.add(bt);
		ret.addSeparator();
		return ret;
	}
	
	public void loadGeojson(File f) throws IOException {
		collection=GeojsonUtil.loadGeojson(f);
		table.setVector(collection);
		file=f;
	}
	
	public void loadGeojson(String str){
		collection=GeojsonUtil.loadGeojson(str);
		table.setVector(collection);
	}
	
	public void loadGeojson(FeatureCollection fc){
		collection=fc;
		table.setVector(collection);
	}

	@SuppressWarnings("unchecked")
	private void initDnD(){
		DropTargetListener dtl = new DropTargetAdapter() {
			@Override public void dragOver(DropTargetDragEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY);
					return;
				}
				dtde.rejectDrag();
			}
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						Transferable transferable = dtde.getTransferable();
						List<Object> list = (List<Object>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
						Object o=list.get(0);
						if(o instanceof File){
							File f=(File)o;
							if(f.getName().toLowerCase().endsWith(".geojson")){
								try{
									file=f;
									collection=GeojsonUtil.loadGeojson(f);
									table.setVector(collection);
								}catch(Exception e){
									JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "読み込みに失敗しました。", "Info", JOptionPane.WARNING_MESSAGE);
								}
							}
						}
						dtde.dropComplete(true);
						return;
					}
				} catch (UnsupportedFlavorException | IOException ex) {
					ex.printStackTrace();
				}
				dtde.rejectDrop();
			}
		};
		new DropTarget(table, DnDConstants.ACTION_COPY, dtl, true);
	}
	
	public ImageIcon getIcon(String arg) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+arg);
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;
	}
}
