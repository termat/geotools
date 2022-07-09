/*
 * Date: 2005/05/15
 *
 * written by t-matsuoka@wesco.co.jp
 *
 */
package net.termat.components.solver;

import java.util.Observable;


/**
 * <b>ソルバのスーパークラス</b><br><br>
 *
 * 計算ソルバは、本クラスを
 * 継承して実装する。<br>
 * 実際の計算処理は、solve()メソッドに記述する。<br>
 *
 * @author t.matsuoka
 * @version 0.2
 */
@SuppressWarnings("deprecation")
public abstract class Solver extends Observable{

	public static final String[] STATUSES={"計算中","停止", "完了", "中止", "エラー","待機"};
	public static enum Status{EXCUTE,PAUSED,COMPLETE,CANCELLED,ERROR,WAIT};

	public static final Status WAIT=Status.WAIT;
	public static final Status PAUSED=Status.PAUSED;
	public static final Status COMPLETE=Status.COMPLETE;
	public static final Status CANCELLED=Status.CANCELLED;
	public static final Status EXCUTE=Status.EXCUTE;
	public static final Status ERROR=Status.ERROR;

	protected Status status=Status.WAIT;
	protected int iter;
	protected int completed;
	protected long sleep=50;


	/**
	 * 計算の進捗率を取得
	 *
	 * @return 進捗率（％）
	 */
	public float getProgress(){
		return ((float)completed/(float)iter)*100;
	}

	/**
	 * 計算開始(スレッド起動)
	 *
	 */
	public void solve(){
		if(status==Solver.Status.WAIT){
			status=Solver.Status.EXCUTE;
			stateChanged();
		}else{
			return;
		}
		try{
			for(int progress=completed;progress<iter;progress++){
				if(status==Solver.Status.EXCUTE){
					calc();
					completed++;
					stateChanged();
				}else{
					if(status==Solver.Status.CANCELLED){
						completed=0;
						canceled();
						stateChanged();
						return;
					}else if(status==Solver.Status.COMPLETE){
						completed();
						stateChanged();
						break;
					}else if(status==Solver.Status.PAUSED){
						pause();
						stateChanged();
						return;
					}
				}
				if(sleep>0)Thread.sleep(sleep);
			}
			if(status==Solver.Status.EXCUTE) {
				status=Solver.Status.COMPLETE;
				completed();
				stateChanged();
			}
		}catch(Exception e){
			e.printStackTrace();
			this.error();
		}
	}

	abstract public void calc();
	abstract public void canceled();
	abstract public void completed();
	abstract public String getName();



	/**
	 * Solverの状態IDを取得
	 *
	 * @return 状態のID
	 */
	public Status getStateValue(){
		return status;
	}

	/**
	 * Solverの状態名を取得
	 *
	 * @return 状態名
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Observerへ通知
	 */
	protected void stateChanged() {
		setChanged();
		notifyObservers();
	}

	/**
	 * 停止
	 *
	 */
	public void pause() {
		status=Status.PAUSED;
		stateChanged();
	}

	/**
	 * 再開
	 *
	 */
	public void resume() {
		status=Status.WAIT;
		stateChanged();
		SolverThread.add(this);
	}

	/**
	 * 中止
	 *
	 */
	public void cancel() {
//		if(status==WAIT)SolverThread.remove(this);
		status=Status.CANCELLED;
		stateChanged();
	}

	public void execute(){
		status=Status.WAIT;
		stateChanged();
		SolverThread.add(this);
	}

	/**
	 * エラー表示
	 *
	 */
	protected void error() {
		status=Status.ERROR;
		stateChanged();
	}

	public void setState(Status i){
		this.status=i;
		stateChanged();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	public long getSleep() {
		return sleep;
	}

	public void setSleep(long sleep) {
		this.sleep = sleep;
	}

}