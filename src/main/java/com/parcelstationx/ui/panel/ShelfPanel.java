package com.parcelstationx.ui.panel;

import com.parcelstationx.service.ShelfService;
import java.awt.GridLayout;
import javax.swing.*;

public final class ShelfPanel extends JPanel {
  public ShelfPanel(ShelfService service) {
    setLayout(new GridLayout(0, 2, 8, 8));
    JTextField code = new JTextField(),
        zone = new JTextField(),
        capacity = new JTextField(),
        id = new JTextField();
    add(new JLabel("货架 ID（修改时填写）"));
    add(id);
    add(new JLabel("货架编号"));
    add(code);
    add(new JLabel("区域"));
    add(zone);
    add(new JLabel("容量"));
    add(capacity);
    JButton save = new JButton("新增货架");
    add(new JLabel());
    add(save);
    save.addActionListener(
        e -> {
          try {
            service.create(code.getText(), zone.getText(), Integer.parseInt(capacity.getText()));
            JOptionPane.showMessageDialog(this, "货架已新增");
          } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
          }
        });
    JComboBox<com.parcelstationx.model.ShelfStatus> status =
        new JComboBox<>(com.parcelstationx.model.ShelfStatus.values());
    add(new JLabel("状态"));
    add(status);
    JButton update = new JButton("修改容量/状态");
    add(new JLabel());
    add(update);
    update.addActionListener(
        e -> {
          try {
            service.update(
                Long.parseLong(id.getText()),
                Integer.parseInt(capacity.getText()),
                (com.parcelstationx.model.ShelfStatus) status.getSelectedItem());
            JOptionPane.showMessageDialog(this, "货架已修改");
          } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
          }
        });
  }
}
