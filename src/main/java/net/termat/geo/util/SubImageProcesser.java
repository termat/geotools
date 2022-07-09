package net.termat.geo.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class SubImageProcesser {

	private Map<File,Rectangle2D> area=new HashMap<>();
	private Map<File,AffineTransform> trans=new HashMap<>();

	public SubImageProcesser(File dir) throws IOException{
		for(File f : dir.listFiles()){
			String name=f.getName().toLowerCase();
			if(name.endsWith(".png")){
				BufferedImage img=ImageIO.read(f);
				File t=new File(f.getAbsolutePath().replace(".png", ".pgw").replace(".PNG", ".pgw"));
				AffineTransform af=PCUtil.loadTransform(t);
				trans.put(f, af);
				area.put(f, af.createTransformedShape(new Rectangle2D.Double(0,0,img.getWidth(),img.getHeight())).getBounds2D());
			}else if(name.endsWith(".jpg")){
				BufferedImage img=ImageIO.read(f);
				File t=new File(f.getAbsolutePath().replace(".jpg", ".jgw").replace(".JPG", ".jgw"));
				AffineTransform af=PCUtil.loadTransform(t);
				trans.put(f, af);
				area.put(f, af.createTransformedShape(new Rectangle2D.Double(0,0,img.getWidth(),img.getHeight())).getBounds2D());
			}
		}
	}

	public BufferedImage create(Rectangle2D rect,AffineTransform af) throws IOException{
		int w=(int)Math.abs(rect.getWidth()/af.getScaleX());
		int h=(int)Math.abs(rect.getHeight()/af.getScaleY());
		BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		List<File> li=new ArrayList<File>();
		for(File t : area.keySet()){
			Rectangle2D r=area.get(t);
			if(rect.intersects(r))li.add(t);
		}
		for(File t : li){
			Point2D pt=new Point2D.Double();
			Rectangle2D r=area.get(t);
			try {
				BufferedImage tmp=ImageIO.read(t);
				AffineTransform at=trans.get(t);
				AffineTransform iat=at.createInverse();
				for(int i=0;i<w;i++){
					for(int j=0;j<h;j++){
						Point2D p=af.transform(new Point2D.Double(i,j), pt);
						if(!r.contains(p))continue;
						p=iat.transform(p, new Point2D.Double());
						int xx=(int)Math.floor(p.getX());
						int yy=(int)Math.floor(p.getY());
						if(xx>=0&&xx<tmp.getWidth()&&yy>=0&&yy<tmp.getHeight()){
							img.setRGB(i, j, tmp.getRGB(xx, yy));
						}
					}
				}
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
		return img;
	}

}
