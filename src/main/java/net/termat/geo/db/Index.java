package net.termat.geo.db;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class Index {
	public enum DataType{IMAGE,NPY,GEOJSON};

	@DatabaseField(generatedId=true)
	public long id;

	@DatabaseField
	public long key;

	@DatabaseField
	public String name;

	@DatabaseField
	public int coordSys;

	@DatabaseField
	public int width;

	@DatabaseField
	public int height;

    @DatabaseField
    public String af;

	@DatabaseField
	public String type;

	@DatabaseField
	public DataType dataType;

    public Rectangle2D getBounds(){
    	AffineTransform af=getTransform();
    	return af.createTransformedShape(new Rectangle2D.Double(0,0,width,height)).getBounds2D();
    }

    public AffineTransform getTransform(){
       	String[] ss=af.split(",");
    	double[] ret=new double[ss.length];
    	for(int i=0;i<ret.length;i++){
    		ret[i]=Double.parseDouble(ss[i]);
    	}
    	return new AffineTransform(ret);
    }

    public void setTransform(AffineTransform a){
    	af=Double.toString(a.getScaleX())+",0,0,"+Double.toString(a.getScaleY())+",";
    	af=af+Double.toString(a.getTranslateX())+","+Double.toString(a.getTranslateY());
    }

    public Date getDate(){
    	return new Date(key);
    }
}
