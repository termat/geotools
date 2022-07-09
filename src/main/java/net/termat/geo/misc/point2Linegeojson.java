package net.termat.geo.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class point2Linegeojson {

	public static void main(String[] args){
		File f=new File("D:/Documents/自転車/ポタリング/20210822.geojson");
		try {
			FeatureCollection ret=FeatureCollection.fromJson(getString(f));
			List<Feature> fs=ret.features();

			Map<String,Object> root=new HashMap<String,Object>();
			root.put("type","FeatureCollection");
			root.put("crs","{ \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } }");
			List<Map<String,Object>> fl=new ArrayList<>();
			root.put("features", fl);
			root.put("name",f.getName());
			int n=fs.size();
			Map<String,Object> obj=new HashMap<>();
			Map<String,Object> geo=new HashMap<>();
			Map<String,Object> prop=new HashMap<>();
			obj.put("type", "Feature");
			obj.put("properties", prop);
			obj.put("geometry", geo);
			geo.put("type", "LineString");
			List<double[]> tmp=new ArrayList<>();
			for(int i=0;i<n;i++){
				Feature f2=fs.get(i);
				Point p2=(Point)f2.geometry();
				tmp.add(new double[]{p2.longitude(),p2.latitude()});
			}
			geo.put("coordinates",tmp.toArray(new double[tmp.size()][]));
			fl.add(obj);
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File(f.getAbsolutePath().replace(".tcx", ".geojson"))));
			Gson gson=new GsonBuilder().setPrettyPrinting().create();
			bw.write(gson.toJson(root));
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getString(File f) throws IOException{
		StringBuffer sb=new StringBuffer();
		String line=null;
		BufferedReader br=new BufferedReader(new FileReader(f));
		while((line=br.readLine())!=null){
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
}
