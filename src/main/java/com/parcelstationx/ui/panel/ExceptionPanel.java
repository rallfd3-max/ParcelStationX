package com.parcelstationx.ui.panel;

import com.parcelstationx.model.*;
import com.parcelstationx.service.ExceptionService;
import java.awt.GridLayout;
import javax.swing.*;

public final class ExceptionPanel extends JPanel {
  public ExceptionPanel(ExceptionService service, long userId) {
    setLayout(new GridLayout(0, 2, 8, 8));
    JTextField parcel = new JTextField(),
        description = new JTextField(),
        record = new JTextField(),
        resolution = new JTextField();
    JComboBox<ExceptionType> type = new JComboBox<>(ExceptionType.values());
    JComboBox<ParcelStatus> target =
        new JComboBox<>(new ParcelStatus[] {ParcelStatus.IN_STOCK, ParcelStatus.RETURNED});
    add(new JLabel("快件 ID"));
    add(parcel);
    add(new JLabel("异常类型"));
    add(type);
    add(new JLabel("描述"));
    add(description);
    JButton create = new JButton("登记异常");
    add(new JLabel());
    add(create);
    add(new JLabel("异常记录 ID"));
    add(record);
    add(new JLabel("处理结果"));
    add(target);
    add(new JLabel("处理说明"));
    add(resolution);
    JButton resolve = new JButton("完成处理");
    add(new JLabel());
    add(resolve);
    create.addActionListener(
        e -> {
          try {
            service.create(
                Long.parseLong(parcel.getText()),
                (ExceptionType) type.getSelectedItem(),
                description.getText(),
                userId);
            JOptionPane.showMessageDialog(this, "异常已登记");
          } catch (RuntimeException x) {
            JOptionPane.showMessageDialog(this, x.getMessage());
          }
        });
    resolve.addActionListener(
        e -> {
          try {
            service.resolve(
                Long.parseLong(record.getText()),
                (ParcelStatus) target.getSelectedItem(),
                resolution.getText(),
                userId);
            JOptionPane.showMessageDialog(this, "异常已处理");
          } catch (RuntimeException x) {
            JOptionPane.showMessageDialog(this, x.getMessage());
          }
        });
  }
}
