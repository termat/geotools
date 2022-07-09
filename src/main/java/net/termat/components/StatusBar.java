package net.termat.components;

import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

public class StatusBar extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JToolBar bar;
	private JLabel memLabel;

	public StatusBar(){
		super(new BorderLayout());
		super.setBorder(BorderFactory.createEtchedBorder());
		bar=new JToolBar();
		bar.setFloatable(false);
		super.add(bar,BorderLayout.CENTER);
		bar.addSeparator();
		bar.add(Box.createHorizontalGlue());
		bar.addSeparator();
		memLabel=new JLabel("");
		bar.add(memLabel);
		Timer t=new Timer();
		t.schedule(new Task(),0,5000);
	}

	private class Task extends TimerTask{
		/* (�� Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			String m=getCurrentMemory();
			memLabel.setText(m);
		}
	}

	private static String getCurrentMemory(){
		long total=(Runtime.getRuntime().totalMemory()/1000000);
		long free=(Runtime.getRuntime().freeMemory()/1000000);
		int rate = (int)((float)free/(float)total*100.0f);
		return "free/total:"+Long.toString(free)+"/"
					+Long.toString(total)+"MB ("
					+Float.toString(rate)+"%)";
	}
}
