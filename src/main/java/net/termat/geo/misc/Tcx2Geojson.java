package net.termat.geo.misc;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tcx2Geojson {
	private JFrame frame;
	
	public Tcx2Geojson() {
		frame=new JFrame();
		frame.setTitle("Tcx2Geojson");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			SwingUtilities.updateComponentTreeUI(frame);
		}catch(Exception e){
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				SwingUtilities.updateComponentTreeUI(frame);
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}
		WindowAdapter wa=new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		};
		frame.addWindowListener(wa);
		frame.setSize(400, 400);
		frame.setResizable(false);
		JLabel la=new JLabel("Drop");
		la.setFont(new Font(Font.SANS_SERIF,Font.BOLD,24));
		la.setHorizontalAlignment(JLabel.CENTER);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(la);
		
		DropTargetListener dtl = new DropTargetAdapter() {
			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY);
					return;
				}
			    dtde.rejectDrag();
			}

			@SuppressWarnings("rawtypes")
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					Transferable transferable = dtde.getTransferable();
					List list = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
						for (Object o: list) {
							if (o instanceof File) {
								File file = (File) o;
								proc(file);
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
		new DropTarget(la, DnDConstants.ACTION_COPY, dtl, true);
	}
	
	private void close(){
		int id=JOptionPane.showConfirmDialog(frame, "Exit?", "Info", JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
		if(id==JOptionPane.YES_OPTION){
			frame.setVisible(false);
			System.exit(0);
		}
	}
	
	public static void main(String[] arg) {
		Tcx2Geojson app=new Tcx2Geojson();
		app.frame.setLocationRelativeTo(null);
		app.frame.setVisible(true);
	}

	public void proc(File f){
		if(!f.getName().endsWith(".tcx"))return;
		try{
			Map<String,Object> root=new HashMap<String,Object>();
			root.put("type","FeatureCollection");
			root.put("crs","{ \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } }");
			List<Map<String,Object>> fl=new ArrayList<>();
			root.put("features", fl);
			root.put("name",f.getName());
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			NodeList list=doc.getElementsByTagName("Trackpoint");
			for(int i=0;i<list.getLength();i++){
				Element e=(Element)list.item(i);
				NodeList nl=e.getElementsByTagName("LatitudeDegrees");
				Element n=(Element)nl.item(0);
				double lat=Double.parseDouble(n.getTextContent());
				nl=e.getElementsByTagName("LongitudeDegrees");
				n=(Element)nl.item(0);
				double lon=Double.parseDouble(n.getTextContent());
				nl=e.getElementsByTagName("AltitudeMeters");
				n=(Element)nl.item(0);
				double alt=Double.parseDouble(n.getTextContent());
				nl=e.getElementsByTagName("ns3:Speed");
				n=(Element)nl.item(0);
				double spd=Double.parseDouble(n.getTextContent());
				Map<String,Object> obj=new HashMap<>();
				Map<String,Object> geo=new HashMap<>();
				Map<String,Object> prop=new HashMap<>();
				obj.put("type", "Feature");
				obj.put("properties", prop);
				obj.put("geometry", geo);
				geo.put("type", "Point");
				geo.put("coordinates",new double[]{lon,lat});
				prop.put("acc", 0.0);
				prop.put("alt", alt);
				prop.put("spd", spd);
				prop.put("time", 0);
				fl.add(obj);
			}
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File(f.getAbsolutePath().replace(".tcx", ".geojson"))));
			Gson gson=new GsonBuilder().setPrettyPrinting().create();
			bw.write(gson.toJson(root));
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
