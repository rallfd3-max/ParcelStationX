package com.parcelstationx.ui.component;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class SimpleTablePanel extends JPanel {
  public SimpleTablePanel(String... columns) {
    setLayout(new BorderLayout());
    DefaultTableModel model =
        new DefaultTableModel(columns, 0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    add(new JTextField("搜索", 18), BorderLayout.NORTH);
    add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
  }
}
