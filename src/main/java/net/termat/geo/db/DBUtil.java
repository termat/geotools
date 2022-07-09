package net.termat.geo.db;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.locationtech.jts.io.ParseException;

public class DBUtil {

	public static void dataIntegrade(PointCloudDB db,Index i1) throws SQLException, IOException, ParseException {
		if(!i1.name.endsWith("a"))return;
		String name=i1.name.substring(0, i1.name.length()-1);
		String type=i1.type;
		List<Index> l2=db.getIndexes(name+"b", type);
		List<Index> l3=db.getIndexes(name+"c", type);
		List<Index> l4=db.getIndexes(name+"d", type);
		if(l2.size()==0||l3.size()==0||l4.size()==0)throw new SQLException("data not found.");
		Index[] indexes=new Index[] {i1,l2.get(0),l3.get(0),l4.get(0)};
		if(i1.dataType==Index.DataType.IMAGE) {
			integradeImage(db,indexes,name);
		}else if(i1.dataType==Index.DataType.NPY) {

		}
	}
		
	private static void integradeImage(PointCloudDB db,Index[] indexes,String name) throws SQLException, IOException {
		Rectangle2D rect=indexes[0].getBounds();
		for(int i=1;i<indexes.length;i++) {
			rect.add(indexes[i].getBounds());
		}
		AffineTransform taf=indexes[0].getTransform();
		double[] p=new double[] {taf.getScaleX(),0,0,taf.getScaleY(),rect.getX(),rect.getY()+rect.getHeight()};
		AffineTransform af=new AffineTransform(p);
		int ww=(int)Math.ceil(rect.getWidth()/af.getScaleX());
		int hh=(int)Math.ceil(rect.getHeight()/af.getScaleX());
		if(ww%2==1)ww=ww-1;
		if(hh%2==1)hh=hh-1;
		BufferedImage img=new BufferedImage(ww,hh,BufferedImage.TYPE_INT_RGB);
		for(Index index : indexes) {
			BufferedImage src=db.getImage(index);
			AffineTransform at=index.getTransform();
			AffineTransform iaf=null;
			try {
				iaf=at.createInverse();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			for(int x=0;x<ww;x++) {
				for(int y=0;y<hh;y++) {
					Point2D pt=af.transform(new Point2D.Double(x, y),new Point2D.Double());
					pt=iaf.transform(pt, new Point2D.Double());
					int xx=(int)Math.round(pt.getX());
					int yy=(int)Math.round(pt.getY());
					if(xx>=0&&xx<src.getWidth()&&yy>=0&&yy<src.getHeight()) {
						img.setRGB(x, y, src.getRGB(xx, yy));
					}
				}
			}
		}
		db.add(indexes[0].coordSys, name, indexes[0].type, img, af);
		for(int i=0;i<indexes.length;i++)db.delete(indexes[i]);
	}
	
	public static void dataDivide(PointCloudDB db,Index ii) throws SQLException, IOException {
		int ww=ii.width/2;
		int hh=ii.height/2;
		String[] suffix=new String[]{"a","b","c","d"};
		AffineTransform af=ii.getTransform();
		AffineTransform[] nf=new AffineTransform[]{
			new AffineTransform(new double[]{af.getScaleX(),0,0,af.getScaleY(),af.getTranslateX(),af.getTranslateY()}),
			new AffineTransform(new double[]{af.getScaleX(),0,0,af.getScaleY(),af.getTranslateX()+ww*af.getScaleX(),af.getTranslateY()}),
			new AffineTransform(new double[]{af.getScaleX(),0,0,af.getScaleY(),af.getTranslateX(),af.getTranslateY()+hh*af.getScaleY()}),
			new AffineTransform(new double[]{af.getScaleX(),0,0,af.getScaleY(),af.getTranslateX()+ww*af.getScaleX(),af.getTranslateY()+hh*af.getScaleY()}),
		};
		int[][] ma=new int[][]{{0,0},{ww,0},{0,hh},{ww,hh}};
		if(ii.dataType==Index.DataType.IMAGE){
			BufferedImage src=db.getImage(ii);
			for(int i=0;i<suffix.length;i++){
				BufferedImage dst=new BufferedImage(ww,hh,BufferedImage.TYPE_INT_RGB);
				for(int x=0;x<ww;x++){
					for(int y=0;y<hh;y++){
						int xx=x+ma[i][0];
						int yy=y+ma[i][1];
						dst.setRGB(x, y, src.getRGB(xx, yy));
					}
				}
				db.add(ii.coordSys, ii.name+suffix[i], ii.type, dst, nf[i]);
			}
			db.delete(ii);
		}
	}
}
