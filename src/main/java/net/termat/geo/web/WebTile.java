package net.termat.geo.web;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import net.termat.geo.util.PCUtil;


public class WebTile {
	private static final double L=85.05112877980659;
	private String url;
	private Map<Point2D,BufferedImage> tiles;
	private BufferedImage img;
	private AffineTransform af;
	private int zoom;
	private double resolution;
	private Color backGround;
	
	public WebTile(String url,int zoom,double res){
		this.url=url;
		this.zoom=zoom;
		this.resolution=res;
		tiles=new HashMap<>();
		backGround=null;
	}
	
	public void setBackBround(Color c) {
		backGround=c;
	}
	
	private void fillBackGround(BufferedImage img) {
		Graphics2D g=img.createGraphics();
		g.setBackground(backGround);
		g.clearRect(0, 0,img.getWidth(), img.getHeight());
		g.dispose();
	}
	
	public void create(int coordSys,Rectangle2D xy)throws IOException{
		int w=(int)Math.ceil(xy.getWidth()/resolution);
		int h=(int)Math.ceil(xy.getHeight()/resolution);
		if(w%2==1)w++;
		if(h%2==1)h++;
		double[] param=new double[]{
				resolution,0,0,-resolution,xy.getX(),xy.getY()+xy.getHeight()};
		af=new AffineTransform(param);
		img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		if(backGround!=null)fillBackGround(img);
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				Point2D pxy=af.transform(new Point2D.Double(i,j),new Point2D.Double());
				Point2D lonlat=PCUtil.getLonlat(coordSys, pxy.getX(), pxy.getY());
				Point2D pixel=lonlatToPixel(zoom,lonlat);
				Point2D tile=new Point2D.Double(Math.floor(pixel.getX()/256),Math.floor(pixel.getY()/256));
				if(tiles.containsKey(tile)){
					BufferedImage tmp=tiles.get(tile);
					if(tmp!=null){
						int xx=(int)pixel.getX()%256;
						int yy=(int)pixel.getY()%256;
						img.setRGB(i, j, tmp.getRGB(xx, yy));
					}
				}else{
					BufferedImage tmp=getTile(zoom,(long)tile.getX(),(long)tile.getY());
					if(tmp!=null){
						int xx=(int)pixel.getX()%256;
						int yy=(int)pixel.getY()%256;
						img.setRGB(i, j, tmp.getRGB(xx, yy));
					}
					tiles.put(tile, tmp);
				}
			}
		}
	}
	
	public void create(int coordSys,Rectangle2D xy,int divisor)throws IOException{
		int w=(int)Math.ceil(xy.getWidth()/resolution);
		int h=(int)Math.ceil(xy.getHeight()/resolution);
		int ww=w;
		int hh=h;
		while(ww%divisor!=0)ww++;
		while(hh%divisor!=0)hh++;
		int dx=ww-w;
		int dy=hh-h;
		double[] param=new double[]{
				resolution,0,0,-resolution,xy.getX()-0.5*dx*resolution,xy.getY()+hh*resolution-0.5*dy*resolution};
		af=new AffineTransform(param);
		img=new BufferedImage(ww,hh,BufferedImage.TYPE_INT_RGB);
		if(backGround!=null)fillBackGround(img);
		for(int i=0;i<ww;i++){
			for(int j=0;j<hh;j++){
				Point2D pxy=af.transform(new Point2D.Double(i,j),new Point2D.Double());
				Point2D lonlat=PCUtil.getLonlat(coordSys, pxy.getX(), pxy.getY());
				Point2D pixel=lonlatToPixel(zoom,lonlat);
				Point2D tile=new Point2D.Double(Math.floor(pixel.getX()/256),Math.floor(pixel.getY()/256));
				if(tiles.containsKey(tile)){
					BufferedImage tmp=tiles.get(tile);
					if(tmp!=null){
						int xx=(int)pixel.getX()%256;
						int yy=(int)pixel.getY()%256;
						img.setRGB(i, j, tmp.getRGB(xx, yy));
					}
				}else{
					BufferedImage tmp=getTile(zoom,(long)tile.getX(),(long)tile.getY());
					if(tmp!=null){
						int xx=(int)pixel.getX()%256;
						int yy=(int)pixel.getY()%256;
						img.setRGB(i, j, tmp.getRGB(xx, yy));
					}
					tiles.put(tile, tmp);
				}
			}
		}
	}

	protected BufferedImage getTile(int zoom,long x,long y) {
		String uu=new String(url).replace("{z}", Integer.toString(zoom));
		uu=uu.replace("{x}", Long.toString(x));
		uu=uu.replace("{y}", Long.toString(y));
		try {
			HttpsURLConnection con=(HttpsURLConnection)new URL(uu).openConnection();
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null,
					new X509TrustManager[] { new LooseTrustManager() },
					new SecureRandom());

			con.setSSLSocketFactory(sslContext.getSocketFactory());
			con.setHostnameVerifier(new LooseHostnameVerifier());
	        BufferedImage tmp=ImageIO.read(con.getInputStream());
			if(tmp!=null)return tmp;
		}catch(IOException | KeyManagementException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public BufferedImage getImage() {
		return img;
	}

	public AffineTransform getTransform() {
		return af;
	}

	private static Point2D lonlatToPixel(int zoom,Point2D p){
		long x=(long)(Math.pow(2, zoom+7)*(p.getX()/180.0+1.0));
		long y=(long)((Math.pow(2, zoom+7)/Math.PI)*(-atanh(Math.sin(Math.toRadians(p.getY())))+atanh(Math.sin(Math.toRadians(L)))));
		return new Point2D.Double(x,y);
	}

	private static double atanh(double v){
		return 0.5*Math.log((1.0+v)/(1.0-v));
	}
	
	public static void main(String[] args){
		double x1=-106000;
		double y1=-69500;
		double x2=12000;
		double y2=-38500;
		double px=Math.min(x1, x2);
		double pw=Math.max(x1, x2)-px;
		double py=Math.min(y1, y2);
		double ph=Math.max(y1, y2)-py;
		Rectangle2D rect=new Rectangle2D.Double(px,py,pw,ph);
		try{
			String url="https://cyberjapandata.gsi.go.jp/xyz/dem_png/{z}/{x}/{y}.png";
			WebTile app=new WebTile(url,14,10);
			app.create(5, rect);
			ImageIO.write(app.getImage(), "png", new File("F:\\衛星\\鳥取\\data\\JGD_2020_DEM.png"));
			AffineTransform af=new AffineTransform(
				10,0,0,-10,rect.getX(),rect.getY()+rect.getHeight());
			PCUtil.writeTransform(af, new File("F:\\衛星\\鳥取\\data\\JGD_2020_DEM.pgw"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
