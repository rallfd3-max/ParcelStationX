package com.parcelstationx.ui.panel;

import com.parcelstationx.model.Parcel;
import com.parcelstationx.service.ParcelService;
import java.awt.BorderLayout;
import javax.swing.*;

public final class OutboundPanel extends JPanel {
  public OutboundPanel(ParcelService service, long operatorId) {
    setLayout(new BorderLayout(8, 8));
    JTextField code = new JTextField();
    JButton button = new JButton("确认出库");
    add(code, BorderLayout.NORTH);
    add(button, BorderLayout.SOUTH);
    button.addActionListener(
        e ->
            new SwingWorker<Parcel, Void>() {
              protected Parcel doInBackground() {
                return service.outbound(code.getText().trim(), operatorId);
              }

              protected void done() {
                try {
                  JOptionPane.showMessageDialog(OutboundPanel.this, "出库成功：" + get().trackingNo());
                } catch (Exception ex) {
                  JOptionPane.showMessageDialog(OutboundPanel.this, ex.getCause().getMessage());
                }
              }
            }.execute());
  }
}
