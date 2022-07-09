package net.termat.geo.util;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.jetbrains.bio.npy.NpyArray;
import org.jetbrains.bio.npy.NpyFile;

import net.termat.components.gradient.Gradient;
import net.termat.components.gradient.Range;

public class PCUtil {

	public static int NA=new Color(128,0,0).getRGB();

	/**
	 * RGB（int)を標高に変換するメソッド
	 *
	 * @param  color RGB:int
	 * @return 標高:double
	 */
	public static double getZ(int color){
		color=(color << 8) >> 8;
		if(color==8388608||color==-8388608){
			return Double.NaN;
		}else if(color<8388608){
			return color * 0.01;
		}else{
			return (color-16777216)*0.01;
		}
	}

	/**
	 * 標高をRGB（INT）に変換するメソッド
	 *
	 * @param  z 標高 :double
	 * @return RGB:int
	 */
	public static int getRGB(double z){
		if(Double.isNaN(z)){
			return NA;
		}else if(z<-83886||z>83886) {
			return NA;
		}else{
			int i=(int)Math.round(z*100);
			if(z<0)	i=i+0x1000000;
			int r=Math.max(0,Math.min(i >> 16,255));
			int g=Math.max(0,Math.min(i-(r << 16) >> 8,255));
			int b=Math.max(0,Math.min(i-((r << 16)+(g << 8)),255));
			return new Color(r,g,b).getRGB();
		}
	}
	
	/**
	 * 平面直角座標系のXY座標を経度緯度に変換するメソッド
	 *
	 * @param coordSys 平面直角座標系の番号:int
	 * @param x X座標:double
	 * @param y Y座標:double
	 * @return 経度緯度：Point2D
	 */
	public static Point2D getLonlat(int coordSys,double x,double y){
		return LonLatXY.xyToLonlat(coordSys, x, y);
	}

	public static void setImageNA(BufferedImage img) {
		for(int i=0;i<img.getWidth();i++) {
			for(int j=0;j<img.getHeight();j++) {
				img.setRGB(i, j, NA);
			}
		}
	}
	
	/**
	 * 経度緯度を平面直角座標系のXY座標に変換するメソッド
	 *
	 * @param coordSys 平面直角座標系の番号:int
	 * @param lon 経度:double
	 * @param lat 緯度;double
	 * @return XY座標:Point2D
	 */
	public static Point2D getXY(int coordSys,double lon,double lat){
		return LonLatXY.lonlatToXY(coordSys, lon, lat);
	}

	public static double[] getMinMax(BufferedImage img){
		double[][] dd=PCUtil.getTableNpy(img);
		double min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		for(int i=0;i<dd.length;i++){
			for(int j=0;j<dd[i].length;j++){
				if(Double.isNaN(dd[i][j]))continue;
				min=Math.min(min, dd[i][j]);
				max=Math.max(max, dd[i][j]);
			}
		}
		return new double[]{min,max};
	}

	public static double[] getMinMax(double[][] dd){
		double min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		for(int i=0;i<dd.length;i++){
			for(int j=0;j<dd[i].length;j++){
				if(Double.isNaN(dd[i][j]))continue;
				min=Math.min(min, dd[i][j]);
				max=Math.max(max, dd[i][j]);
			}
		}
		return new double[]{min,max};
	}

	/**
	 * ワールドファイルの読み込み
	 *
	 * @param path ワールドファイルのパス
	 * @return AffineTransform
	 * @throws IOException
	 */
	public static AffineTransform loadTransform(File path)throws IOException{
		BufferedReader br=new BufferedReader(new FileReader(path));
		List<Double> dd=new ArrayList<Double>();
		String line=null;
		while((line=br.readLine())!=null){
			double d=Double.parseDouble(line);
			dd.add(d);
		}
		br.close();
		double[] p=new double[dd.size()];
		for(int i=0;i<p.length;i++){
			p[i]=dd.get(i);
		}
		return new AffineTransform(p);
	}

	/**
	 * ワールドファイルの出力
	 *
	 * @param af AffineTransform
	 * @param path ワールドファイルのパス
	 * @throws IOException
	 */
	public static void writeTransform(AffineTransform af,File path)throws IOException{
		BufferedWriter bw=new BufferedWriter(new FileWriter((path)));
		bw.write(af.getScaleX()+"\n");
		bw.write(af.getShearX()+"\n");
		bw.write(af.getShearY()+"\n");
		bw.write(af.getScaleY()+"\n");
		bw.write(af.getTranslateX()+"\n");
		bw.write(af.getTranslateY()+"\n");
		bw.flush();
		bw.close();
	}

	/**
	 * 二次元配列をNPY形式で出力
	 *
	 * @param val 出力する二次元配列:double[][]
	 * @param path NPYファイルのパス
	 */
	public static void writeNpy(double[][] val,File path){
		double[] data=new double[val.length*val[0].length];
		int k=0;
		for(int i=0;i<val.length;i++){
			for(int j=0;j<val[i].length;j++){
				data[k]=val[i][j];
				k++;
			}
		}
		int[] shape=new int[]{val[0].length,val.length};
		NpyFile.write(path.toPath(), data, shape);
	}

	public static double[][] readNpy(File f){
		Path path=f.toPath();
		NpyArray array=NpyFile.read(path,65536);
		int[] s=array.getShape();
		double[][] ret=new double[s[1]][s[0]];
		double[] tmp=array.asDoubleArray();
		int it=0;
		for(int i=0;i<ret.length;i++){
			for(int j=0;j<ret[i].length;j++){
				ret[i][j]=tmp[it++];
			}
		}
		return ret;
	}

	public static double[][] getTableNpy(BufferedImage img){
		int w=img.getWidth();
		int h=img.getHeight();
		double[][] ret=new double[h][w];
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				double z=getZ(img.getRGB(i, j));
				ret[j][i]=z;
			}
		}
		return ret;
	}
	
	public static double[][] getTable(BufferedImage img){
		int w=img.getWidth();
		int h=img.getHeight();
		double[][] ret=new double[w][h];
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				double z=getZ(img.getRGB(i, j));
				ret[i][j]=z;
			}
		}
		return ret;
	}

	public static Map<String,Double> getData(BufferedImage bi,AffineTransform af){
		int w=bi.getWidth();
		int h=bi.getHeight();
		double xmin=Double.MAX_VALUE;
		double xmax=-Double.MAX_VALUE;
		double ymin=Double.MAX_VALUE;
		double ymax=-Double.MAX_VALUE;
		double zmin=Double.MAX_VALUE;
		double zmax=-Double.MAX_VALUE;
		Point2D tmp=new Point2D.Double();
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				Point2D p=af.transform(new Point2D.Double(i,j), tmp);
				xmin=Math.min(xmin, p.getX());
				xmax=Math.max(xmax, p.getX());
				ymin=Math.min(ymin, p.getY());
				ymax=Math.max(ymax, p.getY());
				int col=bi.getRGB(i, j);
				if(col==PCUtil.NA)continue;
				double z=PCUtil.getZ(col);
				zmin=Math.min(zmin, z);
				zmax=Math.max(zmax, z);
			}
		}
		Map<String,Double> ret=new HashMap<>();
		ret.put("xmin", xmin);
		ret.put("xmax", xmax);
		ret.put("ymin", ymin);
		ret.put("ymax", ymax);
		ret.put("zmin", zmin);
		ret.put("zmax", zmax);
		return ret;
	}

	public static BufferedImage createElevationPng(double[][] npy){
		BufferedImage ret=new BufferedImage(npy[0].length,npy.length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<npy.length;i++){
			for(int j=0;j<npy[i].length;j++){
				if(Double.isNaN(npy[i][j])){
					ret.setRGB(j, i,NA);
				}else{
					ret.setRGB(j, i, getRGB(npy[i][j]));
				}
			}
		}
		return ret;
	}

	public static BufferedImage createImage(BufferedImage img,Gradient grad,Range range){
		BufferedImage ret=new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				int col=img.getRGB(i, j);
				if(col==NA)continue;
				double z=getZ(col);
				col=grad.getColorByInt(range.getValue(z));
				ret.setRGB(i, j, col);
			}
		}
		return ret;
	}

	public static BufferedImage createImage(double[][] npy,Gradient grad,Range range){
		BufferedImage ret=new BufferedImage(npy[0].length,npy.length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<npy.length;i++){
			for(int j=0;j<npy[i].length;j++){
				double z=npy[i][j];
				int col=grad.getColorByInt(range.getValue(z));
				ret.setRGB(j, i, col);
			}
		}
		return ret;
	}

	public static byte[] doubleToByteArray(double[][] dd){
		ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
		for(int i=0;i<dd.length;i++){
			for(int j=0;j<dd[i].length;j++){
				byteBuffer.putDouble(dd[i][j]);
			}
		}
		 return byteBuffer.array();
	}

	private static byte [] doubleToByteArray(double number) {
		 ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
		 byteBuffer.putDouble(number);
		 return byteBuffer.array();
		}

	public static byte[] doubleArrayToByteArray(double[][] data) {
		byte[] byts = new byte[data.length*data[0].length * Double.BYTES];
		int it=0;
		for(int i=0;i<data.length;i++){
			for(int j=0;j<data[0].length;j++){
				System.arraycopy(doubleToByteArray(data[i][j]), 0, byts, it * Double.BYTES, Double.BYTES);
				it++;
			}
		}
		return byts;
	}

	private static double convertByteArrayToDouble(byte[] doubleBytes){
		 ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
		 byteBuffer.put(doubleBytes);
		 byteBuffer.flip();
		 return byteBuffer.getDouble();
		}

	public static double[][] byteArrayToDoubleArray(byte[] data,int m,int n) {
		double[][] ret=new double[m][n];
		int it=0;
		for(int i=0;i<ret.length;i++){
			for(int j=0;j<ret[0].length;j++){
				ret[i][j]=convertByteArrayToDouble(new byte[]{
					data[(it*Double.BYTES)],
					 data[(it*Double.BYTES)+1],
					 data[(it*Double.BYTES)+2],
					 data[(it*Double.BYTES)+3],
					 data[(it*Double.BYTES)+4],
					 data[(it*Double.BYTES)+5],
					 data[(it*Double.BYTES)+6],
					 data[(it*Double.BYTES)+7],
				});
				it++;
			}
		}
		return ret;
	}

	public static boolean hasHeader(File f,String sep) throws IOException{
		BufferedReader br=createReader(f);
		String line=br.readLine();
		br.close();
		String[] str=line.split("sep");
		for(String s : str){
			try{
				Double.parseDouble(s);
			}catch(NumberFormatException e){
				return true;
			}
		}
		return false;
	}

	private static BufferedReader createReader(File path) throws IOException{
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

	public static Shape createShape(int coordSys,Shape sp) {
		GeneralPath ret=new GeneralPath();
		PathIterator pi=sp.getPathIterator(AffineTransform.getScaleInstance(1.0, 1.0));
		  double[] c = new double[6];
		  while (!pi.isDone()) {
		    switch (pi.currentSegment(c)) {
		      case PathIterator.SEG_MOVETO:
		    	  Point2D p=PCUtil.getLonlat(coordSys, c[0], c[1]);
		    	  ret.moveTo(p.getX(), p.getY());
		    	  break;
		      case PathIterator.SEG_LINETO:
		    	  p=PCUtil.getLonlat(coordSys, c[0], c[1]);
		    	  ret.lineTo(p.getX(), p.getY());
		    	  break;
		      case PathIterator.SEG_CLOSE:
		    	  ret.closePath();
		        break;
		    }
		    pi.next();
		  }
		  return ret;
	}
	
	public static AffineTransform resamplingTransform(AffineTransform af,double resolution) {
		double[] param=new double[] {resolution,0,0,-resolution,af.getTranslateX(),af.getTranslateY()};
		AffineTransform at=new AffineTransform(param);
		return at;
	}
	
	public static BufferedImage resampling(BufferedImage img,AffineTransform af,double resolution){
		int width=img.getWidth();
		int height=img.getHeight();
		AffineTransform iaf=null;
		try {
			iaf = af.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		int ww=(int)Math.abs(Math.floor(img.getWidth()*af.getScaleX()/resolution));
		int hh=(int)Math.abs(Math.floor(img.getHeight()*af.getScaleY()/resolution));
		BufferedImage ret=new BufferedImage(ww,hh,BufferedImage.TYPE_INT_RGB);
		double[] param=new double[] {resolution,0,0,-resolution,af.getTranslateX(),af.getTranslateY()};
		AffineTransform at=new AffineTransform(param);
		for(int i=0;i<ww;i++) {
			for(int j=0;j<hh;j++) {
				Point2D p=at.transform(new Point2D.Double(i,j), new Point2D.Double());
				p=iaf.transform(p, new Point2D.Double());
				int xx=(int)Math.floor(p.getX());
				int yy=(int)Math.floor(p.getY());
				if(xx>=0&&xx<width&&yy>=0&&yy<height) {
					ret.setRGB(i, j, img.getRGB(xx, yy));
				}
			}
		}
		return ret;
	}
	
	public static Rectangle2D getLonlatRect(int coordSys,Rectangle2D r) {
		Point2D p1=PCUtil.getLonlat(coordSys, r.getX(), r.getY());
		Point2D p2=PCUtil.getLonlat(coordSys, r.getX()+r.getWidth(), r.getY()+r.getHeight());
		double miny=Math.min(p1.getY(), p2.getY());
		double maxy=Math.min(p1.getY(), p2.getY());
		return new Rectangle2D.Double(p1.getX(),miny,p2.getX()-p1.getX(),maxy-miny);
	}
	
	public static int getCoordSys() {
		String[] sp=new String[18];
		for(int i=0;i<sp.length;i++) {
			if(i==0) {
				sp[i]="WGS84";
			}else if(i<=9) {
				sp[i]="平面直角第0"+Integer.toString(i)+"系";
			}else {
				sp[i]="平面直角第"+Integer.toString(i)+"系";
			}
		}
		JComboBox<String> comm=new JComboBox<>(sp);
		comm.setSelectedIndex(6);
		int ck=JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), comm, "Info", JOptionPane.YES_NO_OPTION);
		if(ck==JOptionPane.YES_OPTION) {
			return comm.getSelectedIndex();
		}else {
			return 0;
		}
	}
	/*
	public static Map<String,Number> getCoordSysAndResolution() {
		String[] sp=new String[18];
		for(int i=0;i<sp.length;i++) {
			if(i==0) {
				sp[i]="WGS84";
			}else if(i<=9) {
				sp[i]="平面直角第0"+Integer.toString(i)+"系";
			}else {
				sp[i]="平面直角第"+Integer.toString(i)+"系";
			}
		}
		JComboBox<String> comm=new JComboBox<>(sp);
		JPanel panel=new JPanel(new GridLayout(2,1));
		panel.add(comm);
		comm.setSelectedIndex(6);
		JSpinner sp1=new JSpinner();
		SpinnerNumberModel snm=new SpinnerNumberModel(0.25d,0.05d,1.0d,0.05d);
		sp1.setModel(snm);
		JPanel p2=new JPanel(new BorderLayout());
		p2.add(new JLabel("解像度"),BorderLayout.WEST);
		p2.add(sp1,BorderLayout.CENTER);
		panel.add(p2);
		int ck=JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel, "Info", JOptionPane.YES_NO_OPTION);
		if(ck==JOptionPane.YES_OPTION) {
			Map<String,Number> ret=new HashMap<>();
			ret.put("coordSys", comm.getSelectedIndex());
			ret.put("resolution", ((Number)sp1.getValue()));
			return ret;
		}else {
			return null;
		}
	}
	*/
	
	/*
	public static Map<String,Object> getCoordSysAndResAndXYZ() {
		String[] sp=new String[18];
		for(int i=0;i<sp.length;i++) {
			if(i==0) {
				sp[i]="WGS84";
			}else if(i<=9) {
				sp[i]="平面直角第0"+Integer.toString(i)+"系";
			}else {
				sp[i]="平面直角第"+Integer.toString(i)+"系";
			}
		}
		JComboBox<String> comm=new JComboBox<>(sp);
		JPanel panel=new JPanel(new GridLayout(6,1));
		panel.add(comm);
		comm.setSelectedIndex(6);
		JSpinner sp1=new JSpinner();
		SpinnerNumberModel snm=new SpinnerNumberModel(0.25d,0.05d,1.0d,0.05d);
		sp1.setModel(snm);
		JPanel p2=new JPanel(new GridLayout(1,2));
		p2.add(new JLabel("解像度 "));
		p2.add(sp1);
		panel.add(p2);
		JPanel p3=new JPanel(new GridLayout(1,2));
		p3.add(new JLabel("セパレータ "));
		JComboBox<String> sepc=new JComboBox<>(new String[]{"カンマ","スペース","タブ"});
		sepc.setSelectedIndex(0);
		p3.add(sepc);
		panel.add(p3);
		String[] sel=new String[10];
		for(int i=0;i<sel.length;i++)sel[i]=Integer.toString(i+1)+"列目";
		JComboBox<String> px=new JComboBox<>(sel);px.setSelectedIndex(0);
		JComboBox<String> py=new JComboBox<>(sel);py.setSelectedIndex(1);
		JComboBox<String> pz=new JComboBox<>(sel);pz.setSelectedIndex(2);
		JPanel p4=new JPanel(new GridLayout(1,2));
		p4.add(new JLabel("X座標 "));
		p4.add(px);
		panel.add(p4);
		JPanel p5=new JPanel(new GridLayout(1,2));
		p5.add(new JLabel("Y座標 "));
		p5.add(py);
		panel.add(p5);
		JPanel p6=new JPanel(new GridLayout(1,2));
		p6.add(new JLabel("Z座標 "));
		p6.add(pz);
		panel.add(p6);
		int ck=JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel, "Info", JOptionPane.YES_NO_OPTION);
		if(ck==JOptionPane.YES_OPTION) {
			Map<String,Object> ret=new HashMap<>();
			ret.put("coordSys", comm.getSelectedIndex());
			ret.put("resolution", ((Number)sp1.getValue()));
			ret.put("separator",sepc.getSelectedItem());
			ret.put("x", px.getSelectedIndex());
			ret.put("y", py.getSelectedIndex());
			ret.put("z", pz.getSelectedIndex());
			return ret;
		}else {
			return null;
		}
	}
*/
	public static String getSeparator(String arg) {
		if(arg.equals("カンマ")) {
			return ",";
		}else if(arg.equals("スペース")) {
			return " ";
		}else {
			return "\t";
		}
	}
	
	/*
	public static Map<String,Number> getCoordSysAndLider() {
		String[] sp=new String[18];
		for(int i=0;i<sp.length;i++) {
			if(i==0) {
				sp[i]="WGS84";
			}else if(i<=9) {
				sp[i]="平面直角第0"+Integer.toString(i)+"系";
			}else {
				sp[i]="平面直角第"+Integer.toString(i)+"系";
			}
		}
		JComboBox<String> comm=new JComboBox<>(sp);
		JPanel panel=new JPanel(new GridLayout(3,1));
		panel.add(comm);
		comm.setSelectedIndex(6);
		JSpinner sp1=new JSpinner();
		SpinnerNumberModel snm=new SpinnerNumberModel(0.25d,0.05d,1.0d,0.05d);
		sp1.setModel(snm);
		JPanel p2=new JPanel(new BorderLayout());
		p2.add(new JLabel("解像度 "),BorderLayout.WEST);
		p2.add(sp1,BorderLayout.CENTER);
		panel.add(p2);
		int ck=JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), panel, "Info", JOptionPane.YES_NO_OPTION);
		if(ck==JOptionPane.YES_OPTION) {
			Map<String,Number> ret=new HashMap<>();
			ret.put("coordSys", comm.getSelectedIndex());
			ret.put("resolution", ((Number)sp1.getValue()));
			return ret;
		}else {
			return null;
		}
	}
	*/
	
	public static double[][] npyToArray(double[][] npy){
		double[][] ret=new double[npy[0].length][npy.length];
		for(int i=0;i<ret.length;i++) {
			for(int j=0;j<ret[i].length;j++) {
				ret[i][j]=npy[j][i];
			}
		}
		return ret;
	}
		
	public static BufferedImage outliersProcessing(BufferedImage dem) {
		double[][] dd=getTable(dem);
		double sum=0;
		double n=0;
		for(int i=0;i<dd.length;i++) {
			for(int j=0;j<dd[i].length;j++) {
				if(Double.isNaN(dd[i][j]))continue;
				sum +=dd[i][j];
				n++;
			}
		}
		double ave=sum/n;
		double ss=0;
		for(int i=0;i<dd.length;i++) {
			for(int j=0;j<dd[i].length;j++) {
				if(Double.isNaN(dd[i][j]))continue;
				ss +=Math.pow(dd[i][j]-ave,2);
			}
		}
		double std=Math.sqrt(ss/(n-1));
		double up=ave+std*3;
		double dw=ave-std*3;
		for(int i=0;i<dd.length;i++) {
			for(int j=0;j<dd[i].length;j++) {
				if(Double.isNaN(dd[i][j]))continue;
				if(dd[i][j]>=up||dd[i][j]<=dw)dem.setRGB(i, j, NA);
			}
		}
		return dem;
	}
	
	public static double[][] outliersProcessing(double[][] dd) {
		double sum=0;
		double n=0;
		for(int i=0;i<dd.length;i++) {
			for(int j=0;j<dd[i].length;j++) {
				if(Double.isNaN(dd[i][j]))continue;
				sum +=dd[i][j];
				n++;
			}
		}
		double ave=sum/n;
		double ss=0;
		for(int i=0;i<dd.length;i++) {
			for(int j=0;j<dd[i].length;j++) {
				if(Double.isNaN(dd[i][j]))continue;
				ss +=Math.pow(dd[i][j]-ave,2);
			}
		}
		double std=Math.sqrt(ss/(n-1));
		double up=ave+std*3;
		double dw=ave-std*3;
		for(int i=0;i<dd.length;i++) {
			for(int j=0;j<dd[i].length;j++) {
				if(Double.isNaN(dd[i][j]))continue;
				if(dd[i][j]>=up||dd[i][j]<=dw)dd[i][j]=0;
			}
		}
		return dd;
	}
	
	
	public static void main(String[] args) {
		double[] p= {-0.25,-50.5,63.4,900.8};
		for(double v : p) {
			int col=getRGB(v);
			double z=getZ(col);
			System.out.println(v+"  "+z);
		}
	}
}
