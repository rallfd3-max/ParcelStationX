package com.parcelstationx.ui.panel;

import com.parcelstationx.service.*;
import com.parcelstationx.ui.component.BarChartPanel;
import java.awt.BorderLayout;
import javax.swing.*;

public final class StatisticsPanel extends JPanel {
  public StatisticsPanel(StatisticsService service) {
    setLayout(new BorderLayout());
    BarChartPanel chart = new BarChartPanel();
    JLabel summary = new JLabel("正在加载统计...");
    add(summary, BorderLayout.NORTH);
    add(chart, BorderLayout.CENTER);
    new SwingWorker<StatisticsSnapshot, Void>() {
      protected StatisticsSnapshot doInBackground() {
        return service.calculate();
      }

      protected void done() {
        try {
          StatisticsSnapshot s = get();
          summary.setText(
              "库存 "
                  + s.inventory()
                  + " | 滞留 "
                  + s.overdue()
                  + " | 平均滞留 "
                  + Math.round(s.averageStayHours())
                  + " 小时 | 异常 "
                  + s.exceptions());
          chart.setValues(s.courierVolumes());
        } catch (Exception e) {
          summary.setText("统计加载失败");
        }
      }
    }.execute();
  }
}
