package net.termat.geo.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tcx2Geojson {

	public static void main(String[] args){
		File f=new File("D:\\Documents\\自転車\\20220806.tcx");
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
				/*
				nl=e.getElementsByTagName("Time");
				n=(Element)nl.item(0);
				long date=Date.parse(n.getTextContent());
				*/
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
