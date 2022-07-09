package net.termat.geo.geojson;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;

import net.termat.geo.util.LonLatXY;

public class GeojsonUtil {
	
	public static void main(String[] args) {
		File f=new File("F:/20210831/20210831/tmp/06od162a_CONTOUR.geojson");
		String line=null;
		try {
			StringBuffer buf=new StringBuffer();
			BufferedReader br=new BufferedReader(new FileReader(f));
			while((line=br.readLine())!=null) {
				buf.append(line);
			}
			br.close();
			String dst=geojsonTransWGS84(buf.toString());
			f=new File("F:/20210831/20210831/tmp/06od162a_CONTOUR2.geojson");
			BufferedWriter bw=new BufferedWriter(new FileWriter(f));
			bw.write(dst);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	public static String geojsonTransWGS84(String geojson) {
		Gson gson=new Gson();
		Map<String,Object> root=gson.fromJson(geojson, Map.class);
		Map<String,Object> crs=(Map<String,Object>)root.get("crs");
		Map<String,Object> tmp=(Map<String,Object>)crs.get("properties");
		root.remove("crs");
		String cs=(String)tmp.get("name");
		cs=cs.replace("urn:ogc:def:crs:EPSG::", "");
		int coordSys=Integer.parseInt(cs)-6668;
		List<Map<String,Object>> fs=(List<Map<String,Object>> )root.get("features");
		for(Map<String,Object> f : fs) {
			Map<String,Object> geo=(Map<String,Object>)f.get("geometry");
			List<Object> li=(List<Object>)geo.get("coordinates");
			li=ceckList(li,coordSys);
		}
		return gson.toJson(root);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Object> ceckList(List<Object> li,int coordSys){
		for(Object o : li) {
			if(o instanceof List) {
				List ll=(List)o;
				if(ll.size()==2&&ll.get(0) instanceof Double) {
					double x=(Double)ll.remove(0);
					double y=(Double)ll.remove(0);
					Point2D p=LonLatXY.xyToLonlat(coordSys, x, y);
					ll.add(p.getX());
					ll.add(p.getY());
				}else {
					ceckList(ll,coordSys);
				}
			}
		}
		return li;
	}
	
	public static com.mapbox.geojson.Polygon createPolygon(org.locationtech.jts.geom.Geometry geo){
		GeoJsonWriter gr=new GeoJsonWriter();
		String json=gr.write(geo);
		return com.mapbox.geojson.Polygon.fromJson(json);
	}
	
	public static org.locationtech.jts.geom.Geometry createGeom(com.mapbox.geojson.Geometry geo){
		GeoJsonReader gr=new GeoJsonReader();
		try {
			org.locationtech.jts.geom.Geometry geom=gr.read(geo.toJson());
			return geom;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static org.locationtech.jts.geom.Geometry getGeom(Feature f){
		Geometry go=f.geometry();
		return createGeom(go);
	}

	public static FeatureCollection loadGeojson(File f) throws IOException{
		FeatureCollection ret=FeatureCollection.fromJson(getString(f));
		return ret;
	}
	
	public static FeatureCollection loadGeojson(String str){
		FeatureCollection ret=FeatureCollection.fromJson(str);
		return ret;
	}

	public static String getString(File f) throws IOException{
		StringBuffer sb=new StringBuffer();
		String line=null;
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
		while((line=br.readLine())!=null){
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}

	public static void createNewProperties(FeatureCollection fc,String key,Object val){
		List<Feature> fl=fc.features();
		for(Feature f : fl){
			JsonObject jo=f.properties();
			if(val instanceof Number){
				jo.addProperty(key, (Number)val);
			}else if(val instanceof String){
				jo.addProperty(key, (String)val);
			}else if(val instanceof Boolean){
				jo.addProperty(key, (Boolean)val);
			}else if(val instanceof Character){
				jo.addProperty(key, (Character)val);
			}else{
				jo.add(key, null);
			}
		}
	}

	public static void transPropetyValue(FeatureCollection fc,String prop_name,Map<Object,Object> o){
		List<Feature> fl=fc.features();
		for(Feature f : fl){
			JsonObject jo=f.properties();
			JsonElement oe=jo.get(prop_name);
			if(o.containsKey(oe)){
				jo.add(prop_name, (JsonElement)o.get(oe));
			}
		}
	}

	public static JsonElement create(Object o){
		if(o instanceof Number){
			return new JsonPrimitive((Number)o);
		}else if(o instanceof String){
			return new JsonPrimitive((String)o);
		}else if(o instanceof Character){
			return new JsonPrimitive((Character)o);
		}else if(o instanceof Boolean){
			return new JsonPrimitive((Boolean)o);
		}else{
			return null;
		}
	}

	public static void removePropety(FeatureCollection fc,String name){
		List<Feature> fl=fc.features();
		for(Feature f : fl){
			JsonObject jo=f.properties();
			jo.remove(name);
		}
	}

	public static void changePropetyName(FeatureCollection fc,String oldName,String newName){
		List<Feature> fl=fc.features();
		for(Feature f : fl){
			JsonObject jo=f.properties();
			JsonElement je=jo.get(oldName);
			jo.add(newName, je);
			jo.remove(oldName);
		}
	}

	public static void output(FeatureCollection fc,File f) throws IOException{
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
		bw.write(fc.toJson());
		bw.close();
	}

	public static Polygon shpToPoly(Shape gp){
		AffineTransform af=AffineTransform.getScaleInstance(1.0, 1.0);
		PathIterator pi=gp.getPathIterator(af);
		List<Coordinate> coord=new ArrayList<Coordinate>();
		GeometryFactory gf=new GeometryFactory();
		double[] p=new double[6];
		while(!pi.isDone()){
			switch(pi.currentSegment(p)){
				case PathIterator.SEG_MOVETO:
					coord.add(new Coordinate(p[0],p[1]));
					break;
				case PathIterator.SEG_LINETO:
					coord.add(new Coordinate(p[0],p[1]));
					break;
				case PathIterator.SEG_QUADTO:
					coord.add(new Coordinate(p[2],p[3]));
					break;
				case PathIterator.SEG_CUBICTO:
					coord.add(new Coordinate(p[4],p[5]));
					break;
				case PathIterator.SEG_CLOSE:
					Coordinate c=coord.get(0);
					coord.add(new Coordinate(c.x,c.y));
					break;
			}
			pi.next();
		}
		return gf.createPolygon(coord.toArray(new Coordinate[coord.size()]));
	}

	public static GeneralPath shpToGp(Shape sp){
		AffineTransform af=AffineTransform.getScaleInstance(1.0, 1.0);
		PathIterator pi=sp.getPathIterator(af);
		GeneralPath gp=new GeneralPath();
		double[] p=new double[6];
		while(!pi.isDone()){
			switch(pi.currentSegment(p)){
				case PathIterator.SEG_MOVETO:
					gp.moveTo(p[0],p[1]);
					break;
				case PathIterator.SEG_LINETO:
					gp.lineTo(p[0],p[1]);
					break;
				case PathIterator.SEG_CLOSE:
					gp.closePath();
					break;
			}
			pi.next();
		}
		return gp;
	}

	public static Shape geom2Shape(org.locationtech.jts.geom.Geometry g){
		 ShapeWriter writer=new ShapeWriter();
		 return writer.toShape(g);
	}

	public static List<Shape> getShapes(String geojson){
		List<Shape> ret=new ArrayList<>();
		FeatureCollection fc=loadGeojson(geojson);
		for(Feature f :fc.features()) {
			com.mapbox.geojson.Geometry g=f.geometry();
			org.locationtech.jts.geom.Geometry g2=createGeom(g);
			ret.add(geom2Shape(g2));	
		}
		return ret;
	}
	
	public static double[][] getPointsWGS84(String geojson,int coordSys){
		Gson gson=new Gson();
		List<double[]> ret=new ArrayList<>();
		FeatureCollection fc=loadGeojson(geojson);
		for(Feature f :fc.features()) {
			com.mapbox.geojson.Geometry g=f.geometry();
			Point2D p=getPointWGS84(g.toJson(),gson,coordSys);
			String h=f.getStringProperty("h");
			String dem=f.getStringProperty("dem");
			if(h==null||dem==null)continue;
			double hv=Double.parseDouble(h);
			double dv=Double.parseDouble(dem);
			double[] pt=new double[] {
				p.getX(),
				p.getY(),
				hv+dv,
				hv
			};
			ret.add(pt);	
		}
		return ret.toArray(new double[ret.size()][]);
	}
	
	public static double[][] getPoints(String geojson){
		Gson gson=new Gson();
		List<double[]> ret=new ArrayList<>();
		FeatureCollection fc=loadGeojson(geojson);
		for(Feature f :fc.features()) {
			com.mapbox.geojson.Geometry g=f.geometry();
			List<Double> p=getPoint(g.toJson(),gson);
			String h=f.getStringProperty("h");
			String dem=f.getStringProperty("dem");
			if(h==null||dem==null)continue;
			double hv=Double.parseDouble(h);
			double dv=Double.parseDouble(dem);
			double[] pt=new double[] {
				p.get(0),
				p.get(1),
				hv+dv,
				hv
			};
			ret.add(pt);	
		}
		return ret.toArray(new double[ret.size()][]);
	}
	
	@SuppressWarnings("unchecked")
	private static List<Double> getPoint(String json,Gson gson) {
		Map<String,Object> o=gson.fromJson(json, Map.class);
		return (List<Double>)o.get("coordinates");
	}
	
	@SuppressWarnings("unchecked")
	private static Point2D getPointWGS84(String json,Gson gson,int coordSys) {
		Map<String,Object> o=gson.fromJson(json, Map.class);
		List<Double>d=(List<Double>)o.get("coordinates");
		return LonLatXY.xyToLonlat(coordSys, d.get(0), d.get(1));
	}

	public static BufferedImage createImage(GeojsonProcesser gp,double resolution,boolean isFill,Stroke stroke,Color back,Color color) {
		Rectangle2D r=getBounds(gp);
		AffineTransform af=getTransform(gp,resolution);
		int ww=(int)Math.ceil(r.getWidth()*af.getScaleX());
		int hh=(int)Math.ceil(r.getHeight()*af.getScaleX());
		BufferedImage ret=new BufferedImage(ww,hh,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ret.createGraphics();
		g.setBackground(back);
		g.clearRect(0, 0, ww, hh);
		AffineTransform iaf=null;
		try {
			iaf=af.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		g.setColor(color);
		g.setStroke(stroke);
		for(Shape s : gp.getShapes()) {
			Shape sp=iaf.createTransformedShape(s);
			if(isFill) {
				g.fill(sp);
			}else {
				g.draw(s);
			}
		}
		g.dispose();
		return ret;
	}
	
	public static AffineTransform getTransform(GeojsonProcesser gp,double resolution) {
		Rectangle2D r=getBounds(gp);
		double[] p=new double[] {resolution,0,0,-resolution,r.getX(),r.getY()+r.getHeight()};
		AffineTransform af=new AffineTransform(p);
		return af;
	}
	
	public static Rectangle2D getBounds(GeojsonProcesser gp) {
		Rectangle2D ret=null;
		for(Shape s : gp.getShapes()) {
			if(ret==null) {
				ret=s.getBounds2D();
			}else {
				ret.add(s.getBounds2D());
			}
		}
		return ret;
	}
	
	public static org.locationtech.jts.geom.Geometry union(org.locationtech.jts.geom.Geometry p1,org.locationtech.jts.geom.Geometry p2){
//		org.locationtech.jts.geom.Geometry g1=p1.convexHull();
//		org.locationtech.jts.geom.Geometry g2=p2.convexHull();
		org.locationtech.jts.geom.Geometry g3=p1.union(p2);
		return g3;
	}
	
	public static Polygon createGeoPolygon(org.locationtech.jts.geom.Geometry g3) {
		Coordinate[] c1=g3.getCoordinates();
		GeometryFactory gf=new GeometryFactory();
		Polygon p3=gf.createPolygon(c1);
		return p3;
	}
	
	public static org.locationtech.jts.geom.Geometry mapboxMPToGeometry(com.mapbox.geojson.MultiPolygon g){
		org.locationtech.jts.geom.MultiPolygon mp=(org.locationtech.jts.geom.MultiPolygon)mapboxToGeometry(g);
		org.locationtech.jts.geom.Geometry ret=null;
		int n=mp.getNumGeometries();
		for(int i=0;i<n;i++) {
			if(i==0) {
				ret=mp.getGeometryN(i);
			}else {
				ret=union(ret,mp.getGeometryN(i));
			}
		}
		return ret;
	}
	
	public static org.locationtech.jts.geom.Geometry mapboxToGeometry(com.mapbox.geojson.Geometry g){
		return createGeom(g);
	}
	
	public static com.mapbox.geojson.Geometry geometryToMapbox(org.locationtech.jts.geom.Geometry g){
		GeoJsonWriter gr=new GeoJsonWriter();
		String json=gr.write(g);
		return com.mapbox.geojson.Polygon.fromJson(json);
	}
}
