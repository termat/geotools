/*
 * Date: 2005/05/15
 *
 * written by t-matsuoka@wesco.co.jp
 *
 */
package net.termat.components.solver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * <b>ソルバ管理パネル</b><br><br>
 *
 * ソルバの実行状態を管理・表示するパネル<br>
 *
 * @author t.matsuoka
 * @version 0.2
 */
@SuppressWarnings("deprecation")
public class SolverTable extends JPanel implements Observer{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected SolverTableModel model;
	protected Solver selectedSolver;
	private boolean clearing;
	protected JTable table;
	private JButton pauseButton, resumeButton;
	private JButton cancelButton, clearButton,execButton;
	private JPanel under;
	private ButtonType type;
	public enum ButtonType{PRCC,PRCCE,PRCE}

	public SolverTable(){
		this(ButtonType.PRCC);
	}

	public SolverTable(ButtonType t) {
		super();
		type=t;
		this.setLayout(new BorderLayout());
		model=new SolverTableModel();
		table=new JTable(model);
		table.getSelectionModel().addListSelectionListener(new
				ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						tableSelectionChanged();
					}
		});
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ProgressRenderer renderer = new ProgressRenderer(0,100);
		renderer.setStringPainted(true);
		table.setDefaultRenderer(JProgressBar.class, renderer);
//		table.setRowHeight((int) renderer.getPreferredSize().getHeight());
		table.setRowHeight(24);
		JScrollPane jsp=new JScrollPane(table);
		this.add(jsp,BorderLayout.CENTER);
		under=new JPanel();
		under.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		BoxLayout b=new BoxLayout(under,BoxLayout.X_AXIS);
		under.setLayout(b);
		this.add(under,BorderLayout.SOUTH);
		if(type==ButtonType.PRCC){
			initPRCC();
		}else if(type==ButtonType.PRCCE){
			initPRCCE();
		}else{
			initPRCE();
		}
		SolverThread st=new SolverThread();
		st.start();
	}

	public void setTableFont(Font f){
		super.setFont(f);
		if(table!=null)table.setFont(f);
	}

	private void initPRCC(){
	    pauseButton = new JButton("Pause");
	    pauseButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionPause();
	      }
	    });
	    pauseButton.setEnabled(false);
	    under.add(pauseButton);
	    resumeButton = new JButton("Resume");
	    resumeButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionResume();
	      }
	    });
	    resumeButton.setEnabled(false);
	    under.add(resumeButton);
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionCancel();
	      }
	    });
	    cancelButton.setEnabled(false);
	    under.add(cancelButton);
	    clearButton = new JButton("Clear");
	    clearButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionClear();
	      }
	    });
	    clearButton.setEnabled(false);
	    under.add(clearButton);
	}

	private void initPRCCE(){
	    pauseButton = new JButton("Pause");
	    pauseButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionPause();
	      }
	    });
	    pauseButton.setEnabled(false);
	    under.add(pauseButton);
	    resumeButton = new JButton("Resume");
	    resumeButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionResume();
	      }
	    });
	    resumeButton.setEnabled(false);
	    under.add(resumeButton);
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionCancel();
	      }
	    });
	    cancelButton.setEnabled(false);
	    under.add(cancelButton);
	    clearButton = new JButton("Clear");
	    clearButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionClear();
	      }
	    });
	    clearButton.setEnabled(false);
	    under.add(clearButton);
	    execButton = new JButton("Exec");
	    execButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionExec();
	      }
	    });
	    execButton.setEnabled(false);
	    under.add(execButton);
	}

	private void initPRCE(){
	    pauseButton = new JButton("Pause");
	    pauseButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionPause();
	      }
	    });
	    pauseButton.setEnabled(false);
	    under.add(pauseButton);
	    resumeButton = new JButton("Resume");
	    resumeButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionResume();
	      }
	    });
	    resumeButton.setEnabled(false);
	    under.add(resumeButton);
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionCancel();
	      }
	    });
	    cancelButton.setEnabled(false);
	    under.add(cancelButton);
	    execButton = new JButton("Exec");
	    execButton.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        actionExec();
	      }
	    });
	    execButton.setEnabled(false);
	    under.add(execButton);
	}

	public JTable getTable(){
		return table;
	}

	public void setButtonFont(Font f){
		pauseButton.setFont(f);
		resumeButton.setFont(f);
		cancelButton.setFont(f);
		clearButton.setFont(f);
	}

	public void setTableHeaderFont(Font f){
		table.getTableHeader().setFont(f);
		table.setFont(f);
	}

	private void tableSelectionChanged() {
		if (selectedSolver!=null)selectedSolver.deleteObserver(this);
	    if (!clearing) {
	    	int it=table.getSelectedRow();
	    	if(it<0)it=0;
	      selectedSolver=model.getSolver(it);
	      if(selectedSolver!=null){
	    	  selectedSolver.addObserver(this);
		      updateButtons();
	      }
	    }
	  }

	  protected void updateButtons() {
	    if (selectedSolver != null) {
	      Solver.Status status = selectedSolver.getStateValue();
	      switch(status){
	        case EXCUTE:
	          pauseButton.setEnabled(true);
	          resumeButton.setEnabled(false);
	          cancelButton.setEnabled(true);
	          if(clearButton!=null)clearButton.setEnabled(false);
	          if(execButton!=null)execButton.setEnabled(false);
	          break;
	        case PAUSED:
	          pauseButton.setEnabled(false);
	          resumeButton.setEnabled(true);
	          cancelButton.setEnabled(true);
	          if(clearButton!=null)clearButton.setEnabled(false);
	          break;
	        case ERROR:
	          pauseButton.setEnabled(false);
	          resumeButton.setEnabled(true);
	          cancelButton.setEnabled(false);
	          if(clearButton!=null)clearButton.setEnabled(true);
	          break;
	        case COMPLETE:
	        	pauseButton.setEnabled(false);
	        	resumeButton.setEnabled(false);
	        	cancelButton.setEnabled(false);
	        	if(clearButton!=null)clearButton.setEnabled(true);
	        	if(execButton!=null)execButton.setEnabled(false);
	        default:
	        	pauseButton.setEnabled(false);
	        	resumeButton.setEnabled(false);
	        	cancelButton.setEnabled(false);
	        	if(clearButton!=null)clearButton.setEnabled(true);
	        	if(execButton!=null)execButton.setEnabled(true);
	      }
	    } else {
	      pauseButton.setEnabled(false);
	      resumeButton.setEnabled(false);
	      cancelButton.setEnabled(false);
	      if(clearButton!=null)clearButton.setEnabled(false);
          if(execButton!=null)execButton.setEnabled(false);
	    }
	  }

	/* (非 Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {
	    if (selectedSolver!=null&& selectedSolver.equals(arg0)){
	        updateButtons();
	    }
	}

	private void actionPause() {
		selectedSolver.pause();
		updateButtons();
	}

	private void actionExec() {
		selectedSolver.execute();
		updateButtons();
	}

	private void actionResume() {
		selectedSolver.resume();
		updateButtons();
	}

	// 選択ダウンロードを中止する
	private void actionCancel() {
		selectedSolver.cancel();
		updateButtons();
	}

	// 選択ダウンロードをクリアする
	private void actionClear() {
		clearing = true;
		int row=table.getSelectedRow();
		if(row>=0){
			model.removeSolver(table.getSelectedRow());
			clearing = false;
			selectedSolver=null;
			updateButtons();
		}
	 }

	public void removeSolver(Solver s){
		clearing = true;
		model.removeSolver(s);
		clearing = false;
		selectedSolver=null;
		updateButtons();
	}

	public void addSolver(Solver s,boolean run){
		model.addSolver(s);
		updateButtons();
		if(run)SolverThread.add(s);
	}

	public void clear(){
		this.clearing=true;
		selectedSolver=null;
		int num=table.getRowCount();
		for(int i=num-1;0<=i;i--){
			model.removeSolver(i);
		}
		this.clearing=false;
		updateButtons();
	}

	public SolverTableModel getModel(){
		return model;
	}

	public boolean isAllSolverCompleted(){
		int row=model.getRowCount();
		if(row==0)return false;
		for(int i=0;i<row;i++){
			Solver sol=model.getSolver(i);
			if(sol.getStateValue()!=Solver.Status.COMPLETE)return false;
		}
		return true;
	}

	public int getSelectedRow(){
		return table.getSelectedRow();
	}

	public int getSelectedColumn(){
		return table.getSelectedColumn();
	}

}
