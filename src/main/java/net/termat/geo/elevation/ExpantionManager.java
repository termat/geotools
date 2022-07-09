package net.termat.geo.elevation;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.termat.geo.util.PCUtil;

public class ExpantionManager {

	private Map<File,Rectangle2D> area=new HashMap<>();
	private Map<File,AffineTransform> trans=new HashMap<>();
	private BufferedImage tmp;
	private AffineTransform src;
	private AffineTransform dst;
	private AffineTransform iaf;
	private Rectangle2D bounds;
	private Rectangle2D imagebounds;
	private int expantion;

	public ExpantionManager(File dir) throws IOException{
		for(File f : dir.listFiles()){
			String name=f.getName().toLowerCase();
			if(name.endsWith(".png")){
				BufferedImage img=ImageIO.read(f);
				File t=new File(f.getAbsolutePath().replace(".png", ".pgw").replace(".PNG", ".pgw"));
				AffineTransform af=PCUtil.loadTransform(t);
				trans.put(f, af);
				area.put(f, af.createTransformedShape(new Rectangle2D.Double(0,0,img.getWidth(),img.getHeight())).getBounds2D());
			}
		}
	}

	public BufferedImage getExpImage(){
		return tmp;
	}

	public AffineTransform getExpTransform(){
		return dst;
	}

	public BufferedImage getSubImage(BufferedImage img){
		int w=img.getWidth();
		int h=img.getHeight();
		return img.getSubimage(expantion, expantion, w-2*expantion, h-2*expantion);
	}

	public double[][] getSubTable(double[][] data){
		int w=data.length-expantion*2;
		int h=data[0].length-expantion*2;
		double[][] ret=new double[w][h];
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				ret[i][j]=data[i+expantion][j+expantion];
			}
		}
		return ret;
	}

	public AffineTransform getSrcTransform(){
		return src;
	}

	public void create(File f,int expantion) throws IOException{
		this.expantion=expantion;
		init(f,expantion);
		for(File t : area.keySet()){
			if(f.equals(t))continue;
			Rectangle2D r=area.get(t);
			if(bounds.intersects(r)){
				BufferedImage im=ImageIO.read(t);
				AffineTransform at=trans.get(t);
				draw(im,at);
			}
		}
	}

	private void init(File f,int expantion) throws IOException{
		BufferedImage img=ImageIO.read(f);
		File t=new File(f.getAbsolutePath().replace(".png", ".pgw").replace(".PNG", ".pgw"));
		src=PCUtil.loadTransform(t);
		tmp=new BufferedImage(img.getWidth()+2*expantion,img.getHeight()+2*expantion,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<tmp.getWidth();i++){
			for(int j=0;j<tmp.getHeight();j++){
				tmp.setRGB(i, j, PCUtil.NA);
			}
		}
		double sx=src.getScaleX();
		double sy=src.getScaleY();
		double tx=src.getTranslateX();
		double ty=src.getTranslateY();
		tx=tx-expantion*sx;
		ty=ty-expantion*sy;
		dst=new AffineTransform(new double[]{sx,0,0,sy,tx,ty});
		try {
			iaf=dst.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		imagebounds=new Rectangle2D.Double(0,0,tmp.getWidth(),tmp.getHeight());
		bounds=dst.createTransformedShape(imagebounds).getBounds2D();
		draw(img,src);
	}

	private void draw(BufferedImage img,AffineTransform af){
		Point2D sp=new Point2D.Double();
		Point2D dp=new Point2D.Double();
		int w=img.getWidth();
		int h=img.getHeight();
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				int col=img.getRGB(i, j);
				if(col==PCUtil.NA)continue;
				Point2D p=af.transform(new Point2D.Double(),sp);
				if(bounds.contains(p)){
					p=iaf.transform(p, dp);
					int xx=(int)Math.round(p.getX());
					int yy=(int)Math.round(p.getY());
					if(imagebounds.contains(xx, yy)){
						tmp.setRGB(xx, yy, col);
					}
				}
			}
		}
	}
}
