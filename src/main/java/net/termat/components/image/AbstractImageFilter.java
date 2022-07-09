package net.termat.components.image;

import java.awt.Dimension;
import java.awt.image.ColorModel;
import java.awt.image.ImageFilter;

public abstract class AbstractImageFilter extends ImageFilter {
	protected int pixel[];
	protected Dimension size;
	protected static ColorModel colorModel=ColorModel.getRGBdefault();

	public void setDimensions(int w,int h){
		pixel=new int[w*h];
		size=new Dimension(w,h);
	}

	public void setPixels(int x,int y,int w,int h,ColorModel model,byte[] pixels,int offset,int scansize){
		for (int i=0;i<=h-1;i++){
			for (int j=0;j<=w-1;j++){
				pixel[x+j+(y+i)*size.width]=model.getRGB(pixels[offset+j+i*scansize]&0xff);
			}
		}
	}

	public void setPixels(int x,int y,int w,int h,ColorModel model,int[] pixels,int offset,int scansize){
		if(model==colorModel){
			for (int i=0;i<=h-1;i++){
				System.arraycopy(pixels,offset+i*scansize,pixel,x+(y+i)*size.width,w);
			}
		}else{
			for(int i=0;i<=h-1;i++){
				for (int j=0;j<=w-1;j++){
					pixel[x+j+(y+i)*size.width]=model.getRGB(pixels[offset+j+i*scansize]);
				}
			}
		}
	}

	public void setHints(int h){
		consumer.setHints(SINGLEPASS|(h&SINGLEFRAME));
	}

	public void setColorModel(ColorModel model){
		consumer.setColorModel(colorModel);
	}

	public void imageComplete(int status){
		if (status==IMAGEERROR||status==IMAGEABORTED){
			consumer.imageComplete(status);
			return;
		}
		filterImage();
		consumer.imageComplete(status);
	}

	protected int getPixelValue(int x,int y){
		return pixel[x+y*size.width];
	}

	protected int[] getPixelRGBValue(int x,int y){
		int p=pixel[x+y*size.width];
		int t=(p>>24&0xff);
		int r=(p>>16&0xff);
		int g=(p>>8&0xff);
		int b=(p&0xff);
		return new int[]{t,r,g,b};
	}

	protected static int[] getPixelRGBValue(int val){
		int t=(val>>24&0xff);
		int r=(val>>16&0xff);
		int g=(val>>8&0xff);
		int b=(val&0xff);
		return new int[]{t,r,g,b};
	}

	protected abstract void filterImage();

}
