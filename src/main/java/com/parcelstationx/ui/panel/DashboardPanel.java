package com.parcelstationx.ui.panel;

import com.parcelstationx.service.*;
import java.awt.GridLayout;
import javax.swing.*;

public final class DashboardPanel extends JPanel {
  private final JLabel inbound = new JLabel(),
      outbound = new JLabel(),
      stock = new JLabel(),
      exceptions = new JLabel();

  public DashboardPanel(StatisticsService service) {
    setLayout(new GridLayout(2, 2, 8, 8));
    add(inbound);
    add(outbound);
    add(stock);
    add(exceptions);
    new SwingWorker<StatisticsSnapshot, Void>() {
      protected StatisticsSnapshot doInBackground() {
        return service.calculate();
      }

      protected void done() {
        try {
          StatisticsSnapshot s = get();
          inbound.setText("今日入库：" + s.todayInbound());
          outbound.setText("今日出库：" + s.todayOutbound());
          stock.setText("当前库存：" + s.inventory());
          exceptions.setText("异常件：" + s.exceptions());
        } catch (Exception e) {
          inbound.setText("统计加载失败");
        }
      }
    }.execute();
  }
}
