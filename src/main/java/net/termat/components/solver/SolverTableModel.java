/*
 * Date: 2005/05/15
 *
 * written by t-matsuoka@wesco.co.jp
 *
 */
package net.termat.components.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;


/**
 * <b>ソルバ管理パネル用TableModel</b><br><br>
 *
 * @author t.matsuoka
 * @version 0.2
 */
@SuppressWarnings("deprecation")
public class SolverTableModel extends AbstractTableModel implements Observer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] columnNames = {"Name","Progress","Status"};
	private static final Class<?>[] columnClasses = {String.class,JProgressBar.class, String.class};
	private List<Solver> list=new ArrayList<Solver>();
	private boolean isDelete=true;

	public void setCompletedDeleteAction(boolean b){
		isDelete=b;
	}

	/**
	 * ソルバを追加
	 *
	 * @param solver 追加するソルバ
	 */
	public void addSolver(Solver solver) {
		solver.addObserver(this);
		list.add(solver);
		fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
	}

	/**
	 * 登録されているソルバを取得
	 *
	 * @param row 行
	 * @return ソルバ
	 */
	public Solver getSolver(int row) {
		if(list.size()>row){
			return (Solver)list.get(row);
		}else{
			return null;
		}
	}

	/**
	 * ソルバの除去
	 *
	 * @param row 行
	 */
	public void removeSolver(int row) {
		if(row>=0){
			list.remove(row);
		    fireTableRowsDeleted(row,row);
		}
	}

	/**
	 * ソルバの除去
	 *
	 * @param row 行
	 */
	public void removeSolver(Solver s) {
		int num=list.indexOf(s);
	    list.remove(s);
	    fireTableRowsDeleted(num,num);
	}

	/* (非 Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/* (非 Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int col) {
		return columnClasses[col];
	}

	/* (非 Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {
		int index=list.indexOf(arg0);
		if(((Solver)arg0).getStateValue()==Solver.Status.COMPLETE){
			if(isDelete)this.removeSolver(index);
		}else{
			fireTableRowsUpdated(index,index);
		}
	}

	/* (非 Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
	    return columnNames.length;
	}

	/* (非 Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
	    return list.size();
	}

	/* (非 Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		try{
			Solver solver=(Solver)list.get(row);
			switch(col){
				case 0:
					return solver.getName();
				case 1:
					return new Float(solver.getProgress());
				case 2:
					return solver.getStatus();
			}
		    return "";
		}catch(IndexOutOfBoundsException ee){
		    return "";
		}
	}
}
