package net.termat.geo.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class WebDemTile extends WebTile{
	private String[] url=new String[] {
		"https://cyberjapandata.gsi.go.jp/xyz/dem5a_png/{z}/{x}/{y}.png",
		"https://cyberjapandata.gsi.go.jp/xyz/dem5b_png/{z}/{x}/{y}.png",
		"https://cyberjapandata.gsi.go.jp/xyz/dem5c_png/{z}/{x}/{y}.png",
		"https://cyberjapandata.gsi.go.jp/xyz/dem_png/{z}/{x}/{y}.png"
	};
	
	public WebDemTile(int zoom,double res) {
		super(null,zoom,res);
	}

	protected BufferedImage getTile(int zoom,long x,long y) {
		if(zoom<=14) {
			String uu=new String(url[3]).replace("{z}", Integer.toString(zoom));
			uu=uu.replace("{x}", Long.toString(x));
			uu=uu.replace("{y}", Long.toString(y));
			try {
				BufferedImage tmp=ImageIO.read(new URL(uu));
				if(tmp!=null)return tmp;
			}catch(IOException e) {
				e.printStackTrace();
			}
			return null;
		}else {
			for(int i=0;i<url.length-1;i++) {
				String uu=new String(url[i]).replace("{z}", Integer.toString(zoom));
				uu=uu.replace("{x}", Long.toString(x));
				uu=uu.replace("{y}", Long.toString(y));
				try {
					BufferedImage tmp=ImageIO.read(new URL(uu));
					if(tmp!=null)return tmp;
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
			String uu=new String(url[3]).replace("{z}", Integer.toString(14));
			uu=uu.replace("{x}", Long.toString((long)Math.floor(x/2.0)));
			uu=uu.replace("{y}", Long.toString((long)Math.floor(y/2.0)));
			try {
				BufferedImage tmp=ImageIO.read(new URL(uu));
				return getZoomUp(tmp,(long)Math.floor(x/2.0),(long)Math.floor(y/2.0),x,y);
			}catch(IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private BufferedImage getZoomUp(BufferedImage tile,long x,long y,long sx,long sy) {
		BufferedImage ret=new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
		double xx=(double)sx/(double)(x*2);
		double yy=(double)sy/(double)(y*2);
		int mx=0,my=0;
		if(xx>0.5)mx=128;
		if(yy>0.5)my=128;
		int px=0,py=0;
		for(int i=mx;i<mx+128;i++) {
			for(int j=my;j<my+128;j++) {
				int col=tile.getRGB(i, j);
				ret.setRGB(px, py, col);
				ret.setRGB(px+1, py, col);
				ret.setRGB(px, py+1, col);
				ret.setRGB(px+1, py+1, col);
				py=py+2;
			}
			px=px+2;
			py=0;
		}
		return ret;
	}
}
