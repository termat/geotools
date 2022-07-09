package net.termat.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class JCloseButtonTabbedPane extends JTabbedPane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected final Icon btIcon=new CloseTabIcon();
	protected final Dimension buttonSize;
	protected JMenuItem button;

	public JCloseButtonTabbedPane() {
		super();
		buttonSize = new Dimension(btIcon.getIconWidth(), btIcon.getIconHeight());
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
	}
	
	@Override
	public String getTitleAt(int arg0) {
		JPanel p=(JPanel)this.getTabComponentAt(arg0);
		if(p==null){
			return super.getTitleAt(arg0);
		}else{
			JLabel l=(JLabel)p.getComponent(0);
			return l.getText();
		}
	}

	private void addTab2(String title,Icon icon,final Component comp){
		JPanel tab = new JPanel(new BorderLayout());
		tab.setOpaque(false);
		JLabel label = new JLabel(title);
		JLabel image = new JLabel(icon);
		label.setFont(getFont());
		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
		tab.add(label,  BorderLayout.CENTER);
		tab.add(image,  BorderLayout.WEST);
		tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
		super.addTab(null, comp);
		setTabComponentAt(getTabCount()-1, tab);
	}

	public void addTab(String title, final Component comp,boolean closeble) {
		if(closeble){
			JPanel tab = new JPanel(new BorderLayout());
			tab.setOpaque(false);
			JLabel label = new JLabel(title);
			label.setFont(getFont());
			label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
			button=new JMenuItem(btIcon);
			button.setPreferredSize(buttonSize);
			button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTabAt(indexOfComponent(comp));
			}
			});
			tab.add(label,  BorderLayout.WEST);
			tab.add(button, BorderLayout.EAST);
			tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
			super.addTab(null, comp);
			setTabComponentAt(getTabCount()-1, tab);
		}else{
			super.addTab(title, comp);
		}
	}

	public void addTab(String title,Icon icon,final Component comp,boolean closeble) {
		if(closeble){
			JPanel tab = new JPanel(new BorderLayout());
			tab.setOpaque(false);
			JLabel label = new JLabel(title);
			JLabel image = new JLabel(icon);
			label.setFont(getFont());
			label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
			button=new JMenuItem(btIcon);
			button.setPreferredSize(buttonSize);
			button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTabAt(indexOfComponent(comp));
			}
			});
			tab.add(label,  BorderLayout.CENTER);
			tab.add(image,  BorderLayout.WEST);
			tab.add(button, BorderLayout.EAST);
			tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
			super.addTab(null, comp);
			setTabComponentAt(getTabCount()-1, tab);
		}else{
			addTab2(title,icon,comp);
		}
	}

	public void addTab(String title, Icon icon,final Component comp,boolean closeble,ActionListener ac) {
		if(closeble){
			JPanel tab = new JPanel(new BorderLayout());
			tab.setOpaque(false);
			JLabel label = new JLabel(title);
			JLabel image = new JLabel(icon);
			label.setFont(getFont());
			label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
			button=new JMenuItem(btIcon);
			button.setPreferredSize(buttonSize);
			button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTabAt(indexOfComponent(comp));
			}
			});
			button.addActionListener(ac);
			tab.add(label,  BorderLayout.CENTER);
			tab.add(image,  BorderLayout.WEST);
			tab.add(button, BorderLayout.EAST);
			tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
			super.addTab(null, comp);
			setTabComponentAt(getTabCount()-1, tab);
		}else{
			addTab2(title,icon,comp);
		}
	}

	public void addTab(String title, Icon icon,final Component comp,ActionListener buttonAction) {
		JPanel tab = new JPanel(new BorderLayout());
		tab.setOpaque(false);
		JLabel label = new JLabel(title);
		JLabel image = new JLabel(icon);
		label.setFont(getFont());
		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
		button=new JMenuItem(btIcon);
		button.setPreferredSize(buttonSize);
		button.addActionListener(buttonAction);
		tab.add(label,  BorderLayout.CENTER);
		tab.add(image,  BorderLayout.WEST);
		tab.add(button, BorderLayout.EAST);
		tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
		super.addTab(null, comp);
		setTabComponentAt(getTabCount()-1, tab);
	}

	public void addTab(String title, final Component comp,boolean closeble,ActionListener ac) {
		if(closeble){
			JPanel tab = new JPanel(new BorderLayout());
			tab.setOpaque(false);
			JLabel label = new JLabel(title);
			label.setFont(getFont());
			label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
			button=new JMenuItem(btIcon);
			button.setPreferredSize(buttonSize);
			button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTabAt(indexOfComponent(comp));
			}
			});
			button.addActionListener(ac);
			tab.add(label,  BorderLayout.WEST);
			tab.add(button, BorderLayout.EAST);
			tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
			super.addTab(null, comp);
			setTabComponentAt(getTabCount()-1, tab);
		}else{
			super.addTab(title, comp);
		}
	}

	public void addTab(String title, final Component comp,ActionListener buttonAction) {
		JPanel tab = new JPanel(new BorderLayout());
		tab.setOpaque(false);
		JLabel label = new JLabel(title);
		label.setFont(getFont());
		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
		button=new JMenuItem(btIcon);
		button.setPreferredSize(buttonSize);
		button.addActionListener(buttonAction);
		tab.add(label,  BorderLayout.WEST);
		tab.add(button, BorderLayout.EAST);
		tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
		super.addTab(null, comp);
		setTabComponentAt(getTabCount()-1, tab);
	}

	public JMenuItem getCloseButton(){
		return button;
	}

	public int findTabId(String str){
		for(int i=0;i<this.getTabCount();i++){
			if(this.getTitleAt(i).equals(str))return i;
		}
		return -1;
	}

	public void removeTab(String name){
		for(int i=0;i<this.getTabCount();i++){
			if(this.getTitleAt(i).equals(name)){
				this.removeTabAt(i);
				return;
			}
		}
	}

	private static class CloseTabIcon implements Icon {
		private int x_pos;
		private int y_pos;
		private int width;
		private int height;

		public CloseTabIcon() {
			width =16;
			height=16;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			this.x_pos=x;
			this.y_pos=y;
			g.setColor(Color.LIGHT_GRAY);
			g.clearRect(0, 0, 16, 16);
			g.setColor(Color.GRAY);
			g.drawLine(4,  4, 11, 11);
			g.drawLine(4,  5, 10, 11);
			g.drawLine(5,  4, 11, 10);
			g.drawLine(11, 4,  4, 11);
			g.drawLine(11, 5,  5, 11);
			g.drawLine(10, 4,  4, 10);
		}

		public int getIconWidth() {
			return width;
		}

		public int getIconHeight() {
			return height;
		}

		@SuppressWarnings("unused")
		public Rectangle getBounds() {
			return new Rectangle(x_pos, y_pos, width, height);
		}
	}
}
