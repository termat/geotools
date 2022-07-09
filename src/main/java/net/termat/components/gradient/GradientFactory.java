package net.termat.components.gradient;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class GradientFactory {
	public static final Color[] GRADIENT_RGB=new Color[]{Color.BLUE,Color.GREEN,Color.RED};
	public static final Color[] GRADIENT_NATURAL_COLOR=new Color[]{Color.GREEN,new Color(187,135,12),Color.WHITE};
	private enum GType{XAXIS,YAXIS}
	public static final GType X_AXIS=GType.XAXIS;
	public static final GType Y_AXIS=GType.YAXIS;

	private GradientFactory(){}

	public static Gradient createDefaultGradient(){
		return DefaultGradient.getInstance();
	}

	public static Gradient createGradient(Color[] colors){
		if(colors.length==2){
			LinerGradient ret=new LinerGradient(colors,1.0);
			return ret;
		}else if(colors.length>2){
			SplineGradient ret=new SplineGradient(colors,1.0);
			return ret;
		}else{
			throw new IllegalArgumentException();
		}
	}

	public static Gradient createGradient(Color[] colors,double order){
		if(colors.length==2){
			LinerGradient ret=new LinerGradient(colors,order);
			return ret;
		}else if(colors.length>2){
			SplineGradient ret=new SplineGradient(colors,order);
			return ret;
		}else{
			throw new IllegalArgumentException();
		}
	}

	public static BufferedImage createColorBarHorizontal(int width,int height,Gradient grad,Range range){
		BufferedImage ret=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<width;i++){
			double rate=(double)i/(double)width;
			int rgb=grad.getColorByInt(rate);
			for(int j=0;j<height;j++){
				ret.setRGB(i, j, rgb);
			}
		}
		return ret;
	}

	public static BufferedImage createColorBarVertical(int width,int height,Gradient grad,Range range){
		BufferedImage ret=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<height;i++){
			double rate=(double)i/(double)height;
			int rgb=grad.getColorByInt(rate);
			for(int j=0;j<width;j++){
				ret.setRGB(j,i, rgb);
			}
		}
		return ret;
	}

	private static class LinerGradient implements Gradient{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private double[][] color;
		private double order=1.0;
		private Color nanColor=Color.BLACK;

		public LinerGradient(Color[] c,double o){
			color=new double[3][2];
			color[0][0]=(double)c[0].getRed();
			color[0][1]=(double)c[1].getRed();
			color[1][0]=(double)c[0].getGreen();
			color[1][1]=(double)c[1].getGreen();
			color[2][0]=(double)c[0].getBlue();
			color[2][1]=(double)c[1].getBlue();
			order=o;
		}

		@Override
		public Color getColor(double arg) {
			double val=Math.pow(arg, order);
			if(val<0)val=0;
			if(val>1.0)val=1.0;
			int r=(int)((color[0][1]-color[0][0])*val+color[0][0]);
			int g=(int)((color[1][1]-color[1][0])*val+color[1][0]);
			int b=(int)((color[2][1]-color[2][0])*val+color[2][0]);
			Color ret=new Color(r,g,b,255);
			return ret;
		}

		@Override
		public float[] getColorByFloat(double arg) {
			double val=Math.pow(arg, order);
			if(val<0)val=0;
			if(val>1.0)val=1.0;
			float r=(float)((color[0][1]-color[0][0])*val+color[0][0])/255.0f;
			float g=(float)((color[1][1]-color[1][0])*val+color[1][0])/255.0f;
			float b=(float)((color[2][1]-color[2][0])*val+color[2][0])/255.0f;
			return new float[]{r,g,b,1.0f};
		}

		@Override
		public int getColorByInt(double arg) {
			double val=Math.pow(arg, order);
			if(val<0)val=0;
			if(val>1.0)val=1.0;
			int r=(int)((color[0][1]-color[0][0])*val+color[0][0]);
			int g=(int)((color[1][1]-color[1][0])*val+color[1][0]);
			int b=(int)((color[2][1]-color[2][0])*val+color[2][0]);
			if(r<0)r=0;
			if(r>255)r=255;
			if(g<0)g=0;
			if(g>255)g=255;
			if(b<0)b=0;
			if(b>255)b=255;
			int ret=(255<<24)+(r<<16)+(g<<8)+b;
			return ret;
		}

		@Override
		public void setNanColor(Color c){
			nanColor=c;
		}

		@Override
		public Color getNanColor(){
			return nanColor;
		}
	}

	private static class SplineGradient implements Gradient{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private double[][] color;
		private double[] value;
		private Spline[] spline;
		private double order=1.0;
		private Color nanColor=Color.BLACK;

		public SplineGradient(Color[] c,double o){
			color=new double[3][c.length];
			value=new double[c.length];
			spline=new Spline[3];
			for(int i=0;i<c.length;i++){
				color[0][i]=(double)c[i].getRed();
				color[1][i]=(double)c[i].getGreen();
				color[2][i]=(double)c[i].getBlue();
				value[i]=(float)i/(float)(color.length-1);
			}
			spline[0]=new Spline(value,color[0],Spline.NONCYCLE);
			spline[1]=new Spline(value,color[1],Spline.NONCYCLE);
			spline[2]=new Spline(value,color[2],Spline.NONCYCLE);
			order=o;
		}

		public Color getColor(double v){
			if(Double.isNaN(v)||Double.isInfinite(v)){
				return nanColor;
//				throw new ArithmeticException("NaN");
			}else{
				double val=Math.pow(v, order);
				if(val<0)val=0;
				if(val>1.0)val=1.0;
				int r=(int)(spline[0].interpolate(val));
				int g=(int)(spline[1].interpolate(val));
				int b=(int)(spline[2].interpolate(val));
				if(r<0)r=0;
				if(r>255)r=255;
				if(g<0)g=0;
				if(g>255)g=255;
				if(b<0)b=0;
				if(b>255)b=255;
				Color ret=new Color(r,g,b,255);
				return ret;
			}
		}

		public float[] getColorByFloat(double v){
			if(Double.isNaN(v)||Double.isInfinite(v)){
				return new float[]{0,0,0,1.0f};
//				throw new ArithmeticException("NaN");
			}else{
				double val=Math.pow(v, order);
				if(val<0)val=0;
				if(val>1.0)val=1.0;
				float r=(float)(spline[0].interpolate(val))/255.0f;
				float g=(float)(spline[1].interpolate(val))/255.0f;
				float b=(float)(spline[2].interpolate(val))/255.0f;
				if(r<0.0f)r=0.0f;
				if(r>1.0f)r=1.0f;
				if(g<0.0f)g=0.0f;
				if(g>1.0f)g=1.0f;
				if(b<0.0f)b=0.0f;
				if(b>1.0f)b=1.0f;
				return new float[]{r,g,b,1.0f};
			}
		}

		@Override
		public int getColorByInt(double v) {
			if(Double.isNaN(v)||Double.isInfinite(v)){
				return (255<<24)+(0<<16)+(0<<8)+0;
//				throw new ArithmeticException("NaN");
			}else{
				double val=Math.pow(v, order);
				if(val<0)val=0;
				if(val>1.0)val=1.0;
				int r=(int)(spline[0].interpolate(val));
				int g=(int)(spline[1].interpolate(val));
				int b=(int)(spline[2].interpolate(val));
				if(r<0)r=0;
				if(r>255)r=255;
				if(g<0)g=0;
				if(g>255)g=255;
				if(b<0)b=0;
				if(b>255)b=255;
				int ret=(255<<24)+(r<<16)+(g<<8)+b;
				return ret;
			}
		}

		@Override
		public void setNanColor(Color c){
			nanColor=c;
		}

		@Override
		public Color getNanColor(){
			return nanColor;
		}
	}

	private static class DefaultGradient implements Gradient{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private static DefaultGradient instance=null;
		private Color nanColor=Color.BLACK;

		private DefaultGradient() {
			super();
		}

		static Gradient getInstance(){
			if(instance==null){
				instance=new DefaultGradient();
				return instance;
			}else{
				return instance;
			}
		}

		@Override
		public Color getColor(double val) {
			if(Double.isNaN(val)){
				return nanColor;
//				throw new ArithmeticException("NaN");
			}else{
				float r=(float)Math.cos(Math.PI*((val-0.81)*1.1));
				float g=(float)Math.cos(Math.PI*((val-0.50)*1.0));
				float b=(float)Math.cos(Math.PI*((val-0.19)*1.1));
				if(r<0)r=0;
				if(g<0)g=0;
				if(b<0)b=0;
				if(r>1.0f)r=1.0f;
				if(g>1.0f)g=1.0f;
				if(b>1.0f)b=1.0f;
				return new Color(r,g,b,1.0f);
			}
		}

		@Override
		public float[] getColorByFloat(double val) {
			if(Double.isNaN(val)){
				return new float[]{0,0,0,1.0f};
//				throw new ArithmeticException("NaN");
			}else{
				float r=(float)Math.cos(Math.PI*((val-0.81)*1.1));
				float g=(float)Math.cos(Math.PI*((val-0.50)*1.0));
				float b=(float)Math.cos(Math.PI*((val-0.19)*1.1));
				if(r<0)r=0;
				if(g<0)g=0;
				if(b<0)b=0;
				if(r>1.0f)r=1.0f;
				if(g>1.0f)g=1.0f;
				if(b>1.0f)b=1.0f;
				return new float[]{r,g,b,1.0f};
			}
		}

		@Override
		public int getColorByInt(double val) {
			if(Double.isNaN(val)){
				return (255<<24)+(0<<16)+(0<<8)+0;
			}else{
				int r=(int)(Math.cos(Math.PI*((val-0.81)*1.1))*255.0);
				int g=(int)(Math.cos(Math.PI*((val-0.50)*1.0))*255.0);
				int b=(int)(Math.cos(Math.PI*((val-0.19)*1.1))*255.0);
				if(r<0)r=0;
				if(g<0)g=0;
				if(b<0)b=0;
				if(r>255)r=255;
				if(g>255)g=255;
				if(b>255)b=255;
				int ret=(255<<24)+(r<<16)+(g<<8)+b;
				return ret;
			}
		}

		@Override
		public void setNanColor(Color c){
			nanColor=c;
		}

		@Override
		public Color getNanColor(){
			return nanColor;
		}
	}

}
