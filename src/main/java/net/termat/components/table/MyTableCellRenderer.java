package net.termat.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MyTableCellRenderer extends DefaultTableCellRenderer{
	private static final long serialVersionUID = 1L;
//	private Color GRAY=new Color(230,230,230);

	public MyTableCellRenderer(){}

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
		}else{
			return super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
		}
	}

}
