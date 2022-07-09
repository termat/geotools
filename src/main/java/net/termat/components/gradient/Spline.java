package net.termat.components.gradient;

public class Spline {
	private enum Type{NONCYCLE,CYCLE}
	public static final Type NONCYCLE=Type.NONCYCLE;
	public static final Type CYCLE=Type.CYCLE;

	private Type type;
	private int num;
	private double[] x, y, z;

	public Spline(double[][] xy,Type _type){
		type=_type;
		num=xy.length;
		x=new double[num];
		y=new double[num];
		for(int i=0;i<xy.length;i++){
			x[i]=xy[i][0];
			y[i]=xy[i][1];
		}
		if(type==NONCYCLE){
			spline_NonCycle();
		}else{
			spline_Cycle();
		}
	}

	public Spline(double[] _x,double[] _y,Type _type){
		type=_type;
		num=_x.length;
		x=_x;
		y=_y;
		if(type==NONCYCLE){
			spline_NonCycle();
		}else{
			spline_Cycle();
		}
	}

	public double interpolate(double _x){
		if(type==NONCYCLE){
			return interpolate_NonCycle(_x);
		}else{
			return interpolate_Cycle(_x);
		}
	}

	private void spline_NonCycle(){
		z=new double[num];
		double[] h=new double[num];
		double[] d=new double[num];
		z[0]=z[num-1]=0;
		for(int i=0;i<num-1;i++){
			h[i]=x[i+1]-x[i];
			d[i+1]=(y[i+1]-y[i])/ h[i];
		}
		z[1]=d[2]-d[1]-h[0]*z[0];
		d[1]=2*(x[2]-x[0]);
		for(int i=1;i<num-2;i++){
			double t=h[i]/d[i];
			z[i+1]=d[i+2]-d[i+1]-z[i]*t;
			d[i+1]=2*(x[i+2]-x[i])-h[i]*t;
		}
		z[num-2] -=h[num-2]*z[num-1];
		for(int i=num-2;i>0;i--){
			z[i]=(z[i]-h[i]*z[i+1])/d[i];
		}
	}

	private double interpolate_NonCycle(double t){
		int i=0;
		int j=num-1;
		while(i<j){
			int k=(i+j)/2;
			if(x[k]<t){
				i=k+1;
			}else{
				j = k;
			}
		}
		if(i>0)i--;
		double h=x[i+1]-x[i];
		double d=t-x[i];
		return (((z[i+1]-z[i])*d/h+z[i]*3)*d
					+((y[i+1]-y[i])/ h
					-(z[i]*2+z[i+1])*h))*d+y[i];
	}

	private void spline_Cycle(){
		num=num-1;
		z=new double[num+1];
		double[] h=new double[num+1];
		double[] d=new double[num+1];
		double[] w=new double[num+1];
		for(int i=0;i<num;i++){
			h[i]=x[i+1]-x[i];
			w[i]=(y[i+1]-y[i])/h[i];
		}
		w[num]=w[0];

		for(int i=1;i<num;i++)d[i]=2*(x[i+1]-x[i-1]);
		d[num]=2*(h[num-1]+h[0]);
		for(int i=1;i<=num;i++)z[i]=w[i]-w[i-1];
		w[1]=h[0];
		w[num-1]=h[num-1];
		w[num]=d[num];
		for(int i=2;i<num-1;i++)w[i]=0;
		for(int i=1;i<num;i++){
			double t=h[i]/d[i];
			z[i+1] -=z[i]*t;
			d[i+1] -=h[i]*t;
			w[i+1] -=w[i]*t;
		}
		w[0]=w[num];
		z[0]=z[num];
		for(int i=num-2;i>=0;i--){
			double t=h[i]/d[i+1];
			z[i] -=z[i+1]*t;
			w[i] -=w[i+1]*t;
		}
		double t=z[0]/w[0];
		z[0]=z[num]=t;
        for (int i=1;i<num;i++)
            z[i]=(z[i]-w[i]*t)/d[i];
	}

	private double interpolate_Cycle(double t){
		double period=x[num]-x[0];
		while(t>x[num]) t -=period;
		while(t<x[0]) t +=period;
		int i=0;
		int j=num;
		while(i<j){
			int k=(i+j)/2;
			if(x[k]<t){
				i=k+1;
			}else{
				j=k;
			}
		}
		if(i>0)i--;
		double h=x[i+1]-x[i];
		double d=t-x[i];
		return (((z[i+1]-z[i])*d/h+z[i]*3)*d
				+((y[i+1]-y[i])/h-(z[i]*2+z[i+1])*h))*d+y[i];
	}

}
