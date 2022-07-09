package net.termat.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MyTableCellRenderer extends DefaultTableCellRenderer{
	private static final long serialVersionUID = 1L;
	private ImageIcon directory,file;
	private Color GRAY=new Color(230,230,230);

	public MyTableCellRenderer(){
		try{
			directory=getIcon("images/directory.png");
			file=getIcon("images/file.png");
		}catch(IOException e){
		}
	}

	public ImageIcon getIcon(String arg) throws IOException {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+arg);
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;
	}
	
	public static Image getImage(final String pathAndFileName) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/"+pathAndFileName);
	    return Toolkit.getDefaultToolkit().getImage(url);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
		if(arg1 instanceof Boolean){
			JCheckBox b=new JCheckBox();
			b.setBackground(Color.WHITE);
			b.setSelected(((Boolean)arg1).booleanValue());
			return b;
		}else if(arg1 instanceof JComboBox){
			JComboBox c=(JComboBox)arg1;
			return super.getTableCellRendererComponent(arg0, c.getSelectedItem(), arg2, arg3, arg4, arg5);
		}else if(arg1 instanceof JComponent){
			return (JComponent)arg1;
		}else if(arg1 instanceof File){
			File f=(File)arg1;
			JLabel la=null;
			if(f.isDirectory()){
				la=new JLabel(f.getName(),directory,JLabel.LEFT);
			}else{
				la=new JLabel(f.getName(),file,JLabel.LEFT);
			}
			la.setOpaque(true);
			if(arg4%2==1){
				la.setBackground(GRAY);
			}else{
				la.setBackground(Color.WHITE);
			}
			if(arg2){
				la.setBorder(BorderFactory.createLineBorder(Color.BLUE));
			}
			la.setFont(arg0.getFont());
			return la;
		}else{
			return super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
		}
	}

}
