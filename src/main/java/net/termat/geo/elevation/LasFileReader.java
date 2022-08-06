package net.termat.geo.elevation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;

import com.github.mreutegg.laszip4j.LASHeader;
import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;

import net.termat.geo.util.PCUtil;

public class LasFileReader {
	private Rectangle2D bounds;
	private AffineTransform af,iaf;
	private static final float basef=65536f;
	private int width,height;
	private double resolution;
	private File[] list=null;
	private BufferedImage img;

	public LasFileReader(File[] list,double mPerPixel)throws IOException{
		this.resolution=mPerPixel;
		this.list=list;
		init();
	}

	public LasFileReader(File path,double mPerPixel)throws IOException{
		this.resolution=mPerPixel;
		if(path.isDirectory()){
			List<File> ls=new ArrayList<>();
			for(File f : path.listFiles()){
				if(f.getName().toLowerCase().endsWith(".las"))ls.add(f);
			}
			list=ls.toArray(new File[ls.size()]);
		}else{
			list=new File[]{path};
		}
		init();
	}

	private void init(){
		bounds=null;
		for(File f : list){
			LASReader r=new LASReader(f);
			if(bounds==null){
				bounds=getBounds(r);
			}else{
				bounds=bounds.createUnion(getBounds(r));
			}
		}
		af=new AffineTransform(new double[]{resolution,0,0,-resolution,bounds.getX(),bounds.getY()+bounds.getHeight()});
		try {
			iaf=af.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		width=(int)Math.ceil(bounds.getWidth()/resolution);
		height=(int)Math.ceil(bounds.getHeight()/resolution);
		if(width%10<5) {
			width=width-width%5;
		}else {
			width=width+width%5;
		}
		if(height%10<5) {
			height=height-height%5;
		}else {
			height=height+height%5;
		}
	}
	
	public void create(){
		img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				img.setRGB(i, j, PCUtil.NA);
			}
		}
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writePng(r,img);
		}
	}

	public void create(byte cls){
		img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				img.setRGB(i, j, PCUtil.NA);
			}
		}
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writePng(r,img,cls);
		}
	}

	public void createLess(byte cls){
		img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				img.setRGB(i, j, PCUtil.NA);
			}
		}
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writePngLess(r,img,cls);
		}
	}

	public void createPhoto() throws IOException{
		img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writePhoto(r,img);
		}
	}
	
	public void createPhotoARGB() throws IOException{
		img=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=(Graphics2D )img.getGraphics();
		g.setBackground(new Color(0,0,0,0));
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writePhoto(r,img);
		}
	}

	public double[][] createNIR(){
		double[][] data=new double[height][width];
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writeNIR(r,data);
		}
		return data;
	}

	public double[][] createIntencity(){
		double[][] data=new double[height][width];
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writeIntencity(r,data);
		}
		return data;
	}

	public BufferedImage getImage(){
		return img;
	}

	public AffineTransform getTransform(){
		return af;
	}

	public void outputCsv(File path) throws IOException{
		BufferedWriter bw=new BufferedWriter(new FileWriter(path));
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writeList(r,bw,",");
		}
		bw.close();
	}

	public void outputXYZRGB(File path)throws IOException{
		BufferedWriter bw=new BufferedWriter(new FileWriter(path));
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writeXYZRGB(r,bw,",");
		}
		bw.close();
	}

	public void outputElevPng(File path,byte bi) throws IOException{
		create(bi);
		ImageIO.write(img, "png", path);
		File tf=new File(path.getAbsolutePath().replace(".png", ".pgw").replace(".PNG", ".pgw"));
		PCUtil.writeTransform(af, tf);
	}

	public void outputElevPng(File path) throws IOException{
		create();
		ImageIO.write(img, "png", path);
		File tf=new File(path.getAbsolutePath().replace(".png", ".pgw").replace(".PNG", ".pgw"));
		PCUtil.writeTransform(af, tf);
	}

	public void outputPhoto(File path) throws IOException{
		BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<list.length;i++){
			LASReader r = new LASReader(list[i]);
			writePhoto(r,img);
		}
		String ext=extension(path.getName());
		ImageIO.write(img,ext, path);
		File tf=null;
		if(ext.equals("png")){
			tf=new File(path.getAbsolutePath().replace(".png", ".pgw").replace(".PNG", ".pgw"));
		}else{
			tf=new File(path.getAbsolutePath().replace(".jpg", ".jgw").replace(".JPG", ".jgw"));
		}
		PCUtil.writeTransform(af, tf);
	}

	private String extension(String name){
		int id=name.lastIndexOf(".");
		return name.substring(id+1, name.length()).toLowerCase();
	}

	private void writeIntencity(LASReader las,double[][] data){
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	int z=(int)p.getIntensity();
	    	double zz=(double)z;
	    	if(z==32767||z==-32767)zz=Double.NaN;
	    	Point2D pt=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
	    	int xx=(int)Math.floor(pt.getX());
	    	int yy=(int)Math.floor(pt.getY());
	    	data[yy][xx]=zz;
	    }
	}

	private void writeNIR(LASReader las,double[][] data){
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	int z=(int)p.getNIR();
	    	double zz=(double)z;
	    	if(z==32767||z==-32767)zz=Double.NaN;
	    	Point2D pt=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
	    	int xx=(int)Math.floor(pt.getX());
	    	int yy=(int)Math.floor(pt.getY());
	    	data[yy][xx]=zz;
	    }
	}

	private void writePng(LASReader las,BufferedImage img){
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
		double zOffset=h.getZOffset();
		double zScalefactor=h.getZScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	double z=zScalefactor*(double)p.getZ()+zOffset;
	    	Point2D pt=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
	    	int xx=(int)Math.min(Math.max(0,Math.floor(pt.getX())),width-1);
	    	int yy=(int)Math.min(Math.max(0,Math.floor(pt.getY())),height-1);
	    	try{
		    	img.setRGB(xx, yy, PCUtil.getRGB(z));
	    	}catch(java.lang.ArrayIndexOutOfBoundsException ne){
	    		System.out.println(xx+","+yy+"  "+width+","+height);
	    	}
	    }
	}

	private void writePng(LASReader las,BufferedImage img,byte cls){
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
		double zOffset=h.getZOffset();
		double zScalefactor=h.getZScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	if(p.getClassification()!=cls)continue;
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;	
	    	double z=zScalefactor*(double)p.getZ()+zOffset;
	    	Point2D pt=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
	    	int xx=(int)Math.min(Math.max(0,Math.floor(pt.getX())),width-1);
	    	int yy=(int)Math.min(Math.max(0,Math.floor(pt.getY())),height-1);
	    	img.setRGB(xx, yy, PCUtil.getRGB(z));
	    }
	}

	private void writePngLess(LASReader las,BufferedImage img,byte cls){
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
		double zOffset=h.getZOffset();
		double zScalefactor=h.getZScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	if(p.getClassification()>cls)continue;
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	double z=zScalefactor*(double)p.getZ()+zOffset;
	    	Point2D pt=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
	    	int xx=(int)Math.min(Math.max(0,Math.floor(pt.getX())),width-1);
	    	int yy=(int)Math.min(Math.max(0,Math.floor(pt.getY())),height-1);
	    	img.setRGB(xx, yy, PCUtil.getRGB(z));
	    }
	}

	private void writePhoto(LASReader las,BufferedImage img){
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	Point2D pt=iaf.transform(new Point2D.Double(x,y), new Point2D.Double());
	    	float r=((float)p.getRed())/basef;
	    	float g=((float)p.getGreen())/basef;
	    	float b=((float)p.getBlue())/basef;
	    	int col=new Color(r,g,b).getRGB();
	    	int xx=(int)Math.min(Math.max(0,Math.floor(pt.getX())),width-1);
	    	int yy=(int)Math.min(Math.max(0,Math.floor(pt.getY())),height-1);
	    	img.setRGB(xx,yy, col);
	    }
	}

	private void writeList(LASReader las,BufferedWriter bw,String sep) throws IOException{
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
		double zOffset=h.getZOffset();
		double zScalefactor=h.getZScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	double z=zScalefactor*(double)p.getZ()+zOffset;
	    	float r=((float)p.getRed())/basef;
	    	float g=((float)p.getGreen())/basef;
	    	float b=((float)p.getBlue())/basef;
	    	Color col=new Color(r,g,b);
	    	byte cls=p.getClassification();
	    	char intnt=p.getIntensity();
	    	char nir=p.getNIR();
	    	byte ret=p.getReturnNumber();
	    	bw.write(x+sep+y+sep+z+sep);
	    	bw.write(col.getRed()+sep+col.getGreen()+sep+col.getBlue()+sep);
	    	bw.write(nir+sep+intnt+sep+cls+sep+ret+"\n");
	    }
	}

	private void writeXYZRGB(LASReader las,BufferedWriter bw,String sep) throws IOException{
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
		double zOffset=h.getZOffset();
		double zScalefactor=h.getZScaleFactor();
	    for (LASPoint p : las.getPoints()) {
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	double z=zScalefactor*(double)p.getZ()+zOffset;
	    	float r=((float)p.getRed())/basef;
	    	float g=((float)p.getGreen())/basef;
	    	float b=((float)p.getBlue())/basef;
	    	Color col=new Color(r,g,b);
	    	bw.write(x+sep+y+sep+z+sep);
	    	bw.write(col.getRed()+sep+col.getGreen()+sep+col.getBlue()+"\n");
	    }
	}

	private Rectangle2D getBounds(LASReader las){
		LASHeader h=las.getHeader();
		return new Rectangle2D.Double(h.getMinX(),h.getMinY(),h.getMaxX()-h.getMinX(),h.getMaxY()-h.getMinY());
	}

	@SuppressWarnings("deprecation")
	public static DefaultTableModel getDataModel(File f,int row){
		DefaultTableModel ret=new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		ret.setColumnIdentifiers(new String[]{"cls","x","y","z","r","g","b","nir","Intencity","ret_no","ret_num"});
		ret.setColumnCount(11);
		ret.setRowCount(row);
		LASReader las=new LASReader(f);
		LASHeader h=las.getHeader();
		double xOffset=h.getXOffset();
		double xScalefactor=h.getXScaleFactor();
		double yOffset=h.getYOffset();
		double yScalefactor=h.getYScaleFactor();
		double zOffset=h.getZOffset();
		double zScalefactor=h.getZScaleFactor();
		int it=0;
		for(LASPoint p : las.getPoints()){
	    	double x=xScalefactor*(double)p.getX()+xOffset;
	    	double y=yScalefactor*(double)p.getY()+yOffset;
	    	double z=zScalefactor*(double)p.getZ()+zOffset;
	    	float r=((float)p.getRed())/basef;
	    	float g=((float)p.getGreen())/basef;
	    	float b=((float)p.getBlue())/basef;
	    	int intencity=(int)p.getIntensity();
	    	int nir=(int)p.getNIR();	    	
			ret.setValueAt(p.getClassification(), it, 0);
			ret.setValueAt(x, it, 1);
			ret.setValueAt(y, it, 2);
			ret.setValueAt(z, it, 3);
			ret.setValueAt(r, it, 4);
			ret.setValueAt(g, it, 5);
			ret.setValueAt(b, it, 6);
			ret.setValueAt(nir, it, 7);
			ret.setValueAt(intencity, it, 8);
			ret.setValueAt(p.getReturnNumber(), it, 9);
			ret.setValueAt(new Byte(p.getNumberOfReturns()),it,10);
			it++;
			if(it>=row)break;
		}
		return ret;
	}

	public String showHeader(File f){
		LASReader las=new LASReader(f);
		StringBuffer buf=new StringBuffer();
		LASHeader h=las.getHeader();
		buf.append("signature,"+h.getFileSignature()+"\n");
		buf.append("version,"+h.getVersionMajor()+"."+h.getVersionMinor()+"\n");
		buf.append("headerSize,"+(int)h.getHeaderSize()+"\n");
		buf.append("numOfPointRecords,"+h.getNumberOfPointRecords()+"\n");
		buf.append("minX,"+h.getMinX()+"\n");
		buf.append("maxX,"+h.getMaxX()+"\n");
		buf.append("minY,"+h.getMinY()+"\n");
		buf.append("maxY,"+h.getMaxY()+"\n");
		buf.append("minZ,"+h.getMinZ()+"\n");
		buf.append("maxZ,"+h.getMaxZ()+"\n");
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.YEAR, (int)h.getFileCreationYear());
		cal.set(Calendar.DAY_OF_YEAR, (int)h.getFileCreationDayOfYear());
		buf.append(DateFormat.getDateInstance(DateFormat.LONG).format(cal.getTime())+"\n");
		buf.append("offsetX,"+h.getXOffset()+"\n");
		buf.append("scaleX,"+h.getXScaleFactor()+"\n");
		buf.append("offsetY,"+h.getYOffset()+"\n");
		buf.append("scaleY,"+h.getYScaleFactor()+"\n");
		buf.append("offsetZ,"+h.getZOffset()+"\n");
		buf.append("scaleZ,"+h.getZScaleFactor()+"\n");
		return buf.toString();
	}

	public static Rectangle2D getBounds(File f) {
		LASReader las=new LASReader(f);
		LASHeader h=las.getHeader();
		Rectangle2D bounds=new Rectangle2D.Double(h.getMinX(),h.getMinY(),h.getMaxX()-h.getMinX(),h.getMaxY()-h.getMinY());
		return bounds;
	}

	public static void main(String[] args){
		File f=new File("\\\\149-128\\e\\CityGML\\浜松城");
		try {
//			LasFileReader app=new LasFileReader(f,0.05);
//			app.outputCsv(new File("test.csv"));
//			app.outputElevPng(new File("test.png"));
//			app.outputElevPng(new File("test2.png"),(byte)2);
//			app.outputPhoto(new File("test.jpg"));
//			app.outputNirNpy(new File("nir.npy"));
//			app.outputIntensityNpy(new File("intnsity.npy"));
			LasFileReader app=new LasFileReader(f,0.05);
			app.outputXYZRGB(new File("\\\\149-128\\e\\CityGML\\浜松城\\point_cloud.csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
