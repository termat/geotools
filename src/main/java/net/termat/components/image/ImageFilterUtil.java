package net.termat.components.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;


public class ImageFilterUtil {

	private ImageFilterUtil(){}

	public static BufferedImage createBinary(BufferedImage img){
		BinaryFilter f=new BinaryFilter();
		return createImage(img,f);
	}
	
	public static BufferedImage createTinning(BufferedImage img,Color back){
		ThinningFilter f=new ThinningFilter(back);
		return createImage(img,f);
	}

	public static BufferedImage createReverse(BufferedImage img){
		RGBImageFilter f=new RGBImageFilter(){
			@Override
			public int filterRGB(int x, int y, int rgb) {
				super.canFilterIndexColorModel=true;
				int t,r,g,b;
				t=(rgb>>24&0xff);
				r=(rgb>>16&0xff);
				g=(rgb>>8&0xff);
				b=(rgb&0xff);
				r=255-r;
				g=255-g;
				b=255-b;
				int ret=(t<<24)+(r<<16)+(g<<8)+b;
				return ret;
			}
		};
		return createImage(img,f);
	}

	public static BufferedImage createGray(BufferedImage img){
		RGBImageFilter f=new RGBImageFilter(){
			@Override
			public int filterRGB(int x, int y, int rgb) {
				super.canFilterIndexColorModel=true;
				int r=(rgb>>16&0xff);
				int g=(rgb>>8&0xff);
				int b=(rgb&0xff);
				r=(r+g+b)/3;
				int ret=(255<<24)+(r<<16)+(r<<8)+r;
				return ret;
			}
		};
		return createImage(img,f);
	}

	public static BufferedImage createGamma(BufferedImage img,double ganma){
		ImageFilter f=new RGBImageFilter(){
			@Override
			public int filterRGB(int x, int y, int rgb) {
				super.canFilterIndexColorModel=true;
				int t,r,g,b;
				t=(rgb>>24&0xff);
				r=(int)(255*Math.pow(((double)(rgb>>16&0xff)/255),1/ganma));
				g=(int)(255*Math.pow(((double)(rgb>>8&0xff)/255),1/ganma));
				b=(int)(255*Math.pow(((double)(rgb&0xff)/255),1/ganma));
				int ret=(t<<24)+(r<<16)+(g<<8)+b;
				return ret;
			}
		};
		return createImage(img,f);
	}

	public static BufferedImage createExpansion(BufferedImage img){
		ExpansionFilter f=new ExpansionFilter(ExpansionFilter.Operator.EXPANSION);
		return createImage(img,f);
	}

	public static BufferedImage createContraction(BufferedImage img){
		ExpansionFilter f=new ExpansionFilter(ExpansionFilter.Operator.CONTRACTION);
		return createImage(img,f);
	}

	public static BufferedImage createMedian(BufferedImage img){
		SptialFilter f=new SptialFilter(SptialFilter.OP_MEDIAN);
		return createImage(img,f);
	}

	public static BufferedImage createSobel(BufferedImage img){
		SptialFilter f=new SptialFilter(SptialFilter.OP_SOBEL);
		return createImage(img,f);
	}

	private static BufferedImage createImage(BufferedImage img,ImageFilter f){
		ImageProducer ip = new FilteredImageSource(img.getSource() , f);
		Image ii=Toolkit.getDefaultToolkit().createImage(ip);
		BufferedImage ret=new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g=ret.createGraphics();
		g.drawImage(ii,AffineTransform.getScaleInstance(1.0, 1.0), null);
		g.dispose();
		return ret;
	}
}
