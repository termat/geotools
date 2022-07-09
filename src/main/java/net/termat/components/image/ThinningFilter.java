package net.termat.components.image;

import java.awt.Color;

class ThinningFilter extends AbstractImageFilter {
	private int width;
	private int height;
	private int back;

	public ThinningFilter(Color back){
		this.back=(back.getRGB()<<8);
	}

	public void setDimensions(int w,int h){
		super.setDimensions(w,h);
		width=w;
		height=h;
		consumer.setDimensions(width,height);
	}

	protected void filterImage() {
		thnning();
	}

	private void thnning(){
		int[][] main=createBinary();
		int[][] sub=createBinary();
		int[] aa=new int[9];
		int[] bb=new int[9];
		int rev=1;
		while(rev!=0){
			rev=0;
			for(int y=1;y<height-1;y++){
				for(int x=1;x<width-1;x++){
					if(main[x][y]==0)continue;
					aa[0]=main[x+1][y];		bb[0]=sub[x+1][y];
					aa[1]=main[x+1][y-1];	bb[1]=sub[x+1][y-1];
					aa[2]=main[x][y-1];		bb[2]=sub[x][y-1];
					aa[3]=main[x-1][y-1];	bb[3]=sub[x-1][y-1];
					aa[4]=main[x-1][y];		bb[4]=sub[x-1][y];
					aa[5]=main[x-1][y+1];	bb[5]=sub[x-1][y+1];
					aa[6]=main[x][y+1];		bb[6]=sub[x][y+1];
					aa[7]=main[x+1][y+1];	bb[7]=sub[x+1][y+1];
					int sum=0;
					for(int i=0;i<8;i++) sum +=aa[i];
					if(sum==0)sub[x][y]=0;
					if(sum>=2&&sum<=5){	//sum>=6はヒゲや孔が生じやすい
						if(connect(aa)==1&&connect(bb)==1){
							for(int j=1;j<5;j++){
								if(bb[j]==0){
									int c=aa[j];
									aa[j]=0;
									if(connect(aa)!=1){
										aa[j]=c;
										break;
									}
									aa[j]=c;
								}
								sub[x][y]=0;
							}
						}
					}
					if(sub[x][y]==0)rev++;
				}
			}
			for(int y=0;y<height;y++){
				for(int x=0;x<width;x++){
					main[x][y]=sub[x][y];
				}
			}
		}
		int backcolor=(back>>8);
		for(int y=0;y<height;y++){
			int[] p=new int[width];
			for(int x=0;x<width;x++){
				if(main[x][y]==0){
					p[x]=(255<<24)+backcolor;
				}else{
					p[x]=getPixelValue(x,y);
				}
			}
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}

	private int connect(int[] val){
		val[8]=val[0];
		int num=0;
		for(int i=1;i<val.length;i++){
			if(val[i]==1&&val[i-1]==0)num++;
		}
		return num;
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

}
