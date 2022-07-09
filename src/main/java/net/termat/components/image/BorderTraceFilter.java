package net.termat.components.image;

import java.awt.Color;

class BorderTraceFilter extends AbstractImageFilter {
	private int width;
	private int height;
	private int back;
	
	public BorderTraceFilter(Color back){
		this.back=(back.getRGB()<<8);
	}

	public void setDimensions(int w,int h){
		super.setDimensions(w,h);
		width=w;
		height=h;
		consumer.setDimensions(width,height);
	}

	protected void filterImage() {
		int[][] p=createBinary();
		int[][] b=new int[p.length][p[0].length];
		b=border_trace(p,b);
		int[] px=new int[width];
		int backcolor=(back>>8);
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				if(b[x][y]==0){
					px[x]=(255<<24)+backcolor;
				}else{
					px[x]=getPixelValue(x,y);
				}
			}
			consumer.setPixels(0,y,width,1,colorModel,px,0,width);
		}
	}

	private int[][] border_trace(int[][] p,int[][] b){
		int code=0;
		for(int y=1;y<height-1;y++){
			for(int x=1;x<width-1;x++){
 				if(p[x][y]==1&&b[x][y]==0){
					if(p[x-1][y]==0){
						code=0;
						trace(x,y,code,p,b);
					}else if(p[x+1][y]==0){
						code=4;
						trace(x,y,code,p,b);
					}
				}
			}
		}
		return b;
	}

	private void trace(int x,int y,int code,int[][] p,int[][] b){
		if(!check(x,y,p))return;
		int xs=x;
		int ys=y;
		int x1=x;
		int x2=0;
		int y1=y;
		int y2=0;
		while(x2!=xs||y2!=ys){
			switch(code){
				case 0:
					x2=x1;
					y2=y1+1;
					if(y2<height&&p[x2][y2]==1){
						code=6;
					}else{
						code=2;
					}
					break;
				case 2:
					x2=x1+1;
					y2=y1;
					if(x2<width&&p[x2][y2]==1){
						code=0;
					}else{
						code=4;
					}
					break;
				case 4:
					x2=x1;
					y2=y1-1;
					if(y2>=0&&p[x2][y2]==1){
						code=2;
					}else{
						code=6;
					}
					break;
				case 6:
					x2=x1-1;
					y2=y1;
					if(x2>=0&&p[x2][y2]==1){
						code=4;
					}else{
						code=0;
					}
					break;
			}
			if(x2>=0&&x2<width&&y2>=0&&y2<height){
				if(p[x2][y2]==1){
					b[x2][y2]=1;
					x1=x2;
					y1=y2;
				}
			}
		}
	}

	private int[][] createBinary(){
		int[][] ret=new int[width][height];
		int p;
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				p=(getPixelValue(x,y)<<8);
				if(p==back){
					ret[x][y]=0;
				}else{
					ret[x][y]=1;
				}
			}
		}
		return ret;
	}

	private boolean check(int x,int y,int[][] p){
		if(p[x-1][y]==0&&p[x+1][y]==0&&p[x][y-1]==0&&p[x][y+1]==0){
			return false;
		}else{
			return true;
		}
	}
}
