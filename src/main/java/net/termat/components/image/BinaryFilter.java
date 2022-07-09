package net.termat.components.image;

class BinaryFilter extends AbstractImageFilter {
	private int width;
	private int height;
	private int thWidth;
	private int thSize;
	private int threshold;

	public BinaryFilter(){
		thWidth=10;
		thSize=20;
		threshold=0;
	}

	public BinaryFilter(int th){
		thWidth=10;
		thSize=20;
		threshold=th;
	}

	public BinaryFilter(int tw,int ts){
		thWidth=tw;
		thSize=ts;
	}

	protected void filterImage() {
		if(threshold==0){
			dynamic();
		}else{
			fix();
		}
	}

	public void setDimensions(int w,int h){
		super.setDimensions(w,h);
		width=w;
		height=h;
		consumer.setDimensions(width,height);
	}

	private void fix(){
		int gray=0;
		int[] p=new int[width];
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				int[] val=getPixelRGBValue(x,y);
				if(val[1]>threshold){
					gray=255;
				}else{
					gray=0;
				}
				p[x]=(255<<24)+(gray<<16)+(gray<<8)+gray;
			}
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}


	private void dynamic(){
		int y1,y2,x1,x2;
		int prev=255;
		int gray=0;
		int[] p=new int[width];
		for(int y=0;y<height;y++){
			if(y<thSize/2){
				y1=0;
				y2=y+thSize/2;
			}else if(height-1-y<thSize/2){
				y1=y-thSize/2;
				y2=height-1;
			}else{
				y1=y-thSize/2;
				y2=y+thSize/2;
			}
			int sizeY=y2-y1+1;
			for(int x=0;x<width;x++){
				if(x<thSize/2){
					x1=0;
					x2=x+thSize/2;
				}else if(width-1-x<thSize/2){
					x1=x-thSize/2;
					x2=width-1;
				}else{
					x1=x-thSize/2;
					x2=x+thSize/2;
				}
				int sizeX=x2-x1+1;
				int average=0;
				for(int i=y1;i<=y2;i++){
					for(int j=x1;j<=x2;j++){
						int[] rgb=getPixelRGBValue(j,i);
						average +=rgb[1];
					}
				}
				average /=(double)(sizeX*sizeY);
				int th;
				if(prev==255){
					th=(int)average-thWidth;
				}else{
					th=(int)average+thWidth;
				}
				int[] val=getPixelRGBValue(x,y);
				if(val[1]<th){
					gray=0;
				}else{
					gray=255;
				}
				p[x]=(255<<24)+(gray<<16)+(gray<<8)+gray;
			}
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}
}
