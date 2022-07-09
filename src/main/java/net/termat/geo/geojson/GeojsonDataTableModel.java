package net.termat.geo.geojson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

public class GeojsonDataTableModel extends DefaultTableModel{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private FeatureCollection collection;
	private String[] title;

	public GeojsonDataTableModel(FeatureCollection v){
		super();
		collection=v;
		initModel();
	}

	public String getGeoJson(){
		return collection.toJson();
	}

	public void loadGeoJson(File f)throws Exception{
		collection=GeojsonUtil.loadGeojson(f);
		initModel();
	}

	public void changePropertyName(String src,String dst){
		List<Feature> list=collection.features();
		for(Feature f : list){
			JsonObject jo=f.properties();
			jo.add(dst, jo.get(src));
			jo.remove(src);
		}
	}

	public void update(){
		List<Feature> list=collection.features();
		for(int i=0;i<super.getRowCount();i++){
			Feature f=list.get(i);
			for(int j=0;j<super.getColumnCount();j++){
				Object o=super.getValueAt(i, j);
				JsonObject jo=f.properties();
				if(o instanceof Number){
					jo.addProperty(title[j], (Number)o);
				}else if(o instanceof Boolean){
					jo.addProperty(title[j], (Boolean)o);
				}else if(o instanceof String){
					jo.addProperty(title[j], (String)o);
				}else if(o instanceof Character){
					jo.addProperty(title[j], (Character)o);
				}
			}
		}
	}

	private void initModel(){
		List<Feature> list=collection.features();
		super.setRowCount(list.size());
		if(list.size()==0)return;
		Feature f=list.get(0);
		JsonObject jo=f.properties();
		Set<String> keyset=jo.keySet();
		String[] str=keyset.toArray(new String[keyset.size()]);
		Arrays.sort(str);
		List<String> keylist=new ArrayList<String>();
		for(int i=0;i<str.length;i++){
			if(str[i].toLowerCase().equals("id")){
				keylist.add(0,str[i]);
			}else{
				keylist.add(str[i]);
			}
		}
		title=keylist.toArray(new String[keylist.size()]);
		this.setColumnIdentifiers(title);
		super.setColumnCount(title.length);
		int row=0;
		for(Feature fc : list){
			for(int i=0;i<title.length;i++){
				super.setValueAt(fc.getProperty(title[i]), row, i);
			}
			row++;
		}
	}

}
