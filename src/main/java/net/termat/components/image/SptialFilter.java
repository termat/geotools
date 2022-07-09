package net.termat.components.image;

import java.util.Arrays;


class SptialFilter extends AbstractImageFilter {
	private int width;
	private int height;
	private Operator op;
	
	public static final Operator OP_MEDIAN=new Median();
	public static final Operator OP_AVERAGE=new General(
				new double[]{0.1,0.1,0.1,0.1,0.2,0.1,0.1,0.1,0.1}	
				);
	public static final Operator OP_PREWITT=new Differential(
			new double[]{1,0,-1,1,0,-1,1,0,-1},
			new double[]{1,1,1,0,0,0,-1,-1,-1}
			);
	public static final Operator OP_SOBEL=new Differential(
			new double[]{1,0,-1,2,0,-2,1,0,-1},
			new double[]{1,2,1,0,0,0,-1,-2,-1}
			);
	public static final Operator OP_LAPLACIAN=new General(new double[]{0,1,0,1,-4,1,0,1,0});
	public static final Operator OP_SHARP=new General(new double[]{0,-1,0,-1,5,-1,0,-1,0});
	public static final Operator OP_VERTICALEDGE=new General(new double[]{-0.5,1,-0.5,-0.5,1,-0.5,-0.5,1,-0.5});
	public static final Operator OP_HORIZONTALEDGE=new General(new double[]{-0.5,-0.5,-0.5,1,1,1,-0.5,-0.5,-0.5});
	
	/**
	 * 
	 */
	public SptialFilter(Operator o) {
		op=o;
	}

	protected void filterImage() {
		int[] p=new int[width];
		for (int y=1;y<height-1;y++){
			p[0]=getPixelValue(0,y);
			for (int x=1;x<width-1;x++){
				int[] px=new int[9];
				px[0]=getPixelValue(x-1,y-1);
				px[1]=getPixelValue(x,y-1);
				px[2]=getPixelValue(x+1,y-1);
				px[3]=getPixelValue(x-1,y);
				px[4]=getPixelValue(x,y);
				px[5]=getPixelValue(x+1,y);
				px[6]=getPixelValue(x-1,y+1);
				px[7]=getPixelValue(x,y+1);
				px[8]=getPixelValue(x+1,y+1);
				p[x]=op.operate(px);
			}
			p[width-1]=getPixelValue(width-1,y);
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}

	public static Object createAverageOp(double weight){
		double s=8+weight;
		double[] a=new double[]{
				1/s,1/s,1/s,
				1/s,weight/s,1/s,
				1/s,1/s,1/s};
		return new General(a);
	}
	
	public static Object createOp(double[] arg){
		if(arg.length!=9)throw new RuntimeException();
		return new General(arg);
	}

	public static Object createDiffOp(double[] x,double[] y){
		if(x.length!=9)throw new RuntimeException();
		if(y.length!=9)throw new RuntimeException();
		return new Differential(x,y);
	}
	
	public void setDimensions(int w,int h){
		super.setDimensions(w,h);
		width=w;
		height=h;
		consumer.setDimensions(width,height);
	}

	interface Operator{
		public int operate(int[] p);
	}

	private static class Median implements Operator{
		public int operate(int[] p) {
			Arrays.sort(p);
			return p[4];
		}
	}

	private static class General implements Operator{
		private double[] op;
		General(double[] o){
			op=o;
		}
	
		public int operate(int[] p) {
			int[] rgb=new int[4];
			int[] val=new int[4];
			for(int i=0;i<p.length;i++){
				int[] tmp=getPixelRGBValue(p[i]);
				for(int j=1;j<tmp.length;j++){
					val[j] +=(((double)tmp[j])*op[i]);
				}
			}
			rgb[0]=p[0];
			for(int i=1;i<rgb.length;i++){
				rgb[i]=(int)val[i];
				if(rgb[i]>255)rgb[i]=255;
				if(rgb[i]<0)rgb[i]=0;
			}
			return (rgb[0]<<24)+(rgb[1]<<16)+(rgb[2]<<8)+rgb[3];
		}
	}

	private static class Differential implements Operator{
		private double[] opx;
		private double[] opy;
		Differential(double[] x,double[] y){
			opx=x;
			opy=y;
		}

		public int operate(int[] p) {
			int[] rgb=new int[4];
			double[][] val=new double[2][4];
			for(int i=0;i<p.length;i++){
				int[] tmp=getPixelRGBValue(p[i]);
				for(int j=1;j<tmp.length;j++){
					val[0][j] +=(((double)tmp[j])*opx[i]);
					val[1][j] +=(((double)tmp[j])*opy[i]);
				}
			}
			rgb[0]=p[4];
			for(int i=1;i<rgb.length;i++){
				rgb[i]=(int)Math.sqrt((int)val[0][i]*(int)val[0][i]+(int)val[1][i]*(int)val[1][i]);
				if(rgb[i]>255)rgb[i]=255;
				if(rgb[i]<0)rgb[i]=0;
			}
			return (rgb[0]<<24)+(rgb[1]<<16)+(rgb[2]<<8)+rgb[3];
		}
	}
}
