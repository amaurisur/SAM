package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.basex.util.*;

/**
 * Project specific CheckBox implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXTable extends JTable {
  /** Table data. */
  public Table data;
  /** Dialog instance. */
  private final BaseXDialog dialog;
  /** Table model. */
  private final TableModel model;

  /**
   * Default constructor.
   * @param t table input
   * @param d dialog reference
   */
  public BaseXTable(final Table t, final BaseXDialog d) {
    data = t;
    dialog = d;
    model = new TableModel();
    setDefaultRenderer(Boolean.class, new CellRenderer());
    setModel(model);
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setResizingAllowed(false);
    BaseXLayout.addInteraction(this, d);

    getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent e) {
        if(!e.getValueIsAdjusting() && getSelectedColumn() != -1) {
          dialog.action(e.getSource());
        }
      }
    });
  }

  /**
   * Updates the table.
   * @param t table;
   */
  public void update(final Table t) {
    data = t;
    model.fireTableChanged(null);
    for(int i = 1; i < data.cols(); ++i) {
      final TableColumn tc = getColumnModel().getColumn(i);
      tc.setResizable(false);
      tc.setPreferredWidth(30);
    }
  }

  /**
   * Dialog specific table model.
   */
  final class TableModel extends AbstractTableModel {
    @Override
    public int getColumnCount() {
      return data.cols();
    }

    @Override
    public int getRowCount() {
      return data.rows();
    }

    @Override
    public String getColumnName(final int col) {
      return Token.string(data.header.get(col));
    }

    @Override
    public Object getValueAt(final int row, final int col) {
      final String o = data.value(row, col);
      return o.isEmpty() ? Boolean.FALSE : "X".equals(o) ? Boolean.TRUE : o;
    }

    @Override
    public Class<?> getColumnClass(final int c) {
      return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return col != 0 && !"admin".equals(data.value(row, 0));
    }

    @Override
    public void setValueAt(final Object value, final int row, final int col) {
      dialog.action(new Object[] { value, row, col });
    }
  }

  /**
   * Own Renderer for cells.
   */
  private final class CellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable tab, final Object val,
        final boolean sel, final boolean foc, final int row, final int col) {

      if(val instanceof Boolean) {
        final JCheckBox box = new JCheckBox();
        box.setHorizontalAlignment(SwingConstants.CENTER);
        box.setSelected((Boolean) val);
        box.setEnabled(isCellEditable(row, col));
        box.setOpaque(false);
        return box;
      }
      return super.getTableCellRendererComponent(tab, val, sel, foc, row, col);
    }
  }
}
