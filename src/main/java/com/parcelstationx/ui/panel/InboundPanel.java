package com.parcelstationx.ui.panel;

import com.parcelstationx.exception.AppException;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.service.InboundRequest;
import com.parcelstationx.service.ParcelService;
import java.awt.GridLayout;
import javax.swing.*;

public final class InboundPanel extends JPanel {
  public InboundPanel(ParcelService service, long operatorId) {
    setLayout(new GridLayout(0, 2, 8, 8));
    JTextField tracking = new JTextField(), courier = new JTextField(), mobile = new JTextField();
    JTextField shelf = new JTextField(), remark = new JTextField();
    add(new JLabel("运单号"));
    add(tracking);
    add(new JLabel("快递公司"));
    add(courier);
    add(new JLabel("客户手机号"));
    add(mobile);
    add(new JLabel("货架 ID（空为自动）"));
    add(shelf);
    add(new JLabel("备注"));
    add(remark);
    JButton submit = new JButton("入库");
    add(new JLabel());
    add(submit);
    submit.addActionListener(
        e ->
            new SwingWorker<Parcel, Void>() {
              protected Parcel doInBackground() {
                Long shelfId = shelf.getText().isBlank() ? null : Long.valueOf(shelf.getText());
                return service.inbound(
                    new InboundRequest(
                        tracking.getText().trim(),
                        courier.getText().trim(),
                        mobile.getText().trim(),
                        shelfId,
                        operatorId,
                        remark.getText()));
              }

              protected void done() {
                try {
                  Parcel p = get();
                  JOptionPane.showMessageDialog(InboundPanel.this, "入库成功，取件码：" + p.pickupCode());
                } catch (Exception ex) {
                  showError(ex);
                }
              }
            }.execute());
  }

  private void showError(Exception exception) {
    Throwable cause = exception.getCause();
    JOptionPane.showMessageDialog(
        this,
        cause instanceof AppException ? cause.getMessage() : "入库失败。",
        "错误",
        JOptionPane.WARNING_MESSAGE);
  }
}
