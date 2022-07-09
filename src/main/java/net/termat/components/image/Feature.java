package net.termat.components.image;

public class Feature {
	private int x;
	private int y;
	private int order;
	private int id;
	private boolean mark;
	public Feature(int a,int b,int c){
		x=a;
		y=b;
		order=c;
		id=0;
		mark=false;
	}

	public boolean isMark() {
		return mark;
	}

	public void setMark(boolean mark) {
		this.mark = mark;
	}

	int getOrder() {
		return order;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	void setOrder(int i) {
		order = i;
	}

	void setX(int i) {
		x = i;
	}

	void setY(int i) {
		y = i;
	}

	public int getId() {
		return id;
	}

	public void setId(int i) {
		id = i;
	}

	public boolean equals(Object arg0) {
		if(arg0 instanceof Feature){
			Feature f=(Feature)arg0;
			return (x==f.x&&y==f.y);
		}else{
			return false;
		}
	}
}
