package net.termat.geo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PCMath {

	public static Map<String,Double> getStat(double[][] data) {
		double min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		int nan=0;
		double sum=0;
		List<Double> dd=new ArrayList<>();
		for(int i=0;i<data.length;i++) {
			for(int j=0;j<data[i].length;j++) {
				if(Double.isNaN(data[i][j])) {
					nan++;
					continue;
				}
				min=Math.min(min, data[i][j]);
				max=Math.max(max, data[i][j]);
				sum +=data[i][j];
				dd.add(data[i][j]);
			}
		}
		int num=dd.size();
		double ave=sum/(double)num;
		double ss=0;
		for(Double d : dd){
			ss +=Math.pow(d-ave,2);
		}
		double std;
		if(num==1) {
			std=Math.sqrt(ss/(double)num);
		}else {
			std=Math.sqrt(ss/(double)num-1);
		}
		double median;
		Double[] da=dd.toArray(new Double[dd.size()]);
		if(da.length==0) {
			median=Double.NaN;
		}else if(da.length==1){
			median=da[0];
		}else {
			Arrays.sort(da);
			median=da[da.length/2];
		}
		Map<String,Double> ret=new HashMap<>();
		ret.put("min", min);
		ret.put("max", max);
		ret.put("ave", ave);
		ret.put("n", (double)num);
		ret.put("std", std);
		ret.put("median", median);
		ret.put("n_nan", (double)nan);
		return ret;
	}
	
	public static Map<String,Double> getStat(double[] data){
		double min=Double.MAX_VALUE;
		double max=-Double.MAX_VALUE;
		int nan=0;
		double sum=0;
		List<Double> dd=new ArrayList<>();
		for(int i=0;i<data.length;i++) {
			if(Double.isNaN(data[i])) {
				nan++;
				continue;
			}
			min=Math.min(min, data[i]);
			max=Math.max(max, data[i]);
			sum +=data[i];
			dd.add(data[i]);
		}
		int num=dd.size();
		double ave=sum/(double)num;
		double ss=0;
		for(Double d : dd){
			ss +=Math.pow(d-ave,2);
		}
		double std;
		if(num==1) {
			std=Math.sqrt(ss/(double)num);
		}else {
			std=Math.sqrt(ss/(double)num-1);
		}
		double median;
		Double[] da=dd.toArray(new Double[dd.size()]);
		if(da.length==0) {
			median=Double.NaN;
		}else if(da.length==1){
			median=da[0];
		}else {
			Arrays.sort(da);
			median=da[da.length/2];
		}
		Map<String,Double> ret=new HashMap<>();
		ret.put("min", min);
		ret.put("max", max);
		ret.put("ave", ave);
		ret.put("n", (double)num);
		ret.put("std", std);
		ret.put("median", median);
		ret.put("n_nan", (double)nan);
		return ret;
	}
}
