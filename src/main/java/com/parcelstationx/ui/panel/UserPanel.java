package com.parcelstationx.ui.panel;

import com.parcelstationx.dao.UserDao;
import com.parcelstationx.model.User;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class UserPanel extends JPanel {
  public UserPanel(UserDao dao) {
    setLayout(new BorderLayout());
    DefaultTableModel model = new DefaultTableModel(new String[] {"账号", "姓名", "角色", "启用"}, 0);
    JButton refresh = new JButton("刷新员工");
    add(refresh, BorderLayout.NORTH);
    add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
    refresh.addActionListener(
        e -> {
          model.setRowCount(0);
          for (User u : dao.findAll())
            model.addRow(new Object[] {u.username(), u.displayName(), u.role(), u.enabled()});
        });
  }
}
