package net.termat.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private File file;
	private BufferedImage img;
	private boolean isFit=true;
	private double scale=1.0;
	private Point sp=null;
	private Point ep=null;
	private Stroke stroke=new BasicStroke(2.0f,BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_MITER,10.0f,new float[]{10.0f, 3.0f},0.0f);
	private int counter=0;
	private Set<String> files=new HashSet<String>();
	private BufferedImage backup;
	private List<Shape> shapes=new ArrayList<Shape>();

	public ImagePanel(){
		super(new BorderLayout());
		img=null;
//		ThisMouseListener ma=new ThisMouseListener();
//		super.addMouseListener(ma);
//		super.addMouseMotionListener(ma);
		super.addMouseWheelListener(new ThisWheelListener());
	}

	public ActionListener getCopyAction(){
		ActionListener al=new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Toolkit tk = Toolkit.getDefaultToolkit();
				Clipboard cb = tk.getSystemClipboard();
				cb.setContents(new Transferable() {
					@Override
					public boolean isDataFlavorSupported(DataFlavor flavor) {
						return flavor.equals(DataFlavor.imageFlavor);
					}
					@Override
					public DataFlavor[] getTransferDataFlavors() {
						return new DataFlavor[] {DataFlavor.imageFlavor};
					}
					@Override
					public Object getTransferData(DataFlavor flavor)
							throws UnsupportedFlavorException, IOException {
						if (flavor.equals(DataFlavor.imageFlavor)) {
							return img;
						}
						throw new UnsupportedFlavorException(flavor);
					}
				}, null);
			}
		};
		return al;
	}

	public void setBackup(){
		backup=img;
	}

	public void callBackup(){
		this.setImage(backup);
	}

	public File getFile() {
		return file;
	}

	public void setFit(boolean isFit) {
		this.isFit = isFit;
		scale=1.0;
		if(this.isFit)this.setPreferredSize(null);
		updateUI();
	}

	public void loadImage(File f) throws IOException{
		file=f;
		files.clear();
		scanFiles(f);
		if(f.getName().toLowerCase().endsWith(".gz")) {
			InputStream is = Files.newInputStream(f.toPath());
			GZIPInputStream gis = new GZIPInputStream(is);			
			img=ImageIO.read(gis);
		}else {
			img=ImageIO.read(f);
		}
		try {
			img.getRGB(0, 0);
		}catch(java.lang.ArrayIndexOutOfBoundsException ee) {
			throw new IOException("GTIFは画像表示できません。");
		}
		updateUI();
	}

	private void scanFiles(File f){
		File[] ff=f.getParentFile().listFiles();
		for(File fx : ff){
			files.add(fx.getName());
		}
	}

	public BufferedImage getImage(){
		return img;
	}

	public void setImage(BufferedImage i){
		img=i;
		repaint();
	}

	@Override
	public void paint(Graphics g){
		if(img!=null){
			g.clearRect(0, 0, getWidth(), getHeight());
			double w=(double)this.getWidth();
			double h=(double)this.getHeight();
			if(isFit){
				scale=Math.min(w/(double)img.getWidth(),h/(double)img.getHeight());
				((Graphics2D)g).scale(scale, scale);
				g.drawImage(img, 0, 0, this);
			}else{
				this.setPreferredSize(
					new Dimension((int)((double)img.getWidth()*scale),(int)((double)img.getHeight()*scale)));
				((Graphics2D)g).scale(scale, scale);
				g.drawImage(img, 0, 0, this);
			}
			if(sp!=null&&ep!=null){
				((Graphics2D)g).setStroke(stroke);
				((Graphics2D)g).setColor(Color.RED);
				double xx=Math.min(sp.x/scale, ep.x/scale);
				double yy=Math.min(sp.y/scale, ep.y/scale);
				Rectangle2D r=new Rectangle2D.Double(xx,yy,Math.abs(sp.x-ep.x)/scale,Math.abs(sp.y-ep.y)/scale);
				((Graphics2D)g).draw(r);
			}
			for(Shape s :shapes){
				((Graphics2D)g).setStroke(stroke);
				((Graphics2D)g).setColor(Color.RED);
				((Graphics2D)g).draw(s);
			}
		}
	}

	public Point transPoint(int x,int y){
		int xx=(int)((double)x/scale);
		int yy=(int)((double)y/scale);
		return new Point(xx,yy);
	}

	public List<Shape> getShapes() {
		return shapes;
	}
	
	private void saveImage(BufferedImage im)throws Exception{
		String name=file.getName();
		name=name.substring(0, name.lastIndexOf("."));
		while(true){
			name=name+"_"+String.format("%02d", counter++)+".jpg";
			if(!files.contains(name))break;
		}
		File out=new File(file.getParentFile().getAbsolutePath()+"\\"+name);
		ImageIO.write(im, "jpg", out);
	}

	class ThisWheelListener implements MouseWheelListener{
		double rate=0;

		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {
			if(!isFit){
				rate=+arg0.getWheelRotation();
				scale=Math.max(0.5, scale+rate/10.0);
				updateUI();
			}
		}
	}

	class ThisMouseListener extends MouseAdapter{

		@Override
		public void mouseDragged(MouseEvent arg0) {
			if(sp!=null){
				ep=new Point(arg0.getX(),arg0.getY());
				repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if(sp==null){
				sp=new Point(arg0.getX(),arg0.getY());
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(img!=null&&sp!=null&&ep!=null){
				int xx=(int)(Math.min(sp.x, ep.x)/scale);
				int yy=(int)(Math.min(sp.y, ep.y)/scale);
				int ww=(int)(Math.abs(sp.x-ep.x)/scale);
				int hh=(int)(Math.abs(sp.y-ep.y)/scale);
				if(ww==0||hh==0)return;
				BufferedImage tmp=img.getSubimage(xx, yy, ww, hh);
				try{
					saveImage(tmp);
				}catch(Exception e){e.printStackTrace();}
			}
			sp=null;
			ep=null;
			repaint();
		}
	}
}
