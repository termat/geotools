package net.termat.components.image;

class DensityConversionFilter extends AbstractImageFilter {
	private int width;
	private int height;
	private int[] min;
	private int[] max;
	enum Operator {LINER,NONLINER}
	public static final Operator OP_LINER=Operator.LINER;
	public static final Operator OP_NONLINER=Operator.NONLINER;
	private Operator op;
	
	public DensityConversionFilter(Operator i){
		op=i;
	}
	
	protected void filterImage() {
		switch(op){
			case LINER:
				liner();
				break;
			case NONLINER:
				nonliner();
			default:
		}
	}

	public void setDimensions(int w,int h){
		super.setDimensions(w,h);
		width=w;
		height=h;
		consumer.setDimensions(width,height);
	}

	private void nonliner(){
		int[][] hist=getHistgram();
		int mean=width*height/256;
		int[][][] move=new int[3][256][256];
		for(int k=0;k<3;k++){
			for(int i=0;i<256;i++){
				for(int j=0;j<256;j++){
					move[k][i][j]=0;
					move[k][i][i]=hist[k][i];
				}
			}
		}
		for(int k=0;k<3;k++){
			for(int i=0;i<256;i++){
				if(hist[k][i]>mean){
					int ss=move[k][0][k];
					for(int j=0;j<=i;j++){
						if(ss<mean){
							ss +=move[k][j+1][i];
						}else{
							move[k][j][i+1]=ss-mean;
							move[k][j][i]=move[k][j][i]-move[k][j][i+1];
							hist[k][i] -=move[k][j][i+1];
							hist[k][i+1] +=move[k][j][i+1];
							if(i!=j){
								for(int l=j+1;l<=i;l++){
									move[k][l][i+1]=move[k][l][i];
									hist[k][i+1] +=move[k][l][i+1];
									hist[k][i] -=move[k][l][i+1];
									move[k][l][i]=0;
								}
							}
							break;
						}
					}
				}else{
					int ss=hist[k][i];
					for(int j=i;j<256;j++){
						ss +=hist[k][j];
						if(ss<=mean){
							move[k][j][i]=hist[k][i];
							hist[k][i]=hist[k][i]+move[k][j][i];
							move[k][j][j]=0;
							hist[k][j]=0;
						}else{
							move[k][j][i]=mean-hist[k][i];
							hist[k][i]=hist[k][i]+move[k][j][i];
							move[k][j][j]=move[k][j][j]-move[k][j][i];
							hist[k][j]=hist[k][j]-move[k][j][i];
							break;
						}
					}
				}
			}
		}
		int[][][] cnt=new int[3][width][height];
		for(int k=0;k<3;k++)for(int i=0;i<256;i++)for(int j=0;j<256;j++)cnt[k][i][j]=0;
		for(int y=0;y<height;y++){
			int[] p=new int[width];
			for(int x=0;x<width;x++){
				int[] pp=getPixelRGBValue(x,y);
				for(int k=0;k<3;k++){
					int ix=0;
					for(ix=0;ix<256;ix++){
						if(cnt[k][pp[k+1]][ix]<move[k][pp[k+1]][ix])break;
					}
					cnt[k][pp[k+1]][ix]++;
					pp[k+1]=ix;
				}
				p[x]=(pp[0]<<24)+(pp[1]<<16)+(pp[2]<<8)+pp[3];
			}
			consumer.setPixels(0,y,width,1,colorModel,p,0,width);
		}
	}

	private void liner(){
		int[] pp=new int[width];
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				int[] p=getPixelRGBValue(x,y);
				for(int i=0;i<min.length;i++){
					p[i+1]=(int)(((double)p[i+1]-(double)min[i])/((double)max[i]-(double)min[i])*(double)255);
					if(p[i+1]>255)p[i+1]=255;
					if(p[i+1]<0)p[i+1]=0;
				}
				pp[x]=(p[0]<<24)+(p[1]<<16)+(p[2]<<8)+p[3];
			}
			consumer.setPixels(0,y,width,1,colorModel,pp,0,width);
		}
	}

	private int[][] getHistgram(){
		int[][] ret=new int[3][256];
		min=new int[3];
		max=new int[3];
		for(int i=0;i<min.length;i++){
			min[i]=255;
			max[i]=0;
		}
		int[] tmp=new int[3];
		for(int i=0;i<pixel.length;i++){
			tmp[0]=(pixel[i]>>16&0xff);
			tmp[1]=(pixel[i]>>8&0xff);
			tmp[2]=(pixel[i]&0xff);
			ret[0][tmp[0]]++;
			ret[1][tmp[1]]++;
			ret[2][tmp[2]]++;
			for(int j=0;j<tmp.length;j++){
				if(min[j]>tmp[j])min[j]=tmp[j];
				if(max[j]<tmp[j])max[j]=tmp[j];
			}
		}
		return ret;
	}

}
