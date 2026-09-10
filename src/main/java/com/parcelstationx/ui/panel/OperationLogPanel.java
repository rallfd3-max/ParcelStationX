package com.parcelstationx.ui.panel;

import com.parcelstationx.dao.OperationLogDao;
import com.parcelstationx.model.OperationLog;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class OperationLogPanel extends JPanel {
  public OperationLogPanel(OperationLogDao dao) {
    setLayout(new BorderLayout());
    DefaultTableModel model = new DefaultTableModel(new String[] {"时间", "操作", "对象", "说明"}, 0);
    JButton refresh = new JButton("刷新日志");
    add(refresh, BorderLayout.NORTH);
    add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
    refresh.addActionListener(
        e -> {
          model.setRowCount(0);
          for (OperationLog log : dao.findAll())
            model.addRow(
                new Object[] {
                  log.createdAt(), log.operationType(), log.targetId(), log.description()
                });
        });
  }
}
