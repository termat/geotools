package net.termat.components.gradient;

public class Range {
	private double min;
	private double range;
	private int step=0;
	private double[] mark;

	public Range(double min,double max){
		this.min=min;
		range=max-min;
	}

	public Range(double[] arg){
		min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		for(int i=0;i<arg.length;i++){
			if(Double.isNaN(arg[i]))continue;
			if(min>arg[i])min=arg[i];
			if(max<arg[i])max=arg[i];
		}
		range=max-min;
	}

	public Range(double[][] arg,int col){
		min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		for(int i=0;i<arg.length;i++){
			if(Double.isNaN(arg[i][col]))continue;
			if(min>arg[i][col])min=arg[i][col];
			if(max<arg[i][col])max=arg[i][col];
		}
		range=max-min;
	}

	public Range(double[][] arg,int[] col){
		min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		for(int j=0;j<col.length;j++){
			for(int i=0;i<arg.length;i++){
				if(Double.isNaN(arg[i][col[j]]))continue;
				if(min>arg[i][col[j]])min=arg[i][col[j]];
				if(max<arg[i][col[j]])max=arg[i][col[j]];
			}
		}
		range=max-min;
	}

	public boolean isEnable(){
		return (range!=0);
	}

	public void setStep(int s){
		if(s==0){
			step=0;
			mark=null;
		}else{
			step=s;
			mark=new double[step+1];
			double tmp=1.0/(double)step;
			mark[0]=0.0;
			for(int i=1;i<mark.length-1;i++){
				mark[i]=mark[i-1]+tmp;
			}
			mark[mark.length-1]=1.0;
		}
	}

	public double getValue(double arg){
		double ret=(arg-min)/range;
		if(step==0){
			if(ret<0){
				return 0.0;
			}else if(ret>1.0){
				return 1.0;
			}else{
				return ret;
			}
		}else{
			if(ret<0){
				return 0.0;
			}else if(ret>1.0){
				return 1.0;
			}else{
				return getStepVal(ret);
			}
		}
	}

	private double getStepVal(double v){
		for(int i=1;i<mark.length;i++){
			if(mark[i-1]<=v&&mark[i]>v){
				if(v-mark[i-1]>mark[i]-v){
					return mark[i];
				}else{
					return mark[i-1];
				}
			}
		}
		return v;
	}

	public double getRange() {
		return range;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min){
		double tp=this.min-min;
		this.range=range+tp;
		this.min=min;
	}

	public void setMax(double m){
		this.range=m-min;
	}

	public double getMax(){
		return min+range;
	}

	public static double[] getColmunData(double[][] data,int col){
		double[] ret=new double[data.length];
		for(int i=0;i<ret.length;i++){
			ret[i]=data[i][col];
		}
		return ret;
	}
}
