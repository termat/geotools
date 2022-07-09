package net.termat.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;


public class MyTableCellEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;
	private Component comp;
	private Object val;

	public MyTableCellEditor(){
		super(new JTextField());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getCellEditorValue() {
		 if(val instanceof Number){
			try{
				if(val instanceof Integer){
					return Integer.parseInt(super.getCellEditorValue().toString());
				}else if(val instanceof Float){
					return Float.parseFloat(super.getCellEditorValue().toString());
				}else if(val instanceof Double){
					return Double.parseDouble(super.getCellEditorValue().toString());
				}else if(val instanceof Long){
					return Long.parseLong(super.getCellEditorValue().toString());
				}else{
					return Double.parseDouble(super.getCellEditorValue().toString());
				}
			}catch(NumberFormatException ne){
				JOptionPane.showMessageDialog(comp,"NumberFormatException","",JOptionPane.ERROR_MESSAGE);
				return val;
			}
		}else{
			if(comp instanceof JCheckBox){
				if(((JCheckBox)comp).isSelected()){
					return Boolean.TRUE;
				}else{
					return Boolean.FALSE;
				}
			}else if(comp instanceof JTextField){
				return ((JTextField)comp).getText();
			}else if(comp instanceof JRadioButton){
				if(((JRadioButton)comp).isSelected()){
					((JRadioButton)val).setSelected(true);
					return val;
				}else{
					((JRadioButton)val).setSelected(false);
					return val;
				}
			}else if(comp instanceof JComboBox){
				return (JComboBox)comp;
			}else if(comp instanceof JSpinner){
				return (JSpinner)val;
			}else if(comp instanceof JButton){
				return (JButton)comp;
			}else if(comp instanceof JLabel){
				return (JLabel)comp;
			}else{
				return super.getCellEditorValue();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if(value instanceof Boolean){
			JCheckBox b=new JCheckBox();
			b.setBackground(Color.WHITE);
			b.setSelected(((Boolean)value).booleanValue());
			comp=b;
			val=value;
			return comp;
		}else if(value instanceof JButton){
			JButton tmp=new JButton(((JButton)value).getText());
			ActionListener[] ac=((JButton)value).getActionListeners();
			for(int i=0;i<ac.length;i++){
				tmp.addActionListener(ac[i]);
			}
			comp=tmp;
			val=value;
			return comp;
		}else if(value instanceof JComboBox){
			JComboBox tmp=new JComboBox(((JComboBox)value).getModel());
			ActionListener[] ac=((JComboBox)value).getActionListeners();
			for(int i=0;i<ac.length;i++){
				tmp.addActionListener(ac[i]);
			}
			comp=tmp;
			val=value;
			return comp;
		}else if(value instanceof JRadioButton){
			JRadioButton vv=(JRadioButton)value;
			JRadioButton tmp=new JRadioButton(vv.getText(),vv.isSelected());
			comp=tmp;
			val=value;
			return comp;
		}else if(value instanceof JSpinner){
			JSpinner tmp=new JSpinner(((JSpinner)value).getModel());
			ChangeListener[] ac=((JSpinner)value).getChangeListeners();
			for(int i=0;i<ac.length;i++){
				tmp.addChangeListener(ac[i]);
			}
			comp=tmp;
			val=value;
			return comp;
		}else if(value instanceof JLabel){
			JLabel tmp=(JLabel)value;
			comp=tmp;
			val=tmp;
			return comp;
		}else{
			val=value;
			comp=super.getTableCellEditorComponent(table,value,isSelected,row,column);
			return comp;
		}
	}
}
