package net.termat.components.gradient;

import java.awt.Color;

public class LandAndSeaGradient implements Gradient {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Color[] SEA=new Color[]{new Color(3,12,129),Color.BLUE,new Color(55,171,232)};
	private static final Color[] LAND=new Color[]{Color.GREEN,new Color(187,135,12),Color.WHITE};
	private Color nanColor=Color.BLACK;
	private Gradient land=GradientFactory.createGradient(LAND);
	private Gradient sea=GradientFactory.createGradient(SEA);
	private double zero=0.0;

	public LandAndSeaGradient(){}

	public void setMinMax(double min,double max){
		zero=(0-min)/(max-min);
	}

	@Override
	public Color getColor(double arg) {
		if(arg-zero<0){
			double t=arg/zero;
			return sea.getColor(t);
		}else{
			double t=(arg-zero)/(1.0-zero);
			return land.getColor(t);
		}
	}

	@Override
	public float[] getColorByFloat(double arg) {
		if(arg-zero<0){
			double t=arg/zero;
			return sea.getColorByFloat(t);
		}else{
			double t=(arg-zero)/(1.0-zero);
			return land.getColorByFloat(t);
		}
	}

	@Override
	public int getColorByInt(double arg) {
		if(arg-zero<0){
			double t=arg/zero;
			return sea.getColorByInt(t);
		}else{
			double t=(arg-zero)/(1.0-zero);
			return land.getColorByInt(t);
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
