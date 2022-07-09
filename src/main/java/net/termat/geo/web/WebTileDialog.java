package net.termat.geo.web;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class WebTileDialog extends JDialog{
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JSpinner zoom;
	private JSpinner res;
	private JTextField url;
	private WebTile tile;
	private JLabel l1,l2,l3;
	
	public WebTileDialog(Frame frame,boolean modal) {
		super(frame,modal);
		Font font=new Font(Font.SANS_SERIF,Font.PLAIN,12);
		JPanel up=new JPanel(new GridLayout(2,1));
		up.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		JToolBar tool1=new JToolBar();
		tool1.setFloatable(false);
		tool1.setBorder(BorderFactory.createEtchedBorder());
		up.add(tool1);
		super.getContentPane().setLayout(new BorderLayout());
		super.getContentPane().add(up,BorderLayout.NORTH);
		zoom=new JSpinner();
		zoom.setFont(font);
		SpinnerNumberModel sm1=new SpinnerNumberModel(15,13,18,1);
		zoom.setModel(sm1);
		res=new JSpinner();
		res.setFont(font);
		SpinnerNumberModel sm2=new SpinnerNumberModel(0.25d,0.05d,10.0d,0.05d);		
		res.setModel(sm2);
		tool1.addSeparator();
		l1=new JLabel("解像度 ");
		l1.setFont(font);
		tool1.add(l1);
		tool1.add(res);
		tool1.addSeparator();
		l2=new JLabel("Zoomレベル ");
		l2.setFont(font);
		tool1.add(l2);
		tool1.add(zoom);
		tool1.add(Box.createGlue());
		JToolBar tool2=new JToolBar();
		tool2.setFloatable(false);
		tool2.setBorder(BorderFactory.createEtchedBorder());
		up.add(tool2);
		l3=new JLabel("URL ");
		l3.setFont(font);
		tool2.add(l3);
		url=new JTextField();
		url.setBorder(BorderFactory.createLoweredBevelBorder());
		url.setFont(font);
		url.setMargin(new Insets(10,10,10,10));
		JPanel p=new JPanel(new BorderLayout());
		p.add(url,BorderLayout.CENTER);
		tool2.add(p);
		JPanel dw=new JPanel(new BorderLayout());
		super.getContentPane().add(dw,BorderLayout.CENTER);
		table=new JTable();
		table.setFont(font);
		DefaultTableModel model=new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(model);
		model.setColumnCount(3);
		model.setColumnIdentifiers(new String[] {"名称","URL","最大Zoom"});
		model.setRowCount(9);
		table.setRowHeight(24);
		dw.add(new JScrollPane(table),BorderLayout.CENTER);
		dw.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE), "タイル情報"));
		model.setValueAt("地理院標準地図", 0, 0);
		model.setValueAt("https://cyberjapandata.gsi.go.jp/xyz/std/{z}/{x}/{y}.png", 0, 1);
		model.setValueAt(18, 0, 2);
		model.setValueAt("地理院淡色地図", 1, 0);
		model.setValueAt("https://cyberjapandata.gsi.go.jp/xyz/pale/{z}/{x}/{y}.png", 1, 1);
		model.setValueAt(18, 1, 2);
		model.setValueAt("地理院航空写真", 2, 0);
		model.setValueAt("https://cyberjapandata.gsi.go.jp/xyz/seamlessphoto/{z}/{x}/{y}.jpg", 2, 1);
		model.setValueAt(18, 2, 2);
		model.setValueAt("地理院DEM(5-10m)", 3, 0);
		model.setValueAt("https://cyberjapandata.gsi.go.jp/xyz/dem5a_png/{z}/{x}/{y}.png", 3, 1);
		model.setValueAt(15, 3, 2);
		model.setValueAt("オープンストリートマップ", 4, 0);
		model.setValueAt("https://tile.openstreetmap.jp/{z}/{x}/{y}.png", 4, 1);
		model.setValueAt(18, 4, 2);
		model.setValueAt("エコリス植生図(5回)", 5, 0);
		model.setValueAt("https://map.ecoris.info/tiles/vege/{z}/{x}/{y}.png", 5, 1);
		model.setValueAt(15, 5, 2);
		model.setValueAt("エコリス植生図(6-7回)", 6, 0);
		model.setValueAt("https://map.ecoris.info/tiles/vege67/{z}/{x}/{y}.png", 6, 1);
		model.setValueAt(15, 6, 2);
		model.setValueAt("エコリス植生図(6-7回)", 6, 0);
		model.setValueAt("https://map.ecoris.info/tiles/vege67/{z}/{x}/{y}.png", 6, 1);
		model.setValueAt(15, 6, 2);
		model.setValueAt("兵庫県全域DSM(平成22年度～令和元年度)", 7, 0);
		model.setValueAt("https://gio.pref.hyogo.lg.jp/tile/dsm/{z}/{y}/{x}.png", 7, 1);
		model.setValueAt(17, 7, 2);
		model.setValueAt("兵庫県全域DEM(平成22年度～令和元年度)", 8, 0);
		model.setValueAt("https://gio.pref.hyogo.lg.jp/tile/dem/{z}/{y}/{x}.png", 8, 1);
		model.setValueAt(17, 8, 2);
		
		JButton exec=new JButton("データ取得");
		exec.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String u=url.getText();
				if(u.equals("https://cyberjapandata.gsi.go.jp/xyz/dem5a_png/{z}/{x}/{y}.png")) {
					tile=new WebDemTile((int)zoom.getValue(),(double)res.getValue());
				}else {
					tile=new WebTile(url.getText(),	(int)zoom.getValue(),(double)res.getValue());
				}
				setVisible(false);
			}
		});
		exec.setFont(font);
		super.getContentPane().add(exec,BorderLayout.SOUTH);
		super.setSize(640, 640);
		table.getColumnModel().getColumn(2).setMaxWidth(80);
		table.getColumnModel().getColumn(2).setMinWidth(80);
		table.getColumnModel().getColumn(0).setMinWidth(200);
		table.getColumnModel().getColumn(0).setMaxWidth(200);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1&&e.getClickCount()>=2) {
					int row=table.getSelectedRow();
					String u=(String)model.getValueAt(row, 1);
					Number z=(Number)model.getValueAt(row, 2);
					zoom.setValue(z);
					url.setText(u);					
				}
			}			
		});
	    DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
	    tableCellRenderer.setHorizontalAlignment(JLabel.CENTER);    
	    TableColumn col = table.getColumnModel().getColumn(2);
	    col.setCellRenderer(tableCellRenderer);
		super.pack();
	}
	
	public static WebTile getTile(JFrame f) {
		WebTileDialog app=new WebTileDialog(f,true);
		app.setLocationRelativeTo(null);
		app.setVisible(true);
		return app.tile;
	}
	
	public static void main(String[] args) {
		WebTile wt=WebTileDialog.getTile(new JFrame());
		System.out.println(wt);
		Rectangle2D rect=new Rectangle2D.Double(-55121.73598,-85147.39639,707.516947,512.3398582);
		try {
			wt.create(8, rect);
			ImageIO.write(wt.getImage(), "png", new File("test3.png"));
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
