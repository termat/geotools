package net.termat.components.gradient;

import java.awt.Color;

public interface Gradient extends java.io.Serializable{
	public Color getColor(double arg);
	public float[] getColorByFloat(double arg);
	public int getColorByInt(double arg);
	public void setNanColor(Color c);
	public Color getNanColor();
}
