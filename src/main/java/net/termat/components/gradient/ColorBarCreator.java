package net.termat.components.gradient;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import net.termat.components.image.ImageUtil;

public class ColorBarCreator {
	private BufferedImage bar;
	private BufferedImage txt;
	private Range map;
	private NumberFormat nf;

	public ColorBarCreator(int width,int height,Range rm){
		bar=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		txt=new BufferedImage(width*2,height+30,BufferedImage.TYPE_INT_RGB);
		map=rm;
		nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
	}

	public void setFractionDigits(int i){
		nf.setMaximumFractionDigits(i);
		nf.setMinimumFractionDigits(i);
	}

	public void output(File f)throws IOException{
		BufferedImage bi=getColorBarImage();
		ImageIO.write(bi, "png", f);
	}

	public BufferedImage getColorBarImage(){
		BufferedImage ret=new BufferedImage(bar.getWidth()+txt.getWidth(),bar.getHeight()+20,BufferedImage.TYPE_INT_RGB);
		Graphics2D g=ret.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, ret.getWidth(), ret.getHeight());
		g.drawImage(bar,0,10,null);
		g.drawImage(txt,bar.getWidth(),0,txt.getWidth(),txt.getHeight(),null);
		g.dispose();
		return ret;
	}

	public void drawText(int step,boolean zero,int fontSize){
		Graphics2D g=txt.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, txt.getWidth(), txt.getHeight());
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,fontSize));
		Set<Double> tmp=new HashSet<Double>();
		if(zero)tmp.add(0.0);
		double val=map.getRange()/(double)step;
		for(double i=map.getMin();i<=map.getMax();i=i+val){
			tmp.add(i);
		}
		Double[] dd=tmp.toArray(new Double[tmp.size()]);
		Arrays.sort(dd);
		int pad=txt.getHeight()/dd.length;
		for(int i=dd.length-1;i>=0;i--){
			g.drawString(nf.format(dd[i]), 10, i*pad+fontSize);
		}
		g.dispose();
	}

	public void drawColorBar(Gradient grad){
		for(int i=0;i<bar.getHeight();i++){
			double val=(double)i/(double)bar.getHeight();
			val=map.getMin()+map.getRange()*val;
			int col=grad.getColorByInt(map.getValue(val));
			for(int j=0;j<bar.getWidth();j++){
				bar.setRGB(j, i, col);
			}
		}
		bar=ImageUtil.flip(bar);
	}

}
