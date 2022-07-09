package net.termat.geo.elevation;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import net.termat.geo.util.PCUtil;

public class ElevPngCreator{
	private File path;
	private String sep;
	private boolean header;
	private int xcol;
	private int ycol;
	private int zcol;
	private double[] bounds;
	private AffineTransform af;
	private BufferedImage img;

	public ElevPngCreator(File path,String sep,boolean header,int xcol,int ycol,int zcol) throws IOException{
		super();
		this.path=path;
		this.sep=sep;
		this.header=header;
		this.xcol=xcol;
		this.ycol=ycol;
		this.zcol=zcol;
		checkBounds();
	}

	private BufferedReader createReader() throws IOException{
		String name=path.getName().toLowerCase();
		BufferedReader br=null;
		if(name.endsWith(".gz")){
			InputStream is = Files.newInputStream(path.toPath());
			GZIPInputStream gis = new GZIPInputStream(is);
			InputStreamReader isReader = new InputStreamReader(gis, StandardCharsets.UTF_8);
			br = new BufferedReader(isReader);
		}else{
			br=new BufferedReader(new FileReader(path));
		}
		return br;
	}

	public void create(double resolution) throws IOException{
		int w=(int)Math.abs(Math.ceil(bounds[2]/resolution));
		if(w%2==1)w++;
		int h=(int)Math.abs(Math.ceil(bounds[3]/resolution));
		if(h%2==1)h++;
		if(w*h>10000*10000)throw new IOException("Image large");
		img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		PCUtil.setImageNA(img);
		double[] param=new double[]{
				resolution,0,0,-resolution,bounds[0],bounds[1]+bounds[3]};
		af=new AffineTransform(param);
		AffineTransform iaf=null;
		try {
			iaf=af.createInverse();
		} catch (NoninvertibleTransformException e) {}
		BufferedReader br=createReader();
		String line=null;
		if(header)br.readLine();
		while((line=br.readLine())!=null){
			String[] str=line.split(sep);
			double x=Double.parseDouble(str[xcol]);
			double y=Double.parseDouble(str[ycol]);
			double z=Double.parseDouble(str[zcol]);
			Point2D p=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
			int px=(int)Math.floor(p.getX());
			int py=(int)Math.floor(p.getY());
			int rgb=PCUtil.getRGB(z);
			try{
				img.setRGB(px, py, rgb);
			}catch(java.lang.ArrayIndexOutOfBoundsException ne){
				System.out.println(px+" , "+py+"   "+w+","+h+"  / "+z);
				System.exit(0);
			}
		}
		br.close();
	}

	public void output() throws IOException{
		String name=path.getAbsolutePath();
		int ii=name.lastIndexOf(".");
		name=name.substring(0, ii-1);
		ImageIO.write(img, "png", new File(name+".png"));
		File tfw=new File(name+".pgw");
		PCUtil.writeTransform(af, tfw);
	}

	public BufferedImage getImage(){
		return img;
	}

	public AffineTransform getTransform(){
		return af;
	}

	private void checkBounds() throws IOException{
		double xmin=Double.MAX_VALUE;
		double xmax=-Double.MAX_VALUE;
		double ymin=Double.MAX_VALUE;
		double ymax=-Double.MAX_VALUE;
		BufferedReader br=createReader();
		String line=null;
		if(header)br.readLine();
		while((line=br.readLine())!=null){
			String[] str=line.split(sep);
			double x=Double.parseDouble(str[xcol]);
			double y=Double.parseDouble(str[ycol]);
			if(xmin > x)xmin=x;
			if(xmax < x)xmax=x;
			if(ymin > y)ymin=y;
			if(ymax < y)ymax=y;
		}
		br.close();
		bounds= new double[]{xmin,ymin,xmax-xmin,ymax-ymin};
	}

	public static void main(String[] args){
		File f=new File("D:/Downloads/data/30XXX00010013.csv");
		try {
			ElevPngCreator app=new ElevPngCreator(f,",",true,0,1,2);
			app.create(0.25);
			app.output();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
