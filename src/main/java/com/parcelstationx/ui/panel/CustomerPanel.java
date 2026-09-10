package com.parcelstationx.ui.panel;

import com.parcelstationx.service.CustomerService;
import java.awt.GridLayout;
import javax.swing.*;

public final class CustomerPanel extends JPanel {
  public CustomerPanel(CustomerService service) {
    setLayout(new GridLayout(0, 2, 8, 8));
    JTextField name = new JTextField(),
        mobile = new JTextField(),
        building = new JTextField(),
        room = new JTextField(),
        id = new JTextField();
    add(new JLabel("客户 ID（修改时填写）"));
    add(id);
    add(new JLabel("姓名"));
    add(name);
    add(new JLabel("手机号"));
    add(mobile);
    add(new JLabel("楼栋"));
    add(building);
    add(new JLabel("房间"));
    add(room);
    JButton save = new JButton("新增客户");
    add(new JLabel());
    add(save);
    save.addActionListener(
        e -> {
          try {
            service.create(
                name.getText(), mobile.getText(), building.getText(), room.getText(), "");
            JOptionPane.showMessageDialog(this, "客户已新增");
          } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
          }
        });
    JButton update = new JButton("修改客户");
    add(new JLabel());
    add(update);
    update.addActionListener(
        e -> {
          try {
            service.update(
                Long.parseLong(id.getText()),
                name.getText(),
                mobile.getText(),
                building.getText(),
                room.getText(),
                "");
            JOptionPane.showMessageDialog(this, "客户已修改");
          } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
          }
        });
  }
}
