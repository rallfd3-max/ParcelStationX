package com.parcelstationx.ui.panel;

import com.parcelstationx.model.*;
import com.parcelstationx.service.UserService;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class UserPanel extends JPanel {
  private final DefaultTableModel model =
      new DefaultTableModel(new String[] {"ID", "账号", "姓名", "角色", "启用"}, 0);

  public UserPanel(UserService service) {
    setLayout(new BorderLayout());
    JPanel form = new JPanel(new GridLayout(2, 5, 6, 6));
    JTextField username = new JTextField(), password = new JTextField(), name = new JTextField();
    JComboBox<UserRole> role = new JComboBox<>(UserRole.values());
    JButton create = new JButton("新增员工");
    form.add(username);
    form.add(password);
    form.add(name);
    form.add(role);
    form.add(create);
    JTextField id = new JTextField();
    JCheckBox enabled = new JCheckBox("启用", true);
    JButton update = new JButton("更新状态"), refresh = new JButton("刷新");
    form.add(id);
    form.add(enabled);
    form.add(update);
    form.add(refresh);
    add(form, BorderLayout.NORTH);
    add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
    Runnable load =
        () -> {
          model.setRowCount(0);
          for (User u : service.findAll())
            model.addRow(
                new Object[] {u.id(), u.username(), u.displayName(), u.role(), u.enabled()});
        };
    create.addActionListener(
        e -> {
          try {
            service.create(
                username.getText(),
                password.getText(),
                name.getText(),
                (UserRole) role.getSelectedItem());
            load.run();
          } catch (RuntimeException x) {
            JOptionPane.showMessageDialog(this, x.getMessage());
          }
        });
    update.addActionListener(
        e -> {
          try {
            service.setEnabled(Long.parseLong(id.getText()), enabled.isSelected());
            load.run();
          } catch (RuntimeException x) {
            JOptionPane.showMessageDialog(this, x.getMessage());
          }
        });
    refresh.addActionListener(e -> load.run());
  }
}
