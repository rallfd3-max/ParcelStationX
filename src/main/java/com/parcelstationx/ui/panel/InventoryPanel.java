package com.parcelstationx.ui.panel;

import com.parcelstationx.dao.ParcelDao;
import com.parcelstationx.model.Parcel;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class InventoryPanel extends JPanel {
  private final DefaultTableModel model =
      new DefaultTableModel(new String[] {"运单号", "取件码", "状态", "货架"}, 0);

  public InventoryPanel(ParcelDao dao) {
    setLayout(new BorderLayout());
    JButton refresh = new JButton("刷新");
    add(refresh, BorderLayout.NORTH);
    add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
    refresh.addActionListener(
        e ->
            new SwingWorker<List<Parcel>, Void>() {
              protected List<Parcel> doInBackground() {
                return dao.findAll();
              }

              protected void done() {
                try {
                  model.setRowCount(0);
                  for (Parcel p : get())
                    model.addRow(
                        new Object[] {p.trackingNo(), p.pickupCode(), p.status(), p.shelfId()});
                } catch (Exception ex) {
                  JOptionPane.showMessageDialog(InventoryPanel.this, "加载失败");
                }
              }
            }.execute());
  }
}
