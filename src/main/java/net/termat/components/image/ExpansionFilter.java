package net.termat.components.image;

class ExpansionFilter extends AbstractImageFilter {
	private int width;
	private int height;
	private Operator op;
	private final int OFF=(255<<24)+(255<<16)+(255<<8)+255;
	private final int ON=(255<<24)+(0<<16)+(0<<8)+0;
	
	enum Operator{EXPANSION,CONTRACTION}
	public static final Operator OP_EXPANSION=Operator.EXPANSION;
	public static final Operator OP_CONTRACTION=Operator.CONTRACTION;

	public ExpansionFilter(Operator arg){
		op=arg;
	}

	public void setOperation(Operator arg){
		op=arg;
	}

	public void setDimensions(int w,int h){
		super.setDimensions(w,h);
		width=w;
		height=h;
		consumer.setDimensions(width,height);
	}
	
	protected void filterImage() {
		if(op==OP_EXPANSION){
			expantion();
		}else{
			contraction();
		}
	}

	private void expantion(){
		int[] p=new int[width];
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				if(isExpansion(x,y)){
					p[x]=ON;
				}else{
					p[x]=OFF;
				}
			}
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}

	private void contraction(){
		int[] p=new int[width];
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				if(isContraction(x,y)){
					p[x]=OFF;
				}else{
					p[x]=ON;
				}
			}
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}

	private boolean isExpansion(int x,int y){
		if(x>0&&y>0)if(getPixelRGBValue(x-1,y-1)[1]!=255)return true;
		if(y>0)if(getPixelRGBValue(x,y-1)[1]!=255)return true;
		if(x<width-1&&y>0)if(getPixelRGBValue(x+1,y-1)[1]!=255)return true;
		if(x>0)if(getPixelRGBValue(x-1,y)[1]!=255)return true;
		if(x<width-1)if(getPixelRGBValue(x+1,y)[1]!=255)return true;
		if(x>0&&y<height-1)if(getPixelRGBValue(x-1,y+1)[1]!=255)return true;
		if(y<height-1)if(getPixelRGBValue(x,y+1)[1]!=255)return true;
		if(x<width-1&&y<height-1)if(getPixelRGBValue(x+1,y+1)[1]!=255)return true;
		return false;
	}

	private boolean isContraction(int x,int y){
		if(x>0&&y>0)if(getPixelRGBValue(x-1,y-1)[1]!=0)return true;
		if(y>0)if(getPixelRGBValue(x,y-1)[1]!=0)return true;
		if(x<width-1&&y>0)if(getPixelRGBValue(x+1,y-1)[1]!=0)return true;
		if(x>0)if(getPixelRGBValue(x-1,y)[1]!=0)return true;
		if(x<width-1)if(getPixelRGBValue(x+1,y)[1]!=0)return true;
		if(x>0&&y<height-1)if(getPixelRGBValue(x-1,y+1)[1]!=0)return true;
		if(y<height-1)if(getPixelRGBValue(x,y+1)[1]!=0)return true;
		if(x<width-1&&y<height-1)if(getPixelRGBValue(x+1,y+1)[1]!=0)return true;
		return false;
	}

}
