package net.termat.components.image;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FeatureFinder {
	private BufferedImage image;
	private int width;
	private int height;
	private int[][] px;
	
	public FeatureFinder(BufferedImage im){
		image=im;
		width=image.getWidth();
		height=image.getHeight();
		px=new int[width][height];
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				int n=image.getRGB(x,y);
				int val=(n>>16&0xff);
				if(val==255){
					px[x][y]=0;
				}else{
					px[x][y]=1;
				}
			}
		}
	}
	
	public List<Point2D> getFeature(){
		List<Feature> tmp=getFeature(width,height,px);
		List<Point2D> ret=new ArrayList<Point2D>();
		if(tmp.size()>0){
			Iterator<Feature> it=tmp.iterator();
			while(it.hasNext()){
				Feature f=(Feature)it.next();
				ret.add(new Point2D.Double(f.getX(),f.getY()));
			}
			
		}
		return ret;
	}

	private static final List<Feature> getFeature(int width,int height,int[][] binary){
		List<Feature> list=new ArrayList<Feature>();
		for(int y=1;y<height-1;y++){
			for(int x=1;x<width-1;x++){
				if(binary[x][y]==1){
					int val=binary[x+1][y]+binary[x][y-1]+binary[x-1][y]+binary[x][y+1];
					if(val==1||val==3||val==4){
						Feature f=new Feature(x,y,val);
						list.add(f);
					}
				}
			}
		}
		return list;
	}
	
}
