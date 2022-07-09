package net.termat.components.gradient;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

public class GradientLabel extends JLabel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Gradient grad;
	private enum GType{XAXIS,YAXIS}
	public static final GType X_AXIS=GType.XAXIS;
	public static final GType Y_AXIS=GType.YAXIS;

	private GType type;

	public GradientLabel(Gradient g,GType t){
		super();
		type=t;
		grad=g;
	}

	@Override
	public void paintComponent(Graphics arg0) {
		super.paintComponent(arg0);
		int margine=3;
		int w=this.getWidth()-margine*2;
		int h=this.getHeight()-margine*2;
		if(type==GType.XAXIS){
			for(int i=0;i<w;i++){
				Color c=grad.getColor((double)i/(double)w);
				arg0.setColor(c);
				arg0.drawLine(i+margine, margine, i+margine, h);
			}
		}else{
			for(int i=0;i<h;i++){
				Color c=grad.getColor((double)i/(double)h);
				arg0.setColor(c);
				arg0.drawLine(margine, i+margine, w+margine, i+margine);
			}
		}
	}

	public void setGradient(Gradient g){
		grad=g;
		updateUI();
	}
}
