package net.termat.geo.db;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.termat.geo.util.PCUtil;

public class PointCloudDB {
	protected ConnectionSource connectionSource = null;
	protected  Dao<Index,Long> indexDao;
	protected  Dao<Image,Long> imageDao;
	protected  List<ChangeListener> listeners=new ArrayList<>();

	/**
	 * DB接続
	 *
	 * @param dbName DBのパス
	 * @param create DBが無い場合、自動生成
	 * @throws SQLException
	 */
	public void connectDB(String dbName,boolean create) throws SQLException{
		try{
			if(!dbName.endsWith(".db"))dbName=dbName+".db";
			Class.forName("org.sqlite.JDBC");
			connectionSource = new JdbcConnectionSource("jdbc:sqlite:"+dbName);
			indexDao= DaoManager.createDao(connectionSource, Index.class);
			if(create)TableUtils.createTableIfNotExists(connectionSource, Index.class);
			imageDao= DaoManager.createDao(connectionSource, Image.class);
			if(create)TableUtils.createTableIfNotExists(connectionSource, Image.class);
		}catch(SQLException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			connectionSource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void dbchange(){
		ChangeEvent evt=new ChangeEvent(this);
		for(ChangeListener c:listeners)c.stateChanged(evt);
	}

	public void add(int coordSys,String name,String type,BufferedImage img,AffineTransform af){
		Image im=new Image();
		long key=System.currentTimeMillis();
		im.key=key;
		try {
			if(type.contains("PHOTO")) {
				im.imageBytes=ImageUtil.bi2Bytes(img, "jpg");
			}else {
				im.imageBytes=ImageUtil.bi2Bytes(img, "png");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Index index=new Index();
		index.coordSys=coordSys;
		index.type=type;
		index.name=name;
		index.key=key;
		index.dataType=Index.DataType.IMAGE;
		index.setTransform(af);
		index.width=img.getWidth();
		index.height=img.getHeight();
		try {
			imageDao.create(im);
			indexDao.create(index);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbchange();
	}
	
	public void add(int coordSys,String name,String type,BufferedImage img,AffineTransform af,String ext){
		Image im=new Image();
		long key=System.currentTimeMillis();
		im.key=key;
		try {
			im.imageBytes=ImageUtil.bi2Bytes(img, ext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Index index=new Index();
		index.coordSys=coordSys;
		index.type=type;
		index.name=name;
		index.key=key;
		index.dataType=Index.DataType.IMAGE;
		index.setTransform(af);
		index.width=img.getWidth();
		index.height=img.getHeight();
		try {
			imageDao.create(im);
			indexDao.create(index);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbchange();
	}

	public void add(Index index,BufferedImage img,String ext) {
		Image im=new Image();
		long key=System.currentTimeMillis();
		im.key=key;
		index.key=key;
		try {
			im.imageBytes=ImageUtil.bi2Bytes(img, ext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			imageDao.create(im);
			indexDao.create(index);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dbchange();
	}
	
	public void update(Index index) throws SQLException {
		indexDao.update(index);
	}

	public void update(Index index,BufferedImage img,String ext) throws SQLException {
		QueryBuilder<Image, Long> query=imageDao.queryBuilder();
		query.where().eq("key",index.key);
		List<Image> list=imageDao.query(query.prepare());
		Image src=list.get(0);
		try {
			src.imageBytes=ImageUtil.bi2Bytes(img, ext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageDao.update(src);
	}

	public void delete(Index index) throws SQLException{
		indexDao.delete(index);
		QueryBuilder<Image, Long> query=imageDao.queryBuilder();
		query.where().eq("key",index.key);
		List<Image> list=imageDao.query(query.prepare());
		Image img=list.get(0);
		imageDao.delete(img);
		dbchange();
	}

	public String[] getTypeName() throws SQLException {
		List<Index> li=indexDao.queryForAll();
		Set<String> ret=new HashSet<>();
		for(Index i : li)ret.add(i.type);
		String[] str=ret.toArray(new String[ret.size()]);
		Arrays.sort(str);	
		return str;
	}
	
	public List<Index> getIndexes() throws SQLException{
		return indexDao.queryForAll();
	}

	public List<Index> getIndexes(String type) throws SQLException{
		QueryBuilder<Index, Long> query=indexDao.queryBuilder();
		query.where().eq("type",type);
		return indexDao.query(query.prepare());
	}
	
	public List<Index> getIndexesCloseType(String type) throws SQLException{
		List<Index> all=getIndexes();
		List<Index> ret=new ArrayList<>();
		for(Index i : all) {
			if(i.type.contains(type))ret.add(i);
		}
		return ret;
	}
	
	public List<Index> getIndexes(String name,String type) throws SQLException{
		QueryBuilder<Index, Long> query=indexDao.queryBuilder();
		query.where().eq("type",type).and().eq("name", name);
		return indexDao.query(query.prepare());
	}
	
	public List<Index> getIndexes(String type,Rectangle2D rect) throws SQLException{
		List<Index> li=getIndexes(type);
		List<Index> ret=new ArrayList<>();
		for(Index i : li) {
			if(rect.intersects(i.getBounds()))ret.add(i);
		}
		return null;
	}
	
	public List<Index> getIndexesByName(String name) throws SQLException{
		QueryBuilder<Index, Long> query=indexDao.queryBuilder();
		query.where().eq("name",name);
		return indexDao.query(query.prepare());
	}
	
	public BufferedImage getImage(Index index) throws SQLException, IOException{
		if(index.dataType!=Index.DataType.IMAGE)throw new IOException("Index-"+index.id+" is not IMAGE.");
		QueryBuilder<Image, Long> query=imageDao.queryBuilder();
		query.where().eq("key",index.key);
		List<Image> list=imageDao.query(query.prepare());
		Image img=list.get(0);
		return ImageUtil.bytes2Bi(img.imageBytes);
	}

	public BufferedImage getImage(Shape sp,double resolution,String type) throws SQLException, IOException{
		Rectangle2D rect=sp.getBounds2D();
		double[] param=new double[]{
				resolution,0,0,-resolution,rect.getX(),rect.getY()+rect.getHeight()};
		AffineTransform af=new AffineTransform(param);
		AffineTransform iaf=null;
		try {
			iaf=af.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		int w=(int)Math.round(rect.getWidth()/af.getScaleX());
		if(w%2==0)w++;
		int h=(int)Math.abs(Math.round(rect.getHeight()/af.getScaleY()));
		if(h%2==0)h++;
		BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		Graphics2D g=img.createGraphics();
		g.setBackground(new Color(PCUtil.NA));
		g.clearRect(0, 0, w, h);
		g.dispose();
		QueryBuilder<Index, Long> query=indexDao.queryBuilder();
		query.where().eq("type",type);
		List<Index> list=indexDao.query(query.prepare());
		for(Index ix : list){
			Rectangle2D rx=ix.getBounds();
			if(rx.intersects(rect)||rx.contains(rect)){
				BufferedImage tmp=getImage(ix);
				AffineTransform at=ix.getTransform();
				for(int i=0;i<tmp.getWidth();i++){
					for(int j=0;j<tmp.getHeight();j++){
						Point2D px=at.transform(new Point2D.Double(i,j), new Point2D.Double());
						if(!sp.contains(px))continue;
						px=iaf.transform(px, new Point2D.Double());
						int xx=(int)Math.round(px.getX());
						int yy=(int)Math.round(px.getY());
						if(xx>=0&&xx<w&&yy>=0&&yy<h){
							img.setRGB(xx, yy, tmp.getRGB(i, j));
						}
					}
				}
			}
		}
		return img;
	}
	
	public BufferedImage getImage(Rectangle2D XY,double resolution,String type) throws SQLException, IOException{
		double[] param=new double[]{
			resolution,0,0,-resolution,XY.getX(),XY.getY()+XY.getHeight()};
		AffineTransform af=new AffineTransform(param);
		AffineTransform iaf=null;
		try {
			iaf=af.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		int w=(int)Math.round(XY.getWidth()/af.getScaleX());
		if(w%2==0)w++;
		int h=(int)Math.abs(Math.round(XY.getHeight()/af.getScaleY()));
		if(h%2==0)h++;
		BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		QueryBuilder<Index, Long> query=indexDao.queryBuilder();
		query.where().eq("type",type);
		List<Index> list=indexDao.query(query.prepare());
		for(Index ix : list){
			Rectangle2D rx=ix.getBounds();
			if(XY.intersects(rx)){
				BufferedImage tmp=getImage(ix);
				AffineTransform at=ix.getTransform();
				for(int i=0;i<tmp.getWidth();i++){
					for(int j=0;j<tmp.getHeight();j++){
						Point2D px=at.transform(new Point2D.Double(i,j), new Point2D.Double());
						if(!XY.contains(px))continue;
						px=iaf.transform(px, new Point2D.Double());
						int xx=(int)Math.round(px.getX());
						int yy=(int)Math.round(px.getY());
						if(xx>=0&&xx<w&&yy>=0&&yy<h){
							img.setRGB(xx, yy, tmp.getRGB(i, j));
						}
					}
				}
			}
		}
		return img;
	}

	public BufferedImage getTileImage(Rectangle2D rectLonlat,String type,int sizeWH) throws SQLException, IOException {
		List<Index> list=getIndexes(type);
		if(list.size()==0)return null;
		Index i0=list.get(0);
		Point2D xy1=PCUtil.getXY(i0.coordSys, rectLonlat.getX(), rectLonlat.getY());
		Point2D xy2=PCUtil.getXY(i0.coordSys, rectLonlat.getX()+rectLonlat.getWidth(), rectLonlat.getY()+rectLonlat.getHeight());
		Rectangle2D r=new Rectangle2D.Double(xy1.getX(),xy1.getY(),0,0);
		r.add(xy2);
		BufferedImage ret=new BufferedImage(sizeWH,sizeWH,BufferedImage.TYPE_INT_RGB);
		Graphics2D g=ret.createGraphics();
		g.setBackground(new Color(0,0,0));
		g.clearRect(0, 0, sizeWH, sizeWH);
		g.dispose();
		double[] param=new double[] {r.getWidth()/sizeWH,0,0,-r.getWidth()/sizeWH,r.getX(),r.getY()+r.getHeight()};
		AffineTransform af=new AffineTransform(param);
		AffineTransform iaf=null;
		int ck=0;
		for(Index i : list) {
			if(r.intersects(i.getBounds())) {
				BufferedImage tmp=getImage(i);
				AffineTransform at=i.getTransform();
				try {
					iaf=at.createInverse();
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
				for(int ix=0;ix<sizeWH;ix++) {
					for(int iy=0;iy<sizeWH;iy++) {
						Point2D p=af.transform(new Point2D.Double(ix,iy), new Point2D.Double());
						p=iaf.transform(p, new Point2D.Double());
						int xx=(int)Math.floor(p.getX());
						int yy=(int)Math.floor(p.getY());
						if(xx>=0&&xx<i.width&&yy>=0&&yy<i.height) {
							ret.setRGB(ix, iy, tmp.getRGB(xx, yy));
							ck=1;
						}
					}
				}
			}
		}
		if(ck==1) {
			return ret;
		}else {
			return null;
		}
	}
	
	public BufferedImage getExpantionImage(Index id,int exp) throws SQLException, IOException{
		BufferedImage img=new BufferedImage(id.width+exp*2,id.height+exp*2,BufferedImage.TYPE_INT_RGB);
		Graphics2D g=img.createGraphics();
		g.setBackground(new Color(PCUtil.NA));
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		AffineTransform af=id.getTransform();
		BufferedImage tmp=getImage(id);
		g.drawImage(tmp, exp, exp, null);
		g.dispose();
		double[] param=new double[]{af.getScaleX(),0,0,af.getScaleY(),af.getTranslateX()-af.getScaleX()*exp,af.getTranslateY()-af.getScaleY()*exp};
		af=new AffineTransform(param);
		AffineTransform iaf=null;
		try {
			iaf=af.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		Rectangle2D rect=af.createTransformedShape(new Rectangle2D.Double(0,0,img.getWidth(),img.getHeight())).getBounds2D();
		QueryBuilder<Index, Long> query=indexDao.queryBuilder();
		query.where().eq("type",id.type);
		List<Index> list=indexDao.query(query.prepare());
		for(Index i : list){
			if(i.equals(id))continue;
			if(i.getBounds().intersects(rect)){
				tmp=getImage(i);
				AffineTransform at=i.getTransform();
				for(int x=0;x<tmp.getWidth();x++){
					for(int y=0;y<tmp.getHeight();y++){
						Point2D p=at.transform(new Point2D.Double(x,y), new Point2D.Double());
						if(rect.contains(p)){
							Point2D pt=iaf.transform(p, new Point2D.Double());
							int xx=(int)Math.round(pt.getX());
							int yy=(int)Math.round(pt.getY());
							if(xx>=0&&xx<img.getWidth()&&yy>=0&&yy<img.getHeight()){
								img.setRGB(xx, yy, tmp.getRGB(x, y));
							}
						}
					}
				}
			}
		}
		return img;
	}
	
	public Rectangle2D getBounds() throws SQLException {
		Rectangle2D rect=null;
		List<Index> list=getIndexes();
		Set<Rectangle2D> ck=new HashSet<>();
		for(Index i : list) {
			if(rect==null) {
				Rectangle2D p=i.getBounds();
				rect=PCUtil.getLonlatRect(i.coordSys, p);
				ck.add(p);
			}else {
				Rectangle2D p=i.getBounds();
				if(!ck.contains(p)) {
					rect.add(PCUtil.getLonlatRect(i.coordSys, p));
					ck.add(p);
				}
			}
		}
		return rect;
	}
	
	public int size() throws SQLException {
		return getIndexes().size();
	}
	
	public void addChangeListsner(ChangeListener cl){
		this.listeners.add(cl);
	}

	public void removeChangeListsner(ChangeListener cl){
		this.listeners.remove(cl);
	}
}
