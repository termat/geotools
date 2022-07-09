package net.termat.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.JTableHeader;

@SuppressWarnings("rawtypes")
public class RowHeaderList extends JList {
	  /**
	 *
	 */
	private static final long serialVersionUID = 1L;
    private final JTable table;
    private final ListSelectionModel tableSelection;
    private final ListSelectionModel rListSelection;
    @SuppressWarnings({ "unchecked" })
	public RowHeaderList(ListModel model, JTable _table) {
        super(model);
        this.table = _table;
        setFixedCellHeight(table.getRowHeight());
        setCellRenderer(new RowHeaderRenderer(table.getTableHeader()));
        //setSelectionModel(table.getSelectionModel());
        RollOverListener rol = new RollOverListener();
        addMouseListener(rol);
        addMouseMotionListener(rol);
        //setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.gray.brighter()));

        tableSelection = table.getSelectionModel();
        rListSelection = getSelectionModel();
    }

	class RowHeaderRenderer extends JLabel implements ListCellRenderer {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final JTableHeader header; // = table.getTableHeader();
        public RowHeaderRenderer(JTableHeader header) {
            this.header = header;
            this.setOpaque(true);
            //this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            this.setBorder(BorderFactory.createMatteBorder(0,0,1,2,Color.gray.brighter()));
            this.setHorizontalAlignment(CENTER);
            this.setForeground(header.getForeground());
            this.setBackground(header.getBackground());
            this.setFont(header.getFont());
        }
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(index==pressedRowIndex) {
                setBackground(Color.gray);
            }else if(index==rollOverRowIndex) {
                setBackground(Color.white);
            }else if(isSelected) {
                setBackground(Color.gray.brighter());
            }else{
                this.setForeground(header.getForeground());
                this.setBackground(header.getBackground());
            }
            this.setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    private int rollOverRowIndex = -1;
    private int pressedRowIndex = -1;
    class RollOverListener extends MouseInputAdapter {
        //@Override
        public void mouseExited(MouseEvent e) {
            pressedRowIndex  = -1;
            rollOverRowIndex = -1;
            repaint();
        }
        //@Override
        public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if( row != rollOverRowIndex ) {
                rollOverRowIndex = row;
                repaint();
            }
        }
        //@Override
        public void mouseDragged(MouseEvent e) {
            if(pressedRowIndex>=0) {
                int row   = locationToIndex(e.getPoint());
                int start = Math.min(row,pressedRowIndex);
                int end   = Math.max(row,pressedRowIndex);
                tableSelection.clearSelection();
                rListSelection.clearSelection();
                tableSelection.addSelectionInterval(start, end);
                rListSelection.addSelectionInterval(start, end);
                repaint();
            }
        }
        //@Override
        public void mousePressed(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if( row != pressedRowIndex ) {
                pressedRowIndex = row;
                repaint();
            }
            tableSelection.clearSelection();
            rListSelection.clearSelection();
            tableSelection.addSelectionInterval(row,row);
            rListSelection.addSelectionInterval(row,row);
        }
        //@Override
        public void mouseReleased(MouseEvent e) {
            rListSelection.clearSelection();
            pressedRowIndex  = -1;
            rollOverRowIndex = -1;
            repaint();
        }
    }
}
