package net.termat.geo.util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class PCFilter {
	private static final double[][] GALCIAN_3={
			{1,2,1},
			{2,4,2},
			{1,2.1}};

	private static final double[][] GALCIAN_5={
			{1,4,6,4,1},
			{4,16,24,16,4},
			{6,24,36,24,6},
			{4,16,24,16,4},
			{1,4,6,4,1}};

	private static final double[][] AVE_3={
			{1, 1, 1},
			{1, 1, 1},
			{1, 1, 1}};

	private static final double[][] AVE_5={
			{1,1,1,1,1},
			{1,1,1,1,1},
			{1,1,1,1,1},
			{1,1,1,1,1},
			{1,1,1,1,1}};


	public static BufferedImage galcian3(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=filter(data,GALCIAN_3);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] galcian3(double[][] img){
		return filter(img,GALCIAN_3);
	}

	public static BufferedImage galcian5(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=filter(data,GALCIAN_5);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] galcian5(double[][] img){
		return filter(img,GALCIAN_5);
	}

	public static BufferedImage ave3(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=filter(data,AVE_3);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] ave3(double[][] img){
		return filter(img,AVE_3);
	}

	public static BufferedImage ave5(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=filter(data,AVE_5);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] ave5(double[][] img){
		return filter(img,AVE_5);
	}

	public static BufferedImage median3(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=median(data,3);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] median3(double[][] data){
		return median(data,3);
	}

	public static BufferedImage median5(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=median(data,5);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] median5(double[][] data){
		return median(data,5);
	}

	public static double[][] min3(double[][] data){
		return min(data,3);
	}

	public static BufferedImage min3(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=min(data,3);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] min5(double[][] data){
		return min(data,5);
	}

	public static BufferedImage min5(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=min(data,5);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] max3(double[][] data){
		return max(data,3);
	}

	public static BufferedImage max3(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=max(data,3);
		return PCUtil.createElevationPng(data);
	}

	public static double[][] max5(double[][] data){
		return max(data,5);
	}

	public static BufferedImage max5(BufferedImage img){
		double[][] data=PCUtil.getTableNpy(img);
		data=max(data,5);
		return PCUtil.createElevationPng(data);
	}

	private static double[][] median(double[][] data,int col){

		double[][] ans=new double[data.length][data[0].length];
		int cp=(int)Math.floor(col/2.0);
		for(int i=cp;i<data.length-cp;i++){
			for(int j=cp;j<data[i].length-cp;j++){
				double[] px=new double[col*col];
				int ct=0;
				for(int k=-cp;k<=cp;k++){
					for(int l=-1;l<=1;l++){
						if (Double.isNaN(data[i+k][j+l]))continue;
						px[ct++]=data[i+k][j+l];
					}
				}
				if(ct==0){
					ans[i][j]=Double.NaN;
				}else{
					Arrays.sort(px);
					int nn=(int)Math.floor(ct/2.0);
					ans[i][j]=px[nn];
				}
			}
		}
		for(int i=1;i<data.length-1;i++){
			for(int j=1;j<data[i].length-1;j++){
				data[i][j]=ans[i][j];
			}
		}
		return data;
	}

	private static void fill(double[][] p,double val){
		for(int i=0;i<p.length;i++){
			for(int j=0;j<p[i].length;j++){
				p[i][j]=val;
			}
		}
	}

	private static double[][] min(double[][] data,int col){
		double[][] ans=new double[data.length][data[0].length];
		fill(ans,Double.NaN);
		int cp=(int)Math.floor(col/2.0);
		for(int i=cp;i<data.length-cp;i++){
			for(int j=cp;j<data[i].length-cp;j++){
				double min=Double.MAX_VALUE;
				for(int k=-cp;k<=cp;k++){
					for(int l=-1;l<=1;l++){
						if (Double.isNaN(data[i+k][j+l]))continue;
						min=Math.min(min, data[i+k][j+l]);
					}
				}
				if(min!=Double.MAX_VALUE){
					ans[i][j]=min;
				}else{
					ans[i][j]=Double.NaN;
				}
			}
		}
		for(int i=0;i<data.length-0;i++){
			for(int j=0;j<data[i].length-0;j++){
				if(ans[i][j]!=Double.NaN){
					data[i][j]=ans[i][j];
				}
			}
		}
		return data;
	}

	private static double[][] max(double[][] data,int col){
		double[][] ans=new double[data.length][data[0].length];
		int cp=(int)Math.floor(col/2.0);
		for(int i=cp;i<data.length-cp;i++){
			for(int j=cp;j<data[i].length-cp;j++){
				double max=-Double.MAX_VALUE;
				for(int k=-cp;k<=cp;k++){
					for(int l=-1;l<=1;l++){
						if (Double.isNaN(data[i+k][j+l]))continue;
						max=Math.max(max, data[i+k][j+l]);
					}
				}
				if(max!=Double.MAX_VALUE){
					ans[i][j]=max;
				}else{
					ans[i][j]=Double.NaN;
				}
			}
		}
		for(int i=0;i<data.length-0;i++){
			for(int j=0;j<data[i].length-0;j++){
				if(ans[i][j]!=Double.MAX_VALUE){
					data[i][j]=ans[i][j];
				}
			}
		}
		return data;
	}

	private static double[][] filter(double[][] data,double[][] filter){
		double[][] ans=new double[data.length][data[0].length];
		int col=(int)Math.floor(filter.length/2.0);
		for(int i=col;i<data.length-col;i++){
			for(int j=col;j<data[i].length-col;j++){
				double[][] tmp=new double[filter.length][filter.length];
				for(int k=-col;k<=col;k++){
					for(int l=-col;l<=col;l++){
						tmp[k+col][l+col]=data[i+k][j+l];
					}
				}
				ans[i][j]=calFilter(tmp,filter);
			}
		}
		for(int i=col;i<data.length-col;i++){
			for(int j=col;j<data[i].length-col;j++){
				data[i][j]=ans[i][j];
			}
		}
		return data;
	}


	private static double calFilter(double[][] val,double[][] op){
		double ret=0;
		double opv=0;
		int col=(int)Math.floor(op.length/2.0);
		if (Double.isNaN(val[col][col]))return Double.NaN;
		for(int i=0;i<op.length;i++){
			for(int j=0;j<op[i].length;j++){
				if(Double.isNaN(val[i][j]))continue;
				ret +=val[i][j]*op[i][j];
				opv +=op[i][j];
			}
		}
		if(opv==0){
			return Double.NaN;
		}else{
			return ret/opv;
		}
	}
}
