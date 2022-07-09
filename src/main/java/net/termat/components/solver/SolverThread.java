/*
 * Date: 2005/05/15
 *
 * written by t-matsuoka@wesco.co.jp
 *
 */
package net.termat.components.solver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * <b>ソルバ計算スレッド</b><br><br>
 *
 * 並列実行されたSolverThreadは一種のスレッドプール
 * を形成し、キュー（queue）に登録されたソルバがある
 * 場合は各スレッド毎にソルバを取得して計算を行う<br>
 *
 * @author t.matsuoka
 * @version 0.2
 */
public class SolverThread implements Runnable{
	private static List<Solver> queue=Collections.synchronizedList(new LinkedList<Solver>());
	private Thread thread;

	/**
	 * キューにソルバを登録する。
	 *
	 * @param solver ソルバ
	 */
	public static void add(Solver s){
		synchronized (queue){
			if(queue.contains(s)){
				queue.notifyAll();
			}else{
				queue.add(s);
				queue.notifyAll();
			}
	    }
	  }

	public static void remove(Solver s){
		synchronized (queue){
			queue.remove(s);
			queue.notifyAll();
	    }
	}

	/**
	 * スレッドを起動
	 *
	 */
	public void start(){
		thread=new Thread(this);
		thread.start();
	}

	/**
	 * スレッドを停止
	 *
	 */
	public void stop(){
		thread=null;
	}

	/* (非 Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(thread!=null){
			Solver solver=null;
			synchronized(queue){
				while(queue.isEmpty()){
					try{
						queue.wait();
					}catch (InterruptedException e){}
				}
				if(thread!=null){
					solver=(Solver)queue.get(0);
					queue.remove(0);
				}
			}
			try{
				if(solver!=null){
					solver.solve();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}