package net.termat.components;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Downloader {
	private JDialog dialog;
	
	public Downloader(Frame frm,String site, File file) {
		dialog=new JDialog(frm,false);
		final JProgressBar current = new JProgressBar(0, 100);
		current.setSize(50, 100);
		current.setValue(0);
		current.setStringPainted(true);
		dialog.getContentPane().setLayout(new BorderLayout());
		JLabel mes=new JLabel(" ファイルをダウンロードしています... ");
		mes.setHorizontalAlignment(JLabel.CENTER);
		dialog.getContentPane().add(mes,BorderLayout.NORTH);
		dialog.getContentPane().add(current,BorderLayout.CENTER);
		dialog.setResizable(false);
		dialog.setSize(400, 80);
		dialog.setLocationRelativeTo(frm);
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
		final Worker worker = new Worker(site, file);
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pcEvt) {
				if ("progress".equals(pcEvt.getPropertyName())) {
					current.setValue((Integer) pcEvt.getNewValue());
				} else if (pcEvt.getNewValue() == SwingWorker.StateValue.DONE) {
					try {
						worker.get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace(); 
					}
				}
			}
		});
		worker.execute();
	}
	
	class Worker extends SwingWorker<Void, Void> {
		private String site;
		private File file;

		public Worker(String site, File file) {
			this.site = site;
			this.file = file;
		}

		@Override
		protected Void doInBackground() throws Exception {
			URL url = new URL(site);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			long filesize = connection.getContentLength();
			long totalDataRead = 0;
			try (java.io.BufferedInputStream in = new java.io.BufferedInputStream(
					connection.getInputStream())) {
				java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
				try (java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024)) {
					byte[] data = new byte[1024];
					int i;
					while ((i = in.read(data, 0, 1024)) >= 0) {
						totalDataRead = totalDataRead + i;
						bout.write(data, 0, i);
						int percent =(int)((totalDataRead * 100) / filesize);
						setProgress(percent);
					}
				}
			}
			Runnable r=new Runnable() {
				public void run() {
					dialog.setVisible(false);
					JOptionPane.showMessageDialog(dialog, "ダウンロードを終了しました。", "Info", JOptionPane.INFORMATION_MESSAGE);
				}
			};
			SwingUtilities.invokeLater(r);
			return null;
		}
	}
}
