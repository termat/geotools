package net.termat.geo.web;

import java.awt.geom.Point2D;

public class WebUtil {
	private static final double L=85.05112877980659;
	
	public static Point2D lonlatToPixel(int zoom,Point2D p){
		long x=(long)(Math.pow(2, zoom+7)*(p.getX()/180.0+1.0));
		long y=(long)((Math.pow(2, zoom+7)/Math.PI)*(-atanh(Math.sin(Math.toRadians(p.getY())))+atanh(Math.sin(Math.toRadians(L)))));
		return new Point2D.Double(x,y);
	}

	public static Point2D pixelToLonlat(int zoom,long pixelX,long pixelY){
		double zz=Math.pow(2, zoom+7);
		double lon=180*(((double)pixelX/zz)-1);
		double p1=Math.sin(Math.PI/180*L);
		double p2=atanh(p1);
		double p3=Math.tanh((-Math.PI/zz)*(double)pixelY+p2);
		double p4=Math.asin(p3);
		double lat=(180/Math.PI)*p4;
		return new Point2D.Double(lon,lat);
	}
	
	private static double atanh(double v){
		return 0.5*Math.log((1.0+v)/(1.0-v));
	}
	
}
