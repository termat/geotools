package net.termat.components;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class JProgressDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JLabel label;

	public JProgressDialog(Frame f){
		super(f,false);
		this.setTitle("Progress");
		this.setAlwaysOnTop(true);
		this.setSize(300, 200);
		this.setResizable(false);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(loadingPanel(null),BorderLayout.CENTER);
	}

	private JPanel loadingPanel(ImageIcon imageIcon) {
		JPanel panel = new JPanel(new BorderLayout());
		if(imageIcon==null)imageIcon = getIcon();
		JLabel iconLabel = new JLabel();
		iconLabel.setHorizontalAlignment(JLabel.CENTER);
		iconLabel.setIcon(imageIcon);
		imageIcon.setImageObserver(iconLabel);
		label = new JLabel("処理中...");
		label.setHorizontalAlignment(JLabel.CENTER);
		panel.add(iconLabel,BorderLayout.CENTER);
		panel.add(label,BorderLayout.SOUTH);
		return panel;
	}

	public ImageIcon getIcon(){	
	    final URL url = Thread.currentThread().getContextClassLoader().getResource("main/resources/images/spinner.gif");
	    Image ii=Toolkit.getDefaultToolkit().getImage(url);
		ImageIcon ret=new ImageIcon(ii);
		return ret;		
	}
	
	public  void setText(String arg){
		Runnable r=new Runnable(){
			public void run(){
				label.setText(arg);
			}
		};
		SwingUtilities.invokeLater(r);
	}

	public void setVisible(boolean n){
		if(n)this.setLocationRelativeTo(null);
		label.setText("処理中...");
		super.setVisible(n);
	}
}
