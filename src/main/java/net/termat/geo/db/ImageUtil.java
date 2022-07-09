package net.termat.geo.db;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * 画像処理ユーティリティクラス
 * @author t-matsuoka
 * @version 0.5
 */
public class ImageUtil {

	private ImageUtil(){}

	/**
	 * BufferedImageをbyte[]に変換
	 * @param img BufferedImage
	 * @param ext 画像の拡張子
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] bi2Bytes(BufferedImage img,String ext)throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, ext, baos );
		baos.flush();
		return baos.toByteArray();
	}

	/**
	 * byte[]をBufferedImageに変換
	 * @param raw byte[]
	 * @return BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage bytes2Bi(byte[] raw)throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		BufferedImage img=ImageIO.read(bais);
		return img;
	}

	/**
	 * 画像を乗算
	 * @param im1 画像1
	 * @param im2 画像2
	 * @return
	 */
	public static BufferedImage mul(BufferedImage im1,BufferedImage im2){
		int w=im1.getWidth();
		int h=im1.getHeight();
		BufferedImage ret=new BufferedImage(w,h,im1.getType());
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				Color c1=new Color(im1.getRGB(i, j));
				Color c2=new Color(im2.getRGB(i, j));
				ret.setRGB(i, j, mul(c1,c2).getRGB());
			}
		}
		return ret;
	}

	private static Color mul(Color c1,Color c2){
		float[] f1=c1.getRGBComponents(new float[4]);
		float[] f2=c2.getRGBComponents(new float[4]);
		for(int i=0;i<f1.length;i++){
			f1[i]=f1[i]*f2[i];
		}
		return new Color(f1[0],f1[1],f1[2],f1[3]);
	}

	/**
	 * 3×3ガルシアンフィルタを適用
	 * @param img 画像
	 * @return
	 */
	public static BufferedImage galcianFilter3(BufferedImage img){
		final float[] operator={
				0.0625f, 0.125f, 0.0625f,
				0.125f, 0.25f, 0.125f,
				0.0625f, 0.125f, 0.0625f};
		return acceptFilter(img,operator,3);
	}

	/**
	 * 5×5ガルシアンフィルタを適用
	 * @param img 画像
	 * @return
	 */
	public static BufferedImage galcianFilter5(BufferedImage img){
		final float[] operator={
				0.00390625f,0.015625f,0.0234375f,0.015625f,0.00390625f,
				0.015625f,0.0625f,0.09375f,0.0625f,0.015625f,
				0.0234375f,0.09375f,0.140625f,0.09375f,0.0234375f,
				0.015625f,0.0625f,0.09375f,0.0625f,0.015625f,
				0.00390625f,0.015625f,0.0234375f,0.015625f,0.00390625f};
		return acceptFilter(img,operator,5);
	}

	/**
	 * 画像フィルタを適用
	 * @param img	画像
	 * @param operator	オペレータ
	 * @param size ピクセルサイズ
	 * @return
	 */
	public static BufferedImage acceptFilter(BufferedImage img,float[] operator,int size){
		Kernel blur=new Kernel(size,size,operator);
		ConvolveOp convop=new ConvolveOp(blur,ConvolveOp.EDGE_NO_OP,null);
		BufferedImage bimg=convop.filter(img,null);
		return bimg;
	}

	/**
	 * 平均化フィルタを適用
	 * @param img 画像
	 * @param num ピクセルサイズ
	 * @return
	 */
	public static BufferedImage aveFilter(BufferedImage img,int num){
		float[] operator=new float[num*num];
		for(int i=0;i<operator.length;i++){
			operator[i]=1.0f/(float)operator.length;
		}
		Kernel blur=new Kernel(num,num,operator);
		ConvolveOp convop=new ConvolveOp(blur,ConvolveOp.EDGE_NO_OP,null);
		BufferedImage bimg=convop.filter(img,null);
		return bimg;
	}

	/**
	 * 画像フィルタのスケーリング
	 * @param img 画像
	 * @param sx xスケーリング倍率
	 * @param sy yスケーリング倍率
	 * @return
	 */
	public static BufferedImage scale(BufferedImage img,double sx,double sy,Color back){
	    BufferedImage after = new BufferedImage((int)(img.getWidth()*sx), (int)(img.getHeight()*sy), BufferedImage.TYPE_INT_RGB);
	    AffineTransform scaleInstance = AffineTransform.getScaleInstance(sx,sy);
	    Graphics2D g=after.createGraphics();
	    g.setBackground(back);
	    g.clearRect(0, 0, after.getWidth(), after.getHeight());
	    g.setTransform(scaleInstance);
	    g.drawImage(img, 0, 0, null);
	    g.dispose();
	    return after;
	}

	/**
	 * 画像の上下をフリップ
	 * @param im 画像
	 * @return
	 */
	public static BufferedImage flip(BufferedImage im){
		BufferedImage ret=new BufferedImage(im.getWidth(),im.getHeight(),BufferedImage.TYPE_INT_RGB);
		AffineTransform at=AffineTransform.getScaleInstance(1, -1);
		at.translate(0, -im.getHeight());
		Graphics2D g=ret.createGraphics();
		g.setTransform(at);
		g.drawImage(im,0,0,null);
		g.dispose();
		return ret;
	}
	
	public static BufferedImage colorReverse(BufferedImage im) {
		BufferedImage ret=new BufferedImage(im.getWidth(),im.getHeight(),BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<im.getWidth();i++) {
			for(int j=0;j<im.getHeight();j++) {
				Color c=new Color(im.getRGB(i, j));
				Color r=new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue());
				ret.setRGB(i, j, r.getRGB());
			}
		}
		return ret;
	}
	
	public static BufferedImage histogramEqualization(BufferedImage original) {
		int red;
		int green;
		int blue;
		int alpha;
		int newPixel = 0;
		ArrayList<int[]> histLUT = histogramEqualizationLUT(original);
		BufferedImage histogramEQ = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
		for(int i=0; i<original.getWidth(); i++) {
			for(int j=0; j<original.getHeight(); j++) {
				alpha = new Color(original.getRGB (i, j)).getAlpha();
				red = new Color(original.getRGB (i, j)).getRed();
				green = new Color(original.getRGB (i, j)).getGreen();
				blue = new Color(original.getRGB (i, j)).getBlue();
				red = histLUT.get(0)[red];
				green = histLUT.get(1)[green];
				blue = histLUT.get(2)[blue];
				newPixel = colorToRGB(alpha, red, green, blue);
				histogramEQ.setRGB(i, j, newPixel);
			}
		}
		return histogramEQ;
    }
	
	private static ArrayList<int[]> histogramEqualizationLUT(BufferedImage input) {
		ArrayList<int[]> imageHist = imageHistogram(input);
        ArrayList<int[]> imageLUT = new ArrayList<int[]>();
        int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];
        for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
        for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
        for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
        long sumr = 0;
        long sumg = 0;
        long sumb = 0;
        float scale_factor = (float) (255.0 / (input.getWidth() * input.getHeight()));
        for(int i=0; i<rhistogram.length; i++) {
        	sumr += imageHist.get(0)[i];
        	int valr = (int) (sumr * scale_factor);
        	if(valr > 255) {
        		rhistogram[i] = 255;
        	}
        	else rhistogram[i] = valr;

        	sumg += imageHist.get(1)[i];
        	int valg = (int) (sumg * scale_factor);
        	if(valg > 255) {
        		ghistogram[i] = 255;
        	}
        	else ghistogram[i] = valg;

        	sumb += imageHist.get(2)[i];
        	int valb = (int) (sumb * scale_factor);
        	if(valb > 255) {
        		bhistogram[i] = 255;
        	}
        	else bhistogram[i] = valb;
        }
        imageLUT.add(rhistogram);
        imageLUT.add(ghistogram);
        imageLUT.add(bhistogram);
        return imageLUT;
    }
	
	private static int colorToRGB(int alpha, int red, int green, int blue) {
		int newPixel = 0;
		newPixel += alpha; newPixel = newPixel << 8;
		newPixel += red; newPixel = newPixel << 8;
		newPixel += green; newPixel = newPixel << 8;
		newPixel += blue;
		return newPixel;
	}
	
	public static ArrayList<int[]> imageHistogram(BufferedImage input) {
		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];
		for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
		for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
		for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
		for(int i=0; i<input.getWidth(); i++) {
			for(int j=0; j<input.getHeight(); j++) {
				int red = new Color(input.getRGB (i, j)).getRed();
				int green = new Color(input.getRGB (i, j)).getGreen();
				int blue = new Color(input.getRGB (i, j)).getBlue();
				rhistogram[red]++; ghistogram[green]++; bhistogram[blue]++;
			}
		}
		ArrayList<int[]> hist = new ArrayList<int[]>();
		hist.add(rhistogram);
		hist.add(ghistogram);
		hist.add(bhistogram);
		return hist;
	}
	
	public static BufferedImage applyThreshold(BufferedImage gray,int th){
		int white=Color.WHITE.getRGB();
		int black=Color.BLACK.getRGB();
	    BufferedImage dimg = new BufferedImage(gray.getWidth(), gray.getHeight(), gray.getType());
		for(int i=0;i<dimg.getWidth();i++){
			for(int j=0;j<dimg.getHeight();j++){
				Color c1=new Color(gray.getRGB(i, j));
				if(c1.getRed()<th){
					dimg.setRGB(i, j, black);
				}else{
					dimg.setRGB(i, j, white);
				}
			}
		}
	    return dimg;
	}
	
	public static BufferedImage composite(BufferedImage[] im,float alpha){
		BufferedImage img=new BufferedImage(im[0].getWidth(),im[0].getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=img.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		g.drawImage(im[0], 0, 0, null);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		g.setComposite(ac);
		for(int i=1;i<im.length;i++){
			g.drawImage(im[i], 0, 0, null);
		}
		g.dispose();
		return img;
	}

	public static BufferedImage composite(BufferedImage[] im,float[] rate){
		BufferedImage img=new BufferedImage(im[0].getWidth(),im[0].getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=img.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		for(int i=0;i<im.length;i++){
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rate[i]);
			g.setComposite(ac);
			g.drawImage(im[i], 0, 0, null);
		}
		g.dispose();
		return img;
	}

}
