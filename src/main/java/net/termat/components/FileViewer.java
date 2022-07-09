package net.termat.components;

import java.awt.BorderLayout;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import net.termat.components.table.MyTableCellRenderer;


public class FileViewer extends JPanel{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JButton bt_up;
	private JTextField url;
	private JTable table;
	private DefaultTableModel model;


	public FileViewer(){
		super(new BorderLayout());
		JToolBar tool=new JToolBar();
		tool.setFloatable(false);
		tool.setBorder(BorderFactory.createEtchedBorder());;
		this.add(tool,BorderLayout.NORTH);
		tool.addSeparator();
		ImageIcon icon=getIcon("images/up_green.png");
		bt_up=new JButton(icon);
		bt_up.setToolTipText("上の階層へ移動");
		bt_up.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File f=new File(url.getText());
				if(!f.exists())return;
				File p=f.getParentFile();
				if(p!=null){
					setPath(p);
				}
			}
		});
		tool.add(bt_up);
		url=new JTextField(50);
		url.setEditable(false);
		tool.add(url);
		tool.addSeparator();
		table=new JTable();
		model=new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.setColumnCount(1);
		model.setRowCount(0);
		model.setColumnIdentifiers(new String[]{"Path"});
		table.setModel(model);
		table.setRowHeight(24);
		table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		table.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount()>=2){
					JTable t=(JTable)arg0.getSource();
					int rt=t.getSelectedRow();
					if(rt>=0){
						File ff=(File)t.getValueAt(rt,0);
						if(ff.isDirectory()){
							setPath(ff);
						}
					}
				}
			}
		});
		super.add(new JScrollPane(table),BorderLayout.CENTER);
		new DropTarget(table, DnDConstants.ACTION_COPY, dtl, true);
		new DropTarget(this, DnDConstants.ACTION_COPY, dtl, true);
		String path = System.getProperty("user.home");
		setPath(new File(path));
	}

	public void updatePath() {
		File f=new File(url.getText());
		setPath(f);
	}
	
	public void setPath(File path){
		int row=table.getSelectedRow();
		table.changeSelection(row,0, true, false);
		url.setText(path.getAbsolutePath());
		File[] f=path.listFiles();
		model.setRowCount(f.length);
		for(int i=0;i<f.length;i++){
			model.setValueAt(f[i], i, 0);
		}
	}

	public ImageIcon getIcon(String path){
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+path);
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;		
	}

	public JTable getTable() {
		return table;
	}

	public File getSelectedFile(){
		int row=table.getSelectedRow();
		if(row>=0){
			return (File)model.getValueAt(row, 0);
		}else{
			return null;
		}
	}

	public File[] getSelectedFiles(){
		int[] it=table.getSelectedRows();
		List<File> ret=new ArrayList<>();
		for(int i=0;i<it.length;i++){
			ret.add((File)model.getValueAt(it[i], 0));
		}
		return ret.toArray(new File[ret.size()]);
	}

	private DropTargetListener dtl = new DropTargetAdapter() {
		  @Override public void dragOver(DropTargetDragEvent dtde) {
		    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		      dtde.acceptDrag(DnDConstants.ACTION_COPY);
		      return;
		    }
		    dtde.rejectDrag();
		  }

		  @SuppressWarnings("rawtypes")
		@Override public void drop(DropTargetDropEvent dtde) {
		    try {
		      if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		        dtde.acceptDrop(DnDConstants.ACTION_COPY);
		        Transferable transferable = dtde.getTransferable();
		        List list = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
		        for (Object o: list) {
		          if (o instanceof File) {
		            File file = (File) o;
		            if(file.isDirectory()){
		            	setPath(file);
		            }else{
		            	File p=file.getParentFile();
		            	if(p!=null)setPath(p);
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
}
