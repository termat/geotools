package net.termat.geo.elevation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.tinfour.common.Vertex;
import org.tinfour.interpolation.TriangularFacetInterpolator;
import org.tinfour.interpolation.VertexValuatorDefault;
import org.tinfour.standard.IncrementalTin;

import net.termat.geo.util.PCUtil;

public class TinInterpoler{
	private BufferedImage src,img;
	private List<Vertex> list;
	private IncrementalTin tin;
	private int index=0;

	public TinInterpoler(BufferedImage src){
		int w=src.getWidth();
		int h=src.getHeight();
		this.src=src;
		list=new ArrayList<>();
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				int col=src.getRGB(i, j);
				if(col==PCUtil.NA)continue;
				double z=PCUtil.getZ(col);
				Vertex v=new Vertex(i,j,z,index++);
				list.add(v);
			}
		}
	}

	public void create(){
		tin=new IncrementalTin();
		tin.add(list, null);
		TriangularFacetInterpolator tfi=new TriangularFacetInterpolator(tin);
		VertexValuatorDefault vvd=new VertexValuatorDefault();
		int w=src.getWidth();
		int h=src.getHeight();
		img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		PCUtil.setImageNA(img);
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				int col=src.getRGB(i, j);
				if(col!=PCUtil.NA){
					img.setRGB(i, j, col);
				}else{
					double hh=tfi.interpolate(i, j, vvd);
					img.setRGB(i, j, PCUtil.getRGB(hh));
				}
			}
		}
	}

	public BufferedImage getImage(){
		return img;
	}

	public BufferedImage getTINImage(double scale)throws IOException{
		int width=(int)Math.floor(img.getWidth()*scale);
		int height=(int)Math.floor(img.getHeight()*scale);
		BufferedImage ret=TinRender.rendar(tin, width, height);
		return ret;
	}

	public static void main(String[] args){
		File f=new File("test2.png");
		BufferedImage im;
		try {
			im = ImageIO.read(f);
			TinInterpoler app=new TinInterpoler(im);
			app.create();
			ImageIO.write(app.getImage(), "png", new File("dem.png"));
			ImageIO.write(app.getTINImage(3.0), "png", new File("mesh.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
